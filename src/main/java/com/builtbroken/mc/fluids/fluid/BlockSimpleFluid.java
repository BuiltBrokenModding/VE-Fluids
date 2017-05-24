package com.builtbroken.mc.fluids.fluid;

import com.builtbroken.mc.fluids.FluidModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * Created by Dark on 8/8/2015.
 */
public class BlockSimpleFluid extends BlockFluidClassic
{
    private IIcon flowingIcon;
    private String iconName;

    public BlockSimpleFluid(Fluid fluid, String blockName, String iconName)
    {
        super(fluid, Material.water);
        this.iconName = iconName;
        setBlockName(FluidModule.DOMAIN + ":" + blockName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
        this.blockIcon = reg.registerIcon(FluidModule.DOMAIN + ":" + iconName + "_still");
        this.flowingIcon = reg.registerIcon(FluidModule.DOMAIN + ":" + iconName + "_flow");
        getFluid().setFlowingIcon(flowingIcon);
        getFluid().setStillIcon(blockIcon);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z)
    {
        return getFluid().getColor();
    }
}
