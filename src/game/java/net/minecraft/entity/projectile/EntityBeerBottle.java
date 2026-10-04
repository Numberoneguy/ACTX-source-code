package net.minecraft.entity.projectile;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityBeerBottle extends EntityThrowable {

    public EntityBeerBottle(World worldIn) {
        super(worldIn);
    }

    public EntityBeerBottle(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityBeerBottle(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    protected void onImpact(MovingObjectPosition movingObjectPositionIn) {
        if (!this.worldObj.isRemote) {
            this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "game.potion.smash", 1.0F, this.rand.nextFloat() * 0.1F + 0.9F);
            this.worldObj.playAuxSFX(2002, this.getPosition(), 0);

            // 5 block radius area of effect
            double radius = 5.0D;
            AxisAlignedBB area = new AxisAlignedBB(
                this.posX - radius, this.posY - radius, this.posZ - radius,
                this.posX + radius, this.posY + radius, this.posZ + radius
            );

            List<EntityLivingBase> entities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, area);
            for (EntityLivingBase entity : entities) {
                // Nausea II for 20 seconds (400 ticks), amplifier 1 = level II
                entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 400, 1));
            }

            this.setDead();
        }
    }
}
