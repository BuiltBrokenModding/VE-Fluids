package com.builtbroken.mc.fluids.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base class for all bucket events
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 9/9/2016.
 */
public class BucketEvent extends Event
{
    public final ItemStack bucketStack;

    public BucketEvent(ItemStack bucketStack)
    {
        this.bucketStack = bucketStack;
    }
}
