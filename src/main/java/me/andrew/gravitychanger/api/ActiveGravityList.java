package me.andrew.gravitychanger.api;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;

public class ActiveGravityList {
    private final ArrayList<ActiveGravityRecord> list = new ArrayList<>();

    public void set(Identifier id, Direction direction){
        list.removeIf(a -> a.id.equals(id));
        if(direction != null) {
            list.add(new ActiveGravityRecord(id, direction));
        }
    }

    public Direction get(Identifier id){
        return list.stream()
                .filter(agr -> agr.id().equals(id))
                .findFirst()
                .map(ActiveGravityRecord::direction)
                .orElse(null);
    }

    public void clear(){
        list.clear();
    }

    public Direction getDirection() {
        //Remove null directions
        list.removeIf(a -> a.direction == null);
        //Return null if there are no active gravities
        if (list.isEmpty()) return null;
        //Sort into descending priority
        list.sort(Comparator.comparingInt(r -> -GravitySource.getPriority(r.id())));
        //Return the first element
        return list.get(0).direction();
    }

    private static final String ID_KEY = "Id";
    private static final String DIRECTION_KEY = "Direction";

    public NbtList getNbt(){
        NbtList nbtList = new NbtList();
        for(ActiveGravityRecord agr : list){
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString(ID_KEY, agr.id.toString());
            nbtCompound.putInt(DIRECTION_KEY, agr.direction == null ? -1 : agr.direction.getId());
            nbtList.add(nbtCompound);
        }
        return nbtList;
    }

    public void fromNbt(NbtList nbtList){
        clear();
        for (NbtElement nbtElement : nbtList) {
            if (nbtElement instanceof NbtCompound nbtCompound) {
                if(nbtCompound.contains(ID_KEY, NbtType.STRING) && nbtCompound.contains(DIRECTION_KEY, NbtType.INT)) {
                    Identifier id = Identifier.tryParse(nbtCompound.getString(ID_KEY));
                    int dir = nbtCompound.getInt(DIRECTION_KEY);
                    Direction direction = (dir == -1) ? null : Direction.byId(dir);
                    set(id, direction);
                }
            }
        }
    }

    public PacketByteBuf getPacket(){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(list.size());
        for(ActiveGravityRecord agr : list){
            buf.writeIdentifier(agr.id);
            buf.writeInt(agr.direction == null ? -1 : agr.direction.getId());
        }
        return buf;
    }

    public void fromOther(ActiveGravityList other){
        clear();
        for (ActiveGravityRecord agr : other.list) {
            set(agr.id, agr.direction);
        }
    }

    record ActiveGravityRecord(Identifier id, Direction direction){}
}
