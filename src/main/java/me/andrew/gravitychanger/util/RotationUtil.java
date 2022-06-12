package me.andrew.gravitychanger.util;

import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

public abstract class RotationUtil {
    private static final Vec3i[] ALL_TRANSFORMATIONS = {
            new Vec3i(0, 1, 2),
            new Vec3i(0, 2, 1),
            new Vec3i(2, 1, 0),
            new Vec3i(2, 0, 1),
            new Vec3i(1, 2, 0),
            new Vec3i(1, 0, 2)
    };
    private static final Vec3i[] ALL_SIGNS = {
            new Vec3i(1, 1, 1),
            new Vec3i(1, 1, -1),
            new Vec3i(1, -1, 1),
            new Vec3i(1, -1, -1),
            new Vec3i(-1, 1, 1),
            new Vec3i(-1, 1, -1),
            new Vec3i(-1, -1, 1),
            new Vec3i(-1, -1, -1)
    };
    private static Vec3f transform(Vec3f v, VecTransform t){
        Vec3i position = t.pos;
        Vec3i sign = t.sign;
        float[] a = {v.getX(), v.getY(), v.getZ()};
        return new Vec3f(a[position.getX()]*sign.getX(), a[position.getY()]*sign.getY(), a[position.getZ()]*sign.getZ());
    }

    private static Vec3d transform(Vec3d v, VecTransform t){
        Vec3i position = t.pos;
        Vec3i sign = t.sign;
        double[] a = {v.getX(), v.getY(), v.getZ()};
        return new Vec3d(a[position.getX()]*sign.getX(), a[position.getY()]*sign.getY(), a[position.getZ()]*sign.getZ());
    }

    private static final List<VecTransform> VEC_WORLD_TO_PLAYER = new ArrayList<>();
    private static final List<VecTransform> VEC_PLAYER_TO_WORLD = new ArrayList<>();
    static {
        for(Direction d : Direction.values()) {
            //Camera Rotation
            Vec3f cameraRotation = new Vec3f(1,2,3);
            cameraRotation.rotate(getCameraRotationQuaternion(d));
            //Camera Rotation
            Vec3f worldRotation = new Vec3f(1,2,3);
            worldRotation.rotate(getWorldRotationQuaternion(d));
            //Test Vector (Before rotation)
            Vec3f test = new Vec3f(1,2,3);
            for (Vec3i pos : ALL_TRANSFORMATIONS) {
                for (Vec3i sign : ALL_SIGNS) {
                    VecTransform vt = new VecTransform(pos, sign);
                    if(approximatelyEquals(worldRotation, transform(test, vt))){
                        VEC_WORLD_TO_PLAYER.add(vt);
                    }
                    if(approximatelyEquals(cameraRotation, transform(test, vt))){
                        VEC_PLAYER_TO_WORLD.add(vt);
                    }
                }
            }
        }
    }

    public static Vec3d vecWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        return vecWorldToPlayer(new Vec3d(x, y, z), gravityDirection);
    }

    public static Vec3d vecWorldToPlayer(Vec3d vec3d, Direction gravityDirection) {
        return transform(vec3d, VEC_WORLD_TO_PLAYER.get(gravityDirection.getId()));
    }

    public static Vec3d vecPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        return vecPlayerToWorld(new Vec3d(x, y, z), gravityDirection);
    }

    public static Vec3d vecPlayerToWorld(Vec3d vec3d, Direction gravityDirection) {
        return transform(vec3d, VEC_PLAYER_TO_WORLD.get(gravityDirection.getId()));
    }

    public static Vec3f vecWorldToPlayer(float x, float y, float z, Direction gravityDirection) {
        return vecWorldToPlayer(new Vec3f(x, y, z), gravityDirection);
    }

    public static Vec3f vecWorldToPlayer(Vec3f vec3f, Direction gravityDirection) {
        return transform(vec3f, VEC_WORLD_TO_PLAYER.get(gravityDirection.getId()));
    }

    public static Vec3f vecPlayerToWorld(float x, float y, float z, Direction gravityDirection) {
        return vecPlayerToWorld(new Vec3f(x, y, z), gravityDirection);
    }

    public static Vec3f vecPlayerToWorld(Vec3f vec3f, Direction gravityDirection) {
        return transform(vec3f, VEC_PLAYER_TO_WORLD.get(gravityDirection.getId()));
    }

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


    public static Vec3d maskWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        VecTransform vt = VEC_WORLD_TO_PLAYER.get(gravityDirection.getId());
        vt = new VecTransform(vt.pos, new Vec3i(1,1,1));
        return transform(new Vec3d(x, y, z), vt);
    }

    public static Vec3d maskWorldToPlayer(Vec3d vec3d, Direction gravityDirection) {
        return maskWorldToPlayer(vec3d.x, vec3d.y, vec3d.z, gravityDirection);
    }

    public static Vec3d maskPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        VecTransform vt = VEC_PLAYER_TO_WORLD.get(gravityDirection.getId());
        vt = new VecTransform(vt.pos, new Vec3i(1,1,1));
        return transform(new Vec3d(x, y, z), vt);
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
        return getRotationBetween(gravityDirection, Direction.DOWN);
    }

    public static Quaternion getCameraRotationQuaternion(Direction gravityDirection) {
        return getRotationBetween(Direction.DOWN, gravityDirection);
    }

    public static Quaternion getRotationBetween(Direction start, Direction end){
        return getRotationBetween(start.getUnitVector(), end.getUnitVector());
    }

    public static Quaternion getRotationBetween(Direction start, Direction end, float fraction){
        return getRotationBetween(start.getUnitVector(), end.getUnitVector(), fraction);
    }

    public static Quaternion getRotationBetween(Vec3f start, Vec3f end){
        Vec3f rotAxis = start.copy();
        rotAxis.cross(end);
        float rotAngle = (float)Math.acos(start.dot(end));
        //Make sure axis isn't {0, 0, 0}
        if(MathHelper.magnitude(rotAxis.getX(), rotAxis.getY(), rotAxis.getZ()) < 0.1 ) rotAxis = Vec3f.NEGATIVE_Z;
        return new Quaternion(rotAxis, rotAngle, false);
    }

    public static Quaternion getRotationBetween(Vec3f start, Vec3f end, float fraction){
        Vec3f rotAxis = start.copy();
        rotAxis.cross(end);
        float rotAngle = (float)Math.acos(start.dot(end));
        //Make sure axis isn't {0, 0, 0}
        if(MathHelper.magnitude(rotAxis.getX(), rotAxis.getY(), rotAxis.getZ()) < 0.1 ) rotAxis = Vec3f.NEGATIVE_Z;
        return new Quaternion(rotAxis, rotAngle*fraction, false);
    }

    public static Quaternion getReverseRotation(PlayerEntity player, float tickDelta){
        RotatableEntityAccessor.CameraShift shift = ((RotatableEntityAccessor)player).gravitychanger$getCameraShift();
        if(shift != null){
            //Get time
            double time = player.world.getTime();
            time += tickDelta;
            double currentTime = time;
            //Get relative time
            double relTime = (currentTime - shift.start()) / shift.duration();
            if(relTime > 1) return new Quaternion(0,0,0,1);
            //Get quaternion
            return RotationUtil.getRotationBetween(shift.to(), shift.from(), 1.f-(float)relTime);
        }
        return new Quaternion(0,0,0,1);
    }

    public static boolean approximatelyEquals(Vec3f a, Vec3f b){
        return MathHelper.approximatelyEquals(a.getX(), b.getX()) &&
                MathHelper.approximatelyEquals(a.getY(), b.getY()) &&
                MathHelper.approximatelyEquals(a.getZ(), b.getZ());
    }

    record VecTransform(Vec3i pos, Vec3i sign){};
}
