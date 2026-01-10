package mindurka.ui;

import arc.Core;
import arc.func.Intc;
import arc.scene.event.EventListener;
import arc.scene.event.VisibilityListener;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Reflect;
import mindurka.MVars;
import mindurka.rules.Gamemode;
import mindustry.Vars;
import mindustry.editor.MapEditorDialog;
import mindustry.editor.MapInfoDialog;
import mindustry.game.Rules;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CustomRulesDialog;

public class OCustomRulesDialog extends CustomRulesDialog {
    private Table main;
    private RulesWrite writeRoot;

    public OCustomRulesDialog() {
        super();

        {
            Seq<EventListener> listeners = getListeners();
            for (int i = listeners.size - 1; i >= 0; i--) {
                EventListener listener = listeners.get(i);
                if (!(listener instanceof VisibilityListener)) continue;
                removeListener(listener);
                break;
            }
        }

        shown(this::setup);
    }

    public static void inject() {
        MapInfoDialog infoDialog = Reflect.get(MapEditorDialog.class, Vars.ui.editor, "infoDialog");
        CustomRulesDialog customRulesDialog = MVars.customRulesDialog = new OCustomRulesDialog();
        Reflect.set(infoDialog, "ruleInfo", customRulesDialog);
    }

    void setup() {
        cont.clear();
        cont.table(t -> {
            t.add("@search").padRight(10);
            TextField field = t.field(ruleSearch, text -> {
                ruleSearch = text.trim().replaceAll(" +", " ").toLowerCase();
                build();
            }).width(200f).pad(8).get();
            field.setCursorPosition(ruleSearch.length());
            Core.scene.setKeyboardFocus(field);
            t.button(Icon.cancel, Styles.emptyi, () -> {
                ruleSearch = "";
                build();
            }).padLeft(10f).size(35f);
        }).fillX().row();
        Cell<ScrollPane> paneCell = cont.pane(m -> main = m);
        writeRoot = new RulesWrite(main, "");

        build();

        paneCell.scrollX(main.getPrefWidth() + 40f > Core.graphics.getWidth());
    }

    void build() {
        writeRoot.clear(ruleSearch);
        main.table().minWidth(500f).maxWidth(500f).width(500f).row();
        RulesWrite write;

        final Rules rules = Vars.state.rules;

        write = writeRoot.category("waves");
        write.w("rules.waves", () -> rules.waves, b -> rules.waves = b);
        write.w("rules.wavesending", () -> rules.waveSending, b -> rules.waveSending = b).enabled(() -> rules.waves);
        write.w("rules.wavetimer", () -> rules.waveTimer, b -> rules.waveTimer = b).enabled(() -> rules.waves);
        write.w("rules.waitForWaveToEnd", () -> rules.waitEnemies, b -> rules.waitEnemies = b).enabled(() -> rules.waves && rules.waveTimer);
        write.w("rules.randomwaveai", () -> rules.randomWaveAI, b -> rules.randomWaveAI = b).enabled(() -> rules.waves);
        write.w("rules.wavespawnatcores", () -> rules.wavesSpawnAtCores, b -> rules.wavesSpawnAtCores = b).enabled(() -> rules.waves);
        write.w("rules.airUseSpawns", () -> rules.airUseSpawns, b -> rules.airUseSpawns = b).enabled(() -> rules.waves);
        write.i("rules.wavelimit", () -> rules.winWave, i -> rules.winWave = i).min(0).enabled(() -> rules.waves);
        write.f("rules.wavespacing", () -> rules.waveSpacing / 60, f -> rules.waveSpacing = f * 60).min(0).enabled(() -> rules.waves && rules.waveTimer);
        write.f("rules.initialwavespacing", () -> rules.initialWaveSpacing / 60, f -> rules.initialWaveSpacing = f * 60).min(1).enabled(() -> rules.waves && rules.waveTimer);
        write.f("rules.dropzoneradius", () -> rules.dropZoneRadius / Vars.tilesize, f -> rules.dropZoneRadius = f * Vars.tilesize).min(0).enabled(() -> rules.waves);

        write = writeRoot.category("mindurka");
        {
            final Runnable[] extra = new Runnable[1];

            write.selection("rules.title.mindurka", addItem -> {
                addItem.add("mindurka.gamemode.none", null);
                for (String gamemodeName : Gamemode.keys()) {
                    Gamemode gamemode = Gamemode.forName(gamemodeName);
                    addItem.add("mindurka.gamemode." + gamemodeName, gamemode);
                }
            }, value -> {
                MVars.rules.gamemode(value);
                extra[0].run();
            }, MVars.rules.gamemodeFactory());

            {
                RulesWrite extraWrite = write.table();
                extra[0] = () -> {
                    extraWrite.clear();
                    @Nullable Gamemode.Impl gamemode = MVars.rules.gamemode();
                    if (gamemode != null) gamemode.writeGamemodeRules(extraWrite);
                };
                extra[0].run();
            }
        }

        // category("mindurka");

        // if (Core.bundle.get("rules.title.mindurka").toLowerCase().contains(ruleSearch)) {
        //     current.table(Tex.button, t -> {
        //         t.margin(10f);
        //         ButtonGroup<?> group = new ButtonGroup<>();
        //         TextButton.TextButtonStyle style = Styles.flatTogglet;

        //         t.defaults().size(140f, 50f);

        //         for (String gamemodeName : Gamemode.keys()) {
        //             Gamemode.Factory gamemode = Gamemode.forName(gamemodeName);
        //             if (gamemode == Gamemode.UNKNOWN) continue;

        //             t.button(Core.bundle.get("mindurka.gamemode." + gamemode.name()), style, () -> {
        //                 MVars.rules.gamemode(gamemode);
        //                 updateExtraRules();
        //             }).group(group).checked(b -> MVars.rules.ga);

        //             if (t.getChildren().size % 3 == 0) {
        //                 t.row();
        //             }
        //         }

        //         t.button("@mindurka.gamemode.unknown", style, () -> {
        //             MVars.rules.gamemode = MRules.Gamemode.unknown;
        //             MVars.rules.gamemode.save();
        //             MVars.rules.gamemode.assign(Vars.state.rules);
        //             updateExtraRules();
        //         }).group(group).checked(b -> MVars.rules.gamemode == MRules.Gamemode.unknown);
        //     }).left().fill(false).expand(false, false).row();
        // }

        // current.table(t -> gamemodeRules = t).padTop(0).expandX();
        // updateExtraRules();

        // addToMain(current, Core.bundle.get("rules.title.mindurka"));
    }

    // void updateExtraRules() {
    //     gamemodeRules.clear();
    //     gamemodeRules.left().defaults().fillX().left();
    //     gamemodeRules.row();

    //     Table prevCurrent = current;
    //     current = gamemodeRules;

    //     if (MVars.rules.gamemode == MRules.Gamemode.forts) {
    //         check("@rules.mindurka.impactReactorEnabled", b -> {
    //             MVars.rules.impactReactorEnabled = b;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.impactReactorEnabled);
    //         number("@rules.mindurka.impactReactorShieldDuration", f -> {
    //             MVars.rules.impactReactorShieldDuration = f;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.impactReactorShieldDuration, () -> MVars.rules.impactReactorEnabled);
    //         number("@rules.mindurka.impactReactorCooldown", f -> {
    //             MVars.rules.impactReactorCooldown = f;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.impactReactorCooldown, () -> MVars.rules.impactReactorEnabled);
    //         number("@rules.mindurka.impactReactorDelay", f -> {
    //             MVars.rules.impactReactorDelay = f;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.impactReactorDelay, () -> MVars.rules.impactReactorEnabled);

    //         if (current.hasChildren()) {
    //             current.table(t -> {
    //                 t.setHeight(4f);
    //                 t.left();
    //             }).padTop(0);
    //             current.row();
    //         }

    //         check("@rules.mindurka.thorReactorEnabled", b -> {
    //             MVars.rules.thorReactorEnabled = b;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.thorReactorEnabled);
    //         number("@rules.mindurka.thorReactorPowerMultiplier", f -> {
    //             MVars.rules.thorReactorPowerMultiplier = f;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.thorReactorPowerMultiplier, () -> MVars.rules.thorReactorEnabled);
    //         number("@rules.mindurka.thorReactorCooldown", f -> {
    //             MVars.rules.thorReactorCooldown = f;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.thorReactorCooldown, () -> MVars.rules.thorReactorEnabled);
    //         number("@rules.mindurka.thorReactorDelay", f -> {
    //             MVars.rules.thorReactorDelay = f;
    //             MVars.rules.save();
    //         }, () -> MVars.rules.thorReactorDelay, () -> MVars.rules.thorReactorEnabled);

    //         FortsTeamsWidget.create(current, Vars.state.rules, MVars.rules);

    //         if (Core.bundle.get("editor.mindurka.fortsPlotKind").toLowerCase().contains(ruleSearch)) {
    //             current.table(Tex.button, t -> {
    //                 t.margin(10f);
    //                 ButtonGroup<?> group = new ButtonGroup<>();
    //                 TextButton.TextButtonStyle style = Styles.flatTogglet;

    //                 t.defaults().size(140f, 50f);

    //                 for (MRules.FortsPlotKind kind : MRules.FortsPlotKind.values()) {
    //                     t.button(Core.bundle.get("rules.mindurka.fortsPlotKind." + kind.name()), style, () -> {
    //                         MVars.rules.fortsPlotKind = kind;
    //                         MVars.rules.fortsPlotKind.reset(MVars.rules);
    //                         MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //                         updateFortsPlotProps();
    //                     }).group(group).checked(b -> MVars.rules.fortsPlotKind == kind);

    //                     if (t.getChildren().size % 3 == 0) {
    //                         t.row();
    //                     }
    //                 }
    //             }).left().fill(false).expand(false, false).row();
    //         }

    //         current.table(t -> fortsPlotProps = t).padTop(0).expandX();
    //         updateFortsPlotProps();
    //     }

    //     MVars.editorDialog.refreshTools();
    //     current = prevCurrent;
    // }

    // void updateFortsPlotProps() {
    //     fortsPlotProps.clear();
    //     fortsPlotProps.left().defaults().fillX().left();
    //     fortsPlotProps.row();

    //     Table prevCurrent = current;
    //     current = fortsPlotProps;

    //     if (MVars.rules.fortsPlotKind == MRules.FortsPlotKind.square) {
    //         numberi("@rules.mindurka.fortsPlotKind.square.size", i -> {
    //             MVars.rules.fortsPlotParam1i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam1i, 1, 16);
    //         numberi("@rules.mindurka.fortsPlotKind.square.wall", i -> {
    //             MVars.rules.fortsPlotParam3i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam3i, 0, 16);
    //         numberi("@rules.mindurka.fortsPlotKind.square.shiftX", i -> {
    //             MVars.rules.fortsPlotParam4i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam4i, 0, 16);
    //         numberi("@rules.mindurka.fortsPlotKind.square.shiftY", i -> {
    //             MVars.rules.fortsPlotParam5i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam5i, 0, 16);
    //     }

    //     if (MVars.rules.fortsPlotKind == MRules.FortsPlotKind.rect) {
    //         numberi("@rules.mindurka.fortsPlotKind.rect.width", i -> {
    //             MVars.rules.fortsPlotParam1i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam1i, 1, 16);
    //         numberi("@rules.mindurka.fortsPlotKind.rect.height", i -> {
    //             MVars.rules.fortsPlotParam2i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam2i, 1, 16);
    //         numberi("@rules.mindurka.fortsPlotKind.rect.wall", i -> {
    //             MVars.rules.fortsPlotParam3i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam3i, 0, 16);
    //         numberi("@rules.mindurka.fortsPlotKind.rect.shiftX", i -> {
    //             MVars.rules.fortsPlotParam4i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam4i, 0, 16);
    //         numberi("@rules.mindurka.fortsPlotKind.rect.shiftY", i -> {
    //             MVars.rules.fortsPlotParam5i = i;
    //             MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
    //         }, () -> MVars.rules.fortsPlotParam5i, 0, 16);
    //     }

    //     current = prevCurrent;
    // }

    // void addToMain(Table category, String title) {
    //     if (category.hasChildren()) {
    //         main.add(title).color(Pal.accent).padTop(20).padRight(100f).padBottom(-3).fillX().left().pad(5).row();
    //         main.image().color(Pal.accent).height(3f).padRight(100f).padBottom(20).fillX().left().pad(5).row();
    //         main.add(category).row();
    //     }
    // }
}
