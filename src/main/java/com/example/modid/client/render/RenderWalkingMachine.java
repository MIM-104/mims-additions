package com.example.modid.client.render;

import com.example.modid.client.model.WalkingLegsModel;
import com.example.modid.common.EntityWalkingMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderWalkingMachine extends Render<EntityWalkingMachine> {

    private static final float BLOCK_SIDE_OFFSET = 0.5F;
    private static final float LEG_TOP_Y = 2.25F;
    private static final float LEG_SCALE = 0.1F;
    private static final float BODY_CENTER_Y = 2.5F;

    private static final ResourceLocation LEGS_TEXTURE =
            new ResourceLocation("gregtech", "textures/blocks/casings/voltage/lv/side.png");

    private final WalkingLegsModel legs = new WalkingLegsModel();

    public RenderWalkingMachine(RenderManager renderManager) {
        super(renderManager);
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(
            EntityWalkingMachine entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.0F, entity.getRiseOffset(partialTicks), 0.0F);
        GlStateManager.rotate(180.0F - entity.getWalkDirection(), 0.0F, 1.0F, 0.0F);

        float swing = entity.getLegSwing(partialTicks);
        bindTexture(LEGS_TEXTURE);
        renderLeg(entity, -BLOCK_SIDE_OFFSET, -swing);
        renderLeg(entity, BLOCK_SIDE_OFFSET, swing);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, BODY_CENTER_Y, 0.0D);
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        Minecraft.getMinecraft()
                .getRenderItem()
                .renderItem(entity.getMachineStack(), ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWalkingMachine entity) {
        return LEGS_TEXTURE;
    }

    private void renderLeg(EntityWalkingMachine entity, float x, float swing) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, LEG_TOP_Y, 0.0F);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        legs.renderLeg(entity, swing, LEG_SCALE);
        GlStateManager.popMatrix();
    }
}
