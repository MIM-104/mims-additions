package com.example.modid.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

@IFMLLoadingPlugin.Name("modid-core")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({
    "org.spongepowered.asm.mixin.",
    "zone.rong.mixinbooter.",
    "com.example.modid.core."
})
public class ModidCoremod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        // Vanilla hooks load before mod classes.
        return Collections.singletonList("mixins.modid.early.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }

    @Override
    public void onMixinConfigQueued(String mixinConfig) {}
}
