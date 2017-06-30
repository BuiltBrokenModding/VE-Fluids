package com.builtbroken.mc.fluids.fluid;

import com.builtbroken.mc.fluids.FluidModule;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/30/2017.
 */
public class FluidHelper
{
    /** List of fluids generated by this mod */
    public static List<Fluid> generatedFluids = new ArrayList();
    /** List of fluid blocks generated by this mod */
    public static List<BlockSimpleFluid> generatedFluidBlocks = new ArrayList();

    /**
     * Used to quickly create a new fluid with block
     *
     * @param name - unique registry name of the fluid
     * @param icon - icon name / location, used to generate resource locations for fluid textures
     * @return generated fluid or already registered fluid
     */
    public static Fluid createOrGetFluid(String name, String icon)
    {
        return createOrGetFluid(FluidModule.DOMAIN, name, icon);
    }

    /**
     * Used to quickly create a new fluid with block
     *
     * @param domain - mod ID to use when setting varies entries
     * @param icon   - icon name / location, used to generate resource locations for fluid textures
     * @param name   - unique registry name of the fluid
     * @return generated fluid or already registered fluid
     */
    public static Fluid createOrGetFluid(String domain, String name, String icon)
    {
        Fluid fluid;
        if (FluidRegistry.getFluid(name) == null)
        {
            fluid = new FluidVE(domain, name, icon);
            if (!FluidRegistry.registerFluid(fluid))
            {
                //Should never happen
                throw new RuntimeException("Failed to register fluid[" + name + "] even though one is not registered");
            }
            generatedFluids.add(fluid);
        }
        else
        {
            fluid = FluidRegistry.getFluid(name);
        }
        return fluid;
    }

    public static void createBlockForFluidIfMissing(Fluid fluid)
    {
        if (fluid.getBlock() == null)
        {
            final String name = fluid.getName();
            BlockSimpleFluid block = new BlockSimpleFluid(fluid, name);
            block.setRegistryName("veBlock" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length()));
            FluidHelper.generatedFluidBlocks.add(block);
            GameRegistry.register(block);
            GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
    }
}
