package io.prostomaslo.nmb;
import io.prostomaslo.nmb.loader.BackportResourceReloadListener;
import io.prostomaslo.nmb.model.CustomModel;
import io.prostomaslo.nmb.model.ModelCache;
import io.prostomaslo.nmb.renderer.BackportBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import java.util.Map;
import java.util.function.Function;
@EventBusSubscriber(modid = NewModelsBackport.MOD_ID, value = Dist.CLIENT)
public class NewModelsBackportClient {
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new BackportResourceReloadListener());
    }
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Function<Material, TextureAtlasSprite> textureGetter = event.getTextureGetter();
        Function<ResourceLocation, TextureAtlasSprite> spriteGetter = id -> textureGetter.apply(new Material(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS, id));
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            ModelResourceLocation id = entry.getKey();
            CustomModel customModel = findCustomModel(id);
            if (customModel != null && customModel.requiresCustomRenderer()) {
                BakedModel fallback = entry.getValue();
                com.mojang.math.Transformation blockstateTransform = com.mojang.math.Transformation.identity();
                try {
                    java.lang.reflect.Field field = net.minecraft.client.resources.model.ModelBakery.class.getDeclaredField("topLevelModels");
                    field.setAccessible(true);
                    java.util.Map<ModelResourceLocation, net.minecraft.client.resources.model.UnbakedModel> topLevelModels = 
                            (java.util.Map<ModelResourceLocation, net.minecraft.client.resources.model.UnbakedModel>) field.get(event.getModelBakery());
                    net.minecraft.client.resources.model.UnbakedModel unbaked = topLevelModels.get(id);
                    if (unbaked instanceof net.minecraft.client.renderer.block.model.MultiVariant multiVariant) {
                        if (!multiVariant.getVariants().isEmpty()) {
                            blockstateTransform = multiVariant.getVariants().get(0).getRotation();
                        }
                    }
                } catch (Exception e) {
                    NewModelsBackport.LOGGER.error("Failed to extract blockstate transform for {}", id, e);
                }
                entry.setValue(new BackportBakedModel(customModel, fallback, id.id(), spriteGetter, blockstateTransform));
                NewModelsBackport.LOGGER.info("[NMB] Replaced BakedModel for {}", id);
            }
        }
    }
    private static CustomModel findCustomModel(ModelResourceLocation id) {
        ResourceLocation rl = id.id();
        String ns = rl.getNamespace();
        String path = rl.getPath();
        if (path.startsWith("block/") || path.startsWith("item/")) {
            CustomModel model = ModelCache.get(rl);
            if (model != null) return model;
        }
        if (!id.variant().equals("inventory")) {
            CustomModel model = ModelCache.get(ResourceLocation.fromNamespaceAndPath(ns, "block/" + path));
            if (model != null) return model;
        }
        {
            CustomModel model = ModelCache.get(ResourceLocation.fromNamespaceAndPath(ns, "item/" + path));
            if (model != null) return model;
        }
        if (id.variant().equals("inventory")) {
            CustomModel model = ModelCache.get(ResourceLocation.fromNamespaceAndPath(ns, "block/" + path));
            if (model != null) return model;
        }
        return ModelCache.get(rl);
    }
}
