package com.builtbroken.mc.fluids.fluid;

import com.builtbroken.mc.fluids.FluidModule;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Dark on 8/8/2015.
 */
public class BlockSimpleFluid extends BlockFluidClassic
{
    public BlockSimpleFluid(Fluid fluid, String blockName)
    {
        super(fluid, Material.water);
        setUnlocalizedName(FluidModule.DOMAIN + ":" + blockName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass)
    {
        return getFluid().getColor();
    }
}
