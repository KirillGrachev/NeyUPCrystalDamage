package eu.neydev.upcrystaldamage.command.sub;

import eu.neydev.upcrystaldamage.command.CommandContext;
import eu.neydev.upcrystaldamage.command.CrystalDamageSubCommand;
import eu.neydev.upcrystaldamage.config.ConfigManager;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.service.MessageService;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Подкоманда /crystaldamage reload.
 *
 * Перезагружает config.yml и сообщает затраченное время: на серверах с
 * медленным диском reload конфигурации - единственное место, где плагин
 * читает файл, и владельцу полезно видеть его стоимость.
 */
public class ReloadSubCommand implements CrystalDamageSubCommand {

    private final ConfigManager configManager;
    private final MessageService messageService;

    public ReloadSubCommand(@NotNull ConfigManager configManager,
                            @NotNull MessageService messageService) {
        this.configManager = configManager;
        this.messageService = messageService;
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull PermissionNode getPermission() {
        return PermissionNode.RELOAD;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        long startedAt = System.nanoTime();
        configManager.reload();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        messageService.send(context.sender(), MessageKey.RELOAD,
                Map.of("time", String.valueOf(elapsedMillis)));

        return true;

    }

}
