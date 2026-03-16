package com.jeyers.lobbynodata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LobbyNoData extends JavaPlugin implements Listener {

    private Location lobbyLocation;


    //  Enable / Disable
    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        loadLobbyConfig();

        getServer().getPluginManager().registerEvents(this, this);

        World lobbyWorld = Bukkit.getWorld(getConfig().getString("lobby.world"));
        deletePlayerData(lobbyWorld);
    }

    //  Config & Location loading
    private void loadLobbyConfig() {
        String worldName = getConfig().getString("lobby.world");
        double x    = getConfig().getDouble("lobby.x");
        double y    = getConfig().getDouble("lobby.y");
        double z    = getConfig().getDouble("lobby.z");
        float  yaw   = (float) getConfig().getDouble("lobby.yaw");
        float  pitch = (float) getConfig().getDouble("lobby.pitch");

        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            lobbyLocation = new Location(world, x, y, z, yaw, pitch);
        } else {
            getLogger().warning("Lobby world not found! Using fallback world 'world'");
            lobbyLocation = new Location(Bukkit.getWorld("world"), x, y, z, yaw, pitch);
        }
    }

    //  Player data cleanup (only once on startup)
    private void deletePlayerData(World world) {
        if (world == null) return;

        File playerDataFolder = new File(world.getWorldFolder(), "playerdata");
        if (!playerDataFolder.exists()) return;

        File[] files = playerDataFolder.listFiles(
            (dir, name) -> name.endsWith(".dat")
        );

        if (files == null || files.length == 0) return;

        for (File file : files) {
            boolean success = file.delete();

            if (success) {
                getLogger().info("Deleted player data: " + file.getName());
            } else {
                getLogger().warning("Failed to delete: " + file.getName());
            }
        }
    }

    //  "Events"
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (lobbyLocation != null) {
            player.teleport(lobbyLocation);
        }

        // Remove any bed / respawn anchor spawn point
        player.setBedSpawnLocation(null, true);
    }

    //  Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("lobbynodata")) {
            return false;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            loadLobbyConfig();

            sender.sendMessage("§aLobbyNoData config reloaded!");
            getLogger().info("Config reloaded by " + sender.getName());
            return true;
        }

        sender.sendMessage("§eUsage: /lobbynodata reload");
        return true;
    }
}