//------------------------------------------------------------------------------------------------
//
//   SG Craft - 3D Model
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;
import com.google.gson.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.*;

public class SGModel {

    double[] bounds;
    Face[] faces;
    
    public static class Face {
        int texture;
        double[][] vertices;
        int[][] triangles;
    }
    
    public static class Texture {
    
        public ResourceLocation location;
        public double u0, v0, usize, vsize;
        public double red = 1.0, green = 1.0, blue = 1.0;
        public boolean isEmissive = false;
        
        public Texture(ResourceLocation location) {
            this(location, 0.0, 0.0, 1.0, 1.0);
        }
        
        public Texture(Texture base) {
            this(base.location, base.u0, base.v0, base.usize, base.vsize);
        }
        
        public Texture(ResourceLocation location, double u0, double v0, double usize, double vsize) {
            this.location = location;
            this.u0 = u0;
            this.v0 = v0;
            this.usize = usize;
            this.vsize = vsize;
        }
        
        public Texture tinted(double red, double green, double blue) {
            Texture t = new Texture(this);
            t.red = red; t.green = green; t.blue = blue;
            return t;
        }
        
        public Texture emissive(double red, double green, double blue) {
            Texture t = tinted(red, green, blue);
            t.isEmissive = true;
            return t;
        }
        
    }
    
    public static class TiledTexture extends Texture {
        public double tileWidth, tileHeight;
        
        public TiledTexture(ResourceLocation location, int numRows, int numCols) {
            this(new Texture(location), numRows, numCols);
        }
        
        public TiledTexture(Texture base, int numRows, int numCols) {
            super(base);
            tileWidth = usize / numCols;
            tileHeight = vsize / numRows;
        }
        
        public Texture tile(int row, int col) {
            return tile(row, col, 1, 1);
        }
        
        public Texture tile(int row, int col, int width, int height) {
            return new Texture(location,
                u0 + col * tileWidth, v0 + row * tileHeight,
                width * tileWidth, height * tileHeight);
        }
        
    }
    
    static Gson gson = new Gson();
    
    public static SGModel fromResource(ResourceLocation location) {
        try {
            InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            return gson.fromJson(new InputStreamReader(in), SGModel.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public AxisAlignedBB getBounds() {
        return AxisAlignedBB.getBoundingBox(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
    }
    
    public static void setLightingDisabled(boolean off) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        if (off)
            glDisable(GL_TEXTURE_2D);
        else
            glEnable(GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
    
    public void render(Texture[] textures) {
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();
        Texture currentTexture = null;
        boolean lightingOff = false;
        for (Face face : faces) {
            if (face.texture < 0 || face.texture >= textures.length) {
                System.out.printf("SGModel.render: Texture number %s out of range\n", face.texture);
                continue;
            }
            Texture tex = textures[face.texture];
            if (currentTexture != tex) {
                tm.bindTexture(tex.location);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                glColor3d(tex.red, tex.green, tex.blue);
                if (lightingOff != tex.isEmissive) {
                    lightingOff = tex.isEmissive;
                    setLightingDisabled(lightingOff);
                }
                currentTexture = tex;
            }
            glBegin(GL_TRIANGLES);
            for (int[] tri : face.triangles) {
                for (int i : tri) {
                    double[] v = face.vertices[i];
                    glNormal3d(v[3], v[4], v[5]);
                    glTexCoord2d(tex.u0 + tex.usize * v[6], tex.v0 + tex.vsize * v[7]);
                    glVertex3d(v[0], v[1], v[2]);
                }
            }
            glEnd();
        }
        if (lightingOff)
            setLightingDisabled(false);
    }
        
}
