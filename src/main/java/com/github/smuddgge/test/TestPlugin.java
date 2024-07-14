package com.github.smuddgge.test;

import com.github.cozyplugins.cozylibrary.CozyPlugin;
import com.github.cozyplugins.cozylibrary.MessageManager;
import com.github.cozyplugins.cozylibrary.command.CommandManager;
import com.github.cozyplugins.cozylibrary.placeholder.PlaceholderManager;
import com.github.smuddgge.squishyconfiguration.implementation.YamlConfiguration;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.github.smuddgge.squishydatabase.interfaces.Database;
import com.github.smuddgge.test.command.ReloadCommand;
import com.github.smuddgge.test.command.SpinCommand;
import com.github.smuddgge.test.command.StartCommand;
import com.github.smuddgge.test.placeholder.TestPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestPlugin extends CozyPlugin<TestLoader> implements Listener {

    private static TestPlugin plugin;

    private Configuration databaseConfig;
    private Database database;
    private Configuration config;
    private Configuration data;
    private List<UUID> uuidList;

    public TestPlugin(@NotNull TestLoader plugin) {
        super(plugin);

        this.uuidList = new ArrayList<>();

        if (TestPlugin.plugin == null) {
            TestPlugin.plugin = this;
        }
    }

    @Override
    public boolean isCommandTypesEnabled() {
        return true;
    }

    @Override
    public void onEnable() {

        // Create database configuration file.
        this.databaseConfig = new YamlConfiguration(this.getPlugin().getDataFolder(), "database.yml");
        this.databaseConfig.setResourcePath("database.yml");
        this.databaseConfig.load();

        // Create configuration file.
        this.config = new YamlConfiguration(this.getPlugin().getDataFolder(), "config.yml");
        this.config.setResourcePath("config.yml");
        this.config.load();

        // Create data configuration file.
        this.data = new YamlConfiguration(this.getPlugin().getDataFolder(), "data.yml");
        this.data.setResourcePath("data.yml");
        this.data.load();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoadCommands(@NotNull CommandManager commandManager) {

        // Register commands.
        commandManager.getTypeManager().registerCommandType(new ReloadCommand());
        commandManager.getTypeManager().registerCommandType(new SpinCommand());
        commandManager.getTypeManager().registerCommandType(new StartCommand());
    }

    @Override
    public void onLoadPlaceholders(@NotNull PlaceholderManager<TestLoader> placeholderManager) {

        placeholderManager.addPlaceholder(new TestPlaceholder());
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {

        // Remove the player from the list if they are in the list.
        this.uuidList.remove(event.getPlayer().getUniqueId());

        // Check if we have a winner.
        if (this.uuidList.size() <= 1) {
            this.endGame();
        }
    }

    public void reload() {

        this.getCommandDirectory().reload();

        // Reload config.
        this.config.load();

        // Reload data config.
        this.data.load();
    }

    public void startGame() {

        this.uuidList.clear();

        // Add all the players online into a list.
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        this.uuidList.addAll(onlinePlayers.stream().map(Player::getUniqueId).toList());

        // Broadcast game start.
        Bukkit.broadcast(MessageManager.parse("&a&lColour wheel is starting with &f" + onlinePlayers.size() + " &a&lplayers."), "broadcast");
    }

    public void endGame() {

        if (this.uuidList.isEmpty()) {
            Bukkit.broadcast(MessageManager.parse("No one was left in the list of players o:"), "broadcast");
            return;
        }

        UUID winnerUUID = this.uuidList.get(0);
        final Player winner = Bukkit.getPlayer(winnerUUID);

        Bukkit.broadcast(MessageManager.parse(winner.getName() + " has won!"), "broadcast");

        // Create a new winning player as the winning player.
        WinningPlayer winningPlayer = this.getWinningPlayer(winner);

        // Increment winning number.
        winningPlayer.incrementWins();

        // Save.
        this.data.set(winner.getUniqueId().toString(), winningPlayer.convert().getMap());
        this.data.save();
    }

    public WinningPlayer getWinningPlayer(@NotNull Player offlinePlayer) {

        // Check if they are already in data config.
        if (this.data.getKeys().contains(offlinePlayer.getUniqueId().toString())) {

            return new WinningPlayer(null, null, null, 0)
                    .convert(this.data.getSection(offlinePlayer.getUniqueId().toString()));
        }

        return new WinningPlayer(
                offlinePlayer.getUniqueId(),
                offlinePlayer.getName(),
                this.getRealmColor(offlinePlayer),
                0
        );
    }

    public String getRealmColor(@NotNull Player player) {
        for (String realmPermission : this.config.getSection("realms").getKeys()) {
            if (player.hasPermission(realmPermission)) {
                return this.config.getSection("realms").getString(realmPermission);
            }
        }

        return "No realm permission";
    }

    public void spinWithTimer() {

        AtomicInteger number = new AtomicInteger(3);
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskTimer(this.getPlugin(), (task) -> {

            if (number.get() == 3) Bukkit.broadcast(MessageManager.parse("&4&l3!"), "broadcast");
            if (number.get() == 2) Bukkit.broadcast(MessageManager.parse("&4&l2!"), "broadcast");
            if (number.get() == 1) Bukkit.broadcast(MessageManager.parse("&4&l1!"), "broadcast");
            if (number.get() <= 0) {
                task.cancel();
                this.spin();
            }

            number.decrementAndGet();

        }, 20L, 20L);
    }

    public void spin() {

        Bukkit.broadcast(MessageManager.parse("&4&lSpinning!"), "broadcast");

        // Get a random block.
        Material blockMaterial = this.getRandomMaterial();

        // Eliminate players on the block.
        this.eliminatePlayersOnMaterial(blockMaterial);

        // Broadcast material.
        Bukkit.broadcast(MessageManager.parse("&4&l" + blockMaterial.name()), "broadcast");

        // Check if there is a winner.
        if (this.uuidList.size() <= 1) {
            this.endGame();
        }
    }

    public Material getRandomMaterial() {

        // Get all the materials from the config.yml

        List<Material> materialList = new ArrayList<>();
        for (String colourFormatting : this.config.getSection("materials").getKeys()) {
            final String materialName = this.config.getSection("materials")
                    .getString(colourFormatting)
                    .toUpperCase(Locale.ROOT);

            final Material material = Material.getMaterial(materialName);
            materialList.add(material);

        }

        // Return a random one.
        return materialList.get(new Random().nextInt(materialList.size() - 1));
    }

    public void eliminatePlayersOnMaterial(@NotNull Material material) {

        // Loop though all players in game.
        for (UUID uuid : new ArrayList<>(this.uuidList)) {

            Player player = Bukkit.getPlayer(uuid);

            // Check what block is below player.
            if (this.isLocationAboveMaterial(material, player.getLocation())) {
                player.setHealth(0);
                this.uuidList.remove(uuid);
            }
        }
    }

    public boolean isLocationAboveMaterial(Material material, Location location) {

        // Check if the location y is below 0.
        if (location.getY() < 0) return false;

        // If block is material.
        if (location.getBlock().getType().equals(material)) return true;

        // Otherwise, recursive : location -1 y.
        return this.isLocationAboveMaterial(material, location.clone().subtract(new Vector(0, 1, 0)));
    }

    public static TestPlugin getInstance() {
        return TestPlugin.plugin;
    }
}
