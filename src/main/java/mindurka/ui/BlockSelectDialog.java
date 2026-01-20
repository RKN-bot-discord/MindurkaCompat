package mindurka.ui;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Texture;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;

import java.lang.ref.WeakReference;

public class BlockSelectDialog extends BaseDialog {
    Boolf<Block> filter;
    Cons<Block> set;
    WeakReference<Table> filteredContent;

    public BlockSelectDialog(String title) {
        super(title);

        shown(this::build);
        hidden(cont::clear);
    }

    private void build() {
        cont.clear();

        cont.table(t -> {
            Table content = new Table();
            content.setFillParent(true);
            filteredContent = new WeakReference<>(content);
            ScrollPane scroll = new ScrollPane(filteredContent.get());
            t.add(scroll).fillX();
            rebuildItems();
        });
    }

    private void rebuildItems() {
        Table table = filteredContent.get();
        if (table == null) return;

        table.clear();

        final int cols;
        if (Core.graphics.isPortrait()) {
            cols = Math.max(4, (int)((Core.graphics.getWidth() / Scl.scl() - 100f) / 50f));
        } else {
            cols = Math.max(4, (int)((Core.graphics.getWidth() / Scl.scl() - 300f) / 50f / 2));
        }

        int i = 0;
        for (Block block : Vars.content.blocks()) {
            if (block.uiIcon == null) continue;
            if (!filter.get(block)) continue;

            ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNonei);
            button.getStyle().imageUp = new TextureRegionDrawable(block.uiIcon);
            button.resizeImage(32f);
            button.setSize(32f, 32f);
            button.clicked(() -> {
                set.get(block);
                hide();
            });
            table.add(button);

            if (++i >= cols) {
                i = 0;
                table.row();
            }
        }
    }

    public void show(Boolf<Block> filter, Cons<Block> set) {
        this.filter = filter;
        this.set = set;
        show();
    }
}
