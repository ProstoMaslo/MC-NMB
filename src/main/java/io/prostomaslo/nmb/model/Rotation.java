package io.prostomaslo.nmb.model;
public class Rotation {
    private final String axis;
    private final float angle;
    private final float angleX;
    private final float angleY;
    private final float angleZ;
    private final boolean is3D;
    private final float[] origin;
    private final boolean rescale;
    public Rotation(String axis, float angle, float[] origin, boolean rescale) {
        this.axis = axis;
        this.angle = angle;
        this.angleX = 0;
        this.angleY = 0;
        this.angleZ = 0;
        this.is3D = false;
        this.origin = origin;
        this.rescale = rescale;
    }
    public Rotation(float x, float y, float z, float[] origin, boolean rescale) {
        this.axis = null;
        this.angle = 0;
        this.angleX = x;
        this.angleY = y;
        this.angleZ = z;
        this.is3D = true;
        this.origin = origin;
        this.rescale = rescale;
    }
    public String getAxis() {
        return axis;
    }
    public float getAngle() {
        return angle;
    }
    public float getAngleX() {
        return angleX;
    }
    public float getAngleY() {
        return angleY;
    }
    public float getAngleZ() {
        return angleZ;
    }
    public boolean is3D() {
        return is3D;
    }
    public float[] getOrigin() {
        return origin;
    }
    public boolean isRescale() {
        return rescale;
    }
    public boolean hasMultiAxis() {
        return is3D;
    }
    public boolean isVanillaCompatible() {
        if (is3D) {
            return angleX == 0 && angleY == 0 && angleZ == 0;
        }
        if (angle == 0.0f) return true;
        float divided = angle / 22.5f;
        return Math.abs(divided - Math.round(divided)) < 0.001f
                && Math.abs(angle) <= 45.0f;
    }
    @Override
    public String toString() {
        if (is3D) {
            return String.format("Rotation{3D, angles=[%.1f, %.1f, %.1f], origin=[%f, %f, %f], rescale=%b}", 
                angleX, angleY, angleZ, origin[0], origin[1], origin[2], rescale);
        }
        return "Rotation{axis=" + axis + ", angle=" + angle
                + ", origin=[" + origin[0] + "," + origin[1] + "," + origin[2] + "]"
                + ", rescale=" + rescale
                + ", vanillaOK=" + isVanillaCompatible() + "}";
    }
}
