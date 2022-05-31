package me.andrew.gravitychanger.accessor;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.Direction;

public interface RotatableEntityAccessor {
    default Direction gravitychanger$getGravityDirection() {
        return this.gravitychanger$getTrackedGravityDirection();
    }

    default void gravitychanger$setGravityDirection(Direction gravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera) {
        this.gravitychanger$setTrackedGravityDirection(gravityDirection);
    }

    void gravitychanger$onGravityChanged(Direction prevGravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera);

    Direction gravitychanger$getTrackedGravityDirection();

    void gravitychanger$setTrackedGravityDirection(Direction gravityDirection);

    void gravitychanger$onTrackedData(TrackedData<?> data);
}
