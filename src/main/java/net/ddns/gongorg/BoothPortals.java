package net.ddns.gongorg;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

//import org.bukkit.Location;
//import org.bukkit.Server;
//import org.bukkit.World;
//import org.bukkit.block.Block;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.entity.Player;

public class BoothPortals extends org.bukkit.plugin.java.JavaPlugin {

    // private ArrayList<Material> doorTypes = new ArrayList<Material>();
    /**
     * Logging component.
     */
    public Logger log;
    private String pluginName;
    private static final String permissionNode = "boothportals.";

    private String pluginVersion;
    private ResourceBundle i18nResource;

    private Material boothMaterial = Material.REDSTONE_BLOCK;
    TreeMap<String,Portal> portals = new TreeMap<String,Portal>();
    Storage storage;

    private boolean suspended = false;

    public void onLoad() {
        org.bukkit.plugin.PluginDescriptionFile pdfFile = this.getDescription();
        pluginName = pdfFile.getName();
        pluginVersion = pdfFile.getVersion();
    }

    void configurePlugin() {
        FileConfiguration conf = getConfig();
        boolean debugMode = conf.getBoolean("debug", false);
        log = new Logger(pluginName + " v" + pluginVersion, debugMode);
        log.info("Debugging is set to " + debugMode);
        conf.get("locale_country", "UY");
        final String language = conf.getString("locale_language", "es");
        final String country = conf.getString("locale_country", "");
        log.info("Locale set to " + language + " " + country);
        final Locale locale = new Locale(language, country);
        i18nResource = ResourceBundle.getBundle("Messages", locale);
		String materialName = conf.getString("booth_material","QUARTZ_BLOCK");
		boothMaterial = Material.getMaterial(materialName);
		if (boothMaterial == null) {
	    	boothMaterial = Material.QUARTZ_BLOCK;
		}
	this.log.debug("Booth material is " + boothMaterial);
    }

    public void onEnable() {
        configurePlugin();
        storage = new Storage(this);
        storage.loadCSV();
        log.info("Loaded " + portals.size() + " portals.");
        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        getCommand("booth").setExecutor(new CommandExecutor(this));
    }

    public void onDisable() {
        storage.saveCSV();
    }

    boolean isSuspended() {
        return suspended;
    }

    void suspendPortals() {
        suspended = true;
    }

    void resumePortals() {
        suspended = false;
    }

    boolean isDoor(Block b) {
        if (b == null)
            return false;
        Material m = b.getType();
        return (m == Material.DARK_OAK_DOOR) || (m == Material.WOOD_DOOR)
                || (m == Material.WOODEN_DOOR) || (m == Material.ACACIA_DOOR)
                || (m == Material.SPRUCE_DOOR);
    }

    boolean isBooth(Block b) {
        return b.getType() == boothMaterial;
    }

    Portal getPortalAt(org.bukkit.Location l) {
        for (Portal p: portals.values()) {
            if (p.isInterior(l)) return p;
        }
        return null;
    }

    Portal getPortal(String name) {
        if (portals.containsKey(name)) {
            return portals.get(name);
        } else {
            return null;
        }
    }
    
    Location getDestination(String name) {
        Portal destPortal = getPortal(name);
        return destPortal != null ? 
                destPortal.getSourceLocation() : null;
    }
    
    
//    boolean isPortalDoor(org.bukkit.Location l) {
//        for (Portal p : portals) {
//            if (p.isDoorBlock(l))
//                return true;
//        }
//        return false;
//    }

    boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permissionNode + permission);
    }

    void addPortal(String name, Location centerLoc, Location doorLoc) {
        Portal p = new Portal(name,centerLoc, doorLoc);
        this.portals.put(name,p);
    }

    void removePortal(String name) {
        log.info("Removing portal " + name);
        org.bukkit.Bukkit.broadcastMessage(ChatColor.RED
                + i18nResource.getString("removed_portal") + " " + name);
        portals.remove(name);
        for (Portal q : portals.values()) {
            String destName = q.getDestinationName();
            if ((destName != null) && name.equals(destName)) { // the removed portal is
                                                      // the destination of q
                q.setDestinationName(null);
                q.disable();
                log.info("Portal " + q + " points nowhere now. Disabled.");
            }
        }
    }

    String listPortals() {
        StringBuffer s = new StringBuffer("There are " + portals.size()
                + " portals.\n");
        java.util.Iterator<Portal> it = portals.values().iterator();
        while (it.hasNext()) {
            s.append("-").append(it.next()).append('\n');
        }
        return s.toString();
    }
}
