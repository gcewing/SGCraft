//------------------------------------------------------------------------------------------------
//
//   SG Craft - DHD tile entity renderer
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import net.minecraft.util.*;

import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.tileentity.*;

class DHDTERenderer extends BaseTileEntityRenderer {

    SGModel model;
    SGModel.Texture mainTexture;
    SGModel.Texture[] buttonTextures;
    SGModel.Texture[] textures;
    
    final static int buttonTextureIndex = 3;
    
    public DHDTERenderer() {
        SGCraft mod = SGCraft.mod;
        ResourceLocation ttLoc = mod.textureLocation("tileentity/dhd_top.png");
        ResourceLocation stLoc = mod.textureLocation("tileentity/dhd_side.png");
        ResourceLocation dtLoc = mod.textureLocation("tileentity/dhd_detail.png");
        SGModel.TiledTexture detail = new SGModel.TiledTexture(dtLoc, 2, 2);
        textures = new SGModel.Texture[] {
            new SGModel.Texture(ttLoc),
            new SGModel.Texture(stLoc),
            detail.tile(1, 1),
            null, // button texture inserted here
        };
        {
            SGModel.Texture t = textures[2];
            System.out.printf("DHDTERenderer: Panel texture: origin %s,%s size %s,%s\n",
                t.u0, t.v0, t.usize, t.vsize);
        }
        SGModel.Texture button = detail.tile(0, 0);
        buttonTextures = new SGModel.Texture[] {
            button.tinted(0.5, 0.5,  0.5),
            button.tinted(0.5, 0.25, 0.0),
            button.emissive(1.0, 0.5, 0.0),
        };
        model = SGModel.fromResource(mod.resourceLocation("models/dhd.json"));
        DHDTE.bounds = model.getBounds();
    }
    
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float t) {
        DHDTE dte = (DHDTE)te;
        SGBaseTE gte = dte.getLinkedStargateTE();
        int i;
        if (gte == null)
            i = 0;
        else if (gte.isActive())
            i = 2;
        else
            i = 1;
        textures[buttonTextureIndex] = buttonTextures[i];
        //glPushAttrib(GL_LIGHTING_BIT | GL_TEXTURE_BIT);
        glPushMatrix();
        glEnable(GL_RESCALE_NORMAL);
        glColor3d(1.0, 1.0, 1.0);
        glTranslated(x + 0.5, y, z + 0.5);
        glRotatef(90 * dte.getRotation(), 0, 1, 0);
        model.render(textures);
        glDisable(GL_RESCALE_NORMAL);
        glPopMatrix();
        //glPopAttrib();
    }

}
