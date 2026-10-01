package net.dannyfather.mca_descendants.attachments;

import net.dannyfather.mca_descendants.MCADescendants;
import net.minecraft.core.UUIDUtil;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(
                    NeoForgeRegistries.ATTACHMENT_TYPES,
                    MCADescendants.MOD_ID
            );

    public static final Supplier<AttachmentType<UUID>> VILLAGERUUID =
            ATTACHMENTS.register("prev_villager_uuid", () ->
                    AttachmentType.builder(UUID::randomUUID)
                            .serialize(UUIDUtil.CODEC)
                            .copyOnDeath()
                            .build()
            );
}
