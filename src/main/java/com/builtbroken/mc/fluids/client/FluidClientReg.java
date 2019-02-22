package com.builtbroken.mc.fluids.client;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.api.reg.BucketMaterialModelRegistryEvent;
import com.builtbroken.mc.fluids.bucket.BucketMaterial;
import com.builtbroken.mc.fluids.bucket.BucketMaterialHandler;
import com.builtbroken.mc.fluids.fluid.FluidHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/12/19.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = FluidModule.DOMAIN, value = Side.CLIENT)
public class FluidClientReg
{

    private static final String FLUID_MODEL_PATH = FluidModule.DOMAIN + ":fluid";
    private static final ModelResourceLocation DEFAULT_MODEL = new ModelResourceLocation(FluidModule.DOMAIN + ":ve_bucket", "inventory");

    private static final HashMap<BucketMaterial, ModelResourceLocation> materialToModel = new HashMap();

    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event)
    {
        //Register model loader
        ModelLoaderRegistry.registerLoader(new BucketModelLoader(FluidModule.DOMAIN));


        final Set<ModelResourceLocation> models = new HashSet();
        models.add(DEFAULT_MODEL);

        //Collect bucket models, fire events to allow override
        BucketMaterialModelRegistryEvent modelEvent = new BucketMaterialModelRegistryEvent(null, null);
        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            modelEvent.modelResourceLocation = DEFAULT_MODEL;
            modelEvent.material = material;
            MinecraftForge.EVENT_BUS.post(modelEvent);

            if (modelEvent.material != material)
            {
                FluidModule.logger.error("WARNING: Something changed the material in the event from "
                        + material + " to " + modelEvent.material + " while loading models!!! " +
                        "This could cause logic issues and unexpected outcomes with the model system.");
            }
            if (modelEvent.modelResourceLocation == null)
            {
                FluidModule.logger.error("WARNING: Something set the model to null while firing model events for material " + material);
            }
            else if (modelEvent.modelResourceLocation != DEFAULT_MODEL)
            {
                models.add(modelEvent.modelResourceLocation);
            }

            materialToModel.put(material, modelEvent.modelResourceLocation);
        }

        //Register all the types
        ModelBakery.registerItemVariants(FluidModule.bucket, models.stream().toArray(ModelResourceLocation[]::new));

        //Map model to location
        ModelLoader.setCustomMeshDefinition(FluidModule.bucket, stack ->
        {
            final BucketMaterial material = BucketMaterialHandler.getMaterial(stack.getItemDamage());
            if (material != null)
            {
                final ModelResourceLocation location = materialToModel.get(material);
                if (location != null)
                {
                    return location;
                }
            }
            return DEFAULT_MODEL;
        });


        //Register fluid blocks
        FluidHelper.generatedFluidBlocks.forEach(FluidClientReg::registerFluidModel);
    }

    private static void registerFluidModel(IFluidBlock fluidBlock)
    {
        if (fluidBlock != null)
        {
            final Item item = Item.getItemFromBlock((Block) fluidBlock);

            ModelBakery.registerItemVariants(item);

            final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(FLUID_MODEL_PATH, fluidBlock.getFluid().getName());

            ModelLoader.setCustomMeshDefinition(item, stack -> modelResourceLocation);

            ModelLoader.setCustomStateMapper((Block) fluidBlock, new StateMapperBase()
            {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState p_178132_1_)
                {
                    return modelResourceLocation;
                }
            });
        }
    }
}
