package com.builtbroken.mc.fluids.mods.pam;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Simple recipe to handle integration with Pam's Harvestcraft fresh milk buckets
 * Created by Dark on 8/24/2015.
 */
public class PamBucketRecipe extends ShapedOreRecipe
{
    Fluid fluid;

    public PamBucketRecipe(ItemStack pamBucket, Fluid fluid)
    {
        //TODO set group to match PAM's bucket group
        super(new ResourceLocation(FluidModule.DOMAIN, "pamBucket." + fluid.getName()), pamBucket, "   ", " B ", "   ", 'B', FluidModule.bucket);
        setRegistryName(new ResourceLocation(FluidModule.DOMAIN, "bucket.pam.conversion"));
        this.fluid = fluid;
    }

    @Override
    public boolean matches(InventoryCrafting grid, World world)
    {
        if (grid.getSizeInventory() == 9)
        {
            ItemStack stack = grid.getStackInSlot(4);
            if (stack != null && stack.getItem() instanceof ItemFluidBucket)
            {
                ItemFluidBucket item = (ItemFluidBucket) grid.getStackInSlot(4).getItem();
                FluidStack fluidStack = item.getFluid(stack);
                return fluidStack != null && fluidStack.getFluid() == fluid;
            }
        }
        return false;
    }
}
