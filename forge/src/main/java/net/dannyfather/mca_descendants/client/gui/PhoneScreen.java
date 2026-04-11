package net.dannyfather.mca_descendants.client.gui;

import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.util.compat.ButtonWidget;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.c2s.CallToPlayerMessage;
import net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.dannyfather.mca_descendants.network.ModNetwork.CHANNEL;

public class PhoneScreen extends Screen {
    private List<String> keys = new ArrayList<>();
    private CompoundTag villagerData = new CompoundTag();

    private VillagerEntityMCA dummy;
    Quaternionf modelRot = new Quaternionf().rotationXYZ(
            (float) Math.toRadians(0),   // no tilt
            (float) Math.toRadians(180), // fix facing direction
            (float) Math.toRadians(180)
    );

    private ButtonWidget selectionLeftButton;
    private ButtonWidget selectionRightButton;
    private ButtonWidget villagerNameButton;
    private ButtonWidget callButton;
    private int loadingAnimationTicks;
    private int selectedIndex;

    private Quaternionf QFID = new Quaternionf().identity();

    public PhoneScreen(int villager) {
        super(Component.translatable("gui.whistle.title"));
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
        CHANNEL.sendToServer(new getDescendantsRequest());

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
            CHANNEL.sendToServer((new CallToPlayerMessage(UUID.fromString(keys.get(selectedIndex)))));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));

        addRenderableWidget(new ButtonWidget(width / 2 + 40, height / 2 + 90, 60, 20, Component.translatable("gui.button.exit"), b -> Objects.requireNonNull(this.minecraft).setScreen(null)));

        toggleButtons(false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int sizeX, int sizeY, float offset) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.phone.title"), width / 2, height / 2 - 100, 0xffffff);

        if (loadingAnimationTicks != -1) {
            String loadingMsg = new String(new char[(loadingAnimationTicks / 5) % 4]).replace("\0", ".");
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.loading").append(Component.literal(loadingMsg)), width / 2 - 20, height / 2 - 10, 0xffffff);
        } else {
            if (keys.size() == 0) {
                guiGraphics.drawCenteredString(this.font, Component.translatable("gui.phone.noDescendants"), width / 2, height / 2 + 50, 0xffffff);
            } else {
                guiGraphics.drawCenteredString(this.font, (selectedIndex + 1) + " / " + keys.size(), width / 2, height / 2 + 50, 0xffffff);
            }
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0,0,100);
        guiGraphics.pose().popPose();
        drawDummy(guiGraphics);

        super.render(guiGraphics, sizeX, sizeY, offset);
    }

    private void drawDummy(GuiGraphics context) {
        final int posX = width / 2;
        int posY = height / 2 + 45;
        if (dummy != null) {
            InventoryScreen.renderEntityInInventory(context, posX, posY, 60, modelRot, modelRot, dummy);
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
        if (keys.size() > 0) {
            CompoundTag firstData = villagerData.getCompound(keys.get(index));

            Level level = Minecraft.getInstance().level;
            if (level == null) return;


            dummy = EntitiesMCA.MALE_VILLAGER.get().create(Minecraft.getInstance().level);
            if (dummy == null) return;
            dummy.load(firstData);

            // 🔥 REQUIRED FIXES
            dummy.setPos(0, 0, 0);
            dummy.setYRot(0F);
            dummy.setXRot(0.0F);
            dummy.yBodyRot = 0F;
            dummy.yHeadRot = 0F;
            dummy.setPose(Pose.STANDING);

            villagerNameButton.setMessage(dummy.getDisplayName());

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
}
