package com.example.modid;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

public class LateMixin implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        // Mod hooks load after their target classes are available.
        return Collections.singletonList("mixins.modid.late.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }

    @Override
    public void onMixinConfigQueued(String mixinConfig) {}
}
