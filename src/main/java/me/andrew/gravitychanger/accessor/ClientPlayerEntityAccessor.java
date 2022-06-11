package me.andrew.gravitychanger.accessor;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public interface ClientPlayerEntityAccessor {
    void gravitychanger$sendGravityPacket(Identifier id, Direction gravityDirection, PacketByteBuf verifierBuf);

    void gravitychanger$setCameraState(CameraState cameraShift);
    CameraState gravitychanger$getCameraState();

    void gravitychanger$setCameraShift(CameraShift cameraShift);
    CameraShift gravitychanger$getCameraShift();

    record CameraState(Vec3d position, Quaternion rotation){}
    record CameraShift(CameraState cameraState, double start, double duration){}
}
