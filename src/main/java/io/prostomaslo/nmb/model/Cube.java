package io.prostomaslo.nmb.model;
import java.util.EnumMap;
import java.util.Map;
public class Cube {
    private final float[] from;
    private final float[] to;
    private Rotation rotation;
    private final Map<FaceDirection, Face> faces;
    private boolean shade = true;
    private boolean ignoreLight = false;
    public Cube(float[] from, float[] to) {
        this.from = from;
        this.to = to;
        this.faces = new EnumMap<>(FaceDirection.class);
    }
    public float[] getFrom() {
        return from;
    }
    public float[] getTo() {
        return to;
    }
    public Rotation getRotation() {
        return rotation;
    }
    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }
    public Map<FaceDirection, Face> getFaces() {
        return faces;
    }
    public void addFace(FaceDirection direction, Face face) {
        faces.put(direction, face);
    }
    public boolean isShade() {
        return shade;
    }
    public void setShade(boolean shade) {
        this.shade = shade;
    }
    public boolean isIgnoreLight() {
        return ignoreLight;
    }
    public void setIgnoreLight(boolean ignoreLight) {
        this.ignoreLight = ignoreLight;
    }
    public float[] getCenter() {
        return new float[]{
                (from[0] + to[0]) / 2f,
                (from[1] + to[1]) / 2f,
                (from[2] + to[2]) / 2f
        };
    }
}
