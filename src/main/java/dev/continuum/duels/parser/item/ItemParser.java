package dev.continuum.duels.parser.item;

import dev.manere.utils.item.ItemBuilder;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.text.color.TextStyle;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemParser {
    public static @NotNull ItemBuilder parse(final @NotNull FileConfiguration configuration, final @NotNull String path) {
        return parse(configuration, path, new ArrayList<>());
    }

    public static @NotNull ItemBuilder parse(
        final @NotNull FileConfiguration configuration, final @NotNull String path,
        final @NotNull List<Tuple<String, String>> replacements
    ) {
        final ConfigurationSection section = configuration.getConfigurationSection(path);
        if (section == null) return ItemBuilder.item(Material.AIR);
        String rawMaterial = section.getString("material");
        if (rawMaterial == null) rawMaterial = "barrier";

        final String stringMaterial = rawMaterial
            .replaceAll(" ", "_")
            .replaceAll("minecraft:", "")
            .toUpperCase();

        Material material = Material.matchMaterial(stringMaterial);
        if (material == null) material = Material.BARRIER;

        final ItemBuilder builder = ItemBuilder.item(material);

        final String skull = section.getString("skull");

        if (material == Material.PLAYER_HEAD && skull != null) {
            if (skull.startsWith("uuid:")) {
                String skullArg = skull.replaceAll("uuid:", "");

                for (final Tuple<String, String> replacementPair : replacements) {
                    final String find = replacementPair.key();
                    final String replace = replacementPair.val();

                    if (find == null || replace == null) continue;

                    skullArg = skullArg.replaceAll("<" + find + ">", replace);
                }

                builder.skullOwner(skullArg);
            }
        }

        String name = section.getString("name");
        if (name == null) name = path;

        for (final Tuple<String, String> replacementPair : replacements) {
            final String find = replacementPair.key();
            final String replace = replacementPair.val();

            if (find == null || replace == null) continue;

            name = name.replaceAll("<" + find + ">", replace);
        }

        final Component displayName = TextStyle.style(name);

        builder.name(displayName);

        int amount = section.getInt("amount");
        if (amount == 0) amount = 1;

        builder.amount(amount);

        final List<String> rawLore = section.getStringList("lore");
        final List<String> copiedLore = new ArrayList<>();

        for (String line : rawLore) {
            for (final Tuple<String, String> replacementPair : replacements) {
                final String find = replacementPair.key();
                final String replace = replacementPair.val();

                if (find == null || replace == null) continue;

                line = line.replaceAll("<" + find + ">", replace);
            }

            copiedLore.add(line);
        }

        final List<Component> componentLore = TextStyle.style(copiedLore);

        builder.lore(componentLore);

        final boolean glow = section.getBoolean("glow");

        if (glow) builder.glow();
        else builder.removeGlow();

        final ConfigurationSection enchantmentSection = section.getConfigurationSection("enchantments");
        if (enchantmentSection != null) {
            final List<Tuple<Enchantment, Integer>> enchantmentsToAdd = new ArrayList<>();

            for (final String key : enchantmentSection.getKeys(false)) {
                for (final Enchantment possible : Registry.ENCHANTMENT.stream().toList()) {
                    if (possible.getKey().value().equals(key.toLowerCase())) {
                        enchantmentsToAdd.add(enchantmentSection.getInt(key + ".level") == 0 ?
                            Tuple.tuple(possible, 1) :
                            Tuple.tuple(possible, enchantmentSection.getInt(key + ".level")));
                    }
                }
            }

            for (final Tuple<Enchantment, Integer> pair : enchantmentsToAdd) {
                final Enchantment key = pair.key();
                final Integer val = pair.val();
                if (key == null || val == null) continue;

                builder.addUnsafeEnchantment(key, val);
            }
        }

        final List<String> rawFlags = section.getStringList("flags");
        for (final String rawFlag : rawFlags) {
            ItemFlag flag;

            try {
                flag = ItemFlag.valueOf(rawFlag.toUpperCase());
            } catch (final IllegalArgumentException e) {
                continue;
            }

            builder.addFlag(flag);
        }

        final Integer customModelData = (Integer) section.get("custom_model_data");
        if (customModelData != null && section.getKeys(false).contains("custom_model_data")) {
            builder.customModelData(customModelData);
        }

        final boolean unbreakable = section.getBoolean("unbreakable");
        if (unbreakable) builder.unbreakable();

        return builder;
    }
}
