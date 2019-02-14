package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.mods.BucketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Improved version of the vanilla bucket that can accept any fluid type. This
 * version uses a wooden texture for the bucket body.
 *
 * @author Dark
 * @version 7/25/2015.
 */
public class ItemFluidBucket extends Item
{
    public ItemFluidBucket(String name)
    {
        this.maxStackSize = 1;
        this.setRegistryName(new ResourceLocation(FluidModule.DOMAIN, "veBucket"));
        this.setTranslationKey(name);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setHasSubtypes(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        return new FluidCapabilityBucketWrapper(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn)
    {
        if (!isEmpty(stack))
        {
            list.add(I18n.format(getTranslationKey() + ".fluid.name") + ": " + getFluid(stack).getLocalizedName());
            list.add(I18n.format(getTranslationKey() + ".fluid.amount.name") + ": " + getFluid(stack).amount + "mb");
        }
        else if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.capabilities.isCreativeMode)
        {
            list.add("\u00a7c" + I18n.format(getTranslationKey() + ".creative.void"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        final ItemStack itemstack = player.getHeldItem(hand);
        final boolean isBucketEmpty = this.isEmpty(itemstack);
        final BucketMaterial bucketMaterial = BucketMaterialHandler.getMaterial(itemstack.getItemDamage());

        if (bucketMaterial != null)
        {
            RayTraceResult movingobjectposition = this.rayTrace(world, player, isBucketEmpty);

            if (movingobjectposition != null)
            {
                if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK)
                {
                    //Let fluid tiles handle there own logic
                    TileEntity tile = world.getTileEntity(movingobjectposition.getBlockPos());
                    if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, movingobjectposition.sideHit))
                    {
                        return new ActionResult(EnumActionResult.PASS, itemstack);
                    }

                    //Do not edit if blocked
                    if (!world.isBlockModifiable(player, movingobjectposition.getBlockPos()))
                    {
                        return new ActionResult(EnumActionResult.PASS, itemstack);
                    }

                    //Fill bucket code
                    if (isBucketEmpty)
                    {
                        if (player.canPlayerEdit(movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack))
                        {
                            return new ActionResult(EnumActionResult.SUCCESS, pickupFluid(player, itemstack, bucketMaterial, world, movingobjectposition.getBlockPos()));
                        }
                    }
                    else //Empty bucket code
                    {
                        final IBlockState state = world.getBlockState(movingobjectposition.getBlockPos());
                        final Block block = state.getBlock();
                        final Material blockMaterial = state.getMaterial(); //TODO maybe add check to make sure block and state material match?

                        if (player.canPlayerEdit(movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack))
                        {
                            //Material handler
                            if (bucketMaterial.getHandler() != null)
                            {
                                ItemStack stack = bucketMaterial.getHandler().filledBucketClickBlock(player, itemstack, world, movingobjectposition.getBlockPos());
                                if (!ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack, stack))
                                {
                                    return new ActionResult(EnumActionResult.SUCCESS, stack);
                                }
                            }
                            //Mod support handling
                            if (BucketHandler.blockToHandler.containsKey(block))
                            {
                                BucketHandler handler = BucketHandler.blockToHandler.get(block);
                                if (handler != null)
                                {
                                    ItemStack stack = handler.filledBucketClickBlock(player, itemstack, world, movingobjectposition.getBlockPos());
                                    if (!ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack, stack))
                                    {
                                        return new ActionResult(EnumActionResult.SUCCESS, stack);
                                    }
                                }
                            }

                            if (!blockMaterial.isSolid() && block.isReplaceable(world, movingobjectposition.getBlockPos()))
                            {
                                return new ActionResult(EnumActionResult.SUCCESS, placeFluid(player, itemstack, world, movingobjectposition.getBlockPos()));
                            }
                        }

                        //Offset position based on side hit
                        BlockPos blockpos1 = movingobjectposition.getBlockPos().offset(movingobjectposition.sideHit);

                        if (player.canPlayerEdit(blockpos1, movingobjectposition.sideHit, itemstack))
                        {
                            //Bucket material handling
                            if (bucketMaterial.getHandler() != null)
                            {
                                ItemStack stack = bucketMaterial.getHandler().placeFluidClickBlock(player, itemstack, world, blockpos1);
                                if (!ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack, stack))
                                {
                                    return new ActionResult(EnumActionResult.SUCCESS, stack);
                                }
                            }

                            //Mod support handling
                            if (BucketHandler.blockToHandler.containsKey(block))
                            {
                                BucketHandler handler = BucketHandler.blockToHandler.get(block);
                                if (handler != null)
                                {
                                    ItemStack stack = handler.placeFluidClickBlock(player, itemstack, world, blockpos1);
                                    if (!ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack, stack))
                                    {
                                        return new ActionResult(EnumActionResult.SUCCESS, stack);
                                    }
                                }
                            }
                            return new ActionResult(EnumActionResult.SUCCESS, placeFluid(player, itemstack, world, blockpos1));
                        }
                    }
                }
            }
            return new ActionResult(EnumActionResult.PASS, itemstack);
        }
        return new ActionResult(EnumActionResult.FAIL, itemstack);
    }

    protected ItemStack consumeBucket(ItemStack currentStack, EntityPlayer player, ItemStack newStack)
    {
        //Creative mode we don't care about items
        if (player.capabilities.isCreativeMode)
        {
            return currentStack;
        }
        //If we only have one bucket consume and replace slot with new bucket
        else if ((currentStack.getCount() - 1) <= 0)
        {
            return newStack;
        }
        //If we have more than one bucket try to add the new one to the player's inventory
        else
        {
            currentStack.setCount(currentStack.getCount() - 1);
            if (!player.inventory.addItemStackToInventory(newStack))
            {
                player.dropItem(newStack, false);
            }

            return currentStack;
        }
    }

    public ItemStack pickupFluid(EntityPlayer player, ItemStack itemstack, BucketMaterial material, World world, BlockPos pos)
    {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        //Bucket material handling
        if (material.getHandler() != null)
        {
            if (material.getHandler() != null)
            {
                ItemStack stack = material.getHandler().emptyBucketClickBlock(player, itemstack, world, pos);
                if (!ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack, stack))
                {
                    return stack;
                }
            }
        }

        //Mod support
        if (BucketHandler.blockToHandler.containsKey(block))
        {
            BucketHandler handler = BucketHandler.blockToHandler.get(block);
            if (handler != null)
            {
                ItemStack stack = handler.emptyBucketClickBlock(player, itemstack, world, pos);
                if (!ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack, stack))
                {
                    return stack;
                }
            }
        }

        if (block == Blocks.WATER && ((Integer) blockState.getValue(BlockLiquid.LEVEL)).intValue() == 0)
        {
            if (world.setBlockToAir(pos))
            {
                ItemStack bucket = getEmptyBucket(itemstack);
                fill(bucket, new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
                return this.consumeBucket(itemstack, player, bucket);
            }
        }
        else if (block == Blocks.LAVA && ((Integer) blockState.getValue(BlockLiquid.LEVEL)).intValue() == 0)
        {
            if (world.setBlockToAir(pos))
            {
                ItemStack bucket = getEmptyBucket(itemstack);
                fill(bucket, new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), true);
                return this.consumeBucket(itemstack, player, bucket);
            }
        }
        else if (block instanceof IFluidBlock && ((IFluidBlock) block).canDrain(world, pos))
        {
            FluidStack drainedFluid = ((IFluidBlock) block).drain(world, pos, false);

            //TODO allow partial fills
            if (isValidFluidStack(drainedFluid))
            {
                ItemStack bucket = getEmptyBucket(itemstack);
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
        return drainedFluid != null && drainedFluid.getFluid() != null && drainedFluid.amount == Fluid.BUCKET_VOLUME;
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
                        //TODO allow material to change sound
                        world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                        for (int k = 0; k < 8; ++k)
                        {
                            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double) pos.getX() + Math.random(), (double) pos.getY() + Math.random(), (double) pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]);
                        }
                        return consumeBucket(itemstack, player, getEmptyBucket(itemstack));
                    }
                    else
                    {
                        if (!world.isRemote)
                        {
                            //world.func_147480_a(x, y, z, true);
                        }
                        if (stack.getFluid() == FluidRegistry.WATER)
                        {
                            world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
                        }
                        else if (stack.getFluid() == FluidRegistry.LAVA)
                        {
                            world.setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState());
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
            player.sendMessage(new TextComponentTranslation(getTranslationKey() + ".volume.notEnoughForFullBlock"));
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
    public FluidStack getFluid(ItemStack container)
    {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Fluid"))
        {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.getTagCompound().getCompoundTag("Fluid"));
    }

    public int getCapacity(ItemStack container)
    {
        return Fluid.BUCKET_VOLUME;
    }

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

                if (container.getTagCompound().isEmpty())
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
        return isEmpty(stack) ? Items.BUCKET.getItemStackLimit(stack) : 1;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held)
    {
        final BucketMaterial material = BucketMaterialHandler.getMaterial(stack.getItemDamage());
        final FluidStack fluid = getFluid(stack);
        if (material != null && fluid != null && fluid.getFluid() != null)
        {
            //Material handler
            if (material.getHandler() != null && material.getHandler().onUpdate(stack, world, entity, slot, held))
            {
                return;
            }

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
                            //Material handler
                            if (material.getHandler() != null && material.getHandler().onBucketLeaked(stack, world, entity, slot, held))
                            {
                                return;
                            }

                            //Bucket material handling
                            if (material.getHandler() != null)
                            {
                                if (material.getHandler().onBucketLeaked(stack, world, entity, slot, held))
                                {
                                    return;
                                }
                            }

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

                                    final BlockPos pos = new BlockPos(x, y, z);
                                    final IBlockState state = world.getBlockState(pos);

                                    final Block block2 = world.getBlockState(pos.up()).getBlock();

                                    //Check if face of block one is solid
                                    if (state.getBlockFaceShape(world, pos, EnumFacing.UP) == BlockFaceShape.SOLID)
                                    {
                                        //Check that block two can be replaced
                                        if (block2.isAir(world.getBlockState(pos.up()), world, pos.up()) || block2.isReplaceable(world, pos.up()))
                                        {
                                            //Create fire
                                            world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
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
        final BucketMaterial material = BucketMaterialHandler.getMaterial(entityItem.getItem().getItemDamage());
        final FluidStack fluid = getFluid(entityItem.getItem());
        if (material != null && fluid != null && fluid.getFluid() != null)
        {
            //Material handler
            if (material.getHandler() != null && material.getHandler().onEntityItemUpdate(entityItem))
            {
                return true;
            }

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
            if (entityItem.getEntityWorld().getWorldTime() % 5 == 0)
            {
                if (material != null)
                {
                    if (material.preventHotFluidUsage && fluid.getFluid().getTemperature(fluid) > 400)
                    {
                        if (material.damageBucketWithHotFluid && entityItem.getEntityWorld().rand.nextFloat() < ((float) fluid.getFluid().getTemperature(fluid) / 1500f))
                        {
                            //TODO play sound effect of items burning
                            BucketMaterial damaged = material.getDamagedBucket(entityItem.getItem());
                            if (damaged != null)
                            {
                                entityItem.getItem().setItemDamage(damaged.metaValue);
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
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand)
    {
        if (player.world.isRemote)
        {
            return true;
        }

        if (entity != null)
        {
            final Class<? extends Entity> clazz = entity.getClass();
            final BucketMaterial material = BucketMaterialHandler.getMaterial(stack.getItemDamage());
            if (material != null && material.allowInteractionOfEntity(EntityRegistry.getEntry(clazz)))
            {
                //Material handler
                if (material.getHandler() != null && material.getHandler().rightClickEntity(stack, player, entity))
                {
                    return true;
                }

                //Entity handling
                if (BucketHandler.entityToHandler.containsKey(clazz))
                {
                    List<BucketHandler> handlers = BucketHandler.entityToHandler.get(clazz);
                    for (BucketHandler handler : handlers)
                    {
                        if (handler.rightClickEntity(stack, player, entity))
                        {
                            return true;
                        }
                    }
                }

                //Default entity handling
                if (entity instanceof EntityCow && isEmpty(stack))
                {
                    if (player.getEntityWorld().isRemote)
                    {
                        return true;
                    }

                    final Fluid fluid = FluidRegistry.getFluid("milk");
                    if (fluid != null)
                    {
                        ItemStack newBucket = getEmptyBucket(stack);
                        fill(newBucket, new FluidStack(fluid, Fluid.BUCKET_VOLUME), true);
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, consumeBucket(stack, player, newBucket));
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    else
                    {
                        ((EntityCow) entity).playLivingSound();
                        player.sendMessage(new TextComponentTranslation(getTranslationKey() + ".error.fluid.milk.notRegistered"));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper to create a new empty bucket matching data of
     * existing bucket without a fluid
     *
     * @param stack - item
     * @return new item
     */
    public static ItemStack getEmptyBucket(ItemStack stack)
    {
        //TODO save any NBT data needed by the bucket
        return new ItemStack(stack.getItem(), 1, stack.getItemDamage());
    }

    /**
     * Helper to create a new bucket filled with fluid.
     * Used mainly for recipe inputs and other actions
     * that do not require having the exact material.
     *
     * @param fluidToFillWith
     * @return new item with fluid
     */
    public ItemStack getBucket(Fluid fluidToFillWith)
    {
        ItemStack stack = new ItemStack(this);
        if (fluidToFillWith != null) //TODO maybe do registry check?
        {
            fill(stack, new FluidStack(fluidToFillWith, getCapacity(stack)), true);
        }
        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        if (tab == getCreativeTab())
        {
            for (BucketMaterial material : BucketMaterialHandler.getMaterials())
            {
                list.add(new ItemStack(this, 1, material.metaValue));
            }

            for (Fluid fluid : FluidRegistry.getRegisteredFluids().values())
            {
                if (fluid != null)
                {
                    ItemStack milkBucket = new ItemStack(this, 1, BucketMaterialHandler.getMaterials().iterator().next().metaValue);
                    fill(milkBucket, new FluidStack(fluid, Fluid.BUCKET_VOLUME), true);
                    list.add(milkBucket);
                }
            }

            for (BucketHandler handler : BucketHandler.bucketHandlers)
            {
                handler.getSubItems(this, list);
            }
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

    @Override
    public boolean hasContainerItem(ItemStack stack)
    {
        return getFluid(stack) != null;
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        BucketMaterial material = BucketMaterialHandler.getMaterial(stack.getItemDamage());
        if (material != null)
        {
            return material.getUnlocalizedName(stack);
        }
        return super.getTranslationKey();
    }
}
