package me.andrew.gravitychanger.api;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.List;

public class GravitySource {
    private static final HashMap<Identifier, Integer> priorityMap = new HashMap<>();
    private static final HashMap<Identifier, VerifyClientDrivenGravity> verifierMap = new HashMap<>();
    private static final HashMap<Identifier, GetConstantGravity> constantMap = new HashMap<>();
    private static final HashMap<Identifier, Type> typeMap = new HashMap<>();

    private static void register(Identifier id, Type type, int priority){
        priorityMap.put(id, priority);
        typeMap.put(id, type);
    }

    public static void register(Identifier id, int priority){
        register(id, Type.SERVER_DRIVEN, priority);
    }
    /*
    Constant gravities are always applied.
    "init" is called when the player (re)spawns or changes dimensions and constant gravities need to be refreshed
    * */
    public static void registerConstant(Identifier id, int priority, GetConstantGravity init){
        constantMap.put(id, init);
        register(id, Type.SERVER_DRIVEN, priority);
    }

    public static void registerClientDriven(Identifier id, int priority, VerifyClientDrivenGravity verifier){
        verifierMap.put(id, verifier);
        register(id, Type.CLIENT_DRIVEN, priority);
    }

    public static boolean exists(Identifier id){
        return priorityMap.containsKey(id);
    }

    public static Type getType(Identifier id){
        return typeMap.get(id);
    }

    public static int getPriority(Identifier id){
        return priorityMap.getOrDefault(id, Integer.MIN_VALUE);
    }

    public static VerifyClientDrivenGravity getVerify(Identifier id) {
        return verifierMap.get(id);
    }

    public static GetConstantGravity getConstant(Identifier id){
        return constantMap.get(id);
    }

    public static List<Identifier> getConstantGravityIds(){
        return constantMap.keySet().stream().toList();
    }

    enum Type {
        SERVER_DRIVEN,
        CLIENT_DRIVEN,
    }

    @FunctionalInterface
    public interface VerifyClientDrivenGravity {
        //Returns true if the gravity source is valid, otherwise false
        boolean receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender);
    }

    @FunctionalInterface
    public interface GetConstantGravity {
        //Returns true if the gravity source is valid, otherwise false
        Direction init(ServerPlayerEntity player, Direction currentValue);
    }
}
