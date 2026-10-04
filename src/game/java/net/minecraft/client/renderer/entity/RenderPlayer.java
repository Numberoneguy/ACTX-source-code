package net.minecraft.client.renderer.entity;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderPlayer extends RendererLivingEntity<AbstractClientPlayer> {
    private boolean smallArms;
    private boolean zombieModel;

    public RenderPlayer(RenderManager renderManager) {
        this(renderManager, false, false);
    }

    public RenderPlayer(RenderManager renderManager, boolean useSmallArms, boolean zombieModel) {
        super(renderManager, zombieModel ? new ModelZombie(0.0F, true) : new ModelPlayer(0.0F, useSmallArms), 0.5F);
        this.smallArms = useSmallArms;
        this.zombieModel = zombieModel;
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
    }

    protected RenderPlayer(RenderManager renderManager, ModelBase modelBase, float size) {
        super(renderManager, modelBase, size);
    }

    public ModelBiped getMainModel() {
        return (ModelBiped) super.getMainModel();
    }

    @Override
    public void doRender(AbstractClientPlayer abstractclientplayer, double d0, double d1, double d2, float f, float f1) {
        if (!abstractclientplayer.isUser() || this.renderManager.livingPlayer == abstractclientplayer) {
            double d3 = d1;
            if (abstractclientplayer.isSneaking() && !(abstractclientplayer instanceof EntityPlayerSP)) {
                d3 = d1 - 0.125D;
            }

            this.shadowSize = 0.5F;  // Standard shadow

            this.setModelVisibilities(abstractclientplayer);
            super.doRender(abstractclientplayer, d0, d3, d2, f, f1);
        }
    }

    public ResourceLocation getEntityTexture(AbstractClientPlayer abstractclientplayer) {
        return abstractclientplayer.getLocationSkin();
    }

    @Override
    protected void preRenderCallback(AbstractClientPlayer abstractclientplayer, float partialTickTime) {
        float scaleFactor = 0.9375F;
        GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer) {
        ModelBiped modelbiped = this.getMainModel();
        if (clientPlayer.isSpectator()) {
            modelbiped.setInvisible(false);
            modelbiped.bipedHead.showModel = true;
            modelbiped.bipedHeadwear.showModel = true;
        } else {
            ItemStack itemstack = clientPlayer.inventory.getCurrentItem();
            modelbiped.setInvisible(true);
            modelbiped.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            
            if (modelbiped instanceof ModelPlayer) {
                ModelPlayer modelplayer = (ModelPlayer) modelbiped;
                modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
                modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
                modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
                modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
                modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            }

            modelbiped.heldItemLeft = 0;
            modelbiped.aimedBow = false;
            modelbiped.isSneak = clientPlayer.isSneaking();
            if (itemstack == null) {
                modelbiped.heldItemRight = 0;
            } else {
                modelbiped.heldItemRight = 1;
                if (clientPlayer.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemstack.getItemUseAction();
                    if (enumaction == EnumAction.BLOCK) {
                        modelbiped.heldItemRight = 3;
                    } else if (enumaction == EnumAction.BOW) {
                        modelbiped.aimedBow = true;
                    }
                }
            }
        }
    }

    public void renderRightArm(AbstractClientPlayer clientPlayer) {
        if (!zombieModel) {
            float f = 1.0F;
            GlStateManager.color(f, f, f);
            ModelBiped modelbiped = this.getMainModel();
            this.setModelVisibilities(clientPlayer);
            modelbiped.isSneak = false;
            modelbiped.swingProgress = 0.0F;
            modelbiped.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
            
            if (modelbiped instanceof ModelPlayer) {
                ((ModelPlayer) modelbiped).renderRightArm();
            }
        }
    }

    public void renderLeftArm(AbstractClientPlayer clientPlayer) {
        if (!zombieModel) {
            float f = 1.0F;
            GlStateManager.color(f, f, f);
            ModelBiped modelbiped = this.getMainModel();
            this.setModelVisibilities(clientPlayer);
            modelbiped.isSneak = false;
            modelbiped.swingProgress = 0.0F;
            modelbiped.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
            
            if (modelbiped instanceof ModelPlayer) {
                ((ModelPlayer) modelbiped).renderLeftArm();
            }
        }
    }

    public void renderLivingAt(AbstractClientPlayer abstractclientplayer, double d0, double d1, double d2) {
        if (abstractclientplayer.isEntityAlive() && abstractclientplayer.isPlayerSleeping()) {
            super.renderLivingAt(abstractclientplayer, d0 + (double) abstractclientplayer.renderOffsetX,
                    d1 + (double) abstractclientplayer.renderOffsetY, d2 + (double) abstractclientplayer.renderOffsetZ);
        } else {
            super.renderLivingAt(abstractclientplayer, d0, d1, d2);
        }
    }

    protected void rotateCorpse(AbstractClientPlayer abstractclientplayer, float f, float f1, float f2) {
        if (abstractclientplayer.isEntityAlive() && abstractclientplayer.isPlayerSleeping()) {
            GlStateManager.rotate(abstractclientplayer.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(abstractclientplayer), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
        } else {
            super.rotateCorpse(abstractclientplayer, f, f1, f2);
        }
    }
}