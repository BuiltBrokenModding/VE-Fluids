package com.builtbroken.mc.fluids.bucket;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Handles customization for a material value
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/3/2017.
 */
public class BucketMaterial
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

    /** Localization to translate, prefixed with 'item.' */
    public String localization;
    /** Name of the texture to load, should be prefixed with domain 'domain:name' */
    public String textureName;

    /** Name of the material, set on register */
    public String materialName;
    /** Item meta value this material is registered to */
    public int metaValue;

    public BucketMaterial(String localization, String textureName)
    {
        this.localization = localization;
        this.textureName = textureName;
    }

    /**
     * Gets the damaged (from fire, or heat) version of the bucket
     *
     * @param stack - current bucket
     * @return - material to switch to
     */
    public BucketMaterial getDamagedBucket(ItemStack stack)
    {
        return null;
    }

    /**
     * Called to handle config data for the bucket material
     *
     * @param config
     */
    public void handleConfig(Configuration config)
    {
        preventHotFluidUsage = config.getBoolean("PreventHotFluidUsage", "BucketUsage." + materialName, preventHotFluidUsage, "Enables settings that attempt to prevent players from wanting to use the bucket for moving hot fluids");
        damageBucketWithHotFluid = config.getBoolean("DamageBucketWithHotFluid", "BucketUsage." + materialName, damageBucketWithHotFluid, "Will randomly destroy the bucket if it contains hot fluid, lava in other words");
        burnEntityWithHotFluid = config.getBoolean("BurnPlayerWithHotFluid", "BucketUsage." + materialName, burnEntityWithHotFluid, "Will light the player on fire if the bucket contains a hot fluid, lava in other words");
        enableFluidLeaking = config.getBoolean("Enable", "BucketUsage." + materialName, enableFluidLeaking, "Allows fluid to slowly leak out of the bucket as a nerf. Requested by Darkosto");
        viscosityToIgnoreLeaking = config.getInt("MaxViscosity", "BucketUsage." + materialName, viscosityToIgnoreLeaking, -1, 10000, "At which point it the flow rate so slow that the leak is plugged, higher values are slower");
        amountToLeak = config.getInt("MaxLeakAmount", "BucketUsage." + materialName, amountToLeak, 0, 10000, "How much can leak from the bucket each time a leak happens, number is max amount and is randomly ranged between 0 - #");
        chanceToLeak = config.getFloat("LeakChance", "BucketUsage." + materialName, chanceToLeak, 0f, 1f, "What is the chance that a leak will happen, calculated each tick with high numbers being more often");
        allowLeakToCauseFires = config.getBoolean("AllowFires", "BucketUsage." + materialName, allowLeakToCauseFires, "If molten fluid leaks, should there be a chance to cause fires?");
        leakFireChance = config.getFloat("FireChance", "BucketUsage." + materialName, leakFireChance, 0f, 1f, "How often to cause fire from molten fluids leaking");
    }

    @SideOnly(Side.CLIENT)
    public String getUnlocalizedName(ItemStack stack)
    {
        return "item." + localization;
    }
}
