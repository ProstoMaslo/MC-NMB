package io.prostomaslo.nmb.loader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.prostomaslo.nmb.NewModelsBackport;
import io.prostomaslo.nmb.model.*;
import net.minecraft.resources.ResourceLocation;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
public class ModelJsonParser {
    public static CustomModel parse(InputStream stream, ResourceLocation modelId) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            CustomModel model = parseJson(root, modelId);
            JsonObject sanitizedRoot = root.deepCopy();
            String ns = modelId.getNamespace();
            if (sanitizedRoot.has("parent")) {
                sanitizedRoot.addProperty("parent", resolveResourceLocation(sanitizedRoot.get("parent").getAsString(), ns).toString());
            }
            if (sanitizedRoot.has("textures")) {
                JsonObject texObj = sanitizedRoot.getAsJsonObject("textures");
                for (Map.Entry<String, JsonElement> entry : texObj.entrySet()) {
                    String val = entry.getValue().getAsString();
                    if (!val.startsWith("#")) {
                        texObj.addProperty(entry.getKey(), resolveResourceLocation(val, ns).toString());
                    }
                }
            }
            if (sanitizedRoot.has("elements")) {
                JsonArray elements = sanitizedRoot.getAsJsonArray("elements");
                for (JsonElement elem : elements) {
                    JsonObject obj = elem.getAsJsonObject();
                    if (obj.has("rotation")) {
                        JsonObject origRot = obj.getAsJsonObject("rotation");
                        JsonObject safeRot = new JsonObject();
                        safeRot.addProperty("angle", 0.0f); 
                        safeRot.addProperty("axis", "y");
                        safeRot.add("origin", origRot.has("origin") ? origRot.get("origin") : new JsonArray());
                        obj.add("rotation", safeRot);
                    }
                    if (obj.has("faces")) {
                        JsonObject faces = obj.getAsJsonObject("faces");
                        for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                            JsonObject face = faceEntry.getValue().getAsJsonObject();
                            if (face.has("texture")) {
                                String tex = face.get("texture").getAsString();
                                if (!tex.startsWith("#")) {
                                    face.addProperty("texture", resolveResourceLocation(tex, ns).toString());
                                }
                            }
                        }
                    }
                }
            }
            model.setSanitizedJson(sanitizedRoot.toString());
            return model;
        } catch (Exception e) {
            NewModelsBackport.LOGGER.error("Failed to parse backport model json for {}", modelId, e);
            return null;
        }
    }
    public static CustomModel parseJson(JsonObject root, ResourceLocation modelId) {
        CustomModel model = new CustomModel();
        String ns = modelId.getNamespace();
        if (root.has("backport:force") && root.get("backport:force").getAsBoolean()) {
            model.setForceCustom(true);
        }
        if (root.has("render_type")) {
            model.setRenderType(root.get("render_type").getAsString());
        }
        if (root.has("parent")) {
            model.setParent(resolveResourceLocation(root.get("parent").getAsString(), ns));
        }
        if (root.has("format_version")) {
            model.setFormatVersion(root.get("format_version").getAsString());
        }
        if (root.has("textures")) {
            JsonObject texObj = root.getAsJsonObject("textures");
            for (Map.Entry<String, JsonElement> entry : texObj.entrySet()) {
                String val = entry.getValue().getAsString();
                if (!val.startsWith("#")) {
                    model.putTexture(entry.getKey(), resolveResourceLocation(val, ns));
                }
            }
        }
        boolean rootIgnoreLight = false;
        if (root.has("ignore_light")) {
            rootIgnoreLight = root.get("ignore_light").getAsBoolean();
            model.setIgnoreLight(rootIgnoreLight);
            if (rootIgnoreLight) {
                model.setForceCustom(true);
            }
        }
        if (root.has("elements")) {
            JsonArray elements = root.getAsJsonArray("elements");
            for (JsonElement elem : elements) {
                Cube cube = parseCube(elem.getAsJsonObject(), modelId, rootIgnoreLight);
                if (cube != null) {
                    model.addElement(cube);
                    if (cube.getRotation() != null) {
                        float angle = cube.getRotation().getAngle();
                        if (angle != 0 && Math.abs(angle) != 22.5f && Math.abs(angle) != 45f) {
                            model.setForceCustom(true);
                        }
                        if (cube.getRotation().hasMultiAxis()) {
                            model.setForceCustom(true);
                        }
                    }
                }
            }
        }
        return model;
    }
    private static Cube parseCube(JsonObject elemObj, ResourceLocation modelId, boolean defaultIgnoreLight) {
        try {
            float[] from = parseVec3(elemObj.getAsJsonArray("from"));
            float[] to = parseVec3(elemObj.getAsJsonArray("to"));
            Cube cube = new Cube(from, to);
            cube.setIgnoreLight(defaultIgnoreLight);
            if (elemObj.has("shade")) {
                cube.setShade(elemObj.get("shade").getAsBoolean());
            }
            if (elemObj.has("ignore_light")) {
                cube.setIgnoreLight(elemObj.get("ignore_light").getAsBoolean());
            }
            if (elemObj.has("rotation")) {
                cube.setRotation(parseRotation(elemObj.getAsJsonObject("rotation"), cube.getCenter()));
            }
            if (elemObj.has("faces")) {
                JsonObject facesObj = elemObj.getAsJsonObject("faces");
                for (Map.Entry<String, JsonElement> entry : facesObj.entrySet()) {
                    FaceDirection dir = FaceDirection.fromString(entry.getKey());
                    if (dir != null) {
                        cube.addFace(dir, parseFace(entry.getValue().getAsJsonObject(), modelId.getNamespace()));
                    }
                }
            }
            return cube;
        } catch (Exception e) { 
            NewModelsBackport.LOGGER.warn("Failed to parse cube in model {}: {}", modelId, e.getMessage());
            return null; 
        }
    }
    private static Rotation parseRotation(JsonObject rotObj, float[] defaultOrigin) {
        float[] origin = rotObj.has("origin") ? parseVec3(rotObj.getAsJsonArray("origin")) : defaultOrigin;
        boolean rescale = rotObj.has("rescale") && rotObj.get("rescale").getAsBoolean();
        if (rotObj.has("x") || rotObj.has("y") || rotObj.has("z")) {
            return new Rotation(
                rotObj.has("x") ? rotObj.get("x").getAsFloat() : 0f,
                rotObj.has("y") ? rotObj.get("y").getAsFloat() : 0f,
                rotObj.has("z") ? rotObj.get("z").getAsFloat() : 0f,
                origin, rescale
            );
        } else {
            return new Rotation(
                rotObj.has("axis") ? rotObj.get("axis").getAsString() : "y",
                rotObj.has("angle") ? rotObj.get("angle").getAsFloat() : 0f,
                origin, rescale
            );
        }
    }
    private static Face parseFace(JsonObject faceObj, String ns) {
        float[] uv = faceObj.has("uv") ? parseVec4(faceObj.getAsJsonArray("uv")) : new float[]{0, 0, 16, 16};
        String tex = faceObj.has("texture") ? faceObj.get("texture").getAsString() : "#missing";
        if (!tex.startsWith("#")) {
            tex = resolveResourceLocation(tex, ns).toString();
        }
        int rotation = faceObj.has("rotation") ? faceObj.get("rotation").getAsInt() : 0;
        FaceDirection cullface = null;
        if (faceObj.has("cullface")) {
            cullface = FaceDirection.fromString(faceObj.get("cullface").getAsString());
        }
        int tintindex = faceObj.has("tintindex") ? faceObj.get("tintindex").getAsInt() : -1;
        return new Face(uv, tex, rotation, cullface, tintindex);
    }
    private static float[] parseVec3(JsonArray arr) {
        return new float[]{ arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat() };
    }
    private static float[] parseVec4(JsonArray arr) {
        return new float[]{ arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat(), arr.get(3).getAsFloat() };
    }
    private static ResourceLocation resolveResourceLocation(String value, String currentNamespace) {
        if (value.contains(":")) {
            return ResourceLocation.parse(value);
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", value);
    }
}
