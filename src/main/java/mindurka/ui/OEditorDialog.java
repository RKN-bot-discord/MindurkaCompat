package mindurka.ui;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Reflect;
import mindurka.MRules;
import mindurka.MVars;
import mindurka.Util;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.editor.MapEditorDialog;
import mindustry.editor.MapView;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OverlayFloor;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BuildVisibility;

public class OEditorDialog extends MapEditorDialog {
    private final OMapView view;
    private final OMapEditor editor;

    private boolean shownWithMap = false;
    private Map map;
    private Runnable refreshTools;

    private final Seq<Block> blocksOut = new Seq<>();
    private Table blockSelection;
    private ScrollPane blocksPane;

    private Table blockOptions;

    public OEditorDialog(OMapEditor editor, MapEditorDialog oldDialog) {
        super();

        this.editor = editor;
        view = (MVars.mapView = new OMapView());

        background(Styles.black);

        setFillParent(true);
        clearChildren();
        margin(0);

        update(() -> {
            if (Core.scene.getKeyboardFocus() == this) {
                doInput();
            }
        });

        shown(() -> {
            oldDialog.hide();

            beginLandscape();
            editor.clearOp();
            Core.scene.setScrollFocus(view);

            if (!shownWithMap) {
                Vars.logic.reset();
                Vars.state.rules = new Rules();
                editor.OBeginEdit(200, 200);
            } else {
                editor.OBeginEdit(map);
            }
            MVars.toolOptions.reset();
            MVars.rules.sync();
            shownWithMap = false;
        });

        hidden(() -> {
            editor.clearOp();
            Vars.platform.updateRPC();
            refreshTools = null;
            endLandscape();
        });

        shown(this::build);
    }

    @Override
    public MapView getView() {
        return view;
    }

    public void beginLandscape() {
        Vars.platform.beginForceLandscape();
    }

    public void endLandscape() {
        if (!Core.settings.getBool("landscape")) Vars.platform.endForceLandscape();
    }

    @Override
    public void beginEditMap(Fi file) {
        Vars.ui.loadAnd(() -> {
            try {
                shownWithMap = true;
                map = MapIO.createMap(file, true);
                show();
            } catch (Exception e) {
                Log.err(e);
                Vars.ui.showException("@editor.errorload", e);
            }
        });
    }

    public void refreshTools() {
        if (refreshTools != null) refreshTools.run();
    }

    @Override
    public void build() {
        float size = Vars.mobile ? 50f : 58f;

        clearChildren();
        table(cont -> {
            cont.left();

            Table mid = new Table();
            {
                mid.top();

                mid.table(t -> {
                    for (Team team : Team.baseTeams) {
                        ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
                        button.margin(4f);
                        button.getImageCell().grow();
                        button.getStyle().imageUpColor = team.color;
                        button.clicked(() -> MVars.toolOptions.team = team);
                        button.update(() -> button.setChecked(MVars.toolOptions.team == team));
                        t.add(button).size(size, size).left();
                    }
                }).growX().left();
                mid.row();

                Table tools = new Table().top();

                Table[] lastTable = { null };
                Cons<EditorTool> addTool = tool -> {
                    ImageButton button = new ImageButton(Vars.ui.getIcon(tool.name()), Styles.squareTogglei);
                    button.clicked(() -> {
                        MVars.toolOptions.tool = tool;
                        if (lastTable[0] != null) {
                            lastTable[0].remove();
                        }
                        rebuildBlockOptions();
                    });
                    button.update(() -> button.setChecked(MVars.toolOptions.tool == tool));
                    // buttons.add(button);

                    // Label mode = new Label("");
                    // mode.setColor(Pal.remove);
                    // mode.update(() -> mode.setText(tool.mode == -1 ? "" : "M" + (tool.mode + 1) + " "));
                    // mode.setAlignment(Align.bottomRight, Align.bottomRight);
                    // mode.touchable = Touchable.disabled;

                    tools.stack(button);
                };

                tools.defaults().size(size, size);

                mid.add(tools).top().padBottom(6).left();

                refreshTools = () -> {
                    tools.clear();

                    tools.button(Icon.menu, Styles.flati, () -> {
                        BaseDialog menu = Reflect.get(MapEditorDialog.class, this, "menu");
                        if (!menu.isShown()) {
                            menu.show();
                        }
                    });

                    ImageButton grid = tools.button(Icon.grid, Styles.squareTogglei, () -> view.setGrid(!view.isGrid())).get();
                    ImageButton undo = tools.button(Icon.undo, Styles.flati, editor::undo).get();
                    ImageButton redo = tools.button(Icon.redo, Styles.flati, editor::redo).get();

                    undo.setDisabled(() -> !editor.canUndo());
                    redo.setDisabled(() -> !editor.canRedo());

                    undo.update(() -> undo.getImage().setColor(undo.isDisabled() ? Color.gray : Color.white));
                    redo.update(() -> redo.getImage().setColor(redo.isDisabled() ? Color.gray : Color.white));
                    grid.update(() -> grid.setChecked(view.isGrid()));

                    addTool.get(EditorTool.zoom);
                    addTool.get(EditorTool.eraser);
                    tools.row();
                    addTool.get(EditorTool.pencil);
                    addTool.get(EditorTool.line);
                    addTool.get(EditorTool.fill);

                    if (MVars.rules.gamemode == MRules.Gamemode.forts) {
                        tools.row();
                        addTool.get(EditorTool.fortsPlotToggle);
                        addTool.get(EditorTool.fortsPlotCarver);
                    }
                };
                refreshTools.run();

                mid.row();

                mid.table(t -> blockOptions = t).top().padBottom(6).left().growX().fillX();
                rebuildBlockOptions();
            }
            mid.setFillParent(true);
            Table midOverlay = new Table();
            midOverlay.table(t -> {
                ImageButton button = new ImageButton(Icon.cancel, Styles.clearNoneTogglei);
                button.margin(4f);
                button.getImageCell().grow();
                button.clicked(() -> {
                    view.editorAction = null;
                    view.redrawPreview = true;
                    button.setChecked(true);
                });
                button.setChecked(true);
                t.add(button).size(size, size).left().top();
            }).grow();
            midOverlay.background(Tex.whiteui);
            midOverlay.setColor(Color.black);
            midOverlay.visible(() -> view.editorAction != null);
            midOverlay.update(() -> {
                midOverlay.touchable = view.editorAction == null ? Touchable.disabled : Touchable.enabled;
            });
            midOverlay.setFillParent(true);
            cont.stack(mid, midOverlay).margin(0).left().growY();

            cont.table(t -> t.add(view).grow()).grow();

            cont.table(this::addBlockSelection).right().growY();
        }).grow();
    }

    private void addBlockSelection(Table cont) {
        int cols = Vars.mobile ? 4 : 6;

        blockSelection = new Table();
        blockSelection.left();
        blocksPane = new ScrollPane(blockSelection, Styles.smallPane);
        blocksPane.setFadeScrollBars(false);
        blocksPane.setOverscroll(true, false);
        blocksPane.exited(() -> {
            if (blocksPane.hasScroll()) {
                Core.scene.setScrollFocus(view);
            }
        });

        Table[] configTable = { null };
        Block[] lastBlock = { null };

        cont.table(search -> {
            search.image(Icon.zoom).padRight(8);
            search.field("", this::rebuildBlockSelection).growX()
                    .name("editor/search").maxTextLength(Vars.maxNameLength).get().setMessageText("@players.search");
        }).growX().pad(-2).padLeft(6f);
        cont.row();
        cont.table(Tex.underline, extra -> extra.labelWrap(() -> MVars.toolOptions.selectedBlock.localizedName).width(cols * 50f).center()).growX();
        cont.row();
        cont.collapser(t -> {
            configTable[0] = t;
        }, () -> MVars.toolOptions.selectedBlock != null && MVars.toolOptions.selectedBlock.editorConfigurable).with(c -> c.setEnforceMinSize(true)).growX().row();
        cont.add(blocksPane).expandY().growX().top().left();

        rebuildBlockSelection("");
    }

    private void rebuildBlockSelection(String searchText){
        int cols = Vars.mobile ? 4 : 6;

        blockSelection.clear();

        blocksOut.clear();
        blocksOut.addAll(Vars.content.blocks());
        blocksOut.sort((b1, b2) -> {
            int core = -Boolean.compare(b1 instanceof CoreBlock, b2 instanceof CoreBlock);
            if (core != 0) return core;
            int synth = Boolean.compare(b1.synthetic(), b2.synthetic());
            if (synth != 0) return synth;
            int ore = Boolean.compare(b1 instanceof OverlayFloor, b2 instanceof OverlayFloor);
            if (ore != 0) return ore;
            return Integer.compare(b1.id, b2.id);
        });

        int i = 0;

        for (Block block : blocksOut) {
            TextureRegion region = block.uiIcon;

            if ((!Core.atlas.isFound(region) || !block.inEditor
                    || block.buildVisibility == BuildVisibility.debugOnly) && block != Blocks.cliff
                    || (!searchText.isEmpty() && !block.localizedName.toLowerCase().contains(searchText.trim().replaceAll(" +", " ").toLowerCase()))
            ) continue;

            ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
            button.getStyle().imageUp = new TextureRegionDrawable(region);
            button.clicked(() -> {
                MVars.toolOptions.selectedBlock = block;
                rebuildBlockOptions();
            });
            button.resizeImage(8 * 4f);
            button.update(() -> button.setChecked(MVars.toolOptions.selectedBlock == block));
            button.setChecked(MVars.toolOptions.selectedBlock == block);
            blockSelection.add(button).size(50f).tooltip(block.localizedName).left();

            if (i == 0) MVars.toolOptions.selectedBlock = block;

            if (++i % cols == 0) {
                blockSelection.row();
            }
        }

        if (i == 0) {
            blockSelection.add("@none.found").padLeft(54f).padTop(10f);
        }
    }

    private void rebuildBlockOptions() {
        float size = Vars.mobile ? 50f : 58f;

        blockOptions.clearChildren();
        if (!MVars.toolOptions.tool.blockTool) return;

        blockOptions.table(t -> {
            t.label(() -> "Brush size").left().pad(4f);

            Slider slider = new Slider(1, 16, 1, false);
            slider.setValue(MVars.toolOptions.radius);
            slider.moved(f -> MVars.toolOptions.radius = (int) f);

            Label label = new Label(() -> Integer.toString(MVars.toolOptions.radius));
            label.setAlignment(Align.center);
            label.touchable = Touchable.disabled;

            t.stack(slider, label).left().get();

            t.table().growX().fillX();
        }).margin(4).left().growX().fillX();
        blockOptions.row();

        blockOptions.table(t -> {
            t.label(() -> "Blend").left().pad(4f);

            for (Blend value : Blend.values()) {
                ImageButton button = new ImageButton(Vars.ui.getIcon("blend-" + value.name()), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.blend = value);
                button.update(() -> button.setChecked(MVars.toolOptions.blend == value));
                t.add(button).pad(4f).left().size(size, size);
            }

            t.table().growX().fillX();
        }).margin(4).left().growX().fillX();
        blockOptions.row();

        if (MVars.toolOptions.selectedBlock == Blocks.cliff) {
            Table root = new Table();
            Table table = new Table();

            root.label(() -> "Cliff sides").left().pad(4f);
            root.row();
            root.add(table).left().growX().fillX();
            table.defaults().size(size, size).pad(0).left();

            {
                final int field = 5;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            {
                final int field = 6;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            {
                final int field = 7;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            table.table().growX().fillX();

            table.row();

            {
                final int field = 4;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            {
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-auto"), Styles.squareTogglei);
                button.clicked(() -> {
                    MVars.toolOptions.cliffAuto = !MVars.toolOptions.cliffAuto;
                    if (!MVars.toolOptions.cliffAuto) MVars.toolOptions.fakeCliffsMap = null;
                });
                button.update(() -> button.setChecked(MVars.toolOptions.cliffAuto));
                table.add(button).pad(0).left();
            }
            {
                final int field = 0;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            table.table().growX().fillX();

            table.row();

            {
                final int field = 3;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            {
                final int field = 2;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            {
                final int field = 1;
                ImageButton button = new ImageButton(Vars.ui.getIcon("cliff-" + field), Styles.squareTogglei);
                button.clicked(() -> MVars.toolOptions.cliffSides = Util.toggle(MVars.toolOptions.cliffSides, field));
                button.setDisabled(() -> MVars.toolOptions.cliffAuto);
                button.update(() -> button.setChecked(Util.enabled(MVars.toolOptions.cliffSides, field)));
                table.add(button).pad(0).left();
            }
            table.table().growX().fillX();

            blockOptions.add(root).margin(4).left().growX().fillX();
            blockOptions.row();
        }

        if (MVars.toolOptions.selectedBlock.editorConfigurable)
            MVars.toolOptions.selectedBlock.buildEditorConfig(blockOptions);
    }

    private void doInput(){
        if (Core.input.ctrl() && Core.input.shift()) {
            if (Core.input.keyTap(KeyCode.z) && view.editorAction == null) editor.redo();
        } else if (Core.input.ctrl()) {
            if (Core.input.keyTap(KeyCode.y) && view.editorAction == null) editor.redo();
            if (Core.input.keyTap(KeyCode.s) && view.editorAction == null) save();
            if (Core.input.keyTap(KeyCode.g)) view.setGrid(!view.isGrid());
            if (Core.input.keyTap(KeyCode.z) && view.editorAction == null) editor.undo();
        } else if (Core.input.shift()) {
            if (Core.input.keyTap(KeyCode.z) && view.editorAction == null) editor.redo();
        } else {
            if (Core.input.keyTap(KeyCode.z) && view.editorAction == null) editor.undo();

            if (Core.input.keyTap(KeyCode.escape)) {
                if (view.editorAction == null) {
                    BaseDialog menu = Reflect.get(MapEditorDialog.class, this, "menu");
                    if (!menu.isShown()) {
                        menu.show();
                    }
                } else {
                    view.editorAction = null;
                    view.redrawPreview = true;
                }
            }

            if (view.editorAction == null) for (EditorTool tool : EditorTool.values()) {
                if (tool.lockedBehind != null && tool.lockedBehind != MVars.rules.gamemode) continue;
                if (tool.key() == KeyCode.unset) continue;
                if (!Core.input.keyTap(tool.key())) continue;
                MVars.toolOptions.tool = tool;
            }
        }

        // if(Core.input.keyTap(KeyCode.r)){
        //     editor.rotation = Mathf.mod(editor.rotation + 1, 4);
        // }

        // if(Core.input.keyTap(KeyCode.e)){
        //     editor.rotation = Mathf.mod(editor.rotation - 1, 4);
        // }
    }
}
