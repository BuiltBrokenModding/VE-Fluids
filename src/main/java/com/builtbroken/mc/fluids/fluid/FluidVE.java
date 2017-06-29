package com.builtbroken.mc.fluids.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/24/2017.
 */
public class FluidVE extends Fluid
{
    protected int color = 0xFFFFFF;

    public FluidVE(String modID, String fluidName, String icon)
    {
        super(fluidName, new ResourceLocation(modID, icon), new ResourceLocation(modID, icon + "_flowing"));
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    @Override
    public int getColor()
    {
        return color;
    }
}
