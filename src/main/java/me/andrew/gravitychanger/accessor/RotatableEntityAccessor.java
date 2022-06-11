package me.andrew.gravitychanger.accessor;

import me.andrew.gravitychanger.api.ActiveGravityList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface RotatableEntityAccessor {
    ActiveGravityList gravitychanger$getActiveGravityList();

    Direction gravitychanger$getGravityDirection(Identifier id);

    Direction gravitychanger$getGravityDirection();

    Direction gravitychanger$getGravityDirectionAfterChange(Identifier id, Direction dir);

    void gravitychanger$setGravityDirection(Identifier id, Direction gravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera);

    void gravitychanger$onGravityChanged(Direction prevGravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera);

    void gravitychanger$setCameraShift(CameraShift cameraShift);

    CameraShift gravitychanger$getCameraShift();

    record CameraShift(Direction from, Direction to, double start, double duration){}
}
