package mindurka.ui;

import arc.input.KeyCode;
import arc.math.Mathf;
import arc.struct.LongSeq;
import arc.util.Log;
import arc.util.Nullable;
import mindurka.MRules;
import mindurka.MVars;
import mindurka.Util;
import mindustry.world.Block;

public enum EditorTool {
    // How we assign default tool keys:
    // - System stuff take the bottom row
    // - Basic drawing takes the middle row
    // - Gamemode-specific stuff takes the top row
    // - Preferably keys close to the right so you can actually use them
    // - Fuck mnemonics
    // - Whatever feels right tbh
    // - Also undo is bound to 'z' and redo to 'Z'

    zoom(KeyCode.unset) { // Only useful on mobile. Use middle click.
        @Override
        public void touched(ToolContext ctx, int oldx, int oldy, int newx, int newy) {
            MVars.mapView.mouseAction(MouseAction.Drag.begin(MVars.mapView.mousex(), MVars.mapView.mousey()));
        }
    },
    eraser(KeyCode.unset) { // Only useful on mobile. Use right click.
        @Override
        public void start(ToolContext ctx) {
            mapbits(ctx).zero();
        }

        @Override
        public void touched(ToolContext ctx, int oldx, int oldy, int newx, int newy) {
            if (ctx.isLayer()) ctx = EraseToolContext.i;

            BitMap bits = mapbits(ctx);

            final ToolContext finalCtx = ctx;
            EditorTool.line(oldx, oldy, newx, newy, (x, y) -> {
                EditorTool.circle(x, y, MVars.toolOptions.radius, (x$1, y$1) -> {
                    if (!finalCtx.unsizedBlocks() && !(finalCtx instanceof PreviewToolContext)) {
                        if (EditorTool.squareAny(x$1, y$1, MVars.toolOptions.selectedBlock.size, bits::toggled)) return;
                        EditorTool.square(x$1, y$1, MVars.toolOptions.selectedBlock.size, bits::enable);
                    }
                    finalCtx.setAny(x$1, y$1, MVars.toolOptions.selectedBlock);
                });
            });
        }
    },
    pencil(KeyCode.a) { // This is useful on not just mobile. Use left click.
        @Override
        public void start(ToolContext ctx) {
            mapbits(ctx).zero();
        }

        @Override
        public void touched(ToolContext ctx, int oldx, int oldy, int newx, int newy) {
            BitMap bits = mapbits(ctx);

            EditorTool.line(oldx, oldy, newx, newy, (x, y) -> {
                EditorTool.circle(x, y, MVars.toolOptions.radius, (x$1, y$1) -> {
                    if (ctx instanceof EraseToolContext) {
                        if (bits.toggled(x$1, y$1)) return;
                        bits.enable(x$1, y$1);
                    } else if (!ctx.unsizedBlocks() && !(ctx instanceof PreviewToolContext)) {
                        if (EditorTool.squareAny(x$1, y$1, MVars.toolOptions.selectedBlock.size, bits::toggled)) return;
                        EditorTool.square(x$1, y$1, MVars.toolOptions.selectedBlock.size, bits::enable);
                    }
                    ctx.setAny(x$1, y$1, MVars.toolOptions.selectedBlock);
                });
            });
        }
    },
    line(KeyCode.s) { // I ran out of mouse buttons. Idk, button 4 or smth.
        {
            draggable = Drag.Line;
        }

        @Override
        public void touched(ToolContext ctx, int oldx, int oldy, int newx, int newy) {
            if (!(ctx instanceof PreviewToolContext)) return;
            ctx.setAny(newx, newy, MVars.toolOptions.selectedBlock);
        }

        @Override
        public void touchedLine(ToolContext ctx, int x1, int y1, int x2, int y2) {
            BitMap bits = mapbits(ctx);
            bits.zero();

            EditorTool.line(x1, y1, x2, y2, (x, y) -> {
                EditorTool.circle(x, y, MVars.toolOptions.radius, (x$1, y$1) -> {
                    if (!ctx.unsizedBlocks()) {
                        if (EditorTool.squareAny(x$1, y$1, MVars.toolOptions.selectedBlock.size, bits::toggled)) return;
                        EditorTool.square(x$1, y$1, MVars.toolOptions.selectedBlock.size, bits::enable);
                    }
                    ctx.setAny(x$1, y$1, MVars.toolOptions.selectedBlock);
                });
            });
        }
    },
    fill(KeyCode.d) { // Button 5?
        @Override
        public void start(ToolContext ctx) {
            mapbits(ctx).zero();
        }

        @Override
        public void touched(ToolContext ctx, int x1, int y1, int x2, int y2) {
            if (!ctx.isLayer()) {
                ctx.setAny(x2, y2, MVars.toolOptions.selectedBlock);
                return;
            }
            BitMap bits = mapbits(ctx);

            LongSeq points = new LongSeq();

            EditorTool.line(x1, y1, x2, y2, (x, y) -> {
                if (x < 0 || y < 0 || x >= ctx.width() || y >= ctx.height()) return;
                Block target = MVars.toolOptions.selectedBlock.isOverlay()
                        ? ctx.overlay(x, y)
                        : MVars.toolOptions.selectedBlock.isFloor()
                        ? ctx.floor(x, y)
                        : ctx.block(x, y);
                if (ctx.block(x, y) == MVars.toolOptions.selectedBlock) return;

                points.add(Util.packxy(x2, y2));

                while (!points.isEmpty()) {
                    long point = points.pop();
                    int px = Util.unpackx(point);
                    int py = Util.unpacky(point);
                    if (bits.toggled(px, py)) continue;
                    if (px < 0 || py < 0 || px >= ctx.width() || py >= ctx.height()) continue;
                    if ((MVars.toolOptions.selectedBlock.isOverlay()
                            ? ctx.overlay(px, py)
                            : MVars.toolOptions.selectedBlock.isFloor()
                            ? ctx.floor(px, py)
                            : ctx.block(px, py)) != target) continue;
                    ctx.setAny(px, py, MVars.toolOptions.selectedBlock);
                    bits.enable(px, py);

                    points.add(Util.packxy(px + 1, py));
                    points.add(Util.packxy(px - 1, py));
                    points.add(Util.packxy(px, py + 1));
                    points.add(Util.packxy(px, py - 1));
                }
            });
        }
    },
    // TODO: Make this thing work or smth.
    clipboard(KeyCode.c),

    // Forts tools.
    fortsPlotCarver(KeyCode.q) {
        {
            lockedBehind = MRules.Gamemode.forts;
            blockTool = false;
        }
    },
    fortsPlotToggle(KeyCode.w) {
        {
            lockedBehind = MRules.Gamemode.forts;
            blockTool = false;
        }
    },

    ;

    public enum Drag {
        Touch,
        Line,
        /**
         * Uses some cursed way to determine whether you're touching or
         * dragging and triggers the corresponding function accordingly.
         */
        Both,
    }

    EditorTool(KeyCode key) {
        // Maybe I'll let you rebind it later. Maybe.
        // TODO: Add this shit to settings for extra bloat.
        defaultKey = key;
    }

    private static BitMap mapbits(ToolContext ctx) {
        if (ctx instanceof PreviewToolContext) return MVars.mapbits2();
        return MVars.mapbits();
    }

    public boolean blockTool = true;
    private final KeyCode defaultKey;
    public Drag draggable = Drag.Touch;
    public @Nullable MRules.Gamemode lockedBehind = null;

    public KeyCode key() {
        return defaultKey;
    }

    public void preview(int x, int y) {}
    public void preview(int x1, int y1, int x2, int y2) {
        preview(x2, y2);
    }
    public void start(ToolContext ctx) {}
    public void touched(ToolContext ctx, int oldx, int oldy, int newx, int newy) {}
    public void touchedLine(ToolContext ctx, int x1, int y1, int x2, int y2) { touched(ctx, x2, y2, x2, y2); }

    public interface Consii {
        void get(int x, int y);
    }
    public interface Funcbii {
        boolean get(int x, int y);
    }

    public static void line(int startx, int starty, int endx, int endy, Consii cb) {
        float dist = Mathf.dst(startx, starty, endx, endy);
        float dx = (endx - startx) / dist;
        float dy = (endy - starty) / dist;

        int px = -1;
        int py = -1;

        float x = startx;
        float y = starty;

        cb.get(startx, starty);
        for (float i = 0; i < dist; i++) {
            int nx = Mathf.floor(x);
            int ny = Mathf.floor(y);

            x += dx;
            y += dy;

            if (nx != px || ny != py) {
                px = nx;
                py = ny;
                if (nx != startx || ny != starty) cb.get(nx, ny);
            }
        }
        if (startx != endx || starty != endy) cb.get(endx, endy);
    }

    public static int dst2(int x1, int y1, int x2, int y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static void circle(int x, int y, int r, Consii cb) {
        cb.get(x, y);
        for (int i = 1; i < r; i++) {
            for (int o = -i; o <= i; o++)
                if (dst2(x, y, x + o, y + i) <= r * r)
                    cb.get(x + o, y + i);
            for (int o = -i; o < i; o++)
                if (dst2(x, y, x + i, y + o) <= r * r)
                    cb.get(x + i, y + o);
            for (int o = -i; o < i; o++)
                if (dst2(x, y, x - i, y + o) <= r * r)
                    cb.get(x - i, y + o);
            for (int o = -i + 1; o < i; o++)
                if (dst2(x, y, x - o, y - i) <= r * r)
                    cb.get(x - o, y - i);
        }
    }

    public static void square(int x, int y, int size, Consii cb) {
        // I spent days debugging a `-` let's go
        x += size / 2 - size + 1;
        y += size / 2 - size + 1;

        for (int dx = 0; dx < size; dx++) for (int dy = 0; dy < size; dy++)
            cb.get(x + dx, y + dy);
    }

    public static boolean squareAny(int x, int y, int size, Funcbii cb) {
        x += size / 2 - size + 1;
        y += size / 2 - size + 1;

        for (int dx = 0; dx < size; dx++) for (int dy = 0; dy < size; dy++)
            if (cb.get(x + dx, y + dy)) return true;
        return false;
    }
}
