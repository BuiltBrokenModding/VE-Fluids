/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.builtbroken.mc.fluids.bucket.cap;

import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability provider for fluid access to bucket. Mainly acts as a wrapper for old code to new code.
 */
public class FluidCapabilityBucketWrapper implements IFluidHandlerItem
{
    protected ItemStack container;

    public FluidCapabilityBucketWrapper(ItemStack container)
    {
        this.container = container;
    }

    @Nonnull
    @Override
    public ItemStack getContainer()
    {
        return container;
    }

    @Override
    public IFluidTankProperties[] getTankProperties()
    {
        FluidStack fluid = null;
        int cap = Fluid.BUCKET_VOLUME;
        Item item = container.getItem();
        if (item instanceof ItemFluidBucket)
        {
            fluid = ((ItemFluidBucket) item).getFluid(container);
            cap = ((ItemFluidBucket) item).getCapacity(container);
        }
        return new FluidTankProperties[]{new FluidTankProperties(fluid, cap)};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        Item item = container.getItem();
        if (item instanceof ItemFluidBucket)
        {
            return ((ItemFluidBucket) item).fill(container, resource, doFill);
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain)
    {
        Item item = container.getItem();
        if (item instanceof ItemFluidBucket && (((ItemFluidBucket) item).getFluid(container) == null || ((ItemFluidBucket) item).getFluid(container).getFluid() == resource.getFluid()))
        {
            return ((ItemFluidBucket) item).drain(container, resource.amount, doDrain);
        }
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        Item item = container.getItem();
        if (item instanceof ItemFluidBucket)
        {
            return ((ItemFluidBucket) item).drain(container, maxDrain, doDrain);
        }
        return null;
    }
}