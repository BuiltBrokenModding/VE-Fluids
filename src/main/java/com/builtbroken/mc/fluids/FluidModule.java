package com.builtbroken.mc.fluids;

import com.builtbroken.mc.fluids.api.reg.BucketMaterialRegistryEvent;
import com.builtbroken.mc.fluids.api.reg.FluidRegistryEvent;
import com.builtbroken.mc.fluids.bucket.BucketMaterial;
import com.builtbroken.mc.fluids.bucket.BucketMaterialHandler;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import com.builtbroken.mc.fluids.fluid.FluidHelper;
import com.builtbroken.mc.fluids.fluid.Fluids;
import com.builtbroken.mc.fluids.mods.BucketHandler;
import com.builtbroken.mc.fluids.mods.aa.SlimeRiceBucketRecipe;
import com.builtbroken.mc.fluids.mods.pam.PamBucketRecipe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Module class for handling all interaction with fluids for Voltz Engine and it's sub mods
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/2/2017.
 */
@Mod(modid = FluidModule.DOMAIN, name = "VoltzEngine Fluids module", version = FluidModule.VERSION)
@Mod.EventBusSubscriber(modid = FluidModule.DOMAIN)
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

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(bucket = new ItemFluidBucket(DOMAIN + ":bucket"));
        for (Block block : FluidHelper.generatedFluidBlocks)
        {
            event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        //Fire registry events to allow mods to load content for this mod
        MinecraftForge.EVENT_BUS.post(new FluidRegistryEvent.Pre());
        MinecraftForge.EVENT_BUS.post(new FluidRegistryEvent.Post());
        MinecraftForge.EVENT_BUS.post(new BucketMaterialRegistryEvent.Pre());
        MinecraftForge.EVENT_BUS.post(new BucketMaterialRegistryEvent.Post());

        for (Fluid fluid : FluidHelper.generatedFluids)
        {
            FluidHelper.createBlockForFluidIfMissing(fluid, event);
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

        for(BucketHandler handler : BucketHandler.bucketHandlers)
        {
            handler.loadSettings(config);
        }

        proxy.postInit();
        config.save();
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        /* Load recipe handling for other mods */
        if (bucket != null)
        {
            //TODO add pam's harvest craft support
            if (Loader.isModLoaded("harvestcraft"))
            {
                //RecipeSorter.register(DOMAIN + ":woodenBucketFreshMilk", PamBucketRecipe.class, SHAPED, "after:minecraft:shaped");
                if (config.getBoolean("EnableRegisteringMilkBucket", "PamHarvestCraftSupport", true, "Registers the milk bucket to the ore dictionary to be used in Pam's Harvest Craft recipes"))
                {
                    if (FluidRegistry.getFluid("milk") != null)
                    {
                        Item itemFreshMilk = Item.REGISTRY.getObject(new ResourceLocation("harvestcraft:freshmilkItem"));
                        if (itemFreshMilk == null)
                        {
                            logger.error("Failed to find item harvestcraft:freshmilkItem");
                        }
                        else
                        {
                            event.getRegistry().register(new PamBucketRecipe(new ItemStack(itemFreshMilk, 4, 0), Fluids.MILK.fluid));
                        }
                    }
                }
                if (config.getBoolean("EnableRegisteringFreshWaterBucket", "PamHarvestCraftSupport", true, "Registers the water bucket to the ore dictionary to be used in Pam's Harvest Craft recipes"))
                {
                    Item itemFreshWater = Item.REGISTRY.getObject(new ResourceLocation("harvestcraft:freshwaterItem"));
                    if (itemFreshWater == null)
                    {
                        logger.error("Failed to find item harvestcraft:freshwaterItem");
                    }
                    else
                    {
                        event.getRegistry().register(new PamBucketRecipe(new ItemStack(itemFreshWater, 4, 0), FluidRegistry.WATER));
                    }
                }
            }

            if (Loader.isModLoaded("actuallyadditions"))
            {
                Item itemRice = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions:item_misc"));
                if (itemRice == null)
                {
                    logger.error("Failed to find items from Actual additions required to register slime ball recipe");
                }
                else
                {
                    event.getRegistry().register(new SlimeRiceBucketRecipe(itemRice));
                }
            }
        }
    }

    @Mod.EventHandler
    public void loadCompleted(FMLLoadCompleteEvent event)
    {
        //Save config
        BucketMaterialHandler.save(bucketConfig);
        bucketConfig.save();
    }
}
