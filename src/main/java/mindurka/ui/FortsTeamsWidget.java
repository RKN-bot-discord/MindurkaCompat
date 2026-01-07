package mindurka.ui;

import arc.Core;
import arc.func.Boolc;
import arc.func.Boolp;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Image;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import lombok.AllArgsConstructor;
import mindurka.Coder;
import mindurka.FortsTeamRules;
import mindurka.MRules;
import mindurka.MVars;
import mindustry.Vars;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public class FortsTeamsWidget {
    private FortsTeamsWidget(Table root, Rules rules, MRules customRules) {
        this.root = root;
        this.rules = rules;
        this.customRules = customRules;
    }

    private final Table root;
    private final Rules rules;
    private final MRules customRules;
    private final Seq<Team> trackedTeams = new Seq<>(8);

    private Table current;

    private static boolean contains(@Nullable Team team, Team[] teams) {
        if (team == null) return false;
        for (Team other : teams) if (team == other) return true;
        return false;
    }

    @AllArgsConstructor
    private static class ChildCollapserData {
        @Nullable Team team;
        final Table table;
        boolean shown;
        boolean fieldReplaced;
        boolean enabled;
        int teamId;
        FortsTeamRules teamRules;

        public @Nullable Team team() {
            if (teamId == -1) return null;
            return Team.all[teamId];
        }
    }

    private void rerenderChildCollapser(final ChildCollapserData d) {
        ((Table) d.table.getChildren().get(1)).clearChildren();
        ((Table) d.table.getChildren().get(1)).collapser(c -> {
            c.left().defaults().fillX().left().pad(5);

            current = c;

            check("@editor.mindurka.fortsTeam.enabled", e -> {
                assert d.team() != null;
                d.team = d.team();

                d.enabled = e;

                if (e) {
                    customRules.fortsTeamEnabled(d.team());
                    d.teamRules = FortsTeamRules.of(d.team());
                }
                else customRules.fortsTeamDisabled(d.team());

                if (!d.fieldReplaced) {
                    d.fieldReplaced = true;
                    trackedTeams.add(d.team());

                    Table table2 = (Table) (d.table.getChildren().get(0));

                    table2.clearChildren();
                    table2.button(d.team().coloredName(), Icon.downOpen, Styles.togglet, () -> {
                        d.shown = !d.shown;
                    }).update(t -> {
                        ((Image) t.getChildren().get(1)).setDrawable(d.shown ? Icon.upOpen : Icon.downOpen);
                        t.setChecked(d.shown);
                    }).growX().fillX();

                    childFor(null);
                }
            }, () -> {
                d.enabled = d.team != null && customRules.fortsTeamRules(d.team()) != null &&
                        d.teamId != -1 && (d.team == d.team() || !trackedTeams.contains(d.team()));
                return d.enabled;
            }, () -> d.teamId != -1 && (d.team == d.team() || !trackedTeams.contains(d.team())));
            Cell<CheckBox> playable = check("@editor.mindurka.fortsTeam.playable", e -> {
                d.teamRules.playable = e;
                d.teamRules.save(rules);
            }, () -> d.teamRules.playable, () -> d.enabled);

            Cell<TextButton> setPlot = current.button("@rules.mindurka.fortsPlotKind.setPlot", () -> {
                MVars.customRulesDialog.hide();

                int wallSize = MVars.rules.fortsWallSize();
                int width = MVars.rules.fortsPlotWidth() + wallSize * 2;
                int height = MVars.rules.fortsPlotWidth() + wallSize * 2;
                Team team = d.team;

                MVars.mapView.editorAction = new SpecialEditorAction() {
                    @Override
                    public boolean clicked(OMapView view, float mouseX, float mouseY) {
                        Point2 p = view.project(mouseX, mouseY);
                        if (p.x < 0 || p.y < 0
                                || p.x >= Vars.world.width() - width
                                || p.y >= Vars.world.height() - height) return false;
                        MVars.rules.fortsTeamRules(team).platformAreaString = Coder.encodeArea(Vars.world.tiles, p.x, p.y, width, height).unwrap();
                        // Sanity check
                        Coder.Result<Coder.Metrics> metrics = Coder.decodeArea(MVars.rules.fortsTeamRules(team).platformAreaString, team, null, 0, 0);
                        if (metrics instanceof Coder.Result.Err) {
                            String error = ((Coder.Result.Err<Coder.Metrics>) metrics).error;
                            Log.err("Failed to encode plot!");
                            Log.err("For string: " + MVars.rules.fortsTeamRules(team).platformAreaString);
                            Log.err(error);
                            Vars.ui.showErrorMessage("Failed to encode plot!\n\nFor string: " + MVars.rules.fortsTeamRules(team).platformAreaString + "\n\n" + error);
                        } else if (metrics.unwrap().width != width || metrics.unwrap().height != height) {
                            Vars.ui.showErrorMessage("Failed to encode plot!\n\nFor string: " + MVars.rules.fortsTeamRules(team).platformAreaString + "\n\nSize mismatch");
                        }
                        Log.info("Successfully set plot schematic");
                        return true;
                    }

                    @Override
                    public void preview(OMapView view, float mouseX, float mouseY) {
                        Point2 p = view.project(mouseX, mouseY);
                        int px = p.x, py = p.y;
                        Vec2 s = view.unproject(px, py);
                        float sx = s.x, sy = s.y;
                        s = view.unproject(px + width, py + height);

                        Draw.color(Pal.accent);
                        Lines.stroke(Scl.scl(2f));
                        Lines.rect(sx, sy, s.x - sx, s.y - sy);
                        Draw.reset();
                    }
                };
            });
            setPlot.update(button -> button.setDisabled(!d.enabled));
        }, () -> d.shown).left().growX().row();
    }

    private void childFor(@Nullable Team team) {
        final ChildCollapserData d = new ChildCollapserData(team, new Table(), false, true, false, team == null ? -1 : team.id, FortsTeamRules.loadOrDefault(team, rules, customRules));

        d.table.table(table2 -> {
            if (team == null) {
                d.fieldReplaced = false;
                TextButton button = table2.button("", Icon.downOpen, Styles.togglet, () -> {
                    d.shown = !d.shown;
                }).update(t -> {
                    ((Image) t.getChildren().get(0)).setDrawable(d.shown ? Icon.upOpen : Icon.downOpen);
                    t.setChecked(d.shown);
                }).growX().fillX().get();
                Image image = (Image) button.getChildren().get(1);
                button.clearChildren();
                button.add(image);
                button.field("", t -> {
                    try {
                        d.teamId = Integer.parseInt(t, 10);
                    } catch (NumberFormatException ignored) {
                        d.teamId = -1;
                    }
                })
                    .growX().fillX()
                    .pad(0)
                    .valid(f -> f.isEmpty() || Strings.parseInt(f, -1) >= 0 && Strings.parseInt(f, Team.all.length) < Team.all.length - 1);
            } else {
                table2.button(team.coloredName(), Icon.downOpen, Styles.togglet, () -> {
                    d.shown = !d.shown;
                }).update(t -> {
                    ((Image) t.getChildren().get(1)).setDrawable(d.shown ? Icon.upOpen : Icon.downOpen);
                    t.setChecked(d.shown);
                }).growX().fillX();
            }
        }).marginLeft(14f).width(260f).height(55f).left().padBottom(2f).row();

        d.table.table().left().growX().row();
        rerenderChildCollapser(d);

        root.add(d.table).padTop(0).left().fillX().growX().row();
    }

    public static FortsTeamsWidget create(Table parent, Rules rules, MRules customRules) {
        Table[] root = new Table[1];
        parent.table(t -> {
            root[0] = t;
        }).padTop(0).fillX().growX().left().row();
        FortsTeamsWidget widget = new FortsTeamsWidget(root[0], rules, customRules);

        for (Team team : Team.all) {
            if (!contains(team, Team.baseTeams) && !customRules.fortsTeamRules.containsKey(team.id)) continue;
            widget.trackedTeams.add(team);
            widget.childFor(team);
        }

        widget.childFor(null);

        return widget;
    }

    public void check(String text, Boolc cons, Boolp prov) {
        check(text, cons, prov, () -> true);
    }

    public Cell<CheckBox> check(String text, Boolc cons, Boolp prov, Boolp condition) {
        Cell<CheckBox> cell = current.check(text, cons).checked(prov.get()).update(a -> a.setDisabled(!condition.get()));
        cell.get().left();
        if (Vars.mobile && !Core.graphics.isPortrait()) { //disabled in portrait - broken and goes offscreen
            Table table = new Table();
            table.add(cell.get()).left().expandX().fillX();
            cell.clearElement();
            table.button(Icon.infoSmall, () -> Vars.ui.showInfo(text + ".info")).size(32f).right();
            cell.setElement(table).left().expandX().fillX();
        } else {
            cell.tooltip(text + ".info");
        }
        current.row();
        return cell;
    }
}
