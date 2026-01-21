package mindurka.ui;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Reflect;
import mindurka.MVars;
import mindurka.rules.Gamemode;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.editor.DrawOperation;
import mindustry.editor.EditorTile;
import mindustry.editor.MapEditor;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.mod.DataPatcher;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.WorldContext;

import java.io.IOException;

public class OMapEditor extends MapEditor {
    private final Context context = new Context();
    private boolean loading;

    public OMapEditor() {
        super();
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    public static void inject() {
        Vars.editor = new OMapEditor();
    }

    @Override
    public void beginEdit(Map map) {}
    @Override
    public void beginEdit(Pixmap pixmap) {}
    @Override
    public void beginEdit(int width, int height) {}

    public void OBeginEdit(int width, int height) {
        reset();

        loading = true;
        createTiles(width, height);
        renderer.resize(width, height);
        loading = false;
    }

    public void OBeginEdit(Fi file) {
        try {
            OBeginEdit(MapIO.createMap(file, true));
        } catch (IOException e) {
            Vars.ui.showException(e);
        }
    }

    public void OBeginEdit(Map map) {
        reset();

        loading = true;
        tags.putAll(map.tags);
        if (map.file.parent().parent().name().equals("1127400") && Vars.steam) {
            tags.put("steamid", map.file.parent().name());
        }
        load(() -> MapIO.loadMap(map, context));
        renderer.resize(width(), height());

        if (MVars.rules.gamemode() != null) {
            a: {
                if (Vars.state.patcher.patches.size == 0) break a;
                DataPatcher.PatchSet patches = Vars.state.patcher.patches.first();
                if (!patches.name.equals("Mindurka Default Patch")) break a;
                Vars.state.patcher.patches.remove(0);
            }

            Gamemode.Impl gamemode = MVars.rules.gamemode();

            try {
                Seq<String> patches = Vars.state.patcher.patches.map(x -> x.patch);
                b: if (gamemode != null) {
                    String patch = gamemode.builtInContentPatch();
                    if (patch == null) break b;
                    patches.insert(0, "name: Mindurka Default Patch\n" + patch);
                }
                Vars.state.patcher.apply(patches);
            } catch (Exception error) {
                Log.err(error);
                Vars.ui.showException(error);
            }
        }

        loading = false;
    }

    public void OBeginEdit(Pixmap pixmap) {
        reset();

        loading = true;
        createTiles(pixmap.width, pixmap.height);
        load(() -> MapIO.readImage(pixmap, tiles()));
        renderer.resize(pixmap.width, pixmap.height);
        // 'load' sets this to 'false'. What's the fucking point?
        loading = false;
    }

    private void reset() {
        clearOp();
        brushSize = 1;
        drawBlock = Blocks.stone;
        tags = new StringMap();
    }

    public void undoCurrentOp() {
        if (Reflect.get(MapEditor.class, this, "currentOp") == null) return;
        Reflect.<DrawOperation>get(MapEditor.class, this, "currentOp").undo();
        Reflect.set(MapEditor.class, this, "currentOp", null);
    }

    @Override
    public void load(Runnable r) {
        loading = true;
        r.run();
        loading = false;
    }

    private void createTiles(int width, int height) {
        Tiles tiles = Vars.world.resize(width, height);

        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            tiles.set(x, y, new EditorTile(x, y, Blocks.stone.id, 0, 0));
        }
    }

    class Context implements WorldContext {
        @Override
        public Tile tile(int index) {
            return Vars.world.tiles.geti(index);
        }

        @Override
        public void resize(int width, int height) {
            Vars.world.resize(width, height);
        }

        @Override
        public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
            Tile tile = new EditorTile(x, y, floorID, overlayID, wallID);
            tiles().set(x, y, tile);
            return tile;
        }

        @Override
        public boolean isGenerating() {
            return Vars.world.isGenerating();
        }

        @Override
        public void begin() {
            Vars.world.beginMapLoad();
        }

        @Override
        public void end() {
            Vars.world.endMapLoad();
        }
    }
}
