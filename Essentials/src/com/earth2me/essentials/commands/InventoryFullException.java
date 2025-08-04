package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;

public class InventoryFullException extends Exception
{
	public InventoryFullException()
	{
		super(_("inventoryFull"));
	}
	
	public InventoryFullException(String message)
	{
		super(message);
	}
} 