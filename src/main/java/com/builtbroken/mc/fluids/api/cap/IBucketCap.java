package com.builtbroken.mc.fluids.api.cap;

import com.builtbroken.mc.fluids.api.material.IBucketMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Capability instance for a bucket to provider access to
 * per instance save state and {@link com.builtbroken.mc.fluids.api.material.IBucketMaterial}
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 2/24/2019.
 */
public interface IBucketCap
{
    /**
     * Returns the orginal ItemStack of the bucket. This
     * will return null should the current bucket be the
     * original.
     * <p>
     * This is only used in rare cases where a bucket
     * is mimicking another item. See {@link com.builtbroken.mc.fluids.api.material.IBucketMaterialMimic}
     *
     * @param copy - make a copy of the stack instead of returning original
     * @return stack or null to use current stack
     */
    default ItemStack getOriginalStack(boolean copy)
    {
        return null;
    }

    /**
     * Called to store the original itemstack. Only
     * used by {@link #getBucketMaterial()} that are
     * {@link com.builtbroken.mc.fluids.api.material.IBucketMaterialMimic}
     *
     * @param stack - stack to set
     */
    default void setOriginalStack(ItemStack stack)
    {

    }

    /**
     * Material of this bucket
     *
     * @return material
     */
    IBucketMaterial getBucketMaterial();

    default NBTTagCompound save(NBTTagCompound tag)
    {
        return tag;
    }

    default void load(NBTTagCompound tag)
    {
    }
}
