package mindurka.rules;

import mindustry.game.Rules;
import mindustry.maps.Map;

public class FortsPlotKindSquare extends FortsPlotKind {
    public static final String NAME = "square";
    public static final String PREFIX = FortsPlotKind.PREFIX+"."+NAME;
    public static final String SIZE = PREFIX+".size";
    public static final String WALLS_SIZE = PREFIX+".walls_size";
    public static final String SHIFT_X = PREFIX+".shift_x";
    public static final String SHIFT_Y = PREFIX+".shift_y";

    public class Impl extends FortsPlotKind.Impl implements FortsPlotKindRectangular {
        protected Impl(MRules customRules, Rules rules, Map map) {
            super(customRules, rules, map);

            try (TagRead read = TagRead.of(rules)) {
                size = read.r(SIZE, 6);
                wallsSize = read.r(WALLS_SIZE, 1);
                shiftX = read.r(SHIFT_X, 0);
                shiftY = read.r(SHIFT_Y, 0);
            }
        }

        private int size;
        public int size() { return size; }
        @Override public int plotWidth() { return size; }
        @Override public int plotHeight() { return size; }
        public Impl size(int value) {
            size = value;
            try (TagWrite write = TagWrite.of(rules)) { write.w(SIZE, value); }
            return this;
        }

        private int wallsSize;
        @Override public int wallsSize() { return wallsSize; }
        public Impl wallsSize(int value) {
            wallsSize = value;
            try (TagWrite write = TagWrite.of(rules)) { write.w(WALLS_SIZE, value); }
            return this;
        }

        private int shiftX;
        @Override public int shiftX() { return shiftX; }
        public Impl shiftX(int value) {
            shiftX = value;
            try (TagWrite write = TagWrite.of(rules)) { write.w(SHIFT_X, value); }
            return this;
        }

        private int shiftY;
        @Override public int shiftY() { return shiftY; }
        public Impl shiftY(int value) {
            shiftY = value;
            try (TagWrite write = TagWrite.of(rules)) { write.w(SHIFT_Y, value); }
            return this;
        }

        @Override
        void remove() {
            rules.tags.remove(SIZE);
            rules.tags.remove(WALLS_SIZE);
            rules.tags.remove(SHIFT_X);
            rules.tags.remove(SHIFT_Y);
        }
    }

    @Override public String name() { return NAME; }

    @Override
    Impl create(MRules customRules, Rules rules, Map map) {
        return new Impl(customRules, rules, map);
    }
}
