package io.prostomaslo.nmb.api;
import io.prostomaslo.nmb.model.CustomModel;
import io.prostomaslo.nmb.model.ModelCache;
import net.minecraft.resources.ResourceLocation;
public final class BackportAPI {
    private BackportAPI() {} 
    public static void register(ResourceLocation id, CustomModel model) {
        ModelCache.register(id, model);
    }
    public static void forceCustom(ResourceLocation id) {
        ModelCache.forceCustom(id);
    }
    public static boolean isCustomRendered(ResourceLocation id) {
        return ModelCache.requiresCustomRenderer(id);
    }
    public static CustomModel getModel(ResourceLocation id) {
        return ModelCache.get(id);
    }
    public static String getCacheStats() {
        return ModelCache.getStats();
    }
}
