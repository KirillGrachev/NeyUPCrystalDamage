package eu.neydev.upcrystaldamage.command.sub;

import eu.neydev.upcrystaldamage.command.CommandContext;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.service.MessageService;
import eu.neydev.upcrystaldamage.support.StubConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Подкоманда info: диагностика собирается из снапшота конфигурации,
 * множитель печатается без хвостовых нулей, состояние прав - словом из конфига.
 */
class InfoSubCommandTest {

    private final Plugin plugin = mock(Plugin.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final StubConfig config = new StubConfig()
            .withMessage(MessageKey.INFO, "v={version} m={multiplier} b={bypass} s={bypass_state}")
            .withMessage(MessageKey.STATE_ON, "on")
            .withMessage(MessageKey.STATE_OFF, "off");

    @BeforeEach
    void setUp() {
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile(
                "NeyUPCrystalDamage", "2.0.0", "eu.neydev.upcrystaldamage.NeyUPCrystalDamage"));
    }

    private InfoSubCommand subCommand() {
        return new InfoSubCommand(plugin, config, new MessageService(config));
    }

    private String sent() {

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        verify(sender).sendMessage(message.capture());
        return message.getValue();

    }

    @Test
    @DisplayName("Название и узел права совпадают с plugin.yml")
    void nameAndPermissionMatchPluginYml() {

        assertEquals("info", subCommand().getName());
        assertEquals(PermissionNode.RELOAD, subCommand().getPermission());

    }

    @Test
    @DisplayName("Версия, множитель, узел обхода и состояние системы прав")
    void reportsVersionMultiplierAndBypass() {

        config.withMultiplier(0.1D);

        assertTrue(subCommand().execute(CommandContext.of(sender), new String[0]));

        assertEquals("v=2.0.0 m=0.1 b=crystal.damage.bypass s=off", sent());

    }

    @Test
    @DisplayName("Включенная система прав показывает state_on")
    void enabledPermissionsUseStateOn() {

        config.withPermissionsEnabled(true).withMultiplier(1.0D);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().endsWith("s=on"), sent());

    }

    @Test
    @DisplayName("Настраиваемый узел обхода попадает в диагностику")
    void configuredBypassNodeIsReported() {

        config.withPermission(PermissionNode.BYPASS, "donor.crystal");

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("b=donor.crystal"), sent());

    }

    @Test
    @DisplayName("Множитель печатается без хвостовых нулей")
    void multiplierIsPrintedWithoutTrailingZeros() {

        config.withMultiplier(4.0D);
        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("m=4 "), sent());

    }

    @Test
    @DisplayName("Ноль и дробный множитель не теряют разряды")
    void zeroAndFractionKeepTheirDigits() {

        config.withMultiplier(0.0D);
        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("m=0 "), sent());

    }

    @Test
    @DisplayName("Выключенное слово-фрагмент дает пустую подстановку")
    void disabledStateFragmentBecomesEmpty() {

        config.withMultiplier(2.5D).withMessage(MessageKey.STATE_OFF);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().endsWith("s="), sent());

    }

}
