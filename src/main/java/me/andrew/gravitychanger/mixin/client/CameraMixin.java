package me.andrew.gravitychanger.mixin.client;

import me.andrew.gravitychanger.GravityChangerMod;
import me.andrew.gravitychanger.accessor.ClientPlayerEntityAccessor;
import me.andrew.gravitychanger.accessor.EntityAccessor;
import me.andrew.gravitychanger.util.RotationUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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

    @Shadow private float pitch;

    @Shadow private float yaw;

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V",
                    ordinal = 0
            )
    )
    private void redirect_update_setPos_0(Camera camera, double x, double y, double z, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        //Set Time
        double time = focusedEntity.world.getTime();
        time += tickDelta;
        double currentTime = time;

        Direction gravityDirection = ((EntityAccessor) focusedEntity).gravitychanger$getAppliedGravityDirection();
        double entityLerpedY = MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY());

        Vec3d eyeOffset = RotationUtil.vecPlayerToWorld(0, y - entityLerpedY, 0, gravityDirection);

        Vec3d endPos = new Vec3d(
                x + eyeOffset.x,
                entityLerpedY + eyeOffset.y,
                z + eyeOffset.z
        );

        /*Quaternion endRot = getRotation();
        if(focusedEntity instanceof ClientPlayerEntity player){
            ClientPlayerEntityAccessor accessor = (ClientPlayerEntityAccessor) player;
            ClientPlayerEntityAccessor.CameraShift shift = accessor.gravitychanger$getCameraShift();
            if(shift != null){
                Vec3d startPos = shift.cameraState().position();
                Quaternion startRot = shift.cameraState().rotation();
                GravityChangerMod.LOGGER.info("ROTATION");
                GravityChangerMod.LOGGER.info(startRot);
                GravityChangerMod.LOGGER.info(endRot);
                double relTime = (currentTime - shift.start()) / shift.duration();
                if(relTime < 0.0){
                    endPos = startPos;
                    endRot = startRot;
                }else if (relTime > 1.0){
                    accessor.gravitychanger$setCameraShift(null);
                }else{
                    endPos = new Vec3d(
                            MathHelper.lerp(relTime, startPos.x, endPos.x),
                            MathHelper.lerp(relTime, startPos.y, endPos.y),
                            MathHelper.lerp(relTime, startPos.z, endPos.z)
                    );
                    endRot = new Quaternion(
                            MathHelper.lerp((float)relTime, startRot.getX(), endRot.getX()),
                            MathHelper.lerp((float)relTime, startRot.getY(), endRot.getY()),
                            MathHelper.lerp((float)relTime, startRot.getZ(), endRot.getZ()),
                            MathHelper.lerp((float)relTime, startRot.getW(), endRot.getW())
                    );
                }
            }
        }*/
        setPos(endPos.x, endPos.y, endPos.z);

        /*this.rotation.set(endRot.getX(), endRot.getY(), endRot.getZ(), endRot.getW());
        this.horizontalPlane.set(0.0f, 0.0f, 1.0f);
        this.horizontalPlane.rotate(this.rotation);
        this.verticalPlane.set(0.0f, 1.0f, 0.0f);
        this.verticalPlane.rotate(this.rotation);
        this.diagonalPlane.set(1.0f, 0.0f, 0.0f);
        this.diagonalPlane.rotate(this.rotation);*/

    }

    /*@Inject(
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
        if (gravityDirection != Direction.DOWN) {
            Quaternion rotation = RotationUtil.getCameraRotationQuaternion(gravityDirection).copy();
            rotation.hamiltonProduct(this.rotation);
            this.rotation.set(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW());
        }
    }*/
    /**
     * @author CyborgCabbage
     *
     */
    @Overwrite
    public void setRotation(float yaw, float pitch) {
        //Get Gravity Rotation
        Direction gravityDirection = ((EntityAccessor) this.focusedEntity).gravitychanger$getAppliedGravityDirection();
        Quaternion gravityRotation = RotationUtil.getCameraRotationQuaternion(gravityDirection).copy();
        //Set Rotation
        this.pitch = pitch;
        this.yaw = yaw;
        this.rotation.set(0.0f, 0.0f, 0.0f, 1.0f);
        this.rotation.hamiltonProduct(gravityRotation);
        this.rotation.hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw));
        this.rotation.hamiltonProduct(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch));
        this.horizontalPlane.set(0.0f, 0.0f, 1.0f);
        this.horizontalPlane.rotate(this.rotation);
        this.verticalPlane.set(0.0f, 1.0f, 0.0f);
        this.verticalPlane.rotate(this.rotation);
        this.diagonalPlane.set(1.0f, 0.0f, 0.0f);
        this.diagonalPlane.rotate(this.rotation);
    }
}
