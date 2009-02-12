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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;

import com.vividsolutions.jts.geom.Coordinate;

public class LinearContourLines {
	static LinkedList contours = new LinkedList();
	static ArrayList finalIsolines  = new ArrayList();
	static Double maxIso = Double.NEGATIVE_INFINITY;
	static Double minIso = Double.POSITIVE_INFINITY;

	
	/************************************************************************
	 * Private function which generetes izolines from triangle with defined elevated Step
	 * @param T - triangleDT
	 * @param elevatedStep - int elevated step
	 * @return linked list of extract izolines
	 */
	private static void trianglesIsoLines(TriangleDT T, double elevatedStep){
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
					if (elev<minIso)
						minIso = elev;
					
					startIZO = null;
					stopIZO = null;
					elev = elev + elevatedStep;
					
					}
				//finalIsolines = sortIsolines(contours, minIso, maxIso, elevatedStep);
				//return finalIsolines;
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
	
	
	public static ArrayList countIsolines(LinkedList triangles, Double elevatedStep){
		Iterator iter = triangles.iterator();
		while (iter.hasNext()){
			trianglesIsoLines((TriangleDT) iter.next(),elevatedStep);
		}
		sortIsolines(contours, minIso, maxIso, elevatedStep);
		return finalIsolines;
	}
	
	private static void sortIsolines(LinkedList isolines, double minIso, double maxIso, double elevatedStep){
		int numberOfIsolines = Double.valueOf((maxIso-minIso)/elevatedStep).intValue()+1;
		DVertex izoA = null;
		DVertex izoB = null;

		System.out.println("maxIso> "+maxIso+" minIso"+ minIso+ " elev"+ elevatedStep+" number "+numberOfIsolines);
		BinaryTree[] treeIndex = new BinaryTree[numberOfIsolines];
		Iterator iter = isolines.iterator();
		///active Binary tree
		for (int i=0; i<numberOfIsolines; i++)
			treeIndex[i] = new BinaryTree();
		
		while(iter.hasNext()){
			Izolines izo = (Izolines)iter.next();
			izo.toStringa();
			int elevIndex = new Double((izo.elevation - minIso)/elevatedStep).intValue();
			System.out.println(elevIndex);
			Coordinate coordA = (Coordinate)izo.A;
			Coordinate coordB = (Coordinate)izo.B;
			
			izoA = (DVertex)  treeIndex[elevIndex].search(coordA);
			izoB = (DVertex) treeIndex[elevIndex].search(coordB);
			
			if (izoA == null && izoB == null){
				System.out.println("Delam novou izo:");
				LinkedList izoList = new LinkedList();
				izoList.add(coordA);
				izoList.add(coordB);
				
				treeIndex[elevIndex].insert(coordA, new Integer(finalIsolines.size()),true);
				treeIndex[elevIndex].insert(coordB, new Integer(finalIsolines.size()),false);
				finalIsolines.add(finalIsolines.size(), izoList);
			}		
			else{
				if (izoA == izoB){
					treeIndex[elevIndex].remove(coordA);
					treeIndex[elevIndex].remove(coordB);
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
					if (((Coordinate)izoList.getLast()).compareTo(coordA)==1){
						izoList.addFirst(coordA);
					}
					else
						izoList.addLast(coordA);
					System.out.println("rovnaji se");
				}
				else{
					if (izoA != null){
						LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
						
						if (izoB == null){
							treeIndex[elevIndex].remove(coordA);
							//Coordinate helpCoord = (Coordinate)izoList.getFirst();
							if (izoA.first){
								
								System.out.println(coordA.toString());
								System.out.println((Coordinate)izoList.getFirst());
								izoList.addFirst(coordB);
								treeIndex[elevIndex].insert(coordB, izoA.data, true);
							}
							else{
								izoList.addLast(coordB);
								treeIndex[elevIndex].insert(coordB, izoA.data, false);
							}
						}
						else{
							LinkedList izoListB = (LinkedList) finalIsolines.get(izoB.data);
							Iterator iterIzoB = izoListB.iterator();
							//Coordinate helpCoord = (Coordinate)izoList.getFirst();
							if (izoA.first){
								while (iterIzoB.hasNext()){
									izoList.addFirst(iterIzoB.next());
								}
							}
							else{
								while (iterIzoB.hasNext()){
								izoList.addLast(iterIzoB.next());
								}
								
							}
						}
					}
					else{
						LinkedList izoList = (LinkedList) finalIsolines.get(izoB.data);
						
						if (izoA == null){
							treeIndex[elevIndex].remove(coordB);
						//	Coordinate helpCoord = (Coordinate)izoList.getFirst();
							if (izoB.first){
								System.out.println(coordA.toString());
								System.out.println((Coordinate)izoList.getFirst());
	
								izoList.addFirst(coordA);
								treeIndex[elevIndex].insert(coordA, izoB.data, true);
								
							}	
							else{
								izoList.addLast(coordA);
								treeIndex[elevIndex].insert(coordA, izoB.data, false);
							}
						}	
						else{
							LinkedList izoListA = (LinkedList) finalIsolines.get(izoB.data);
							Iterator iterIzoA = izoListA.iterator();
							//Coordinate helpCoord = (Coordinate)izoList.getFirst();
							if (izoB.first){
								while (iterIzoA.hasNext()){
									izoList.addFirst(iterIzoA.next());
								}
							}
							else{
								while (iterIzoA.hasNext()){
								izoList.addLast(iterIzoA.next());
								}
								
							}
						}	
					}
				}
			}	
		}
		/// convert to linestring:
		
		//return finalIsolines;
		
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
