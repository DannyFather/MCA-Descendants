package net.dannyfather.mca_descendants.client.gui;

import net.conczin.mca.util.compat.ButtonWidget;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.c2s.RandomToPlayerMessage;
import net.dannyfather.mca_descendants.network.c2s.SpectateWorldMessage;
import net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Quaternionf;

import java.util.Objects;


public class PhoneOptionsScreen extends Screen {

    private ButtonWidget redoDestinyButton;

    private Quaternionf QFID = new Quaternionf().identity();

    public PhoneOptionsScreen() {
        super(Component.translatable("gui.whistle.title"));
    }


    @Override
    public void init() {
        redoDestinyButton = addRenderableWidget(new ButtonWidget(width / 2 - 140, height / 2, 120, 20, Component.translatable("gui.phone.randomrespawn"), b -> {
            ModNetwork.sendToServer((new RandomToPlayerMessage()));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));

        addRenderableWidget(new ButtonWidget(width / 2 + 20, height / 2, 120, 20, Component.translatable("gui.phone.spectator"), b -> {
            ModNetwork.sendToServer((new SpectateWorldMessage()));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));

        addRenderableWidget(new ButtonWidget(width / 2 + 40, height / 2 + 90, 60, 20, Component.translatable("gui.button.exit"), b -> Objects.requireNonNull(this.minecraft).setScreen(null)));

        addRenderableWidget(new ButtonWidget(width / 2 - 100, height / 2 + 90, 60, 20, Component.translatable("gui.button.back"), (b) -> {
            Minecraft.getInstance().setScreen(new PhoneScreen());
        }));

        if(MCADescendantsCommonConfig.RESPAWN_RANDOM.get() && !Minecraft.getInstance().level.getLevelData().isHardcore()) {
            redoDestinyButton.active = true;
        } else {
            redoDestinyButton.active = false;
        }

    }

    @Override
    public void render(GuiGraphics guiGraphics, int sizeX, int sizeY, float offset) {
        super.render(guiGraphics,sizeX,sizeY,offset);

        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.phone.title"), width / 2, height / 2 - 100, 0xffffff);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.phone.respawn.1"), width / 2 - 80, height / 2 - 40, 0xffffff);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.phone.respawn.2"), width / 2 - 80, height / 2 - 30, 0xffffff);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.phone.spectate.1"), width / 2 + 80, height / 2 - 40, 0xffffff);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0,0,100);
        guiGraphics.pose().popPose();
    }


    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().getSoundManager().stop(ModSounds.PHONE_DIAL_TONE.get().getLocation(), SoundSource.BLOCKS);
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.getSoundManager().stop(null,SoundSource.PLAYERS);
            this.minecraft.player.playSound(
                    ModSounds.PHONE_HANGUP.get(),
                    1.0F,
                    1.0F
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
