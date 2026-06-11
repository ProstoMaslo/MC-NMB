package io.prostomaslo.nmb.model;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CustomModel {
    private final List<Cube> elements;
    private final Map<String, ResourceLocation> textures;
    private boolean requiresCustomRenderer;
    private ResourceLocation parent;
    private boolean forceCustom;
    private String sanitizedJson;
    private String renderType;
    private String formatVersion;
    private boolean ignoreLight;
    public CustomModel() {
        this.elements = new ArrayList<>();
        this.textures = new HashMap<>();
        this.requiresCustomRenderer = false;
        this.forceCustom = false;
        this.sanitizedJson = null;
        this.renderType = null;
        this.formatVersion = null;
        this.ignoreLight = false;
    }
    public boolean isIgnoreLight() {
        if (ignoreLight) return true;
        if (parent != null) {
            CustomModel p = ModelCache.get(parent);
            if (p != null) return p.isIgnoreLight();
        }
        return false;
    }
    public void setIgnoreLight(boolean ignoreLight) {
        this.ignoreLight = ignoreLight;
    }
    public void addElement(Cube cube) {
        elements.add(cube);
        if (cube.getRotation() != null && !cube.getRotation().isVanillaCompatible()) {
            requiresCustomRenderer = true;
        }
        if (cube.isIgnoreLight()) {
            requiresCustomRenderer = true;
        }
    }
    public List<Cube> getElements() {
        if (!elements.isEmpty()) return elements;
        if (parent != null) {
            CustomModel p = ModelCache.get(parent);
            if (p != null) return p.getElements();
        }
        return elements;
    }
    public void putTexture(String key, ResourceLocation textureId) {
        textures.put(key, textureId);
    }
    public Map<String, ResourceLocation> getTextures() {
        return textures;
    }
    public ResourceLocation resolveTexture(String ref) {
        if (ref == null) return null;
        String key = ref.startsWith("#") ? ref.substring(1) : ref;
        ResourceLocation id = textures.get(key);
        if (id != null) return id;
        if (parent != null) {
            CustomModel parentModel = ModelCache.get(parent);
            if (parentModel != null) {
                return parentModel.resolveTexture(ref);
            }
        }
        return null;
    }
    public boolean requiresCustomRenderer() {
        if (requiresCustomRenderer || forceCustom || "1.21.11".equals(getFormatVersion())) return true;
        if (parent != null) {
            CustomModel p = ModelCache.get(parent);
            if (p != null) return p.requiresCustomRenderer();
        }
        return false;
    }
    public void setRequiresCustomRenderer(boolean value) {
        this.requiresCustomRenderer = value;
    }
    public boolean isForceCustom() {
        return forceCustom;
    }
    public void setForceCustom(boolean forceCustom) {
        this.forceCustom = forceCustom;
        if (forceCustom) {
            this.requiresCustomRenderer = true;
        }
    }
    public ResourceLocation getParent() {
        return parent;
    }
    public void setParent(ResourceLocation parent) {
        this.parent = parent;
    }
    public String getSanitizedJson() {
        return sanitizedJson;
    }
    public void setSanitizedJson(String sanitizedJson) {
        this.sanitizedJson = sanitizedJson;
    }
    public String getRenderType() {
        if (renderType != null) return renderType;
        if (parent != null) {
            CustomModel p = ModelCache.get(parent);
            if (p != null) return p.getRenderType();
        }
        return null;
    }
    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }
    public String getFormatVersion() {
        if (formatVersion != null) return formatVersion;
        if (parent != null) {
            CustomModel p = ModelCache.get(parent);
            if (p != null) return p.getFormatVersion();
        }
        return null;
    }
    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }
    @Override
    public String toString() {
        return "CustomModel{elements=" + elements.size()
                + ", textures=" + textures.size()
                + ", customRenderer=" + requiresCustomRenderer
                + ", force=" + forceCustom + "}";
    }
}
