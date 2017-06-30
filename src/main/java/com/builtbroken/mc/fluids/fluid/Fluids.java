package com.builtbroken.mc.fluids.fluid;

import com.builtbroken.mc.fluids.FluidModule;
import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;

import java.awt.*;

/**
 * Enum of supported fluids for generation in the mod
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/30/2017.
 */
public enum Fluids
{
    MILK("milk", new Color(235, 231, 210)),
    FUEL("fuel", new Color(110, 109, 19)),
    OIL("oil", new Color(27, 27, 27));

    public final String name;
    public final Color color;
    public final int colorInt;

    public boolean generate = false;

    public Fluid fluid;
    public Block block;

    Fluids(String name, Color color)
    {
        this.name = name;
        this.color = color;
        this.colorInt = color != null ? color.getRGB() : 0xFFFFFF;
    }

    public static void load(Configuration configuration)
    {
        FluidModule.logger.info("Generating fluids");
        for (Fluids data : values())
        {
            if (data.generate || configuration.getBoolean(data.name, "GenerateFluid", true, "Set to true to load the fluid into the game. Mods can override this behavior if they depend on the fluid."))
            {
                FluidModule.logger.info("\tGenerating '" + data.name);
                Fluid fluid = FluidHelper.createOrGetFluid(data.name, "fluid");
                if (fluid != null)
                {
                    if (fluid instanceof FluidVE)
                    {
                        ((FluidVE) fluid).setColor(data.colorInt);
                    }
                    FluidModule.logger.info("\t\t= " + fluid);
                }
            }
        }
    }
}
