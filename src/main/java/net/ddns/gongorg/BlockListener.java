package net.ddns.gongorg;

//import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.BlockFace;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
//import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
//import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.Location;

/**
 * block listener
 * 
 * @author cppchriscpp
 */
public class BlockListener implements org.bukkit.event.Listener {
    private final BoothPortals plugin;


    /**
     * Constructor
     * 
     * @param plugin
     *            The plugin to attach to.
     */
    public BlockListener(final BoothPortals plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a block is placed; let us know if a portal is being created.
     * 
     * @param event
     *            The event related to the block placement.
     * 
     *            The portal is finished with the door (any wooden door)
     *            leading to the interior of the portal.
     *            The situation we are looking for is when a door is placed
     *            between two columns of redstone block (at least 2 blocks high
     *            each), and two other such columns are located in front so that
     *            the four of them form the vertices of a square:
     * 
     *             R | * | R 
     *            ---+-S-+--- 
     *               |   |  
     *            ---+---+--- 
     *             R | D | R
     * 
     *            Besides the four redstone columns, a sign must be placed on top of the door
     *            (which gives the unique name of the portal), and another one must be placed
     *            inside the portal  (where the S appears).
     * 
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location bl = block.getLocation();
        org.bukkit.World world = player.getWorld();

        if (event.isCancelled()) {
            return;
        }
        //
        // do not allow placing any blocks inside a portal
        //
        for (Portal p  : plugin.portals.values()) {
            if (p.isInterior(bl)) {
                event.setCancelled(true);
                return;
            }
        }
        //
        // nothing else to check if object being placed is not a door
        //
        if (!plugin.isDoor(block))
            return;
        
        //
        // may be a portal
        //
        int dx = block.getX();
        int dy = block.getY();
        int dz = block.getZ();
        // data unique to doors
        org.bukkit.material.Door doorData = (org.bukkit.material.Door) block.getState().getData(); 
        if (doorData.isTopHalf()) { // top half of door does not contain valid information
            doorData = (org.bukkit.material.Door) block.getRelative(0, -1, 0).getState().getData();
            dy -= 1;
        }
        //
        // we are sure that (dx,dy,dz) corresponds to the lower block of the door
        //
        Block doorSign = null;
        // 
        // now determine orientation of the portal
        // further checks are done depending on that
        //
        BlockFace facing =  doorData.getFacing();
        int cx = dx;
        int cy = dy;
        int cz = dz;
        switch (facing) {
        case NORTH:
            cx = dx; cy = dy; cz = dz+1; 
            doorSign = world.getBlockAt(dx,dy+2,dz-1);
            break;
        case SOUTH:
            cx = dx; cy = dy; cz = dz-1; 
            doorSign = world.getBlockAt(dx,dy+2,dz+1);
            break;
        case EAST:
            cx = dx-1; cy = dy; cz = dz; 
            doorSign = world.getBlockAt(dx+1,dy+2,dz);
            break;
        case WEST:
            cx = dx+1; cy = dy; cz = dz; 
            doorSign = world.getBlockAt(dx-1,dy+2,dz);
            break;
            default:
                // cannot be
        }
        plugin.log.debug("door: x= " + dx + " y=" + dy + "z=" + dz);
        plugin.log.debug("center: x= " + cx + " y=" + cy + "z=" + cz);
        if (doorSign.getType() != Material.WALL_SIGN) {
            plugin.log.debug("No sign above door.");
            return;
        }
        //
        // block at center of booth must be empty
        //
        if (world.getBlockAt(cx, cy, cz).getType() != Material.AIR) {
            plugin.log.debug("Not a booth. Center must be empty.");
            return;
        }
        //
        // check for inner sign, the one that points to the destination
        // it must be facing to the same side as the other sign
        //
        Block innerSign = world.getBlockAt(cx,cy+1,cz);
        if (innerSign.getType() != Material.WALL_SIGN) {
            plugin.log.debug("Not a booth. Lacks inner sign.");
            return;
        }
        Sign signState = (Sign) innerSign.getState();
        plugin.log.debug("Destination reads:" + String.join("\n", signState.getLines()));
        
        //
        // Name is obtained from door sign, and it must be unique!
        //
        String signName = String.join(" ", ((Sign)doorSign.getState()).getLines()).trim();
        if (plugin.getPortal(signName) != null) {
            plugin.log.debug("There is another sign with the name " + signName);
            player.sendMessage(ChatColor.YELLOW + "sign_exists" + signName);
            return;
        }
        //
        // check for columns
        //
        if (    plugin.isBooth(world.getBlockAt(cx+1,cy  ,cz+1)) &&
                plugin.isBooth(world.getBlockAt(cx+1,cy  ,cz-1)) &&
                plugin.isBooth(world.getBlockAt(cx-1,cy  ,cz+1)) &&
                plugin.isBooth(world.getBlockAt(cx-1,cy  ,cz-1)) &&
                plugin.isBooth(world.getBlockAt(cx+1,cy+1,cz+1)) &&
                plugin.isBooth(world.getBlockAt(cx+1,cy+1,cz-1)) &&
                plugin.isBooth(world.getBlockAt(cx-1,cy+1,cz+1)) &&
                plugin.isBooth(world.getBlockAt(cx-1,cy+1,cz-1))) {
            plugin.log.debug("Congratulations! It is a portal!");
            double tmp = 0.03125; // a little off centering so that so that roundings go towards correct place
            plugin.addPortal(signName,new Location(world, cx + 0.5 - tmp, cy+ tmp, cz + 0.5-tmp),
                    new Location(world, dx, dy, dz));
        }
    }

}
