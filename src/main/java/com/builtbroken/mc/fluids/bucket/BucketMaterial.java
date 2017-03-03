package com.builtbroken.mc.fluids.bucket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/3/2017.
 */
public class BucketMaterial
{
    //Settings
    public boolean PREVENT_HOT_FLUID_USAGE = true;
    public boolean DAMAGE_BUCKET_WITH_HOT_FLUID = true;
    public boolean BURN_ENTITY_WITH_HOT_FLUID = true;
    public boolean ENABLE_FLUID_LEAKING = false;
    public boolean ALLOW_LEAK_TO_CAUSE_FIRES = true;

    public int VISCOSITY_TO_IGNORE_LEAKING = 3000;
    public int AMOUNT_TO_LEAK = 1;
    public float CHANCE_TO_LEAK = 0.03f;
    public float LEAK_FIRE_CHANCE = 0.4f;

    @SideOnly(Side.CLIENT)
    public IIcon icon;

    public String localization;
    public String textureName;

    public String materialName;
    public int metaValue;

    public BucketMaterial(String localization, String textureName)
    {
        this.localization = localization;
        this.textureName = textureName;
    }

    public BucketMaterial getDamagedBucket(ItemStack stack)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {
        icon = register.registerIcon(textureName);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack)
    {
        return icon;
    }
}
