package com.earth2me.essentials.spawn;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsCommand;
import org.bukkit.Server;


public class Commandsetspawn extends EssentialsCommand
{
	public Commandsetspawn()
	{
		super("setspawn");
	}

	@Override
	public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{
		((SpawnStorageWrapper)module).setSpawn(user.getLocation(), "default");
		user.sendMessage(_("spawnSet", "default"));
	}
}
