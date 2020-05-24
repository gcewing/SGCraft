//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - Rendering target rendering to tessellator
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;
import java.nio.*;
import static java.lang.Math.*;

import net.minecraft.block.*;
// import net.minecraft.block.state.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
// import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
// import net.minecraft.client.renderer.vertex.*;
// import net.minecraft.client.resources.model.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import net.minecraftforge.client.model.*;

import gcewing.sg.BaseModClient.ITexture;
import static gcewing.sg.BaseUtils.*;

public class BaseWorldRenderTarget extends BaseRenderTarget {

    protected IBlockAccess world;
    protected BlockPos blockPos;
    protected Block block;
    protected Tessellator tess;
    protected float cmr = 1, cmg = 1, cmb = 1;
    protected boolean ao;
    protected boolean axisAlignedNormal;
    protected boolean renderingOccurred;
    protected float vr, vg, vb, va; // Colour to be applied to next vertex
    protected int vlm1, vlm2; // Light map values to be applied to next vertex
    
    public BaseWorldRenderTarget(IBlockAccess world, BlockPos pos, Tessellator tess, IIcon overrideIcon) {
        super(pos.getX(), pos.getY(), pos.getZ(), overrideIcon);
        //System.out.printf("BaseWorldRenderTarget(%s)\n", pos);
        this.world = world;
        this.blockPos = pos;
        this.block = world.getBlock(pos.x, pos.y, pos.z);
        this.tess = tess;
        ao = Minecraft.isAmbientOcclusionEnabled() && block.getLightValue() == 0;
        expandTrianglesToQuads = true;
    }
    
    // ---------------------------- IRenderTarget ----------------------------
    
    @Override
    public void setNormal(Vector3 n) {
//      System.out.printf("BaseWorldRenderer.setNormal: %s (%.3f, %.3f, %.3f)\n",
//          vertexCount, n.x, n.y, n.z);
        super.setNormal(n);
        axisAlignedNormal = n.dot(face) >= 0.99;
    }
    
    protected void rawAddVertex(Vector3 p, double u, double v) {
//      System.out.printf("BaseWorldRenderer.rawAddVertex: %s (%.3f, %.3f, %.3f) uv (%.5f, %.5f) at %s\n",
//          vertexCount, p.x, p.y, p.z, u, v, tess.getCurrentOffset());
        lightVertex(p);
        tess.setColorRGBA_F(vr, vg, vb, va);
        tess.setTextureUV(u, v);
        tess.setBrightness((vlm1 << 16) | vlm2);
        tess.addVertex(p.x, p.y, p.z);
        renderingOccurred = true;
//      if (textureOverride)
//          tess.dumpLastVertex();
    }
    
    //-----------------------------------------------------------------------------------------

    protected void lightVertex(Vector3 p) {
        // TODO: Colour multiplier
        if (ao)
            aoLightVertex(p);
        else
            brLightVertex(p);
    }

    protected void aoLightVertex(Vector3 v) {
        //System.out.printf("BaseWorldRenderer.aoLightVertex: %s normal %s\n", v, normal);
        Vector3 n = normal;
        double brSum1 = 0, brSum2 = 0, lvSum = 0, wt = 0;
        // Sample a unit cube offset half a block in the direction of the normal
        double vx = v.x + 0.5 * n.x;
        double vy = v.y + 0.5 * n.y;
        double vz = v.z + 0.5 * n.z;
        // Examine 8 neighbouring blocks
        for (int dx = -1; dx <= 1; dx += 2)
            for (int dy = -1; dy <= 1; dy += 2)
                for (int dz = -1; dz <= 1; dz += 2) {
                    int X = ifloor(vx + 0.5 * dx);
                    int Y = ifloor(vy + 0.5 * dy);
                    int Z = ifloor(vz + 0.5 * dz);
                    //System.out.printf("Examining (%s, %s, %s) delta (%s, %s, %s)\n", X, Y, Z, dx, dy, dz);
                    BlockPos pos = new BlockPos(X, Y, Z);
                    //System.out.printf("wnx = %.3f wny = %.3f wnz = %.3f\n", wnx, wny, wnz);
                    // Calculate overlap of sampled block with sampling cube
                    double wox = (dx < 0) ? (X + 1) - (vx - 0.5) : (vx + 0.5) - X;
                    double woy = (dy < 0) ? (Y + 1) - (vy - 0.5) : (vy + 0.5) - Y;
                    double woz = (dz < 0) ? (Z + 1) - (vz - 0.5) : (vz + 0.5) - Z;
                    //System.out.printf("wox = %.3f woy = %.3f woz = %.3f\n", wox, woy, woz);
                    // Take weighted sample of brightness and light value
                    double w = wox * woy * woz;
                    if (w > 0) {
                        int br;
                        try {
                            br = block.getMixedBrightnessForBlock(world, pos.x, pos.y, pos.z);
                        }
                        catch (RuntimeException e) {
                            System.out.printf("BaseWorldRenderTarget.aoLightVertex: getMixedBrightnessForBlock(%s) with weight %s for block at %s: %s\n",
                                pos, w, blockPos, e);
                            System.out.printf("BaseWorldRenderTarget.aoLightVertex: v = %s n = %s\n",
                                v, n);
                            throw e;
                        }
                        float lv;
                        if (!pos.equals(blockPos))
                            lv = world.getBlock(pos.x, pos.y, pos.z).getAmbientOcclusionLightValue();
                        else
                            lv = 1.0f;
                        //System.out.printf("(%s,%s,%s) br = 0x%08x lv = %.3f w = %.3f\n",
                        //  X, Y, Z, br, lv, w);
                        if (br != 0) {
                            double br1 = ((br >> 16) & 0xff) / 240.0;
                            double br2 = (br & 0xff) / 240.0;
                            //System.out.printf("br1 = %.3f br2 = %.3f\n", br1, br2);
                            brSum1 += w * br1;
                            brSum2 += w * br2;
                            wt += w;
                        }
                        lvSum += w * lv;
                    }
                }
        //System.out.printf("brSum1 = %.3f brSum2 = %.3f lvSum = %.3f\n", brSum1, brSum2, lvSum);
        //System.out.printf("wt = %.3f\n", wt);
        int brv;
        if (wt > 0)
            brv = (iround(brSum1 / wt * 0xf0) << 16) | iround(brSum2 / wt * 0xf0);
        else
            brv = block.getMixedBrightnessForBlock(world, blockPos.x, blockPos.y, blockPos.z);
        float lvv = (float)lvSum;
        //System.out.printf("brv = 0x%08x lvv = %.3f shade = %.3f\n", brv, lvv, shade);
        setLight(shade * lvv, brv);
    }

    protected void brLightVertex(Vector3 p) {
        //System.out.printf("BaseWorldRenderTarget.brLightVertex: %s\n", p);
        Vector3 n = normal;
        BlockPos pos;
        if (axisAlignedNormal)
            pos = new BlockPos(
                (int)floor(p.x + 0.01 * n.x),
                (int)floor(p.y + 0.01 * n.y),
                (int)floor(p.z + 0.01 * n.z));
        else
            pos = blockPos;
        int br = block.getMixedBrightnessForBlock(world, pos.x, pos.y, pos.z);
        setLight(shade, br);
    }

    protected void setLight(float shadow, int br) {
        vr = shadow * cmr * r();
        vg = shadow * cmg * g();
        vb = shadow * cmb * b();
        va = a();
        vlm1 = br >> 16;
        vlm2 = br & 0xffff;
//      System.out.printf("BaseWorldRenderTarget.setLight: (%.3f, %.3f, %.3f, %.3f) (%s, %s)\n",
//          vr, vg, vb, va, vlm1, vlm2);
    }

    public boolean end() {
        super.finish();
        return renderingOccurred;
    }
    
    public void setRenderingOccurred() {
        renderingOccurred = true;
    }

}
