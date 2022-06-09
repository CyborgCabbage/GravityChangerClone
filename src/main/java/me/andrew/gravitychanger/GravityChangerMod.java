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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
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

        /*for (Direction value : Direction.values()) {
            LOGGER.info(value);
            LOGGER.info("Camera: "+RotationUtil.getCameraRotationQuaternion(value));
            LOGGER.info("World:  "+RotationUtil.getWorldRotationQuaternion(value));
            LOGGER.info("");
        }
        System.exit(0);

        Quaternion[] WORLD_ROTATION_QUATERNIONS = new Quaternion[6];
        WORLD_ROTATION_QUATERNIONS[0] = Quaternion.IDENTITY.copy();
        WORLD_ROTATION_QUATERNIONS[1] = Vec3f.POSITIVE_Z.getDegreesQuaternion(-180);
        WORLD_ROTATION_QUATERNIONS[2] = Vec3f.POSITIVE_X.getDegreesQuaternion(-90);
        WORLD_ROTATION_QUATERNIONS[3] = Vec3f.POSITIVE_X.getDegreesQuaternion(-90);
        WORLD_ROTATION_QUATERNIONS[3].hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-180));
        WORLD_ROTATION_QUATERNIONS[4] = Vec3f.POSITIVE_X.getDegreesQuaternion(-90);
        WORLD_ROTATION_QUATERNIONS[4].hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90));
        WORLD_ROTATION_QUATERNIONS[5] = Vec3f.POSITIVE_X.getDegreesQuaternion(-90);
        WORLD_ROTATION_QUATERNIONS[5].hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-270));

        Quaternion[] ENTITY_ROTATION_QUATERNIONS = new Quaternion[6];
        ENTITY_ROTATION_QUATERNIONS[0] = Quaternion.IDENTITY;
        ENTITY_ROTATION_QUATERNIONS[1] = Vec3f.POSITIVE_Z.getDegreesQuaternion(-180);
        ENTITY_ROTATION_QUATERNIONS[2] = Vec3f.POSITIVE_X.getDegreesQuaternion(90);
        ENTITY_ROTATION_QUATERNIONS[3] = Vec3f.POSITIVE_X.getDegreesQuaternion(-90);
        ENTITY_ROTATION_QUATERNIONS[3].hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-180));
        ENTITY_ROTATION_QUATERNIONS[4] = Vec3f.POSITIVE_Y.getDegreesQuaternion(90);
        ENTITY_ROTATION_QUATERNIONS[4].hamiltonProduct(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
        ENTITY_ROTATION_QUATERNIONS[5] = Vec3f.POSITIVE_X.getDegreesQuaternion(90);
        ENTITY_ROTATION_QUATERNIONS[5].hamiltonProduct(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));

        for (Direction value : Direction.values()) {
            Quaternion q1 = ENTITY_ROTATION_QUATERNIONS[value.getId()];
            Quaternion q2 = RotationUtil.getCameraRotationQuaternion(value);
            LOGGER.info(value);
            LOGGER.info("Approximately Equal: "+equalQuaternions(q1, q2));
            LOGGER.info("");
        }
        for (Direction value : Direction.values()) {
            Quaternion q1 = WORLD_ROTATION_QUATERNIONS[value.getId()];
            Quaternion q2 = RotationUtil.getWorldRotationQuaternion(value);
            LOGGER.info(value);
            LOGGER.info("Approximately Equal: "+equalQuaternions(q1, q2));
            LOGGER.info("");
        }
        System.exit(0);*/
    }

    public static boolean equalQuaternions(Quaternion a, Quaternion b){
        return MathHelper.approximatelyEquals(a.getX(), b.getX())
                && MathHelper.approximatelyEquals(a.getY(), b.getY())
                && MathHelper.approximatelyEquals(a.getZ(), b.getZ())
                && MathHelper.approximatelyEquals(a.getW(), b.getW());
    }
}
