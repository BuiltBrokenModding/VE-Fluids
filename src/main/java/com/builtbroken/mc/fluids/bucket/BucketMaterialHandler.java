package com.builtbroken.mc.fluids.bucket;

import com.builtbroken.mc.fluids.FluidModule;

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
        material.materialName = name;
        if (nameToMaterial.containsKey(name))
        {
            FluidModule.logger.error("Entry: " + name + " is being overridden with " + material);
            nameToMaterial.put(name, material);
        }
        else
        {
            nameToMaterial.put(name, material);
            boolean foundID = false;
            if (requestedMeta >= 0)
            {
                if (metaToName.get(requestedMeta) == null)
                {
                    metaToName.put(requestedMeta, name);
                    nameToMeta.put(name, requestedMeta);
                    material.metaValue = requestedMeta;
                    foundID = true;
                }
            }
            if (!foundID)
            {
                while (nextID < 32000)
                {
                    nextID++;
                    if (metaToName.get(nextID) == null)
                    {
                        metaToName.put(nextID, name);
                        nameToMeta.put(name, nextID);
                        material.metaValue = nextID;
                        break;
                    }
                }
            }
            if (nextID >= 32000)
            {
                throw new RuntimeException("More than 32000 bucket materials have been registered. Report this to the author so more bucket items can be add to expand the max size. In all honesty this should never happen unless a mod is overloading the register.");
            }
        }
    }

    /**
     * Gets the material by name
     *
     * @param name
     * @return
     */
    public static BucketMaterial getMaterial(String name)
    {
        return nameToMaterial.get(name);
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
}
