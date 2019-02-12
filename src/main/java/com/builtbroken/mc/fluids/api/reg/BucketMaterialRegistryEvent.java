package com.builtbroken.mc.fluids.api.reg;

import com.builtbroken.mc.fluids.bucket.BucketMaterial;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Event fired during registration to allow hooking into the preInit phase of the mod to add new bucket materials
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/30/2017.
 */
public class BucketMaterialRegistryEvent extends Event
{

    /**
     * Called to load new bucket materials
     */
    public static class Pre extends BucketMaterialRegistryEvent
    {
        //Call BucketMaterialHandler to add new materials
    }

    /**
     * Called when a new bucket material is added. Use this event
     * to directly edit the material or add a new material that
     * requires this material.
     */
    public static class Reg extends BucketMaterialRegistryEvent
    {

        /**
         * Material, do not change reference or meta values
         */
        public final BucketMaterial material;

        public Reg(BucketMaterial material)
        {
            this.material = material;
        }
    }

    /**
     * Called after all bucket materials have been loaded. Use this
     * to update information about the materials or reference data as needed.
     */
    public static class Post extends BucketMaterialRegistryEvent
    {
        //Call BucketMaterialHandler to gain access to materials
    }

    /**
     * Called client side to allow changing the model used by the bucket.
     * <p>
     * This event is recycled to save memory. Do not cache or modify
     * values outside of the model resource location.
     */
    @SideOnly(Side.CLIENT)
    public static class Model extends BucketMaterialRegistryEvent
    {

        /**
         * Material, do not change properties
         */
        public BucketMaterial material;

        /**
         * Model currently in use, change this if you want
         */
        public ModelResourceLocation modelResourceLocation;

        public Model(BucketMaterial material, ModelResourceLocation modelResourceLocation)
        {
            this.material = material;
            this.modelResourceLocation = modelResourceLocation;
        }
    }
}
