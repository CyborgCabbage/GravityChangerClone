package me.andrew.gravitychanger.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;


public abstract class RotationUtil {
    private static final double RADIANS_PER_DEGREE = Math.PI / 180.0;
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
        return getClosestDirection(vecWorldToPlayer(direction.getUnitVector(), gravityDirection));
        //return DIR_WORLD_TO_PLAYER[gravityDirection.getId()][direction.getId()];
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
        return getClosestDirection(vecPlayerToWorld(direction.getUnitVector(), gravityDirection));
        //return DIR_PLAYER_TO_WORLD[gravityDirection.getId()][direction.getId()];
    }

    public static Direction getClosestDirection(Vec3f vec){
        double dotMax = -1;
        Direction directionMax = Direction.DOWN;
        for (Direction direction : Direction.values()) {
            double dot = vec.dot(direction.getUnitVector());
            if(dot > dotMax){
                dotMax = dot;
                directionMax = direction;
            }
        }
        return directionMax;
    }

    public static Vec3d vecWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        /*return switch(gravityDirection) {
            case DOWN  -> new Vec3d( x, y, z);
            case UP    -> new Vec3d(-x,-y, z);
            case NORTH -> new Vec3d( x, z,-y);
            case SOUTH -> new Vec3d( x,-z, y);
            case WEST  -> new Vec3d(-y, x, z);
            case EAST  -> new Vec3d( y,-x, z);
        };*/
        return getWorldRotationQuaternion(gravityDirection).rotate(new Vec3d(x,y,z));
    }

    public static Vec3d vecWorldToPlayer(Vec3d vec3d, Direction gravityDirection) {
        return vecWorldToPlayer(vec3d.x, vec3d.y, vec3d.z, gravityDirection);
    }

    public static Vec3d vecPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        /*return switch(gravityDirection) {
            case DOWN  -> new Vec3d( x, y, z);
            case UP    -> new Vec3d(-x,-y, z);
            case NORTH -> new Vec3d( x,-z, y);
            case SOUTH -> new Vec3d( x, z,-y);
            case WEST  -> new Vec3d( y,-x, z);
            case EAST  -> new Vec3d(-y, x, z);
        };*/
        return getCameraRotationQuaternion(gravityDirection).rotate(new Vec3d(x, y, z));
    }

    public static Vec3d vecPlayerToWorld(Vec3d vec3d, Direction gravityDirection) {
        return vecPlayerToWorld(vec3d.x, vec3d.y, vec3d.z, gravityDirection);
    }

    public static Vec3f vecWorldToPlayer(float x, float y, float z, Direction gravityDirection) {
        /*return switch(gravityDirection) {
            case DOWN  -> new Vec3f( x, y, z);
            case UP    -> new Vec3f(-x,-y, z);
            case NORTH -> new Vec3f( x, z,-y);
            case SOUTH -> new Vec3f( x,-z, y);
            case WEST  -> new Vec3f(-y, x, z);
            case EAST  -> new Vec3f( y,-x, z);
        };*/
        return new Vec3f(getWorldRotationQuaternion(gravityDirection).rotate(new Vec3d(x,y,z)));
    }

    public static Vec3f vecWorldToPlayer(Vec3f vec3f, Direction gravityDirection) {
        return vecWorldToPlayer(vec3f.getX(), vec3f.getY(), vec3f.getZ(), gravityDirection);
    }

    public static Vec3f vecPlayerToWorld(float x, float y, float z, Direction gravityDirection) {
        /*return switch(gravityDirection) {
            case DOWN  -> new Vec3f( x, y, z);
            case UP    -> new Vec3f(-x,-y, z);
            case NORTH -> new Vec3f( x,-z, y);
            case SOUTH -> new Vec3f( x, z,-y);
            case WEST  -> new Vec3f( y,-x, z);
            case EAST  -> new Vec3f(-y, x, z);
        };*/
        return new Vec3f(getCameraRotationQuaternion(gravityDirection).rotate(new Vec3d(x,y,z)));
    }

    public static Vec3f vecPlayerToWorld(Vec3f vec3f, Direction gravityDirection) {
        return vecPlayerToWorld(vec3f.getX(), vec3f.getY(), vec3f.getZ(), gravityDirection);
    }

    public static Vec3d maskWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        /*return switch(gravityDirection) {
            case DOWN, UP     -> new Vec3d(x, y, z);
            case NORTH, SOUTH -> new Vec3d(x, z, y);
            case WEST, EAST   -> new Vec3d(y, x, z);
        };*/
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
        /*return switch(gravityDirection) {
            case DOWN, UP     -> new Vec3d(x, y, z);
            case NORTH, SOUTH -> new Vec3d(x, z, y);
            case WEST, EAST   -> new Vec3d(y, x, z);
        };*/
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
        double radPitch = pitch * RADIANS_PER_DEGREE;
        double radNegYaw = -yaw * RADIANS_PER_DEGREE;
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

        return new Vec2f(MathHelper.wrapDegrees((float)(-radNegYaw / RADIANS_PER_DEGREE)), (float)(radPitch / RADIANS_PER_DEGREE));
    }

    public static Vec2f vecToRot(Vec3d vec) {
        return vecToRot(vec.x, vec.y, vec.z);
    }

    public static QuaternionDouble getWorldRotationQuaternion(Direction gravityDirection) {
        QuaternionDouble r = getCameraRotationQuaternion(gravityDirection);
        QuaternionDouble.inverse(r);
        return r;
    }

    public static QuaternionDouble getCameraRotationQuaternion(Direction gravityDirection) {
        QuaternionDouble q = getRotationBetween(Direction.DOWN, gravityDirection);
        if(gravityDirection.getHorizontal() != -1)
            q.hamiltonProduct(new QuaternionDouble(new Vec3d(0.0, -1.0, 0.0), gravityDirection.getHorizontal()*90+180, true));
        return q;
    }

    public static QuaternionDouble getRotationBetween(Direction start, Direction end){
        return getRotationBetween(new Vec3d(start.getUnitVector()), new Vec3d(end.getUnitVector()));
    }

    public static QuaternionDouble getRotationBetween(Vec3d start, Vec3d end){
        Vec3d rotAxis = start.crossProduct(end);
        double rotAngle = Math.acos(start.dotProduct(end));
        //Make sure axis isn't {0, 0, 0}
        if(rotAxis.length() < 0.1 ) rotAxis = new Vec3d(0,0,-1);
        return new QuaternionDouble(rotAxis, rotAngle, false);
    }

    public static Vec3d oldVecPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN  -> new Vec3d( x,  y,  z);
            case UP    -> new Vec3d(-x, -y,  z);
            case NORTH -> new Vec3d( x, -z,  y);
            case SOUTH -> new Vec3d(-x, -z, -y);
            case WEST  -> new Vec3d( y, -z, -x);
            case EAST  -> new Vec3d(-y, -z,  x);
        };
    }

    public static Vec3d newVecPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        return getCameraRotationQuaternion(gravityDirection).rotate(new Vec3d(x, y, z));
    }

    public static Direction newDirPlayerToWorld(Direction direction, Direction gravityDirection) {
        return getClosestDirection(vecPlayerToWorld(direction.getUnitVector(), gravityDirection));
    }

    private static final Direction[][] OLD_DIR_PLAYER_TO_WORLD = new Direction[6][];
    static {
        for(Direction gravityDirection : Direction.values()) {
            OLD_DIR_PLAYER_TO_WORLD[gravityDirection.getId()] = new Direction[6];
            for(Direction direction : Direction.values()) {
                Vec3d directionVector = Vec3d.of(direction.getVector());
                directionVector = RotationUtil.oldVecPlayerToWorld(directionVector.x, directionVector.y, directionVector.z, gravityDirection);
                OLD_DIR_PLAYER_TO_WORLD[gravityDirection.getId()][direction.getId()] = Direction.fromVector(new BlockPos(directionVector));
            }
        }
    }

    public static Direction oldDirPlayerToWorld(Direction direction, Direction gravityDirection) {
        return OLD_DIR_PLAYER_TO_WORLD[gravityDirection.getId()][direction.getId()];
    }

    public static Direction newDirWorldToPlayer(Direction direction, Direction gravityDirection) {
        return getClosestDirection(vecWorldToPlayer(direction.getUnitVector(), gravityDirection));
    }

    private static final Direction[][] OLD_DIR_WORLD_TO_PLAYER = new Direction[6][];
    static {
        for(Direction gravityDirection : Direction.values()) {
            OLD_DIR_WORLD_TO_PLAYER[gravityDirection.getId()] = new Direction[6];
            for(Direction direction : Direction.values()) {
                Vec3d directionVector = Vec3d.of(direction.getVector());
                directionVector = RotationUtil.oldVecWorldToPlayer(directionVector.x, directionVector.y, directionVector.z, gravityDirection);
                OLD_DIR_WORLD_TO_PLAYER[gravityDirection.getId()][direction.getId()] = Direction.fromVector(new BlockPos(directionVector));
            }
        }
    }

    public static Direction oldDirWorldToPlayer(Direction direction, Direction gravityDirection) {
        return OLD_DIR_WORLD_TO_PLAYER[gravityDirection.getId()][direction.getId()];
    }

    public static Vec3d oldVecWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN  -> new Vec3d( x,  y,  z);
            case UP    -> new Vec3d(-x, -y,  z);
            case NORTH -> new Vec3d( x,  z, -y);
            case SOUTH -> new Vec3d(-x, -z, -y);
            case WEST  -> new Vec3d(-z,  x, -y);
            case EAST  -> new Vec3d( z, -x, -y);
        };
    }

    public static Vec3d newVecWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        return getWorldRotationQuaternion(gravityDirection).rotate(new Vec3d(x,y,z));
    }
}
