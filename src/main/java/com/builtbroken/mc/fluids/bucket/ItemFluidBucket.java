package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.mods.BucketHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.*;

import java.util.HashMap;
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
    @SideOnly(Side.CLIENT)
    public static IIcon fluidTextureWater;

    @SideOnly(Side.CLIENT)
    public static IIcon fluidTextureWhite;

    @SideOnly(Side.CLIENT)
    public static IIcon fluidTextureLava;

    @SideOnly(Side.CLIENT)
    public static IIcon blankTexture;

    public static HashMap<String, IIcon> fluidToIconMap = new HashMap();

    //TODO rename to fluid.molten
    public static String[] supportedFluidTextures = new String[]{"milk", "blood", "slime.blue", "fuel", "aluminum.molten", "glue", "alubrass.molten", "alumite.molten", "angmallen.molten", "ardite.molten", "bronze.molten", "cobalt.molten", "copper.molten", "electrum.molten", "emerald.molten", "ender.molten", "enderium.molten", "glass.molten", "gold.molten", "invar.molten", "iron.molten", "lead.molten", "lumium.molten", "manyullyn.molten", "mithril.molten", "nickel.molten", "obsidian.molten", "pigiron.molten", "shiny.molten", "signalum.molten", "silver.molten", "steel.molten", "tin.molten", "oil", "redplasma"};

    public ItemFluidBucket(String name)
    {
        this.maxStackSize = 1;
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
            TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);

            if (tile instanceof IFluidHandler)
            {
                boolean isBucketEmpty = this.isEmpty(event.entityPlayer.getCurrentEquippedItem());
                ForgeDirection side = ForgeDirection.getOrientation(event.face);
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

    @Override //Called first
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        return false; //return true will result in next 2 methods not getting called
    }

    @Override //Called second
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        return false; //return true will result in next method not getting called
    }

    @Override //Called third
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        boolean isBucketEmpty = this.isEmpty(itemstack);
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, isBucketEmpty);

        if (movingobjectposition != null)
        {
            if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                int i = movingobjectposition.blockX;
                int j = movingobjectposition.blockY;
                int k = movingobjectposition.blockZ;

                TileEntity tile = world.getTileEntity(i, j, k);

                if (tile instanceof IFluidHandler)
                {
                    return itemstack;
                }


                if (!world.canMineBlock(player, i, j, k))
                {
                    return itemstack;
                }

                //Fill bucket code
                if (isBucketEmpty)
                {
                    if (player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemstack))
                    {
                        return pickupFluid(player, itemstack, world, i, j, k);
                    }
                }
                else //Empty bucket code
                {
                    Block block = world.getBlock(i, j, k);
                    Material material = block.getMaterial();

                    if (player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemstack))
                    {
                        if (BucketHandler.blockToHandler.containsKey(block))
                        {
                            BucketHandler handler = BucketHandler.blockToHandler.get(block);
                            if (handler != null)
                            {
                                return handler.filledBucketClickBlock(player, itemstack, world, i, j, k, world.getBlockMetadata(i, j, k));
                            }
                        }

                        if (!material.isSolid() && block.isReplaceable(world, i, j, k))
                        {
                            return placeFluid(player, itemstack, world, i, j, k);
                        }
                    }

                    //Offset position based on side hit
                    if (movingobjectposition.sideHit == 0)
                    {
                        --j;
                    }

                    if (movingobjectposition.sideHit == 1)
                    {
                        ++j;
                    }

                    if (movingobjectposition.sideHit == 2)
                    {
                        --k;
                    }

                    if (movingobjectposition.sideHit == 3)
                    {
                        ++k;
                    }

                    if (movingobjectposition.sideHit == 4)
                    {
                        --i;
                    }

                    if (movingobjectposition.sideHit == 5)
                    {
                        ++i;
                    }

                    if (player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemstack))
                    {
                        if (BucketHandler.blockToHandler.containsKey(block))
                        {
                            BucketHandler handler = BucketHandler.blockToHandler.get(block);
                            if (handler != null)
                            {
                                return handler.placeFluidClickBlock(player, itemstack, world, i, j, k, world.getBlockMetadata(i, j, k));
                            }
                        }
                        return placeFluid(player, itemstack, world, i, j, k);
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

    /**
     * Called to pick up the fluid
     *
     * @param player
     * @param container
     * @param world
     * @param i
     * @param j
     * @param k
     * @return
     */
    public ItemStack pickupFluid(EntityPlayer player, ItemStack container, World world, int i, int j, int k)
    {
        FluidStack fluidOfBlock = null;

        final Block block = world.getBlock(i, j, k);
        final int blockMeta = world.getBlockMetadata(i, j, k);

        //Allow handler to override interaction
        if (BucketHandler.blockToHandler.containsKey(block))
        {
            BucketHandler handler = BucketHandler.blockToHandler.get(block);
            if (handler != null)
            {
                return handler.emptyBucketClickBlock(player, container, world, i, j, k, blockMeta);
            }
        }

        if (block == Blocks.water && blockMeta == 0 || block == Blocks.lava && blockMeta == 0)
        {
            //Create fluid
            if (block == Blocks.water)
            {
                fluidOfBlock = new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);
            }
            else
            {
                fluidOfBlock = new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
            }

            //Verify that we can pickup fluidOfBlock and that the block was removed
            if (isValidFluidStack(container, fluidOfBlock) && world.setBlockToAir(i, j, k))
            {
                //Generate new bucket
                ItemStack bucket = new ItemStack(this, 1, container.getItemDamage());

                //Fill bucket
                fill(bucket, fluidOfBlock, true);

                //Return bucket
                return this.consumeBucket(container, player, bucket);
            }
        }
        else if (block instanceof IFluidBlock && ((IFluidBlock) block).canDrain(world, i, j, k))
        {
            //Get fluidOfBlock from block
            fluidOfBlock = ((IFluidBlock) block).drain(world, i, j, k, false);

            //Check that the fluidOfBlock is valid
            if (isValidFluidStack(container, fluidOfBlock))
            {
                //Create new bucket
                ItemStack bucket = new ItemStack(this, 1, container.getItemDamage());

                //Drain block and get fluidOfBlock
                fluidOfBlock = ((IFluidBlock) block).drain(world, i, j, k, true);

                //Check that fluidOfBlock is valid, aka sanity check
                if (isValidFluidStack(container, fluidOfBlock))
                {
                    //Fill bucket
                    fill(bucket, fluidOfBlock, true);

                    //Return bucket
                    return this.consumeBucket(container, player, bucket);
                }
                //Reverse action as something went wrong
                else if (world.getBlock(i, j, k) != block)
                {
                    world.setBlock(i, j, k, block, blockMeta, 3);
                }
            }
        }

        return container;
    }

    /**
     * Used to check if the fluid stack is valid for the container
     *
     * @param container  - bucket or container that will be filled
     * @param fluidStack - stack being inserted into the bucket
     * @return true if the stack is valid
     */
    public boolean isValidFluidStack(ItemStack container, FluidStack fluidStack)
    {
        return fluidStack != null
                && fluidStack.getFluid() != null
                && fluidStack.amount == FluidContainerRegistry.BUCKET_VOLUME
                && fill(container, fluidStack, false) == fluidStack.amount;
    }

    /**
     * Attempts to place the liquid contained inside the bucket.
     */
    public ItemStack placeFluid(EntityPlayer player, ItemStack itemstack, World world, int x, int y, int z)
    {
        Block block = world.getBlock(x, y, z);
        //Material material = block.getMaterial();
        if (isFull(itemstack))
        {
            if (world.isAirBlock(x, y, z) || block.isReplaceable(world, x, y, z))
            {
                FluidStack stack = getFluid(itemstack);
                if (stack != null && stack.getFluid() != null && stack.getFluid().canBePlacedInWorld() && stack.getFluid().getBlock() != null)
                {
                    //TODO add support for oil and other fuel types to explode in the nether
                    if (world.provider.isHellWorld && stack.getFluid().getUnlocalizedName().contains("water"))
                    {
                        world.playSoundEffect((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                        for (int l = 0; l < 8; ++l)
                        {
                            world.spawnParticle("largesmoke", (double) x + Math.random(), (double) y + Math.random(), (double) z + Math.random(), 0.0D, 0.0D, 0.0D);
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
                            world.setBlock(x, y, z, Blocks.flowing_water);
                        }
                        else if (stack.getFluid() == FluidRegistry.LAVA)
                        {
                            world.setBlock(x, y, z, Blocks.flowing_lava);
                        }
                        else
                        {
                            world.setBlock(x, y, z, stack.getFluid().getBlock());
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
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid"))
        {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("Fluid"));
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
            BucketMaterial material = BucketMaterialHandler.getMaterial(container);

            //Check that the material supports the fluid
            if (material == null || !material.supportsFluid(container, resource))
            {
                return 0;
            }

            if (!doFill)
            {
                if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid"))
                {
                    return Math.min(getCapacity(container), resource.amount);
                }

                FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("Fluid"));

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

            if (container.stackTagCompound == null)
            {
                container.stackTagCompound = new NBTTagCompound();
            }

            if (!container.stackTagCompound.hasKey("Fluid"))
            {
                NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

                if (getCapacity(container) < resource.amount)
                {
                    fluidTag.setInteger("Amount", getCapacity(container));
                    container.stackTagCompound.setTag("Fluid", fluidTag);
                    return getCapacity(container);
                }

                container.stackTagCompound.setTag("Fluid", fluidTag);
                return resource.amount;
            }
            else
            {

                NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("Fluid");
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

                container.stackTagCompound.setTag("Fluid", stack.writeToNBT(fluidTag));
                return filled;
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
    {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid"))
        {
            return null;
        }

        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("Fluid"));
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
                container.stackTagCompound.removeTag("Fluid");

                if (container.stackTagCompound.hasNoTags())
                {
                    container.stackTagCompound = null;
                }
                return stack;
            }

            NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("Fluid");
            fluidTag.setInteger("Amount", currentAmount - stack.amount);
            container.stackTagCompound.setTag("Fluid", fluidTag);
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

                                    Block block = world.getBlock(x, y, z);
                                    Block block2 = world.getBlock(x, y + 1, z);
                                    if (block.isSideSolid(world, x, y, z, ForgeDirection.UP))
                                    {
                                        if (block2.isAir(world, x, y + 1, z) || block2.isReplaceable(world, x, y + 1, z))
                                        {
                                            world.setBlock(x, y + 1, z, Blocks.air);
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
        //Add empty buckets
        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            list.add(new ItemStack(item, 1, material.metaValue));
        }

        //Add water bucket, separated from loop due to using default texture
        ItemStack waterBucket = getBucketForFluid(new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME));
        if (waterBucket != null)
        {
            list.add(waterBucket);
        }

        //Loop over supported fluid textures TODO replace when render code is switched over to use fluid texture
        for (String fluid_id : supportedFluidTextures)
        {
            //Check to make sure the fluid exists
            if (FluidRegistry.getFluid(fluid_id) != null)
            {
                //Create fluid
                FluidStack fluid = new FluidStack(FluidRegistry.getFluid(fluid_id), FluidContainerRegistry.BUCKET_VOLUME);

                //Get bucket
                ItemStack bucket = getBucketForFluid(fluid);

                //If null we have no valid bucket for the fluid
                if (bucket != null)
                {
                    list.add(bucket);
                }
            }
        }

        //Allow handlers to add buckets to tab
        for (BucketHandler handler : BucketHandler.bucketHandlers)
        {
            handler.getSubItems(item, list);
        }
    }

    //Attempts to locate a bucket that supports the fluid, prevents empty buckets from showing in creative tab
    private ItemStack getBucketForFluid(FluidStack stack)
    {
        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            ItemStack bucket = new ItemStack(this, 1, material.metaValue);

            int fill = fill(bucket, stack, true);

            if (fill > 0)
            {
                return bucket;
            }
        }
        return null;
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

    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
    {
        return isEmpty(stack);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack)
    {
        return getFluid(stack) != null;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg)
    {
        //Bucket textures
        itemIcon = reg.registerIcon(FluidModule.DOMAIN + ":bucket");

        //Fluid overlay defaults
        fluidTextureWater = reg.registerIcon(FluidModule.DOMAIN + ":bucket.fluid");
        fluidTextureWhite = reg.registerIcon(FluidModule.DOMAIN + ":bucket.fluid2");
        fluidTextureLava = reg.registerIcon(FluidModule.DOMAIN + ":bucket.lava");
        //Fluid overlay blank
        blankTexture = reg.registerIcon(FluidModule.DOMAIN + ":blank");

        //Supported fluids
        for (String string : supportedFluidTextures)
        {
            fluidToIconMap.put(string, reg.registerIcon(FluidModule.DOMAIN + ":bucket." + string));
        }

        //Register defaults
        fluidToIconMap.put("water", fluidTextureWater);
        fluidToIconMap.put("lava", fluidTextureLava);

        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            material.registerIcons(reg);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        final BucketMaterial material = BucketMaterialHandler.getMaterial(stack.getItemDamage());
        if (pass == 0)
        {
            if (material != null)
            {
                IIcon icon = material.getBucketIcon(stack);
                if (icon != null)
                {
                    return icon;
                }
            }
        }
        else if (pass == 1)
        {
            if (isEmpty(stack))
            {
                return blankTexture;
            }
            else
            {
                final Fluid fluid = getFluid(stack).getFluid();
                if (fluid != null) //shouldn't happen
                {
                    //Get icon from bucket first
                    if (material != null)
                    {
                        IIcon icon = material.getFluidIcon(stack, fluid);
                        if (icon != null)
                        {
                            return icon;
                        }
                    }

                    //Use built in icons if material doesn't have one
                    //Icon per material
                    if (fluidToIconMap.containsKey(fluid.getName()))
                    {
                        return fluidToIconMap.get(getFluid(stack).getFluid().getName());
                    }
                    //grey scale icon
                    else if (fluid.getColor() != 0xFFFFFF)
                    {
                        return fluidTextureWhite;
                    }
                    //Lava icon
                    else if (getFluid(stack).getFluid().getTemperature() > 600)
                    {
                        return fluidTextureLava;
                    }
                    //Water icon
                    else
                    {
                        return fluidTextureWater;
                    }
                }
                return blankTexture;
            }
        }
        return super.getIcon(stack, pass);
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

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }
}
