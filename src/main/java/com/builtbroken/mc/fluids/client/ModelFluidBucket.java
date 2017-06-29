package com.builtbroken.mc.fluids.client;

import com.builtbroken.mc.fluids.FluidModule;
import com.builtbroken.mc.fluids.bucket.BucketMaterial;
import com.builtbroken.mc.fluids.bucket.BucketMaterialHandler;
import com.builtbroken.mc.fluids.bucket.ItemFluidBucket;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.fluids.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import java.util.Collection;
import java.util.Map;

/**
 * Clone of {@link net.minecraftforge.client.model.ModelDynBucket} to be more customized towards the application of VE's bucket
 * Though a lot of the code is custom All credit goes to the orginal creator plus fry, lex, and anyone else.
 */
public class ModelFluidBucket implements IModel, IModelCustomData<ModelFluidBucket>
{
    public static final ResourceLocation default_fluid_texture = new ResourceLocation(FluidModule.DOMAIN, "items/bucket.fluid2");
    public static final ResourceLocation default_bucket_texture = new ResourceLocation(FluidModule.DOMAIN, "items/bucket");

    // minimal Z offset to prevent depth-fighting
    private static final float NORTH_Z_FLUID = 7.498f / 16f;
    private static final float SOUTH_Z_FLUID = 8.502f / 16f;

    public static final IModel MODEL = new ModelFluidBucket();

    protected final ResourceLocation baseLocation;
    protected final ResourceLocation liquidLocation;

    protected final Fluid fluid;

    public ModelFluidBucket()
    {
        this(null, null, null);
    }

    public ModelFluidBucket(ResourceLocation baseLocation, ResourceLocation liquidLocation, Fluid fluid)
    {
        this.baseLocation = baseLocation != null ? baseLocation : default_bucket_texture;
        this.liquidLocation = liquidLocation != null ? liquidLocation : default_fluid_texture;
        this.fluid = fluid;
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getTextures()
    {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

        builder.add(default_fluid_texture);
        builder.add(default_fluid_texture);

        for(BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            if(material.getBucketResourceLocation() != null)
            {
                builder.add(material.getBucketResourceLocation());
            }
            if(material.getFluidResourceLocation() != null)
            {
                builder.add(material.getFluidResourceLocation());
            }
        }

        return builder.build();
    }

    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = IPerspectiveAwareModel.MapWrapper.getTransforms(state);

        // if the fluid is a gas wi manipulate the initial state to be rotated 180? to turn it upside down
        if (fluid != null && fluid.isGaseous())
        {
            state = new ModelStateComposition(state, TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, new Quat4f(0, 0, 1, 0), null, null)));
        }

        TRSRTransformation transform = state.apply(Optional.<IModelPart>absent()).or(TRSRTransformation.identity());
        TextureAtlasSprite fluidSprite = null;
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        if (fluid != null)
        {
            fluidSprite = bakedTextureGetter.apply(fluid.getStill());
        }

        if (baseLocation != null)
        {
            // build base (insidest)
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(baseLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }
        if (liquidLocation != null && fluidSprite != null)
        {
            TextureAtlasSprite liquid = bakedTextureGetter.apply(liquidLocation);
            // build liquid layer (inside)
            builder.addAll(ItemTextureQuadConverter.convertTexture(format, transform, liquid, fluidSprite, NORTH_Z_FLUID, EnumFacing.NORTH, fluid.getColor()));
            builder.addAll(ItemTextureQuadConverter.convertTexture(format, transform, liquid, fluidSprite, SOUTH_Z_FLUID, EnumFacing.SOUTH, fluid.getColor()));
        }

        return new BakedDynBucket(this, builder.build(), fluidSprite, format, Maps.immutableEnumMap(transformMap), Maps.<String, IFlexibleBakedModel>newHashMap());
    }

    @Override
    public IModelState getDefaultState()
    {
        return TRSRTransformation.identity();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
        String fluidName = customData.get("fluid");
        Fluid fluid = FluidRegistry.getFluid(fluidName);

        if (fluid == null)
        {
            fluid = this.fluid;
        }

        String materialName = customData.get("material");
        BucketMaterial material = BucketMaterialHandler.getMaterial(materialName);
        if (material == null)
        {
            material = FluidModule.materialIron;
        }

        // create new model with correct liquid
        return new ModelFluidBucket(material.getBucketResourceLocation(), material.getFluidResourceLocation(), fluid);
    }

    // the dynamic bucket is based on the empty bucket
    protected static class BakedDynBucket extends ItemLayerModel.BakedModel implements ISmartItemModel, IPerspectiveAwareModel
    {

        private final ModelFluidBucket parent;
        private final Map<String, IFlexibleBakedModel> cache; // contains all the baked models since they'll never change
        private final ImmutableMap<TransformType, TRSRTransformation> transforms;

        public BakedDynBucket(ModelFluidBucket parent,
                              ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms,
                              Map<String, IFlexibleBakedModel> cache)
        {
            super(quads, particle, format);
            this.parent = parent;
            this.transforms = transforms;
            this.cache = cache;
        }

        @Override
        public IBakedModel handleItemState(ItemStack stack)
        {
            FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);
            if (fluidStack == null)
            {
                if (stack.getItem() instanceof IFluidContainerItem)
                {
                    fluidStack = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                }
            }

            String fluidName = "";
            if (fluidStack != null && fluidStack.getFluid() != null)
            {
                fluidName = fluidStack.getFluid().getName();
            }

            String material = "iron";
            if (stack.getItem() instanceof ItemFluidBucket)
            {
                BucketMaterial bucketMaterial = BucketMaterialHandler.getMaterial(stack.getItemDamage());
                if (bucketMaterial != null)
                {
                    material = bucketMaterial.materialName;
                }
            }

            String key = material + ":" + fluidName;

            if (!cache.containsKey(key))
            {
                IModel model = parent.process(ImmutableMap.of("fluid", fluidName, "material", material));
                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
                {
                    public TextureAtlasSprite apply(ResourceLocation location)
                    {
                        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                    }
                };

                IFlexibleBakedModel bakedModel = model.bake(new SimpleModelState(transforms), this.getFormat(), textureGetter);
                cache.put(key, bakedModel);
                return bakedModel;
            }

            return cache.get(key);
        }

        @Override
        public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
        {
            return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, cameraTransformType);
        }
    }
}