package org.ney.neyupcrystaldamage.Modules.Utils;

import org.ney.neyupcrystaldamage.NeyUPCrystalDamage;

import java.util.List;
import java.util.stream.Collectors;

public class MainUtils {

    public final double DamageMultiplier = getDouble("Settings.multiplier");
    ;
    public final boolean exp = getBoolean("Settings.drop-exp");
    public final boolean messagesend = getBoolean("Messages.message-before-damage.enabled");

    public double getDouble(String message) {
        return NeyUPCrystalDamage.getInstance().getConfig().getDouble(message);
    }

    public boolean getBoolean(String message) {
        return NeyUPCrystalDamage.getInstance().getConfig().getBoolean(message);
    }

    public List<String> getStringList(String message) {

        return NeyUPCrystalDamage.getInstance().getConfig().getStringList(message)
                .stream()
                .map(Hex::color)
                .collect(Collectors.toList());

    }
}