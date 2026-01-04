package mindurka.ui;

import arc.Core;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import mindurka.MRules;
import mindurka.MVars;
import mindustry.Vars;
import mindustry.editor.MapEditorDialog;
import mindustry.editor.MapInfoDialog;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CustomRulesDialog;

public class OCustomRulesDialog extends CustomRulesDialog {
    private Table main;

    private Table gamemodeRules;
    private Table fortsPlotProps;

    public OCustomRulesDialog() {
        super();

        shown(this::setup);
    }

    public static void inject() {
        MapInfoDialog infoDialog = Reflect.get(MapEditorDialog.class, Vars.ui.editor, "infoDialog");
        CustomRulesDialog customRulesDialog = MVars.customRulesDialog = new OCustomRulesDialog();
        Reflect.set(infoDialog, "ruleInfo", customRulesDialog);
    }

    void setup() {
        // I mean hey it is the second element, so I guess that works right?
        {
            ScrollPane pane = (ScrollPane) (cont.getChildren().get(1));
            main = (Table) pane.getWidget();
        }

        category("mindurka");

        if (Core.bundle.get("rules.title.mindurka").toLowerCase().contains(ruleSearch)) {
            current.table(Tex.button, t -> {
                t.margin(10f);
                ButtonGroup<?> group = new ButtonGroup<>();
                TextButton.TextButtonStyle style = Styles.flatTogglet;

                t.defaults().size(140f, 50f);

                for (MRules.Gamemode gamemode : MRules.Gamemode.values()) {
                    if (gamemode == MRules.Gamemode.unknown) continue;

                    t.button(Core.bundle.get("mindurka.gamemode." + gamemode.name()), style, () -> {
                        MVars.rules.gamemode = gamemode;
                        MVars.rules.gamemode.save();
                        MVars.rules.gamemode.assign(Vars.state.rules);
                        updateExtraRules();
                    }).group(group).checked(b -> MVars.rules.gamemode == gamemode);

                    if (t.getChildren().size % 3 == 0) {
                        t.row();
                    }
                }

                t.button("@mindurka.gamemode.unknown", style, () -> {
                    MVars.rules.gamemode = MRules.Gamemode.unknown;
                    MVars.rules.gamemode.save();
                    MVars.rules.gamemode.assign(Vars.state.rules);
                    updateExtraRules();
                }).group(group).checked(b -> MVars.rules.gamemode == MRules.Gamemode.unknown);
            }).left().fill(false).expand(false, false).row();
        }

        current.table(t -> gamemodeRules = t).padTop(0).expandX();
        updateExtraRules();

        addToMain(current, Core.bundle.get("rules.title.mindurka"));
    }

    void updateExtraRules() {
        gamemodeRules.clear();
        gamemodeRules.left().defaults().fillX().left();
        gamemodeRules.row();

        Table prevCurrent = current;
        current = gamemodeRules;

        if (MVars.rules.gamemode == MRules.Gamemode.forts) {
            check("@rules.mindurka.impactReactorEnabled", b -> {
                MVars.rules.impactReactorEnabled = b;
                MVars.rules.save();
            }, () -> MVars.rules.impactReactorEnabled);
            number("@rules.mindurka.impactReactorShieldDuration", f -> {
                MVars.rules.impactReactorShieldDuration = f;
                MVars.rules.save();
            }, () -> MVars.rules.impactReactorShieldDuration, () -> MVars.rules.impactReactorEnabled);
            number("@rules.mindurka.impactReactorCooldown", f -> {
                MVars.rules.impactReactorCooldown = f;
                MVars.rules.save();
            }, () -> MVars.rules.impactReactorCooldown, () -> MVars.rules.impactReactorEnabled);
            number("@rules.mindurka.impactReactorDelay", f -> {
                MVars.rules.impactReactorDelay = f;
                MVars.rules.save();
            }, () -> MVars.rules.impactReactorDelay, () -> MVars.rules.impactReactorEnabled);

            if (current.hasChildren()) {
                current.table(t -> {
                    t.setHeight(4f);
                    t.left();
                }).padTop(0);
                current.row();
            }

            check("@rules.mindurka.thorReactorEnabled", b -> {
                MVars.rules.thorReactorEnabled = b;
                MVars.rules.save();
            }, () -> MVars.rules.thorReactorEnabled);
            number("@rules.mindurka.thorReactorPowerMultiplier", f -> {
                MVars.rules.thorReactorPowerMultiplier = f;
                MVars.rules.save();
            }, () -> MVars.rules.thorReactorPowerMultiplier, () -> MVars.rules.thorReactorEnabled);
            number("@rules.mindurka.thorReactorCooldown", f -> {
                MVars.rules.thorReactorCooldown = f;
                MVars.rules.save();
            }, () -> MVars.rules.thorReactorCooldown, () -> MVars.rules.thorReactorEnabled);
            number("@rules.mindurka.thorReactorDelay", f -> {
                MVars.rules.thorReactorDelay = f;
                MVars.rules.save();
            }, () -> MVars.rules.thorReactorDelay, () -> MVars.rules.thorReactorEnabled);

            FortsTeamsWidget.create(current, Vars.state.rules, MVars.rules);

            if (Core.bundle.get("editor.mindurka.fortsPlotKind").toLowerCase().contains(ruleSearch)) {
                current.table(Tex.button, t -> {
                    t.margin(10f);
                    ButtonGroup<?> group = new ButtonGroup<>();
                    TextButton.TextButtonStyle style = Styles.flatTogglet;

                    t.defaults().size(140f, 50f);

                    for (MRules.FortsPlotKind kind : MRules.FortsPlotKind.values()) {
                        t.button(Core.bundle.get("rules.mindurka.fortsPlotKind." + kind.name()), style, () -> {
                            MVars.rules.fortsPlotKind = kind;
                            MVars.rules.fortsPlotKind.reset(MVars.rules);
                            MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
                            updateFortsPlotProps();
                        }).group(group).checked(b -> MVars.rules.fortsPlotKind == kind);

                        if (t.getChildren().size % 3 == 0) {
                            t.row();
                        }
                    }
                }).left().fill(false).expand(false, false).row();
            }

            current.table(t -> fortsPlotProps = t).padTop(0).expandX();
            updateFortsPlotProps();
        }

        MVars.editorDialog.refreshTools();
        current = prevCurrent;
    }

    void updateFortsPlotProps() {
        fortsPlotProps.clear();
        fortsPlotProps.left().defaults().fillX().left();
        fortsPlotProps.row();

        Table prevCurrent = current;
        current = fortsPlotProps;

        if (MVars.rules.fortsPlotKind == MRules.FortsPlotKind.square) {
            numberi("@rules.mindurka.fortsPlotKind.square.size", i -> {
                MVars.rules.fortsPlotParam1i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam1i, 1, 16);
            numberi("@rules.mindurka.fortsPlotKind.square.wall", i -> {
                MVars.rules.fortsPlotParam3i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam3i, 0, 16);
            numberi("@rules.mindurka.fortsPlotKind.square.shiftX", i -> {
                MVars.rules.fortsPlotParam4i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam4i, 0, 16);
            numberi("@rules.mindurka.fortsPlotKind.square.shiftY", i -> {
                MVars.rules.fortsPlotParam5i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam5i, 0, 16);
        }

        if (MVars.rules.fortsPlotKind == MRules.FortsPlotKind.rect) {
            numberi("@rules.mindurka.fortsPlotKind.rect.width", i -> {
                MVars.rules.fortsPlotParam1i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam1i, 1, 16);
            numberi("@rules.mindurka.fortsPlotKind.rect.height", i -> {
                MVars.rules.fortsPlotParam2i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam2i, 1, 16);
            numberi("@rules.mindurka.fortsPlotKind.rect.wall", i -> {
                MVars.rules.fortsPlotParam3i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam3i, 0, 16);
            numberi("@rules.mindurka.fortsPlotKind.rect.shiftX", i -> {
                MVars.rules.fortsPlotParam4i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam4i, 0, 16);
            numberi("@rules.mindurka.fortsPlotKind.rect.shiftY", i -> {
                MVars.rules.fortsPlotParam5i = i;
                MVars.rules.fortsPlotKind.save(Vars.state.rules, MVars.rules);
            }, () -> MVars.rules.fortsPlotParam5i, 0, 16);
        }

        current = prevCurrent;
    }

    void addToMain(Table category, String title) {
        if (category.hasChildren()) {
            main.add(title).color(Pal.accent).padTop(20).padRight(100f).padBottom(-3).fillX().left().pad(5).row();
            main.image().color(Pal.accent).height(3f).padRight(100f).padBottom(20).fillX().left().pad(5).row();
            main.add(category).row();
        }
    }
}
