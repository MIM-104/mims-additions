package com.example.modid.firmware;

import com.example.modid.MimsAdditions;
import com.example.modid.firmware.client.FirmwareWorkbenchGui;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class FirmwareNetwork implements IGuiHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("mim_firmware");

    public static void init(MimsAdditions mod) {
        CHANNEL.registerMessage(ActionHandler.class, Action.class, 0, Side.SERVER);
        CHANNEL.registerMessage(CatalogHandler.class, Catalog.class, 1, Side.CLIENT);
        NetworkRegistry.INSTANCE.registerGuiHandler(mod, new FirmwareNetwork());
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != 41 || !(world.getTileEntity(new BlockPos(x, y, z)) instanceof FirmwareWorkbenchTile)) return null;
        FirmwareWorkbenchTile bench = (FirmwareWorkbenchTile) world.getTileEntity(new BlockPos(x, y, z));
        CHANNEL.sendTo(Catalog.snapshot(), (EntityPlayerMP) player);
        return new FirmwareContainer(player.inventory, bench);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != 41 || !(world.getTileEntity(new BlockPos(x, y, z)) instanceof FirmwareWorkbenchTile)) return null;
        return new FirmwareWorkbenchGui(
                player.inventory, (FirmwareWorkbenchTile) world.getTileEntity(new BlockPos(x, y, z)));
    }

    public static final class Action implements IMessage {
        private BlockPos pos;
        private int action, value;
        private String target;

        public Action() {}

        public Action(BlockPos pos, int action, int value, String target) {
            this.pos = pos;
            this.action = action;
            this.value = value;
            this.target = target;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(pos.toLong());
            buf.writeInt(action);
            buf.writeInt(value);
            ByteBufUtils.writeUTF8String(buf, target);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            pos = BlockPos.fromLong(buf.readLong());
            action = buf.readInt();
            value = buf.readInt();
            target = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static final class ActionHandler implements IMessageHandler<Action, IMessage> {
        @Override
        public IMessage onMessage(Action message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld()
                    .addScheduledTask(
                            () -> {
                                if (!(player.openContainer instanceof FirmwareContainer)) return;
                                FirmwareContainer container = (FirmwareContainer) player.openContainer;
                                if (!container.canInteractWith(player) || !container.bench.getPos().equals(message.pos))
                                    return;
                                if (message.target.length() > 256) return;
                                container.bench.action(player, message.action, message.value, message.target);
                            });
            return null;
        }
    }

    public static final class Catalog implements IMessage {
        private NBTTagCompound data;

        public Catalog() {
            data = new NBTTagCompound();
        }

        public static Catalog snapshot() {
            Catalog result = new Catalog();
            NBTTagList list = new NBTTagList();
            for (FirmwareTarget target : FirmwareTargets.all()) list.appendTag(target.write());
            result.data.setTag("Targets", list);
            return result;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeTag(buf, data);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            data = ByteBufUtils.readTag(buf);
        }
    }

    public static final class CatalogHandler implements IMessageHandler<Catalog, IMessage> {
        @Override
        public IMessage onMessage(Catalog message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> FirmwareWorkbenchGui.receiveCatalog(message.data));
            return null;
        }
    }
}
