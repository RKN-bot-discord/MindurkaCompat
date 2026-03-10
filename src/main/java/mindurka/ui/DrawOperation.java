package mindurka.ui;

import arc.struct.ByteSeq;
import arc.struct.Seq;
import arc.util.Log;
import mindurka.MVars;
import mindurka.Util;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import net.jpountz.lz4.LZ4Factory;

public class DrawOperation {
    private ByteSeq data;
    private Seq<Object> dataObjects;

    private int compressed = -1;
    private byte[] compressedData;
    private Object[] compressedDataObjects;

    public short lastX = -1, lastY = -1;

    private final byte opFloor = 0,
        opFloorData = 1,
        opExtraData = 2,
        opBlock = 3,
        opBlockData = 4,
        opRotation = 5,
        opTeam = 6,
        opTile = 7,
        opOverlay = 8,
        opOverlayData = 9;

    public int size() {
        int size = 0;

        if (data != null) size += data.size;
        else if (compressedData != null) size += compressedData.length;

        if (dataObjects != null) size += dataObjects.size * 1024;
        else if (compressedDataObjects != null) size += compressedDataObjects.length * 1024;

        return size;
    }

    public void maybeCompress() {
        if (data != null) {
            compressedData = data.toArray();
            compressed = compressedData.length > 50 ? compressedData.length : -1;
            if (compressed != -1) compressedData = LZ4Factory.fastestInstance().fastCompressor().compress(compressedData);
            data = null;
        }
        if (dataObjects != null) {
            compressedDataObjects = dataObjects.toArray(Object.class);
            dataObjects = null;
        }
    }

    public void maybeDecompress() {
        if (compressedData != null) {
            if (compressed != -1) {
                byte[] decompressed = new byte[compressed];
                LZ4Factory.fastestInstance().safeDecompressor().decompress(compressedData, decompressed);
                data = new ByteSeq(decompressed);
            } else {
                data = new ByteSeq(compressedData);
            }
            compressedData = null;
        }
        if (compressedDataObjects != null) {
            dataObjects = new Seq<>(compressedDataObjects);
            compressedDataObjects = null;
        }
    }

    public void undo() {
        maybeDecompress();

        if (data == null) {
            dataObjects = null;
            return;
        }

        Tile cursor = null;

        int i = data.size - 1;
        int bi = dataObjects == null ? 0 : dataObjects.size - 1;
        try {
            for (; i >= 0; i--) {
                switch (data.items[i]) {
                    case opTile: {
                        i -= 2;
                        short x = Util.readShort(data, i);
                        i -= 2;
                        short y = Util.readShort(data, i);
                        i -= 2;
                        cursor = Vars.world.tile(x, y);
                        break;
                    }

                    case opFloor: {
                        i -= 2;
                        Floor floor = (Floor) Vars.content.block(Util.readShort(data, i));
                        i -= 2;
                        short x = Util.readShort(data, i);
                        i -= 2;
                        short y = Util.readShort(data, i);
                        cursor = Vars.world.tile(x, y);
                        cursor.setFloor(floor);
                        break;
                    }
                    case opFloorData: {
                        assert cursor != null;
                        byte tileData = data.get(--i);
                        MVars.mapEditor.currentOp().overlayData(cursor.floorData);
                        MVars.mapEditor.currentOp().tile(cursor.x, cursor.y);
                        cursor.floorData = tileData;
                        break;
                    }

                    case opBlock: {
                        i -= 2;
                        Block block = Vars.content.block(Util.readShort(data, i));

                        i -= 2;
                        short x = Util.readShort(data, i);

                        i -= 2;
                        short y = Util.readShort(data, i);

                        cursor = Vars.world.tile(x, y);

                        int rotation;
                        if (block.rotate) {
                            i -= 4;
                            rotation = Util.readInt(data, i);
                        } else rotation = 0;

                        Team team;
                        if (block.hasBuilding()) team = Team.all[data.get(--i) & 0xff];
                        else team = Team.derelict;

                        Building build = (Building) dataObjects.get(bi--);

                        cursor.setBlock(block, team, rotation, () -> build);
                        break;
                    }
                    case opBlockData: {
                        assert cursor != null;
                        byte tileData = data.get(--i);
                        MVars.mapEditor.currentOp().overlayData(cursor.data);
                        MVars.mapEditor.currentOp().tile(cursor.x, cursor.y);
                        cursor.data = tileData;
                        break;
                    }
                    case opTeam: {
                        assert cursor != null;
                        cursor.setTeam(Team.all[data.get(--i) & 0xff]);
                        break;
                    }
                    case opRotation: {
                        assert cursor != null;
                        i -= 4;
                        int rotation = Util.readInt(data, i);
                        if (cursor.build != null) cursor.build.rotation = rotation;
                        break;
                    }

                    case opOverlay: {
                        i -= 2;
                        Floor floor = (Floor) Vars.content.block(Util.readShort(data, i));
                        i -= 2;
                        short x = Util.readShort(data, i);
                        i -= 2;
                        short y = Util.readShort(data, i);
                        cursor = Vars.world.tile(x, y);
                        cursor.setOverlay(floor);
                        break;
                    }
                    case opOverlayData: {
                        assert cursor != null;
                        byte tileData = data.get(--i);
                        MVars.mapEditor.currentOp().overlayData(cursor.overlayData);
                        MVars.mapEditor.currentOp().tile(cursor.x, cursor.y);
                        cursor.overlayData = tileData;
                        break;
                    }

                    case opExtraData: {
                        assert cursor != null;
                        i -= 4;
                        int tileData = Util.readInt(data, i);
                        MVars.mapEditor.currentOp().extraData(cursor.extraData);
                        MVars.mapEditor.currentOp().tile(cursor.x, cursor.y);
                        cursor.extraData = tileData;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            StringBuilder builder = new StringBuilder();
            for (int o = 0; o < data.size; o++) {
                if (builder.length() > 0) builder.append(',');
                builder.append(data.items[o] & 0xff);
            }

            Log.err("Fatal!");
            Log.err("Command buffer: " + builder);
            Log.err("Idx: " + i);
            throw new RuntimeException(e);
        }

        data = null;
        dataObjects = null;
    }

    public void tile(short x, short y) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        Util.writeShort(data, y);
        Util.writeShort(data, x);
        data.add(opTile);

        lastX = x;
        lastY = y;
    }

    public void floor(Floor floor, short x, short y) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        Util.writeShort(data, y);
        Util.writeShort(data, x);
        Util.writeShort(data, floor.id);
        data.add(opFloor);

        lastX = x;
        lastY = y;
    }
    public void floorData(byte newData) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        data.add(newData, opFloorData);

        Log.info("floorData(" + (newData & 0xff) + ")");
    }

    public void block(Block block, short x, short y, Team team, int rotation, Building build) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);
        if (dataObjects == null) dataObjects = new Seq<>(4);

        dataObjects.add(build);
        if (block.hasBuilding()) data.add((byte) team.id);
        if (block.rotate) Util.writeInt(data, rotation);
        Util.writeShort(data, y);
        Util.writeShort(data, x);
        Util.writeShort(data, block.id);
        data.add(opBlock);

        lastX = x;
        lastY = y;
    }
    public void rotation(int rotation) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        Util.writeInt(data, rotation);
        data.add(opRotation);
    }
    public void team(Team team) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        data.add((byte) team.id, opTeam);
    }
    public void blockData(byte newData) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        data.add(newData, opBlockData);

        Log.info("blockData(" + (newData & 0xff) + ")");
    }

    public void extraData(int newData) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        Util.writeInt(data, newData);
        data.add(opExtraData);

        Log.info("extraData(" + (newData & 0xffffffffL) + ")");
    }

    public void overlay(Floor overlay, short x, short y) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        Util.writeShort(data, y);
        Util.writeShort(data, x);
        Util.writeShort(data, overlay.id);
        data.add(opOverlay);

        lastX = x;
        lastY = y;
    }
    public void overlayData(byte newData) {
        maybeDecompress();
        if (data == null) data = new ByteSeq(32);

        data.add(newData, opOverlayData);
    }
}
