package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.mods.BucketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
    //TODO rename to fluid.molten
    public static String[] supportedFluidTextures = new String[]{"milk", "blood", "slime.blue", "fuel", "aluminum.molten", "glue", "alubrass.molten", "alumite.molten", "angmallen.molten", "ardite.molten", "bronze.molten", "cobalt.molten", "copper.molten", "electrum.molten", "emerald.molten", "ender.molten", "enderium.molten", "glass.molten", "gold.molten", "invar.molten", "iron.molten", "lead.molten", "lumium.molten", "manyullyn.molten", "mithril.molten", "nickel.molten", "obsidian.molten", "pigiron.molten", "shiny.molten", "signalum.molten", "silver.molten", "steel.molten", "tin.molten", "oil", "redplasma"};

    public ItemFluidBucket(String name)
    {
        this.maxStackSize = 1;
        this.setRegistryName(new ResourceLocation(FluidModule.DOMAIN, "veBucket"));
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setHasSubtypes(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn)
    {
        if (!isEmpty(stack))
        {
            list.add(I18n.format(getUnlocalizedName() + ".fluid.name") + ": " + getFluid(stack).getLocalizedName());
            list.add(I18n.format(getUnlocalizedName() + ".fluid.amount.name") + ": " + getFluid(stack).amount + "mb");
        }
        else if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.capabilities.isCreativeMode)
        {
            list.add("\u00a7c" + I18n.format(getUnlocalizedName() + ".creative.void"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        boolean isBucketEmpty = this.isEmpty(itemstack);
        RayTraceResult movingobjectposition = this.rayTrace(world, player, isBucketEmpty);

        if (movingobjectposition != null)
        {
            if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                TileEntity tile = world.getTileEntity(movingobjectposition.getBlockPos());

                if (tile instanceof IFluidHandler)
                {
                    return new ActionResult(EnumActionResult.PASS, itemstack);
                }


                if (!world.isBlockModifiable(player, movingobjectposition.getBlockPos()))
                {
                    return new ActionResult(EnumActionResult.PASS, itemstack);
                }

                //Fill bucket code
                if (isBucketEmpty)
                {
                    if (player.canPlayerEdit(movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack))
                    {
                        return new ActionResult(EnumActionResult.SUCCESS, pickupFluid(player, itemstack, world, movingobjectposition.getBlockPos()));
                    }
                }
                else //Empty bucket code
                {
                    Block block = world.getBlockState(movingobjectposition.getBlockPos()).getBlock();
                    Material material = block.getMaterial(world.getBlockState(movingobjectposition.getBlockPos()));

                    if (player.canPlayerEdit(movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack))
                    {
                        if (BucketHandler.blockToHandler.containsKey(block))
                        {
                            BucketHandler handler = BucketHandler.blockToHandler.get(block);
                            if (handler != null)
                            {
                                return new ActionResult(EnumActionResult.SUCCESS, handler.filledBucketClickBlock(player, itemstack, world, movingobjectposition.getBlockPos()));
                            }
                        }

                        if (!material.isSolid() && block.isReplaceable(world, movingobjectposition.getBlockPos()))
                        {
                            return new ActionResult(EnumActionResult.SUCCESS, placeFluid(player, itemstack, world, movingobjectposition.getBlockPos()));
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
                                return new ActionResult(EnumActionResult.SUCCESS, handler.placeFluidClickBlock(player, itemstack, world, blockpos1));
                            }
                        }
                        return new ActionResult(EnumActionResult.SUCCESS, placeFluid(player, itemstack, world, blockpos1));
                    }
                }
            }
        }
        return new ActionResult(EnumActionResult.PASS, itemstack);

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

        if (block == Blocks.WATER && ((Integer) blockState.getValue(BlockLiquid.LEVEL)).intValue() == 0)
        {
            if (world.setBlockToAir(pos))
            {
                ItemStack bucket = new ItemStack(this, 1, itemstack.getItemDamage());
                fill(bucket, new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
                return this.consumeBucket(itemstack, player, bucket);
            }
        }
        else if (block == Blocks.LAVA && ((Integer) blockState.getValue(BlockLiquid.LEVEL)).intValue() == 0)
        {
            if (world.setBlockToAir(pos))
            {
                ItemStack bucket = new ItemStack(this, 1, itemstack.getItemDamage());
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
                        world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                        for (int k = 0; k < 8; ++k)
                        {
                            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double) pos.getX() + Math.random(), (double) pos.getY() + Math.random(), (double) pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]);
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
            player.sendMessage(new TextComponentTranslation(getUnlocalizedName() + ".volume.notEnoughForFullBlock"));
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
        return isEmpty(stack) ? Items.BUCKET.getItemStackLimit() : 1;
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
                                    if (block.isSideSolid(world.getBlockState(pos), world, pos, EnumFacing.UP))
                                    {
                                        if (block2.isAir(world.getBlockState(pos.up()), world, pos.up()) || block2.isReplaceable(world, pos.up()))
                                        {
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
        FluidStack fluid = getFluid(entityItem.getItem());
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
            if (entityItem.getEntityWorld().getWorldTime() % 5 == 0)
            {
                BucketMaterial material = BucketMaterialHandler.getMaterial(entityItem.getItem().getItemDamage());
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
        if (entity != null)
        {
            Class<? extends Entity> clazz = entity.getClass();
            if (BucketHandler.entityToHandler.containsKey(clazz) && BucketHandler.entityToHandler.get(clazz).rightClickEntity(stack, player, entity))
            {
                return true;
            }

            if (entity instanceof EntityCow && isEmpty(stack))
            {
                if (player.getEntityWorld().isRemote)
                {
                    return true;
                }

                Fluid fluid = FluidRegistry.getFluid("milk");
                if (fluid != null)
                {
                    ItemStack newBucket = new ItemStack(this, 1, stack.getItemDamage());
                    fill(newBucket, new FluidStack(fluid, Fluid.BUCKET_VOLUME), true);
                    stack.setCount(stack.getCount() - 1);
                    player.inventory.addItemStackToInventory(newBucket);
                    player.inventoryContainer.detectAndSendChanges();
                }
                else
                {
                    ((EntityCow) entity).playLivingSound();
                    player.sendMessage(new TextComponentTranslation(getUnlocalizedName() + ".error.fluid.milk.notRegistered"));
                }
                return true;
            }
        }
        return false;
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

    public ItemStack getBucket(Fluid water)
    {
        ItemStack stack = new ItemStack(this);
        fill(stack, new FluidStack(water, getCapacity(stack)), true);
        return stack;
    }
}
