package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.Trade.OverflowType;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;


public class SignSell extends EssentialsSign
{
	private static final Map<String, Long> lastBulkSellTime = new HashMap<String, Long>();
	private static final long BULK_SELL_COOLDOWN = 1000; // 1 second in milliseconds
	
	public SignSell()
	{
		super("Sell");
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
		// Check if player is shifting for bulk sell
		boolean isBulkSell = player.getBase().isSneaking();
		
		if (isBulkSell)
		{
			// Check rate limit for bulk sell
			String playerName = player.getName();
			long currentTime = System.currentTimeMillis();
			Long lastTime = lastBulkSellTime.get(playerName);
			
			if (lastTime != null && (currentTime - lastTime) < BULK_SELL_COOLDOWN)
			{
				// Rate limit exceeded
				ess.showError(player.getSource(), new Exception("Bulk sell is on cooldown. Please wait 1 second."), "sign: Sell");
				return false;
			}
			
			// Update last bulk sell time
			lastBulkSellTime.put(playerName, currentTime);
			
			// Perform bulk sell
			return performBulkSell(sign, player, username, ess);
		}
		else
		{
			// Normal single item sell
			return performSingleSell(sign, player, username, ess);
		}
	}
	
	private boolean performBulkSell(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException
	{
		// Get the item type from the sign
		final Trade charge = getTrade(sign, 1, 2, player, ess);
		final Trade money = getTrade(sign, 3, ess);
		
		// Get the item type to sell
		ItemStack signItem = charge.getItemStack();
		if (signItem == null || signItem.getType() == Material.AIR)
		{
			ess.showError(player.getSource(), new Exception("Invalid item on sign"), "sign: Sell");
			return false;
		}
		
		// Count all matching items in player's inventory
		PlayerInventory inventory = player.getBase().getInventory();
		int totalAmount = 0;
		
		for (ItemStack item : inventory.getContents())
		{
			if (item != null && item.getType() != Material.AIR && 
				item.getType() == signItem.getType() && 
				item.getData().equals(signItem.getData()))
			{
				totalAmount += item.getAmount();
			}
		}
		
		if (totalAmount == 0)
		{
			ess.showError(player.getSource(), new Exception("You don't have any " + signItem.getType().name().toLowerCase() + " to sell"), "sign: Sell");
			return false;
		}
		
		// Calculate price per item and total money to receive
		BigDecimal signItemAmount = new BigDecimal(signItem.getAmount());
		BigDecimal pricePerItem = money.getMoney().divide(signItemAmount, 2, BigDecimal.ROUND_HALF_UP);
		BigDecimal totalMoney = pricePerItem.multiply(new BigDecimal(totalAmount));
		Trade totalMoneyTrade = new Trade(totalMoney, ess);
		
		// Create trade for all items
		ItemStack bulkItem = signItem.clone();
		bulkItem.setAmount(totalAmount);
		Trade bulkItemTrade = new Trade(bulkItem, ess);
		
		// Process the bulk trade
		try
		{
			// Remove all matching items from inventory
			for (int i = 0; i < inventory.getSize(); i++)
			{
				ItemStack item = inventory.getItem(i);
				if (item != null && item.getType() != Material.AIR && 
					item.getType() == signItem.getType() && 
					item.getData().equals(signItem.getData()))
				{
					inventory.setItem(i, null);
				}
			}
			
			// Give money to player
			totalMoneyTrade.pay(player, OverflowType.DROP);
			
			// Log the bulk transaction
			Trade.log("Sign", "BulkSell", "Interact", username, bulkItemTrade, username, totalMoneyTrade, sign.getBlock().getLocation(), ess);
			
			// Notify player
			player.sendMessage("§aSold " + totalAmount + "x " + signItem.getType().name().toLowerCase() + " for " + ess.getSettings().getCurrencySymbol() + totalMoney);
			
			return true;
		}
		catch (Exception e)
		{
			ess.showError(player.getSource(), e, "sign: BulkSell");
			return false;
		}
	}
	
	private boolean performSingleSell(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException
	{
		// Skip throttle check for sell signs - allow fast clicking
		final Trade charge = getTrade(sign, 1, 2, player, ess);
		final Trade money = getTrade(sign, 3, ess);
		
		// Quick affordability check
		try
		{
			charge.isAffordableFor(player);
		}
		catch (ChargeException e)
		{
			// Show the error message to the player
			ess.showError(player.getSource(), e, "sign: Sell");
			return false;
		}
		
		// Optimized trade processing - no delays
		try
		{
			// Process the trade immediately
			money.pay(player, OverflowType.DROP);
			charge.charge(player);
			
			// Log the transaction
			Trade.log("Sign", "Sell", "Interact", username, charge, username, money, sign.getBlock().getLocation(), ess);
			return true;
		}
		catch (Exception e)
		{
			// Show the error message to the player
			ess.showError(player.getSource(), e, "sign: Sell");
			return false;
		}
	}
}
