package me.andrew.gravitychanger.mixin.client;

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
    @Shadow protected abstract void setPos(double x, double y, double z);

    @Shadow private Entity focusedEntity;

    @Shadow @Final private Quaternion rotation;

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract Quaternion getRotation();

    @Shadow @Final private Vec3f horizontalPlane;
    @Shadow @Final private Vec3f verticalPlane;
    @Shadow @Final private Vec3f diagonalPlane;

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

        Quaternion endRot = getRotation();
        if(focusedEntity instanceof ClientPlayerEntity player){
            //Calculate reverse axis
            Quaternion reverseRotation = RotationUtil.getReverseRotation(player, tickDelta);
            //Process
            eyeOffset.rotate(reverseRotation);
            reverseRotation.hamiltonProduct(endRot);
            endRot = reverseRotation;
        }
        //Set Position
        setPos(eyeOffset.getX()+x, eyeOffset.getY()+entityLerpedY, eyeOffset.getZ()+z);
        //Set Rotation
        this.rotation.set(endRot.getX(), endRot.getY(), endRot.getZ(), endRot.getW());
        this.horizontalPlane.set(0.0f, 0.0f, 1.0f);
        this.horizontalPlane.rotate(this.rotation);
        this.verticalPlane.set(0.0f, 1.0f, 0.0f);
        this.verticalPlane.rotate(this.rotation);
        this.diagonalPlane.set(1.0f, 0.0f, 0.0f);
        this.diagonalPlane.rotate(this.rotation);
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
        if (gravityDirection == Direction.DOWN) return;
        Quaternion rotation = RotationUtil.getCameraRotationQuaternion(gravityDirection).copy();
        rotation.hamiltonProduct(this.rotation);
        this.rotation.set(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW());
    }
}
