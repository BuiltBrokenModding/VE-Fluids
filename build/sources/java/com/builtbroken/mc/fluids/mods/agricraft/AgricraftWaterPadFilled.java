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
public class AgricraftWaterPadFilled extends BucketHandler
{
    @Override
    public ItemStack emptyBucketClickBlock(EntityPlayer player, ItemStack itemstack, World world, int i, int j, int k, int meta)
    {
        if (itemstack.getItem() instanceof ItemFluidBucket)
        {
            if (((ItemFluidBucket) itemstack.getItem()).isEmpty(itemstack))
            {
                if (!world.isRemote)
                {
                    if (!player.capabilities.isCreativeMode)
                    {
                        ItemStack bucket = new ItemStack(itemstack.getItem(), 1, itemstack.getItemDamage());
                        ((ItemFluidBucket) itemstack.getItem()).fill(bucket, new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), true);
                        if (!player.inventory.addItemStackToInventory(bucket))
                        {
                            player.dropPlayerItemWithRandomChoice(bucket, false);
                        }
                        --player.getCurrentEquippedItem().stackSize;
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    world.setBlock(i, j, k, Blocks.blockWaterPad, 0, 3);
                }
            }
        }
        return itemstack;
    }
}
