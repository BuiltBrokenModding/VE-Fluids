package com.builtbroken.mc.fluids.fluid;

import com.builtbroken.mc.fluids.FluidModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * Created by Dark on 8/8/2015.
 */
public class BlockMilk extends BlockFluidClassic
{
    IIcon blockFlowing;

    public BlockMilk(Fluid fluid)
    {
        super(fluid, Material.water);
        setBlockName(FluidModule.DOMAIN + ":milk");
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
        this.blockIcon = reg.registerIcon(FluidModule.DOMAIN + ":milk_still");
        this.blockFlowing = reg.registerIcon(FluidModule.DOMAIN + ":milk_flow");
        getFluid().setFlowingIcon(blockFlowing);
        getFluid().setStillIcon(blockIcon);
    }
}
