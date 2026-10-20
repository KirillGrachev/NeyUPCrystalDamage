package eu.neydev.upcrystaldamage.command;

import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.service.MessageService;
import eu.neydev.upcrystaldamage.service.PermissionService;
import eu.neydev.upcrystaldamage.support.StubConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Маршрутизатор команды: чужие команды игнорируются, справка вместо
 * неизвестной подкоманды, права и автодополнение.
 */
class CrystalDamageCommandTest {

    private final CommandSender sender = mock(CommandSender.class);
    private final Command command = mock(Command.class);
    private final StubConfig config = new StubConfig()
            .withMessage(MessageKey.USAGE, "usage")
            .withMessage(MessageKey.NO_PERMISSION, "denied");

    private CrystalDamageCommand command(List<CrystalDamageSubCommand> subCommands) {
        return new CrystalDamageCommand(new MessageService(config),
                new PermissionService(config), subCommands);
    }

    @BeforeEach
    void setUp() {
        when(command.getName()).thenReturn("crystaldamage");
    }

    @Test
    @DisplayName("Чужая команда не обрабатывается")
    void foreignCommandIsIgnored() {

        when(command.getName()).thenReturn("gps");
        RecordingSubCommand reload = new RecordingSubCommand("reload", null, List.of());

        assertFalse(command(List.of(reload)).onCommand(sender, command, "gps", new String[0]));
        assertEquals(List.of(),
                command(List.of(reload)).onTabComplete(sender, command, "gps", new String[]{""}));
        assertEquals(0, reload.calls);

    }

    @Test
    @DisplayName("Без аргументов печатается справка")
    void noArgumentsPrintUsage() {

        RecordingSubCommand reload = new RecordingSubCommand("reload", null, List.of());

        assertTrue(command(List.of(reload)).onCommand(sender, command, "crystaldamage", new String[0]));

        verify(sender).sendMessage("usage");
        assertEquals(0, reload.calls);

    }

    @Test
    @DisplayName("Неизвестная подкоманда тоже печатает справку")
    void unknownSubCommandPrintsUsage() {

        RecordingSubCommand reload = new RecordingSubCommand("reload", null, List.of());

        command(List.of(reload)).onCommand(sender, command, "crystaldamage", new String[]{"fly"});

        verify(sender).sendMessage("usage");
        assertEquals(0, reload.calls);

    }

    @Test
    @DisplayName("Подкоманда находится без учета регистра и получает хвост аргументов")
    void subCommandIsCaseInsensitiveAndReceivesTheTail() {

        RecordingSubCommand reload = new RecordingSubCommand("reload", null, List.of());

        command(List.of(reload)).onCommand(sender, command, "crystaldamage",
                new String[]{"RELOAD", "a", "b"});

        assertEquals(1, reload.calls);
        assertArrayEquals(new String[]{"a", "b"}, reload.lastArgs);

    }

    @Test
    @DisplayName("Без прав подкоманда не выполняется")
    void subCommandWithoutPermissionIsRefused() {

        config.withPermissionsEnabled(true);
        when(sender.hasPermission(PermissionNode.RELOAD.getDefaultPermission())).thenReturn(false);

        RecordingSubCommand reload = new RecordingSubCommand("reload", PermissionNode.RELOAD, List.of());

        command(List.of(reload)).onCommand(sender, command, "crystaldamage", new String[]{"reload"});

        verify(sender).sendMessage("denied");
        assertEquals(0, reload.calls);

    }

    @Test
    @DisplayName("С правом подкоманда выполняется")
    void subCommandWithPermissionIsExecuted() {

        config.withPermissionsEnabled(true);
        when(sender.hasPermission(PermissionNode.RELOAD.getDefaultPermission())).thenReturn(true);

        RecordingSubCommand reload = new RecordingSubCommand("reload", PermissionNode.RELOAD, List.of());

        command(List.of(reload)).onCommand(sender, command, "crystaldamage", new String[]{"reload"});

        assertEquals(1, reload.calls);

    }

    @Test
    @DisplayName("Подкоманда без узла права доступна всем даже при включенной системе")
    void subCommandWithoutNodeIsPublic() {

        config.withPermissionsEnabled(true);
        RecordingSubCommand help = new RecordingSubCommand("help", null, List.of());

        command(List.of(help)).onCommand(sender, command, "crystaldamage", new String[]{"help"});

        assertEquals(1, help.calls);

    }

    @Test
    @DisplayName("Автодополнение первого аргумента фильтрует по префиксу")
    void tabCompleteFiltersByPrefix() {

        List<CrystalDamageSubCommand> subCommands = List.of(
                new RecordingSubCommand("reload", null, List.of()),
                new RecordingSubCommand("info", null, List.of()));

        assertEquals(List.of("reload"),
                command(subCommands).onTabComplete(sender, command, "crystaldamage", new String[]{"re"}));
        assertEquals(List.of("reload", "info"),
                command(subCommands).onTabComplete(sender, command, "crystaldamage", new String[]{""}));

    }

    @Test
    @DisplayName("Автодополнение прячет подкоманды без доступа")
    void tabCompleteHidesUnavailableSubCommands() {

        config.withPermissionsEnabled(true);
        when(sender.hasPermission(PermissionNode.RELOAD.getDefaultPermission())).thenReturn(false);

        List<CrystalDamageSubCommand> subCommands = List.of(
                new RecordingSubCommand("reload", PermissionNode.RELOAD, List.of()),
                new RecordingSubCommand("help", null, List.of()));

        assertEquals(List.of("help"),
                command(subCommands).onTabComplete(sender, command, "crystaldamage", new String[]{""}));

    }

    @Test
    @DisplayName("Второй аргумент делегируется подкоманде и фильтруется")
    void tabCompleteDelegatesToSubCommand() {

        RecordingSubCommand reload = new RecordingSubCommand("reload", null, List.of("fast", "slow", "other"));

        assertEquals(List.of("slow"), command(List.of(reload))
                .onTabComplete(sender, command, "crystaldamage", new String[]{"reload", "s"}));

    }

    @Test
    @DisplayName("Автодополнение неизвестной подкоманды пусто")
    void tabCompleteUnknownSubCommandIsEmpty() {

        RecordingSubCommand reload = new RecordingSubCommand("reload", null, List.of("fast"));

        assertEquals(List.of(), command(List.of(reload))
                .onTabComplete(sender, command, "crystaldamage", new String[]{"fly", ""}));

    }

    @Test
    @DisplayName("Автодополнение недоступной подкоманды пусто")
    void tabCompleteOfDeniedSubCommandIsEmpty() {

        config.withPermissionsEnabled(true);
        when(sender.hasPermission(PermissionNode.RELOAD.getDefaultPermission())).thenReturn(false);

        RecordingSubCommand reload = new RecordingSubCommand("reload", PermissionNode.RELOAD, List.of("fast"));

        assertEquals(List.of(), command(List.of(reload))
                .onTabComplete(sender, command, "crystaldamage", new String[]{"reload", ""}));

    }

    @Test
    @DisplayName("Подкоманда без своего автодополнения ничего не подсказывает")
    void defaultCompletionIsEmpty() {

        MinimalSubCommand minimal = new MinimalSubCommand();

        assertEquals(List.of(), command(List.of(minimal))
                .onTabComplete(sender, command, "crystaldamage", new String[]{"minimal", ""}));

        assertTrue(command(List.of(minimal)).onCommand(sender, command, "crystaldamage",
                new String[]{"minimal"}));

    }

    /** Подкоманда-регистратор: запоминает факт вызова и полученные аргументы. */
    private static final class RecordingSubCommand implements CrystalDamageSubCommand {

        private final String name;
        private final PermissionNode permission;
        private final List<String> completions;

        private String[] lastArgs = new String[0];
        private int calls;

        private RecordingSubCommand(String name, PermissionNode permission, List<String> completions) {
            this.name = name;
            this.permission = permission;
            this.completions = completions;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public @Nullable PermissionNode getPermission() {
            return permission;
        }

        @Override
        public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

            this.lastArgs = args;
            this.calls++;
            return true;

        }

        @Override
        public @NotNull List<String> complete(@NotNull CommandContext context, String @NotNull [] args) {
            return completions;
        }

    }

    /** Подкоманда без переопределенного автодополнения - для проверки default-метода. */
    private static final class MinimalSubCommand implements CrystalDamageSubCommand {

        @Override
        public @NotNull String getName() {
            return "minimal";
        }

        @Override
        public @Nullable PermissionNode getPermission() {
            return null;
        }

        @Override
        public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {
            return true;
        }

    }

}
