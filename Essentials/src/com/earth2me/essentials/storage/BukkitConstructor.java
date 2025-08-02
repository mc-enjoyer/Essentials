package com.earth2me.essentials.storage;


import com.earth2me.essentials.storage.EnchantmentLevel;
import com.earth2me.essentials.utils.NumberUtil;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;
import com.earth2me.essentials.storage.MapValueType;


public class BukkitConstructor extends CustomClassLoaderConstructor
{
	private final transient Plugin plugin;

	public BukkitConstructor(final Class clazz, final Plugin plugin)
	{
		super(clazz, plugin.getClass().getClassLoader());
		this.plugin = plugin;
		yamlClassConstructors.put(NodeId.scalar, new ConstructBukkitScalar());
		yamlClassConstructors.put(NodeId.mapping, new ConstructBukkitMapping());
	}


	private class ConstructBukkitScalar extends ConstructScalar
	{
		@Override
		public Object construct(final Node node)
		{
			if (node.getType().equals(Material.class))
			{
				final String val = (String)construct((ScalarNode)node);
				Material mat;
				if (NumberUtil.isInt(val))
				{
					final int typeId = Integer.parseInt(val);
					mat = Material.getMaterial(typeId);
				}
				else
				{
					mat = Material.matchMaterial(val);
				}
				return mat;
			}
			if (node.getType().equals(MaterialData.class))
			{
				final String val = (String)construct((ScalarNode)node);
				if (val.isEmpty())
				{
					return null;
				}
				final String[] split = val.split("[:+',;.]", 2);
				if (split.length == 0)
				{
					return null;
				}
				Material mat;
				if (NumberUtil.isInt(split[0]))
				{
					final int typeId = Integer.parseInt(split[0]);
					mat = Material.getMaterial(typeId);
				}
				else
				{
					mat = Material.matchMaterial(split[0]);
				}
				if (mat == null)
				{
					return null;
				}
				byte data = 0;
				if (split.length == 2 && NumberUtil.isInt(split[1]))
				{
					data = Byte.parseByte(split[1]);
				}
				return new MaterialData(mat, data);
			}
			if (node.getType().equals(ItemStack.class))
			{
				final String val = (String)construct((ScalarNode)node);
				if (val.isEmpty())
				{
					return null;
				}
				final String[] split1 = val.split("\\W");
				if (split1.length == 0)
				{
					return null;
				}
				final String[] split2 = split1[0].split("[:+',;.]", 2);
				if (split2.length == 0)
				{
					return null;
				}
				Material mat;
				if (NumberUtil.isInt(split2[0]))
				{
					final int typeId = Integer.parseInt(split2[0]);
					mat = Material.getMaterial(typeId);
				}
				else
				{
					mat = Material.matchMaterial(split2[0]);
				}
				if (mat == null)
				{
					return null;
				}
				short data = 0;
				if (split2.length == 2 && NumberUtil.isInt(split2[1]))
				{
					data = Short.parseShort(split2[1]);
				}
				int size = mat.getMaxStackSize();
				if (split1.length > 1 && NumberUtil.isInt(split1[1]))
				{
					size = Integer.parseInt(split1[1]);
				}
				final ItemStack stack = new ItemStack(mat, size, data);
				if (split1.length > 2)
				{
					for (int i = 2; i < split1.length; i++)
					{
						final String[] split3 = split1[0].split("[:+',;.]", 2);
						if (split3.length < 1)
						{
							continue;
						}
						Enchantment enchantment;
						if (NumberUtil.isInt(split3[0]))
						{
							final int enchantId = Integer.parseInt(split3[0]);
							enchantment = Enchantment.getById(enchantId);
						}
						else
						{
							enchantment = Enchantment.getByName(split3[0].toUpperCase(Locale.ENGLISH));
						}
						if (enchantment == null)
						{
							continue;
						}
						int level = enchantment.getStartLevel();
						if (split3.length == 2 && NumberUtil.isInt(split3[1]))
						{
							level = Integer.parseInt(split3[1]);
						}
						if (level < enchantment.getStartLevel())
						{
							level = enchantment.getStartLevel();
						}
						if (level > enchantment.getMaxLevel())
						{
							level = enchantment.getMaxLevel();
						}
						stack.addUnsafeEnchantment(enchantment, level);
					}
				}
				return stack;
			}
			if (node.getType().equals(EnchantmentLevel.class))
			{
				final String val = (String)construct((ScalarNode)node);
				if (val.isEmpty())
				{
					return null;
				}
				final String[] split = val.split("[:+',;.]", 2);
				if (split.length == 0)
				{
					return null;
				}
				Enchantment enchant;
				if (NumberUtil.isInt(split[0]))
				{
					final int typeId = Integer.parseInt(split[0]);
					enchant = Enchantment.getById(typeId);
				}
				else
				{
					enchant = Enchantment.getByName(split[0].toUpperCase(Locale.ENGLISH));
				}
				if (enchant == null)
				{
					return null;
				}
				int level = enchant.getStartLevel();
				if (split.length == 2 && NumberUtil.isInt(split[1]))
				{
					level = Integer.parseInt(split[1]);
				}
				if (level < enchant.getStartLevel())
				{
					level = enchant.getStartLevel();
				}
				if (level > enchant.getMaxLevel())
				{
					level = enchant.getMaxLevel();
				}
				return new EnchantmentLevel(enchant, level);
			}

			return super.construct(node);
		}
	}


	private class ConstructBukkitMapping extends ConstructMapping
	{
		@Override
		public Object construct(final Node node)
		{
			if (node.getType().equals(Location.class))
			{
				//TODO: NPE checks
				final MappingNode mnode = (MappingNode)node;
				String worldName = "";
				double x = 0, y = 0, z = 0;
				float yaw = 0, pitch = 0;
				if (mnode.getValue().size() < 4)
				{
					return null;
				}
				for (NodeTuple nodeTuple : mnode.getValue())
				{
					final String key = (String)construct((ScalarNode)nodeTuple.getKeyNode());
					final ScalarNode snode = (ScalarNode)nodeTuple.getValueNode();
					if (key.equalsIgnoreCase("world"))
					{
						worldName = (String)construct(snode);
					}
					if (key.equalsIgnoreCase("x"))
					{
						x = Double.parseDouble((String)construct(snode));
					}
					if (key.equalsIgnoreCase("y"))
					{
						y = Double.parseDouble((String)construct(snode));
					}
					if (key.equalsIgnoreCase("z"))
					{
						z = Double.parseDouble((String)construct(snode));
					}
					if (key.equalsIgnoreCase("yaw"))
					{
						yaw = Float.parseFloat((String)construct(snode));
					}
					if (key.equalsIgnoreCase("pitch"))
					{
						pitch = Float.parseFloat((String)construct(snode));
					}
				}
				if (worldName == null || worldName.isEmpty())
				{
					return null;
				}
				final World world = Bukkit.getWorld(worldName);
				if (world == null)
				{
					return null;
				}
				return new Location(world, x, y, z, yaw, pitch);
			}
			return super.construct(node);
		}

		@Override
		protected Object constructJavaBean2ndStep(final MappingNode node, final Object object)
		{
			Map<Class<? extends Object>, TypeDescription> typeDefinitions;
			try
			{
				final Field typeDefField = Constructor.class.getDeclaredField("typeDefinitions");
				typeDefField.setAccessible(true);
				typeDefinitions = (Map<Class<? extends Object>, TypeDescription>)typeDefField.get((Constructor)BukkitConstructor.this);
				if (typeDefinitions == null)
				{
					throw new NullPointerException();
				}
			}
			catch (Exception ex)
			{
				throw new YAMLException(ex);
			}
			flattenMapping(node);
			final Class<? extends Object> beanType = node.getType();
			final List<NodeTuple> nodeValue = node.getValue();
			for (NodeTuple tuple : nodeValue)
			{
				ScalarNode keyNode;
				if (tuple.getKeyNode() instanceof ScalarNode)
				{
					// key must be scalar
					keyNode = (ScalarNode)tuple.getKeyNode();
				}
				else
				{
					throw new YAMLException("Keys must be scalars but found: " + tuple.getKeyNode());
				}
				final Node valueNode = tuple.getValueNode();
				// keys can only be Strings
				keyNode.setType(String.class);
				final String key = (String)constructObject(keyNode);
				try
				{
					Property property;
					try
					{
						property = getProperty(beanType, key);
					}
					catch (YAMLException e)
					{
						continue;
					}
					valueNode.setType(property.getType());
					final TypeDescription memberDescription = typeDefinitions.get(beanType);
					boolean typeDetected = false;
					if (memberDescription != null)
					{
						switch (valueNode.getNodeId())
						{
						case sequence:
							final SequenceNode snode = (SequenceNode)valueNode;
							final Class<? extends Object> memberType = memberDescription.getListPropertyType(key);
							if (memberType != null)
							{
								snode.setListType(memberType);
								typeDetected = true;
							}
							else if (property.getType().isArray())
							{
								snode.setListType(property.getType().getComponentType());
								typeDetected = true;
							}
							break;
						case mapping:
							final MappingNode mnode = (MappingNode)valueNode;
							final Class<? extends Object> keyType = memberDescription.getMapKeyType(key);
							if (keyType != null)
							{
								mnode.setTypes(keyType, memberDescription.getMapValueType(key));
								typeDetected = true;
							}
							break;
						}
					}
					
					// Check for MapValueType annotation if type wasn't detected
					if (!typeDetected && valueNode.getNodeId() != NodeId.scalar)
					{
						try
						{
							Field field = beanType.getDeclaredField(key);
							if (field.isAnnotationPresent(MapValueType.class))
							{
								MapValueType annotation = field.getAnnotation(MapValueType.class);
								Class<?> valueType = annotation.value();
								
								if (valueNode.getNodeId() == NodeId.sequence)
								{
									final SequenceNode snode = (SequenceNode)valueNode;
									snode.setListType(valueType);
									typeDetected = true;
								}
								else if (valueNode.getTag().equals(Tag.SET))
								{
									final MappingNode mnode = (MappingNode)valueNode;
									mnode.setOnlyKeyType(valueType);
									mnode.setUseClassConstructor(true);
									typeDetected = true;
								}
								else if (property.getType().isAssignableFrom(Map.class))
								{
									final MappingNode mnode = (MappingNode)valueNode;
									mnode.setTypes(String.class, valueType);
									mnode.setUseClassConstructor(true);
									typeDetected = true;
								}
							}
						}
						catch (NoSuchFieldException e)
						{
							// Field not found, continue with normal processing
						}
					}
					
					// Handle case where a scalar value is encountered for a Map field
					if (!typeDetected && valueNode.getNodeId() == NodeId.scalar && property.getType().isAssignableFrom(Map.class))
					{
						// If we encounter a scalar value for a Map field, create an empty map
						// This handles corrupted YAML files where a scalar value is present instead of a mapping
						try
						{
							Map<Object, Object> emptyMap = new HashMap<Object, Object>();
							property.set(object, emptyMap);
							continue; // Skip to next property
						}
						catch (Exception e)
						{
							// Log the error but continue processing
							plugin.getLogger().warning("Failed to create empty map for field " + key + ": " + e.getMessage());
						}
					}
					
					if (!typeDetected && valueNode.getNodeId() != NodeId.scalar)
					{
						// only if there is no explicit TypeDescription
						final Class<?>[] arguments = property.getActualTypeArguments();
						if (arguments != null)
						{
							// type safe (generic) collection may contain the
							// proper class
							if (valueNode.getNodeId() == NodeId.sequence)
							{
								final Class<?> t = arguments[0];
								final SequenceNode snode = (SequenceNode)valueNode;
								snode.setListType(t);
							}
							else if (valueNode.getTag().equals(Tag.SET))
							{
								final Class<?> t = arguments[0];
								final MappingNode mnode = (MappingNode)valueNode;
								mnode.setOnlyKeyType(t);
								mnode.setUseClassConstructor(true);
							}
							else if (property.getType().isAssignableFrom(Map.class))
							{
								final Class<?> ketType = arguments[0];
								final Class<?> valueType = arguments[1];
								final MappingNode mnode = (MappingNode)valueNode;
								mnode.setTypes(ketType, valueType);
								mnode.setUseClassConstructor(true);
							}
							else
							{
								// the type for collection entries cannot be
								// detected
							}
						}
					}
					
					// Additional safety check for scalar values in map fields
					if (valueNode.getNodeId() == NodeId.scalar && property.getType().isAssignableFrom(Map.class))
					{
						// Create an empty map for scalar values in map fields
						try
						{
							Map<Object, Object> emptyMap = new HashMap<Object, Object>();
							property.set(object, emptyMap);
							continue; // Skip to next property
						}
						catch (Exception e)
						{
							plugin.getLogger().warning("Failed to create empty map for field " + key + ": " + e.getMessage());
						}
					}
					
					// Handle the specific case where a scalar is encountered for a Map field with MapValueType annotation
					if (valueNode.getNodeId() == NodeId.scalar)
					{
						try
						{
							Field field = beanType.getDeclaredField(key);
							if (field.isAnnotationPresent(MapValueType.class) && property.getType().isAssignableFrom(Map.class))
							{
								// Create an empty map for the specific type
								MapValueType annotation = field.getAnnotation(MapValueType.class);
								Class<?> valueType = annotation.value();
								
								// Create a properly typed empty map
								Map<String, Object> emptyMap = new HashMap<String, Object>();
								property.set(object, emptyMap);
								continue; // Skip to next property
							}
						}
						catch (NoSuchFieldException e)
						{
							// Field not found, continue with normal processing
						}
						catch (Exception e)
						{
							plugin.getLogger().warning("Failed to handle scalar value for Map field " + key + ": " + e.getMessage());
						}
					}
					
					final Object value = constructObject(valueNode);
					property.set(object, value);
				}
				catch (Exception e)
				{
					// Check if this is the specific case we're trying to handle
					if (e.getMessage() != null && e.getMessage().contains("ScalarNode cannot be cast to") && e.getMessage().contains("MappingNode"))
					{
						plugin.getLogger().warning("Handling corrupted YAML for property " + key + ": " + e.getMessage());
						
						// Try to create an empty map for Map fields
						try
						{
							Field field = beanType.getDeclaredField(key);
							if (field.isAnnotationPresent(MapValueType.class))
							{
								Map<Object, Object> emptyMap = new HashMap<Object, Object>();
								// Use reflection to set the field directly
								field.setAccessible(true);
								field.set(object, emptyMap);
								plugin.getLogger().info("Successfully created empty map for corrupted field " + key);
								continue; // Skip to next property
							}
						}
						catch (Exception ex)
						{
							plugin.getLogger().warning("Failed to create empty map for field " + key + ": " + ex.getMessage());
							// Continue with the exception to let the recovery mechanism handle it
						}
					}
					
					// Re-throw the exception to let the recovery mechanism handle it
					throw new YAMLException("Cannot create property=" + key + " for JavaBean="
											+ object + "; " + e.getMessage(), e);
				}
			}
			return object;
		}
	}
}
