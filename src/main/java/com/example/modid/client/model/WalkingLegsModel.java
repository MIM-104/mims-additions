package com.example.modid.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class WalkingLegsModel extends ModelBase {

    private final ModelRenderer leg;

    public WalkingLegsModel() {
        textureWidth = 16;
        textureHeight = 16;

        leg = new ModelRenderer(this);
        leg.setRotationPoint(0.0F, 0.0F, 0.0F);
        leg.cubeList.add(new ModelBox(leg, 0, 4, -1.5F, 0.0F, -1.0F, 3, 3, 2, 0.0F, false));
        leg.cubeList.add(new ModelBox(leg, 4, 4, -0.75F, 3.0F, -1.0F, 3, 3, 2, 0.0F, false));
        leg.cubeList.add(new ModelBox(leg, 4, 5, 0.25F, 6.0F, -1.0F, 3, 3, 2, 0.0F, false));
        leg.cubeList.add(new ModelBox(leg, 3, 5, 0.75F, 9.0F, -1.0F, 3, 3, 2, 0.0F, false));
        leg.cubeList.add(new ModelBox(leg, 3, 5, 0.0F, 12.0F, -1.0F, 3, 3, 2, 0.0F, false));
        leg.cubeList.add(new ModelBox(leg, 3, 5, -0.75F, 15.0F, -1.0F, 3, 3, 2, 0.0F, false));
        leg.cubeList.add(new ModelBox(leg, 3, 5, -3.0F, 18.0F, -2.0F, 4, 2, 4, 0.0F, false));
    }

    public void renderLeg(Entity entity, float swing, float scale) {
        leg.rotateAngleZ = swing;
        leg.render(scale);
    }
}
