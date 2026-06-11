package io.prostomaslo.nmb.mixin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(BlockElement.Deserializer.class)
public class BlockElementRotationMixin {
    @ModifyVariable(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElement;", at = @At("HEAD"), argsOnly = true)
    private JsonElement modifyJson(JsonElement json) {
        if (json != null && json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("rotation")) {
                JsonObject rot = obj.getAsJsonObject("rotation");
                boolean needsSanitize = false;
                if (rot.has("x") || rot.has("z") || (rot.has("y") && rot.has("x"))) {
                    needsSanitize = true;
                }
                if (rot.has("angle")) {
                    float angle = rot.get("angle").getAsFloat();
                    if (angle != 0 && Math.abs(angle) != 22.5f && Math.abs(angle) != 45f) {
                        needsSanitize = true;
                    }
                }
                if (needsSanitize) {
                    JsonObject safeRot = rot.deepCopy();
                    safeRot.addProperty("angle", 0.0f);
                    safeRot.addProperty("axis", "y");
                    safeRot.remove("x");
                    safeRot.remove("z");
                    JsonObject safe = obj.deepCopy();
                    safe.add("rotation", safeRot);
                    return safe;
                }
            }
        }
        return json;
    }
}
