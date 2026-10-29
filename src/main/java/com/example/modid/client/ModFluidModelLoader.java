package com.example.modid.client;

import com.example.modid.Tags;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ModFluidModelLoader implements ICustomModelLoader {
    INSTANCE;

    private static final String PREFIX = "fluid/";

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // No cached resources to reset.
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return Tags.MOD_ID.equals(modelLocation.getNamespace()) && modelLocation.getPath().startsWith(PREFIX);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        String fluidName = modelLocation.getPath().substring(PREFIX.length());
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            return ModelLoaderRegistry.getMissingModel();
        }
        return new ModelFluid(fluid);
    }
}
