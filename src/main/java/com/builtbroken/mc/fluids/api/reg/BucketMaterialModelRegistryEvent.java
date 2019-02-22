package com.builtbroken.mc.fluids.api.reg;

import com.builtbroken.mc.fluids.bucket.BucketMaterial;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Called client side to allow changing the model used by the bucket.
 * <p>
 * This event is recycled to save memory. Do not cache or modify
 * values outside of the model resource location.
 */
@SideOnly(Side.CLIENT)
public class BucketMaterialModelRegistryEvent  extends BucketMaterialRegistryEvent
{
    /**
     * Material, do not change properties
     */
    public BucketMaterial material;

    /**
     * Model currently in use, change this if you want
     */
    public ModelResourceLocation modelResourceLocation;

    public BucketMaterialModelRegistryEvent(BucketMaterial material, ModelResourceLocation modelResourceLocation)
    {
        this.material = material;
        this.modelResourceLocation = modelResourceLocation;
    }
}
