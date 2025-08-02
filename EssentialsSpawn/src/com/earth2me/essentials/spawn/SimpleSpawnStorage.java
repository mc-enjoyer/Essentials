package com.earth2me.essentials.spawn;

import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.IConf;
import net.ess3.api.IEssentials;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class SimpleSpawnStorage implements IEssentialsModule, IConf
{
	private final IEssentials ess;
	private File configFile;
	private FileConfiguration config;
	private Location spawnLocation;

	public SimpleSpawnStorage(final IEssentials ess)
	{
		this.ess = ess;
		loadConfig();
	}

	private void loadConfig()
	{
		configFile = new File(ess.getDataFolder(), "simple-spawn.yml");
		if (!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e)
			{
				ess.getLogger().severe("Could not create simple-spawn.yml: " + e.getMessage());
			}
		}
		
		config = YamlConfiguration.loadConfiguration(configFile);
		loadSpawnLocation();
	}

	private void loadSpawnLocation()
	{
		if (config.contains("spawn.world") && config.contains("spawn.x") && 
			config.contains("spawn.y") && config.contains("spawn.z"))
		{
			String worldName = config.getString("spawn.world");
			double x = config.getDouble("spawn.x");
			double y = config.getDouble("spawn.y");
			double z = config.getDouble("spawn.z");
			float yaw = (float) config.getDouble("spawn.yaw", 0.0);
			float pitch = (float) config.getDouble("spawn.pitch", 0.0);

			World world = ess.getServer().getWorld(worldName);
			if (world != null)
			{
				spawnLocation = new Location(world, x, y, z, yaw, pitch);
				ess.getLogger().info("Loaded spawn location: " + worldName + " at " + x + ", " + y + ", " + z);
			}
			else
			{
				ess.getLogger().warning("Spawn world '" + worldName + "' not found, using default spawn");
				spawnLocation = getDefaultSpawn();
			}
		}
		else
		{
			ess.getLogger().info("No spawn location configured, using default spawn");
			spawnLocation = getDefaultSpawn();
		}
	}

	private Location getDefaultSpawn()
	{
		for (World world : ess.getServer().getWorlds())
		{
			if (world.getEnvironment() == World.Environment.NORMAL)
			{
				return world.getSpawnLocation();
			}
		}
		return ess.getServer().getWorlds().get(0).getSpawnLocation();
	}

	public void setSpawn(final Location loc)
	{
		spawnLocation = loc.clone();
		
		// Save to config
		config.set("spawn.world", loc.getWorld().getName());
		config.set("spawn.x", loc.getX());
		config.set("spawn.y", loc.getY());
		config.set("spawn.z", loc.getZ());
		config.set("spawn.yaw", loc.getYaw());
		config.set("spawn.pitch", loc.getPitch());
		
		try
		{
			config.save(configFile);
			ess.getLogger().info("Spawn location saved: " + loc.getWorld().getName() + " at " + 
							   loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
		}
		catch (IOException e)
		{
			ess.getLogger().severe("Could not save spawn location: " + e.getMessage());
		}
		
		// Set world spawn location
		loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public Location getSpawn()
	{
		return spawnLocation != null ? spawnLocation.clone() : getDefaultSpawn();
	}

	public void reloadConfig()
	{
		loadConfig();
	}

	public void onReload()
	{
		loadConfig();
	}
} 