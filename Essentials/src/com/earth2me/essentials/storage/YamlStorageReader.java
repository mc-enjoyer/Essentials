package com.earth2me.essentials.storage;

import java.io.Reader;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

public class YamlStorageReader implements IStorageReader
{
	private transient static final Map<Class, ReentrantLock> LOCKS = new HashMap<Class, ReentrantLock>();
	private transient final Reader reader;
	private transient final Plugin plugin;

	public YamlStorageReader(final Reader reader, final Plugin plugin)
	{
		this.reader = reader;
		this.plugin = plugin;
	}

	@Override
	public <T extends StorageObject> T load(final Class<? extends T> clazz) throws ObjectLoadException
	{
		ReentrantLock lock;
		synchronized (LOCKS)
		{
			lock = LOCKS.get(clazz);
			if (lock == null)
			{
				lock = new ReentrantLock();
				LOCKS.put(clazz, lock);
			}
		}
		lock.lock();
		try
		{
			// Use SafeConstructor to avoid security issues and compatibility problems
			Yaml yaml = new Yaml(new SafeConstructor());
			
			// Load the YAML data
			Object data = yaml.load(reader);
			
			// Create the object instance
			T object = clazz.newInstance();
			
			// If data is null, return the empty object
			if (data == null)
			{
				return object;
			}
			
			// Apply the data to the object
			if (data instanceof Map)
			{
				applyMapToObject(object, (Map<?, ?>) data);
			}
			
			return object;
		}
		catch (YAMLException e)
		{
			// If YAML parsing fails, try to create a clean object
			try
			{
				return clazz.newInstance();
			}
			catch (Exception ex)
			{
				throw new ObjectLoadException(ex);
			}
		}
		catch (Exception e)
		{
			throw new ObjectLoadException(e);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private void applyMapToObject(Object object, Map<?, ?> data) throws Exception
	{
		// Use reflection to set fields from the map data
		java.lang.reflect.Field[] fields = object.getClass().getDeclaredFields();
		
		for (java.lang.reflect.Field field : fields)
		{
			field.setAccessible(true);
			String fieldName = field.getName();
			
			if (data.containsKey(fieldName))
			{
				Object value = data.get(fieldName);
				
				// Handle different field types safely
				if (value != null)
				{
					try
					{
						if (field.getType().isAssignableFrom(value.getClass()))
						{
							field.set(object, value);
						}
						else if (field.getType() == String.class)
						{
							field.set(object, value.toString());
						}
						else if (field.getType() == int.class || field.getType() == Integer.class)
						{
							if (value instanceof Number)
							{
								field.set(object, ((Number) value).intValue());
							}
							else
							{
								field.set(object, Integer.parseInt(value.toString()));
							}
						}
						else if (field.getType() == double.class || field.getType() == Double.class)
						{
							if (value instanceof Number)
							{
								field.set(object, ((Number) value).doubleValue());
							}
							else
							{
								field.set(object, Double.parseDouble(value.toString()));
							}
						}
						else if (field.getType() == boolean.class || field.getType() == Boolean.class)
						{
							if (value instanceof Boolean)
							{
								field.set(object, value);
							}
							else
							{
								field.set(object, Boolean.parseBoolean(value.toString()));
							}
						}
						else if (field.getType() == Map.class)
						{
							if (value instanceof Map)
							{
								field.set(object, value);
							}
							else
							{
								field.set(object, new HashMap<>());
							}
						}
					}
					catch (Exception e)
					{
						// Log the error but continue processing other fields
						plugin.getLogger().warning("Failed to set field " + fieldName + " in " + object.getClass().getSimpleName() + ": " + e.getMessage());
					}
				}
			}
		}
	}
}
