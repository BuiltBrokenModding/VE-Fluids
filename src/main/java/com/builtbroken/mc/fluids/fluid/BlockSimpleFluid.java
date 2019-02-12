package com.builtbroken.mc.fluids.fluid;

import com.builtbroken.mc.fluids.FluidModule;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * Created by Dark on 8/8/2015.
 */
public class BlockSimpleFluid extends BlockFluidClassic
{
    public BlockSimpleFluid(Fluid fluid, String blockName)
    {
        super(fluid, Material.WATER);
        setTranslationKey(FluidModule.DOMAIN + ":" + blockName);
    }
}
