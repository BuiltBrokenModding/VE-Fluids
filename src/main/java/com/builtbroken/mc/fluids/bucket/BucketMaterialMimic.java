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
    public ItemStack getEmptyBucket(ItemStack heldItemStack)
    {
        final ItemStack stack = getOriginalStack(heldItemStack, true);
        if (stack != null && !stack.isEmpty())
        {
            return stack;
        }
        return getNewBucketStack(heldItemStack);
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        ItemStack mimicStack = getItemToMimic(stack);
        if (mimicStack != null && !mimicStack.isEmpty())
        {
            return mimicStack.getTranslationKey();
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public ItemStack getNewBucketStack(ItemStack heldItemStack)
    {
        final ItemStack bucketStack = new ItemStack(FluidModule.bucket, 1, metaValue);
        if (bucketStack.hasCapability(FluidModule.BUCKET_CAPABILITY, null))
        {
            final IBucketCap cap = bucketStack.getCapability(FluidModule.BUCKET_CAPABILITY, null);
            if (cap != null)
            {
                IBucketMaterial material = cap.getBucketMaterial();
                if (material instanceof IBucketMaterialMimic && ((IBucketMaterialMimic) material).shouldSaveOriginalItemStack(bucketStack))
                {
                    ((IBucketMaterialMimic) material).setOriginalStack(getOriginalItemToStore(heldItemStack), bucketStack, cap);
                }
            }
        }
        return bucketStack;
    }

    protected ItemStack getOriginalItemToStore(ItemStack current)
    {
        if (current != null && !current.isEmpty())
        {
            if (current.getItem() == FluidModule.bucket)
            {
                BucketMaterial bucketMaterial = BucketMaterialHandler.getMaterial(current.getItemDamage());
                if (bucketMaterial instanceof BucketMaterialMimic)
                {
                    return ((BucketMaterialMimic) bucketMaterial).getOriginalStack(current, true);
                }
            }
            return current.copy();
        }
        return mimicStack.copy();
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
        return copy ? mimicStack.copy() : mimicStack;
    }
}
