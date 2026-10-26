package com.jessicazayka.harrypotter.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class AvadaKedavraEntity extends ThrownItemEntity {

    public AvadaKedavraEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public AvadaKedavraEntity(World world, LivingEntity owner) {
        super(EntityType.SNOWBALL, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL; // Visual placeholder item model
    }

    @Override
    public void tick() {
        super.tick();
        // Spawns vibrant green particles in flight
        if (this.getWorld().isClient()) {
            this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getWorld().isClient()) {
            // Avada Kedavra deals heavy magic damage ignoring standard physical armor
            entityHitResult.getEntity().damage(this.getDamageSources().magic(), 25.0F); // 12.5 Hearts of damage
            this.discard();
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient()) {
            this.discard();
        }
    }
}
