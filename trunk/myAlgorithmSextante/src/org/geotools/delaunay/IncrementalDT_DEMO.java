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


package org.geotools.delaunay;

import java.util.Date;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;
public class IncrementalDT_DEMO {
	
	
	/***************************************************************
	 * The private method for counting time of triangulation
	 *
	 * @return long - actual time
	 * 
	 */
	private static long getTimeMillis() {
			Date d = new Date();
			return d.getTime();
		    }

	/**
	 * The main method
	 * @param args 
	 */
	 public static void main(String[] args) {
		 /***************************************************************
		  * Simple demo for using modules IncrementalDT, TINWithFixedLines, LinearContourLines
		  * 
		  *  At first you must create object DelaunayDataStore for saving triangles.
		  *  You can choose between variants of data storing 
		  *  		- to memory:  DelaunayDataStoreRAM()
		  *  						constructor is without parametres
		  *  		- to file:	DelaunayDataStoreHDD(String path, String file)
		  *  					 
		  */
		 
		 //DelaunayDataStore triangles = new DelaunayDataStoreHDD("","data");
		 DelaunayDataStore triangles = new DelaunayDataStoreRAM();
	 
		 
		 /***************************************************************
		  * Creating instance of IncrementalDT,
		  * params for constructor is DelaunayDataStore
		  */
		 IncrementalDT triangulace=new IncrementalDT(triangles);
	 
		 /***************************************************************
		  * This cycle generates random points and these are saved into triangulation 
		  */
		 long before = getTimeMillis();
		 for (int i=0;i<500;i++){
			
			 Coordinate x=new Coordinate(((-1*(Math.random()*1000000))),((-1*(Math.random()*1000000))),(Math.random()*1000));
			 triangulace.insertPoint(x);
			 if (i%1000==0)
				 System.out.println(i);
		 }
		 
				 
		 System.out.println("");
		 System.out.println("time>  " +(getTimeMillis()-before));
		 System.out.println("triangulation OK:");
		 System.out.println("number of triangles: "+triangulace.number_Triangles);

	/*	 Coordinate x1 = new Coordinate(1,1,10);
		 triangulace.insertPoint(x1);
		 Coordinate x2 = new Coordinate(1,31,10);
		 triangulace.insertPoint(x2);
		 Coordinate x3 = new Coordinate(31,1,10);
		 triangulace.insertPoint(x3);
		 Coordinate x4 = new Coordinate(31,31,10);
		 triangulace.insertPoint(x4);
		 Coordinate x5 = new Coordinate(16,16,20);
		 triangulace.insertPoint(x5);
		
		 x5 = new Coordinate(7.5,7.5,10);
		 triangulace.insertPoint(x5);
		 x5 = new Coordinate(23.5,7.5,10);
		 triangulace.insertPoint(x5);
		 x5 = new Coordinate(23.5,23.5,10);
		 triangulace.insertPoint(x5);
		 x5 = new Coordinate(7.5,23.5,10);
		 triangulace.insertPoint(x5);

		 
		*/ 
		 
		 /***************************************************************
		  * The next four points are saved separately, because they will be used for
		  * generating fixed lines as start point and end point of line 
		  */
		 Coordinate x1 = new Coordinate(-1*(Math.random()*1000000),-1*(Math.random()*1000000),(Math.random()*100));
		 triangulace.insertPoint(x1);
		 Coordinate x2 = new Coordinate(-1*(Math.random()*1000000),-1*(Math.random()*1000000),(Math.random()*100));
		 triangulace.insertPoint(x2);
		 Coordinate x3 = new Coordinate(-1*(Math.random()*1000000),-1*(Math.random()*1000000),(Math.random()*100));
		 triangulace.insertPoint(x3);
		 Coordinate x4 = new Coordinate(-1*(Math.random()*1000000),-1*(Math.random()*1000000),(Math.random()*100));
		 triangulace.insertPoint(x4);			
 
		 /***************************************************************
		  * creating two objects LineDT
		  * 	constructor is LineDT(Coordinate startPoint, Coordinate endPoint, boolean isHardBreakLine)
		  * 
		  * New lines are stored to LinkedList()
		  */
		 LinkedList fixedLines = new LinkedList();
		 LineDT L1 = new LineDT(x1,x2,false);
		 LineDT L2 = new LineDT(x3,x4,false);
		 fixedLines.add(L1);
		 fixedLines.add(L2);
	 
		 /***************************************************************
		  * for creating shape file you must use this method
		  * 
		  * triangles.createShapefile(String path, String File, String EPSG code);
		  */
		// triangles.createShapefile("", "TINoriginal.shp","EPSG: 2065");
	 
		 
		 
		 /***************************************************************
		  * for correction TIN module TINWithFixedLines is used
		  * 
		  *  TINWithFixedLines.countTIN(DelaunayDataStore triangles,LinkedList fixedLines);
		  *   
		  *   this method returns corrected TIN
		  */
	//	 triangles = TINWithFixedLines.countTIN(triangles,fixedLines);
	//	 triangles.createShapefile("", "TINWithFL","");

		 /***************************************************************
		  * for creating izolines module LinearContourLines is used
		  * 
		  *  this static method creates shape file of izolines
		  *  the method have these parametres
		  *  LinearContourLines.createIzolinesShapeFile(DelaunayDataStore triangles,
		  *  											int ElevatedDifference,
		  *  											String path,
		  *  											String File,
		  *  											String EPSG code);
		  */
		 
 		 //LinearContourLines.createIzolinesShapeFile(DelaunayDataStore triangles,int ElevatedDifference 10,String path, String Filep", String EPSG code);
 		// LinearContourLines.createIzolinesShapeFile(triangles, 100, "", "IzoLines_linear.shp", "EPSG: 2065");
		 System.out.println("izolinis was counted");
		 
		 /**********************************************************
		  * for creating bezier surface you must
		  * 1. close inserting to triangles
		  * 2. create new Delaunay Data store where new points will be stored
		  * 3. use static method countSurface with these parametres>
		  * BezierSurface.countSurface(DelaunayDataStore newTriangles, DelaunayDataStore originalTIN, int levelOfDensityNewTriangles);
		  */
		 triangles.closeInserting();
		 DelaunayDataStore trianglesNew = new DelaunayDataStoreRAM();
		 
		 //BezierSurface.countSurface(trianglesNew, triangles, 4);
	
		 //you can see function of level of density
		 //trianglesNew.createShapefile("", "TIN_BEZIER_ST2.shp","EPSG: 2065");
		 //now you can use LinearContourLines module for extracting izolines 
		 //LinearContourLines.createIzolinesShapeFile(trianglesNew, 100, "", "IzoLines_Bezie_ST2.shp", "EPSG: 2065");
		 System.out.println("bezier izolines was counted");
	
	
		 
		 
		 
	 }
}
