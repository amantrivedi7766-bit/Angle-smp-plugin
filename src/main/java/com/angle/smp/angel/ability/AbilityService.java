package com.angle.smp.angel.ability;

import com.angle.smp.angel.AngelPlugin;
import com.angle.smp.angel.data.PlayerData;
import com.angle.smp.angel.model.AbilityType;
import com.angle.smp.angel.model.Element;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;

import java.util.*;

public class AbilityService {
    private final AngelPlugin plugin;
    private final ElementKitRegistry registry;
    private final Set<UUID> canDoubleJump = new HashSet<>();

    public AbilityService(AngelPlugin plugin, ElementKitRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void unlockForLevel(PlayerData data) {
        if (data.getElement() == null) return;
        for (AbilityDefinition def : registry.get(data.getElement())) {
            if (data.getLevel() >= def.unlockLevel()) {
                data.unlock(def.id());
            }
        }
    }

    public void runPrimary(Player player, PlayerData data) {
        cast(player, data, AbilityType.PRIMARY);
    }

    public void runSecondary(Player player, PlayerData data) {
        cast(player, data, AbilityType.SECONDARY);
    }

    private void cast(Player player, PlayerData data, AbilityType type) {
        Element element = data.getElement();
        if (element == null) return;
        AbilityDefinition def = registry.get(element).stream().filter(a -> a.type() == type).findFirst().orElse(null);
        if (def == null || !data.isUnlocked(def.id()) || !data.isToggled(def.id())) return;
        if (def.cooldownSeconds() > 0 && data.onCooldown(def.id())) {
            long sec = Math.max(1, data.getCooldownLeftMillis(def.id()) / 1000);
            player.sendActionBar(Component.text("Ability on cooldown: " + sec + "s", net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }

        boolean casted = switch (def.id()) {
            case "fire_blast" -> fireBlast(player, def);
            case "fire_dash" -> fireDash(player, def);
            case "ice_spike" -> iceSpike(player, def);
            case "freeze" -> freeze(player, def);
            case "lightning_strike" -> lightningStrike(player, def);
            case "speed_boost" -> speedBoost(player);
            case "wind_push" -> windPush(player, def);
            case "double_jump" -> windDoubleJump(player, data);
            case "earth_wall" -> earthWall(player);
            case "stomp" -> stomp(player, def);
            case "heal" -> heal(player);
            case "flash" -> flash(player, def);
            default -> false;
        };

        if (casted && def.cooldownSeconds() > 0) {
            data.setCooldown(def.id(), def.cooldownSeconds() * 1000L);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f);
        }
    }

    public void runPassives(Player player, PlayerData data) {
        if (data.getElement() == null) return;
        if (data.getElement() == Element.FIRE && data.isUnlocked("flame_aura") && data.isToggled("flame_aura")) {
            for (Entity e : player.getNearbyEntities(3, 2, 3)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.setFireTicks(Math.max(le.getFireTicks(), 40));
                    le.damage(1.0 + plugin.cfg().value("elements.fire.flame-aura.damage", 1.0), player);
                    le.getWorld().spawnParticle(Particle.FLAME, le.getLocation().add(0, 1, 0), 8, .2, .2, .2, .01);
                }
            }
        }
        if (data.getElement() == Element.ICE && data.isUnlocked("water_walk") && data.isToggled("water_walk")) {
            Block b = player.getLocation().subtract(0, 1, 0).getBlock();
            if (b.getType() == Material.WATER) {
                b.setType(Material.FROSTED_ICE);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (b.getType() == Material.FROSTED_ICE) b.setType(Material.WATER);
                }, 40L);
            }
        }
        if (data.getElement() == Element.LIGHTNING && data.isUnlocked("fast_attack")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, true, false, false));
        }
        if (data.getElement() == Element.LIGHT && data.isUnlocked("regen")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 50, 0, true, false, false));
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 4, .3, .5, .3, .01);
        }
        if (data.getElement() == Element.EARTH && data.isUnlocked("earth_skin")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 0, true, false, false));
        }
        if (data.getElement() == Element.WIND && data.isUnlocked("double_jump") && player.isOnGround()) {
            canDoubleJump.add(player.getUniqueId());
            player.setAllowFlight(true);
        }
    }

    public boolean handleWindDoubleJumpFlight(Player player, PlayerData data) {
        if (data.getElement() != Element.WIND || !data.isUnlocked("double_jump") || !canDoubleJump.contains(player.getUniqueId())) {
            return false;
        }
        if (data.onCooldown("double_jump")) return true;
        player.setFlying(false);
        player.setAllowFlight(false);
        Vector velocity = player.getLocation().getDirection().multiply(0.8).setY(0.7);
        player.setVelocity(velocity);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 24, .5, .2, .5, .05);
        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.3f);
        data.setCooldown("double_jump", 3000);
        canDoubleJump.remove(player.getUniqueId());
        return true;
    }

    private boolean fireBlast(Player player, AbilityDefinition def) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        for (int i = 1; i <= 12; i++) {
            Location point = eye.clone().add(dir.clone().multiply(i));
            player.getWorld().spawnParticle(Particle.FLAME, point, 6, .1, .1, .1, .01);
            player.getWorld().spawnParticle(Particle.SMOKE, point, 2, .1, .1, .1, .01);
            for (Entity e : point.getWorld().getNearbyEntities(point, 1, 1, 1)) {
                if (e instanceof LivingEntity le && e != player) {
                    le.damage(def.baseDamage() + plugin.levelBonus(player), player);
                    le.setFireTicks(80);
                    point.getWorld().playSound(point, Sound.ITEM_FIRECHARGE_USE, 1f, 1.2f);
                    return true;
                }
            }
        }
        return true;
    }

    private boolean fireDash(Player player, AbilityDefinition def) {
        Vector v = player.getLocation().getDirection().multiply(1.6).setY(0.2);
        player.setVelocity(v);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                Location l = player.getLocation();
                player.getWorld().spawnParticle(Particle.FLAME, l, 14, .3, .1, .3, .02);
                for (Entity e : l.getWorld().getNearbyEntities(l, 1.7, 1.2, 1.7)) {
                    if (e instanceof LivingEntity le && e != player) {
                        le.damage(def.baseDamage(), player);
                        le.setFireTicks(60);
                    }
                }
                if (ticks > 10 || player.isOnGround()) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private boolean iceSpike(Player player, AbilityDefinition def) {
        Location base = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(3));
        base.getWorld().spawnParticle(Particle.SNOWFLAKE, base, 30, .8, .5, .8, .02);
        base.getWorld().playSound(base, Sound.BLOCK_GLASS_BREAK, 1f, 0.7f);
        for (Entity e : base.getWorld().getNearbyEntities(base, 2.5, 2, 2.5)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(def.baseDamage() + plugin.levelBonus(player), player);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
            }
        }
        return true;
    }

    private boolean freeze(Player player, AbilityDefinition def) {
        Entity target = player.getTargetEntity(15);
        if (!(target instanceof LivingEntity le) || target == player) return false;
        le.damage(def.baseDamage(), player);
        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
        le.getWorld().spawnParticle(Particle.BLOCK, le.getLocation().add(0,1,0), 35, .4,.7,.4, Material.ICE.createBlockData());
        le.getWorld().playSound(le.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.8f);
        return true;
    }

    private boolean lightningStrike(Player player, AbilityDefinition def) {
        Entity target = player.getTargetEntity(20);
        Location hit = target != null ? target.getLocation() : player.getLocation().add(player.getLocation().getDirection().multiply(8));
        hit.getWorld().strikeLightningEffect(hit);
        hit.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, hit.add(0,1,0), 80, .6, 1, .6, .2);
        hit.getWorld().playSound(hit, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.1f);
        for (Entity e : hit.getWorld().getNearbyEntities(hit, 2.5, 2.5, 2.5)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(def.baseDamage() + plugin.levelBonus(player), player);
            }
        }
        return true;
    }

    private boolean speedBoost(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 2));
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0,1,0), 20, .4,.4,.4,.1);
        player.playSound(player.getLocation(), Sound.ENTITY_BEE_LOOP_AGGRESSIVE, 0.8f, 2f);
        return true;
    }

    private boolean windPush(Player player, AbilityDefinition def) {
        Location l = player.getLocation();
        l.getWorld().spawnParticle(Particle.CLOUD, l.add(0,1,0), 40, 1.2,.6,1.2,.06);
        l.getWorld().playSound(l, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.4f);
        for (Entity e : l.getWorld().getNearbyEntities(l, 4, 2, 4)) {
            if (e instanceof LivingEntity le && e != player) {
                Vector kb = le.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.4).setY(.35);
                le.setVelocity(kb);
                le.damage(def.baseDamage(), player);
            }
        }
        return true;
    }

    private boolean windDoubleJump(Player player, PlayerData data) {
        if (!player.isOnGround()) return false;
        player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(0.9));
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 25, .5, .2, .5, .06);
        data.setCooldown("double_jump", 3000L);
        return true;
    }

    private boolean earthWall(Player player) {
        Location l = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(2));
        List<Block> changed = new ArrayList<>();
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                Block b = l.clone().add(x, y, 0).getBlock();
                if (b.getType().isAir()) {
                    b.setType(Material.DEEPSLATE_TILES);
                    changed.add(b);
                }
            }
        }
        l.getWorld().playSound(l, Sound.BLOCK_DEEPSLATE_PLACE, 1f, 0.8f);
        l.getWorld().spawnParticle(Particle.BLOCK, l.add(0,1,0), 50, 1,.8,.8, Material.DEEPSLATE.createBlockData());
        Bukkit.getScheduler().runTaskLater(plugin, () -> changed.forEach(b -> {
            if (b.getType() == Material.DEEPSLATE_TILES) b.setType(Material.AIR);
        }), 80L);
        return true;
    }

    private boolean stomp(Player player, AbilityDefinition def) {
        Location l = player.getLocation();
        l.getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 0.7f);
        l.getWorld().spawnParticle(Particle.BLOCK, l, 80, 2,.3,2, Material.DIRT.createBlockData());
        for (Entity e : l.getWorld().getNearbyEntities(l, 4, 2, 4)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(def.baseDamage() + plugin.levelBonus(player), player);
                le.setVelocity(new Vector(0, .45, 0));
            }
        }
        return true;
    }

    private boolean heal(Player player) {
        Location l = player.getLocation();
        for (Entity e : l.getWorld().getNearbyEntities(l, 5, 3, 5)) {
            if (e instanceof Player ally) {
                double max = Objects.requireNonNull(ally.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).getValue();
                ally.setHealth(Math.min(max, ally.getHealth() + 6));
                ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0));
                ally.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, ally.getLocation().add(0,1,0), 12, .4,.5,.4,.02);
            }
        }
        l.getWorld().playSound(l, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.7f);
        return true;
    }

    private boolean flash(Player player, AbilityDefinition def) {
        Location eye = player.getEyeLocation();
        eye.getWorld().spawnParticle(Particle.END_ROD, eye, 35, .6,.3,.6,.08);
        eye.getWorld().playSound(eye, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1f, 1.6f);
        for (Entity e : eye.getWorld().getNearbyEntities(eye, 6, 3, 6)) {
            if (e instanceof LivingEntity le && e != player) {
                le.damage(def.baseDamage(), player);
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            }
        }
        return true;
    }
}
