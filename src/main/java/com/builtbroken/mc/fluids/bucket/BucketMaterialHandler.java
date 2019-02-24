package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.api.reg.BucketMaterialRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.Collection;
import java.util.HashMap;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/3/2017.
 */
public class BucketMaterialHandler
{
    private static HashMap<String, BucketMaterial> nameToMaterial = new HashMap();

    private static HashMap<String, Integer> nameToMeta = new HashMap();
    private static HashMap<Integer, String> metaToName = new HashMap();
    private static int nextID = -1;

    /**
     * Adds a new material type to the mods
     * <p>
     * Keep in mind there is only 32,000 positions for materials. If you
     * plan to register more than a few hundred consider creating by extending
     * the existing version. Then adding your own list of materials to it. This way
     * the handler can be used to register materials for mods that only add a few types.
     *
     * @param name     - material name (e.g. iron, birch, copper, stone)
     * @param material - object describing the material
     */
    public static void addMaterial(String name, BucketMaterial material)
    {
        addMaterial(name, material, -1);
    }

    /**
     * Adds a new material type to the mods
     * <p>
     * Keep in mind there is only 32,000 positions for materials. If you
     * plan to register more than a few hundred consider creating by extending
     * the existing version. Then adding your own list of materials to it. This way
     * the handler can be used to register materials for mods that only add a few types.
     *
     * @param name          - material name (e.g. iron, birch, copper, stone)
     * @param material      - object describing the material
     * @param requestedMeta - this is for legacy support only. It should only be used
     *                      to transition old mods over to the new material system.
     */
    public static void addMaterial(String name, BucketMaterial material, int requestedMeta)
    {
        if (name.contains(":"))
        {
            throw new RuntimeException("[BucketMaterialHandler] Bucket material names can not contain ':' colin as this is used as part of the config system", new RuntimeException("trace"));
        }

        boolean registered = false;


        //Set material name
        material.materialName = name;

        //Give a warning if something is about to be overridden
        if (nameToMaterial.containsKey(name))
        {
            FluidModule.logger.error("Entry: " + name + " is being overridden with " + material);
        }

        //Store material
        nameToMaterial.put(name, material);

        //Attempt to use the requested meta value
        if (requestedMeta >= 0 && metaToName.get(requestedMeta) == null)
        {
            metaToName.put(requestedMeta, name);
            nameToMeta.put(name, requestedMeta);
            material.metaValue = requestedMeta;
            registered = true;
        }
        //If the nameToMeta contains a value, use said value
        else if (nameToMeta.containsKey(name))
        {
            material.metaValue = nameToMeta.get(name);
            registered = true;
        }
        //Try to locate an empty slot
        else
        {
            //Loop ID slots to find a free slot
            while (nextID < 32000)
            {
                nextID++;
                if (metaToName.get(nextID) == null)
                {
                    reserveMaterial(name, nextID);
                    material.metaValue = nextID;
                    registered = true;
                    break;
                }
            }

            //Error if we go over the meta value for items
            if (nextID >= 32000)
            {
                throw new RuntimeException("More than 32000 bucket materials have been registered. Report this to the author so more bucket items can be add to expand the max size. In all honesty this should never happen unless a mod is overloading the register.");
            }
        }

        if (registered)
        {
            MinecraftForge.EVENT_BUS.post(new BucketMaterialRegistryEvent.Reg(material));
        }
    }

    /**
     * Called to reserve a place in the material list
     *
     * @param name
     * @param meta
     */
    public static void reserveMaterial(String name, int meta)
    {
        metaToName.put(meta, name);
        nameToMeta.put(name, meta);
    }

    /**
     * Gets the material by name
     *
     * @param name
     * @return
     */
    public static BucketMaterial getMaterial(String name)
    {
        return name != null ? nameToMaterial.get(name) : null;
    }

    public static BucketMaterial getMaterial(int meta)
    {
        return getMaterial(metaToName.get(meta));
    }

    public static String getName(int meta)
    {
        return metaToName.get(meta);
    }

    public static int getMeta(String name)
    {
        return nameToMeta.get(name);
    }

    public static Collection<BucketMaterial> getMaterials()
    {
        return nameToMaterial.values();
    }


    /**
     * Called to load bucket material IDs from config
     *
     * @param bucketConfig
     */
    public static void load(Configuration bucketConfig)
    {
        //TODO add event for changing mappings
        if (bucketConfig.hasKey(Configuration.CATEGORY_GENERAL, "metaValues"))
        {
            String[] values = bucketConfig.getStringList("metaValues", Configuration.CATEGORY_GENERAL, new String[]{""}, "");
            if (values != null)
            {
                for (String s : values)
                {
                    if (s != null && s.contains(":"))
                    {
                        String[] split = s.split(":");
                        try
                        {
                            int meta = Integer.parseInt(split[1]);
                            BucketMaterialHandler.reserveMaterial(split[0], meta);
                        }
                        catch (NumberFormatException e)
                        {
                            throw new RuntimeException("Invalid data [" + s + "] in metaValue field in " + bucketConfig.getConfigFile());
                        }
                    }
                    else
                    {
                        throw new RuntimeException("Invalid data [" + s + "] in metaValue field in " + bucketConfig.getConfigFile());
                    }
                }
            }
        }
    }

    /**
     * Called to save bucket material ids to config
     *
     * @param bucketConfig
     */
    public static void save(Configuration bucketConfig)
    {
        //Get prop
        Property prop = bucketConfig.get(Configuration.CATEGORY_GENERAL, "metaValues", new String[]{""});
        prop.setComment("List of materials to meta values for containers. Do not change any of these values unless you know what you are doing. Changing these values will affect the world save and could result in unexpected behavior.");

        //Create list
        String[] list = new String[BucketMaterialHandler.getMaterials().size()];
        int i = 0;
        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            list[i] = material.materialName + ":" + material.metaValue;
            i++;
        }
        //Set list
        prop.set(list);
    }
}
