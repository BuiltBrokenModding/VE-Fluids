package com.builtbroken.mc.fluids.bucket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

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

    /** Does the material support liquid fluids */
    public boolean supportsLiquids = true;
    /** Does the material support gas based fluids */
    public boolean supportsGases = true;

    /** Should the bucket limits what fluids can be used */
    public boolean restrictFluids = false;
    /** Is the restriction list an allow or deny list */
    public boolean restrictFluidsAllowList = false;
    /** Restriction list */
    public List<String> restrictFluidList = new ArrayList();

    public int viscosityToIgnoreLeaking = 3000;
    public int amountToLeak = 1;
    public float chanceToLeak = 0.03f;
    public float leakFireChance = 0.4f;

    /** Localization to translate, prefixed with 'item.' */
    public String localization;

    /** Name of the material, set on register */
    public String materialName;
    /** Item meta value this material is registered to */
    public int metaValue;

    protected ResourceLocation bucketResourceLocation;
    protected ResourceLocation fluidResourceLocation;

    /** Inventory icon */
    @SideOnly(Side.CLIENT)
    private IIcon bucketIcon;
    @SideOnly(Side.CLIENT)
    private IIcon fluidIcon;

    public BucketMaterial(String localization, ResourceLocation bucketResourceLocation)
    {
        this.localization = localization;
        this.bucketResourceLocation = bucketResourceLocation;
    }

    public BucketMaterial(String localization, String bucketResourceLocation)
    {
        this.localization = localization;
        this.bucketResourceLocation = new ResourceLocation(bucketResourceLocation);
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
        final String category = "BucketUsage." + materialName;

        //Hot fluid configs
        preventHotFluidUsage = config.getBoolean("PreventHotFluidUsage", category, preventHotFluidUsage, "Enables settings that attempt to prevent players from wanting to use the bucket for moving hot fluids");
        damageBucketWithHotFluid = config.getBoolean("DamageBucketWithHotFluid", category, damageBucketWithHotFluid, "Will randomly destroy the bucket if it contains hot fluid, lava in other words");
        burnEntityWithHotFluid = config.getBoolean("BurnPlayerWithHotFluid", category, burnEntityWithHotFluid, "Will light the player on fire if the bucket contains a hot fluid, lava in other words");

        //Leak configs
        enableFluidLeaking = config.getBoolean("Enable", category, enableFluidLeaking, "Allows fluid to slowly leak out of the bucket as a nerf. Requested by Darkosto");
        viscosityToIgnoreLeaking = config.getInt("MaxViscosity", category, viscosityToIgnoreLeaking, -1, 10000, "At which point it the flow rate so slow that the leak is plugged, higher values are slower");
        amountToLeak = config.getInt("MaxLeakAmount", category, amountToLeak, 0, 10000, "How much can leak from the bucket each time a leak happens, number is max amount and is randomly ranged between 0 - #");
        chanceToLeak = config.getFloat("LeakChance", category, chanceToLeak, 0f, 1f, "What is the chance that a leak will happen, calculated each tick with high numbers being more often");
        allowLeakToCauseFires = config.getBoolean("AllowFires", category, allowLeakToCauseFires, "If molten fluid leaks, should there be a chance to cause fires?");
        leakFireChance = config.getFloat("FireChance", category, leakFireChance, 0f, 1f, "How often to cause fire from molten fluids leaking");

        //Fluid restriction configs
        supportsLiquids = config.getBoolean("EnableLiquids", category, supportsLiquids, "Allows disabling liquid based fluids (water, lava, anything not a gas)");
        supportsGases = config.getBoolean("EnableGases", category, supportsGases, "Allows disabling gas based fluids (steam, air, gas, anything not a liquid)");
        restrictFluids = config.getBoolean("EnableFluidList", category, restrictFluids, "Enables the container to restrict the fluid it contains");
        restrictFluidsAllowList = config.getBoolean("FluidAllowList", category, restrictFluidsAllowList, "True will use the list as an allow check, false will use the list as a deny check");

        //Load restriction list
        if (restrictFluidList.isEmpty())
        {
            restrictFluidList.add("water");
            restrictFluidList.add("lava");
        }
        String[] fluids = config.getStringList("FluidList", category, restrictFluidList.stream().toArray(String[]::new), "List of fluids to use for restriction");
        if (fluids != null && fluids.length > 0)
        {
            restrictFluidList.clear();
            for (String s : fluids)
            {
                if (s != null && !s.isEmpty())
                {
                    restrictFluidList.add(s.trim().toLowerCase());
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public String getUnlocalizedName(ItemStack stack)
    {
        return "item." + localization;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {
        //this is using 1.8 code for texture location that was back ported, thus we need to modify it to work with 1.7
        if (getBucketResourceLocation() != null)
        {
            bucketIcon = register.registerIcon(getBucketResourceLocation().toString().replace("items/", ""));
        }
        if (getFluidResourceLocation() != null)
        {
            fluidIcon = register.registerIcon(getFluidResourceLocation().toString().replace("items/", ""));
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getBucketIcon(ItemStack stack)
    {
        return bucketIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getFluidIcon(ItemStack stack, Fluid fluid)
    {
        return fluidIcon;
    }

    public ResourceLocation getBucketResourceLocation()
    {
        return bucketResourceLocation;
    }

    public ResourceLocation getFluidResourceLocation()
    {
        return fluidResourceLocation;
    }

    /**
     * Checks if the bucket material can support the fluid
     * <p>
     * By default this is always true, but users can define there own settings
     * per material.
     *
     * @param container - bucket stack
     * @param resource  - fluid
     * @return true if is supported
     */
    public boolean supportsFluid(ItemStack container, FluidStack resource)
    {
        if (resource != null && resource.getFluid() != null)
        {
            final Fluid fluid = resource.getFluid();
            final String name = fluid.getName().toLowerCase();

            //Check for gas support
            if (fluid.isGaseous(resource) && !supportsGases)
            {
                return false;
            }

            //Check for liquid support
            if (!fluid.isGaseous(resource) && !supportsLiquids)
            {
                return false;
            }

            //Check if the fluid is restricted
            if (restrictFluids)
            {
                if (restrictFluidsAllowList)
                {
                    return restrictFluidList.contains(name);
                }
                else
                {
                    return !restrictFluidList.contains(name);
                }
            }
            return true;
        }
        return false;
    }
}
