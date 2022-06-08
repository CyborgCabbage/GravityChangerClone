package me.andrew.gravitychanger.mixin;

import me.andrew.gravitychanger.GravityChangerMod;
import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import me.andrew.gravitychanger.accessor.ServerPlayerEntityAccessor;
import me.andrew.gravitychanger.api.ActiveGravityList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements RotatableEntityAccessor, ServerPlayerEntityAccessor {
    @Override
    public void gravitychanger$setGravityDirection(Identifier id, Direction gravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera) {
        Direction prevGravityDirection = gravitychanger$getGravityDirection();
        //Update gravity list state
        gravitychanger$getActiveGravityList().set(id, gravityDirection);//Server
        gravitychanger$sendGravityPacket(id, gravityDirection, initialGravity, rotateVelocity, rotateCamera);//Client
        //Change gravity
        if(prevGravityDirection != gravitychanger$getGravityDirection()) {
            gravitychanger$onGravityChanged(prevGravityDirection, initialGravity, rotateVelocity, rotateCamera);
        }
    }

    @Override
    public void gravitychanger$sendGravityPacket(Identifier id, Direction gravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeIdentifier(id);
        buffer.writeInt(gravityDirection == null ? -1 : gravityDirection.getId());
        buffer.writeBoolean(initialGravity);
        buffer.writeBoolean(rotateVelocity);
        buffer.writeBoolean(rotateCamera);
        ServerPlayNetworking.send((ServerPlayerEntity)(Object)this, GravityChangerMod.CHANNEL_GRAVITY, buffer);
    }

    @Override
    public void gravitychanger$sendGravityResetPacket() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object)this;
        ServerPlayNetworking.send(player, GravityChangerMod.CHANNEL_GRAVITY_RESET, gravitychanger$getActiveGravityList().getPacket());
    }

    @Inject(
            method = "moveToWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void inject_moveToWorld_sendPacket_1(CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if(GravityChangerMod.config.resetGravityOnDimensionChange) {
            gravitychanger$getActiveGravityList().set(GravityChangerMod.PLAYER_GRAVITY, null);
        }
        gravitychanger$sendGravityResetPacket();
    }

    @Inject(
            method = "teleport",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void inject_teleport_sendPacket_0(CallbackInfo ci) {
        if(GravityChangerMod.config.resetGravityOnDimensionChange) {
            gravitychanger$getActiveGravityList().set(GravityChangerMod.PLAYER_GRAVITY, null);
        }
        gravitychanger$sendGravityResetPacket();
    }

    @Inject(
            method = "copyFrom",
            at = @At("TAIL")
    )
    private void inject_copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        //Copy gravity list
        ActiveGravityList oldList = ((RotatableEntityAccessor) oldPlayer).gravitychanger$getActiveGravityList();
        if(oldList != null) {
            gravitychanger$getActiveGravityList().fromOther(oldList);
        }
        //Reset player gravity if required
        if(GravityChangerMod.config.resetGravityOnRespawn) {
            gravitychanger$getActiveGravityList().set(GravityChangerMod.PLAYER_GRAVITY, null);
        }
        //Bring client up to date
        gravitychanger$sendGravityResetPacket();
    }
}
