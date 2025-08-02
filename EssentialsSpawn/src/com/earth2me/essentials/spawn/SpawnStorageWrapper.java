package com.earth2me.essentials.spawn;

import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.IConf;
import net.ess3.api.IEssentials;
import org.bukkit.Location;

public class SpawnStorageWrapper implements IEssentialsModule, IConf
{
	private final SimpleSpawnStorage simpleStorage;

	public SpawnStorageWrapper(final IEssentials ess)
	{
		this.simpleStorage = new SimpleSpawnStorage(ess);
	}

	public Location getSpawn(final String group)
	{
		// Ignore group parameter and just return the single spawn location
		return simpleStorage.getSpawn();
	}

	public void setSpawn(final Location loc, final String group)
	{
		// Ignore group parameter and just set the single spawn location
		simpleStorage.setSpawn(loc);
	}

	@Override
	public void reloadConfig()
	{
		simpleStorage.reloadConfig();
	}

	public void onReload()
	{
		simpleStorage.onReload();
	}
} 