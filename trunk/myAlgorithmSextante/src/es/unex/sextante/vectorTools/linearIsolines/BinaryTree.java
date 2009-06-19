/****************************************************************************
 *	Sextante - Geospatial analysis tools
 *  www.sextantegis.com
 *  (C) 2009
 *    
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * 	You should have received a copy of the GNU General Public License
 *	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 *    @author      	Josef Bezdek, ZCU Plzen
 *	  @version     	1.0
 *    @since 		JDK1.5 
 */


package es.unex.sextante.vectorTools.linearIsolines;

import java.util.Iterator;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

class DVertex {
	Coordinate pointKey;
	Integer data;
	DVertex left;
	DVertex right;
	
	DVertex (Coordinate pointKey, Integer data){
		this.pointKey = pointKey;
		this.data = data;
	}
}


public class BinaryTree {
	DVertex root = null;
	LinkedList helpList;
	
	/************************************************************************
	 * The method searchs object with pointKey index 
	 * @param pointKey - index of object
	 * @return object with current index 
	 */
	public Object search(Coordinate pointKey){
		return searchV(root, pointKey);
	}
	
	/************************************************************************
	 * The private method for recursive searching 
	 * @param v - vertex of tree
	 * @param pointKey - index of object
	 * @return object with current index 
	 */
	private Object searchV(DVertex v, Coordinate pointKey){
		if (v == null)
			return null;
		if (pointKey.x == v.pointKey.x){
			if (pointKey.y == v.pointKey.y)
				return v;
			else
				return searchV(v.left, pointKey);
		}
		if (pointKey.x < v.pointKey.x)
			return searchV(v.left, pointKey);
		else
			return searchV(v.right, pointKey);
	}
	
	/************************************************************************
	 * The method inserts new object into tree 
	 * @param pointKey - index of object
	 * @param data - integer index of object to main data structure
	 */
	public void insert(Coordinate pointKey, Integer data){
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
	
	/************************************************************************
	 * The method removes object from tree 
	 * @param pointKey - index of object
	 */
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
	
}

