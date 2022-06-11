package me.andrew.gravitychanger.mixin.client;

import com.mojang.authlib.GameProfile;
import me.andrew.gravitychanger.GravityChangerMod;
import me.andrew.gravitychanger.accessor.ClientPlayerEntityAccessor;
import me.andrew.gravitychanger.accessor.EntityAccessor;
import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import me.andrew.gravitychanger.api.ActiveGravityList;
import me.andrew.gravitychanger.mixin.PlayerEntityMixin;
import me.andrew.gravitychanger.util.RotationUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements RotatableEntityAccessor, ClientPlayerEntityAccessor {
    @Shadow protected abstract boolean wouldCollideAt(BlockPos pos);

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    private CameraShift gravitychanger$cameraShift;
    private CameraState gravitychanger$cameraState = new CameraState(Vec3d.ZERO, Quaternion.IDENTITY);

    @Override
    public void gravitychanger$setGravityDirection(Identifier id, Direction gravityDirection, boolean initialGravity, boolean rotateVelocity, boolean rotateCamera) {
        Direction prevGravityDirection = gravitychanger$getGravityDirection();
        gravitychanger$getActiveGravityList().set(id, gravityDirection);
        if(prevGravityDirection != gravitychanger$getGravityDirection()) {
            gravitychanger$onGravityChanged(prevGravityDirection, initialGravity, rotateVelocity, rotateCamera);
        }
    }

    @Override
    public void gravitychanger$sendGravityPacket(Identifier id, Direction gravityDirection, PacketByteBuf verifierBuf) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(id);
        buf.writeInt(gravityDirection == null ? -1 : gravityDirection.getId());
        buf.writeBoolean(false);
        buf.writeBoolean(false);
        buf.writeBoolean(false);
        buf.writeBytes(verifierBuf);
        ClientPlayNetworking.send(GravityChangerMod.CHANNEL_GRAVITY, buf);
    }

    @Override
    public void gravitychanger$setCameraShift(CameraShift cameraShift) {
        gravitychanger$cameraShift = cameraShift;
    }

    @Override
    public CameraShift gravitychanger$getCameraShift() {
        return gravitychanger$cameraShift;
    }

    @Override
    public void gravitychanger$setCameraState(CameraState cameraState) {
        gravitychanger$cameraState = cameraState;
    }

    @Override
    public CameraState gravitychanger$getCameraState() {
        return gravitychanger$cameraState;
    }

    @Redirect(
            method = "wouldCollideAt",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/util/math/Box",
                    ordinal = 0
            )
    )
    private Box redirect_wouldCollideAt_new_0(double x1, double y1, double z1, double x2, double y2, double z2, BlockPos pos) {
        Direction gravityDirection = ((EntityAccessor) this).gravitychanger$getAppliedGravityDirection();
        if(gravityDirection == Direction.DOWN) {
            return new Box(x1, y1, z1, x2, y2, z2);
        }

        Box playerBox = this.getBoundingBox();
        Vec3d playerMask = RotationUtil.maskPlayerToWorld(0.0D, 1.0D, 0.0D, gravityDirection);
        Box posBox = new Box(pos);
        Vec3d posMask = RotationUtil.maskPlayerToWorld(1.0D, 0.0D, 1.0D, gravityDirection);

        return new Box(
                playerMask.multiply(playerBox.minX, playerBox.minY, playerBox.minZ).add(posMask.multiply(posBox.minX, posBox.minY, posBox.minZ)),
                playerMask.multiply(playerBox.maxX, playerBox.maxY, playerBox.maxZ).add(posMask.multiply(posBox.maxX, posBox.maxY, posBox.maxZ))
        );
    }

    @Inject(
            method = "pushOutOfBlocks",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        Direction gravityDirection = ((EntityAccessor) this).gravitychanger$getAppliedGravityDirection();
        if(gravityDirection == Direction.DOWN) return;

        ci.cancel();

        Vec3d pos = RotationUtil.vecPlayerToWorld(x - this.getX(), 0.0D, z - this.getZ(), gravityDirection).add(this.getPos());
        BlockPos blockPos = new BlockPos(pos);
        if (this.wouldCollideAt(blockPos)) {
            double dx = pos.x - (double)blockPos.getX();
            double dy = pos.y - (double)blockPos.getY();
            double dz = pos.z - (double)blockPos.getZ();
            Direction direction = null;
            double minDistToEdge = Double.MAX_VALUE;

            Direction[] directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
            for(Direction playerDirection : directions) {
                Direction worldDirection = RotationUtil.dirPlayerToWorld(playerDirection, gravityDirection);

                double g = worldDirection.getAxis().choose(dx, dy, dz);
                double distToEdge = worldDirection.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - g : g;
                if (distToEdge < minDistToEdge && !this.wouldCollideAt(blockPos.offset(worldDirection))) {
                    minDistToEdge = distToEdge;
                    direction = playerDirection;
                }
            }

            if (direction != null) {
                Vec3d velocity = this.getVelocity();
                if (direction.getAxis() == Direction.Axis.X) {
                    this.setVelocity(0.1D * (double)direction.getOffsetX(), velocity.y, velocity.z);
                } else if(direction.getAxis() == Direction.Axis.Z) {
                    this.setVelocity(velocity.x, velocity.y, 0.1D * (double)direction.getOffsetZ());
                }
            }
        }
    }
}
