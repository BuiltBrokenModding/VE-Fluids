package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.api.material.IBucketMaterial;
import com.builtbroken.mc.fluids.mods.BucketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles customization for a material value
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/3/2017.
 */
public class BucketMaterial implements IBucketMaterial
{
    //Settings
    public boolean preventHotFluidUsage = true;
    public boolean damageBucketWithHotFluid = true;
    public boolean burnEntityWithHotFluid = true;
    public boolean enableFluidLeaking = false;
    public boolean allowLeakToCauseFires = true;

    public int viscosityToIgnoreLeaking = 3000;
    public int amountToLeak = 1;
    public float chanceToLeak = 0.03f;
    public float leakFireChance = 0.4f;

    public List<String> entityInteractionList = new ArrayList();
    public boolean entityInteractionAllowList = false;

    private BucketHandler handler;

    /**
     * Localization to translate, prefixed with 'item.'
     */
    public String localization;

    /**
     * Name of the material, set on register
     */
    public String materialName;
    /**
     * Item meta value this material is registered to
     */
    public int metaValue = -1;

    protected ResourceLocation bucketResourceLocation;
    protected ResourceLocation fluidResourceLocation;
    protected boolean invertBucketRender = false;
    protected boolean invertFluidRender = false;
    protected boolean disableGasRenderFlip = false;

    public BucketMaterial(String localization, ResourceLocation bucketResourceLocation)
    {
        this.localization = localization;
        this.bucketResourceLocation = bucketResourceLocation;
    }

    /**
     * Gets the damaged (from fire, or heat) version of the bucket
     *
     * @param stack - current bucket
     * @return - material to switch to
     */
    public BucketMaterial getDamagedBucket(ItemStack stack) //TODO define damage cause
    {
        return null;
    }

    /**
     * Called to handle config data for the bucket material
     *
     * @param config
     */
    public void handleConfig(Configuration config) //TODO nest inside a settings class to clean up the code
    {
        preventHotFluidUsage = config.getBoolean("PreventHotFluidUsage", getConfigCategory(), preventHotFluidUsage, "Enables settings that attempt to prevent players from wanting to use the bucket for moving hot fluids");
        damageBucketWithHotFluid = config.getBoolean("DamageBucketWithHotFluid", getConfigCategory(), damageBucketWithHotFluid, "Will randomly destroy the bucket if it contains hot fluid, lava in other words");
        burnEntityWithHotFluid = config.getBoolean("BurnPlayerWithHotFluid", getConfigCategory(), burnEntityWithHotFluid, "Will light the player on fire if the bucket contains a hot fluid, lava in other words");
        enableFluidLeaking = config.getBoolean("Enable", getConfigCategory(), enableFluidLeaking, "Allows fluid to slowly leak out of the bucket as a nerf. Requested by Darkosto");
        viscosityToIgnoreLeaking = config.getInt("MaxViscosity", getConfigCategory(), viscosityToIgnoreLeaking, -1, 10000, "At which point it the flow rate so slow that the leak is plugged, higher values are slower");
        amountToLeak = config.getInt("MaxLeakAmount", getConfigCategory(), amountToLeak, 0, 10000, "How much can leak from the bucket each time a leak happens, number is max amount and is randomly ranged between 0 - #");
        chanceToLeak = config.getFloat("LeakChance", getConfigCategory(), chanceToLeak, 0f, 1f, "What is the chance that a leak will happen, calculated each tick with high numbers being more often");
        allowLeakToCauseFires = config.getBoolean("AllowFires", getConfigCategory(), allowLeakToCauseFires, "If molten fluid leaks, should there be a chance to cause fires?");
        leakFireChance = config.getFloat("FireChance", getConfigCategory(), leakFireChance, 0f, 1f, "How often to cause fire from molten fluids leaking");

        String[] entities = config.getStringList("EntityInteractionList", getConfigCategory(), new String[]{"entity1", "entity2"}, "List of entities for interaction, can be used as allow or block list by changing setting.");
        entityInteractionAllowList = !config.getBoolean("EntityInteractionBlockList", getConfigCategory(), !entityInteractionAllowList, "Changes how entity interaction list is used, true will act as a block list, false will act as allow only.");

        if (entities != null)
        {
            for (String s : entities)
            {
                if (s != null && !s.isEmpty())
                {
                    entityInteractionList.add(s.trim()); //TODO add debug to check if entity exists
                }
            }
        }
    }

    protected final String getConfigCategory()
    {
        return "BucketUsage." + materialName;
    }

    public String getTranslationKey(ItemStack stack)
    {
        return "item." + localization;
    }

    //TODO move to JSON and front end reg
    public ResourceLocation getBucketResourceLocation()
    {
        return bucketResourceLocation;
    }

    //TODO move to JSON and front end reg
    public ResourceLocation getFluidResourceLocation()
    {
        return fluidResourceLocation;
    }

    /**
     * Should the bucket render upside down by default.
     * <p>
     * Gases are handled automatically this is for the actual
     * default render. Gases will render in the opposite direction
     * unless disabled.
     *
     * @return true to flip upside down by default
     */
    public boolean shouldInvertBucketRender()
    {
        return invertBucketRender;
    }

    public boolean shouldInvertFluidRender()
    {
        return invertFluidRender;
    }

    public BucketMaterial invertBucketRender()
    {
        invertBucketRender = true;
        return this;
    }

    public BucketMaterial invertFluidRender()
    {
        invertFluidRender = true;
        return this;
    }

    /**
     * Should we disable the gas render flip
     * <p>
     * Use this if your container does not need to
     * change based on if the fluid is a gas or not.
     * By default most mods fluid there container if
     * it is not sealed to help fit the Minecraft
     * lore.
     *
     * @return true to disable
     */
    public boolean disableGasFlip()
    {
        return disableGasRenderFlip;
    }

    /**
     * Called to check if the bucket material will work with the entity
     *
     * @param entry
     * @return
     */
    public boolean allowInteractionOfEntity(EntityEntry entry)
    {
        return entityInteractionAllowList ? entityInteractionList.contains(entry.getName()) : !entityInteractionList.contains(entry.getName());
    }

    /**
     * Handler for this material, gets called first before any external handler
     */
    public BucketHandler getHandler()
    {
        return handler;
    }

    public void setHandler(BucketHandler handler)
    {
        this.handler = handler;
        BucketHandler.addBucketHandler(handler);
    }

    /**
     * Gets a new bucket stack instance.
     * <p>
     * The stack passed in may be a bucket matching the current material
     * or anther item. It is up to the implementation to decide how
     * to handle this stack.
     * <p>
     * If its a bucket the recommendation is to copy material specific data. If
     * its an item and this material is mean to mimic said item. Then the recommendation
     * is to copy the item to return as am empty container.
     *
     * @param heldItemStack - can be null, will be provided for copying data
     * @return new stack of this bucket
     */
    public ItemStack getNewBucketStack(ItemStack heldItemStack)
    {
        return new ItemStack(FluidModule.bucket, 1, metaValue);
    }

    public ItemStack getEmptyBucket(ItemStack heldItemStack)
    {
        return getNewBucketStack(heldItemStack);
    }

    @Override
    public String toString()
    {
        return "BucketMaterial[" + materialName + "]@" + metaValue;
    }
}
