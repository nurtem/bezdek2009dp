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



package es.unex.sextante.vectorTools.linearIsolines;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;

import es.unex.sextante.vectorTools.tinWithFixedLines.TriangleDT;

public class LinearContourLines {
	//static LinkedList contours = new LinkedList();
	static ArrayList finalIsolines  = null;
	static double elevatedStep;
	static double minIso;
	static double maxIso;
	static int numberOfIsolines;
	static BinaryTree[] treeIndex;
	
	/************************************************************************
	 * Private function which generetes izolines from triangle with defined elevated Step
	 * @param T - triangleDT
	 * @param elevatedStep - int elevated step
	 * @return linked list of extract izolines
	 */
	private static void trianglesIsoLines(Coordinate[] triangle){
		Double minZ = new Double(0);
		Double maxZ = new Double(0);
		Coordinate startIZO = null;
		Coordinate stopIZO = null;
		//T.toStringa();
		double elev = Double.NEGATIVE_INFINITY;
	//	System.out.println(triangle[0].z/elevatedStep);
	//	System.out.println((int)(triangle[0].z/elevatedStep));

		//TEST OF SINGULAR POINTS
		if (triangle[0].z/elevatedStep == (int)(triangle[0].z/elevatedStep))
			triangle[0].z = triangle[0].z + elevatedStep*0.01;//Float.MIN_VALUE;
		if (triangle[1].z/elevatedStep == (int)(triangle[1].z/elevatedStep))
			triangle[1].z = triangle[1].z +  elevatedStep*0.01;//Float.MIN_VALUE;
		if (triangle[2].z/elevatedStep == (int)(triangle[2].z/elevatedStep))
			triangle[2].z = triangle[2].z +  elevatedStep*0.01;//Float.MIN_VALUE;

				minZ = triangle[0].z;
				maxZ = triangle[0].z;
				if (minZ > triangle[1].z)
					minZ = triangle[1].z;
				if (minZ > triangle[2].z)
					minZ = triangle[2].z;
				if (maxZ < triangle[1].z)
					maxZ = triangle[1].z;
				if (maxZ < triangle[2].z)
					maxZ = triangle[2].z;
				elev = ((int)(minZ/elevatedStep+1))*elevatedStep;

		//		TriangleDT T = new TriangleDT(triangle[0],triangle[1],triangle[2]);//	T.toStringa();
		//		T.toStringa();		
						
				while (elev <= maxZ){
		//			if (elev == T.A.z || elev == T.B.z || elev == T.C.z){
						
		//			}
					
					if (((triangle[0].z<elev)&(triangle[1].z>elev))||((triangle[0].z>elev)&(triangle[1].z<elev))){
						startIZO = solveLinearInterpolation(triangle[0], triangle[1], elev);
					}
					if (((triangle[0].z<elev)&(triangle[2].z>elev))||((triangle[0].z>elev)&(triangle[2].z<elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(triangle[0], triangle[2], elev);
						else
							stopIZO = solveLinearInterpolation(triangle[0], triangle[2], elev);
					}
					if (((triangle[1].z<elev)&(triangle[2].z>elev))||((triangle[1].z>elev)&(triangle[2].z<elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(triangle[1], triangle[2], elev);
						if (stopIZO == null)
							stopIZO = solveLinearInterpolation(triangle[1], triangle[2], elev);
					}
					
					
					
					/////////////
					if (!startIZO.equals2D(stopIZO)){
						sortIsolines(startIZO,stopIZO, elev);	//
						
					}
					//else{
					//	System.out.println("JE TO TUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
				//	}

						/////////////
					
					
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
	private static  Coordinate solveLinearInterpolation(Coordinate A, Coordinate B, double elev){
		//double distance = Math.sqrt(Math.pow((A.x-B.x), 2)+Math.pow((A.y-B.y), 2));
		double koef;
		double rate;
		//if (B.z == A.z)
			//return B;
		if (B.z>A.z){
			rate = (elev - A.z) / (B.z-A.z);
			return  new Coordinate((A.x+(B.x-A.x)*rate),(A.y+(B.y-A.y)*rate),elev);
		}
		else{
			rate = (elev - B.z) / (A.z-B.z);
			return  new Coordinate((B.x+(A.x-B.x)*rate),(B.y+(A.y-B.y)*rate),elev);
		}
	}
	
	
	public static ArrayList countIsolines(Coordinate[][] triangles, double equiDistance, double minZ, double maxZ){
		elevatedStep = equiDistance;
		minIso = minZ;
		maxIso = maxZ;
		finalIsolines = new ArrayList();
		numberOfIsolines = Double.valueOf((maxZ-minZ)/elevatedStep).intValue()+1;
		//System.out.println(numberOfIsolines+" CIIIIIIIIIIIISLO");
		treeIndex = new BinaryTree[numberOfIsolines];
		for (int i=0; i<numberOfIsolines; i++)
			treeIndex[i] = new BinaryTree();
		
		for (int i = 0; i<triangles.length; i++ ){
			trianglesIsoLines(triangles[i]);
		//	System.out.println("TROJUHELNIK"+ i);
		}
		
		//sortIsolines(contours, minIso, maxIso, elevatedStep);
		return finalIsolines;
	}
	
	private static void sortIsolines(Coordinate coordA, Coordinate coordB,  double elevation){
		//int numberOfIsolines = Double.valueOf((maxIso-minIso)/elevatedStep).intValue()+1;
		DVertex izoA = null;
		DVertex izoB = null;
		int indexA = 0;
		int indexB = 0;

	//	System.out.println("maxIso> "+maxIso+" minIso"+ minIso+ " elev"+ elevatedStep+" number "+numberOfIsolines);
//		BinaryTree[] treeIndex = new BinaryTree[numberOfIsolines];
//		Iterator iter = isolines.iterator();
		///active Binary tree
//		for (int i=0; i<numberOfIsolines; i++)
//			treeIndex[i] = new BinaryTree();
		
//		while(iter.hasNext()){
			//indexA = 0;
			//indexB = 0;
			//Izolines izo = (Izolines)iter.next();
		//	izo.toStringa();
			int elevIndex = new Double((elevation - minIso)/elevatedStep).intValue();
	//		System.out.println(elevIndex);
	//		System.out.println("coordA"+ coordA.toString());
	//		System.out.println("coordB"+ coordB.toString());
		//	Coordinate coordA = 
		//	Coordinate coordB = (Coordinate)izo.B;
			
			izoA = (DVertex)  treeIndex[elevIndex].search(coordA);
			izoB = (DVertex) treeIndex[elevIndex].search(coordB);
			
			if (izoA != null)
				indexA = 1;
			if (izoB != null)
				indexB = 2;
			
			switch (indexA + indexB){
				case 0:{
			//		System.out.println("Delam novou izo:");
					LinkedList izoList = new LinkedList();
					izoList.add(coordA);
					izoList.add(coordB);
					
					treeIndex[elevIndex].insert(coordA, new Integer(finalIsolines.size()));
					treeIndex[elevIndex].insert(coordB, new Integer(finalIsolines.size()));
					finalIsolines.add(finalIsolines.size(), izoList);
			//		System.out.println("oba jsou null  "+ finalIsolines.size());
					break;
				}
				case 1:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
		//			System.out.println("B je null");
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
			//		System.out.println("A je null");
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
			//		System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);

					if ((izoA.data.intValue() == izoB.data.intValue())){
					//	System.out.println("A i B stejny spojuje");
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
			//			System.out.println("A i B ale nejsou stejny");
						LinkedList izoListB = (LinkedList) finalIsolines.get(izoB.data);
						
						
						
						
						
						
						//Coordinate helpCoord = (Coordinate)izoList.getFirst();
					//	System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);
					//	System.out.println("IZOLIST:"+izoList.toString());
						if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
							if (((Coordinate)izoListB.getFirst()).equals2D(coordB)){
								Iterator iterIzoB = izoListB.iterator();
								while (iterIzoB.hasNext()){
									izoList.addFirst(iterIzoB.next());
							//		System.out.println("prvni1  "+((Coordinate)izoList.getFirst()).toString());
								}
							}
							else{
								//izoList.addAll(0, izoListB);
								Iterator iterIzoB = izoListB.descendingIterator();
								while (iterIzoB.hasNext()){
									izoList.addFirst(iterIzoB.next());
						//			System.out.println("prvni2");
								}
							}
						}
						else{
							if (((Coordinate)izoListB.getFirst()).equals2D(coordB)){
								Iterator iterIzoB = izoListB.iterator();
								while (iterIzoB.hasNext()){
									izoList.addLast(iterIzoB.next());
						//			System.out.println("prvni3");
								}
							}
							else{
								Iterator iterIzoB = izoListB.descendingIterator();
								//izoList.addAll(izoListB);
								while (iterIzoB.hasNext()){
									izoList.addLast(iterIzoB.next());
						//			System.out.println("prvni4");
								}
							}
						
						}
						
					//	System.out.println("TOJR TEN UDAJ" + finalIsolines.size());
						finalIsolines.set(izoB.data, null);
					//	System.out.println("TOJR TEN UDAJ" + finalIsolines.size());
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
	
	/************************************************************************************
	 * The method for creating shapefile of izolines
	 * @param triangles - DelauanayDataStore triangles
	 * @param elevatedDifference - elevated step between iyolines
	 * @param path - path, where the shapefile will be creating
	 * @param File - name of file
	 * @param EPSG - EPSG code
	 */
	
}
