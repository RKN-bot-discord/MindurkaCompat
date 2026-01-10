package mindurka;

import mindurka.rules.FortsPlotKind;
import mindurka.rules.Gamemode;
import mindurka.ui.OCustomRulesDialog;
import mindurka.ui.OEditorDialog;
import mindurka.ui.OMapEditor;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.world.blocks.payloads.*;

public class Injects {
    public static void load() {
        Vars.content.blocks().each(x -> x instanceof PayloadDeconstructor, x -> {
            x.buildType = () -> ((PayloadDeconstructor) x).new PayloadDeconstructorBuild() {
                @Override
                public boolean acceptPayload(Building source, Payload payload) {
                    return super.acceptPayload(source, payload) &&
                            (!(payload instanceof BuildPayload) ||
                                    ((BuildPayload) payload).build.block != Blocks.carbideWallLarge ||
                                    !Vars.state.rules.tags.get("mindurkaGamemode", "").equals("forts"));
                }
            };
        });

        Gamemode.init();
        FortsPlotKind.init();

        MVars.oldMapView = Vars.ui.editor.getView();

        OMapEditor.inject();
        MVars.mapEditor = (OMapEditor) Vars.editor;
        Vars.ui.editor = MVars.editorDialog = new OEditorDialog(MVars.mapEditor, Vars.ui.editor);
        OCustomRulesDialog.inject();
        OMobileInput.inject();
    }
}
