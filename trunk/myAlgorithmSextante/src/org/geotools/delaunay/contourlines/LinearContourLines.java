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



package org.geotools.delaunay.contourlines;


import java.util.Iterator;
import java.util.LinkedList;

import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;

import com.vividsolutions.jts.geom.Coordinate;

public class LinearContourLines {
	static LinkedList contours = new LinkedList();
	
	/************************************************************************
	 * Private function which generetes izolines from triangle with defined elevated Step
	 * @param T - triangleDT
	 * @param elevatedStep - int elevated step
	 * @return linked list of extract izolines
	 */
	private static LinkedList countIzoLines(TriangleDT T, double elevatedStep){
		Double maxIso = Double.POSITIVE_INFINITY;
		Double minIso = Double.NEGATIVE_INFINITY;
		Double minZ = new Double(0);
		Double maxZ = new Double(0);
		PointDT startIZO = null;
		PointDT stopIZO = null;
		
		double elev = Double.NEGATIVE_INFINITY;
				minZ = T.A.z;
				maxZ = T.A.z;
				if (minZ > T.B.z)
					minZ = T.B.z;
				if (minZ > T.C.z)
					minZ = T.C.z;
				if (maxZ < T.B.z)
					maxZ = T.B.z;
				if (maxZ < T.C.z)
					maxZ = T.C.z;
				elev = ((int)(minZ/elevatedStep+1))*elevatedStep;
				
				//TEST OF SINGULAR POINTS
				if (T.A.z/elevatedStep == (int)T.A.z/elevatedStep)
					T.A.z = T.A.z - Double.MIN_VALUE;
				if (T.B.z/elevatedStep == (int)T.B.z/elevatedStep)
					T.B.z = T.B.z - Double.MIN_VALUE;
				if (T.C.z/elevatedStep == (int)T.C.z/elevatedStep)
					T.C.z = T.C.z - Double.MIN_VALUE;
						
						
				while (elev <= maxZ){
					if (((T.A.z<=elev)&(T.B.z>=elev))||((T.A.z>=elev)&(T.B.z<=elev))){
						startIZO = solveLinearInterpolation(T.A, T.B, elev);
					}
					if (((T.A.z<=elev)&(T.C.z>=elev))||((T.A.z>=elev)&(T.C.z<=elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(T.A, T.C, elev);
						else
							stopIZO = solveLinearInterpolation(T.A, T.C, elev);
					}
					if (((T.B.z<=elev)&(T.C.z>=elev))||((T.B.z>=elev)&(T.C.z<=elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(T.B, T.C, elev);
						if (stopIZO == null)
							stopIZO = solveLinearInterpolation(T.B, T.C, elev);
					}
					
					contours.add(new Izolines(startIZO,stopIZO,(int)elev));	//}
					
					if (elev>maxIso)
						maxIso = elev;
					else
						if (elev<minIso)
							minIso = elev;
					
					startIZO = null;
					stopIZO = null;
					elev = elev + elevatedStep;
					
					}
				LinkedList finalIsolines = sortIsolines(contours, minIso, maxIso, elevatedStep);
				return finalIsolines;
			}
	
	
	
	

	/********************************************************************************
	 * Private function which computes  point on line, (Linear interpolation)
	 * @param A - start point of line
	 * @param B - end point of line
	 * @param elev - defined elevation
	 * @return - coordinate of point with definied elevation
	 */
	private static  PointDT solveLinearInterpolation(PointDT A, PointDT B, double elev){
		//double distance = Math.sqrt(Math.pow((A.x-B.x), 2)+Math.pow((A.y-B.y), 2));
		double koef;
		double rate;
		
		if (B.z>A.z){
			rate = (elev - A.z) / (B.z-A.z);
			return  new PointDT((A.x+(B.x-A.x)*rate),(A.y+(B.y-A.y)*rate),elev);
		}
		else{
			rate = (elev - B.z) / (A.z-B.z);
			return  new PointDT((B.x+(A.x-B.x)*rate),(B.y+(A.y-B.y)*rate),elev);
		}
	}
	
	
	
	private static LinkedList sortIsolines(LinkedList isolines, double minIso, double maxIso, double elevatedStep){
		int numberOfIsolines = Double.valueOf((maxIso-minIso)/elevatedStep).intValue();
		BinaryTree[] treeIndex = new BinaryTree[numberOfIsolines];
		Iterator iter = isolines.iterator();
				
		while(iter.hasNext()){
			Izolines izo = (Izolines)iter.next();
			int elevIndex = new Double(izo.elevation/elevatedStep).intValue();
			Coordinate coordA = (Coordinate)izo.A;
			Coordinate coordB = (Coordinate)izo.B;
			LinkedList izoA = null;
			LinkedList izoB = null;
			
			izoA = (LinkedList) treeIndex[elevIndex].search(coordA);
			izoB = (LinkedList) treeIndex[elevIndex].search(coordB);
		
			if (izoA == null && izoB == null){
				LinkedList izoList = new LinkedList();
				izoList.add(coordA);
				izoList.add(coordB);
				treeIndex[elevIndex].insert(coordA, izoList);
				treeIndex[elevIndex].insert(coordB, izoList);
			}		
			else{
				if (izoA == izoB){
					treeIndex[elevIndex].remove(coordA);
				}
				else{
					if (izoA != null){
						treeIndex[elevIndex].remove(coordA);
						if (coordA.compareTo((Coordinate)izoA.getFirst()) == 1)
							izoA.addFirst(coordB);
						else
							izoA.addLast(coordB);
						treeIndex[elevIndex].insert(coordB, izoA);
					}
					else{
						treeIndex[elevIndex].remove(coordB);
						if (coordB.compareTo((Coordinate)izoB.getFirst()) == 1)
							izoA.addFirst(coordA);
						else
							izoA.addLast(coordA);
						treeIndex[elevIndex].insert(coordA, izoB);
					}
				}
			}	
		}
		/// convert to linestring:
		LinkedList finalIsolines = new LinkedList();
		for (int i = 0; i < numberOfIsolines; i++){
			finalIsolines.addAll(treeIndex[i].getIsolines());
		}
		
		return finalIsolines;
		
	}
	
	
	/************************************************************************************
	 * The method for creating shapefile of izolines
	 * @param triangles - DelauanayDataStore triangles
	 * @param elevatedDifference - elevated step between iyolines
	 * @param path - path, where the shapefile will be creating
	 * @param File - name of file
	 * @param EPSG - EPSG code
	 */
	
}
