package dev.continuum.duels.config;

import dev.continuum.duels.game.GamePlayer;
import dev.continuum.duels.util.Files;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.model.Tuple;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unchecked")
public class Messages {
    @NotNull
    public static FileConfiguration config() {
        try {
            return CompletableFuture.supplyAsync(() -> Files.config(Files.create(Files.file("messages.yml")))).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static TextMessage message(final @NotNull String name) {
        final String textKey = name + ".text";
        final String typeKey = name + ".type";
        final String soundsKey = name + ".sounds";
        final String soundKey = soundsKey + ".sound";
        final String pitchKey = soundsKey + ".pitch";
        final String volumeKey = soundsKey + ".volume";

        final FileConfiguration config = config();

        Object raw = config.get(textKey);
        String type = config.getString(typeKey);

        if (raw == null) raw = name;
        if (type == null) type = "chat";

        TextMessageType textMessageType = TextMessageType.CHAT;
        if (type.equalsIgnoreCase("action bar") || type.equalsIgnoreCase("action_bar")) {
            textMessageType = TextMessageType.ACTION_BAR;
        }

        if (raw instanceof List<?> list) {
            final String soundRaw = config.getString(soundKey);
            final double pitch = config.getDouble(pitchKey);
            final double volume = config.getDouble(volumeKey);

            final TextMessage message = new TextMessage(textMessageType, (List<String>) list);

            if (soundRaw != null && pitch != -1 && volume != -1) {
                message.sound(soundRaw, pitch, volume);
            }

            return message;
        } else if (raw instanceof String string) {
            final String soundRaw = config.getString(soundKey);
            final double pitch = config.getDouble(pitchKey);
            final double volume = config.getDouble(volumeKey);

            final TextMessage message = new TextMessage(textMessageType, string);

            if (soundRaw != null && pitch != -1 && volume != -1) {
                message.sound(soundRaw, pitch, volume);
            }

            return message;
        }

        return new TextMessage(textMessageType, raw.toString());
    }

    @NotNull
    public static TextMessage message(final @NotNull String name, final @NotNull Elements<Tuple<String, String>> replacements) {
        final String textKey = name + ".text";
        final String typeKey = name + ".type";
        final String soundsKey = name + ".sounds";
        final String soundKey = soundsKey + ".sound";
        final String pitchKey = soundsKey + ".pitch";
        final String volumeKey = soundsKey + ".volume";

        final FileConfiguration config = config();

        Object raw = config.get(textKey);
        String type = config.getString(typeKey);

        if (raw == null) raw = name;
        if (type == null) type = "chat";

        TextMessageType textMessageType = TextMessageType.CHAT;
        if (type.equalsIgnoreCase("action bar") || type.equalsIgnoreCase("action_bar")) {
            textMessageType = TextMessageType.ACTION_BAR;
        }

        if (raw instanceof List<?> list) {
            final String soundRaw = config.getString(soundKey);
            final double pitch = config.getDouble(pitchKey);
            final double volume = config.getDouble(volumeKey);

            final TextMessage message = new TextMessage(textMessageType, (List<String>) list, replacements);

            if (soundRaw != null && pitch != -1 && volume != -1) {
                message.sound(soundRaw, pitch, volume);
            }

            return message;
        } else if (raw instanceof String string) {
            final String soundRaw = config.getString(soundKey);
            final double pitch = config.getDouble(pitchKey);
            final double volume = config.getDouble(volumeKey);

            final TextMessage message = new TextMessage(textMessageType, string, replacements);

            if (soundRaw != null && pitch != -1 && volume != -1) {
                message.sound(soundRaw, pitch, volume);
            }

            return message;
        }

        return new TextMessage(textMessageType, raw.toString());
    }

    public static void findAndSend(final @NotNull String name, final @NotNull Audience audience) {
        final TextMessage message = message(name);
        if (message.type() == TextMessageType.CHAT) {
            audience.sendMessage(message.text());
        } else {
            audience.sendActionBar(message.text());
        }

        final org.bukkit.Sound sound = message.sound();

        if (sound != null && message.pitch() != -1 && message.volume() != -1) {
            audience.playSound(Sound.sound(builder -> {
                builder.pitch((float) message.pitch());
                builder.volume((float) message.volume());
                builder.type(sound.key());
            }));
        }
    }

    public static void findAndSend(final @NotNull String name, final @NotNull Elements<Tuple<String, String>> replacements, final @NotNull Audience audience) {
        final TextMessage message = message(name, replacements);
        if (message.type() == TextMessageType.CHAT) {
            audience.sendMessage(message.text());
        } else {
            audience.sendActionBar(message.text());
        }

        final org.bukkit.Sound sound = message.sound();

        if (sound != null && message.pitch() != -1 && message.volume() != -1) {
            audience.playSound(Sound.sound(builder -> {
                builder.pitch((float) message.pitch());
                builder.volume((float) message.volume());
                builder.type(sound.key());
            }));
        }
    }

    public static void message(final @NotNull String key, final @NotNull Player player, final @NotNull PlaceholderReplacements replacements) {
        findAndSend(key, replacements.replacements(Elements.of()), player);
    }

    public static void message(final @NotNull String key, final @NotNull GamePlayer player, final @NotNull PlaceholderReplacements replacements) {
        message(key, player.player(), replacements);
    }

    public static interface PlaceholderReplacements {
        @NotNull
        Elements<Tuple<String, String>> replacements(final @NotNull Elements<Tuple<String, String>> elements);
    }
}
