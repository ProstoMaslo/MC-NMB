package io.prostomaslo.nmb;
import com.mojang.math.Transformation;
import io.prostomaslo.nmb.loader.BackportResourceReloadListener;
import io.prostomaslo.nmb.mixin.ModelBakeryAccessor;
import io.prostomaslo.nmb.model.CustomModel;
import io.prostomaslo.nmb.model.ModelCache;
import io.prostomaslo.nmb.renderer.BackportBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Mod.EventBusSubscriber(modid = NewModelsBackport.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NewModelsBackportClient {
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new BackportResourceReloadListener());
    }
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, UnbakedModel> topLevelModels =
                ((ModelBakeryAccessor) event.getModelBakery()).nmb$getTopLevelModels();
        for (Map.Entry<ResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            ResourceLocation id = entry.getKey();
            CustomModel customModel = findCustomModel(id);
            if (customModel != null && customModel.requiresCustomRenderer()) {
                BakedModel fallback = entry.getValue();
                Transformation blockstateTransform = Transformation.identity();
                UnbakedModel unbaked = topLevelModels.get(id);
                if (unbaked instanceof MultiVariant multiVariant && !multiVariant.getVariants().isEmpty()) {
                    blockstateTransform = multiVariant.getVariants().get(0).getRotation();
                }
                Map<ResourceLocation, TextureAtlasSprite> sprites = collectSprites(fallback);
                Function<ResourceLocation, TextureAtlasSprite> spriteGetter = sprites::get;
                entry.setValue(new BackportBakedModel(
                        customModel,
                        fallback,
                        spriteGetter,
                        blockstateTransform
                ));
                NewModelsBackport.LOGGER.info("[NMB] Replaced BakedModel for {}", id);
            }
        }
    }
    private static Map<ResourceLocation, TextureAtlasSprite> collectSprites(BakedModel model) {
        Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
        RandomSource random = RandomSource.create(0L);
        collectSprites(model.getQuads(null, null, random), sprites);
        for (Direction direction : Direction.values()) {
            random.setSeed(0L);
            collectSprites(model.getQuads(null, direction, random), sprites);
        }
        TextureAtlasSprite particle = model.getParticleIcon();
        sprites.putIfAbsent(particle.contents().name(), particle);
        return sprites;
    }
    private static void collectSprites(
            Iterable<BakedQuad> quads,
            Map<ResourceLocation, TextureAtlasSprite> sprites
    ) {
        for (BakedQuad quad : quads) {
            TextureAtlasSprite sprite = quad.getSprite();
            sprites.putIfAbsent(sprite.contents().name(), sprite);
        }
    }
    private static ResourceLocation toModelId(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath());
    }
    private static CustomModel findCustomModel(ResourceLocation id) {
        ResourceLocation rl = toModelId(id);
        String ns = rl.getNamespace();
        String path = rl.getPath();
        String variant = id instanceof ModelResourceLocation modelId ? modelId.getVariant() : "inventory";
        if (path.startsWith("block/") || path.startsWith("item/")) {
            CustomModel model = ModelCache.get(rl);
            if (model != null) return model;
        }
        if (!variant.equals("inventory")) {
            CustomModel model = ModelCache.get(ResourceLocation.fromNamespaceAndPath(ns, "block/" + path));
            if (model != null) return model;
        }
        CustomModel itemModel = ModelCache.get(ResourceLocation.fromNamespaceAndPath(ns, "item/" + path));
        if (itemModel != null) return itemModel;
        if (variant.equals("inventory")) {
            CustomModel model = ModelCache.get(ResourceLocation.fromNamespaceAndPath(ns, "block/" + path));
            if (model != null) return model;
        }
        return ModelCache.get(rl);
    }
}
