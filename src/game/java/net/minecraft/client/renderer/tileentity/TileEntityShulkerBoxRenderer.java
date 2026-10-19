package net.minecraft.client.renderer.tileentity;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileEntityShulkerBoxRenderer extends TileEntitySpecialRenderer<TileEntityShulkerBox> {
	private static final ResourceLocation[] SHULKER_TEXTURES = new ResourceLocation[16];
	private final ModelShulker model;

	static {
		for (int i = 0; i < 16; ++i) {
			SHULKER_TEXTURES[i] = new ResourceLocation(
					"textures/entity/shulker/shulker_" + EnumDyeColor.byMetadata(i).getName() + ".png");
		}
	}

	public TileEntityShulkerBoxRenderer(ModelShulker model) {
		this.model = model;
	}

	public void renderTileEntityAt(TileEntityShulkerBox te, double x, double y, double z, float partialTicks,
			int destroyStage) {
		EnumFacing enumfacing = EnumFacing.UP;
		if (te.hasWorldObj()) {
			IBlockState iblockstate = te.getWorld().getBlockState(te.getPos());
			if (iblockstate.getBlock() instanceof BlockShulkerBox) {
				enumfacing = (EnumFacing) iblockstate.getValue(BlockShulkerBox.FACING);
			}
		}

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(GL_LEQUAL);
		GlStateManager.depthMask(true);
		GlStateManager.disableCull();
		if (destroyStage >= 0) {
			this.bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(GL_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4.0F, 4.0F, 1.0F);
			GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(GL_MODELVIEW);
		} else {
			this.bindTexture(SHULKER_TEXTURES[te.getColor().getMetadata()]);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		if (destroyStage < 0) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GlStateManager.scale(1.0F, -1.0F, -1.0F);
		GlStateManager.translate(0.0F, 1.0F, 0.0F);
		GlStateManager.scale(0.9995F, 0.9995F, 0.9995F);
		GlStateManager.translate(0.0F, -1.0F, 0.0F);
		switch (enumfacing) {
		case DOWN:
			GlStateManager.translate(0.0F, 2.0F, 0.0F);
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			break;
		case UP:
		default:
			break;
		case NORTH:
			GlStateManager.translate(0.0F, 1.0F, 1.0F);
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
			break;
		case SOUTH:
			GlStateManager.translate(0.0F, 1.0F, -1.0F);
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			break;
		case WEST:
			GlStateManager.translate(-1.0F, 1.0F, 0.0F);
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
			break;
		case EAST:
			GlStateManager.translate(1.0F, 1.0F, 0.0F);
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
		}

		this.model.base.render(0.0625F);
		GlStateManager.translate(0.0F, -te.getProgress(partialTicks) * 0.5F, 0.0F);
		GlStateManager.rotate(270.0F * te.getProgress(partialTicks), 0.0F, 1.0F, 0.0F);
		this.model.lid.render(0.0625F);
		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if (destroyStage >= 0) {
			GlStateManager.matrixMode(GL_TEXTURE);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL_MODELVIEW);
		}
	}
}