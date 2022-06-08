package me.andrew.gravitychanger.util;

import net.minecraft.util.math.*;

public abstract class RotationUtil {
    private static final Direction[][] DIR_WORLD_TO_PLAYER = new Direction[6][];
    static {
        for(Direction gravityDirection : Direction.values()) {
            DIR_WORLD_TO_PLAYER[gravityDirection.getId()] = new Direction[6];
            for(Direction direction : Direction.values()) {
                Vec3d directionVector = Vec3d.of(direction.getVector());
                directionVector = RotationUtil.vecWorldToPlayer(directionVector, gravityDirection);
                DIR_WORLD_TO_PLAYER[gravityDirection.getId()][direction.getId()] = Direction.fromVector(new BlockPos(directionVector));
            }
        }
    }

    public static Direction dirWorldToPlayer(Direction direction, Direction gravityDirection) {
        return DIR_WORLD_TO_PLAYER[gravityDirection.getId()][direction.getId()];
    }

    private static final Direction[][] DIR_PLAYER_TO_WORLD = new Direction[6][];
    static {
        for(Direction gravityDirection : Direction.values()) {
            DIR_PLAYER_TO_WORLD[gravityDirection.getId()] = new Direction[6];
            for(Direction direction : Direction.values()) {
                Vec3d directionVector = Vec3d.of(direction.getVector());
                directionVector = RotationUtil.vecPlayerToWorld(directionVector, gravityDirection);
                DIR_PLAYER_TO_WORLD[gravityDirection.getId()][direction.getId()] = Direction.fromVector(new BlockPos(directionVector));
            }
        }
    }

    public static Direction dirPlayerToWorld(Direction direction, Direction gravityDirection) {
        return DIR_PLAYER_TO_WORLD[gravityDirection.getId()][direction.getId()];
    }

    public static Vec3d vecWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN  -> new Vec3d( x,  y,  z);
            case UP    -> new Vec3d(-x, -y,  z);
            case NORTH -> new Vec3d( x,  z, -y);
            case SOUTH -> new Vec3d(-x, -z, -y);
            case WEST  -> new Vec3d(-z,  x, -y);
            case EAST  -> new Vec3d( z, -x, -y);
        };
    }

    public static Vec3d vecWorldToPlayer(Vec3d vec3d, Direction gravityDirection) {
        return vecWorldToPlayer(vec3d.x, vec3d.y, vec3d.z, gravityDirection);
    }

    public static Vec3d vecPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN  -> new Vec3d( x,  y,  z);
            case UP    -> new Vec3d(-x, -y,  z);
            case NORTH -> new Vec3d( x, -z,  y);
            case SOUTH -> new Vec3d(-x, -z, -y);
            case WEST  -> new Vec3d( y, -z, -x);
            case EAST  -> new Vec3d(-y, -z,  x);
        };
    }

    public static Vec3d vecPlayerToWorld(Vec3d vec3d, Direction gravityDirection) {
        return vecPlayerToWorld(vec3d.x, vec3d.y, vec3d.z, gravityDirection);
    }

    public static Vec3f vecWorldToPlayer(float x, float y, float z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN  -> new Vec3f( x,  y,  z);
            case UP    -> new Vec3f(-x, -y,  z);
            case NORTH -> new Vec3f( x,  z, -y);
            case SOUTH -> new Vec3f(-x, -z, -y);
            case WEST  -> new Vec3f(-z,  x, -y);
            case EAST  -> new Vec3f( z, -x, -y);
        };
    }

    public static Vec3f vecWorldToPlayer(Vec3f vec3f, Direction gravityDirection) {
        return vecWorldToPlayer(vec3f.getX(), vec3f.getY(), vec3f.getZ(), gravityDirection);
    }

    public static Vec3f vecPlayerToWorld(float x, float y, float z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN  -> new Vec3f( x,  y,  z);
            case UP    -> new Vec3f(-x, -y,  z);
            case NORTH -> new Vec3f( x, -z,  y);
            case SOUTH -> new Vec3f(-x, -z, -y);
            case WEST  -> new Vec3f( y, -z, -x);
            case EAST  -> new Vec3f(-y, -z,  x);
        };
    }

    public static Vec3f vecPlayerToWorld(Vec3f vec3f, Direction gravityDirection) {
        return vecPlayerToWorld(vec3f.getX(), vec3f.getY(), vec3f.getZ(), gravityDirection);
    }

    public static Vec3d maskWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN , UP    -> new Vec3d(x, y, z);
            case NORTH, SOUTH -> new Vec3d(x, z, y);
            case WEST , EAST  -> new Vec3d(z, x, y);
        };
    }

    public static Vec3d maskWorldToPlayer(Vec3d vec3d, Direction gravityDirection) {
        return maskWorldToPlayer(vec3d.x, vec3d.y, vec3d.z, gravityDirection);
    }

    public static Vec3d maskPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN , UP    -> new Vec3d(x, y, z);
            case NORTH, SOUTH -> new Vec3d(x, z, y);
            case WEST , EAST  -> new Vec3d(y, z, x);
        };
    }

    public static Vec3d maskPlayerToWorld(Vec3d vec3d, Direction gravityDirection) {
        return maskPlayerToWorld(vec3d.x, vec3d.y, vec3d.z, gravityDirection);
    }

    public static Box boxWorldToPlayer(Box box, Direction gravityDirection) {
        return new Box(
                RotationUtil.vecWorldToPlayer(box.minX, box.minY, box.minZ, gravityDirection),
                RotationUtil.vecWorldToPlayer(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static Box boxPlayerToWorld(Box box, Direction gravityDirection) {
        return new Box(
                RotationUtil.vecPlayerToWorld(box.minX, box.minY, box.minZ, gravityDirection),
                RotationUtil.vecPlayerToWorld(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static Vec2f rotWorldToPlayer(float yaw, float pitch, Direction gravityDirection) {
        Vec3d vec3d = RotationUtil.vecWorldToPlayer(rotToVec(yaw, pitch), gravityDirection);
        return vecToRot(vec3d.x, vec3d.y, vec3d.z);
    }

    public static Vec2f rotWorldToPlayer(Vec2f vec2f, Direction gravityDirection) {
        return rotWorldToPlayer(vec2f.x, vec2f.y, gravityDirection);
    }

    public static Vec2f rotPlayerToWorld(float yaw, float pitch, Direction gravityDirection) {
        Vec3d vec3d = RotationUtil.vecPlayerToWorld(rotToVec(yaw, pitch), gravityDirection);
        return vecToRot(vec3d.x, vec3d.y, vec3d.z);
    }

    public static Vec2f rotPlayerToWorld(Vec2f vec2f, Direction gravityDirection) {
        return rotPlayerToWorld(vec2f.x, vec2f.y, gravityDirection);
    }

    public static Vec3d rotToVec(float yaw, float pitch) {
        double radPitch = pitch * 0.017453292;
        double radNegYaw = -yaw * 0.017453292;
        double cosNegYaw = Math.cos(radNegYaw);
        double sinNegYaw = Math.sin(radNegYaw);
        double cosPitch = Math.cos(radPitch);
        double sinPitch = Math.sin(radPitch);
        return new Vec3d(sinNegYaw * cosPitch, -sinPitch, cosNegYaw * cosPitch);
    }

    public static Vec2f vecToRot(double x, double y, double z) {
        double sinPitch = -y;
        double radPitch = Math.asin(sinPitch);
        double cosPitch = Math.cos(radPitch);
        double sinNegYaw = x / cosPitch;
        double cosNegYaw = MathHelper.clamp(z / cosPitch, -1, 1);
        double radNegYaw = Math.acos(cosNegYaw);
        if(sinNegYaw < 0) radNegYaw = Math.PI * 2 - radNegYaw;

        return new Vec2f(MathHelper.wrapDegrees((float)(-radNegYaw) / 0.017453292F), (float)(radPitch) / 0.017453292F);
    }

    public static Vec2f vecToRot(Vec3d vec) {
        return vecToRot(vec.x, vec.y, vec.z);
    }

    public static Quaternion getWorldRotationQuaternion(Direction gravityDirection) {
        Quaternion r = getCameraRotationQuaternion(gravityDirection);
        QuaternionUtil.inverse(r);
        return r;
    }

    public static Quaternion getCameraRotationQuaternion(Direction gravityDirection) {
        Quaternion q1 = getRotationBetween(Direction.DOWN, gravityDirection);
        /*if(gravityDirection.getHorizontal() != -1)
            q1.hamiltonProduct(new Quaternion(Vec3f.NEGATIVE_Y, gravityDirection.getHorizontal()*90+180, true));*/
        return q1;
    }

    public static Quaternion getRotationBetween(Direction start, Direction end){
        return getRotationBetween(start.getUnitVector(), end.getUnitVector());
    }

    public static Quaternion getRotationBetween(Vec3f start, Vec3f end){
        Vec3f rotAxis = start.copy();
        rotAxis.cross(end);
        float rotAngle = (float)Math.acos(start.dot(end));
        //Make sure axis isn't {0, 0, 0}
        if(MathHelper.magnitude(rotAxis.getX(), rotAxis.getY(), rotAxis.getZ()) < 0.1 ) rotAxis = Vec3f.NEGATIVE_Z;
        return new Quaternion(rotAxis, rotAngle, false);
    }
}
