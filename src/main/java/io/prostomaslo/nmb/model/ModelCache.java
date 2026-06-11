package io.prostomaslo.nmb.model;
import io.prostomaslo.nmb.NewModelsBackport;
import net.minecraft.resources.ResourceLocation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public final class ModelCache {
    private static final Map<ResourceLocation, CustomModel> CACHE = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> CUSTOM_RENDER_SET = ConcurrentHashMap.newKeySet();
    private static final Set<ResourceLocation> FORCE_CUSTOM_SET = ConcurrentHashMap.newKeySet();
    private static final Set<ResourceLocation> TEXTURE_DEPENDENCIES = ConcurrentHashMap.newKeySet();
    private ModelCache() {} 
    public static void put(ResourceLocation id, CustomModel model) {
        CACHE.put(id, model);
        TEXTURE_DEPENDENCIES.addAll(model.getTextures().values());
    }
    public static CustomModel get(ResourceLocation id) {
        return CACHE.get(id);
    }
    public static boolean requiresCustomRenderer(ResourceLocation id) {
        if (FORCE_CUSTOM_SET.contains(id)) return true;
        CustomModel model = CACHE.get(id);
        if (model != null) return model.requiresCustomRenderer();
        return false;
    }
    public static Map<ResourceLocation, CustomModel> getAll() {
        return Collections.unmodifiableMap(CACHE);
    }
    public static void forceCustom(ResourceLocation id) {
        FORCE_CUSTOM_SET.add(id);
        CustomModel model = CACHE.get(id);
        if (model != null) {
            model.setForceCustom(true);
        }
    }
    public static void register(ResourceLocation id, CustomModel model) {
        model.setForceCustom(true);
        CACHE.put(id, model);
        NewModelsBackport.LOGGER.debug("[NMB] API-registered custom model: {}", id);
    }
    public static void clear() {
        int prevSize = CACHE.size();
        CACHE.clear();
        TEXTURE_DEPENDENCIES.clear();
        NewModelsBackport.LOGGER.debug("[NMB] Cache cleared ({} models)", prevSize);
    }
    public static boolean isTextureUsed(ResourceLocation id) {
        return TEXTURE_DEPENDENCIES.contains(id);
    }
    public static Set<ResourceLocation> getAllUsedTextures() {
        return Collections.unmodifiableSet(TEXTURE_DEPENDENCIES);
    }
    public static String getStats() {
        return String.format("Cache: %d total, %d force-registered",
                CACHE.size(), FORCE_CUSTOM_SET.size());
    }
}
