package com.jessicazayka.harrypotter.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class VoldemortEntity extends HostileEntity {

    // Red/Dark Boss Bar for Lord Voldemort
    private final ServerBossBar bossBar = new ServerBossBar(
            Text.literal("Lord Voldemort"),
            BossBar.Color.RED,
            BossBar.Style.NOTCHED_10
    );

    private int attackCooldown = 0;
    private boolean isPhaseTwo = false;

    public VoldemortEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setHealth(this.getMaxHealth());
    }

    // Define Voldemort's Base Stats
    public static DefaultAttributeContainer.Builder createVoldemortAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0D) // 300 HP (150 Hearts)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D); // Immune to knockback
    }

    @Override
    public void mobTick() {
        super.mobTick();

        // Update Boss Bar HP
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        if (!this.getWorld().isClient()) {
            // Check for Phase 2 Transition at 50% HP (150 HP)
            if (!isPhaseTwo && this.getHealth() <= 150.0F) {
                isPhaseTwo = true;
                this.bossBar.setColor(BossBar.Color.PURPLE); // Boss bar changes color in Phase 2
            }

            // Attack Loop
            LivingEntity target = this.getTarget();
            if (target != null) {
                this.getLookControl().lookAt(target, 30.0F, 30.0F);

                if (attackCooldown <= 0) {
                    castSpell(target);
                    attackCooldown = isPhaseTwo ? 40 : 60; // Faster attacks in Phase 2
                } else {
                    attackCooldown--;
                }
            }
        } else {
            // Phase 2 Visual Effect: Blue Fiendfyre particle trail on Client
            if (isPhaseTwo) {
                this.getWorld().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D),
                        this.getY() + this.random.nextDouble() * 2.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D),
                        0.0D, 0.05D, 0.0D
                );
            }
        }
    }

    // Boss Attack & Spell Mechanics
    private void castSpell(LivingEntity target) {
        double d0 = target.getX() - this.getX();
        double d1 = target.getBodyY(0.5D) - this.getBodyY(0.5D);
        double d2 = target.getZ() - this.getZ();

        // Fire Avada Kedavra Projectile
        AvadaKedavraEntity spell = new AvadaKedavraEntity(this.getWorld(), this);
        spell.setVelocity(d0, d1, d2, 1.5F, 1.0F);
        this.getWorld().spawnEntity(spell);
    }

    // Teleportation Defense Mechanic when taking damage
    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean tookDamage = super.damage(source, amount);

        if (tookDamage && amount > 15.0F && !this.getWorld().isClient()) {
            teleportRandomly();
        }
        return tookDamage;
    }

    private void teleportRandomly() {
        double newX = this.getX() + (this.random.nextDouble() - 0.5D) * 16.0D;
        double newY = this.getY() + (this.random.nextInt(16) - 8);
        double newZ = this.getZ() + (this.random.nextDouble() - 0.5D) * 16.0D;
        this.teleport(newX, newY, newZ, true);
    }

    // Add Boss Bar to player's screen when approaching
    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }
}
