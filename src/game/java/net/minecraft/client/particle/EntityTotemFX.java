package net.minecraft.client.particle;

import net.minecraft.world.World;

public class EntityTotemFX extends EntityFX {
	public EntityTotemFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn);
		this.motionX = xSpeedIn;
		this.motionY = ySpeedIn;
		this.motionZ = zSpeedIn;
		this.particleScale *= 0.75F;
		this.particleMaxAge = 60 + this.rand.nextInt(12);

		if (this.rand.nextInt(4) == 0) {
			this.setRBGColorF(0.6F + this.rand.nextFloat() * 0.2F, 0.6F + this.rand.nextFloat() * 0.3F,
					this.rand.nextFloat() * 0.2F);
		} else {
			this.setRBGColorF(0.1F + this.rand.nextFloat() * 0.2F, 0.4F + this.rand.nextFloat() * 0.3F,
					this.rand.nextFloat() * 0.2F);
		}

		this.setParticleTextureIndex(183);
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setDead();
		}

		if (this.particleAge > this.particleMaxAge / 2) {
			this.setAlphaF(1.0F
					- ((float) this.particleAge - (float) (this.particleMaxAge / 2)) / (float) this.particleMaxAge);
		}

		this.setParticleTextureIndex(176 + (7 - this.particleAge * 8 / this.particleMaxAge));
		this.motionY += -0.05D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.6D;
		this.motionY *= 0.6D;
		this.motionZ *= 0.6D;

		if (this.isCollided) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}

	public int getBrightnessForRender(float partialTicks) {
		return 15728880;
	}

	public static class Factory implements IParticleFactory {
		public EntityFX getEntityFX(int var1, World world, double d0, double d1, double d2, double d3, double d4,
				double d5, int... var15) {
			return new EntityTotemFX(world, d0, d1, d2, d3, d4, d5);
		}
	}
}
