package com.builtbroken.mc.fluids.bucket.cap;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.api.cap.IBucketCap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/24/2019.
 */
public class BucketCapProvider implements ICapabilityProvider
{
    final IFluidHandlerItem fluid_cap;
    final IBucketCap bucket_cap;

    final ItemStack host;

    public BucketCapProvider(ItemStack host)
    {
        this.host = host;
        fluid_cap = new FluidCapabilityBucketWrapper(host);
        bucket_cap = new CapabilityBucket(host);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY || capability == FluidModule.BUCKET_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == FluidModule.BUCKET_CAPABILITY)
        {
            return (T) bucket_cap;
        }
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
        {
            return (T) fluid_cap;
        }
        return null;
    }
}
