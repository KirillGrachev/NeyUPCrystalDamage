package eu.neydev.upcrystaldamage.service;

import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.support.StubConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Доставка сообщений: префикс, плейсхолдеры, многострочность и отказы
 * (получатель null, сообщения выключены, игрок оффлайн).
 */
class MessageServiceTest {

    private final Player player = mock(Player.class);
    private final ConsoleCommandSender console = mock(ConsoleCommandSender.class);

    private MessageService service(StubConfig config) {
        return new MessageService(config);
    }

    @Test
    @DisplayName("Многострочное сообщение уходит построчно с плейсхолдерами")
    void sendsEveryLineWithPlaceholders() {

        StubConfig config = new StubConfig()
                .withPrefix("&8» ")
                .withMessage(MessageKey.INFO, "{prefix}multiplier {multiplier}", "second {multiplier}");
        when(player.isOnline()).thenReturn(true);

        service(config).send(player, MessageKey.INFO, Map.of("multiplier", "4"));

        verify(player).sendMessage("&8» multiplier 4");
        verify(player).sendMessage("second 4");

    }

    @Test
    @DisplayName("Явный плейсхолдер prefix важнее префикса из конфига")
    void explicitPrefixPlaceholderWins() {

        StubConfig config = new StubConfig()
                .withPrefix("config")
                .withMessage(MessageKey.RELOAD, "{prefix}done");

        List<String> lines = service(config).build(MessageKey.RELOAD, Map.of("prefix", "custom"));

        assertEquals(List.of("customdone"), lines);

    }

    @Test
    @DisplayName("Префикс не пришивается сам: только через {prefix}")
    void prefixIsNotPrependedAutomatically() {

        StubConfig config = new StubConfig()
                .withPrefix("PFX")
                .withMessage(MessageKey.RELOAD, "no placeholder here");

        assertEquals(List.of("no placeholder here"),
                service(config).build(MessageKey.RELOAD, Map.of()));

    }

    @Test
    @DisplayName("Выключенное сообщение дает пустой список и ничего не шлет")
    void disabledMessageSendsNothing() {

        StubConfig config = new StubConfig();
        when(player.isOnline()).thenReturn(true);

        assertEquals(List.of(), service(config).build(MessageKey.USAGE, Map.of()));

        service(config).send(player, MessageKey.USAGE);

        verify(player, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());

    }

    @Test
    @DisplayName("Мастер-выключатель сообщений глушит доставку")
    void masterSwitchStopsDelivery() {

        StubConfig config = new StubConfig()
                .withMessagesEnabled(false)
                .withMessage(MessageKey.USAGE, "line");

        service(config).send(player, MessageKey.USAGE);

        verifyNoInteractions(player);

    }

    @Test
    @DisplayName("Оффлайн-игрок сообщения не получает")
    void offlinePlayerIsSkipped() {

        StubConfig config = new StubConfig().withMessage(MessageKey.USAGE, "line");
        when(player.isOnline()).thenReturn(false);

        service(config).send(player, MessageKey.USAGE);

        verify(player, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());

    }

    @Test
    @DisplayName("Null-получатель не роняет отправку")
    void nullSenderIsIgnored() {

        StubConfig config = new StubConfig().withMessage(MessageKey.USAGE, "line");

        service(config).send((CommandSender) null, MessageKey.USAGE);

        assertEquals(List.of("line"), service(config).build(MessageKey.USAGE, Map.of()));

    }

    @Test
    @DisplayName("Консоль получает сообщение без проверки онлайна")
    void consoleReceivesMessages() {

        StubConfig config = new StubConfig().withMessage(MessageKey.RELOAD, "reloaded {time}");

        service(config).send(console, MessageKey.RELOAD, Map.of("time", "3"));

        verify(console).sendMessage("reloaded 3");

    }

    @Test
    @DisplayName("Перегрузка без плейсхолдеров делегирует основной")
    void sendWithoutPlaceholdersDelegates() {

        StubConfig config = new StubConfig().withMessage(MessageKey.MESSAGE_BEFORE_DAMAGE, "boom");
        when(player.isOnline()).thenReturn(true);

        service(config).send(player, MessageKey.MESSAGE_BEFORE_DAMAGE);

        verify(player).sendMessage("boom");

    }

}
