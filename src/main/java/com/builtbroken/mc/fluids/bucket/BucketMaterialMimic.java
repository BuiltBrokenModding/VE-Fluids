package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.api.cap.IBucketCap;
import com.builtbroken.mc.fluids.api.material.IBucketMaterial;
import com.builtbroken.mc.fluids.api.material.IBucketMaterialMimic;
import net.minecraft.item.ItemStack;

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
    public ItemStack getNewBucketStack(ItemStack heldItemStack)
    {
        final ItemStack stack = new ItemStack(FluidModule.bucket, 1, metaValue);
        if (heldItemStack != null && stack.hasCapability(FluidModule.BUCKET_CAPABILITY, null))
        {
            final IBucketCap cap = stack.getCapability(FluidModule.BUCKET_CAPABILITY, null);
            if (cap != null)
            {
                IBucketMaterial material = cap.getBucketMaterial();
                if (material instanceof IBucketMaterialMimic && ((IBucketMaterialMimic) material).shouldSaveOriginalItemStack(stack))
                {
                    ((IBucketMaterialMimic) material).setOriginalStack(stack, heldItemStack, cap);
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack getItemToMimic(@Nullable ItemStack bucketStack)
    {
        if (bucketStack != null && !bucketStack.isEmpty())
        {
            ItemStack original = getOriginalStack(bucketStack);
            if (original != null && !original.isEmpty())
            {
                return original;
            }
        }
        return mimicStack;
    }

    @Override
    public ItemStack getOriginalStack(ItemStack heldItemStack)
    {
        return getOriginalStack(heldItemStack, true);
    }

    public ItemStack getOriginalStack(ItemStack bucketStack, boolean copy)
    {
        if (bucketStack.hasCapability(FluidModule.BUCKET_CAPABILITY, null))
        {
            IBucketCap cap = bucketStack.getCapability(FluidModule.BUCKET_CAPABILITY, null);
            if (cap != null)
            {
                return cap.getOriginalStack(copy);
            }
        }
        return null;
    }
}
