package io.prostomaslo.nmb.model;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
public enum FaceDirection {
    NORTH, SOUTH, EAST, WEST, UP, DOWN;
    public static FaceDirection fromString(String name) {
        if (name == null) return null;
        switch (name.toLowerCase()) {
            case "north": return NORTH;
            case "south": return SOUTH;
            case "east": return EAST;
            case "west": return WEST;
            case "up": return UP;
            case "down": return DOWN;
        }
        return null;
    }
    public float[][] getVertices(float x0, float y0, float z0, float x1, float y1, float z1) {
        switch (this) {
            case NORTH: return new float[][]{ {x1, y1, z0}, {x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0} };
            case SOUTH: return new float[][]{ {x0, y1, z1}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1} };
            case EAST: return new float[][]{ {x1, y1, z1}, {x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0} };
            case WEST: return new float[][]{ {x0, y1, z0}, {x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1} };
            case UP: return new float[][]{ {x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0} };
            case DOWN: return new float[][]{ {x0, y0, z1}, {x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1} };
        }
        return new float[0][0];
    }
    @Nullable
    public Direction toVanilla() {
        switch (this) {
            case NORTH: return Direction.NORTH;
            case SOUTH: return Direction.SOUTH;
            case EAST: return Direction.EAST;
            case WEST: return Direction.WEST;
            case UP: return Direction.UP;
            case DOWN: return Direction.DOWN;
        }
        return null;
    }
}
