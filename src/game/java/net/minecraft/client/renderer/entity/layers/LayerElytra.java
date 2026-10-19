package net.minecraft.client.renderer.entity.layers;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
public class LayerElytra implements LayerRenderer<EntityLivingBase> {
	private static final ResourceLocation texture_elytra = new ResourceLocation("textures/entity/elytra.png");
	private static final ResourceLocation encant_glint = new ResourceLocation(
			"textures/misc/enchanted_item_glint.png");
	private static final boolean cape_texture = false;
	protected final RendererLivingEntity<?> renderPlayer;
	private final ModelElytra modelElytra = new ModelElytra();

	public LayerElytra(RendererLivingEntity<?> renderPlayerIn) {
		this.renderPlayer = renderPlayerIn;
	}
	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack itemstack = entitylivingbaseIn.getEquipmentInSlot(3);
		if (itemstack != null && itemstack.getItem() == Items.elytra) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(1, 0);
			if (cape_texture && entitylivingbaseIn instanceof AbstractClientPlayer) {
				AbstractClientPlayer player = (AbstractClientPlayer) entitylivingbaseIn;

				if (player.hasPlayerInfo() && player.getLocationCape() != null
						&& player.isWearing(EnumPlayerModelParts.CAPE)) {
					this.renderPlayer.bindTexture(player.getLocationCape());
				} else {
					this.renderPlayer.bindTexture(texture_elytra);
				}
			} else {
				this.renderPlayer.bindTexture(texture_elytra);
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.0F, 0.125F);
			this.modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale,
					entitylivingbaseIn);
			this.modelElytra.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
					scale);

			if (itemstack.isItemEnchanted()) {
				this.renderGlint(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
						headPitch, scale);
			}

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

	private void renderGlint(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		float f = (float) entity.ticksExisted + partialTicks;
		this.renderPlayer.bindTexture(encant_glint);
		GlStateManager.enableBlend();
		GlStateManager.depthFunc(514);
		GlStateManager.depthMask(false);
		GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
		for (int i = 0; i < 2; ++i) {
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(768, 1);
			GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
			GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(5888);
			this.modelElytra.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		}
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		GlStateManager.disableBlend();
	}
	public boolean shouldCombineTextures() {
		return false;
	}
}