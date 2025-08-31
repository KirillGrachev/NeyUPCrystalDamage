package org.ney.crystal_damage.service;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExperienceDropService {

    public void spawnExperienceOrb(@NotNull Player player, double damage) {

        int xp = (int) Math.ceil(damage);
        Location location = player.getLocation();

        ExperienceOrb orb = (ExperienceOrb) location.getWorld()
                .spawnEntity(location, EntityType.EXPERIENCE_ORB);

        orb.setExperience(xp);

    }
}