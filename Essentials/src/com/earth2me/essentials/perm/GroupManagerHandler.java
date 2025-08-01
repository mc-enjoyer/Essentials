package com.earth2me.essentials.perm;

import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class GroupManagerHandler implements IPermissionsHandler
{
	private final transient Plugin groupManager;

	public GroupManagerHandler(final Plugin permissionsPlugin)
	{
		groupManager = permissionsPlugin;
	}

	@Override
	public String getGroup(final Player base)
	{
		return null;
	}

	@Override
	public List<String> getGroups(final Player base)
	{
		return null;
	}

	@Override
	public boolean canBuild(final Player base, final String group)
	{
		return false;
	}

	@Override
	public boolean inGroup(final Player base, final String group)
	{
		return false;
	}

	@Override
	public boolean hasPermission(final Player base, final String node)
	{
		return false;
	}

	@Override
	public String getPrefix(final Player base)
	{
		return null;
	}

	@Override
	public String getSuffix(final Player base)
	{
		return null;
	}
}
