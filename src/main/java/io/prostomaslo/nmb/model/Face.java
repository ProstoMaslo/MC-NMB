package io.prostomaslo.nmb.model;
public class Face {
    private final float[] uv;
    private final String texture;
    private final int uvRotation;
    private final FaceDirection cullface;
    private final int tintIndex;
    public Face(float[] uv, String texture, int uvRotation, FaceDirection cullface, int tintIndex) {
        this.uv = uv;
        this.texture = texture;
        this.uvRotation = uvRotation;
        this.cullface = cullface;
        this.tintIndex = tintIndex;
    }
    public float[] getUv() {
        return uv;
    }
    public String getTexture() {
        return texture;
    }
    public int getUvRotation() {
        return uvRotation;
    }
    public FaceDirection getCullface() {
        return cullface;
    }
    public int getTintIndex() {
        return tintIndex;
    }
}
