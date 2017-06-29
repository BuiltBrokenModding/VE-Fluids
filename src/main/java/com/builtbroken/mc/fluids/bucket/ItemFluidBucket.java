package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.mods.BucketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Improved version of the vanilla bucket that can accept any fluid type. This
 * version uses a wooden texture for the bucket body.
 *
 * @author Dark
 * @version 7/25/2015.
 */
public class ItemFluidBucket extends Item implements IFluidContainerItem
{
    //TODO rename to fluid.molten
    public static String[] supportedFluidTextures = new String[]{"milk", "blood", "slime.blue", "fuel", "aluminum.molten", "glue", "alubrass.molten", "alumite.molten", "angmallen.molten", "ardite.molten", "bronze.molten", "cobalt.molten", "copper.molten", "electrum.molten", "emerald.molten", "ender.molten", "enderium.molten", "glass.molten", "gold.molten", "invar.molten", "iron.molten", "lead.molten", "lumium.molten", "manyullyn.molten", "mithril.molten", "nickel.molten", "obsidian.molten", "pigiron.molten", "shiny.molten", "signalum.molten", "silver.molten", "steel.molten", "tin.molten", "oil", "redplasma"};

    public ItemFluidBucket(String name)
    {
        this.maxStackSize = 1;
        this.setRegistryName(new ResourceLocation(FluidModule.DOMAIN, "veBucket"));
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b)
    {
        if (!isEmpty(stack))
        {
            list.add(StatCollector.translateToLocal(getUnlocalizedName() + ".fluid.name") + ": " + getFluid(stack).getLocalizedName());
            list.add(StatCollector.translateToLocal(getUnlocalizedName() + ".fluid.amount.name") + ": " + getFluid(stack).amount + "mb");
        }
        else if (player.capabilities.isCreativeMode)
        {
            list.add("\u00a7c" + StatCollector.translateToLocal(getUnlocalizedName() + ".creative.void"));
        }
    }

    @SubscribeEvent
    public void onRightClickEvent(PlayerInteractEvent event)
    {
        if (!event.world.isRemote && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer.getCurrentEquippedItem() != null && event.entityPlayer.getCurrentEquippedItem().getItem() == this)
        {
            TileEntity tile = event.world.getTileEntity(event.pos);

            if (tile instanceof IFluidHandler)
            {
                boolean isBucketEmpty = this.isEmpty(event.entityPlayer.getCurrentEquippedItem());
                EnumFacing side = event.face;
                if (isBucketEmpty)
                {
                    FluidStack drainedFromTank = ((IFluidHandler) tile).drain(side, getCapacity(event.entityPlayer.getCurrentEquippedItem()), false);
                    if (drainedFromTank != null && drainedFromTank.getFluid() != null && ((IFluidHandler) tile).canDrain(side, drainedFromTank.getFluid()))
                    {
                        if (event.entityPlayer.capabilities.isCreativeMode)
                        {
                            ((IFluidHandler) tile).drain(side, FluidContainerRegistry.BUCKET_VOLUME, true);
                        }
                        else
                        {
                            ItemStack bucket = new ItemStack(this, 1, event.entityPlayer.getCurrentEquippedItem().getItemDamage());
                            int filledIntoBucket = fill(bucket, drainedFromTank, true);
                            if (filledIntoBucket > 0)
                            {
                                ((IFluidHandler) tile).drain(side, filledIntoBucket, true);
                                event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, consumeBucket(event.entityPlayer.getCurrentEquippedItem(), event.entityPlayer, bucket));
                                event.entityPlayer.inventoryContainer.detectAndSendChanges();
                            }
                        }
                    }
                }
                else
                {
                    FluidStack containedFluid = getFluid(event.entityPlayer.getCurrentEquippedItem());
                    if (((IFluidHandler) tile).canFill(side, containedFluid.getFluid()))
                    {
                        int filled = ((IFluidHandler) tile).fill(side, containedFluid, true);
                        if (!event.entityPlayer.capabilities.isCreativeMode)
                        {
                            drain(event.entityPlayer.getCurrentEquippedItem(), filled, true);
                            containedFluid = getFluid(event.entityPlayer.getCurrentEquippedItem());
                            if (containedFluid == null || containedFluid.amount == 0)
                            {
                                event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, consumeBucket(event.entityPlayer.getCurrentEquippedItem(), event.entityPlayer, new ItemStack(this, 1, event.entityPlayer.getCurrentEquippedItem().getItemDamage())));
                                event.entityPlayer.inventoryContainer.detectAndSendChanges();
                            }
                        }
                    }
                }
                if (event.isCancelable())
                {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        boolean isBucketEmpty = this.isEmpty(itemstack);
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, isBucketEmpty);

        if (movingobjectposition != null)
        {
            if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                TileEntity tile = world.getTileEntity(movingobjectposition.getBlockPos());

                if (tile instanceof IFluidHandler)
                {
                    return itemstack;
                }


                if (!world.isBlockModifiable(player, movingobjectposition.getBlockPos()))
                {
                    return itemstack;
                }

                //Fill bucket code
                if (isBucketEmpty)
                {
                    if (player.canPlayerEdit(movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack))
                    {
                        return pickupFluid(player, itemstack, world, movingobjectposition.getBlockPos());
                    }
                }
                else //Empty bucket code
                {
                    Block block = world.getBlockState(movingobjectposition.getBlockPos()).getBlock();
                    Material material = block.getMaterial();

                    if (player.canPlayerEdit(movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack))
                    {
                        if (BucketHandler.blockToHandler.containsKey(block))
                        {
                            BucketHandler handler = BucketHandler.blockToHandler.get(block);
                            if (handler != null)
                            {
                                return handler.filledBucketClickBlock(player, itemstack, world, movingobjectposition.getBlockPos());
                            }
                        }

                        if (!material.isSolid() && block.isReplaceable(world, movingobjectposition.getBlockPos()))
                        {
                            return placeFluid(player, itemstack, world, movingobjectposition.getBlockPos());
                        }
                    }

                    //Offset position based on side hit
                    BlockPos blockpos1 = movingobjectposition.getBlockPos().offset(movingobjectposition.sideHit);

                    if (player.canPlayerEdit(blockpos1, movingobjectposition.sideHit, itemstack))
                    {
                        if (BucketHandler.blockToHandler.containsKey(block))
                        {
                            BucketHandler handler = BucketHandler.blockToHandler.get(block);
                            if (handler != null)
                            {
                                return handler.placeFluidClickBlock(player, itemstack, world, blockpos1);
                            }
                        }
                        return placeFluid(player, itemstack, world, blockpos1);
                    }
                }
            }
        }
        return itemstack;

    }

    protected ItemStack consumeBucket(ItemStack currentStack, EntityPlayer player, ItemStack newStack)
    {
        //Creative mode we don't care about items
        if (player.capabilities.isCreativeMode)
        {
            return currentStack;
        }
        //If we only have one bucket consume and replace slot with new bucket
        else if (--currentStack.stackSize <= 0)
        {
            return newStack;
        }
        //If we have more than one bucket try to add the new one to the player's inventory
        else
        {
            if (!player.inventory.addItemStackToInventory(newStack))
            {
                player.dropPlayerItemWithRandomChoice(newStack, false);
            }

            return currentStack;
        }
    }

    public ItemStack pickupFluid(EntityPlayer player, ItemStack itemstack, World world, BlockPos pos)
    {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (BucketHandler.blockToHandler.containsKey(block))
        {
            BucketHandler handler = BucketHandler.blockToHandler.get(block);
            if (handler != null)
            {
                return handler.emptyBucketClickBlock(player, itemstack, world, pos);
            }
        }

        if (block == Blocks.water && ((Integer) blockState.getValue(BlockLiquid.LEVEL)).intValue() == 0)
        {
            if (world.setBlockToAir(pos))
            {
                ItemStack bucket = new ItemStack(this, 1, itemstack.getItemDamage());
                fill(bucket, new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), true);
                return this.consumeBucket(itemstack, player, bucket);
            }
        }
        else if (block == Blocks.lava && ((Integer) blockState.getValue(BlockLiquid.LEVEL)).intValue() == 0)
        {
            if (world.setBlockToAir(pos))
            {
                ItemStack bucket = new ItemStack(this, 1, itemstack.getItemDamage());
                fill(bucket, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), true);
                return this.consumeBucket(itemstack, player, bucket);
            }
        }
        else if (block instanceof IFluidBlock && ((IFluidBlock) block).canDrain(world, pos))
        {
            FluidStack drainedFluid = ((IFluidBlock) block).drain(world, pos, false);

            //TODO allow partial fills
            if (isValidFluidStack(drainedFluid))
            {
                ItemStack bucket = new ItemStack(this, 1, itemstack.getItemDamage());
                drainedFluid = ((IFluidBlock) block).drain(world, pos, true);

                if (isValidFluidStack(drainedFluid))
                {
                    fill(bucket, drainedFluid, true);
                    return this.consumeBucket(itemstack, player, bucket);
                }
                //Backup in case or error
                else if (world.getBlockState(pos).getBlock() != block)
                {
                    world.setBlockState(pos, blockState, 3);
                }
            }
        }
        return itemstack;
    }

    private boolean isValidFluidStack(FluidStack drainedFluid)
    {
        return drainedFluid != null && drainedFluid.getFluid() != null && drainedFluid.amount == FluidContainerRegistry.BUCKET_VOLUME;
    }

    /**
     * Attempts to place the liquid contained inside the bucket.
     */
    public ItemStack placeFluid(EntityPlayer player, ItemStack itemstack, World world, BlockPos pos)
    {
        Block block = world.getBlockState(pos).getBlock();
        //Material material = block.getMaterial();
        if (isFull(itemstack))
        {
            if (world.isAirBlock(pos) || block.isReplaceable(world, pos))
            {
                FluidStack stack = getFluid(itemstack);
                if (stack != null && stack.getFluid() != null && stack.getFluid().canBePlacedInWorld() && stack.getFluid().getBlock() != null)
                {
                    //TODO add support for oil and other fuel types to explode in the nether
                    if (world.provider.doesWaterVaporize() && stack.getFluid().getUnlocalizedName().contains("water"))
                    {
                        world.playSoundEffect((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F), "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                        for (int l = 0; l < 8; ++l)
                        {
                            world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, (double) pos.getX() + Math.random(), (double) pos.getY() + Math.random(), (double) pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
                        }
                        return consumeBucket(itemstack, player, new ItemStack(this, 1, itemstack.getItemDamage()));
                    }
                    else
                    {
                        if (!world.isRemote)
                        {
                            //world.func_147480_a(x, y, z, true);
                        }
                        if (stack.getFluid() == FluidRegistry.WATER)
                        {
                            world.setBlockState(pos, Blocks.flowing_water.getDefaultState());
                        }
                        else if (stack.getFluid() == FluidRegistry.LAVA)
                        {
                            world.setBlockState(pos, Blocks.flowing_lava.getDefaultState());
                        }
                        else
                        {
                            world.setBlockState(pos, stack.getFluid().getBlock().getDefaultState());
                        }
                        return consumeBucket(itemstack, player, new ItemStack(this, 1, itemstack.getItemDamage()));
                    }
                }
            }
        }
        else if (!world.isRemote)
        {
            player.addChatComponentMessage(new ChatComponentText(getUnlocalizedName() + ".volume.notEnoughForFullBlock"));
        }
        return itemstack;
    }

    /**
     * Helper method to check if the bucket is empty
     *
     * @param container - bucket
     * @return true if it is empty
     */
    public boolean isEmpty(ItemStack container)
    {
        return getFluid(container) == null;
    }

    /**
     * Helper method to check if the bucket is full
     *
     * @param container - bucket
     * @return true if it is full
     */
    public boolean isFull(ItemStack container)
    {
        FluidStack stack = getFluid(container);
        if (stack != null)
        {
            return stack.amount == getCapacity(container);
        }
        return false;
    }

    /* IFluidContainerItem */
    @Override
    public FluidStack getFluid(ItemStack container)
    {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Fluid"))
        {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.getTagCompound().getCompoundTag("Fluid"));
    }

    @Override
    public int getCapacity(ItemStack container)
    {
        return FluidContainerRegistry.BUCKET_VOLUME;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill)
    {
        if (resource != null)
        {
            if (!doFill)
            {
                if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Fluid"))
                {
                    return Math.min(getCapacity(container), resource.amount);
                }

                FluidStack stack = FluidStack.loadFluidStackFromNBT(container.getTagCompound().getCompoundTag("Fluid"));

                if (stack == null)
                {
                    return Math.min(getCapacity(container), resource.amount);
                }

                if (!stack.isFluidEqual(resource))
                {
                    return 0;
                }

                return Math.min(getCapacity(container) - stack.amount, resource.amount);
            }

            if (container.getTagCompound() == null)
            {
                container.setTagCompound(new NBTTagCompound());
            }

            if (!container.getTagCompound().hasKey("Fluid"))
            {
                NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

                if (getCapacity(container) < resource.amount)
                {
                    fluidTag.setInteger("Amount", getCapacity(container));
                    container.getTagCompound().setTag("Fluid", fluidTag);
                    return getCapacity(container);
                }

                container.getTagCompound().setTag("Fluid", fluidTag);
                return resource.amount;
            }
            else
            {

                NBTTagCompound fluidTag = container.getTagCompound().getCompoundTag("Fluid");
                FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);

                if (!stack.isFluidEqual(resource))
                {
                    return 0;
                }

                int filled = getCapacity(container) - stack.amount;
                if (resource.amount < filled)
                {
                    stack.amount += resource.amount;
                    filled = resource.amount;
                }
                else
                {
                    stack.amount = getCapacity(container);
                }

                container.getTagCompound().setTag("Fluid", stack.writeToNBT(fluidTag));
                return filled;
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
    {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Fluid"))
        {
            return null;
        }

        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.getTagCompound().getCompoundTag("Fluid"));
        if (stack == null)
        {
            return null;
        }

        int currentAmount = stack.amount;
        stack.amount = Math.min(stack.amount, maxDrain);
        if (doDrain)
        {
            if (currentAmount == stack.amount)
            {
                container.getTagCompound().removeTag("Fluid");

                if (container.getTagCompound().hasNoTags())
                {
                    container.setTagCompound(null);
                }
                return stack;
            }

            NBTTagCompound fluidTag = container.getTagCompound().getCompoundTag("Fluid");
            fluidTag.setInteger("Amount", currentAmount - stack.amount);
            container.getTagCompound().setTag("Fluid", fluidTag);
        }
        return stack;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return isEmpty(stack) ? Items.bucket.getItemStackLimit() : 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getColorFromItemStack(ItemStack stack, int pass)
    {
        if (!isEmpty(stack) && pass == 1)
        {
            return getFluid(stack).getFluid().getColor();
        }
        return 16777215;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held)
    {
        FluidStack fluid = getFluid(stack);
        if (fluid != null && fluid.getFluid() != null)
        {
            //Mod support
            if (BucketHandler.fluidToHandler.containsKey(fluid))
            {
                BucketHandler handler = BucketHandler.fluidToHandler.get(fluid);
                if (handler != null)
                {
                    if (handler.onUpdate(stack, world, entity, slot, held))
                    {
                        return;
                    }
                }
            }
            if (world.getWorldTime() % 5 == 0)
            {
                final boolean moltenFluid = fluid.getFluid().getTemperature(fluid) > 400;

                BucketMaterial material = BucketMaterialHandler.getMaterial(stack.getItemDamage());
                if (material != null)
                {
                    //Handles burning the player
                    if (material.preventHotFluidUsage && moltenFluid)
                    {
                        //Default 26% chance to be caught on fire
                        if (material.burnEntityWithHotFluid && entity instanceof EntityLivingBase && world.rand.nextFloat() < ((float) fluid.getFluid().getTemperature(fluid) / 1500f))
                        {
                            EntityLivingBase living = (EntityLivingBase) entity;
                            if (!living.isImmuneToFire())
                            {
                                living.setFire(1 + world.rand.nextInt(15));
                            }
                            //TODO implement direct damage based on armor, or leave that to ItHurtsToDie?
                        }
                        if (material.damageBucketWithHotFluid && world.rand.nextFloat() < ((float) fluid.getFluid().getTemperature(fluid) / 1500f))
                        {
                            //TODO play sound effect of items burning
                            BucketMaterial damaged = material.getDamagedBucket(stack);
                            if (damaged != null)
                            {
                                stack.setItemDamage(damaged.metaValue);
                            }
                        }
                    }

                    //Handles leaking of buckets
                    if (material.enableFluidLeaking && fluid.getFluid().getViscosity(fluid) < material.viscosityToIgnoreLeaking)
                    {
                        if (world.rand.nextFloat() < material.chanceToLeak)
                        {
                            //Event handling for bucket leaking
                            if (BucketHandler.fluidToHandler.containsKey(fluid))
                            {
                                BucketHandler handler = BucketHandler.fluidToHandler.get(fluid);
                                if (handler != null)
                                {
                                    if (handler.onBucketLeaked(stack, world, entity, slot, held))
                                    {
                                        return;
                                    }
                                }
                            }

                            //Remove fluid from bucket
                            int drain = material.amountToLeak <= 1 ? 1 : world.rand.nextInt(material.amountToLeak);
                            drain(stack, drain, true);
                            //TODO play dripping sound

                            //Handles setting the world on fire if bucket leaks
                            if (material.allowLeakToCauseFires && moltenFluid && world.rand.nextFloat() < material.leakFireChance)
                            {
                                for (int i = 0; i < 7; i++)
                                {
                                    int x = (int) entity.posX;
                                    int y = (int) entity.posY + 2 - i;
                                    int z = (int) entity.posZ;

                                    BlockPos pos = new BlockPos(x, y, z);
                                    Block block = world.getBlockState(pos).getBlock();
                                    Block block2 = world.getBlockState(pos.up()).getBlock();
                                    if (block.isSideSolid(world, pos, EnumFacing.UP))
                                    {
                                        if (block2.isAir(world, pos.up()) || block2.isReplaceable(world, pos.up()))
                                        {
                                            world.setBlockState(pos.up(), Blocks.fire.getDefaultState());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem)
    {
        FluidStack fluid = getFluid(entityItem.getEntityItem());
        if (fluid != null && fluid.getFluid() != null)
        {
            //Mod support
            if (BucketHandler.fluidToHandler.containsKey(fluid))
            {
                BucketHandler handler = BucketHandler.fluidToHandler.get(fluid);
                if (handler != null)
                {
                    if (handler.onEntityItemUpdate(entityItem))
                    {
                        return true;
                    }
                }
            }

            //Base hot fluid support
            if (entityItem.worldObj.getWorldTime() % 5 == 0)
            {
                BucketMaterial material = BucketMaterialHandler.getMaterial(entityItem.getEntityItem().getItemDamage());
                if (material != null)
                {
                    if (material.preventHotFluidUsage && fluid.getFluid().getTemperature(fluid) > 400)
                    {
                        if (material.damageBucketWithHotFluid && entityItem.worldObj.rand.nextFloat() < ((float) fluid.getFluid().getTemperature(fluid) / 1500f))
                        {
                            //TODO play sound effect of items burning
                            BucketMaterial damaged = material.getDamagedBucket(entityItem.getEntityItem());
                            if (damaged != null)
                            {
                                entityItem.getEntityItem().setItemDamage(damaged.metaValue);
                            }
                        }
                        //TODO chance to catch area on fire around it
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity)
    {
        if (entity != null)
        {
            Class<? extends Entity> clazz = entity.getClass();
            if (BucketHandler.entityToHandler.containsKey(clazz) && BucketHandler.entityToHandler.get(clazz).rightClickEntity(stack, player, entity))
            {
                return true;
            }

            if (entity instanceof EntityCow && isEmpty(stack))
            {
                if (player.worldObj.isRemote)
                {
                    return true;
                }

                Fluid fluid = FluidRegistry.getFluid("milk");
                if (fluid != null)
                {
                    ItemStack newBucket = new ItemStack(this, 1, stack.getItemDamage());
                    fill(newBucket, new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), true);
                    stack.stackSize--;
                    player.inventory.addItemStackToInventory(newBucket);
                    player.inventoryContainer.detectAndSendChanges();
                }
                else
                {
                    ((EntityCow) entity).playLivingSound();
                    player.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocal(getUnlocalizedName() + ".error.fluid.milk.notRegistered")));
                }
                return true;
            }
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            list.add(new ItemStack(item, 1, material.metaValue));
        }

        ItemStack waterBucket = new ItemStack(item);
        fill(waterBucket, new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), true);
        list.add(waterBucket);

        for (String string : supportedFluidTextures)
        {
            if (FluidRegistry.getFluid(string) != null)
            {
                ItemStack milkBucket = new ItemStack(item);
                fill(milkBucket, new FluidStack(FluidRegistry.getFluid(string), FluidContainerRegistry.BUCKET_VOLUME), true);
                list.add(milkBucket);
            }
        }

        for (BucketHandler handler : BucketHandler.bucketHandlers)
        {
            handler.getSubItems(item, list);
        }
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemstack)
    {
        if (isEmpty(itemstack))
        {
            return null;
        }
        return new ItemStack(FluidModule.bucket, 1, itemstack.getItemDamage());
    }

    //@Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
    {
        return isEmpty(stack);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack)
    {
        return getFluid(stack) != null;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        BucketMaterial material = BucketMaterialHandler.getMaterial(stack.getItemDamage());
        if (material != null)
        {
            return material.getUnlocalizedName(stack);
        }
        return super.getUnlocalizedName();
    }
}
