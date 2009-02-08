package org.geotools.delaunay.fixedlines;

/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 *    @author      Josef Bezdek
 *	  @version     %I%, %G%
 *    @since JDK1.3 
 */


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.delaunay.DelaunayDataStoreRAM;
import org.geotools.delaunay.IncrementalDT;
import org.geotools.delaunay.LineDT;
import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.RTree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.index.strtree.STRtree;

public class TINWithFixedLines {
	static RTree trianglesIdx;
	static LinkedList fixedLines;	// list of hard lines
	static LineDT line;
	static ArrayList triangles;
	/***100
	   * The private method for searching a triangle which are intersect by new hard line
	   *
	   * @param PointDT - coordinate of new hard line
	   */			
	
	private static LinkedList getTrianglesIntersectLine(){
		Data data;
		int index = 0;
		LinkedList trianglesToChange = new LinkedList();
		List trianglesOverEnvelopeIdx = null;
		boolean containA = false;
		boolean containB = false;
		
		Coordinate[] newPoints={line.A,line.B}; 	// creating new geometry of line
		CoordinateArraySequence newPointP=new CoordinateArraySequence(newPoints);
		LineString newL=new LineString(newPointP,new GeometryFactory());
		
		try{
			trianglesOverEnvelopeIdx = (List) trianglesIdx.search(newL.getEnvelopeInternal());
		//	LinkedList trianglesOverEnvelope = new LinkedList();
			System.out.println("JEEEEE TAAAM"+trianglesOverEnvelopeIdx.size());

		Iterator iter = trianglesOverEnvelopeIdx.iterator();
		while (iter.hasNext()){
			index = (Integer)((Data) iter.next()).getValue(0);
			
			System.out.println(index);
			if (triangles.get(index)!=null){
				TriangleDT T = (TriangleDT) triangles.get(index);
				//T.toStringa();
				if (!containA&&(T.containsPointAsVertex(line.A))){
					containA = true;
					line.A.z = setZ(line.A,T);
				}
				if (!containA&&(T.containsPointAsVertex(line.B))){
					containB = true;
					line.B.z = setZ(line.B,T);

				}
				if (T.containsLine(newL)){	// test if line intersect triangle
					triangles.set(index, null);
					trianglesToChange.add(T);
				}
				else{	// test if line or vertex is containg in triangle
					if (lineContainsPoint(newL, T.A)&&!T.A.compare(line.A)&&!T.A.compare(line.B)){
						triangles.set(index, null);
						trianglesToChange.add(T);
					}
					else{
						if (lineContainsPoint(newL, T.B)&&!T.B.compare(line.A)&&!T.B.compare(line.B)){
							triangles.set(index, null);
							trianglesToChange.add(T);
						}
						else{
							if (lineContainsPoint(newL, T.C)&&!T.C.compare(line.A)&&!T.C.compare(line.B)){
								triangles.set(index, null);;
								trianglesToChange.add(T);
							}
						}	
					}	
				}	
		
			}
		}	
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if ((containA == false)||(containB == false)){
			
			System.out.println("CRACI NULL");
			System.out.println(trianglesToChange.size());
			System.out.println(containA);
			System.out.println(containB);
			
			return null;
			
		}
		return trianglesToChange;
		
		
	}
	
	private static double setZ(PointDT A, TriangleDT T){
		if (T.A.compare(A))
			return T.A.z;
		if (T.B.compare(A))
			return T.B.z;
		return T.C.z;
		
		
	}
	
	/***
	   * The private method, which finds out if the line contains the point P
	   *
	   * @param PointDT - coordinates of the point P
	   *
	   * @return boolean - true : when the line contains the point P
	   *                   false: when the line doesn't contain the point P
	   *    
       */		
	
	private static boolean lineContainsPoint(LineString line, PointDT P){
		Coordinate[] newPoint={P}; 
		CoordinateArraySequence newPointP=new CoordinateArraySequence(newPoint);
		Point newP=new Point(newPointP,new GeometryFactory());

		return line.covers(newP);
	}
	
	/***
	   * The private method for testing, if the LinkedList contains point P
	   *
	   * @param LinkedList - List of existing points
	   * @param PointDT - coordinates of the point P
	   *
	   * @return boolean - true : when the list contains the point P
	   *                   false: when the list doesn't contain the point P
	   *    
       */		
	
	private static boolean listContainsPoint(LinkedList points, PointDT P){
		Iterator iter = points.iterator();
		while (iter.hasNext()){		
			if (((PointDT)iter.next()).compare(P))
				return true;
		}
		return false;
	}
	
	/***
	   * The private method for testing, if the LinkedList contains point P
	   *
	   * @param LinkedList - List of existing TIN triangles
	   * @param PointDT A- coordinates of start point of line
	   * @param PointDT B- coordinates of end point of line
	   *
	   * @return LinkedList - return List of point, which generate new TIN around hard line
	   * 						without start and end point of hard line
	   *    
	   */		
	
	private static LinkedList getPoints(LinkedList trianglesT, PointDT A, PointDT B){
		Iterator iter = trianglesT.iterator();
		LinkedList points = new LinkedList();
		TriangleDT T = (TriangleDT) iter.next();
		points.add(T.A);
		points.add(T.B);
		points.add(T.C);
		while (iter.hasNext()){  //test duplicity bodu v seznamu
			T = (TriangleDT) iter.next();
			if (!listContainsPoint(points,T.A))
				points.add(T.A);
			if (!listContainsPoint(points,T.B))
				points.add(T.B);
			if (!listContainsPoint(points,T.C))
				points.add(T.C);
		}
		//vymazani bodu A a B
		iter = points.iterator();
		PointDT P;
		while (iter.hasNext()){	//vymazani yacatku a konce linie ze seznamu bodu
			P = (PointDT)iter.next();
			if (P.compare(A)||P.compare(B))
					iter.remove();
		}
		return points;
	}

	/***
	   * The private method for testing, if the old triangles contains new triangle
	   *
	   * @param TriangleDT - triangle for test
	   * @param LinkedList - List of triangles of old triangulation,which will be deleted
	   *
	   * @return boolean - true : when the triangles contains the new triangle
	   *                   false: when the triangles don't contains the new triangle
	   *                       
       */		
	
	private static boolean testIsInside(TriangleDT T, LinkedList trians){
		Iterator iter = trians.iterator();
		while (iter.hasNext()){			//testovani zda teziste noveho trojuhelnika je uvnitr zrusenych trojuhelniku
			if (((TriangleDT)iter.next()).contains(new PointDT(T.key[0],T.key[1],0))){
				return true;
			}
		}
		return false;
	}
	
	/***
	   * The private method for testing and adding in old triangulation triangles
	   *
	   * @param LinkedList left - List of new triangles, which are on the left side of the line
	   * @param LinkedList right - List of new triangles, which are on the right side of the line
	   * @param LinkedList trianglesToChange - List of old triangles, which will be deleted from triangulation
	   *	
	   */		
	
	private static void testAndAddTrianglesToTIN(LinkedList left, LinkedList right, LinkedList trianglesToChange, LineDT line){
		TriangleDT T;
		Data data;
		DataDefinition dd = new DataDefinition("US-ASCII"); 
		dd.addField(Integer.class);
		
		int i = triangles.size();
		if (left!=null){						//test leve triangulace
			Iterator it = left.iterator();
			
			while (it.hasNext()){
				T = (TriangleDT)it.next();
				if (line.isHardBreakLine&&T.contains(line.A)&&T.contains(line.B))
					T.haveBreakLine = true;
				if (testIsInside(T, trianglesToChange)){
					try{
						data = new Data(dd);
						data.addValue(i);
						trianglesIdx.insert(T.getEnvelope(), data);
					}
					catch(Exception e){
						e.printStackTrace();
					}
					triangles.add(i, T);				//vlozeni do celkove triangulace
					i++;
				}
			}
		}
		if (right!=null){						//test prave triagulace
			Iterator it = right.iterator();
			while (it.hasNext()){
				T = (TriangleDT)it.next();
				if (line.isHardBreakLine&&T.contains(line.A)&&T.contains(line.B))
					T.haveBreakLine = true;
				if (testIsInside(T, trianglesToChange)){
					try{
						data = new Data(dd);
						data.addValue(i);
						trianglesIdx.insert(T.getEnvelope(), data);
					}
					catch(Exception e){
						e.printStackTrace();
					}
					triangles.add(i, T);				//vlozeni do celkove triangulace
					i++;
				}
				
			}
		}

	}
	
	/***
	   * The static method for creating new triangles around the fixed lines in existing triangulation
	   *
	   * @param trianglesDT - DelaunayDataStore, triangles, which generate TIN
	   * @param fixedLinesDT - List of fixed lines, which will be triangulated
	   * 
	   * @return LinkedList - list of new triangles, which generate TIN	
	   */		
	
	
	public static ArrayList countTIN(ArrayList trianglesDT, RTree trianglesDTIdx, LinkedList fixedLinesDT){
					// list pevnych hran
		triangles = trianglesDT;
		trianglesIdx = trianglesDTIdx;
		fixedLines = fixedLinesDT;
		
		Iterator iter = fixedLines.iterator();
							// pomocna linie
		LinkedList trianglesToChange;  //nalezene trojuhelniky pres ktere prochazi linie
		LinkedList points; 				// body kterych se budou nove triangulovat
		LinkedList leftPoints;				// body nad primkou
		LinkedList rightPoints;			// body pod primkou
		IncrementalDT lTIN;				// triangulace na primkou
		IncrementalDT rTIN;				// triangulace pod primkou
		LinkedList leftTIN;				// TIN nad primkou 
		LinkedList rightTIN;			// TIN pod primkou
		PointDT P;						// pomocny bod
		double alfa;					// uhel, ktery svira pevna hrana s osou x
		double yTrans,xTrans;			// transfomovane souradnice (shodnostni transformace
										// uhel pooteceni alfa, posunuti 0 v x-ove i v y-ove ose
		while (iter.hasNext()){			//cyklus pro pevne hrany
			leftTIN = null;
			rightTIN = null;
			line = (LineDT) iter.next();
			leftPoints = new LinkedList();
			rightPoints = new LinkedList();
			
			
			trianglesToChange = getTrianglesIntersectLine();	// getting triangles which intersect fixed line
			
			
			if ((trianglesToChange!=null)&&trianglesToChange.size() != 0){
				points = getPoints(trianglesToChange, line.A, line.B);	// getting points from intersect triangles
			
																	// counting angle of turning, identity transformation
				if ((line.B.x - line.A.x)!= 0)
					alfa = -1*(Math.atan((line.B.y-line.A.y)/(line.B.x - line.A.x)));
				else
					alfa = Math.PI/2;
			
				double yTransNullPoints = Math.cos(alfa)*line.A.y + Math.sin(alfa)*line.A.x;	// getting value y which sorting left and righ triangulation
				Iterator it = points.iterator();
			
				while (it.hasNext()){			
					P = (PointDT)it.next();
					yTrans = Math.cos(alfa)*P.y + Math.sin(alfa)*P.x;		// identity transformation
					//xTrans = Math.cos(alfa)*P.x - Math.sin(alfa)*P.y;
					if (yTrans>=yTransNullPoints-0.002){
						leftPoints.add(P);
					}
					if (yTrans<=yTransNullPoints+0.002){
						rightPoints.add(P);
					}
				}
				
				DelaunayDataStoreRAM lTriangles = new DelaunayDataStoreRAM();
				lTIN = new IncrementalDT(lTriangles);    	// counting left triangulation
				if (!leftPoints.isEmpty()){
					it = leftPoints.iterator();
					while (it.hasNext()){
						P = (PointDT)it.next();
						lTIN.insertPoint(P);
					}
			
					lTIN.insertPoint(line.A);
					lTIN.insertPoint(line.B);
					leftTIN = (LinkedList)lTriangles.getCollection();
				}
				DelaunayDataStoreRAM rTriangles = new DelaunayDataStoreRAM();
				rTIN = new IncrementalDT(rTriangles);		// counting right triangulation
				if (!rightPoints.isEmpty()){
				
					it = rightPoints.iterator();
					while (it.hasNext()){
						P =(PointDT)it.next();
						rTIN.insertPoint(P);
					}
				
					rTIN.insertPoint(line.A);
					rTIN.insertPoint(line.B);
					rightTIN = (LinkedList)rTriangles.getCollection();
				}
			
					//test newlz formed triangles if inside shape
				testAndAddTrianglesToTIN(leftTIN, rightTIN, trianglesToChange, line);
			}
		}
		return triangles;
	}
}
