package mindurka.rules;

import mindurka.ui.RulesWrite;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.maps.Map;

public class FortsPlotKindSquare extends FortsPlotKind {
    public static final String NAME = "square";
    public static final String SIZE = PREFIX+".size";
    public static final String WALLS_SIZE = PREFIX+".walls_size";
    public static final String SHIFT_X = PREFIX+".shift_x";
    public static final String SHIFT_Y = PREFIX+".shift_y";
    public static final String DATA = PREFIX+".states";

    public class Impl extends FortsPlotKind.Impl implements FortsPlotKindRectangular {
        protected Impl(RulesContext rc) {
            super(rc);

            final Rules rules = rc.rules;

            String plotStatesS;

            try (TagRead read = TagRead.of(rules)) {
                size = read.r(SIZE, 6);
                wallsSize = read.r(WALLS_SIZE, 1);
                shiftX = read.r(SHIFT_X, 0);
                shiftY = read.r(SHIFT_Y, 0);
                plotStatesS = read.r(DATA, "");
            }

            plotStates = new FortsRectangularStates(size, size, wallsSize, shiftX, shiftY,
                    rc.mapWidth, rc.mapHeight, plotStatesS);
        }

        private void changePlotStates() {
            String plotStatesS;

            try (TagRead read = TagRead.of(rc.rules)) {
                plotStatesS = read.r(DATA, "");
            }

            plotStates = new FortsRectangularStates(size, size, wallsSize, shiftX, shiftY,
                    rc.mapWidth, rc.mapHeight, plotStatesS);
        }

        private int size;
        public int size() { return size; }
        @Override public int plotWidth() { return size; }
        @Override public int plotHeight() { return size; }
        public Impl size(int value) {
            size = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(SIZE, value); }
            changePlotStates();
            return this;
        }

        private int wallsSize;
        @Override public int wallsSize() { return wallsSize; }
        public Impl wallsSize(int value) {
            wallsSize = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(WALLS_SIZE, value); }
            changePlotStates();
            return this;
        }

        private int shiftX;
        @Override public int shiftX() { return shiftX; }
        public Impl shiftX(int value) {
            shiftX = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(SHIFT_X, value); }
            changePlotStates();
            return this;
        }

        private int shiftY;
        @Override public int shiftY() { return shiftY; }
        public Impl shiftY(int value) {
            shiftY = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(SHIFT_Y, value); }
            changePlotStates();
            return this;
        }

        private FortsRectangularStates plotStates;

        @Override
        void remove() {
            rc.rules.tags.remove(SIZE);
            rc.rules.tags.remove(WALLS_SIZE);
            rc.rules.tags.remove(SHIFT_X);
            rc.rules.tags.remove(SHIFT_Y);
            rc.rules.tags.remove(DATA);
        }

        @Override
        public void writeRules(RulesWrite write) {
            write.i(SIZE, this::size, this::size);
            write.i(WALLS_SIZE, this::wallsSize, this::wallsSize);
            write.i(SHIFT_X, this::shiftX, this::shiftX);
            write.i(SHIFT_Y, this::shiftY, this::shiftY);
        }

        @Override
        public void drawEditorGuides() {
            plotStates.drawEditorGuides();
        }

        @Override
        public void setPlotInfo(int x, int y, FortsPlotState state, Team team) {
            plotStates.setPlotInfo(x, y, state, team);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(DATA, plotStates.save()); }
        }
    }

    @Override public String name() { return NAME; }

    @Override
    Impl create(RulesContext rc) {
        return new Impl(rc);
    }
}
