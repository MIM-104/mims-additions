package com.example.modid.client;

import com.example.modid.Tags;
import com.example.modid.client.render.RenderWalkingMachine;
import com.example.modid.common.EntityWalkingMachine;
import com.example.modid.common.GregtechFluidBlockRegistrar;
import com.example.modid.common.ModItems;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ClientProxy {

    private static boolean fluidLoaderRegistered = false;

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityWalkingMachine.class, RenderWalkingMachine::new);

        if (!fluidLoaderRegistered) {
            ModelLoaderRegistry.registerLoader(ModFluidModelLoader.INSTANCE);
            fluidLoaderRegistered = true;
        }

        ModelLoader.setCustomModelResourceLocation(
                ModItems.titaniumCoilMicroMiner,
                0,
                new ModelResourceLocation(Tags.MOD_ID + ":titaniumcoilmicrominer", "inventory"));

        ModelLoader.setCustomModelResourceLocation(
                ModItems.omniumPlate, 0, new ModelResourceLocation(Tags.MOD_ID + ":omnium_plate", "inventory"));

        ModelLoader.setCustomModelResourceLocation(
                ModItems.microminerMicrominer,
                0,
                new ModelResourceLocation(Tags.MOD_ID + ":microminermicrominer", "inventory"));

        ModelLoader.setCustomModelResourceLocation(
                ModItems.stabilizedEpsteinium,
                0,
                new ModelResourceLocation(Tags.MOD_ID + ":stabilized_epsteinium", "inventory"));

        ModelLoader.setCustomModelResourceLocation(
                ModItems.babyVillagerEssence,
                0,
                new ModelResourceLocation(Tags.MOD_ID + ":baby_villager_essence", "inventory"));

        ModelLoader.setCustomModelResourceLocation(
                ModItems.epsteiniumInfusedWetwareMainframe,
                0,
                new ModelResourceLocation(Tags.MOD_ID + ":epsteinium_infused_wetware_mainframe", "inventory"));

        ModelLoader.setCustomModelResourceLocation(
                ModItems.rtaNeutronateFineWire,
                0,
                new ModelResourceLocation(Tags.MOD_ID + ":rta_neutronate_fine_wire", "inventory"));

        for (BlockFluidClassic block : GregtechFluidBlockRegistrar.REGISTERED_BLOCKS) {
            Fluid fluid = block.getFluid();
            if (fluid == null) {
                continue;
            }
            ResourceLocation modelLocation = new ResourceLocation(Tags.MOD_ID, "fluid/" + fluid.getName());
            ModelResourceLocation model = new ModelResourceLocation(modelLocation, "normal");
            ModelLoader.setCustomStateMapper(
                    block,
                    new StateMapperBase() {
                        @Override
                        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                            return model;
                        }
                    });

            Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR) {
                ModelBakery.registerItemVariants(item, model);
                ModelLoader.setCustomModelResourceLocation(item, 0, model);
                ModelLoader.setCustomMeshDefinition(item, stack -> model);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerSprites(TextureStitchEvent.Pre event) {
        // Install these before generic fluid/model registration requests the same names.
        event.getMap()
                .setTextureEntry(
                        new InfinityPulseSprite(new ResourceLocation(Tags.MOD_ID, "blocks/molten_infinity_still")));
        event.getMap()
                .setTextureEntry(
                        new InfinityPulseSprite(new ResourceLocation(Tags.MOD_ID, "blocks/molten_infinity_flow")));
        event.getMap().registerSprite(new ResourceLocation(Tags.MOD_ID, "blocks/epsteinium_still"));
        event.getMap().registerSprite(new ResourceLocation(Tags.MOD_ID, "blocks/epsteinium_flow"));
        event.getMap().registerSprite(new ResourceLocation(Tags.MOD_ID, "blocks/liquidbabyvillager_still"));
        event.getMap().registerSprite(new ResourceLocation(Tags.MOD_ID, "blocks/liquidbabyvillager_flow"));

        for (BlockFluidClassic block : GregtechFluidBlockRegistrar.REGISTERED_BLOCKS) {
            Fluid fluid = block.getFluid();
            if (fluid == null) {
                continue;
            }
            if (fluid.getStill() != null) {
                event.getMap().registerSprite(fluid.getStill());
            }
            if (fluid.getFlowing() != null) {
                event.getMap().registerSprite(fluid.getFlowing());
            }
        }
    }
}
