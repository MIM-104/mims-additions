package com.example.modid.client;

import com.example.modid.Tags;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.client.FluidContainerColorer;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IRegistryDelegate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class HotFluidBucketColorHandler {

    private static final FluidContainerColorer FLUID_COLORER = new FluidContainerColorer();

    private HotFluidBucketColorHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemColors(ColorHandlerEvent.Item event) {
        ItemColors itemColors = event.getItemColors();
        Map<IRegistryDelegate<Item>, IItemColor> snapshot = new HashMap<>(getColorMap(itemColors));

        for (Item item : ForgeRegistries.ITEMS) {
            IItemColor existing = snapshot.get(item.delegate);
            itemColors.registerItemColorHandler(createHeatColorHandler(existing), item);
        }
    }

    private static IItemColor createHeatColorHandler(IItemColor fallback) {
        return (stack, tintIndex) -> {
            if (tintIndex == 1) {
                return fallback == null
                        ? FLUID_COLORER.colorMultiplier(stack, tintIndex)
                        : fallback.colorMultiplier(stack, tintIndex);
            }
            if (tintIndex != 0 && tintIndex != 2) {
                return fallback == null ? -1 : fallback.colorMultiplier(stack, tintIndex);
            }

            float ratio = HotFluidBucketHeatTracker.getHeatRatio(stack);
            if (ratio <= 0.0f) {
                return fallback == null ? -1 : fallback.colorMultiplier(stack, tintIndex);
            }
            return getHeatColor(ratio);
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<IRegistryDelegate<Item>, IItemColor> getColorMap(ItemColors itemColors) {
        try {
            return (Map<IRegistryDelegate<Item>, IItemColor>) findColorMapField().get(itemColors);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to read the item color handlers from ItemColors", e);
        }
    }

    private static Field findColorMapField() {
        try {
            return ReflectionHelper.findField(ItemColors.class, "itemColorMap", "field_186732_a");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Field match = null;
            for (Field field : ItemColors.class.getDeclaredFields()) {
                if (!Map.class.isAssignableFrom(field.getType()) || !hasColorMapTypes(field.getGenericType())) {
                    continue;
                }
                if (match != null) {
                    throw new IllegalStateException("Multiple item color handler maps found in ItemColors", e);
                }
                match = field;
            }

            if (match == null) {
                throw new IllegalStateException("Unable to locate the item color handler map in ItemColors", e);
            }
            match.setAccessible(true);
            return match;
        }
    }

    private static boolean hasColorMapTypes(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
        return arguments.length == 2
                && hasRawType(arguments[0], IRegistryDelegate.class)
                && hasRawType(arguments[1], IItemColor.class);
    }

    private static boolean hasRawType(Type type, Class<?> expectedType) {
        if (type instanceof Class<?>) {
            return type == expectedType;
        }
        return type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == expectedType;
    }

    public static int getHeatColor(float ratio) {
        if (ratio < 0.4f) {
            return lerpColor(0xFFFFFF, 0xFF4040, ratio / 0.4f);
        }
        if (ratio < 0.75f) {
            return lerpColor(0xFF4040, 0xFFB040, (ratio - 0.4f) / 0.35f);
        }
        return lerpColor(0xFFB040, 0xFFFFFF, (ratio - 0.75f) / 0.25f);
    }

    private static int lerpColor(int from, int to, float t) {
        t = clamp01(t);
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int r = fr + Math.round((tr - fr) * t);
        int g = fg + Math.round((tg - fg) * t);
        int b = fb + Math.round((tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }
}
