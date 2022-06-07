package me.andrew.gravitychanger.accessor;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface ServerPlayerEntityAccessor {
    void gravitychanger$sendGravityPacket(Identifier id, Direction gravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera);
    void gravitychanger$sendGravityResetPacket();
}
