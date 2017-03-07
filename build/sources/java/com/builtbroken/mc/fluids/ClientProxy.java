package com.builtbroken.mc.fluids;

import com.builtbroken.mc.fluids.bucket.ItemRenderFluidBucket;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/7/2017.
 */
public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        if (FluidModule.bucket != null)
        {
            MinecraftForgeClient.registerItemRenderer(FluidModule.bucket, new ItemRenderFluidBucket());
        }
    }
}
