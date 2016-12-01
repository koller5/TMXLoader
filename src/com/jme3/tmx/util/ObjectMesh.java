package com.jme3.tmx.util;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.tmx.math2d.Triangulation;
import com.jme3.util.BufferUtils;

/**
 * Create mesh for the visual part of an ObjectNode.
 * Rectangle, Ellipse, Polygon, Polyline
 * 
 * @author yanmaoyuan
 *
 */
public class ObjectMesh {
	
	public static Mesh makeEllipse(double width, double height) {
		float xc = (float) (width * 0.5);
		float yc = (float) (height * 0.5);
		
		// how many points we need?
		int count = 24;
		List<Vector2f> points = new ArrayList<Vector2f>(count);
		float radian = FastMath.TWO_PI / count;
		float r = 0;
		for(int i=0; i<count; i++) {
			float x = FastMath.sin(r) * xc + xc;
			float y = FastMath.cos(r) * yc + yc;
			points.add(new Vector2f(x,-y));
			
			r += radian;
		}
		
		return makePolyline(points, true);
	}
	
	public static Mesh makeRectangle(double width, double height) {
		List<Vector2f> points = new ArrayList<Vector2f>();
		points.add(new Vector2f(0,0));
		points.add(new Vector2f((float) width,0));
		points.add(new Vector2f((float)width, -(float)height));
		points.add(new Vector2f(0,-(float) height));
		
		return makePolyline(points, true);
	}
	
	public static Mesh makePolygon(List<Vector2f> points) {

		int len = points.size();
		List<Vector2f> vec2 = new ArrayList<Vector2f>(len);
		List<Integer> result = new ArrayList<Integer>();
		for(int i=0; i<len; i++) {
			Vector2f p = points.get(i);
			vec2.add(new Vector2f(p.x, -p.y));
		}
		
		Triangulation.Process(vec2, result);
		
		Vector3f[] vertex = new Vector3f[len];
		Vector3f[] normal = new Vector3f[len];
		Vector2f[] texCoord = new Vector2f[len];
		for(int i=0; i<len; i++) {
			Vector2f p = vec2.get(i);
			vertex[i] = new Vector3f(p.x, p.y, 0f);
			normal[i] = new Vector3f(0, 0, 1);
			texCoord[i] = new Vector2f(0, 0);
		}
		
		len = result.size();
		int[] index = new int[len];
		for(int i=0; i<len; i++) {
			index[i] = result.get(i);
		}
		
		Mesh mesh = new Mesh();
		mesh.setMode(Mode.Triangles);
		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertex));
		mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normal));
		mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		mesh.setBuffer(Type.Index, 3, index);
		mesh.setStatic();
		mesh.updateBound();
		mesh.updateCounts();
		
		return mesh;
	}
	public static Mesh makePolyline(List<Vector2f> points, boolean closePath) {
		int len = points.size();
		if (len < 2) {
			throw new IllegalArgumentException("An polygon must have 2 points at least.");
		}
		
		float[] vertex = new float[len * 3];
		float[] normal = new float[len * 3];
		short[] index = new short[closePath?len+1:len];
		
		Vector2f point = new Vector2f(points.get(0));
		
		for(int i=0; i<len; i++) {
			point.set(points.get(i));
			
			vertex[i*3] = point.x;
			vertex[i*3+1] = -point.y;
			vertex[i*3+2] = 0;
			
			normal[i*3] = 0f;
			normal[i*3+1] = 0f;
			normal[i*3+2] = 1f;
			
			index[i] = (short) i;
		}
		
		if (closePath)
			index[len] = 0;
		
		Mesh mesh = new Mesh();
		mesh.setMode(Mode.LineStrip);
		mesh.setBuffer(Type.Position, 3, vertex);
		mesh.setBuffer(Type.Normal, 3, normal);
		mesh.setBuffer(Type.Index, 2, index);
		mesh.setStatic();
		mesh.updateBound();
		mesh.updateCounts();
		
		return mesh;
	}
}
