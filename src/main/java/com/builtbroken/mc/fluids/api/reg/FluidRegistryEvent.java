package com.builtbroken.mc.fluids.api.reg;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired during registration to allow hooking into the preInit phase of the mod to add new fluids.
 * <p>
 * If you need to know when a fluid has been registered use the {@link net.minecraftforge.fluids.FluidRegistry.FluidRegisterEvent} provided
 * by forge to note when a fluid has been registered.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/30/2017.
 */
public class FluidRegistryEvent extends Event
{
    /**
     * Called to load fluids and ask the fluid module to generate the fluids
     */
    public static class Pre extends FluidRegistryEvent
    {
        //Nothing to do with the event call FluidHelper to register/request fluids
    }

    /**
     * Called after all fluids have been generated. Use this event
     * to change properties about the fluid. As well require loading
     * of Fluid blocks if required.
     */
    public static class Post extends FluidRegistryEvent
    {
        //Nothing to do with the event call FluidHelper to get generated fluids
    }

}
