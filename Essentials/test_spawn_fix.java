import java.io.StringReader;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import com.earth2me.essentials.settings.Spawns;
import com.earth2me.essentials.storage.YamlStorageReader;
import com.earth2me.essentials.storage.ObjectLoadException;

public class test_spawn_fix {
    
    public static void main(String[] args) {
        // Test YAML data that represents a spawn location
        String yamlData = 
            "spawns:\n" +
            "  default:\n" +
            "    world: world\n" +
            "    x: 100.5\n" +
            "    y: 64.0\n" +
            "    z: 200.5\n" +
            "    yaw: 90.0\n" +
            "    pitch: 0.0\n";
        
        try {
            // Create a mock plugin for testing
            MockPlugin mockPlugin = new MockPlugin();
            
            // Test the YamlStorageReader with our fix
            YamlStorageReader reader = new YamlStorageReader(new StringReader(yamlData), mockPlugin);
            Spawns spawns = reader.load(Spawns.class);
            
            // Verify that the spawn location was properly deserialized
            Map<String, Location> spawnMap = spawns.getSpawns();
            if (spawnMap != null && spawnMap.containsKey("default")) {
                Location spawnLoc = spawnMap.get("default");
                if (spawnLoc != null && spawnLoc instanceof Location) {
                    System.out.println("SUCCESS: Spawn location properly deserialized!");
                    System.out.println("World: " + spawnLoc.getWorld().getName());
                    System.out.println("X: " + spawnLoc.getX());
                    System.out.println("Y: " + spawnLoc.getY());
                    System.out.println("Z: " + spawnLoc.getZ());
                    System.out.println("Yaw: " + spawnLoc.getYaw());
                    System.out.println("Pitch: " + spawnLoc.getPitch());
                } else {
                    System.out.println("FAILED: Spawn location is not a proper Location object");
                }
            } else {
                System.out.println("FAILED: No spawn data found or spawn map is null");
            }
            
        } catch (ObjectLoadException e) {
            System.out.println("FAILED: Exception during deserialization: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("FAILED: Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Mock plugin class for testing
    static class MockPlugin implements Plugin {
        @Override
        public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
        @Override
        public org.bukkit.configuration.file.FileConfiguration getConfig() { return null; }
        @Override
        public void reloadConfig() {}
        @Override
        public org.bukkit.Server getServer() { return null; }
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public void onDisable() {}
        @Override
        public void onLoad() {}
        @Override
        public void onEnable() {}
        @Override
        public boolean isNaggable() { return false; }
        @Override
        public void setNaggable(boolean canNag) {}
        @Override
        public org.bukkit.util.FileUtil getFile() { return null; }
        @Override
        public org.bukkit.command.Command getCommand(String name) { return null; }
        @Override
        public void saveConfig() {}
        @Override
        public void saveDefaultConfig() {}
        @Override
        public void saveResource(String filePath, boolean replace) {}
        @Override
        public java.io.InputStream getResource(String filename) { return null; }
        @Override
        public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getLogger("MockPlugin"); }
        @Override
        public java.lang.String getName() { return "MockPlugin"; }
        @Override
        public java.lang.ClassLoader getClassLoader() { return getClass().getClassLoader(); }
    }
} 