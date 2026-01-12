package mindurka.rules;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.util.Log;
import mindurka.MVars;
import mindustry.game.Team;

import java.util.Arrays;

public class FortsRectangularStates {
    private final Color fillColor = new Color();

    private final FortsPlotState[] states;
    private final Team[] teams;
    private final int startX;
    private final int startY;
    private final int jX;
    private final int jY;
    private final int plotsX;
    private final int plotsY;
    private final int width;
    private final int height;

    public FortsRectangularStates(int width, int height, int wallSize, int shiftX, int shiftY,
                                  int mapWidth, int mapHeight, String statesData) {
        jX = width + wallSize;
        jY = height + wallSize;

        startX = ((shiftX % jX) + jX) % jX;
        startY = ((shiftY % jY) + jY) % jY;

        plotsX = (mapWidth - startX + wallSize) / jX;
        plotsY = (mapHeight - startY + wallSize) / jY;

        states = new FortsPlotState[plotsX * plotsY];
        teams = new Team[plotsX * plotsY];
        Arrays.fill(states, FortsPlotState.enabled);
        Arrays.fill(teams, Team.derelict);

        this.width = width;
        this.height = height;

        if (statesData.length() != states.length * 3) return;
        for (int i = 0; i < states.length; i++) {
            char ch = statesData.charAt(i * 3);
            if (ch < 'a' || ch - 'a' >= FortsPlotState.values().length) return;

            char a = statesData.charAt(i * 3 + 1);
            char b = statesData.charAt(i * 3 + 2);

            if (!(a >= '0' && a <= '9') && !(a >= 'a' && a <= 'f')) return;
            if (!(b >= '0' && b <= '9') && !(b >= 'a' && b <= 'f')) return;
        }
        for (int i = 0; i < states.length; i++) {
            char ch = statesData.charAt(i * 3);
            states[i] = FortsPlotState.values()[ch - 'a'];

            char a = statesData.charAt(i * 3 + 1);
            char b = statesData.charAt(i * 3 + 2);

            int team = ((a >= '0' && a <= '9') ? a - '0' : a - 'a' + 10) * 16 +
                    ((b >= '0' && b <= '9') ? b - '0' : b - 'a' + 10);
            teams[i] = Team.all[team];
        }
    }

    public void drawEditorGuides() {
        Color stroke;
        for (int i = 0; i < plotsX; i++) for (int o = 0; o < plotsY; o++) {
            int x = startX + jX * i;
            int y = startY + jY * o;

            Vec2 s = MVars.mapView.unproject(x, y);
            float sx = s.x, sy = s.y;
            s = MVars.mapView.unproject(x + width, y + height);

            Team team = teams[i + o * plotsX];
            FortsPlotState state = states[i + o * plotsX];

            stroke = state.stroke();

            if (state.fill(fillColor, team).a != 0) {
                Draw.color(fillColor);
                float w = s.x - sx;
                float h = s.y - sy;
                Fill.rect(sx + w / 2, sy + h / 2, w, h);
            }
            Draw.color(stroke);
            Lines.stroke(Scl.scl(1f));
            Lines.rect(sx, sy, s.x - sx, s.y - sy);
            Draw.reset();
        }
    }

    public void setPlotInfo(int x, int y, FortsPlotState state, Team team) {
        if (x < startX || y < startY) return;
        int plotX = (x - startX) / jX;
        int plotY = (y - startY) / jY;
        if (plotX >= plotsX || plotY >= plotsY) return;
        if ((x - startX) % jX >= width || (y - startY) % jY >= height) return;

        int i = plotX + plotY * plotsX;

        states[i] = state;
        teams[i] = team;
    }

    public String save() {
        StringBuilder str = new StringBuilder(states.length * 3);
        for (int i = 0; i < states.length; i++) {
            str.append((char) ('a' + states[i].ordinal()));

            int b = teams[i].id;
            int a = b / 16;
            b %= 16;

            str.append((char) (a >= 10 ? a + 'a' - 10 : a + '0'));
            str.append((char) (b >= 10 ? b + 'a' - 10 : b + '0'));
        }
        return str.toString();
    }
}
