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


import java.util.LinkedList;

import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;

public class LinearContourLines {
	
	/************************************************************************
	 * Private function which generetes izolines from triangle with defined elevated Step
	 * @param T - triangleDT
	 * @param elevatedStep - int elevated step
	 * @return linked list of extract izolines
	 */
	public static LinkedList countIzoLines(TriangleDT T, int elevatedStep){
		Double minZ = new Double(0);
		Double maxZ = new Double(0);
		PointDT startIZO = null;
		PointDT stopIZO = null;
		LinkedList contours = new LinkedList();
		double elev = -1000;
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
				
				elev = elev+0.0001;
				
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
					startIZO = null;
					stopIZO = null;
					elev = elev + elevatedStep;
					
					}
				return contours;
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
	
	/************************************************************************************
	 * The method for creating shapefile of izolines
	 * @param triangles - DelauanayDataStore triangles
	 * @param elevatedDifference - elevated step between iyolines
	 * @param path - path, where the shapefile will be creating
	 * @param File - name of file
	 * @param EPSG - EPSG code
	 */
	
}
