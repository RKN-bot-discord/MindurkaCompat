package mindurka.ui;

import arc.Core;
import arc.graphics.Gl;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.Shader;
import arc.math.geom.Point2;
import arc.struct.IntSet;
import arc.util.Reflect;
import arc.util.Tmp;
import mindurka.MRules;
import mindurka.MVars;
import mindustry.Vars;
import mindustry.editor.EditorRenderer;
import mindustry.editor.EditorSpriteCache;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Shaders;

public class OEditorRenderer extends EditorRenderer {
    private static final float packPad = Vars.tilesize * 10f;
    private static final int chunkSize = 60;

    public Shader shader;

    @Override
    public void draw(float tx, float ty, float tw, float th){
        if(shader == null){
            shader = new Shader(
                    "attribute vec4 a_position;\n" +
                    "attribute vec4 a_color;\n" +
                    "attribute vec2 a_texCoord0;\n" +
                    "uniform mat4 u_projTrans;\n" +
                    "varying vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "void main(){\n" +
                    "   v_color = a_color;\n" +
                    "   v_color.a = v_color.a * (255.0/254.0);\n" +
                    "   v_texCoords = a_texCoord0;\n" +
                    "   gl_Position = u_projTrans * a_position;\n" +
                    "}\n",

                    "varying lowp vec4 v_color;\n" +
                    "varying vec2 v_texCoords;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "void main(){\n" +
                    "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
                    "}\n"
            );

            Reflect.set(EditorRenderer.class, this, "shader", shader);
        }

        final EditorSpriteCache[][] chunks = Reflect.get(EditorRenderer.class, this, "chunks");
        final IntSet recacheChunks = Reflect.get(EditorRenderer.class, this, "recacheChunks");
        final int width = Reflect.get(EditorRenderer.class, this, "width");
        final int height = Reflect.get(EditorRenderer.class, this, "height");
        final float packWidth = Reflect.get(EditorRenderer.class, this, "packWidth");
        final float packHeight = Reflect.get(EditorRenderer.class, this, "packHeight");

        Draw.flush();

        //don't process terrain updates every frame (helps with lag on low end devices)
        boolean doUpdate = Core.graphics.getFrameId() % 2 == 0;

        if(doUpdate) Vars.renderer.blocks.floor.checkChanges(!Vars.editor.showTerrain);

        boolean prev = Vars.renderer.animateWater;
        Vars.renderer.animateWater = false;

        Tmp.m4.set(Draw.trans());
        Draw.trans().idt();

        Tmp.v3.set(Core.camera.position);
        Core.camera.position.set(Vars.world.width()/2f * Vars.tilesize, Vars.world.height() / 2f * Vars.tilesize);
        Core.camera.width = 999999f;
        Core.camera.height = 999999f;
        Core.camera.mat.set(Draw.proj()).mul(Tmp.m3.setToTranslation(tx, ty).scale(tw / (width * Vars.tilesize), th / (height * Vars.tilesize)).translate(4f, 4f));
        if(Vars.editor.showFloor){
            Vars.renderer.blocks.floor.drawFloor();
        }

        Tmp.m2.set(Draw.proj());

        //scissors are always enabled because this is drawn clipped in UI, make sure they don't interfere with drawing shadow events
        Gl.disable(Gl.scissorTest);

        if(doUpdate) Vars.renderer.blocks.processShadows(!Vars.editor.showBuildings, !Vars.editor.showTerrain);

        Gl.enable(Gl.scissorTest);

        Draw.proj(Core.camera.mat);

        Draw.shader(Shaders.darkness);
        Draw.rect(Draw.wrap(Vars.renderer.blocks.getShadowBuffer().getTexture()), Vars.world.width() * Vars.tilesize/2f - Vars.tilesize/2f, Vars.world.height() * Vars.tilesize/2f - Vars.tilesize/2f, Vars.world.width() * Vars.tilesize, -Vars.world.height() * Vars.tilesize);
        Draw.shader();

        Draw.proj(Tmp.m2);

        Vars.renderer.blocks.floor.beginDraw();
        if(Vars.editor.showTerrain){
            Vars.renderer.blocks.floor.drawLayer(CacheLayer.walls);
        }
        Vars.renderer.animateWater = prev;

        if(chunks == null) return;

        if(doUpdate){
            recacheChunks.each(i -> {
                Reflect.invoke(EditorRenderer.class, this, "recacheChunk", new Object[] { (int) Point2.x(i), (int) Point2.y(i) }, int.class, int.class);
            });
            recacheChunks.clear();
        }

        if(Vars.editor.showBuildings){
            shader.bind();
            shader.setUniformMatrix4("u_projTrans", Tmp.m1.set(Core.camera.mat).translate(-packPad, -packPad).scale(packWidth, packHeight));

            for (int x = 0; x < chunks.length; x++) {
                for (int y = 0; y < chunks[0].length; y++) {
                    EditorSpriteCache mesh = chunks[x][y];

                    if(mesh == null) continue;

                    mesh.render(shader);
                }
            }
        }

        Core.camera.position.set(Tmp.v3);
        Draw.trans(Tmp.m4);
    }
}
