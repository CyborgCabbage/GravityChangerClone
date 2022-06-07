/*package me.andrew.gravitychanger.mixin.client;

import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OtherClientPlayerEntity.class)
public abstract class OtherClientPlayerEntityMixin implements RotatableEntityAccessor {

    private Direction gravitychanger$activeGravityDirection = Direction.DOWN;

    @Override
    public Direction gravitychanger$getGravityDirection() {
        return gravitychanger$activeGravityDirection;
    }

    @Override
    public void gravitychanger$setGravityDirection(Direction gravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera) {
        this.gravitychanger$setTrackedGravityDirection(gravityDirection);
    }
}*/
