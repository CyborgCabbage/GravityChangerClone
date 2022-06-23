package me.andrew.gravitychanger.mixin.client;

import me.andrew.gravitychanger.GravityChangerMod;
import me.andrew.gravitychanger.accessor.EntityAccessor;
import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import me.andrew.gravitychanger.util.RotationUtil;
import net.minecraft.block.AmethystBlock;
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

    @Shadow protected abstract void setPos(Vec3d pos);

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
        Vec3d startPos = temp(x, y, z, focusedEntity, tickDelta, focusedEntity.prevY);
        Vec3d endPos = temp(x, y, z, focusedEntity, tickDelta, focusedEntity.getY());
        setPos(startPos.lerp(endPos, tickDelta));
    }

    private Vec3d temp(double x, double y, double z, Entity focusedEntity, float tickDelta, double focusedEntityY) {
        Direction gravityDirection = ((EntityAccessor) focusedEntity).gravitychanger$getAppliedGravityDirection();
        Vec3f startEyeOffset = new Vec3f(RotationUtil.vecPlayerToWorld(0, y - focusedEntityY, 0, gravityDirection));

        if (focusedEntity instanceof ClientPlayerEntity player) {
            RotatableEntityAccessor.CameraShift shift = ((RotatableEntityAccessor)player).gravitychanger$getCameraShift();
            if(shift != null) {
                //Get time
                double time = player.world.getTime();
                time += tickDelta;
                double currentTime = time;
                //Get relative time
                double relTime = (currentTime - shift.start()) / shift.duration();
                if (relTime < 1) {
                    if(shift.from().getOpposite() != shift.to()) {
                        startEyeOffset.rotate(RotationUtil.getRotationBetween(shift.to(), shift.from(), 1.f - (float) relTime));
                    }else{
                        Vec3f oldPos1 = new Vec3f(0,0,0);
                        startEyeOffset.lerp(oldPos1,1.f - (float) relTime);
                    }
                }
            }
        }

        return new Vec3d(startEyeOffset.getX() + x, startEyeOffset.getY() + focusedEntityY, startEyeOffset.getZ() + z);
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
        Quaternion rotation = RotationUtil.getCameraRotationQuaternion(gravityDirection).copy();
        rotation.hamiltonProduct(this.rotation);
        if(focusedEntity instanceof ClientPlayerEntity player){
            RotatableEntityAccessor.CameraShift shift = ((RotatableEntityAccessor)player).gravitychanger$getCameraShift();
            if(shift != null) {
                //Get time
                double time = player.world.getTime();
                time += tickDelta;
                double currentTime = time;
                //Get relative time
                double relTime = (currentTime - shift.start()) / shift.duration();
                if (relTime < 1) {
                    if(shift.from().getOpposite() != shift.to()) {
                        //Get quaternion
                        Quaternion reverseRotation = RotationUtil.getRotationBetween(shift.to(), shift.from(), 1.f - (float) relTime);
                        reverseRotation.hamiltonProduct(rotation);
                        rotation = reverseRotation;
                    }else{
                        Quaternion reverseRotation = RotationUtil.getFlipRotation( 1.f - (float) relTime);
                        rotation.hamiltonProduct(reverseRotation);
                    }
                }
            }
        }
        this.rotation.set(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW());
    }
}
