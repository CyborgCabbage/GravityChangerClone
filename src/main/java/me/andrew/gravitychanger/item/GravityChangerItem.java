package me.andrew.gravitychanger.item;

import me.andrew.gravitychanger.GravityChangerMod;
import me.andrew.gravitychanger.api.GravityChangerAPI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GravityChangerItem extends Item {
    public final Direction gravityDirection;

    public GravityChangerItem(Settings settings, Direction gravityDirection) {
        super(settings);

        this.gravityDirection = gravityDirection;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!world.isClient())
            GravityChangerAPI.setGravityDirection((ServerPlayerEntity) user, GravityChangerMod.PLAYER_GRAVITY, gravityDirection);
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
