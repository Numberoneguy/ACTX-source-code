package net.minecraft.client.renderer.tileentity;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;

import net.lax1dude.eaglercraft.v1_8.internal.buffer.FloatBuffer;
import net.lax1dude.eaglercraft.v1_8.EaglercraftRandom;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.WorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntityCustomGateway;
import net.minecraft.util.ResourceLocation;

public class TileEntityCustomGatewayRenderer extends TileEntitySpecialRenderer<TileEntityCustomGateway> {
    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final EaglercraftRandom RANDOM = new EaglercraftRandom(31100L);
    FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    @Override
    public void renderTileEntityAt(TileEntityCustomGateway te, double x, double y, double z, float partialTicks, int destroyStage) {
        float f = (float)this.rendererDispatcher.entityX;
        float f1 = (float)this.rendererDispatcher.entityY;
        float f2 = (float)this.rendererDispatcher.entityZ;
        GlStateManager.disableLighting();
        RANDOM.setSeed(31100L);

        for (int i = 0; i < 16; ++i) {
            GlStateManager.pushMatrix();
            float f3 = (float)(16 - i);
            float f4 = 0.0625F;
            float f5 = 1.0F / (f3 + 1.0F);

            if (i == 0) {
                this.bindTexture(END_SKY_TEXTURE);
                f5 = 0.1F;
                f3 = 65.0F;
                f4 = 0.125F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }

            if (i >= 1) {
                this.bindTexture(END_PORTAL_TEXTURE);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL_ONE, GL_ONE);
                f4 = 0.5F;
            }

            float f6 = (float)(-(y + 0.5D));
            GlStateManager.translate(f, f6, f2);
            
            GlStateManager.texGen(GlStateManager.TexGen.S, GL_OBJECT_LINEAR);
            GlStateManager.texGen(GlStateManager.TexGen.T, GL_OBJECT_LINEAR);
            GlStateManager.texGen(GlStateManager.TexGen.R, GL_OBJECT_LINEAR);
            GlStateManager.texGen(GlStateManager.TexGen.Q, GL_EYE_LINEAR);
            
            GlStateManager.func_179105_a(GlStateManager.TexGen.S, GL_OBJECT_PLANE, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.func_179105_a(GlStateManager.TexGen.T, GL_OBJECT_PLANE, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.func_179105_a(GlStateManager.TexGen.R, GL_OBJECT_PLANE, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GlStateManager.func_179105_a(GlStateManager.TexGen.Q, GL_EYE_PLANE, this.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            
            GlStateManager.enableTexGen();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, (float)(Minecraft.getSystemTime() % 70000L) / 70000.0F, 0.0F);
            GlStateManager.scale(f4, f4, f4);
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.rotate((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.0F);
            GlStateManager.translate(-f, -f2, -f1);
            
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            
            float r = (RANDOM.nextFloat() * 0.5F + 0.1F) * f5;
            float g = (RANDOM.nextFloat() * 0.5F + 0.4F) * f5;
            float b = (RANDOM.nextFloat() * 0.5F + 0.5F) * f5;

            // North Face
            worldrenderer.pos(x, y, z).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x, y + 1.0D, z).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x + 1.0D, y + 1.0D, z).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x + 1.0D, y, z).color(r, g, b, 1.0F).endVertex();
            // South Face
            worldrenderer.pos(x, y, z + 1.0D).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x + 1.0D, y, z + 1.0D).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x, y + 1.0D, z + 1.0D).color(r, g, b, 1.0F).endVertex();
            // West Face
            worldrenderer.pos(x, y, z).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x, y, z + 1.0D).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x, y + 1.0D, z + 1.0D).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x, y + 1.0D, z).color(r, g, b, 1.0F).endVertex();
            // East Face
            worldrenderer.pos(x + 1.0D, y, z).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x + 1.0D, y + 1.0D, z).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(r, g, b, 1.0F).endVertex();
            worldrenderer.pos(x + 1.0D, y, z + 1.0D).color(r, g, b, 1.0F).endVertex();

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL_MODELVIEW);
        }

        GlStateManager.disableTexGen();
        GlStateManager.enableLighting();
    }

    private FloatBuffer getBuffer(float p1, float p2, float p3, float p4) {
        this.buffer.clear();
        this.buffer.put(p1).put(p2).put(p3).put(p4);
        this.buffer.flip();
        return this.buffer;
    }
}
