package org.ney.neyupcrystaldamage.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.ney.neyupcrystaldamage.Modules.Utils.MainUtils;

public class DamageByEntity implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        MainUtils mainUtils = new MainUtils();

        Entity damager = event.getDamager();
        Entity damagedEntity = event.getEntity();

        if (damager.getType() != EntityType.ENDER_CRYSTAL || !(damagedEntity instanceof Player)) return;

        Player player = (Player) damagedEntity;

        if (mainUtils.messagesend) mainUtils.getStringList("Messages.message-before-damage.text").forEach(player::sendMessage);

        double damage = event.getDamage() * mainUtils.DamageMultiplier;
        event.setDamage(damage);

        if (damage == 0) {

            event.setCancelled(true);
            return;

        }

        if (mainUtils.exp) {

            int xp = (int) Math.ceil(damage);
            ExperienceOrb xpOrb = (ExperienceOrb) player.getWorld().spawnEntity(player.getLocation(), EntityType.EXPERIENCE_ORB);
            xpOrb.setExperience(xp);

        }
    }
}