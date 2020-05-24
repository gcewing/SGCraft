//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - OpenGL Utilities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.nio.*;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GLAllocation;

class BaseGLUtils {

    public static void glMultMatrix(Trans3 t) {
        FloatBuffer b = GLAllocation.createDirectFloatBuffer(16);
        Vector3 v = t.offset;
        double[][] m = t.rotation.m;
        
        b.put(0, (float)m[0][0]);
        b.put(1, (float)m[1][0]);
        b.put(2, (float)m[2][0]);
        b.put(3, 0);

        b.put(4, (float)m[0][1]);
        b.put(5, (float)m[1][1]);
        b.put(6, (float)m[2][1]);
        b.put(7, 0);

        b.put(8, (float)m[0][2]);
        b.put(9, (float)m[1][2]);
        b.put(10, (float)m[2][2]);
        b.put(11, 0);

        b.put(12, (float)v.x);
        b.put(13, (float)v.y);
        b.put(14, (float)v.z);
        b.put(15, 1);
        
        GL11.glMultMatrix(b);
    }

}
