package com.example.modid;

import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;

public class EarlyMixinLoader implements IEarlyMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.modid.early.json");
    }
}
