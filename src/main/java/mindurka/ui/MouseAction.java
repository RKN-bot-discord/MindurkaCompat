package mindurka.ui;

import arc.Core;
import arc.math.geom.Point2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import mindurka.MVars;

public abstract class MouseAction {
    void move(float mouseX, float mouseY) {}
    void end(float mouseX, float mouseY) {}
    void update() {}
    boolean cancelForgets() { return true; }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Draw extends MouseAction {
        public float startX;
        public float startY;

        public final EditorTool tool;

        public float mouseX;
        public float mouseY;

        public static Draw begin(float mouseX, float mouseY, EditorTool tool) {
            tool.start(LayerToolContext.i);
            return new Draw(mouseX, mouseY, tool, mouseX, mouseY);
        }

        @Override
        void move(float newX, float newY) {
            mouseX = newX;
            mouseY = newY;

            if (tool.draggable == EditorTool.Drag.Touch) {
                Point2 p = MVars.mapView.project(startX, startY);
                int sx = p.x, sy = p.y;
                p = MVars.mapView.project(mouseX, mouseY);
                tool.touched(LayerToolContext.i, sx, sy, p.x, p.y);

                startX = mouseX;
                startY = mouseY;
            }
        }

        @Override
        void end(float mouseX, float mouseY) {
            if (tool.draggable == EditorTool.Drag.Line) {
                Point2 p = MVars.mapView.project(startX, startY);
                int sx = p.x, sy = p.y;
                p = MVars.mapView.project(mouseX, mouseY);
                tool.touchedLine(LayerToolContext.i, sx, sy, p.x, p.y);
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Erase extends MouseAction {
        private float startX;
        private float startY;

        private final EditorTool tool;

        private float mouseX;
        private float mouseY;

        public static Erase begin(float mouseX, float mouseY, EditorTool tool) {
            tool.start(EraseToolContext.i);
            return new Erase(mouseX, mouseY, tool, mouseX, mouseY);
        }

        @Override
        void move(float newX, float newY) {
            mouseX = newX;
            mouseY = newY;

            if (tool.draggable == EditorTool.Drag.Touch) {
                Point2 p = MVars.mapView.project(startX, startY);
                int sx = p.x, sy = p.y;
                p = MVars.mapView.project(mouseX, mouseY);
                tool.touched(EraseToolContext.i, sx, sy, p.x, p.y);

                startX = mouseX;
                startY = mouseY;
            }
        }

        @Override
        void end(float mouseX, float mouseY) {
            if (tool.draggable == EditorTool.Drag.Line) {
                Point2 p = MVars.mapView.project(startX, startY);
                int sx = p.x, sy = p.y;
                p = MVars.mapView.project(mouseX, mouseY);
                tool.touchedLine(EraseToolContext.i, sx, sy, p.x, p.y);
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Drag extends MouseAction {
        // TODO: Handle 2 pointers.
        //       For the second pointer, I think if the first one gets release,
        //       we can just reassign the second one to the first one.
        //       We'll see ig.
        private float mouseX;
        private float mouseY;

        public static Drag begin(float mouseX, float mouseY) {
            return new Drag(mouseX, mouseY);
        }

        @Override
        void move(float newX, float newY) {
            MVars.mapView.moveByScaled(newX - mouseX, newY - mouseY);
            mouseX = newX;
            mouseY = newY;
        }

        @Override
        void update() {
            if (Core.input.ctrl()) MVars.mapView.mouseAction(CtrlZoom.begin(mouseY));
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class CtrlZoom extends MouseAction {
        private float mouseY;

        public static CtrlZoom begin(float mouseY) {
            return new CtrlZoom(mouseY);
        }

        @Override
        void move(float newX, float newY) {
            MVars.mapView.logScaleBy((mouseY - newY) / 80);
            mouseY = newY;
        }

        @Override
        void update() {
            if (!Core.input.ctrl()) MVars.mapView.mouseAction(Drag.begin(MVars.mapView.mousex(), mouseY));
        }
    }

    static class Cancelled extends MouseAction {
        public static Cancelled begin() { return new Cancelled(); }
    }
}
