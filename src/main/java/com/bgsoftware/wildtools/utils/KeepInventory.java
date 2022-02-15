package com.bgsoftware.wildtools.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KeepInventory {

    private final JavaPlugin plugin;
    private final File file;

    private final Map<UUID, List<ItemStack>> data = new HashMap<>();
    private YamlConfiguration config;
    private BukkitTask autoSaveTask;

    public KeepInventory(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
    }

    public void load() {
        config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            List<ItemStack> items = config.getStringList(key).stream()
                    .map(Base64Util::decodeItemStack)
                    .collect(Collectors.toList());

            data.put(uuid, items);
        }

        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }

        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::save, 20*120, 20*120);
    }

    public void save() {
        config.getKeys(false) // clear any previous data
                .forEach(key -> config.set(key, null));

        data.forEach((uuid, items) -> {
            if (items.isEmpty())
                return;

            config.set(uuid.toString(), items.stream()
                    .map(Base64Util::encodeItemStackToString)
                    .collect(Collectors.toList())
            );
        });

        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<ItemStack> getItems(Player player) {
        return data.computeIfAbsent(player.getUniqueId(), u -> new ArrayList<>());
    }

    public void addItem(Player player, ItemStack item) {
        getItems(player).add(item);
    }

    public void removeItem(Player player, ItemStack item) {
        getItems(player).remove(item);
    }

    public void clearItems(Player player) {
        data.remove(player.getUniqueId());
    }
}
