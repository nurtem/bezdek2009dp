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
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;

public class LinearContourLines {
	
	LinearContourLines (double equiDistance, double clusterTol){
		elevatedStep = equiDistance;
		int coeficient = 1;
		while (clusterTol<1){
			coeficient *= 10;
			clusterTol *= coeficient;
		}
		this.clusterTol = coeficient;
	//	System.out.println("CLUSTER"+this.clusterTol);
		
		finalIsolines = new ArrayList();
		//numberOfIsolines = Double.valueOf((maxZ-minZ)/elevatedStep).intValue()+1     +30;
		treeIndex = new TreeMap<Integer, BinaryTree>();
	}
	
	
	//static LinkedList contours = new LinkedList();
	ArrayList finalIsolines  = null;
	double elevatedStep;
	double clusterTol;
	double minIso;
	double maxIso;
	int numberOfIsolines;
	TreeMap treeIndex;
	
	/************************************************************************
	 * Private function which generetes izolines from triangle with defined elevated Step
	 * @param T - triangleDT
	 * @param elevatedStep - int elevated step
	 * @return linked list of extract izolines
	 */
	private void trianglesIsoLines(Coordinate[] triangle){
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
				if (minZ >= 0)
					elev = ((int)(minZ/elevatedStep+1))*elevatedStep;
				else
					elev = ((int)(minZ/elevatedStep))*elevatedStep;
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
					
					
					
					
					startIZO.x = Math.round(startIZO.x*clusterTol)/clusterTol;
					startIZO.y = Math.round(startIZO.y*clusterTol)/clusterTol;
					stopIZO.x = Math.round(stopIZO.x*clusterTol)/clusterTol;
					stopIZO.y = Math.round(stopIZO.y*clusterTol)/clusterTol;
					
					if (!startIZO.equals2D(stopIZO))
						sortIsolines(startIZO,stopIZO, elev);	//
						
					

						/////////////
					
					
					startIZO = null;
					stopIZO = null;
					elev = elev + elevatedStep;
					
					}
				
			}
	
	
	
	

	/********************************************************************************
	 * Private function which computes  point on line, (Linear interpolation)
	 * @param A - start point of line
	 * @param B - end point of line
	 * @param elev - defined elevation
	 * @return - coordinate of point with definied elevation
	 */
	private  Coordinate solveLinearInterpolation(Coordinate A, Coordinate B, double elev){
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
	
	
	public void countIsolines(Coordinate[][] triangles){
		for (int i = 0; i<triangles.length; i++ ){
			trianglesIsoLines(triangles[i]);
			triangles[i] = null;
			//	System.out.println("TROJUHELNIK"+ i);
		}
	}
	
	public ArrayList getIsolines(){
		return finalIsolines;
	}

	
	private void sortIsolines(Coordinate coordA, Coordinate coordB,  double elevation){
		//int numberOfIsolines = Double.valueOf((maxIso-minIso)/elevatedStep).intValue()+1;
		DVertex izoA = null;
		DVertex izoB = null;
		int indexA = 0;
		int indexB = 0;
		BinaryTree tree = null;
		
		int elevIndex = new Double((elevation - minIso)/elevatedStep).intValue();
		
		if (treeIndex.containsKey(elevIndex)){
			tree  = (BinaryTree)treeIndex.get(elevIndex);
		}
		else{
			tree = new BinaryTree();
			treeIndex.put(elevIndex, tree);
		}
		
			izoA = (DVertex)  tree.search(coordA);
			izoB = (DVertex) tree.search(coordB);
			
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
					
					tree.insert(coordA, new Integer(finalIsolines.size()));
					tree.insert(coordB, new Integer(finalIsolines.size()));
					finalIsolines.add(finalIsolines.size(), izoList);
			//		System.out.println("oba jsou null  "+ finalIsolines.size());
					break;
				}
				case 1:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
					if (izoList == null)
						break;
					//			System.out.println("B je null");
			//		System.out.println(coordA.toString());
			//		System.out.println(((Coordinate)izoList.getFirst()).toString());
			//		System.out.println(((Coordinate)izoList.getFirst()).compareTo(coordA)==1);
			//		System.out.println(((Coordinate)izoList.getFirst()).toString());
					//System.out.println(((Coordinate)izoList.getFirst()).compareTo(coordA));
					tree.remove(coordA);
					//Coordinate helpCoord = (Coordinate)izoList.getFirst();
					if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
						
				//		System.out.println(coordA.toString());
				//		System.out.println((Coordinate)izoList.getFirst());
						izoList.addFirst(coordB);
						tree.insert(coordB, izoA.data);
					}
					else{
						
						izoList.addLast(coordB);
						tree.insert(coordB, izoA.data);
					}
					break;
				}
				case 2:{
			//		System.out.println("A je null");
					LinkedList izoList = (LinkedList) finalIsolines.get(izoB.data);
					if (izoList == null)
						break;
					tree.remove(coordB);
					//	Coordinate helpCoord = (Coordinate)izoList.getFirst();
						if (((Coordinate)izoList.getFirst()).equals2D(coordB)){
					//		System.out.println(coordA.toString());
					//		System.out.println((Coordinate)izoList.getFirst());

							izoList.addFirst(coordA);
							tree.insert(coordA, izoB.data);
							
						}	
						else{
							izoList.addLast(coordA);
							tree.insert(coordA, izoB.data);
						}
						break;
				}
				case 3:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
					if (izoList == null)
						break;
				//	System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);
					if ((izoA.data.intValue() == izoB.data.intValue())){
					//	System.out.println("A i B stejny spojuje");
	//					System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);
							tree.remove(coordA);
							tree.remove(coordB);
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
					//	System.out.println("A i B ale nejsou stejny");
							LinkedList izoListB = (LinkedList) finalIsolines.get(izoB.data);
							if (izoListB == null)
								break;
								
						
						
						
						
						
						//Coordinate helpCoord = (Coordinate)izoList.getFirst();
					//	System.out.println("IZOA "+izoA.data+"    IZOB "+izoB.data);
					//	System.out.println("IZOLIST:"+izoList.toString());
							if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
								if (((Coordinate)izoListB.getFirst()).equals2D(coordB)){
									Iterator iterIzoB = izoListB.iterator();
									while (iterIzoB.hasNext()){
										izoList.addFirst(iterIzoB.next());
							//			System.out.println("prvni1  "+((Coordinate)izoList.getFirst()).toString());
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
						//				System.out.println("prvni3");
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
						((DVertex) tree.search((Coordinate)izoList.getLast())).data = izoA.data;
						((DVertex) tree.search((Coordinate)izoList.getFirst())).data = izoA.data;

					}
					tree.remove(coordA);
					tree.remove(coordB);
	
				}
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
