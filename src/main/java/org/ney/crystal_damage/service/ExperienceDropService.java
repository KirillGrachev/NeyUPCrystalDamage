package org.ney.crystal_damage.service;

import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExperienceDropService {

    /**
     * Спавнит орб опыта возле игрока
     */
    public void spawnExperienceOrb(@NotNull Player player, double damage) {

        int xp = (int) Math.ceil(damage);
        Location location = player.getLocation();

        ExperienceOrb orb = (ExperienceOrb) location.getWorld()
                .spawnEntity(location, org.bukkit.entity.EntityType.EXPERIENCE_ORB);

        orb.setExperience(xp);

    }
}