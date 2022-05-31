package me.andrew.gravitychanger;

import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.Direction;

public class ClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GravityChangerMod.CHANNEL_GRAVITY, (client, handler, buf, responseSender) -> {
            Direction gravityDirection = buf.readEnumConstant(Direction.class);
            boolean initialGravity = buf.readBoolean();
            boolean rotateVelocity = buf.readBoolean();
            boolean rotateCamera = buf.readBoolean();
            client.execute(() -> {
                if(client.player == null) return;

                ((RotatableEntityAccessor) client.player).gravitychanger$setGravityDirection(gravityDirection, initialGravity, rotateVelocity, rotateCamera);
            });
        });
    }
}
