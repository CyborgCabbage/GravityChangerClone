package me.andrew.gravitychanger.api;

import me.andrew.gravitychanger.accessor.EntityAccessor;
import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import me.andrew.gravitychanger.util.RotationUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public abstract class GravityChangerAPI {
    /**
     * Returns the applied gravity direction for the given player
     * This is the direction that directly affects everything this mod changes
     * If the player is riding a vehicle this will be the applied gravity direction of the vehicle
     * Otherwise it will be the main gravity gravity direction of the player itself
     */
    public static Direction getAppliedGravityDirection(PlayerEntity playerEntity) {
        return ((EntityAccessor) playerEntity).gravitychanger$getAppliedGravityDirection();
    }

    /**
     * Returns the main gravity direction for the given player
     * This may not be the applied gravity direction for the player, see GravityChangerAPI#getAppliedGravityDirection
     */
    public static Direction getGravityDirection(PlayerEntity playerEntity) {
        return ((RotatableEntityAccessor) playerEntity).gravitychanger$getGravityDirection();
    }

    /**
     * Sets the main gravity direction for the given player
     * If the player is a ServerPlayerEntity and gravity direction changed also syncs the direction to the clients
     * If the player is either a ServerPlayerEntity or a ClientPlayerEntity also slightly adjusts player position
     * This may not immediately change the applied gravity direction for the player, see GravityChangerAPI#getAppliedGravityDirection
     */
    public static void setGravityDirection(PlayerEntity playerEntity, Direction gravityDirection) {
        ((RotatableEntityAccessor) playerEntity).gravitychanger$setGravityDirection(gravityDirection, false, false, false);
    }
    /**
     * The same as above but with additional parameters for controlling the manner in which gravity switches
     */
    public static void setGravityDirectionAdvanced(PlayerEntity playerEntity, Direction gravityDirection, boolean rotateVelocity, boolean rotateCamera) {
        ((RotatableEntityAccessor) playerEntity).gravitychanger$setGravityDirection(gravityDirection, false, rotateVelocity, rotateCamera);
    }

    /**
     * Returns the world relative velocity for the given player
     * Using minecraft's methods to get the velocity of a the player will return player relative velocity
     */
    public static Vec3d getWorldVelocity(PlayerEntity playerEntity) {
        return RotationUtil.vecPlayerToWorld(playerEntity.getVelocity(), ((EntityAccessor) playerEntity).gravitychanger$getAppliedGravityDirection());
    }

    /**
     * Sets the world relative velocity for the given player
     * Using minecraft's methods to set the velocity of a the player will set player relative velocity
     */
    public static void setWorldVelocity(PlayerEntity playerEntity, Vec3d worldVelocity) {
        playerEntity.setVelocity(RotationUtil.vecWorldToPlayer(worldVelocity, ((EntityAccessor) playerEntity).gravitychanger$getAppliedGravityDirection()));
    }

    /**
     * Returns eye position offset from feet position for the given player
     */
    public static Vec3d getEyeOffset(PlayerEntity playerEntity) {
        return RotationUtil.vecPlayerToWorld(0, (double) playerEntity.getStandingEyeHeight(), 0, ((EntityAccessor) playerEntity).gravitychanger$getAppliedGravityDirection());
    }
}
