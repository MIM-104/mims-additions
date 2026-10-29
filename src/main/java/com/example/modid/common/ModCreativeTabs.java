package com.example.modid.common;

import com.example.modid.Tags;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class ModCreativeTabs {

    public static final CreativeTabs MIMS_ADDITIONS =
            new CreativeTabs(Tags.MOD_ID + ".mims_additions") {

                @Override
                public ItemStack createIcon() {
                    // Use Micro Miner as the tab icon
                    if (ModItems.titaniumCoilMicroMiner != null) {
                        return new ItemStack(ModItems.titaniumCoilMicroMiner);
                    }
                    // Safe fallback during early init
                    return new ItemStack(Items.NETHER_STAR);
                }
            };

    private ModCreativeTabs() {}
}
