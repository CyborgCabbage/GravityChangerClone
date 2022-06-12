package me.andrew.gravitychanger.mixin.client;

import me.andrew.gravitychanger.accessor.EntityAccessor;
import me.andrew.gravitychanger.util.QuaternionUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Camera camera;

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V",
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    private void inject_renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        Direction gravityDirection = ((EntityAccessor) this.camera.getFocusedEntity()).gravitychanger$getAppliedGravityDirection();
        //if(gravityDirection == Direction.DOWN) return;
        //Undo vanilla rotations
        matrix.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0f));
        matrix.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));
        //Rotate 180, that's just something you gotta do
        matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));
        //Get inverse of camera rotation (we are rotating the world, not the camera)
        Quaternion q = new Quaternion(camera.getRotation());
        QuaternionUtil.inverse(q);
        matrix.multiply(q);
    }
}
