package net.dannyfather.mca_descendants.fabric;

import net.fabricmc.api.ModInitializer;

import net.dannyfather.mca_descendants.ExampleMod;

public final class MCADescendantsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ExampleMod.init();
    }
}
