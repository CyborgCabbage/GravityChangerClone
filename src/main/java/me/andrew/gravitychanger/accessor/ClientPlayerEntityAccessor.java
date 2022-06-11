package me.andrew.gravitychanger.accessor;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface ClientPlayerEntityAccessor {
    void gravitychanger$sendGravityPacket(Identifier id, Direction gravityDirection, PacketByteBuf verifierBuf);
}
