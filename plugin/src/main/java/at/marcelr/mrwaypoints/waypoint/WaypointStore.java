package at.marcelr.mrwaypoints.waypoint;

import at.marcelr.mrwaypoints.MRWaypoints;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * The categories (categories.yml) and the waypoints (waypoints.yml). Main thread only; files are written in the
 * background, one after the other. Everyone who shows waypoints (map, mod, web) listens with {@link #onChange}.
 */
public final class WaypointStore {

    private final MRWaypoints plugin;
    private final File categoriesFile;
    private final File waypointsFile;
    /** In file order = order in menus and on the maps. */
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final Map<Integer, Waypoint> waypoints = new LinkedHashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final ExecutorService writer = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "MRwaypoints-save");
        thread.setDaemon(true);
        return thread;
    });
    private int nextId = 1;

    public WaypointStore(MRWaypoints plugin) {
        this.plugin = plugin;
        this.categoriesFile = new File(plugin.getDataFolder(), "categories.yml");
        this.waypointsFile = new File(plugin.getDataFolder(), "waypoints.yml");
    }

    // ------------------------------------------------------------ loading

    public void load() {
        loadCategories();
        loadWaypoints();
    }

    /** (Re)reads categories.yml - on start and on /mrwaypoints reload. The bundled examples on the first start. */
    public void loadCategories() {
        if (!categoriesFile.exists()) {
            plugin.saveResource("categories.yml", false);
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(Files.readString(categoriesFile.toPath(), StandardCharsets.UTF_8).replace("﻿", ""));
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "categories.yml is invalid, keeping the categories loaded so far: "
                    + e.getMessage());
            return;
        }
        categories.clear();
        ConfigurationSection section = yaml.getConfigurationSection("categories");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection c = section.getConfigurationSection(id);
                if (!Category.validId(id) || c == null) {
                    plugin.getLogger().warning("categories.yml: '" + id + "' is no valid category id (a-z, 0-9, -, _) - skipped.");
                    continue;
                }
                categories.put(id, parse(id, c));
            }
        }
        plugin.permissions().register(categories.values());
        changed();
    }

    private Category parse(String id, ConfigurationSection c) {
        Map<String, String> names = new LinkedHashMap<>();
        ConfigurationSection nameSection = c.getConfigurationSection("name");
        if (nameSection != null) {
            for (String lang : nameSection.getKeys(false)) {
                names.put(lang.toLowerCase(Locale.ROOT), nameSection.getString(lang, ""));
            }
        } else if (c.isString("name")) {
            names.put("en", c.getString("name"));
        }
        Material icon = Material.matchMaterial(c.getString("icon", "PAPER"));
        if (icon == null || !icon.isItem() || icon.isAir()) {
            plugin.getLogger().warning("categories.yml: unknown icon '" + c.getString("icon") + "' for " + id + ", using PAPER.");
            icon = Material.PAPER;
        }
        return new Category(id, names, color(c.getString("color", "#FFFFFF"), id), icon,
                c.getBoolean("players-create", false), Math.max(0, c.getInt("max-per-player", 0)),
                c.getBoolean("visible", true), c.getBoolean("map", true));
    }

    private int color(String raw, String id) {
        String value = raw == null ? "" : raw.trim().replace("#", "");
        try {
            return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("categories.yml: invalid colour '" + raw + "' for " + id + ", using #FFFFFF.");
            return 0xFFFFFF;
        }
    }

    private void loadWaypoints() {
        waypoints.clear();
        if (!waypointsFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(waypointsFile);
        nextId = Math.max(1, yaml.getInt("next-id", 1));
        ConfigurationSection section = yaml.getConfigurationSection("waypoints");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection w = section.getConfigurationSection(key);
            try {
                int id = Integer.parseInt(key);
                Waypoint waypoint = new Waypoint(id, w.getString("category", ""), w.getString("name", "?"),
                        UUID.fromString(w.getString("world", "")), w.getString("world-name", "?"),
                        w.getInt("x"), w.getInt("y"), w.getInt("z"),
                        UUID.fromString(w.getString("creator", "")), w.getString("creator-name", "?"),
                        w.getLong("created"));
                waypoints.put(id, waypoint);
                nextId = Math.max(nextId, id + 1);
            } catch (RuntimeException e) {
                plugin.getLogger().warning("waypoints.yml: waypoint " + key + " is broken and was skipped (" + e + ").");
            }
        }
    }

    // ------------------------------------------------------------ reading

    public Collection<Category> categories() {
        return categories.values();
    }

    public @Nullable Category category(String id) {
        return id == null ? null : categories.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<Waypoint> all() {
        return waypoints.values();
    }

    public @Nullable Waypoint byId(int id) {
        return waypoints.get(id);
    }

    /** The waypoints of a category, sorted by name. Waypoints of a deleted category are in none. */
    public List<Waypoint> inCategory(String category) {
        return waypoints.values().stream().filter(w -> w.category().equals(category))
                .sorted(Comparator.comparing(w -> w.name().toLowerCase(Locale.ROOT))).toList();
    }

    /** How many waypoints the player created in the category (for max-per-player). */
    public int countBy(UUID creator, String category) {
        return (int) waypoints.values().stream()
                .filter(w -> w.category().equals(category) && w.creator().equals(creator)).count();
    }

    /** A waypoint of the category with this name (case-insensitive), or null. */
    public @Nullable Waypoint byName(String category, String name) {
        return waypoints.values().stream()
                .filter(w -> w.category().equals(category) && w.name().equalsIgnoreCase(name.trim()))
                .findFirst().orElse(null);
    }

    // ------------------------------------------------------------ writing

    public Waypoint add(Waypoint template) {
        Waypoint waypoint = new Waypoint(nextId++, template.category(), template.name(), template.world(),
                template.worldName(), template.x(), template.y(), template.z(), template.creator(),
                template.creatorName(), template.created());
        waypoints.put(waypoint.id(), waypoint);
        saveWaypoints();
        changed();
        return waypoint;
    }

    public void update(Waypoint waypoint) {
        if (waypoints.containsKey(waypoint.id())) {
            waypoints.put(waypoint.id(), waypoint);
            saveWaypoints();
            changed();
        }
    }

    public void remove(int id) {
        if (waypoints.remove(id) != null) {
            saveWaypoints();
            changed();
        }
    }

    /** Adds or replaces a category (web interface), keeping the order; a new one comes last. */
    public void putCategory(Category category) {
        categories.put(category.id(), category);
        plugin.permissions().register(categories.values());
        saveCategories();
        changed();
    }

    /** Removes a category together with its waypoints. */
    public void removeCategory(String id) {
        if (categories.remove(id) == null) {
            return;
        }
        waypoints.values().removeIf(w -> w.category().equals(id));
        plugin.permissions().register(categories.values());
        saveCategories();
        saveWaypoints();
        changed();
    }

    /** New order of the categories (ids in the wanted order; unknown ids are ignored, missing ones go last). */
    public void reorder(List<String> ids) {
        Map<String, Category> ordered = new LinkedHashMap<>();
        for (String id : ids) {
            Category c = categories.get(id);
            if (c != null) {
                ordered.put(id, c);
            }
        }
        categories.forEach(ordered::putIfAbsent);
        categories.clear();
        categories.putAll(ordered);
        saveCategories();
        changed();
    }

    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    private void changed() {
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.WARNING, "A waypoint listener failed", e);
            }
        }
    }

    private void saveCategories() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().width(10_000);
        yaml.options().setHeader(header());
        for (Category c : categories.values()) {
            String base = "categories." + c.id() + ".";
            c.names().forEach((lang, name) -> yaml.set(base + "name." + lang, name));
            yaml.set(base + "color", c.hexColor());
            yaml.set(base + "icon", c.icon().name());
            yaml.set(base + "players-create", c.playersCreate());
            yaml.set(base + "max-per-player", c.maxPerPlayer());
            yaml.set(base + "visible", c.visible());
            yaml.set(base + "map", c.onMap());
        }
        write(categoriesFile, yaml.saveToString());
    }

    /** The comment block of the bundled categories.yml, so a file saved from the web keeps its explanation. */
    private List<String> header() {
        List<String> lines = new ArrayList<>();
        try (var in = plugin.getResource("categories.yml")) {
            if (in != null) {
                for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                    if (!line.startsWith("#")) {
                        break;
                    }
                    lines.add(line.length() > 1 ? line.substring(2) : "");
                }
            }
        } catch (IOException e) {
            // no header then
        }
        return lines;
    }

    private void saveWaypoints() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("next-id", nextId);
        for (Waypoint w : waypoints.values()) {
            String base = "waypoints." + w.id() + ".";
            yaml.set(base + "category", w.category());
            yaml.set(base + "name", w.name());
            yaml.set(base + "world", w.world().toString());
            yaml.set(base + "world-name", w.worldName());
            yaml.set(base + "x", w.x());
            yaml.set(base + "y", w.y());
            yaml.set(base + "z", w.z());
            yaml.set(base + "creator", w.creator().toString());
            yaml.set(base + "creator-name", w.creatorName());
            yaml.set(base + "created", w.created());
        }
        write(waypointsFile, yaml.saveToString());
    }

    /** Writes in the background, one file after the other in call order. */
    private void write(File file, String content) {
        writer.execute(() -> {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                File temp = new File(file.getPath() + ".tmp");
                Files.writeString(temp.toPath(), content, StandardCharsets.UTF_8);
                Files.move(temp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save " + file.getName(), e);
            }
        });
    }

    /** Waits for the files still being written (on disable). */
    public void close() {
        writer.shutdown();
        try {
            if (!writer.awaitTermination(10, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Saving the waypoints took too long.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
