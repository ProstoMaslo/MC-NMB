package io.prostomaslo.nmb.loader;
import io.prostomaslo.nmb.NewModelsBackport;
import io.prostomaslo.nmb.model.CustomModel;
import io.prostomaslo.nmb.model.ModelCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
public class BackportResourceReloadListener implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(
            PreparationBarrier synchronizer,
            ResourceManager manager,
            ProfilerFiller prepareProfiler,
            ProfilerFiller applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            prepareProfiler.startTick();
            prepareProfiler.push("backport_model_scan");
            ModelCache.clear();
            int scanned = 0;
            scanned += scanModels(manager, "models");
            NewModelsBackport.LOGGER.info(
                    "[NMB] Resource scan complete: {} models scanned",
                    scanned);
            prepareProfiler.pop();
            prepareProfiler.endTick();
            return null;
        }, prepareExecutor).thenCompose(synchronizer::wait).thenAcceptAsync(v -> {
            applyProfiler.startTick();
            applyProfiler.push("backport_model_apply");
            NewModelsBackport.LOGGER.debug("[NMB] {}", ModelCache.getStats());
            applyProfiler.pop();
            applyProfiler.endTick();
        }, applyExecutor);
    }
    private int scanModels(ResourceManager manager, String prefix) {
        int count = 0;
        Map<ResourceLocation, Resource> resources = manager.listResources(prefix, 
                id -> id.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                String path = resourceId.getPath();
                String modelPath = path.substring("models/".length(), path.length() - ".json".length());
                ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), modelPath);
                CustomModel model = ModelJsonParser.parse(stream, modelId);
                if (model != null) {
                    ModelCache.put(modelId, model);
                    count++;
                }
            } catch (Exception e) {
                NewModelsBackport.LOGGER.warn("[NMB] Error scanning {}: {}",
                        resourceId, e.getMessage());
            }
        }
        return count;
    }
}
