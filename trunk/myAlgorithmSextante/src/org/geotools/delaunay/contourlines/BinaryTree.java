package org.geotools.delaunay.contourlines;

import java.util.Iterator;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

class DVertex {
	Coordinate pointKey;
	Object data;
	DVertex left;
	DVertex right;
	
	DVertex (Coordinate pointKey, Object data){
		this.pointKey = pointKey;
		this.data = data;
	}
}


public class BinaryTree {
	DVertex root = null;
	LinkedList helpList;
	
	public Object search(Coordinate pointKey){
		return searchV(root, pointKey);
	}
	
	private Object searchV(DVertex v, Coordinate pointKey){
		if (v == null)
			return null;
		if (pointKey.x == v.pointKey.x){
			if (pointKey.y == v.pointKey.y)
				return v.data;
			else
				return searchV(v.left, pointKey);
		}
		if (pointKey.x < v.pointKey.x)
			return searchV(v.left, pointKey);
		else
			return searchV(v.right, pointKey);
	}
	
	public void insert(Coordinate pointKey, Object data){
		DVertex v = null;
		DVertex vNext = root;
		
		while (vNext != null){
			v = vNext;
			if (pointKey.x <= vNext.pointKey.x)
				vNext = vNext.left;
			else
				vNext = vNext.right;
		}
		DVertex newVertex = new DVertex(pointKey, data);
		if (v == null)
			root = newVertex;
		else
			if (pointKey.x <= v.pointKey.x)
				v.left = newVertex;
			else
				v.right = newVertex;
	}
	
	public void remove(Coordinate pointKey){
		DVertex v = root;
		while(v!=null && pointKey.x != v.pointKey.x && v.pointKey.y!=pointKey.y){
			if (pointKey.x < v.pointKey.x)
				v = v.left;
			else
				v = v.right;
		}
		
		DVertex y = v;
		if (v.left != null && v.right != null){
			y = v.right;
			while (y.left != null)
				y = y.left;
		}
		
		DVertex x;
		if (y.left != null)
			x = y.left;
		else
			x = y.right;
	}
	
	private void preorder(DVertex v){
		if (v == null)
			return;
		
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coords = new Coordinate[((LinkedList)v.data).size()];
		Iterator iter = ((LinkedList)v.data).iterator();
		int i = 0;
		while (iter.hasNext()){
			coords[i] = (Coordinate) iter.next();
			i++;
		}
		LineString ls = gf.createLineString(coords);
		
		helpList.add(ls);
		
		preorder(v.left);
		preorder(v.right);
		
	}	
	
	public LinkedList getIsolines(){
		helpList = new LinkedList();
		preorder(root);
		return helpList;
	}
}

