package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.api.material.IBucketMaterialMimic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/13/19.
 */
public class BucketMaterialMimic extends BucketMaterial implements IBucketMaterialMimic
{
    private final ItemStack mimicStack;

    public BucketMaterialMimic(String localization, ItemStack mimicStack)
    {
        super(localization, null);
        this.mimicStack = mimicStack;
    }

    @Override
    public ItemStack getItemToMimic(@Nullable ItemStack bucketStack)
    {
        return mimicStack;
    }
}
