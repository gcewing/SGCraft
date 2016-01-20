//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - 3D Vector
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.util.*;

public class Vector3 {

	static Vector3 zero = new Vector3(0, 0, 0);

	static Vector3 unitX = new Vector3(1, 0, 0);
	static Vector3 unitY = new Vector3(0, 1, 0);
	static Vector3 unitZ = new Vector3(0, 0, 1);
	
	static Vector3 unitNX = new Vector3(-1, 0, 0);
	static Vector3 unitNY = new Vector3(0, -1, 0);
	static Vector3 unitNZ = new Vector3(0, 0, -1);
	
	static Vector3 unitPYNZ = new Vector3(0, 0.707, -0.707);
	static Vector3 unitPXPY = new Vector3(0.707, 0.707, 0);
	static Vector3 unitPYPZ = new Vector3(0, 0.707, 0.707);
	static Vector3 unitNXPY = new Vector3(-0.707, 0.707, 0);

	double x, y, z;
	
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3(Vec3 v) {
		this(v.xCoord, v.yCoord, v.zCoord);
	}
	
	public Vec3 toVec3() {
		return Vec3.createVectorHelper(x, y, z);
	}
	
	public String toString() {
		return String.format("(%.3f,%.3f,%.3f)", x, y, z);
	}
	
	public Vector3 add(double x, double y, double z) {
		return new Vector3(this.x + x, this.y + y, this.z + z);
	}
	
	public Vector3 add(Vector3 v) {
		return add(v.x, v.y, v.z);
	}
	
	public Vector3 sub(double x, double y, double z) {
		return new Vector3(this.x - x, this.y - y, this.z - z);
	}
	
	public Vector3 sub(Vector3 v) {
		return sub(v.x, v.y, v.z);
	}
	
	public static Vector3 sub(double[] u, double[] v) {
		return new Vector3(u[0] - v[0], u[1] - v[1], u[2] - v[2]);
	}
	
	public Vector3 mul(double c) {
		return new Vector3(c * x, c * y, c * z);
	}
	
	public double dot(Vector3 v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	public Vector3 cross(Vector3 v) {
		return new Vector3(
			y * v.z - z * v.y,
			z * v.x - x * v.z,
			x * v.y - y * v.x);
	}
	
	public Vector3 min(Vector3 v) {
		return new Vector3(Math.min(x, v.x), Math.min(y, v.y), Math.min(z, v.z));
	}

	public Vector3 max(Vector3 v) {
		return new Vector3(Math.max(x, v.x), Math.max(y, v.y), Math.max(z, v.z));
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public double distance(Vector3 v) {
		double dx = x - v.x, dy = y - v.y, dz = z - v.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public static Vector3 unit(Vector3 v) {
		return v.mul(1/v.length());
	}
	
	public static Vector3 average(Vector3... va) {
		double x = 0, y = 0, z = 0;
		for (Vector3 v : va) {
			x += v.x; y += v.y; z += v.z;
		}
		int n = va.length;
		return new Vector3(x / n, y / n, z / n);
	}

	public static Vector3 average(double[]... va) {
		double x = 0, y = 0, z = 0;
		for (double[] v : va) {
			x += v[0]; y += v[1]; z += v[2];
		}
		int n = va.length;
		return new Vector3(x / n, y / n, z / n);
	}

	public int floorX() {
		return (int)Math.floor(x);
	}

	public int floorY() {
		return (int)Math.floor(y);
	}

	public int floorZ() {
		return (int)Math.floor(z);
	}

}
