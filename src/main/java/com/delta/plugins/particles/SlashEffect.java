package com.delta.plugins.particles;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.ParticleEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * A simple slash particle effect that extends in front of the target player up to a maximum length.
 * The effect advances over time according to the configured speed (blocks per second).
 */
public class SlashEffect extends ParticleEffect {

    // Maximum length of the slash in blocks (default 7 as requested)
    public double length = 7.0;

    // Speed in blocks per second. Default chosen so 7 blocks are reached in ~0.3s -> 7 / 0.3
    public double speed = 7.0 / 0.3; // ≈ 23.3333 blocks/sec

    // Particle appearance
    private final Color color = Color.RED;
    private final float size = 1.0f;

    // internal step counter (number of onRun executions)
    private int step = 0;

    // explicit target player (allows callers to set target even if EffectLib naming differs)
    private Player targetPlayer = null;

    public SlashEffect(EffectManager effectManager) {
        super(effectManager);
        this.particle = Particle.DUST;
        // run every tick (EffectLib runs with period in ticks); default period is fine
    }

    /**
     * Set the length in blocks (maximum reach).
     */
    public void setLength(double length) {
        this.length = Math.max(0, length);
    }

    /**
     * Set the speed in blocks per second. A larger value makes the slash reach its max sooner.
     */
    public void setSpeed(double speed) {
        this.speed = Math.max(0, speed);
    }

    /**
     * Set the target player for this effect. Some callers expect setTargetPlayer exist.
     */
    public void setTargetPlayer(Player p) {
        this.targetPlayer = p;
    }

    @Override
    public void onRun() {
        // Advance internal step and compute how far the slash has reached so far
        step++;

        Player player = (this.targetPlayer != null ? this.targetPlayer : getTargetPlayer());
        if (player == null || !player.isValid()) {
            cancel();
            return;
        }

        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().clone().normalize();

        // EffectLib's onRun is called once per tick by default -> 20 ticks per second
        double blocksPerTick = this.speed / 20.0;
        double currentReach = step * blocksPerTick;

        // If we've reached or exceeded the maximum length, draw the final frame and finish
        boolean finished = false;
        if (currentReach >= this.length) {
            currentReach = this.length;
            finished = true;
        }

        // Draw particles along the line from the eye outwards up to currentReach
        try {
            DustOptions opts = new DustOptions(this.color, this.size);
            // sample density (distance between particle points)
            double sampleStep = 0.25; // quarter-block spacing
            for (double d = 0; d <= currentReach; d += sampleStep) {
                Location point = eye.clone().add(dir.clone().multiply(d));
                // spawn a single dust particle at this point
                player.getWorld().spawnParticle(Particle.DUST, point, 1, opts);
            }
        } catch (Throwable ignored) {}

        if (finished) {
            cancel();
        }
    }

    //useful lines
    //getTargetPlayer()

}
