package com.builtbroken.mc.fluids.client;

import com.builtbroken.mc.fluids.CommonProxy;
import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.fluid.FluidHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fluids.IFluidBlock;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/7/2017.
 */
public class ClientProxy extends CommonProxy
{
    private static final String FLUID_MODEL_PATH = FluidModule.DOMAIN + ":fluid";

    public void registerAllModels()
    {
        ModelLoaderRegistry.registerLoader(new BucketModelLoader(FluidModule.DOMAIN));

        final ModelResourceLocation location = new ModelResourceLocation(FluidModule.DOMAIN + ":ve_bucket", "inventory");
        ModelLoader.setCustomMeshDefinition(FluidModule.bucket, stack -> location);
        ModelBakery.registerItemVariants(FluidModule.bucket, location);

        FluidHelper.generatedFluidBlocks.forEach(ClientProxy::registerFluidModel);
    }

    private static void registerFluidModel(IFluidBlock fluidBlock)
    {
        if(fluidBlock != null)
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
