package com.builtbroken.mc.fluids.mods.aa;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Simple recipe to handle integration with Pam's Harvestcraft fresh milk buckets
 * Created by Dark on 8/24/2015.
 */
public class SlimeRiceBucketRecipe extends ShapedOreRecipe
{
    public SlimeRiceBucketRecipe(Item itemRice)
    {
        //TODO set group to match PAM's bucket group
        super(new ResourceLocation(FluidModule.DOMAIN, "actualAdditionsBucketRiceSlime"), new ItemStack(itemRice, 4, 12),
                " R ",
                "RBR",
                " R ",
                'R', new ItemStack(itemRice, 1, 9),
                'B', FluidModule.bucket);
        setRegistryName(new ResourceLocation(FluidModule.DOMAIN, "bucket.aa.conversion"));
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
                return fluidStack != null && fluidStack.getFluid() == FluidRegistry.WATER;
            }
            return super.matches(grid, world);
        }
        return false;
    }
}
