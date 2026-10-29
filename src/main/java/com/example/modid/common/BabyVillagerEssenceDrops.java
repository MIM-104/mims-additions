package com.example.modid.common;

import com.example.modid.Tags;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class BabyVillagerEssenceDrops {

    private BabyVillagerEssenceDrops() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntityLiving() instanceof EntityVillager)) {
            return;
        }

        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        if (!villager.isChild()) {
            return;
        }

        if (event.getEntityLiving().world.rand.nextFloat() > 0.7f) {
            return;
        }

        ItemStack drop = new ItemStack(ModItems.babyVillagerEssence, 1);
        EntityItem entityItem =
                new EntityItem(event.getEntityLiving().world, villager.posX, villager.posY, villager.posZ, drop);
        event.getDrops().add(entityItem);
    }
}
