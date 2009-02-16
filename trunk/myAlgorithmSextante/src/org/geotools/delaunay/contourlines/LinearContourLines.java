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
		//T.toStringa();
		double elev = Double.NEGATIVE_INFINITY;

		//TEST OF SINGULAR POINTS
		if (T.A.z/elevatedStep == (int)T.A.z/elevatedStep)
			T.A.z = T.A.z + 0.0000000000001;//Float.MIN_VALUE;
		if (T.B.z/elevatedStep == (int)T.B.z/elevatedStep)
			T.B.z = T.B.z + 0.0000000000001;//Float.MIN_VALUE;
		if (T.C.z/elevatedStep == (int)T.C.z/elevatedStep)
			T.C.z = T.C.z + 0.0000000000001;//Float.MIN_VALUE;

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
			//	T.toStringa();
						
						
				while (elev <= maxZ){
		//			if (elev == T.A.z || elev == T.B.z || elev == T.C.z){
						
		//			}
					
					if (((T.A.z<elev)&(T.B.z>elev))||((T.A.z>elev)&(T.B.z<elev))){
						startIZO = solveLinearInterpolation(T.A, T.B, elev);
					}
					if (((T.A.z<elev)&(T.C.z>elev))||((T.A.z>elev)&(T.C.z<elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(T.A, T.C, elev);
						else
							stopIZO = solveLinearInterpolation(T.A, T.C, elev);
					}
					if (((T.B.z<elev)&(T.C.z>elev))||((T.B.z>elev)&(T.C.z<elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(T.B, T.C, elev);
						if (stopIZO == null)
							stopIZO = solveLinearInterpolation(T.B, T.C, elev);
					}
					
					contours.add(new Izolines(startIZO,stopIZO,(int)elev));	//}
					
					/////////////
				//	Izolines i = new Izolines(startIZO,stopIZO,(int)elev);
				//	i.toStringa();
					/////////////
					
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
		//if (B.z == A.z)
			//return B;
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
		byte indexA, indexB;

//		System.out.println("maxIso> "+maxIso+" minIso"+ minIso+ " elev"+ elevatedStep+" number "+numberOfIsolines);
		BinaryTree[] treeIndex = new BinaryTree[numberOfIsolines];
		Iterator iter = isolines.iterator();
		///active Binary tree
		for (int i=0; i<numberOfIsolines; i++)
			treeIndex[i] = new BinaryTree();
		
		while(iter.hasNext()){
			indexA = 0;
			indexB = 0;
			Izolines izo = (Izolines)iter.next();
			izo.toStringa();
			int elevIndex = new Double((izo.elevation - minIso)/elevatedStep).intValue();
	//		System.out.println(elevIndex);
			Coordinate coordA = (Coordinate)izo.A;
			Coordinate coordB = (Coordinate)izo.B;
			
			izoA = (DVertex)  treeIndex[elevIndex].search(coordA);
			izoB = (DVertex) treeIndex[elevIndex].search(coordB);
			
			if (izoA != null)
				indexA = 1;
			if (izoB != null)
				indexB = 2;
			
			switch (indexA + indexB){
				case 0:{
					System.out.println("Delam novou izo:");
					LinkedList izoList = new LinkedList();
					izoList.add(coordA);
					izoList.add(coordB);
					
					treeIndex[elevIndex].insert(coordA, new Integer(finalIsolines.size()));
					treeIndex[elevIndex].insert(coordB, new Integer(finalIsolines.size()));
					finalIsolines.add(finalIsolines.size(), izoList);
					System.out.println("oba jsou null  "+ finalIsolines.size());
					break;
				}
				case 1:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
					System.out.println("B je null");
			//		System.out.println(coordA.toString());
			//		System.out.println(((Coordinate)izoList.getFirst()).toString());
			//		System.out.println(((Coordinate)izoList.getFirst()).compareTo(coordA)==1);
			//		System.out.println(((Coordinate)izoList.getFirst()).toString());
					//System.out.println(((Coordinate)izoList.getFirst()).compareTo(coordA));
					treeIndex[elevIndex].remove(coordA);
					//Coordinate helpCoord = (Coordinate)izoList.getFirst();
					if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
						
				//		System.out.println(coordA.toString());
				//		System.out.println((Coordinate)izoList.getFirst());
						izoList.addFirst(coordB);
						treeIndex[elevIndex].insert(coordB, izoA.data);
					}
					else{
						
						izoList.addLast(coordB);
						treeIndex[elevIndex].insert(coordB, izoA.data);
					}
					break;
				}
				case 2:{
					System.out.println("A je null");
					LinkedList izoList = (LinkedList) finalIsolines.get(izoB.data);
					treeIndex[elevIndex].remove(coordB);
					//	Coordinate helpCoord = (Coordinate)izoList.getFirst();
						if (((Coordinate)izoList.getFirst()).equals2D(coordB)){
					//		System.out.println(coordA.toString());
					//		System.out.println((Coordinate)izoList.getFirst());

							izoList.addFirst(coordA);
							treeIndex[elevIndex].insert(coordA, izoB.data);
							
						}	
						else{
							izoList.addLast(coordA);
							treeIndex[elevIndex].insert(coordA, izoB.data);
						}
						break;
				}
				case 3:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
					System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);

					if ((izoA.data.intValue() == izoB.data.intValue())){
						System.out.println("A i B stejny spojuje");
	//					System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);
						treeIndex[elevIndex].remove(coordA);
						treeIndex[elevIndex].remove(coordB);
						//LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
						if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
							izoList.addLast(coordA);
						}
						else{
							izoList.addFirst(coordA);
						}
			//			Iterator iterPOMOCNY = izoList.iterator();
			//			while (iterPOMOCNY.hasNext()){
							//izoList.addFirst(iterPOMOCNY.next());
			//				Coordinate v =(Coordinate) iterPOMOCNY.next();
				//			System.out.println("215SSSSSS: "+ v.toString());
						//}
		//				System.out.println("rovnaji se");

					}
					else{
						System.out.println("A i B ale nejsou stejny");
						LinkedList izoListB = (LinkedList) finalIsolines.get(izoB.data);
						
						
						
						
						
						
						//Coordinate helpCoord = (Coordinate)izoList.getFirst();
					//	System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);
					//	System.out.println("IZOLIST:"+izoList.toString());
						if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
							if (((Coordinate)izoListB.getFirst()).equals2D(coordB)){
								Iterator iterIzoB = izoListB.iterator();
								while (iterIzoB.hasNext()){
									izoList.addFirst(iterIzoB.next());
									System.out.println("prvni1  "+((Coordinate)izoList.getFirst()).toString());
								}
							}
							else{
								//izoList.addAll(0, izoListB);
								Iterator iterIzoB = izoListB.descendingIterator();
								while (iterIzoB.hasNext()){
									izoList.addLast(iterIzoB.next());
									System.out.println("prvni2");
								}
							}
						}
						else{
							if (((Coordinate)izoListB.getFirst()).equals2D(coordB)){
								Iterator iterIzoB = izoListB.iterator();
								while (iterIzoB.hasNext()){
									izoList.addLast(iterIzoB.next());
									System.out.println("prvni3");
								}
							}
							else{
								Iterator iterIzoB = izoListB.descendingIterator();
								//izoList.addAll(izoListB);
								while (iterIzoB.hasNext()){
									izoList.addLast(iterIzoB.next());
									System.out.println("prvni4");
								}
							}
						
						}
						
						System.out.println("TOJR TEN UDAJ" + finalIsolines.size());
						finalIsolines.set(izoB.data, null);
						System.out.println("TOJR TEN UDAJ" + finalIsolines.size());
						((DVertex) treeIndex[elevIndex].search((Coordinate)izoList.getLast())).data = izoA.data;
						((DVertex) treeIndex[elevIndex].search((Coordinate)izoList.getFirst())).data = izoA.data;

					}
					treeIndex[elevIndex].remove(coordA);
					treeIndex[elevIndex].remove(coordB);
	
				}
			}
		/*	System.out.println("VYPISUJE ISOLINIE====================================================");
			for (int k=0; k<finalIsolines.size(); k++){
				Object o = finalIsolines.get(k);
				if (o!=null){
					Iterator isoL = ((LinkedList)o).iterator();
					while(isoL.hasNext()){
						Coordinate c = ((Coordinate)isoL.next());
						System.out.println((c).toString());
					}	
					System.out.println("----------------------------------------");
				}
			}
			System.out.println("===================================================");
			*/// convert to linestring:
		
		//return finalIsolines;
		
	
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
