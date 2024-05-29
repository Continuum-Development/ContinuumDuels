package dev.continuum.duels.config;

import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.TimeUnitParser;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.misc.ObjectUtils;
import dev.manere.utils.model.Tuple;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class Messages {
    @NotNull
    public static FileConfiguration config() {
        try {
            return CompletableFuture.supplyAsync(
                () -> Files.config(Files.create(Files.file("messages.yml")))
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static TextMessage of(final @NotNull String name) {
        return of(name, Elements.of());
    }

    @NotNull
    private static TextMessageType type(final @NotNull String type) {
        TextMessageType textMessageType = TextMessageType.CHAT;

        if (type.equalsIgnoreCase("action bar") || type.equalsIgnoreCase("action_bar")) {
            textMessageType = TextMessageType.ACTION_BAR;
        } else if (type.equalsIgnoreCase("both")) {
            textMessageType = TextMessageType.BOTH;
        } else if (type.equalsIgnoreCase("title")) {
            textMessageType = TextMessageType.TITLE;
        }

        return textMessageType;
    }

    @NotNull
    public static TextMessage of(final @NotNull String name, final @NotNull Elements<Tuple<String, String>> replacements) {
        /*final String textKey = name + ".text";
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

        final TextMessageType textMessageType = type(type);

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

        return new TextMessage(textMessageType, raw.toString());*/

        final FileConfiguration config = config();

        final String soundsKey = name + ".sounds";
        final String soundKey = soundsKey + ".sound";
        final String pitchKey = soundsKey + ".pitch";
        final String volumeKey = soundsKey + ".volume";

        final Object rawText = config.get(name + ".text");
        final String rawType = config.getString(name + ".type", "chat");
        final TextMessageType type = type(rawType);

        if (rawText == null && type != TextMessageType.TITLE) return new TextMessage(type, "", replacements);

        if (type == TextMessageType.TITLE) {
            final String soundRaw = config.getString(soundKey);
            final double pitch = config.getDouble(pitchKey);
            final double volume = config.getDouble(volumeKey);

            // todo
            final TextMessage message = new TextMessage(type, "", replacements);

            if (soundRaw != null && pitch != -1 && volume != -1) {
                message.sound(soundRaw, pitch, volume);
            }

            message.title(
                config.getString(name + "title", ""),
                config.getString(name + "subtitle", ""),
                Duration.of(config.getInt(name + "fade_in.time", 0), TimeUnitParser.parse(config.getString(name + "fade_in.unit", "seconds")).toChronoUnit()),
                Duration.of(config.getInt(name + "stay.time", 1), TimeUnitParser.parse(config.getString(name + "stay.unit", "seconds")).toChronoUnit()),
                Duration.of(config.getInt(name + "fade_out.time", 0), TimeUnitParser.parse(config.getString(name + "fade_out.unit", "seconds")).toChronoUnit())
            );

            return message;
        }

        if (rawText instanceof List<?> list) {
            final String soundRaw = config.getString(soundKey);
            final double pitch = config.getDouble(pitchKey);
            final double volume = config.getDouble(volumeKey);

            final TextMessage message = new TextMessage(type, (List<String>) list, replacements);

            if (soundRaw != null && pitch != -1 && volume != -1) {
                message.sound(soundRaw, pitch, volume);
            }

            return message;
        } else if (rawText instanceof String string) {
            final String soundRaw = config.getString(soundKey);
            final double pitch = config.getDouble(pitchKey);
            final double volume = config.getDouble(volumeKey);

            final TextMessage message = new TextMessage(type, string, replacements);

            if (soundRaw != null && pitch != -1 && volume != -1) {
                message.sound(soundRaw, pitch, volume);
            }

            return message;
        }

        return new TextMessage(TextMessageType.CHAT, config.getString(name, ""), replacements);
    }

    public static void findAndSend(final @NotNull String name, final @NotNull Audience audience) {
        final TextMessage message = of(name);
        if (message.replacedRaw().isEmpty()) return;

        if (message.type() == TextMessageType.CHAT) {
            audience.sendMessage(message.text());
        } else if (message.type() == TextMessageType.ACTION_BAR) {
            audience.sendActionBar(message.text());
        } else if (message.type() == TextMessageType.BOTH) {
            audience.sendMessage(message.text());
            audience.sendActionBar(message.text());
        } else if (message.type() == TextMessageType.TITLE) {
            final Duration fadeIn = ObjectUtils.defaultIfNull(message.fadeIn(), Duration.of(0, TimeUnit.SECONDS.toChronoUnit()));
            final Duration stay = message.stay();
            final Duration fadeOut = ObjectUtils.defaultIfNull(message.fadeOut(), Duration.of(0, TimeUnit.SECONDS.toChronoUnit()));

            if (stay == null) throw new RuntimeException();

            if (message.title() != null) audience.sendTitlePart(TitlePart.TITLE, ObjectUtils.defaultIfNull(message.title(), Component.empty()));
            if (message.subtitle() != null) audience.sendTitlePart(TitlePart.SUBTITLE, ObjectUtils.defaultIfNull(message.subtitle(), Component.empty()));

            audience.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                fadeIn,
                stay,
                fadeOut
            ));
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
        final TextMessage message = of(name, replacements);
        if (message.replacedRaw().isEmpty()) return;

        if (message.type() == TextMessageType.CHAT) {
            audience.sendMessage(message.text());
        } else if (message.type() == TextMessageType.ACTION_BAR) {
            audience.sendActionBar(message.text());
        } else if (message.type() == TextMessageType.BOTH) {
            audience.sendMessage(message.text());
            audience.sendActionBar(message.text());
        } else if (message.type() == TextMessageType.TITLE) {
            final Duration fadeIn = ObjectUtils.defaultIfNull(message.fadeIn(), Duration.of(0, TimeUnit.SECONDS.toChronoUnit()));
            final Duration stay = message.stay();
            final Duration fadeOut = ObjectUtils.defaultIfNull(message.fadeOut(), Duration.of(0, TimeUnit.SECONDS.toChronoUnit()));

            if (stay == null) throw new RuntimeException();

            if (message.title() != null) audience.sendTitlePart(TitlePart.TITLE, ObjectUtils.defaultIfNull(message.title(), Component.empty()));
            if (message.subtitle() != null) audience.sendTitlePart(TitlePart.SUBTITLE, ObjectUtils.defaultIfNull(message.subtitle(), Component.empty()));

            audience.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                fadeIn,
                stay,
                fadeOut
            ));
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

    public static void message(final @NotNull String key, final @NotNull Audience audience, final @NotNull PlaceholderReplacements replacements) {
        findAndSend(key, replacements.replacements(Elements.of()), audience);
    }

    public static void message(final @NotNull String key, final @NotNull Audience audience) {
        message(key, audience, elements -> elements);
    }

    public static void message(final @NotNull String key, final @NotNull Audience audience, final @NotNull Elements<Tuple<String, String>> replacements) {
        message(key, audience, elements -> elements);
    }

    public interface PlaceholderReplacements {
        @NotNull
        Elements<Tuple<String, String>> replacements(final @NotNull Elements<Tuple<String, String>> elements);
    }
}
