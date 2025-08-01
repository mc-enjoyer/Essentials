package com.earth2me.essentials.storage;

import java.io.Reader;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import com.earth2me.essentials.storage.BukkitConstructor;

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
			// Use BukkitConstructor to properly handle Location deserialization
			Yaml yaml = new Yaml(new BukkitConstructor(clazz, plugin));
			
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
			// Log the YAML parsing error with full details
			plugin.getLogger().warning("YAML parsing error in " + clazz.getSimpleName() + ": " + e.getMessage());
			plugin.getLogger().warning("Attempting to create clean object instance...");
			
			// If YAML parsing fails, try to create a clean object
			try
			{
				T cleanObject = clazz.newInstance();
				plugin.getLogger().info("Successfully created clean " + clazz.getSimpleName() + " instance");
				return cleanObject;
			}
			catch (Exception ex)
			{
				plugin.getLogger().severe("Failed to create clean " + clazz.getSimpleName() + " instance: " + ex.getMessage());
				throw new ObjectLoadException(ex);
			}
		}
		catch (Exception e)
		{
			// Log the general error with full details
			plugin.getLogger().severe("Error loading " + clazz.getSimpleName() + ": " + e.getMessage());
			throw new ObjectLoadException(e);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private void applyMapToObject(Object object, Map<?, ?> data) throws Exception
	{
		// Get all declared fields of the object
		java.lang.reflect.Field[] fields = object.getClass().getDeclaredFields();
		
		for (java.lang.reflect.Field field : fields)
		{
			field.setAccessible(true);
			String fieldName = field.getName();
			
			// Check if the field exists in the data map
			if (data.containsKey(fieldName))
			{
				Object value = data.get(fieldName);
				
				try
				{
					// Handle different types of values
					if (value instanceof Map)
					{
						// For Map fields, we need to handle them specially
						if (field.getType().isAssignableFrom(Map.class))
						{
							field.set(object, value);
						}
						else
						{
							// Try to create a new instance and apply the map data
							Object fieldObject = field.getType().newInstance();
							applyMapToObject(fieldObject, (Map<?, ?>) value);
							field.set(object, fieldObject);
						}
					}
					else if (value instanceof java.util.List)
					{
						// Handle List fields
						if (field.getType().isAssignableFrom(java.util.List.class))
						{
							field.set(object, value);
						}
					}
					else
					{
						// Handle primitive types and other objects
						if (field.getType().isPrimitive())
						{
							// Handle primitive types
							if (field.getType() == int.class && value instanceof Number)
							{
								field.setInt(object, ((Number) value).intValue());
							}
							else if (field.getType() == double.class && value instanceof Number)
							{
								field.setDouble(object, ((Number) value).doubleValue());
							}
							else if (field.getType() == boolean.class && value instanceof Boolean)
							{
								field.setBoolean(object, (Boolean) value);
							}
							else if (field.getType() == long.class && value instanceof Number)
							{
								field.setLong(object, ((Number) value).longValue());
							}
							else if (field.getType() == float.class && value instanceof Number)
							{
								field.setFloat(object, ((Number) value).floatValue());
							}
							else if (field.getType() == byte.class && value instanceof Number)
							{
								field.setByte(object, ((Number) value).byteValue());
							}
							else if (field.getType() == short.class && value instanceof Number)
							{
								field.setShort(object, ((Number) value).shortValue());
							}
							else if (field.getType() == char.class && value instanceof Character)
							{
								field.setChar(object, (Character) value);
							}
						}
						else
						{
							// Handle non-primitive types
							if (field.getType().isAssignableFrom(value.getClass()))
							{
								field.set(object, value);
							}
							else if (value instanceof String && field.getType() == java.math.BigDecimal.class)
							{
								field.set(object, new java.math.BigDecimal((String) value));
							}
							else if (value instanceof Number && field.getType() == java.math.BigDecimal.class)
							{
								field.set(object, new java.math.BigDecimal(value.toString()));
							}
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
