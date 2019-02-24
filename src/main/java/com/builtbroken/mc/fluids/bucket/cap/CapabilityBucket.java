package com.builtbroken.mc.fluids.bucket.cap;

import com.builtbroken.mc.fluids.api.cap.IBucketCap;
import com.builtbroken.mc.fluids.api.material.IBucketMaterial;
import com.builtbroken.mc.fluids.bucket.BucketMaterialHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/24/2019.
 */
public class CapabilityBucket implements IBucketCap
{
    public static final String NBT_ORIGINAL_STACK = "original_stack";
    public final IBucketMaterial material;
    private ItemStack originalStack;

    public CapabilityBucket(ItemStack host)
    {
        this(BucketMaterialHandler.getMaterial(host.getItemDamage()));
    }

    public CapabilityBucket(IBucketMaterial material)
    {
        this.material = material;
    }

    @Override
    public IBucketMaterial getBucketMaterial()
    {
        return material;
    }

    @Override
    public ItemStack getOriginalStack(boolean copy)
    {
        return copy && originalStack != null ? originalStack.copy() : originalStack;
    }

    @Override
    public void setOriginalStack(ItemStack stack)
    {
        originalStack = stack.copy();
        if (originalStack != null)
        {
            originalStack.setCount(1);
        }
    }

    @Override
    public NBTTagCompound save(NBTTagCompound tag)
    {
        if (originalStack != null)
        {
            tag.setTag(NBT_ORIGINAL_STACK, originalStack.writeToNBT(new NBTTagCompound()));
        }
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        if (tag.hasKey(NBT_ORIGINAL_STACK))
        {
            originalStack = new ItemStack(tag.getCompoundTag(NBT_ORIGINAL_STACK));
        }
    }
}
