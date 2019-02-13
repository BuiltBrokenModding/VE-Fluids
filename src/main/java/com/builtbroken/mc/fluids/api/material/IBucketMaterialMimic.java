package com.builtbroken.mc.fluids.api.material;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Bucket that mimics an existing item
 * Created by Dark(DarkGuardsman, Robert) on 2/13/19.
 */
public interface IBucketMaterialMimic extends IBucketMaterial
{

    /**
     * Item to mimic
     *
     * @param bucketStack - bucket stack
     * @return item to use as the bucket icon
     */
    ItemStack getItemToMimic(@Nullable ItemStack bucketStack);
}
