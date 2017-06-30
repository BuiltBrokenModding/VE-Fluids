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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Clone of {@link net.minecraftforge.client.model.ModelDynBucket} to be more customized towards the application of VE's bucket
 * Though a lot of the code is custom All credit goes to the orginal creator plus fry, lex, and anyone else.
 */
public class ModelFluidBucket implements IModel, IModelCustomData
{
    public static final ResourceLocation default_fluid_texture = new ResourceLocation(FluidModule.DOMAIN, "items/bucket.fluid2");
    public static final ResourceLocation default_bucket_texture = new ResourceLocation(FluidModule.DOMAIN, "items/bucket.missing");

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
        builder.add(default_bucket_texture);

        for (BucketMaterial material : BucketMaterialHandler.getMaterials())
        {
            if (material.getBucketResourceLocation() != null)
            {
                builder.add(material.getBucketResourceLocation());
            }
            if (material.getFluidResourceLocation() != null)
            {
                builder.add(material.getFluidResourceLocation());
            }
        }

        return builder.build();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = IPerspectiveAwareModel.MapWrapper.getTransforms(state);

        if(transformMap.isEmpty())
        {
            TRSRTransformation thirdperson = get(0, 3, 1, 0, 0, 0, 0.55f);
            TRSRTransformation firstperson = get(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f);
            ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();
            builder.put(TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.5f));
            builder.put(TransformType.HEAD, get(0, 13, 7, 0, 180, 0, 1));
            builder.put(TransformType.THIRD_PERSON_RIGHT_HAND, thirdperson);
            builder.put(TransformType.THIRD_PERSON_LEFT_HAND, leftify(thirdperson));
            builder.put(TransformType.FIRST_PERSON_RIGHT_HAND, firstperson);
            builder.put(TransformType.FIRST_PERSON_LEFT_HAND, leftify(firstperson));
            transformMap = IPerspectiveAwareModel.MapWrapper.getTransforms(new SimpleModelState(builder.build()));
        }

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
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(baseLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }
        if (liquidLocation != null && fluidSprite != null)
        {
            TextureAtlasSprite liquid = bakedTextureGetter.apply(liquidLocation);
            // build liquid layer (inside)
            builder.addAll(ItemTextureQuadConverter.convertTexture(format, transform, liquid, fluidSprite, NORTH_Z_FLUID, EnumFacing.NORTH, fluid.getColor()));
            builder.addAll(ItemTextureQuadConverter.convertTexture(format, transform, liquid, fluidSprite, SOUTH_Z_FLUID, EnumFacing.SOUTH, fluid.getColor()));
        }

        return new BakedDynBucket(this, builder.build(), fluidSprite, format, Maps.immutableEnumMap(transformMap), Maps.<String, IBakedModel>newHashMap());
    }

    private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s)
    {
        return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                new Vector3f(tx / 16, ty / 16, tz / 16),
                TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
                new Vector3f(s, s, s),
                null));
    }

    private static final TRSRTransformation flipX = new TRSRTransformation(null, null, new Vector3f(-1, 1, 1), null);

    private static TRSRTransformation leftify(TRSRTransformation transform)
    {
        return TRSRTransformation.blockCenterToCorner(flipX.compose(TRSRTransformation.blockCornerToCenter(transform)).compose(flipX));
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

    private static final class BakedDynBucketOverrideHandler extends ItemOverrideList
    {
        public static final BakedDynBucketOverrideHandler INSTANCE = new BakedDynBucketOverrideHandler();

        private BakedDynBucketOverrideHandler()
        {
            super(ImmutableList.<ItemOverride>of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
        {
            ModelFluidBucket.BakedDynBucket model = (ModelFluidBucket.BakedDynBucket) originalModel;

            //Get fluid from container
            FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);
            if (fluidStack == null)
            {
                if (stack.getItem() instanceof IFluidContainerItem)
                {
                    fluidStack = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                }
            }

            //Get fluid name for key
            String fluidName = "";
            if (fluidStack != null && fluidStack.getFluid() != null)
            {
                fluidName = fluidStack.getFluid().getName();
            }

            //Get material name for key
            String material = "iron";
            if (stack.getItem() instanceof ItemFluidBucket)
            {
                BucketMaterial bucketMaterial = BucketMaterialHandler.getMaterial(stack.getItemDamage());
                if (bucketMaterial != null)
                {
                    material = bucketMaterial.materialName;
                }
            }

            //Create key for cache
            String key = material + ":" + fluidName;

            //Populate cached value if it doesn't exist
            if (!model.cache.containsKey(key))
            {
                IModel parent = model.parent.process(ImmutableMap.of("fluid", fluidName, "material", material));
                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
                {
                    public TextureAtlasSprite apply(ResourceLocation location)
                    {
                        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                    }
                };

                IBakedModel bakedModel = parent.bake(new SimpleModelState(model.transforms), model.format, textureGetter);
                model.cache.put(key, bakedModel);
                return bakedModel;
            }

            return model.cache.get(key);
        }
    }

    // the dynamic bucket is based on the empty bucket
    private static final class BakedDynBucket implements IPerspectiveAwareModel
    {

        private final ModelFluidBucket parent;
        // FIXME: guava cache?
        private final Map<String, IBakedModel> cache; // contains all the baked models since they'll never change
        private final ImmutableMap<TransformType, TRSRTransformation> transforms;
        private final ImmutableList<BakedQuad> quads;
        private final TextureAtlasSprite particle;
        private final VertexFormat format;

        public BakedDynBucket(ModelFluidBucket parent,
                              ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms,
                              Map<String, IBakedModel> cache)
        {
            this.quads = quads;
            this.particle = particle;
            this.format = format;
            this.parent = parent;
            this.transforms = transforms;
            this.cache = cache;
        }

        @Override
        public ItemOverrideList getOverrides()
        {
            return BakedDynBucketOverrideHandler.INSTANCE;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
        {
            return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, cameraTransformType);
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
        {
            if (side == null)
            {
                return quads;
            }
            return ImmutableList.of();
        }

        @Override
        public boolean isAmbientOcclusion()
        {
            return true;
        }

        @Override
        public boolean isGui3d()
        {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer()
        {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture()
        {
            return particle;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms()
        {
            return ItemCameraTransforms.DEFAULT;
        }
    }
}