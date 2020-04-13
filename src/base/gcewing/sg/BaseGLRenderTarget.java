//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 version B - OpenGL rendering target
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.BaseModClient.ITexture;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

public class BaseGLRenderTarget extends BaseRenderTarget {

    public static final boolean debugGL = false;

    protected boolean usingLightmap;
    protected int glMode;
    protected int emissiveMode;
    protected int texturedMode;

    public BaseGLRenderTarget() {
        super(0, 0, 0, null);
    }

    public void start(boolean usingLightmap) {
        this.usingLightmap = usingLightmap;
        if (debugGL) System.out.print("BaseGLRenderTarget: glPushAttrib()\n");
        glPushAttrib(GL_LIGHTING_BIT | GL_TEXTURE_BIT | GL_TRANSFORM_BIT);
        if (debugGL) System.out.print("BaseGLRenderTarget: glEnable(GL_RESCALE_NORMAL)\n");
        glEnable(GL_RESCALE_NORMAL);
        glShadeModel(GL_SMOOTH);
        glMode = 0;
        emissiveMode = -1;
        texturedMode = -1;
        texture = null;
    }

    @Override
    public void setTexture(ITexture tex) {
        if (texture != tex) {
            super.setTexture(tex);
            ResourceLocation loc = tex.location();
            if (loc != null) {
                setGLMode(0);
                if (debugGL) System.out.printf("BaseGLRenderTarget: bindTexture(%s)\n", loc);
                BaseModClient.bindTexture(loc);
            }
            setTexturedMode(!tex.isSolid());
            setEmissiveMode(tex.isEmissive());
        }
    }

    protected void setEmissiveMode(boolean state) {
        int mode = state ? 1 : 0;
        if (emissiveMode != mode) {
            if (debugGL) System.out.printf("BaseGLRenderTarget: glSetEnabled(GL_LIGHTING, %s)\n", !state);
            glSetEnabled(GL_LIGHTING, !state);
            if (usingLightmap)
                setLightmapEnabled(!state);
            emissiveMode = mode;
        }
    }

    protected void setTexturedMode(boolean state) {
        int mode = state ? 1 : 0;
        if (texturedMode != mode) {
            //System.out.printf("BaseGLRenderTarget.setTexturedMode: %s\n", state);
            setGLMode(0);
            if (debugGL) System.out.printf("BaseGLRenderTarget: glSetEnabled(GL_TEXTURE_2D, %s)\n", state);
            glSetEnabled(GL_TEXTURE_2D, state);
            texturedMode = mode;
        }
    }

    protected void setLightmapEnabled(boolean state) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        glSetEnabled(GL_TEXTURE_2D, state);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    protected void glSetEnabled(int mode, boolean state) {
        if (state)
            glEnable(mode);
        else
            glDisable(mode);
    }

    @Override
    protected void rawAddVertex(Vector3 p, double u, double v) {
        if (debugGL)
            System.out.printf(
                    "BaseGLRenderTarget: rawAddVertex: %s %s normal %s rgba (%.2f,%.2f.%.2f,%.2f) uv (%.4f,%.4f)\n",
                    vertexCount, p, normal, r(), g(), b(), a(), u, v);
        setGLMode(verticesPerFace);
        glColor4f(r(), g(), b(), a());
        glNormal3d(normal.x, normal.y, normal.z);
        glTexCoord2d(u, v);
        glVertex3d(p.x, p.y, p.z);
    }

    protected void setGLMode(int mode) {
        if (glMode != mode) {
            if (glMode != 0) {
                if (debugGL) System.out.print("BaseGLRenderTarget: glEnd()\n");
                glEnd();
            }
            glMode = mode;
            switch (glMode) {
                case 0:
                    break;
                case 3:
                    if (debugGL) System.out.print("BaseGLRenderTarget: glBegin(GL_TRIANGLES)\n");
                    glBegin(GL_TRIANGLES);
                    break;
                case 4:
                    if (debugGL) System.out.print("BaseGLRenderTarget: glBegin(GL_QUADS)\n");
                    glBegin(GL_QUADS);
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid glMode %s", glMode));
            }
        }
    }

    @Override
    public void finish() {
        setGLMode(0);
        setEmissiveMode(false);
        setTexturedMode(true);
        if (debugGL) System.out.print("BaseGLRenderTarget: glPopAttrib()\n");
        glPopAttrib();
        super.finish();
    }

}
