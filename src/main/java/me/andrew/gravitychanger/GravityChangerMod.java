package me.andrew.gravitychanger;

import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import me.andrew.gravitychanger.accessor.ServerPlayerEntityAccessor;
import me.andrew.gravitychanger.api.GravitySource;
import me.andrew.gravitychanger.command.GravityCommand;
import me.andrew.gravitychanger.config.GravityChangerConfig;
import me.andrew.gravitychanger.item.ModItems;
import me.andrew.gravitychanger.util.RotationUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GravityChangerMod implements ModInitializer {
    public static final String MOD_ID = "gravitychanger";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    /* CLIENT -> SERVER:
     * Identifier gravitySourceId
     * Direction gravityDirection
     * Boolean initialGravity
     * Boolean rotateVelocity
     * Boolean rotateCamera
     * [Parameters for verifier]
     * SERVER -> CLIENT:
     * Identifier gravitySourceId
     * Direction gravityDirection
     * Boolean initialGravity
     * Boolean rotateVelocity
     * Boolean rotateCamera
     * */
    public static final Identifier CHANNEL_GRAVITY = new Identifier(MOD_ID, "gravity");
    /* SERVER -> CLIENT:
     * int size
     * [
     * Identifier gravitySourceId
     * Direction gravityDirection
     * ] * size
     * */
    public static final Identifier CHANNEL_GRAVITY_RESET = new Identifier(MOD_ID, "gravity_reset");
    /* SERVER -> CLIENT:
     * Direction gravityDirection
     * */
    public static final Identifier CHANNEL_GRAVITY_OTHER_PLAYERS = new Identifier(MOD_ID, "gravity_other_players");

    public static final Identifier DEFAULT_GRAVITY = new Identifier(MOD_ID, "default_gravity_source");
    public static final Identifier PLAYER_GRAVITY = new Identifier(MOD_ID, "player_gravity_source");

    public static GravityChangerConfig config;

    @Override
    public void onInitialize() {
        ModItems.init();

        AutoConfig.register(GravityChangerConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(GravityChangerConfig.class).getConfig();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> GravityCommand.register(dispatcher));

        ServerPlayNetworking.registerGlobalReceiver(CHANNEL_GRAVITY, (server, player, handler, buf, responseSender) -> {
            Identifier gravitySourceId = buf.readIdentifier();
            int dir = buf.readInt();
            Direction gravityDirection = (dir == -1) ? null : Direction.byId(dir);
            boolean initialGravity = buf.readBoolean();
            boolean rotateVelocity = buf.readBoolean();
            boolean rotateCamera = buf.readBoolean();
            server.execute(() -> {
                //Attempt to verify
                boolean verified = false;
                try {
                    if (GravitySource.exists(gravitySourceId)) {
                        var verify = GravitySource.getVerify(gravitySourceId);
                        if (verify != null) {
                            verified = verify.receive(server, player, handler, buf, responseSender);
                        }else{
                            GravityChangerMod.LOGGER.error("Client driven gravity change from source "+gravitySourceId+" could not be verified because the verifier is null");
                        }
                    }else{
                        GravityChangerMod.LOGGER.error("Client driven gravity change from source "+gravitySourceId+" could not be verified because the source does not exist.");
                    }
                } catch(Exception e){
                    GravityChangerMod.LOGGER.error("Client driven gravity change from source "+gravitySourceId+" could not be verified: ", e);
                }
                if(verified){
                    //Set gravity on for ServerPlayerEntity and update other clients
                    ((RotatableEntityAccessor) player).gravitychanger$setGravityDirection(gravitySourceId, gravityDirection, initialGravity, rotateVelocity, rotateCamera);
                    //TODO: update other clients of gravity change
                }else{
                    //Revert change on source client
                    ((ServerPlayerEntityAccessor) player).gravitychanger$sendGravityPacket(gravitySourceId, gravityDirection, initialGravity, rotateVelocity, rotateCamera);
                }
            });
        });
        GravitySource.register(DEFAULT_GRAVITY, 0);
        GravitySource.register(PLAYER_GRAVITY, 1);
        /*LOGGER.info(RotationUtil.vecPlayerToWorld(Direction.EAST.getUnitVector(), Direction.WEST));
        LOGGER.info(RotationUtil.vecPlayerToWorld(Direction.WEST.getUnitVector(), Direction.EAST));
        LOGGER.info(RotationUtil.vecPlayerToWorld(Direction.NORTH.getUnitVector(), Direction.SOUTH));
        LOGGER.info(RotationUtil.vecPlayerToWorld(Direction.SOUTH.getUnitVector(), Direction.NORTH));
        LOGGER.info(RotationUtil.vecPlayerToWorld(Direction.UP.getUnitVector(), Direction.DOWN));
        LOGGER.info(RotationUtil.vecPlayerToWorld(Direction.DOWN.getUnitVector(), Direction.UP));
        LOGGER.info(" ");
        LOGGER.info(RotationUtil.vecWorldToPlayer(Direction.EAST.getUnitVector(), Direction.WEST));
        LOGGER.info(RotationUtil.vecWorldToPlayer(Direction.WEST.getUnitVector(), Direction.EAST));
        LOGGER.info(RotationUtil.vecWorldToPlayer(Direction.NORTH.getUnitVector(), Direction.SOUTH));
        LOGGER.info(RotationUtil.vecWorldToPlayer(Direction.SOUTH.getUnitVector(), Direction.NORTH));
        LOGGER.info(RotationUtil.vecWorldToPlayer(Direction.UP.getUnitVector(), Direction.DOWN));
        LOGGER.info(RotationUtil.vecWorldToPlayer(Direction.DOWN.getUnitVector(), Direction.UP));
        System.exit(0);*/
        /*for (int i = 0; i < 6; i++) {
            var a = RotationUtil.DIR_WORLD_TO_PLAYER[i];
            for (int j = 0; j < 6; j++) {
                System.out.printf("%-10s", a[j]);
            }
            System.out.println();
        }
        System.exit(0);*/
    }
}
