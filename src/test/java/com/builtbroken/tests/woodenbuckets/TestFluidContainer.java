package com.builtbroken.tests.woodenbuckets;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.bucket.BucketMaterial;
import com.builtbroken.mc.fluids.bucket.BucketMaterialHandler;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import com.builtbroken.mc.testing.junit.AbstractTest;
import com.builtbroken.mc.testing.junit.VoltzTestRunner;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/28/2016.
 */
@RunWith(VoltzTestRunner.class)
public class TestFluidContainer extends AbstractTest
{
    @Override
    public void setUpForEntireClass()
    {
        super.setUpForEntireClass();

        //Create bucket to allow tests to run
        if (FluidModule.bucket == null)
        {
            FluidModule.bucket = new ItemFluidBucket("bucket");
            GameRegistry.registerItem(FluidModule.bucket, "testWoodenBucketII");
        }

        //Create material to allow tests to run
        BucketMaterialHandler.addMaterial("test", new BucketMaterial("test", "test"));
    }

    @Test
    public void testFill()
    {
        //Create fluid
        final Fluid fluid = FluidRegistry.WATER;
        final FluidStack stack = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);

        //Created filled item
        ItemStack item = new ItemStack(FluidModule.bucket, 1, 0);

        //Try to fill item
        int f = FluidModule.bucket.fill(item, stack, true);

        //Check that it filled
        assertEquals("Failed to fill container with 1000mb of water", f, FluidContainerRegistry.BUCKET_VOLUME);
    }

    //TODO finish tests

    @Override
    public void tearDownForEntireClass()
    {
        //Clear test data
        BucketMaterialHandler.clear();
    }
}
