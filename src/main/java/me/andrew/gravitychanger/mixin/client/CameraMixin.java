package me.andrew.gravitychanger.mixin.client;

import me.andrew.gravitychanger.GravityChangerMod;
import me.andrew.gravitychanger.accessor.EntityAccessor;
import me.andrew.gravitychanger.util.RotationUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    private float tickDelta;

    @Shadow protected abstract void setPos(double x, double y, double z);

    @Shadow private Entity focusedEntity;

    @Shadow @Final private Quaternion rotation;

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract Quaternion getRotation();

    @Inject(
            method = "update",
            at = @At("HEAD")
    )
    private void inject_update_head(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci){
        this.tickDelta = tickDelta;
    }

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V",
                    ordinal = 0
            )
    )
    private void redirect_update_setPos_0(Camera camera, double x, double y, double z, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        Direction gravityDirection = ((EntityAccessor) focusedEntity).gravitychanger$getAppliedGravityDirection();
        double entityLerpedY = MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY());

        Vec3f eyeOffset = new Vec3f(RotationUtil.vecPlayerToWorld(0, y - entityLerpedY, 0, gravityDirection));

        if(focusedEntity instanceof ClientPlayerEntity player){
            eyeOffset.rotate(RotationUtil.getReverseRotation(player, tickDelta));
        }

        setPos(eyeOffset.getX()+x, eyeOffset.getY()+entityLerpedY, eyeOffset.getZ()+z);
    }

    @Inject(
            method = "setRotation",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/Quaternion;hamiltonProduct(Lnet/minecraft/util/math/Quaternion;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void inject_setRotation(CallbackInfo ci) {
        Direction gravityDirection = ((EntityAccessor) this.focusedEntity).gravitychanger$getAppliedGravityDirection();
        //if (gravityDirection == Direction.DOWN) return;
        Quaternion rotation = RotationUtil.getCameraRotationQuaternion(gravityDirection).copy();
        rotation.hamiltonProduct(this.rotation);
        if(focusedEntity instanceof ClientPlayerEntity player){
            Quaternion reverseRotation = RotationUtil.getReverseRotation(player, tickDelta);
            reverseRotation.hamiltonProduct(rotation);
            rotation = reverseRotation;
        }
        this.rotation.set(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW());
    }
}
