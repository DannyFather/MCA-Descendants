package net.dannyfather.mca_descendants.server.command;

import net.dannyfather.MCADescendants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = MCADescendants.MOD_ID)
public class ModCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        AdminCommand.register(event.getDispatcher());
    }
}
