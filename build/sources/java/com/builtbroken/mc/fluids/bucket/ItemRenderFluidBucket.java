package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/7/2017.
 */
public class ItemRenderFluidBucket implements IItemRenderer
{
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        if (item != null && item.getItem() instanceof ItemFluidBucket)
        {
            BucketMaterial material = BucketMaterialHandler.getMaterial(item.getItemDamage());
            if (material != null)
            {
                return material instanceof IItemRenderer && ((IItemRenderer) material).handleRenderType(item, type);
            }
        }
        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        if (item != null && item.getItem() instanceof ItemFluidBucket)
        {
            BucketMaterial material = BucketMaterialHandler.getMaterial(item.getItemDamage());
            if (material != null)
            {
                return material instanceof IItemRenderer && ((IItemRenderer) material).shouldUseRenderHelper(type, item, helper);
            }
        }
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        if (item != null && item.getItem() instanceof ItemFluidBucket)
        {
            BucketMaterial material = BucketMaterialHandler.getMaterial(item.getItemDamage());
            if (material != null)
            {
                if (material instanceof IItemRenderer)
                {
                    ((IItemRenderer) material).renderItem(type, item, data);
                }
                //Should never happen
                else
                {
                    FluidModule.logger.error("RenderItem(" + type + ", " + item + ", " + data + ") was called on a bucket without an item renderer.");
                }
            }
            //Should never happen
            else
            {
                FluidModule.logger.error("RenderItem(" + type + ", " + item + ", " + data + ") was called on a bucket without a material.");
            }
        }
        //Should never happen
        else
        {
            FluidModule.logger.error("RenderItem(" + type + ", " + item + ", " + data + ") was called but was not a fluid bucket.");
        }
    }
}
