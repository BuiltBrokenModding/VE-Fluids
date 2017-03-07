package com.builtbroken.mc.fluids.mods.agricraft;

import com.InfinityRaider.AgriCraft.init.Blocks;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import com.builtbroken.mc.fluids.mods.BucketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/28/2016.
 */
public class AgricraftWaterPad extends BucketHandler
{
    @Override
    public ItemStack filledBucketClickBlock(EntityPlayer player, ItemStack itemstack, World world, int i, int j, int k, int meta)
    {
        if (itemstack.getItem() instanceof ItemFluidBucket)
        {
            FluidStack stack = ((ItemFluidBucket) itemstack.getItem()).getFluid(itemstack);
            if (stack != null && stack.getFluid() == FluidRegistry.WATER && stack.amount == FluidContainerRegistry.BUCKET_VOLUME)
            {
                if (!world.isRemote)
                {
                    if (!player.capabilities.isCreativeMode)
                    {
                        ItemStack emptyBucket = new ItemStack(itemstack.getItem(), 1, itemstack.getItemDamage());
                        if (!player.inventory.addItemStackToInventory(emptyBucket))
                        {
                            player.dropPlayerItemWithRandomChoice(emptyBucket, false);
                        }
                        --player.getCurrentEquippedItem().stackSize;
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    world.setBlock(i, j, k, Blocks.blockWaterPadFull, 7, 3);
                }
            }
        }
        return itemstack;
    }
}
