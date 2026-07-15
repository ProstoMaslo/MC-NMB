package io.prostomaslo.nmb.renderer;
import com.mojang.math.Transformation;
import io.prostomaslo.nmb.model.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
public class BackportBakedModel implements BakedModel {
    private final BakedModel fallbackModel;
    private final List<BakedQuad> generalQuads;
    private final Map<Direction, List<BakedQuad>> faceQuads = new EnumMap<>(Direction.class);
    private final CustomModel customModel;
    private final TextureAtlasSprite particleTextureAtlasSprite;
    private final Function<ResourceLocation, TextureAtlasSprite> spriteGetter;
    private final Transformation blockstateTransform;
    private static final int VERTEX_SIZE = 8;
    private static final int QUAD_SIZE = 4 * VERTEX_SIZE;
    public BackportBakedModel(CustomModel customModel, BakedModel fallbackModel, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, Transformation blockstateTransform) {
        this.fallbackModel = fallbackModel;
        this.customModel = customModel;
        this.spriteGetter = spriteGetter;
        this.blockstateTransform = blockstateTransform;
        this.particleTextureAtlasSprite = fallbackModel.getParticleIcon();
        for (Direction dir : Direction.values()) {
            faceQuads.put(dir, new ArrayList<>());
        }
        this.generalQuads = new ArrayList<>();
        buildAllQuads(customModel);
        ((ArrayList<BakedQuad>) this.generalQuads).trimToSize();
        for (List<BakedQuad> quads : faceQuads.values()) {
            ((ArrayList<BakedQuad>) quads).trimToSize();
        }
    }
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        if (face == null) return generalQuads;
        return faceQuads.getOrDefault(face, Collections.emptyList());
    }
    @Override
    public boolean useAmbientOcclusion() { return fallbackModel.useAmbientOcclusion(); }
    @Override
    public boolean isGui3d() { return !customModel.getElements().isEmpty() || fallbackModel.isGui3d(); }
    @Override
    public boolean usesBlockLight() { return !customModel.getElements().isEmpty() || fallbackModel.usesBlockLight(); }
    @Override
    public boolean isCustomRenderer() { return fallbackModel.isCustomRenderer(); }
    @Override
    public TextureAtlasSprite getParticleIcon() { return particleTextureAtlasSprite; }
    @Override
    public ItemTransforms getTransforms() { return fallbackModel.getTransforms(); }
    @Override
    public ItemOverrides getOverrides() { return fallbackModel.getOverrides(); }
    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        String rt = customModel.getRenderType();
        if ("translucent".equals(rt) || "minecraft:translucent".equals(rt)) {
            return ChunkRenderTypeSet.of(RenderType.translucent());
        } else if ("cutout".equals(rt) || "minecraft:cutout".equals(rt)) {
            return ChunkRenderTypeSet.of(RenderType.cutout());
        } else if ("cutout_mipped".equals(rt) || "minecraft:cutout_mipped".equals(rt)) {
            return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
        }
        return fallbackModel.getRenderTypes(state, rand, data);
    }
    @Override
    public List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) {
        if (customModel.isIgnoreLight()) {
            return java.util.List.of(
                RenderType.entityCutout(
                    InventoryMenu.BLOCK_ATLAS
                ),
                RenderType.entityTranslucentEmissive(
                    InventoryMenu.BLOCK_ATLAS
                )
            );
        }
        return fallbackModel.getRenderTypes(itemStack, fabulous);
    }
    private void buildAllQuads(CustomModel customModel) {
        boolean forceIgnoreLight = customModel.isIgnoreLight();
        for (Cube cube : customModel.getElements()) {
            buildCubeQuads(cube, customModel, forceIgnoreLight);
        }
    }
    private void buildCubeQuads(Cube cube, CustomModel customModel, boolean forceIgnoreLight) {
        float[] from = cube.getFrom();
        float[] to = cube.getTo();
        float x0 = from[0] / 16f;
        float y0 = from[1] / 16f;
        float z0 = from[2] / 16f;
        float x1 = to[0] / 16f;
        float y1 = to[1] / 16f;
        float z1 = to[2] / 16f;
        Rotation rotation = cube.getRotation();
        for (Map.Entry<FaceDirection, Face> entry : cube.getFaces().entrySet()) {
            FaceDirection dir = entry.getKey();
            Face face = entry.getValue();
            float[][] vertices = dir.getVertices(x0, y0, z0, x1, y1, z1);
            if (rotation != null && (!rotation.isVanillaCompatible() || rotation.getAngle() != 0)) {
                applyRotation(vertices, rotation);
            }
            if (blockstateTransform != null && !blockstateTransform.isIdentity()) {
                Matrix4f matrix = blockstateTransform.getMatrix();
                for (float[] v : vertices) {
                    Vector4f vec = new Vector4f(v[0] - 0.5f, v[1] - 0.5f, v[2] - 0.5f, 1.0f);
                    matrix.transform(vec);
                    v[0] = vec.x() + 0.5f;
                    v[1] = vec.y() + 0.5f;
                    v[2] = vec.z() + 0.5f;
                }
            }
            float[] uv = face.getNormalizedUv();
            float[] normal = computeNormal(vertices);
            ResourceLocation texResourceLocation = customModel.resolveTexture(face.getTexture());
            TextureAtlasSprite sprite = null;
            if (texResourceLocation != null && spriteGetter != null) {
                sprite = spriteGetter.apply(texResourceLocation);
            }
            if (sprite == null) sprite = particleTextureAtlasSprite;
            int[] vertexData = buildQuadVertexData(vertices, uv, normal, sprite, cube.isIgnoreLight() || forceIgnoreLight, face.getUvRotation());
            BakedQuad quad = new BakedQuad(
                    vertexData,
                    face.getTintIndex(),
                    directionFromNormal(normal),
                    sprite,
                    !(cube.isIgnoreLight() || forceIgnoreLight) && cube.isShade(),
                    !(cube.isIgnoreLight() || forceIgnoreLight) && cube.isShade()
            );
            if (face.getCullface() != null) {
                Direction faceCull = face.getCullface().toVanilla();
                if (faceCull != null) {
                    if (blockstateTransform != null && !blockstateTransform.isIdentity()) {
                        faceCull = Direction.rotate(blockstateTransform.getMatrix(), faceCull);
                    }
                    faceQuads.get(faceCull).add(quad);
                    continue;
                }
            }
            generalQuads.add(quad);
        }
    }
    private void applyRotation(float[][] vertices, Rotation rotation) {
        float[] origin = rotation.getOrigin();
        float ox = origin[0] / 16f, oy = origin[1] / 16f, oz = origin[2] / 16f;
        for (float[] v : vertices) { v[0] -= ox; v[1] -= oy; v[2] -= oz; }
        if (rotation.is3D()) {
            if (rotation.getAngleX() != 0) applyRotationAxis(vertices, "x", rotation.getAngleX());
            if (rotation.getAngleY() != 0) applyRotationAxis(vertices, "y", rotation.getAngleY());
            if (rotation.getAngleZ() != 0) applyRotationAxis(vertices, "z", rotation.getAngleZ());
        } else {
            if (rotation.getAngle() != 0) applyRotationAxis(vertices, rotation.getAxis(), rotation.getAngle());
        }
        for (float[] v : vertices) { v[0] += ox; v[1] += oy; v[2] += oz; }
        if (rotation.isRescale() && !rotation.is3D()) {
            float angleRad = (float) Math.toRadians(rotation.getAngle());
            float scale = 1.0f / Math.max(Math.abs((float)Math.cos(angleRad)), Math.abs((float)Math.sin(angleRad)));
            for (float[] v : vertices) {
                v[0] = ox + (v[0] - ox) * scale;
                v[1] = oy + (v[1] - oy) * scale;
                v[2] = oz + (v[2] - oz) * scale;
            }
        }
    }
    private void applyRotationAxis(float[][] vertices, String axis, float angle) {
        float angleRad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(angleRad), sin = (float) Math.sin(angleRad);
        for (float[] v : vertices) {
            float x = v[0], y = v[1], z = v[2];
            switch (axis) {
                case "x" -> { v[1] = y * cos - z * sin; v[2] = y * sin + z * cos; }
                case "y" -> { v[0] = x * cos + z * sin; v[2] = -x * sin + z * cos; }
                case "z" -> { v[0] = x * cos - y * sin; v[1] = x * sin + y * cos; }
            }
        }
    }
    private void applyUvRotation(float[][] uvCorners, int rotation) {
        if (rotation == 0) return;
        int shifts = rotation / 90;
        float[][] original = new float[4][2];
        for (int i = 0; i < 4; i++) {
            original[i][0] = uvCorners[i][0];
            original[i][1] = uvCorners[i][1];
        }
        for (int i = 0; i < 4; i++) {
            int shifted = (i + shifts) % 4;
            uvCorners[i][0] = original[shifted][0];
            uvCorners[i][1] = original[shifted][1];
        }
    }
    private float[] computeNormal(float[][] v) {
        float e1x = v[1][0] - v[0][0], e1y = v[1][1] - v[0][1], e1z = v[1][2] - v[0][2];
        float e2x = v[2][0] - v[0][0], e2y = v[2][1] - v[0][1], e2z = v[2][2] - v[0][2];
        float nx = e1y * e2z - e1z * e2y, ny = e1z * e2x - e1x * e2z, nz = e1x * e2y - e1y * e2x;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.0001f) { nx /= len; ny /= len; nz /= len; }
        return new float[]{nx, ny, nz};
    }
    private int[] buildQuadVertexData(float[][] positions, float[] uv, float[] normal, TextureAtlasSprite sprite, boolean ignoreLight, int uvRotation) {
        int[] data = new int[QUAD_SIZE];
        float[][] uvCorners = { {uv[0], uv[1]}, {uv[0], uv[3]}, {uv[2], uv[3]}, {uv[2], uv[1]} };
        applyUvRotation(uvCorners, uvRotation);
        int packedNormal = packNormal(normal[0], normal[1], normal[2]);
        int packedLight = ignoreLight ? LightTexture.pack(15, 15) : 0;
        for (int i = 0; i < 4; i++) {
            int offset = i * VERTEX_SIZE;
            data[offset] = Float.floatToRawIntBits(positions[i][0]);
            data[offset + 1] = Float.floatToRawIntBits(positions[i][1]);
            data[offset + 2] = Float.floatToRawIntBits(positions[i][2]);
            data[offset + 3] = 0xFFFFFFFF;
            data[offset + 4] = Float.floatToRawIntBits(sprite.getU(uvCorners[i][0]));
            data[offset + 5] = Float.floatToRawIntBits(sprite.getV(uvCorners[i][1]));
            data[offset + 6] = packedLight; 
            data[offset + 7] = packedNormal;
        }
        return data;
    }
    private int packNormal(float x, float y, float z) {
        return (((byte) (x * 127)) & 0xFF) | ((((byte) (y * 127)) & 0xFF) << 8) | ((((byte) (z * 127)) & 0xFF) << 16);
    }
    private Direction directionFromNormal(float[] normal) {
        float maxDot = -1; Direction best = Direction.NORTH;
        for (Direction dir : Direction.values()) {
            float dot = normal[0] * dir.getStepX() + normal[1] * dir.getStepY() + normal[2] * dir.getStepZ();
            if (dot > maxDot) { maxDot = dot; best = dir; }
        }
        return best;
    }
}
