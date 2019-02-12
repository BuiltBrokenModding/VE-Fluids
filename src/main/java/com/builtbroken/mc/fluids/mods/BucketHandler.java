package com.builtbroken.mc.fluids.mods;

import com.builtbroken.mc.fluids.FluidModule;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers interaction between the bucket and it's enviroment
 * <p>
 * This class is designed for mod support but can be used to customize the bucket's logic
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/28/2016.
 */
public class BucketHandler
{
    /** Map of Block to handler that supports functionality for bucket interaction */
    public static final HashMap<Block, BucketHandler> blockToHandler = new HashMap();
    /** Map of Item to handler that supports functionality for bucket interaction */
    public static final HashMap<Fluid, BucketHandler> fluidToHandler = new HashMap();

    /** Map of Entity to handler that supports functionality for bucket interaction */
    public static final HashMap<Class<? extends Entity>, List<BucketHandler>> entityToHandler = new HashMap();

    /** List of all register bucket handlers */
    public static final List<BucketHandler> bucketHandlers = new ArrayList();

    public final String mod;
    public final String name;
    public boolean isEnabled = true;

    public static void addBucketHandler(BucketHandler handler)
    {
        if (!bucketHandlers.contains(handler))
        {
            bucketHandlers.add(handler);
        }
    }

    public static void addBucketHandler(Block block, BucketHandler handler)
    {
        addBucketHandler(handler);
        if (blockToHandler.containsKey(block) && blockToHandler.get(block) != null)
        {
            FluidModule.logger.error("Overriding handler '" + blockToHandler.get(block) + "' for block '" + block.getTranslationKey() + "' with " + handler);
        }
        blockToHandler.put(block, handler);
    }

    public static void addBucketHandler(Fluid fluid, BucketHandler handler)
    {
        addBucketHandler(handler);
        if (fluidToHandler.containsKey(fluid) && fluidToHandler.get(fluid) != null)
        {
            FluidModule.logger.error("Overriding BucketHandler '" + fluidToHandler.get(fluid) + "' for fluid '" + fluid.getUnlocalizedName() + "' with " + handler);
        }
        fluidToHandler.put(fluid, handler);
    }

    public static void addBucketHandler(Class<? extends Entity> entity, BucketHandler handler)
    {
        addBucketHandler(handler);
    }

    public BucketHandler()
    {
        this(null, null);
    }

    public BucketHandler(String mod, String name)
    {
        this.mod = mod;
        this.name = name;
    }


    /**
     * Called to load settings about the the handler
     *
     * @param configuration
     */
    public void loadSettings(Configuration configuration)
    {
        if (name != null)
        {
            isEnabled = configuration.getBoolean("enable", getConfigCategory(), true, "Allows disabling bucket handling, disabling this could result in " +
                    "logic not working and issues with the mod. Only disable this if you know what your doing.");
        }
    }

    protected String getConfigCategory()
    {
        return "BucketHandler_" + getID();
    }

    public String getID()
    {
        return mod + ":" + name;
    }

    /**
     * Called when the bucket is right clicked while empty
     *
     * @param itemstack
     * @param world
     * @param player
     * @return
     */
    public ItemStack emptyBucketClickBlock(EntityPlayer player, ItemStack itemstack, World world, BlockPos pos)
    {
        return itemstack;
    }

    /**
     * Called when the bucket contains fluid and is clicked
     *
     * @param player
     * @param itemstack
     * @param world
     * @return
     */
    public ItemStack filledBucketClickBlock(EntityPlayer player, ItemStack itemstack, World world, BlockPos pos)
    {
        return itemstack;
    }

    public ItemStack placeFluidClickBlock(EntityPlayer player, ItemStack itemstack, World world, BlockPos pos)
    {
        return itemstack;
    }

    /**
     * Called each tick from an entities inventory
     *
     * @param stack  - bucket
     * @param world  - world inside
     * @param entity - entity who has the item
     * @param slot   - slot the item is inside
     * @param held   - is the item currently held
     * @return true to cancel default functionality
     */
    public boolean onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held)
    {
        return false;
    }

    /**
     * Called in the same loop as {@link #onUpdate(ItemStack, World, Entity, int, boolean)} but
     * only when the bucket has leaked fluid
     *
     * @param stack  - bucket
     * @param world  - world inside
     * @param entity - entity who has the item
     * @param slot   - slot the item is inside
     * @param held   - is the item currently held
     * @return true to cancel default functionality
     */
    public boolean onBucketLeaked(ItemStack stack, World world, Entity entity, int slot, boolean held)
    {
        //If you still want the fluid to drain you need to do it yourself if you return true
        return false;
    }

    /**
     * Called each tick for the bucket when sitting on the ground
     *
     * @param entityItem - bucket entity
     * @return true to stop receiving updates
     */
    public boolean onEntityItemUpdate(EntityItem entityItem)
    {
        return false;
    }

    /**
     * Called when the player right clicks an entity
     *
     * @param stack  - bucket
     * @param player - player
     * @param entity - clicked entity
     * @return
     */
    public boolean rightClickEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity)
    {
        return false;
    }

    /**
     * Pass threw from bucket item to add sub types
     *
     * @param item - the bucket
     * @param list - the list of bucket items to be added to the creative tab
     */
    public void getSubItems(Item item, List list)
    {

    }
}
