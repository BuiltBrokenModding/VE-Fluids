package com.builtbroken.mc.fluids;

import com.builtbroken.mc.fluids.api.reg.BucketMaterialRegistryEvent;
import com.builtbroken.mc.fluids.api.reg.FluidRegistryEvent;
import com.builtbroken.mc.fluids.bucket.BucketMaterial;
import com.builtbroken.mc.fluids.bucket.BucketMaterialHandler;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import com.builtbroken.mc.fluids.fluid.FluidHelper;
import com.builtbroken.mc.fluids.fluid.Fluids;
import com.builtbroken.mc.fluids.mods.pam.PamFreshWaterBucketRecipe;
import com.builtbroken.mc.fluids.mods.pam.PamMilkBucketRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;

/**
 * Module class for handling all interaction with fluids for Voltz Engine and it's sub mods
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/2/2017.
 */
@Mod(modid = FluidModule.DOMAIN, name = "VoltzEngine Fluids module", version = FluidModule.VERSION)
public final class FluidModule
{
    public static final String DOMAIN = "vefluids";

    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;

    /** VM argument to trigger running of debug only options */
    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    /** Information output thing */
    public static final Logger logger = LogManager.getLogger("SBM-NoMoreRain");
    /** Main config */
    public static Configuration config;
    /** Bucket material config */
    public static Configuration bucketConfig;

    @SidedProxy(clientSide = "com.builtbroken.mc.fluids.client.ClientProxy", serverSide = "com.builtbroken.mc.fluids.CommonProxy")
    public static CommonProxy proxy;

    /** Test material that mimics the vanilla bucket */
    public static BucketMaterial materialIron;

    /** Bucket item */
    public static ItemFluidBucket bucket;

    public FluidModule()
    {
        if (runningAsDev)
        {
            FluidRegistry.enableUniversalBucket();
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent preInitEvent)
    {
        config = new Configuration(new File(preInitEvent.getModConfigurationDirectory(), "bbm/Fluid_Module/core.cfg"));
        config.load();

        bucketConfig = new Configuration(new File(config.getConfigFile().getParent(), "bucket_materials.cfg"));
        bucketConfig.load();

        //Load config, should always happen for any material is registered
        BucketMaterialHandler.load(bucketConfig);

        //Default bucket, mainly for testing as its not craftable
        materialIron = new BucketMaterial(DOMAIN + ":ironBucket", new ResourceLocation(DOMAIN, "items/bucket"));
        BucketMaterialHandler.addMaterial("iron", materialIron, 30000);

        //Handle default supported fluids
        Fluids.load(config);

        //Fire registry events to allow mods to load content for this mod
        MinecraftForge.EVENT_BUS.post(new FluidRegistryEvent.Pre());
        MinecraftForge.EVENT_BUS.post(new FluidRegistryEvent.Post());
        MinecraftForge.EVENT_BUS.post(new BucketMaterialRegistryEvent.Pre());
        MinecraftForge.EVENT_BUS.post(new BucketMaterialRegistryEvent.Post());

        //Register Item
        registerItems();
        registerBlocks();

        proxy.preInit();

        //Used to compare rendering
        if (runningAsDev)
        {
            for (Fluid fluid : FluidHelper.generatedFluids)
            {
                FluidRegistry.addBucketForFluid(fluid);
            }
        }
    }

    public void registerItems()
    {
        GameRegistry.register(bucket = new ItemFluidBucket(DOMAIN + ":bucket"));
    }

    public void registerBlocks()
    {
        for (Fluid fluid : FluidHelper.generatedFluids)
        {
            FluidHelper.createBlockForFluidIfMissing(fluid);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        //Load per material configs
        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            material.handleConfig(bucketConfig);
        }

        //Load recipe handling for other mods
        if (bucket != null)
        {
            //TODO add pam's harvest craft support
            if (Loader.isModLoaded("harvestcraft"))
            {
                if (config.getBoolean("EnableRegisteringMilkBucket", "PamHarvestCraftSupport", true, "Registers the milk bucket to the ore dictionary to be used in Pam's Harvest Craft recipes"))
                {
                    RecipeSorter.register(DOMAIN + ":woodenBucketFreshMilk", PamMilkBucketRecipe.class, SHAPED, "after:minecraft:shaped");
                    if (FluidRegistry.getFluid("milk") != null)
                    {
                        Item itemFreshMilk = (Item) Item.REGISTRY.getObject(new ResourceLocation("harvestcraft:freshmilkItem"));
                        if (itemFreshMilk == null)
                        {
                            logger.error("Failed to find item harvestcraft:freshmilkItem");
                        }

                        FluidStack milkFluidStack = new FluidStack(FluidRegistry.getFluid("milk"), FluidContainerRegistry.BUCKET_VOLUME);
                        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
                        {
                            ItemStack milkBucket = new ItemStack(bucket, 1, material.metaValue);
                            bucket.fill(milkBucket, milkFluidStack, true);

                            GameRegistry.addRecipe(new PamMilkBucketRecipe(milkBucket, new ItemStack(itemFreshMilk, 4, 0)));
                        }
                    }
                }
                if (config.getBoolean("EnableRegisteringFreshWaterBucket", "PamHarvestCraftSupport", true, "Registers the water bucket to the ore dictionary to be used in Pam's Harvest Craft recipes"))
                {
                    RecipeSorter.register(DOMAIN + ":woodenBucketFreshMilk", PamFreshWaterBucketRecipe.class, SHAPED, "after:minecraft:shaped");
                    if (FluidRegistry.getFluid("milk") != null)
                    {
                        Item itemFreshWater = (Item) Item.REGISTRY.getObject(new ResourceLocation("harvestcraft:freshwaterItem"));
                        if (itemFreshWater == null)
                        {
                            logger.error("Failed to find item harvestcraft:freshwaterItem");
                        }

                        FluidStack waterStack = new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);
                        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
                        {
                            ItemStack milkBucket = new ItemStack(bucket, 1, material.metaValue);
                            bucket.fill(milkBucket, waterStack, true);

                            GameRegistry.addRecipe(new PamFreshWaterBucketRecipe(milkBucket, new ItemStack(itemFreshWater, 1, 0)));
                        }
                    }
                }
            }
        }
        proxy.postInit();
        config.save();
    }

    @Mod.EventHandler
    public void loadCompleted(FMLLoadCompleteEvent event)
    {

        //Save config
        BucketMaterialHandler.save(bucketConfig);
        bucketConfig.save();
    }
}
