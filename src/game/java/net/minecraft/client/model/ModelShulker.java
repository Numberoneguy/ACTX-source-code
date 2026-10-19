package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelShulker extends ModelBase {
	public final ModelRenderer base;
	public final ModelRenderer lid;

	public ModelShulker() {
		this.textureHeight = 64;
		this.textureWidth = 64;
		this.lid = new ModelRenderer(this);
		this.base = new ModelRenderer(this);
		this.lid.setTextureOffset(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16, 12, 16);
		this.lid.setRotationPoint(0.0F, 24.0F, 0.0F);
		this.base.setTextureOffset(0, 28).addBox(-8.0F, -8.0F, -8.0F, 16, 8, 16);
		this.base.setRotationPoint(0.0F, 24.0F, 0.0F);
	}

	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		this.base.render(scale);
		this.lid.render(scale);
	}
}
