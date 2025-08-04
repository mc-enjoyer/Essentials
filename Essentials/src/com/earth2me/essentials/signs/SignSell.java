package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.Trade.OverflowType;
import com.earth2me.essentials.User;
import static com.earth2me.essentials.I18n._;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SignSell extends EssentialsSign
{
	private static final long BULK_SELL_COOLDOWN = 1000; // 1 second cooldown
	private static final Map<String, Long> lastBulkSellTime = new HashMap<String, Long>();

	public SignSell()
	{
		super("Sell");
	}

	@Override
	protected boolean shouldSkipThrottle()
	{
		return true; // Skip throttle for sell signs - allow fast clicking
	}

	@Override
	protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException
	{
		// Check if player is sneaking for bulk sell
		if (player.getBase().isSneaking())
		{
			return performBulkSell(sign, player, username, ess);
		}
		else
		{
			return performSingleSell(sign, player, username, ess);
		}
	}

	private boolean performBulkSell(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException
	{
		// Rate limiting for bulk sell
		String playerName = player.getName();
		long currentTime = System.currentTimeMillis();
		Long lastTime = lastBulkSellTime.get(playerName);
		
		if (lastTime != null && (currentTime - lastTime) < BULK_SELL_COOLDOWN)
		{
			long remainingTime = BULK_SELL_COOLDOWN - (currentTime - lastTime);
			player.sendMessage(_("cooldownWithMessage", remainingTime + "ms"));
			return false;
		}
		
		lastBulkSellTime.put(playerName, currentTime);

		// Get the sign's item and price
		final Trade charge = getTrade(sign, 1, 2, player, ess);
		final Trade money = getTrade(sign, 3, ess);
		
		// Get the sign's item stack to determine what to sell
		final ItemStack signItem = charge.getItemStack();
		if (signItem == null)
		{
			ess.showError(player.getSource(), new Exception("Invalid sign configuration"), "sign: BulkSell");
			return false;
		}

		// Count all matching items in player's inventory
		PlayerInventory inventory = player.getBase().getInventory();
		int totalAmount = 0;
		
		for (ItemStack item : inventory.getContents())
		{
			if (item != null && item.getType() != Material.AIR && 
				item.getType() == signItem.getType())
			{
				// Special handling for spawners - compare by durability instead of data
				if (signItem.getType() == Material.MOB_SPAWNER)
				{
					if (item.getDurability() == signItem.getDurability())
					{
						totalAmount += item.getAmount();
					}
				}
				else if (item.getData().equals(signItem.getData()))
				{
					totalAmount += item.getAmount();
				}
			}
		}

		// Check if player has items to sell (after cooldown check)
		if (totalAmount == 0)
		{
			// Send user-friendly message directly to player
			player.sendMessage("§4You don't have any " + ess.getItemDb().name(signItem) + " to sell");
			return false;
		}

		try
		{
			// Calculate price per item and total price
			BigDecimal signItemAmount = new BigDecimal(signItem.getAmount());
			BigDecimal pricePerItem = money.getMoney().divide(signItemAmount, 2, BigDecimal.ROUND_HALF_UP);
			BigDecimal totalMoney = pricePerItem.multiply(new BigDecimal(totalAmount));

			// Remove all matching items from inventory
			for (int i = 0; i < inventory.getSize(); i++)
			{
				ItemStack item = inventory.getItem(i);
				if (item != null && item.getType() != Material.AIR && 
					item.getType() == signItem.getType())
				{
					// Special handling for spawners - compare by durability instead of data
					if (signItem.getType() == Material.MOB_SPAWNER)
					{
						if (item.getDurability() == signItem.getDurability())
						{
							inventory.setItem(i, null);
						}
					}
					else if (item.getData().equals(signItem.getData()))
					{
						inventory.setItem(i, null);
					}
				}
			}
			// Give money to player
			player.giveMoney(totalMoney);

			// Notify player
			player.sendMessage(_("itemSold", totalMoney.toString(), totalAmount, ess.getItemDb().name(signItem), pricePerItem.toString()));
			Bukkit.getPlayer(playerName).updateInventory();

			// Log the transaction
			Trade.log("Sign", "BulkSell", "Interact", username, new Trade(totalAmount, ess), username, new Trade(totalMoney, ess), sign.getBlock().getLocation(), ess);
			return true;
		}
		catch (Exception e)
		{
			// For bulk sell, just show a generic message to the player without logging as error
			// since this is usually just a normal case (player doesn't have items, etc.)
			player.sendMessage(_("genericError"));
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
			// Show the specific error message to the player without logging as error
			// since this is a normal case (player doesn't have enough items)
			player.sendMessage(e.getMessage());
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
			// For single sell, just show a generic message to the player without logging as error
			// since this is usually just a normal case (player doesn't have items, etc.)
			player.sendMessage(_("genericError"));
			return false;
		}
	}
}
