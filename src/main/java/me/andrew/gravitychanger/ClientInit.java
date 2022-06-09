package me.andrew.gravitychanger;

import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import me.andrew.gravitychanger.api.ActiveGravityList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class ClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GravityChangerMod.CHANNEL_GRAVITY, (client, handler, buf, responseSender) -> {
            Identifier id = buf.readIdentifier();
            int dir = buf.readInt();
            Direction gravityDirection = (dir == -1) ? null : Direction.byId(dir);
            boolean initialGravity = buf.readBoolean();
            boolean rotateVelocity = buf.readBoolean();
            boolean rotateCamera = buf.readBoolean();
            client.execute(() -> {
                if(client.player == null) return;

                ((RotatableEntityAccessor) client.player).gravitychanger$setGravityDirection(id, gravityDirection, initialGravity, rotateVelocity, rotateCamera);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GravityChangerMod.CHANNEL_GRAVITY_RESET, (client, handler, buf, responseSender) -> {
            //Read packet into lists
            int size = buf.readInt();
            ArrayList<Identifier> idList = new ArrayList<>();
            ArrayList<Direction> directionList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                idList.add(buf.readIdentifier());
                int dir = buf.readInt();
                directionList.add(dir == -1 ? null : Direction.byId(dir));
            }
            client.execute(() -> {
                if(client.player == null) return;

                ActiveGravityList agl = ((RotatableEntityAccessor) client.player).gravitychanger$getActiveGravityList();
                Direction prevGravityDirection = ((RotatableEntityAccessor) client.player).gravitychanger$getGravityDirection();
                //Clear agl and refill with new gravities
                agl.clear();
                for (int i = 0; i < size; i++) {
                    agl.set(idList.get(i), directionList.get(i));
                }
                //If the gravity direction changed
                if(prevGravityDirection != ((RotatableEntityAccessor) client.player).gravitychanger$getGravityDirection()) {
                    ((RotatableEntityAccessor) client.player).gravitychanger$onGravityChanged(prevGravityDirection, true, false, false);
                }
            });
        });
    }
}
