package net.dannyfather.mca_descendants.entity.layers;

import net.dannyfather.mca_descendants.entity.BabySittingEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BabySittingRenderer extends EntityRenderer<BabySittingEntity> {
    public BabySittingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BabySittingEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/block/air.png");
    }

    @Override
    public boolean shouldRender(
            BabySittingEntity entity,
            Frustum frustum,
            double camX,
            double camY,
            double camZ
    ) {
        return false;
    }
}

