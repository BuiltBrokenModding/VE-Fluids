package com.builtbroken.mc.fluids.api.material;

import com.builtbroken.mc.fluids.api.cap.IBucketCap;
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

    /**
     * Should the {@link com.builtbroken.mc.fluids.api.cap.IBucketCap} store
     * the original stack that was used to make the mimic bucket.
     *
     * @param stack - original stack
     * @return true to save
     */
    default boolean shouldSaveOriginalItemStack(ItemStack stack)
    {
        return true;
    }

    /**
     * Called to set the original bucket stack, just wrappers
     * to the capability but provides a useful hook to change
     * the original stack.
     *
     * @param originalStack - original itemstack used to make the bucket
     * @param bucketStack   - this bucket
     * @param cap           - current bucket's capability
     */
    default void setOriginalStack(ItemStack originalStack, ItemStack bucketStack, IBucketCap cap)
    {
        cap.setOriginalStack(originalStack);
    }

    /**
     * Called to get the original bucket stack, just wrappers
     * to the capability but provides a useful hook to change
     * the original stack.
     *
     * @param bucketStack - this bucket
     * @return original item used to make the bucket
     */
    default ItemStack getOriginalStack(ItemStack bucketStack)
    {
        return null;
    }
}
