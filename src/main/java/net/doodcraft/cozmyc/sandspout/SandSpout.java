package net.doodcraft.cozmyc.sandspout;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class SandSpout extends SandAbility implements AddonAbility {

    private final List<Location> currentSpoutLocations;

    @Attribute("BlindnessDuration")
    private final int blindnessDuration;
    @Attribute(Attribute.COOLDOWN)
    private final long cooldown;
    @Attribute(Attribute.DAMAGE)
    private final double damage;
    @Attribute(Attribute.SPEED)
    private final double flySpeed;
    @Attribute("Height")
    private final double height;

    private final String mainSoundName;
    private final double mainSoundVolume;
    private final double mainSoundPitch;

    private final double initialSpeed;
    private final Map<UUID, Long> lastBlinded;
    private final int queueSize;
    private Queue<Location> previousLocations;

    public SandSpout(Player player) {
        super(player);

        this.currentSpoutLocations = new ArrayList<>();

        this.blindnessDuration = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Cozmyc.SandSpout.BlindnessTime");
        this.cooldown = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Cozmyc.SandSpout.Cooldown");
        this.damage = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SandSpout.SpoutDamage");
        this.flySpeed = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SandSpout.FlySpeed");
        this.height = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SandSpout.Height");
        this.mainSoundName = ConfigManager.defaultConfig.get().getString("ExtraAbilities.Cozmyc.SandSpout.Sound.Name");
        this.mainSoundVolume = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SandSpout.Sound.Volume");
        this.mainSoundPitch = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SandSpout.Sound.Pitch");

        this.initialSpeed = this.player.getFlySpeed();
        this.lastBlinded = new ConcurrentHashMap<>();
        this.previousLocations = new LinkedList<>();
        this.queueSize = 9;

        if (!initializeAbility()) return;
        start();
        this.bPlayer.addCooldown(this);
    }

    private boolean initializeAbility() {
        if (getAbility(this.player, SandSpout.class) != null) {
            getAbility(this.player, SandSpout.class).remove();
            return false;
        }

        if (!this.bPlayer.canBend(this)) {
            return false;
        }

        return isValidSandBlock(this.player.getLocation());
    }

    @Override
    public void remove() {
        if (this.lastBlinded != null) this.lastBlinded.clear();
        resetFlight();
        super.remove();
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public List<Location> getLocations() {
        return new ArrayList<>(this.currentSpoutLocations);
    }

    private boolean isValidSandBlock(Location location) {
        Block topBlock = GeneralMethods.getTopBlock(location, 0, -50);
        if (topBlock == null) topBlock = location.getBlock();

        Material material = topBlock.getType();
        return EarthAbility.isSand(material) && !DensityShift.isPassiveSand(topBlock);
    }

    private void resetFlight() {
        this.player.setAllowFlight(this.player.getGameMode() == GameMode.SPECTATOR || this.player.getGameMode() == GameMode.CREATIVE);
        this.player.setFlying(this.player.getGameMode() == GameMode.SPECTATOR || this.player.getGameMode() == GameMode.CREATIVE);
        this.player.setFlySpeed((float) this.initialSpeed);
    }

    @Override
    public void progress() {
        if (shouldRemovePlayer()) {
            remove();
            return;
        }

        for (Ability ability : ElementalAbility.getAbilitiesByInstances()) {
            if (ability.getPlayer().getUniqueId().equals(this.player.getUniqueId()) && (ability.getName().equalsIgnoreCase("airspout") || ability.getName().equalsIgnoreCase("waterspout"))) {
                ability.remove();
            }
        }

        this.player.setFallDistance(0);
        this.player.setSprinting(false);

        Block groundBlock = getGroundBlock();
        if (groundBlock == null || !EarthAbility.isSand(groundBlock.getType()) || DensityShift.isPassiveSand(groundBlock)) {
            this.bPlayer.addCooldown(this);
            remove();
            return;
        }

        double playerHeightAboveGround = this.player.getLocation().getY() - groundBlock.getY();
        if (playerHeightAboveGround > this.height) {
            resetFlight();
        } else {
            enableFlight();
        }

        if (this.previousLocations == null) {
            this.previousLocations = new LinkedList<>();
        }

        this.previousLocations.add(this.player.getLocation().clone());
        if (this.previousLocations.size() > this.queueSize) {
            this.previousLocations.poll();
        }

        rotateSandColumn(groundBlock);
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "SandSpout";
    }

    @Override
    public Location getLocation() {
        return this.player != null ? this.player.getLocation() : null;
    }

    private boolean shouldRemovePlayer() {
        Block eyeBlock = this.player.getEyeLocation().getBlock();
        return !this.bPlayer.canBendIgnoreBindsCooldowns(this) || eyeBlock.isLiquid() || GeneralMethods.isSolid(eyeBlock);
    }

    private Block getGroundBlock() {
        Block standingBlock = this.player.getLocation().getBlock();
        for (int i = 0; i <= this.height + 5; i++) {
            Block block = standingBlock.getRelative(BlockFace.DOWN, i);
            if (GeneralMethods.isSolid(block) || block.isLiquid()) {
                return block;
            }
        }
        return null;
    }

    private void enableFlight() {
        this.player.setAllowFlight(true);
        this.player.setFlying(true);
        this.player.setFlySpeed((float) this.flySpeed);
    }

    private void rotateSandColumn(Block block) {
        Location baseLocation = previousLocations.peek();
        if (baseLocation == null) {
            baseLocation = this.player.getLocation().clone();
        }

        Location playerLocation = this.player.getLocation();
        Location blockLocation = new Location(block.getLocation().getWorld(), baseLocation.getX(), block.getLocation().getY(), baseLocation.getZ());

        double playerHeightAboveGround = playerLocation.getY() - block.getY();
        if (playerHeightAboveGround > this.height) {
            playerHeightAboveGround = this.height;
        }

        double steps = Math.ceil(playerHeightAboveGround);
        int soundCounter = 0;
        this.currentSpoutLocations.clear();

        for (int step = 0; step <= steps; step++) {
            double t = step / steps;
            double x = baseLocation.getX() + (playerLocation.getX() - baseLocation.getX()) * t;
            double y = block.getY() + step;
            double z = baseLocation.getZ() + (playerLocation.getZ() - baseLocation.getZ()) * t;

            Location effectLocation = new Location(blockLocation.getWorld(), x, y, z);
            if (!isFinite(effectLocation)) return;

            this.currentSpoutLocations.add(effectLocation);

            if (soundCounter % 4 == 0 && new Random().nextInt(4) == 0) {
                playSound(effectLocation);
            }
            soundCounter++;

            if (effectLocation.getWorld() == null) continue;

            if (step < playerHeightAboveGround * 0.15) {
                effectLocation.getWorld().spawnParticle(Particle.BLOCK, effectLocation, 6, Math.random(), Math.random(), Math.random(), 0.0, block.getBlockData(), true);
                effectLocation.getWorld().spawnParticle(Particle.ITEM, effectLocation, 3, Math.random(), Math.random(), Math.random(), 0.0, new ItemStack(block.getType()), true);
                effectLocation.getWorld().spawnParticle(Particle.BLOCK, effectLocation, 3, Math.random(), Math.random(), Math.random(), 0.0, block.getBlockData(), true);
            } else if (step < playerHeightAboveGround * 0.2) {
                effectLocation.getWorld().spawnParticle(Particle.BLOCK, effectLocation, 1, 0.5, Math.random(), 0.5, 0.0, block.getBlockData(), true);
            } else if (step < playerHeightAboveGround * 0.9) {
                effectLocation.getWorld().spawnParticle(Particle.ITEM, effectLocation, 1, 0.5, Math.random(), 0.5, 0.0, new ItemStack(block.getType()), true);
            } else {
                effectLocation.getWorld().spawnParticle(Particle.BLOCK, effectLocation, 2, Math.random(), Math.min(Math.random(), 0.5), Math.random(), 0.0, block.getBlockData(), true);
                effectLocation.getWorld().spawnParticle(Particle.ITEM, effectLocation, 9, 0.5, Math.min(Math.random(), 0.5), 0.5, 0.0, new ItemStack(block.getType()), true);
                effectLocation.getWorld().spawnParticle(Particle.FALLING_DUST, effectLocation, 1, Math.random(), Math.min(Math.random(), 0.5), Math.random(), 0.0, block.getBlockData(), true);
            }

            displaySandParticles(effectLocation, block.getType());
            applyEffectsToNearbyEntities(effectLocation);
        }
    }

    public boolean isFinite(Location loc) {
        return Double.isFinite(loc.getX()) && Double.isFinite(loc.getY()) && Double.isFinite(loc.getZ());
    }

    private void playSound(Location location) {
        if (location.getWorld() == null) return;
        NamespacedKey key = NamespacedKey.fromString(this.mainSoundName);
        if (key == null) return;
        Sound sound = Registry.SOUNDS.get(key);
        if (sound == null) return;
        location.getWorld().playSound(location, sound, (float) this.mainSoundVolume, (float) this.mainSoundPitch);
    }

    private void displaySandParticles(Location location, Material material) {
        if (location.getWorld() == null) return;
        location.getWorld().spawnParticle(Particle.BLOCK, location, 4, 0.5, Math.random(), 0.5, 0.0, material.createBlockData(), true);
        if (material == Material.SOUL_SAND) {
            location.getWorld().spawnParticle(Particle.SOUL, location, 1, Math.random(), Math.random(), Math.random(), 0.02f, null, true);
        }
    }

    private void applyEffectsToNearbyEntities(Location location) {
        Collection<Entity> nearbyEntities = GeneralMethods.getEntitiesAroundPoint(location, 1.5f);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity)) continue;
            if (!entity.equals(this.player)) {
                long currentTime = System.currentTimeMillis();
                long lastHurtTime = this.lastBlinded.getOrDefault(entity.getUniqueId(), 0L);
                if (currentTime - lastHurtTime >= 1000) {
                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, this.blindnessDuration * 20, 1));
                    DamageHandler.damageEntity(entity, this.damage, this);
                    this.lastBlinded.put(entity.getUniqueId(), currentTime);
                }
            }
        }
    }

    @Override
    public Element getElement() {
        return Element.SAND;
    }

    @Override
    public void load() {
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new SandSpoutListener(), ProjectKorra.plugin);
        
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.Cooldown", 0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.Height", 10);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.BlindnessTime", 10);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.SpoutDamage", 0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.Sound.Name", "minecraft:block.soul_sand.fall");
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.Sound.Volume", 0.3);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.Sound.Pitch", 0.1);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SandSpout.FlySpeed", 0.11);
        ConfigManager.defaultConfig.save();

        ConfigManager.languageConfig.get().addDefault("Abilities.Earth.SandSpout.DeathMessage", "{victim} was buried alive under {attacker}'s {ability}");
        ConfigManager.languageConfig.get().addDefault("Abilities.Earth.SandSpout.Description", "An advanced Sandbending skill that takes advantage of the properties of the element to form a mobile column of sand. Sandbenders are able to use this ability for travelling, evasion, to gain height advantage in combat and to build. The erosion generated from the column will blind and damage entities standing below.");
        ConfigManager.languageConfig.get().addDefault("Abilities.Earth.SandSpout.Instructions", "You must be standing on sand to use this ability. Left Click to activate, Space to go up, Shift to go down.");
        ConfigManager.languageConfig.save();

        setupCollisions();

        ProjectKorra.plugin.getLogger().info("Loaded SandSpout " + getVersion() + " by Cozmyc and LuxaelNI!");
    }

    @Override
    public void stop() {
        ProjectKorra.plugin.getLogger().info("Unloaded SandSpout!");
    }

    @Override
    public String getAuthor() {
        return "Cozmyc, LuxaelNI";
    }

    @Override
    public String getVersion() {
        return "1.1.0";
    }

    private void setupCollisions() {
        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
            List<Collision> collisions = ProjectKorra.getCollisionManager().getCollisions().stream()
                    .filter(collision -> collision.getAbilitySecond().getName().equalsIgnoreCase("airspout"))
                    .map(collision -> new Collision(CoreAbility.getAbility(collision.getAbilityFirst().getClass()), this, false, true))
                    .collect(Collectors.toList());
            collisions.forEach(ProjectKorra.getCollisionManager()::addCollision);
        }, 1L);
    }
}
