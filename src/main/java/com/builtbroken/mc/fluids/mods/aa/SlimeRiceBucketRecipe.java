package com.builtbroken.mc.fluids.mods.aa;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import net.minecraft.init.Items;
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
    public static int RICE_META = 9;
    public static int SLIME_META = 12;
    public static int SLIME_OUTPUT = 4;

    public Item rice;

    public SlimeRiceBucketRecipe(Item itemRice)
    {
        //TODO set group to match PAM's bucket group
        super(new ResourceLocation(FluidModule.DOMAIN, "actualAdditionsBucketRiceSlime"), new ItemStack(itemRice, SLIME_OUTPUT, SLIME_META),
                " R ",
                "RBR",
                " R ",
                'R', new ItemStack(itemRice, 1, RICE_META),
                'B', FluidModule.bucket.getBucket(FluidRegistry.WATER));
        this.rice = itemRice; //TODO ore-dictionary
        setRegistryName(new ResourceLocation(FluidModule.DOMAIN, "bucket.aa.conversion"));
    }

    @Override
    public boolean matches(InventoryCrafting grid, World world)
    {
        boolean bucket = false;
        int rice = 0;

        //Only works on 3x3 grid, this could be changed later
        if (grid.getSizeInventory() == 9 && grid.getWidth() == 3 && grid.getHeight() == 3)
        {
            //Bucket detection/check
            ItemStack stack = grid.getStackInSlot(4);
            if (stack != null && stack.getItem() instanceof ItemFluidBucket)
            {
                ItemFluidBucket item = (ItemFluidBucket) grid.getStackInSlot(4).getItem();
                FluidStack fluidStack = item.getFluid(stack);
                bucket = fluidStack != null && fluidStack.getFluid() == FluidRegistry.WATER;
            }

            //Manual detection
            for (int i = 0; i < 9; i++)
            {
                if (i != 4)
                {
                    stack = grid.getStackInSlot(i);
                    if(stack != null && stack.getItem() != Items.AIR)
                    {
                        if (stack.getItem() == this.rice)
                        {
                            rice += 1;
                        }
                        else
                        {
                            return false;
                        }
                    }
                }
            }
        }
        return bucket && rice == 4;
    }
}
