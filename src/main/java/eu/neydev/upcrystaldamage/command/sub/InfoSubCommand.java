package eu.neydev.upcrystaldamage.command.sub;

import eu.neydev.upcrystaldamage.command.CommandContext;
import eu.neydev.upcrystaldamage.command.CrystalDamageSubCommand;
import eu.neydev.upcrystaldamage.config.CrystalDamageConfig;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.service.MessageService;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Подкоманда /crystaldamage info.
 *
 * Диагностика читается из снапшота конфигурации, а не из файла: команда
 * показывает ровно те значения, которые сейчас применяет слушатель урона.
 */
public class InfoSubCommand implements CrystalDamageSubCommand {

    private final Plugin plugin;
    private final CrystalDamageConfig config;
    private final MessageService messageService;

    public InfoSubCommand(@NotNull Plugin plugin,
                          @NotNull CrystalDamageConfig config,
                          @NotNull MessageService messageService) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
    }

    @Override
    public @NotNull String getName() {
        return "info";
    }

    @Override
    public @NotNull PermissionNode getPermission() {
        return PermissionNode.RELOAD;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        MessageKey stateKey = config.arePermissionsEnabled()
                ? MessageKey.STATE_ON
                : MessageKey.STATE_OFF;

        messageService.send(context.sender(), MessageKey.INFO, Map.of(
                "version", plugin.getDescription().getVersion(),
                "multiplier", formatMultiplier(config.getDamageMultiplier()),
                "bypass", config.getPermission(PermissionNode.BYPASS),
                "bypass_state", String.join("", config.getMessages(stateKey))
        ));

        return true;

    }

    /**
     * Печатает множитель без хвостовых нулей: 4.0 читается как 4, а 0.1
     * остается 0.1. BigDecimal, а не String.format, чтобы не получать 0.100000.
     */
    private @NotNull String formatMultiplier(double multiplier) {
        return BigDecimal.valueOf(multiplier).stripTrailingZeros().toPlainString();
    }

}
