package com.example.modid.firmware.client;

import static com.example.modid.firmware.FirmwareWorkbenchLayout.*;

import com.example.modid.firmware.*;
import com.example.modid.firmware.FirmwareWorkbenchLayout.Rect;

import gregtech.common.items.MetaItems;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public final class FirmwareWorkbenchGui extends GuiContainer {
    private static List<FirmwareTarget> catalog = Collections.emptyList();
    private final FirmwareWorkbenchTile bench;
    private GuiTextField search;
    private int page;
    private String lastSearch = "", cachedQuery = "";
    private List<FirmwareTarget> cachedCatalog, cachedMatches = Collections.emptyList();
    private static final int INK = 0xFFE0E3DF, MUTED = 0xFF95A19E, ACCENT = 0xFF8DC4AF, COPPER = 0xFFC9AC71;

    public FirmwareWorkbenchGui(InventoryPlayer inventory, FirmwareWorkbenchTile bench) {
        super(new FirmwareContainer(inventory, bench));
        this.bench = bench;
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    public static void receiveCatalog(NBTTagCompound data) {
        List<FirmwareTarget> received = new ArrayList<>();
        if (data != null) {
            NBTTagList list = data.getTagList("Targets", 10);
            for (int i = 0; i < list.tagCount(); i++) received.add(FirmwareTarget.read(list.getCompoundTagAt(i)));
        }
        catalog = received;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        search = new GuiTextField(0, fontRenderer, guiLeft + SEARCH.x, guiTop + SEARCH.y, SEARCH.w, SEARCH.h);
        search.setMaxStringLength(64);
        search.setText(lastSearch);
        search.setTextColor(0xDEE4DD);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (search != null) search.updateCursorCounter();
    }

    private List<FirmwareTarget> filtered() {
        String query = (search == null ? lastSearch : search.getText()).toLowerCase(Locale.ROOT).trim();
        if (cachedCatalog != catalog || !cachedQuery.equals(query)) {
            cachedCatalog = catalog;
            cachedQuery = query;
            cachedMatches = new ArrayList<>();
            for (FirmwareTarget target : catalog)
                if (query.isEmpty()
                        || (target.name + " " + target.kind + " " + target.key)
                                .toLowerCase(Locale.ROOT)
                                .contains(query)) cachedMatches.add(target);
        }
        page = Math.max(0, Math.min(page, pages() - 1));
        return cachedMatches;
    }

    private int pages() {
        return Math.max(1, (cachedMatches.size() + 3) / 4);
    }

    private FirmwareTarget selected() {
        for (FirmwareTarget target : catalog) if (target.key.equals(bench.selectedTarget)) return target;
        return null;
    }

    private void send(int action, int value, String target) {
        FirmwareNetwork.CHANNEL.sendToServer(new FirmwareNetwork.Action(bench.getPos(), action, value, target));
    }

    private void box(int x, int y, int w, int h, int color) {
        drawRect(guiLeft + x, guiTop + y, guiLeft + x + w, guiTop + y + h, color);
    }

    private void fill(Rect r, int color) {
        box(r.x, r.y, r.w, r.h, color);
    }

    private void text(String s, int x, int y, int color) {
        fontRenderer.drawString(s, guiLeft + x, guiTop + y, color);
    }

    private String trim(String s, int width) {
        return fontRenderer.trimStringToWidth(s, width);
    }

    private void panel(int x, int y, int w, int h) {
        box(x, y, w, h, 0xFF101719);
        box(x + 1, y + 1, w - 2, h - 2, 0xFF253032);
        box(x + 1, y + 1, w - 2, 1, 0xFF54615E);
    }

    private void button(Rect r, String label, boolean enabled, int mx, int my) {
        fill(r, 0xFF11191B);
        box(
                r.x + 1,
                r.y + 1,
                r.w - 2,
                r.h - 2,
                enabled && r.contains(mx, my) ? 0xFF52665E : enabled ? 0xFF3B4C46 : 0xFF2C3634);
        box(r.x + 1, r.y + 1, r.w - 2, 1, enabled ? 0xFF8B9E8F : 0xFF48554E);
        text(label, r.x + (r.w - fontRenderer.getStringWidth(label)) / 2, r.y + 3, enabled ? INK : 0xFF718078);
    }

    private int storedIndex(int visible) {
        return FirmwareContainer.STORAGE_SLOTS[visible];
    }

    private ItemStack ghost(int slot) {
        switch (slot) {
            case FirmwareWorkbenchTile.CIRCUIT:
                return MetaItems.ELECTRONIC_CIRCUIT_LV.getStackForm();
            case FirmwareWorkbenchTile.IRON:
                return new ItemStack(FirmwareContent.SOLDERING_IRON);
            case FirmwareWorkbenchTile.STRIPPERS:
                return new ItemStack(FirmwareContent.WIRE_STRIPPERS);
            case FirmwareWorkbenchTile.WIRE:
                return bench.getWorld() != null && (bench.getWorld().getTotalWorldTime() / 30) % 2 == 0
                        ? FirmwareMaterials.tinWire()
                        : FirmwareMaterials.solder();
            default:
                return slot >= 6 && slot < 6 + bench.branches() ? bench.requirement(slot - 6) : ItemStack.EMPTY;
        }
    }

    private void icon(ItemStack stack, int x, int y, boolean faded) {
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(stack, guiLeft + x, guiTop + y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        if (faded) {
            GlStateManager.disableDepth();
            box(x, y, 16, 16, 0xB0253032);
            GlStateManager.enableDepth();
        }
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int mx = mouseX - guiLeft, my = mouseY - guiTop;
        GlStateManager.color(1, 1, 1, 1);
        box(0, 0, xSize, ySize, 0xFF111819);
        box(1, 1, xSize - 2, ySize - 2, 0xFF394441);
        box(2, 2, xSize - 4, 1, 0xFF819086);
        box(3, 3, xSize - 6, 16, 0xFF26312E);
        text("Firmware Workbench", 10, 7, INK);
        box(143, 6, 1, 10, 0xFF687C70);
        FirmwareTarget current = selected();
        text(trim(current == null ? "Select firmware" : current.name, 157), 151, 7, current == null ? MUTED : ACCENT);

        fill(BOARD, 0xFF101B18);
        box(BOARD.x + 1, BOARD.y + 1, BOARD.w - 2, BOARD.h - 2, 0xFF243B30);
        for (int x = 14; x < 150; x += 12) box(x, 24, 1, 112, 0xFF294236);
        for (int y = 26; y < 135; y += 12) box(10, y, 142, 1, 0xFF294236);
        int ox = FirmwareContainer.BOARD_X, oy = FirmwareContainer.BOARD_Y, cell = FirmwareContainer.CELL;
        box(ox + 5 * cell - 17, oy + 3 * cell - 17, 34, 34, 0xFF102119);
        box(ox + 5 * cell - 16, oy + 3 * cell - 16, 32, 32, 0xFF50634A);
        for (int branch = 0; branch < bench.branches(); branch++) {
            int color = 0xFF000000 | FirmwarePuzzle.COLORS[branch];
            boolean connected = FirmwarePuzzle.connected(branch, bench.rotations);
            int trace = bench.complete(branch) ? color : connected ? 0xFFE1CC91 : 0xFF9B9770;
            for (int node = 0; node < FirmwarePuzzle.PATHS[branch].length; node++) {
                int[] at = FirmwarePuzzle.PATHS[branch][node];
                int px = ox + at[0] * cell, py = oy + at[1] * cell;
                box(px - 5, py - 5, 10, 10, 0xFF1B3025);
                int mask =
                        FirmwarePuzzle.rotatedMask(
                                FirmwarePuzzle.goalMask(branch, node), bench.rotations[branch * 5 + node]);
                if ((mask & 1) != 0) box(px - 1, py - 6, 2, 6, trace);
                if ((mask & 2) != 0) box(px, py - 1, 6, 2, trace);
                if ((mask & 4) != 0) box(px - 1, py, 2, 6, trace);
                if ((mask & 8) != 0) box(px - 6, py - 1, 6, 2, trace);
                box(px - 1, py - 1, 2, 2, COPPER);
                if (new Rect(px - 5, py - 5, 10, 10).contains(mx, my) && !bench.complete(branch))
                    box(px - 5, py - 5, 10, 1, INK);
            }
            int[] port = FirmwarePuzzle.PORTS[branch];
            box(ox + port[0] * cell - 2, oy + port[1] * cell - 2, 4, 4, bench.complete(branch) ? color : COPPER);
            int[] terminal = FirmwarePuzzle.TERMINALS[branch];
            box(
                    ox + terminal[0] * cell - 10,
                    oy + terminal[1] * cell - 10,
                    20,
                    20,
                    bench.complete(branch) ? color : connected ? 0xFFB2B381 : 0xFF526951);
        }
        button(RESET, "RESET", bench.branches() > 0, mx, my);
        if (bench.branches() == 0) text("Insert a circuit", 30, 94, MUTED);

        panel(CATALOG.x, CATALOG.y, CATALOG.w, CATALOG.h);
        search.drawTextBox();
        if (search.getText().isEmpty() && !search.isFocused()) text("Search firmware", 172, 29, MUTED);
        List<FirmwareTarget> matches = filtered();
        for (int i = 0; i < 4; i++) {
            int at = page * 4 + i;
            if (at >= matches.size()) break;
            FirmwareTarget target = matches.get(at);
            Rect row = row(i);
            boolean active = target.key.equals(bench.selectedTarget);
            fill(row, active ? 0xFF465D50 : row.contains(mx, my) ? 0xFF36473D : 0xFF2A3732);
            if (active) box(row.x, row.y, 2, row.h, ACCENT);
            icon(target.icon, row.x + 3, row.y + 1, false);
            text(trim(target.name, 116), row.x + 22, row.y + 1, active ? ACCENT : INK);
            text(trim(target.kind, 116), row.x + 22, row.y + 10, MUTED);
        }
        if (matches.isEmpty()) text("No matching firmware", 172, 68, MUTED);
        button(PREVIOUS, "<", page > 0, mx, my);
        button(NEXT, ">", page + 1 < pages(), mx, my);
        String pageText = (page + 1) + " / " + pages();
        text(pageText, 238 - fontRenderer.getStringWidth(pageText) / 2, 123, MUTED);

        panel(8, 142, 68, 92);
        panel(78, 142, 166, 92);
        panel(250, 142, 62, 92);
        text("TOOLS", 12, 146, MUTED);
        text("WIRE", 12, 184, MUTED);
        text("OUTPUT", 254, 146, MUTED);
        text(trim(bench.status, 160), 80, 146, MUTED);
        int wear = FirmwareMaterials.solderWear(bench.items.getStackInSlot(FirmwareWorkbenchTile.WIRE));
        if (wear > 0) {
            text(wear == 2 ? "TIN" : "ALLOY", 34, 198, COPPER);
            text(wear + "/tool", 34, 209, MUTED);
        } else {
            text("Tin or", 34, 198, MUTED);
            text("alloy", 34, 209, MUTED);
        }
        button(SOLDER, "SOLDER", bench.nextSolderableBranch() >= 0, mx, my);
        button(FLASH, "FLASH", bench.canFlash(), mx, my);
        String progress = Integer.bitCount(bench.soldered) + " / " + bench.branches();
        text(progress, 281 - fontRenderer.getStringWidth(progress) / 2, 220, bench.branches() > 0 ? ACCENT : MUTED);
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            if (!slot.isEnabled()) continue;
            box(slot.xPos - 1, slot.yPos - 1, 18, 18, 0xFF101817);
            box(slot.xPos, slot.yPos, 16, 16, 0xFF1D2824);
            box(slot.xPos, slot.yPos, 16, 1, 0xFF52675B);
            if (i < FirmwareContainer.BENCH_SLOTS && !slot.getHasStack()) {
                ItemStack ghost = ghost(storedIndex(i));
                if (!ghost.isEmpty()) icon(ghost, slot.xPos, slot.yPos, true);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        int mx = mouseX - guiLeft, my = mouseY - guiTop;
        for (int i = 0; i < FirmwareContainer.BENCH_SLOTS; i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            if (!slot.isEnabled() || slot.getHasStack() || !new Rect(slot.xPos, slot.yPos, 16, 16).contains(mx, my))
                continue;
            int stored = storedIndex(i);
            ItemStack ghost = ghost(stored);
            if (ghost.isEmpty()) continue;
            if (stored == FirmwareWorkbenchTile.WIRE)
                drawHoveringText(
                        Arrays.asList(
                                "Fine Tin or Fine Soldering Alloy Wire",
                                "One supply for soldering and flashing.",
                                "Tin: 2 durability per tool per branch",
                                "Alloy: 1 durability per tool per branch",
                                "Flashing uses one extra wire."),
                        mouseX,
                        mouseY);
            else if (stored >= 6)
                drawHoveringText(Arrays.asList("Branch " + (stored - 5), ghost.getDisplayName()), mouseX, mouseY);
            else
                drawHoveringText(
                        stored == 0 ? "Unconfigured circuit / any tier" : ghost.getDisplayName(), mouseX, mouseY);
        }
        if (SOLDER.contains(mx, my)) {
            int next = bench.nextSolderableBranch();
            drawHoveringText(
                    next >= 0
                            ? Arrays.asList(
                                    "Solder branch " + (next + 1), "Uses its component, one wire and tool durability.")
                            : Arrays.asList(
                                    "Solder the next ready branch",
                                    "Connect a path and insert its component,",
                                    "both tools, and Tin or Soldering Alloy Wire."),
                    mouseX,
                    mouseY);
        }
        if (FLASH.contains(mx, my))
            drawHoveringText(
                    Arrays.asList(
                            "Flash the completed board",
                            "Solder every branch and leave one wire in the supply.",
                            "Collect the previous output before flashing again."),
                    mouseX,
                    mouseY);
        if (RESET.contains(mx, my))
            drawHoveringText(
                    Arrays.asList("Reset the board", "Soldered components and wire are not refunded."), mouseX, mouseY);
        if (STATUS.contains(mx, my))
            drawHoveringText(fontRenderer.listFormattedStringToWidth(bench.status, 220), mouseX, mouseY);
        FirmwareTarget current = selected();
        if (current != null && new Rect(151, 4, 157, 14).contains(mx, my))
            drawHoveringText(Arrays.asList(current.name, TextFormatting.GRAY + current.kind), mouseX, mouseY);
        List<FirmwareTarget> targets = filtered();
        for (int i = 0; i < 4; i++)
            if (row(i).contains(mx, my) && page * 4 + i < targets.size()) {
                FirmwareTarget target = targets.get(page * 4 + i);
                drawHoveringText(Arrays.asList(target.name, TextFormatting.GRAY + target.kind), mouseX, mouseY);
            }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (search.mouseClicked(mouseX, mouseY, button)) return;
        int mx = mouseX - guiLeft, my = mouseY - guiTop;
        List<FirmwareTarget> matches = filtered();
        if (button == 0) {
            if (PREVIOUS.contains(mx, my)) {
                page = Math.max(0, page - 1);
                return;
            }
            if (NEXT.contains(mx, my)) {
                page = Math.min(pages() - 1, page + 1);
                return;
            }
            for (int i = 0; i < 4; i++)
                if (row(i).contains(mx, my) && page * 4 + i < matches.size()) {
                    send(0, 0, matches.get(page * 4 + i).key);
                    return;
                }
            if (SOLDER.contains(mx, my)) {
                int next = bench.nextSolderableBranch();
                if (next >= 0) send(2, next, "");
                return;
            }
            if (FLASH.contains(mx, my)) {
                if (bench.canFlash()) send(3, 0, "");
                return;
            }
            if (RESET.contains(mx, my)) {
                if (bench.branches() > 0) send(4, 0, "");
                return;
            }
            boolean onSlot = false;
            for (Slot slot : inventorySlots.inventorySlots)
                if (slot.isEnabled() && new Rect(slot.xPos, slot.yPos, 16, 16).contains(mx, my)) onSlot = true;
            if (!onSlot)
                for (int b = 0; b < bench.branches(); b++)
                    for (int n = 0; n < FirmwarePuzzle.PATHS[b].length; n++) {
                        int[] at = FirmwarePuzzle.PATHS[b][n];
                        if (!bench.complete(b)
                                && new Rect(
                                                FirmwareContainer.BOARD_X + at[0] * 12 - 5,
                                                FirmwareContainer.BOARD_Y + at[1] * 12 - 5,
                                                10,
                                                10)
                                        .contains(mx, my)) {
                            send(1, b * 5 + n, "");
                            return;
                        }
                    }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mx = Mouse.getEventX() * width / mc.displayWidth - guiLeft;
        int my = height - Mouse.getEventY() * height / mc.displayHeight - 1 - guiTop;
        int wheel = Mouse.getEventDWheel();
        if (CATALOG.contains(mx, my) && wheel != 0) {
            filtered();
            page = Math.max(0, Math.min(pages() - 1, page + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        if (search.isFocused() && key != Keyboard.KEY_ESCAPE) {
            if (search.textboxKeyTyped(character, key)) {
                lastSearch = search.getText();
                page = 0;
            }
            return;
        }
        super.keyTyped(character, key);
    }
}
