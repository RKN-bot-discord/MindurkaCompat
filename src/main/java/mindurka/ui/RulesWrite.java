package mindurka.ui;

import arc.Core;
import arc.func.Boolc;
import arc.func.Boolf;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Floatc;
import arc.func.Floatp;
import arc.func.Func2;
import arc.func.Intc;
import arc.func.Intp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import mindustry.world.Block;

/**
 * Utility class for writing rules.
 * <p>
 * Exists to dump all the bloat from {@link mindustry.ui.dialogs.CustomRulesDialog} somewhere where
 * widgets can reuse them.
 */
public class RulesWrite {
    private final OLoadoutDialog loadoutDialog = new OLoadoutDialog();

    private static final Boolp TRUE = () -> true;

    private final Table root;
    /**
     * A hack to add the category if there's a property that
     * matches the search.
     */
    private @Nullable String categoryNameToAdd = null;
    private @Nullable RulesWrite parent = null;
    private String filter;
    private boolean canPlaceSpacer = false;
    private boolean shownCategory = false;

    public RulesWrite(Table root, String filter) {
        this.root = root;
        this.filter = filter.toLowerCase();
    }

    private boolean shouldAdd(String tlkey) {
        // TODO: Use a better algorithm.
        if (!Core.bundle.get(tlkey).toLowerCase().contains(filter)) return false;

        // TODO: Potentially bad on performance?
        if (!shownCategory) {
            while (true) {
                RulesWrite cursor = this;

                while (cursor.parent != null && cursor.categoryNameToAdd == null)
                    cursor = cursor.parent;

                if (cursor.categoryNameToAdd == null || cursor.parent == null) break;

                String name = Core.bundle.get("rules.title." + cursor.categoryNameToAdd);

                cursor.parent.root.add(name).color(Pal.accent).padTop(20).padBottom(-3).pad(6).fillX().left().row();
                cursor.parent.root.image().color(Pal.accent).height(3f).padBottom(20).pad(6).fillX().left().row();
                cursor.parent.root.add(cursor.root).fillX().pad(0);
                cursor.parent.root.row();

                cursor.categoryNameToAdd = null;
            }
            shownCategory = true;
        }
        canPlaceSpacer = true;

        return true;
    }

    public void clear() {
        root.clear();
        canPlaceSpacer = false;
    }
    public void clear(String newFilter) {
        clear();
        filter = newFilter;
    }

    public interface AddItem<T> {
        void add(String tlKey, T value);
    }
    public <T> void selection(String tlKey, Cons<AddItem<T>> addItem, Cons<T> onClick, T def) {
        if (!shouldAdd(tlKey)) return;

        root.table(Tex.button, t -> {
            t.margin(10f);
            ButtonGroup<?> group = new ButtonGroup<>();
            TextButton.TextButtonStyle style = Styles.flatTogglet;

            t.defaults().size(140f, 50f);

            final Seq<TextButton> buttons = new Seq<>();

            addItem.get(new AddItem<T>() {
                @Override
                public void add(String tlKey, T value) {
                    @SuppressWarnings("unchecked")
                    Cell<TextButton>[] button = new Cell[1]; // Java
                    button[0] = t.button(Core.bundle.get(tlKey), style, () -> {
                        buttons.each(x -> x.setChecked(false));
                        button[0].get().setChecked(true);
                        onClick.get(value);
                    }).group(group);
                    button[0].get().setChecked(value == null ? def == null : value.equals(def));
                    if (buttons.size % 3 == 2) button[0].row();
                    buttons.add(button[0].get());
                }
            });
        }).left().pad(6).fill(false).expand(false, false).row();
    }

    public static class BoolCtl {
        public static final BoolCtl throwaway = new BoolCtl();

        public Boolp enabled = TRUE;
        public BoolCtl enabled(Boolp value) {
            enabled = value;
            return this;
        }
    }
    public BoolCtl w(String tlKey, Boolp def, Boolc onClick) {
        if (!shouldAdd(tlKey)) return BoolCtl.throwaway;

        final BoolCtl ctl = new BoolCtl();

        Cell<CheckBox> cell = root.check(Core.bundle.get(tlKey), onClick).checked(def.get())
                .update(it -> {
                    it.setChecked(def.get());
                    it.setDisabled(!ctl.enabled.get());
                });
        cell.pad(6).get().left().row();
        root.row();

        return ctl;
    }
    public BoolCtl b(String tlKey, Boolp def, Boolc onClick) {
        return w(tlKey, def, onClick);
    }

    public static class IntCtl {
        public static final IntCtl throwaway = new IntCtl();

        private Label label;

        public Boolp enabled = TRUE;
        public IntCtl enabled(Boolp value) {
            enabled = value;
            return this;
        }

        public int min = Integer.MIN_VALUE;
        public IntCtl min(int value) {
            min = value;
            return this;
        }

        public int max = Integer.MAX_VALUE;
        public IntCtl max(int value) {
            max = value;
            return this;
        }

        public IntCtl range(int min, int max) {
            min(min);
            max(max);
            return this;
        }

        public IntCtl label(String raw) {
            if (label != null) label.setText(raw);
            return this;
        }

        int _prevValue;
    }
    public IntCtl w(String tlKey, Intp def, Intc onClick) {
        if (!shouldAdd(tlKey)) return IntCtl.throwaway;

        final IntCtl ctl = new IntCtl();
        ctl._prevValue = def.get();

        Cell<Table> cell = root.table(t -> {
            t.left();
            ctl.label = t.add("@" + tlKey).left().padRight(5).marginTop(0).marginBottom(0)
                    .update(a -> a.setColor(ctl.enabled.get() ? Color.white : Color.gray)).get();
            t.field(ctl._prevValue + "", s -> {
                        int i = Strings.parseInt(s);
                        ctl._prevValue = i;
                        onClick.get(i);
                    })
                    .update(a -> {
                        a.setDisabled(!ctl.enabled.get());
                        int i = def.get();
                        if (ctl._prevValue != i) {
                            ctl._prevValue = i;
                            a.setText(i + "");
                        }
                    }).marginTop(0).marginBottom(0)
                    .valid(f -> Strings.parseInt(f) >= ctl.min && Strings.parseInt(f) <= ctl.max).width(120f).left();
        }).padTop(0);
        cell.pad(6).get().left().row();
        root.row();

        return ctl;
    }
    public IntCtl i(String tlKey, Intp def, Intc onClick) {
        return w(tlKey, def, onClick);
    }

    public static class FloatCtl {
        public static final FloatCtl throwaway = new FloatCtl();

        public Boolp enabled = TRUE;
        public FloatCtl enabled(Boolp value) {
            enabled = value;
            return this;
        }

        public float min = Float.MIN_VALUE;
        public FloatCtl min(float value) {
            min = value;
            return this;
        }

        public float max = Float.MAX_VALUE;
        public FloatCtl max(float value) {
            max = value;
            return this;
        }

        public FloatCtl range(float min, float max) {
            min(min);
            max(max);
            return this;
        }

        float _prevValue;
    }
    public FloatCtl w(String tlKey, Floatp def, Floatc onClick) {
        if (!shouldAdd(tlKey)) return FloatCtl.throwaway;

        final FloatCtl ctl = new FloatCtl();
        ctl._prevValue = def.get();

        Cell<Table> cell = root.table(t -> {
            t.left();
            t.add("@" + tlKey).left().padRight(5).marginTop(0).marginBottom(0)
                    .update(a -> a.setColor(ctl.enabled.get() ? Color.white : Color.gray));
            t.field(ctl._prevValue + "", s -> {
                        float f = Strings.parseFloat(s);
                        ctl._prevValue = f;
                        onClick.get(f);
                    })
                    .update(a -> {
                        a.setDisabled(!ctl.enabled.get());
                        float f = def.get();
                        if (f != ctl._prevValue) {
                            ctl._prevValue = f;
                            a.setText(f + "");
                        }
                    }).marginTop(0).marginBottom(0)
                    .valid(f -> Strings.parseFloat(f) >= ctl.min && Strings.parseFloat(f) <= ctl.max).width(120f).left();
        }).padTop(0);
        cell.pad(6).padTop(0).get().left().row().marginTop(0);
        root.row();

        return ctl;
    }
    public FloatCtl f(String tlKey, Floatp def, Floatc onClick) {
        return w(tlKey, def, onClick);
    }

    public void loadout(String tlKey, int capacity, Prov<Seq<ItemStack>> loadout, Boolf<Item> verifier,
                        Runnable resetter, Runnable updater, Runnable hider) {
        button(tlKey, () -> loadoutDialog.show(capacity, loadout.get(), verifier,
                resetter, updater, hider));
    }

    public void color(String tlKey, Prov<Color> def, Cons<Color> onClick) {
        if (!shouldAdd(tlKey)) return;

        root.button(b -> {
            b.left();
            b.table(Tex.pane, in -> in.stack(new Image(Tex.alphaBg), new Image(Tex.whiteui) {{
                update(() -> setColor(def.get()));
            }}).grow()).margin(4).size(50).padRight(10);
            b.add("@"+tlKey);
        }, () -> Vars.ui.picker.show(def.get(), onClick)).left().width(300f).padLeft(6).row();
    }

    public void button(String tlKey, Runnable click) {
        if (!shouldAdd(tlKey)) return;

        root.button("@" + tlKey, click).left().width(300f).padLeft(6).row();
    }

    public void teams(String tlKey, Cons2<Team, RulesWrite> sectionConf, Boolf<Team> enabled) {
        if (!shouldAdd(tlKey)) return;

        final Table table = new Table();
        root.add(table).fillX().row();

        class TeamData {
            boolean shown;
            final Table container = new Table();

            TextureRegionDrawable buttonIcon() { return shown ? Icon.downOpen : Icon.upOpen; }

            TeamData(final Team team) { this(team, false); }
            TeamData(final Team team, final boolean shown) {
                this.shown = shown;

                container.button(team.coloredName(), Icon.downOpen, Styles.togglet, () -> this.shown = !this.shown)
                    .marginLeft(14).width(260).height(55).update(t -> {
                        ((Image) t.getChildren().get(1)).setDrawable(buttonIcon());
                        t.setChecked(this.shown);
                    }).left().padBottom(2).row();
                container.collapser(c -> {
                    c.defaults().left();
                    RulesWrite write = new RulesWrite(c, filter);
                    write.parent = RulesWrite.this;
                    sectionConf.get(team, write);
                }, () -> this.shown).left().fillX().row();
                table.add(container).fillX().padLeft(6).row();
            }

            void remove() {
                table.removeChild(container);
            }
        }

        final OrderedMap<Team, TeamData> dataList = new OrderedMap<>();

        for (Team team : Team.baseTeams) {
            dataList.put(team, new TeamData(team));
        }
        for (Team team : Team.all) {
            if (!enabled.get(team)) continue;
            if (dataList.containsKey(team)) continue;
            dataList.put(team, new TeamData(team));
        }
    }

    public static class BlockCtl {
        private Boolp enabled = TRUE;
        private Boolf<Block> filter = block -> true;

        public BlockCtl enabled(Boolp value) {
            enabled = value;
            return this;
        }
        public BlockCtl filter(Boolf<Block> value) {
            filter = value;
            return this;
        }
    }
    public BlockCtl block(String tlKey, Cons<Block> onClick, Prov<Block> def) {
        if (!shouldAdd(tlKey)) return new BlockCtl();

        BlockCtl ctl = new BlockCtl();
        final Table[] table = new Table[1];

        root.button(b -> {
            b.left();
            table[0] = b.table(Tex.pane, in -> in.stack(new Image(Tex.alphaBg), new Image(new TextureRegionDrawable(def.get().uiIcon, 4f / Math.min(def.get().uiIcon.width, def.get().uiIcon.height))) {{
                update(() -> {
                    b.setDisabled(!ctl.enabled.get());
                    b.setChecked(false);
                });
            }}).grow()).margin(4).size(50).padRight(10).get();
            b.add("@"+tlKey);
        }, () -> new BlockSelectDialog("@" + tlKey).show(ctl.filter, block -> {
            Image image = (Image) ((Stack) (table[0].getChildren().get(0))).getChildren().get(1);
            image.setDrawable(new TextureRegionDrawable(block.uiIcon, 4f / Math.min(block.uiIcon.width, block.uiIcon.height)));
            onClick.get(block);
        }, def.get())).left().width(300f).padLeft(6).row();

        // root.table(t -> {
        //     t.left();
        //     t.add("@" + tlKey).left().padRight(5).marginTop(0).marginBottom(0)
        //             .update(a -> a.setColor(ctl.enabled.get() ? Color.white : Color.gray));
        //     final ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNonei);
        //     button.getStyle().imageUp = new TextureRegionDrawable(def.get().uiIcon);
        //     button.resizeImage(32f);
        //     button.setSize(32f, 32f);
        //     button.update(() -> {
        //         button.setDisabled(!ctl.enabled.get());
        //         button.setChecked(false);
        //     });
        //     button.clicked(() -> {
        //         BlockSelectDialog dialog = new BlockSelectDialog("@" + tlKey);
        //         dialog.show(ctl.filter, block -> {
        //             onClick.get(block);
        //             button.getStyle().imageUp = new TextureRegionDrawable(block.uiIcon);
        //             button.resizeImage(32f);
        //         }, def.get());
        //     });
        //     t.add(button).left();
        // }).left().padLeft(6).row();

        return ctl;
    }

    public void spacer() {
        if (!canPlaceSpacer) return;
        canPlaceSpacer = false;

        root.table().fillX().height(16f).minHeight(16f).maxHeight(16f);
        root.row();
    }

    public RulesWrite table() {
        RulesWrite write = new RulesWrite(new Table(), filter);
        write.parent = this;
        write.canPlaceSpacer = canPlaceSpacer;
        write.root.left().defaults().left().pad(0);
        root.add(write.root).fillX().left().row();
        return write;
    }

    public RulesWrite category(String name) {
        RulesWrite write = new RulesWrite(new Table(), filter);
        write.parent = this;
        write.categoryNameToAdd = name;
        canPlaceSpacer = false;
        write.root.left().defaults().left().pad(0);
        return write;
    }
    public void category(String name, Cons<RulesWrite> cb) {
        cb.get(category(name));
    }
}
