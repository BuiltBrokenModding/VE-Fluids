package com.builtbroken.mc.fluids.client;

import com.builtbroken.mc.fluids.CommonProxy;
import com.builtbroken.mc.fluids.FluidModule;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/7/2017.
 */
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();

        ModelLoaderRegistry.registerLoader(new BucketModelLoader(FluidModule.DOMAIN));

        final ModelResourceLocation location = new ModelResourceLocation(FluidModule.DOMAIN + ":ve_bucket", "inventory");
        ModelLoader.setCustomMeshDefinition(FluidModule.bucket, new ItemMeshDefinition()
        {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return location;
            }
        });
        ModelBakery.registerItemVariants(FluidModule.bucket, location);
    }
}
