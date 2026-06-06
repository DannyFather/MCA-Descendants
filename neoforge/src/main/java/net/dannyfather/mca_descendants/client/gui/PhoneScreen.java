package net.dannyfather.mca_descendants.client.gui;



import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.util.compat.ButtonWidget;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.c2s.CallToPlayerMessage;
import net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.dannyfather.mca_descendants.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class PhoneScreen extends Screen {
    private List<String> keys = new ArrayList<>();
    private CompoundTag villagerData = new CompoundTag();
    private boolean dialSoundFired = false;
    private Map<UUID,String> villagerNames = new HashMap<>();

    private VillagerEntityMCA dummy;

    private ButtonWidget selectionLeftButton;
    private ButtonWidget selectionRightButton;
    private ButtonWidget villagerNameButton;
    private ButtonWidget callButton;
    private int loadingAnimationTicks;
    private int selectedIndex;

    public PhoneScreen() {
        super(Component.translatable("gui.phone.title"));
    }

    @Override
    public void tick() {
        super.tick();

        if (loadingAnimationTicks != -1) {
            loadingAnimationTicks++;
        }

        if (loadingAnimationTicks >= 20) {
            loadingAnimationTicks = 0;
        }
    }

    @Override
    public void init() {
        ModNetwork.sendToServer(new getDescendantsRequest());
        dialSoundFired = true;


        selectionLeftButton = addRenderableWidget(new ButtonWidget(width / 2 - 123, height / 2 + 65, 20, 20, Component.literal("<<"), b -> {
            if (selectedIndex == 0) {
                selectedIndex = keys.size() - 1;
            } else {
                selectedIndex--;
            }
            setVillagerData(selectedIndex);
        }));
        selectionRightButton = addRenderableWidget(new ButtonWidget(width / 2 + 103, height / 2 + 65, 20, 20, Component.literal(">>"), b -> {
            if (selectedIndex == keys.size() - 1) {
                selectedIndex = 0;
            } else {
                selectedIndex++;
            }
            setVillagerData(selectedIndex);
        }));
        villagerNameButton = addRenderableWidget(new ButtonWidget(width / 2 - 100, height / 2 + 65, 200, 20, Component.literal(""), b -> {
        }));

        callButton = addRenderableWidget(new ButtonWidget(width / 2 - 100, height / 2 + 90, 60, 20, Component.translatable("gui.button.call"), (b) -> {
            ModNetwork.sendToServer(new CallToPlayerMessage(UUID.fromString(keys.get(selectedIndex))));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));

        addRenderableWidget(new ButtonWidget(width / 2 - 35, height / 2 + 90, 70, 20, Component.translatable("gui.phone.options"), b -> {
            Minecraft.getInstance().setScreen(new PhoneOptionsScreen());
        }));

        addRenderableWidget(new ButtonWidget(width / 2 + 40, height / 2 + 90, 60, 20, Component.translatable("gui.button.exit"), b -> Objects.requireNonNull(this.minecraft).setScreen(null)));

        toggleButtons(false);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float offset) {
        super.render(context, mouseX, mouseY, offset);

        context.drawCenteredString(font, Component.translatable("gui.phone.title"), width / 2, height / 2 - 100, 0xffffff);

        if (keys.isEmpty()) {
            context.drawCenteredString(font, Component.translatable("gui.phone.noDescendants"), width / 2, height / 2 + 50, 0xffffff);
        } else {
            context.drawCenteredString(font, (selectedIndex + 1) + " / " + keys.size(), width / 2, height / 2 + 50, 0xffffff);
        }

        if (dialSoundFired) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(
                        ModSounds.PHONE_DIAL_TONE.get(),
                        1.0F,
                        1.0F
                );
            }
            dialSoundFired = false;
        }

        if (dummy != null) {
            int posX = width / 2;
            int posY = height / 2;
            InventoryScreen.renderEntityInInventoryFollowsMouse(context, posX - 30, posY - 70, posX + 30, posY + 45, 60, 0, mouseX, mouseY, dummy);
        }



    }

    public void setVillagerData(@NotNull CompoundTag data) {
        villagerData = data;
        keys = new ArrayList<>(data.getAllKeys());
        loadingAnimationTicks = -1;
        selectedIndex = 0;

        setVillagerData(0);
    }

    private void setVillagerData(int index) {
        if (!keys.isEmpty()) {
            CompoundTag firstData = villagerData.getCompound(keys.get(index));
            String villagerName = firstData.getString("name");

            dummy = EntitiesMCA.MALE_VILLAGER.create(Minecraft.getInstance().level);
            dummy.readAdditionalSaveData(firstData);
            villagerNameButton.setMessage(Component.nullToEmpty(villagerName));
            this.minecraft.getSoundManager().stop(null,SoundSource.PLAYERS);

            toggleButtons(true);
        } else {
            toggleButtons(false);
        }
    }

    private void toggleButtons(boolean enabled) {
        selectionLeftButton.active = enabled;
        selectionRightButton.active = enabled;
        callButton.active = enabled;
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

}
