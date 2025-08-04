package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.InventoryFullException;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import static com.earth2me.essentials.I18n._;

public class SignBuy extends EssentialsSign
{
	public SignBuy()
	{
		super("Buy");
	}

	@Override
	protected boolean shouldSkipThrottle()
	{
		return true; // Skip throttle for buy signs - allow fast clicking
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
			// Log detailed error to console
			ess.getLogger().warning("Buy sign affordability check failed for player " + username + ": " + e.getMessage());
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
				// Throw InventoryFullException instead of logging and showing generic error
				throw new InventoryFullException();
			}
			
			// Process the trade immediately
			charge.charge(player);
			
			// Log the transaction
			Trade.log("Sign", "Buy", "Interact", username, charge, username, items, sign.getBlock().getLocation(), ess);
			return true;
		}
		catch (Exception e)
		{
			// Log detailed error to console
			ess.getLogger().warning("Buy sign transaction failed for player " + username + ": " + e.getMessage());
			// Show the error message to the player
			ess.showError(player.getSource(), e, "sign: Buy");
			return false;
		}
	}
}
