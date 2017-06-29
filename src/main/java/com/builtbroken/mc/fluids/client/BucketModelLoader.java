package com.builtbroken.mc.fluids.client;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.io.IOException;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/29/2017.
 */
public class BucketModelLoader implements ICustomModelLoader
{
    public final String domain;

    public BucketModelLoader(String domain)
    {
        this.domain = domain;
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        return modelLocation.getResourceDomain().equals(domain) && modelLocation.getResourcePath().contains("bucket");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws IOException
    {
        return ModelFluidBucket.MODEL;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {

    }
}
