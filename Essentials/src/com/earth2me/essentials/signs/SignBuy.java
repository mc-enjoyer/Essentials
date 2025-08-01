package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class SignBuy extends EssentialsSign
{
	public SignBuy()
	{
		super("Buy");
	}

	@Override
	protected boolean shouldSkipThrottle()
	{
		return true; // Skip throttling for fast clicking
	}

	@Override
	protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException
	{
		validateTrade(sign, 1, 2, player, ess);
		validateTrade(sign, 3, ess);
		return true;
	}

	@Override
	protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException
	{
		// Skip throttle check for buy signs - allow fast clicking
		final Trade items = getTrade(sign, 1, 2, player, ess);
		final Trade charge = getTrade(sign, 3, ess);
		
		// Quick affordability check
		try
		{
			charge.isAffordableFor(player);
		}
		catch (ChargeException e)
		{
			// Show the error message to the player
			ess.showError(player.getSource(), e, "sign: Buy");
			return false;
		}
		
		// Optimized trade processing - no delays
		try
		{
			// Check if player can receive items
			if (!items.pay(player))
			{
				// Show inventory full message to player
				ess.showError(player.getSource(), new Exception("Inventory is full"), "sign: Buy");
				return false; // Inventory full
			}
			
			// Process the trade immediately
			charge.charge(player);
			
			// Log the transaction
			Trade.log("Sign", "Buy", "Interact", username, charge, username, items, sign.getBlock().getLocation(), ess);
			return true;
		}
		catch (Exception e)
		{
			// Show the error message to the player
			ess.showError(player.getSource(), e, "sign: Buy");
			return false;
		}
	}
}
