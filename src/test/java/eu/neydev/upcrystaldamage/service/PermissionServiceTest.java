package eu.neydev.upcrystaldamage.service;

import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.support.StubConfig;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Два противоположных режима узлов: функциональные при выключенной системе
 * прав разрешены всем, ограничивающий bypass - никому.
 */
class PermissionServiceTest {

    private final Player player = mock(Player.class);
    private final ConsoleCommandSender console = mock(ConsoleCommandSender.class);

    @Test
    @DisplayName("Выключенная система прав разрешает команды")
    void disabledSystemAllowsCommands() {

        PermissionService service = new PermissionService(new StubConfig());

        assertTrue(service.has(player, PermissionNode.RELOAD));
        assertTrue(service.has(null, PermissionNode.RELOAD), "даже без отправителя");

    }

    @Test
    @DisplayName("Выключенная система прав не дает обхода урона")
    void disabledSystemNeverBypasses() {

        when(player.hasPermission("crystal.damage.bypass")).thenReturn(true);

        assertFalse(new PermissionService(new StubConfig()).hasBypass(player),
                "иначе одна строка в конфиге молча лишала бы донатеров обхода");

    }

    @Test
    @DisplayName("Включенная система прав проверяет узел из конфига")
    void enabledSystemChecksConfiguredNode() {

        StubConfig config = new StubConfig()
                .withPermissionsEnabled(true)
                .withPermission(PermissionNode.RELOAD, "custom.reload");
        when(player.hasPermission("custom.reload")).thenReturn(true);

        assertTrue(new PermissionService(config).has(player, PermissionNode.RELOAD));
        verify(player).hasPermission("custom.reload");

    }

    @Test
    @DisplayName("Без нужного узла доступ закрыт")
    void missingNodeDeniesAccess() {

        StubConfig config = new StubConfig().withPermissionsEnabled(true);
        when(player.hasPermission("crystaldamage.reload")).thenReturn(false);

        assertFalse(new PermissionService(config).has(player, PermissionNode.RELOAD));

    }

    @Test
    @DisplayName("Null-отправитель при включенной системе прав не проходит")
    void nullSenderIsDeniedWhenEnabled() {
        assertFalse(new PermissionService(new StubConfig().withPermissionsEnabled(true))
                .has(null, PermissionNode.RELOAD));
    }

    @Test
    @DisplayName("Консоль проверяется так же, как игрок")
    void consoleIsCheckedLikePlayer() {

        StubConfig config = new StubConfig().withPermissionsEnabled(true);
        when(console.hasPermission("crystaldamage.reload")).thenReturn(true);

        assertTrue(new PermissionService(config).has(console, PermissionNode.RELOAD));

    }

    @Test
    @DisplayName("Обход урона работает по настраиваемому узлу")
    void bypassUsesConfiguredNode() {

        StubConfig config = new StubConfig()
                .withPermissionsEnabled(true)
                .withPermission(PermissionNode.BYPASS, "donor.crystal");
        when(player.hasPermission("donor.crystal")).thenReturn(true);

        assertTrue(new PermissionService(config).hasBypass(player));

    }

    @Test
    @DisplayName("Обход урона без игрока не срабатывает")
    void bypassWithoutPlayerIsFalse() {

        StubConfig config = new StubConfig().withPermissionsEnabled(true);

        assertFalse(new PermissionService(config).hasBypass(null));

    }

}
