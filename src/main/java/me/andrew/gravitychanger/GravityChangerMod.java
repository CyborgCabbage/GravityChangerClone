package me.andrew.gravitychanger;

import me.andrew.gravitychanger.accessor.RotatableEntityAccessor;
import me.andrew.gravitychanger.api.GravitySource;
import me.andrew.gravitychanger.command.GravityCommand;
import me.andrew.gravitychanger.config.GravityChangerConfig;
import me.andrew.gravitychanger.item.ModItems;
import me.andrew.gravitychanger.util.RotationUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
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
    /* Server -> Client:
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
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeInt(gravityDirection == null ? -1 : gravityDirection.getId());
                    buffer.writeBoolean(initialGravity);
                    buffer.writeBoolean(rotateVelocity);
                    buffer.writeBoolean(rotateCamera);
                    ServerPlayNetworking.send(player, CHANNEL_GRAVITY, buffer);
                }
            });
        });

        GravitySource.register(DEFAULT_GRAVITY, 0);

        GravitySource.register(PLAYER_GRAVITY, 1);
    }
}
