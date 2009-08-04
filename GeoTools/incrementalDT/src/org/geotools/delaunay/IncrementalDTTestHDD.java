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

import com.vividsolutions.jts.geom.Coordinate;
import java.util.Date;
import junit.framework.*;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;

public class IncrementalDTTestHDD extends TestCase {
	LinkedList points = new LinkedList();
	DelaunayDataStoreHDD triangles = new DelaunayDataStoreHDD("","data");
	IncrementalDT triangulace=new IncrementalDT(triangles);
	
	public IncrementalDTTestHDD(String name){
		super(name);
	}
	
	protected void setUp(){
		
	}
	
	protected void tearDown(){
		
	}

	private static long getTimeMillis() {
		Date d = new Date();
		return d.getTime();
	}
	/*****************************************************************
	 * protected function for counting circum circle of Triangle T
	 * @param T - testing triangle
	 * @param A	- testing point, is inside circle?
	 * @return
	 */
	protected boolean delaunay(TriangleDT T, PointDT A){
		double N=((Math.pow(T.C.x,2)-Math.pow(T.A.x,2)+Math.pow(T.C.y,2)-Math.pow(T.A.y,2))*(T.B.x-T.A.x)-(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)+Math.pow(T.B.y,2)-Math.pow(T.A.y,2))*(T.C.x-T.A.x))/
		 (-2*((T.A.y-T.C.y)*(T.B.x-T.A.x)-(T.A.y-T.B.y)*(T.C.x-T.A.x)));
		double M=(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)-Math.pow((T.A.y-N),2)+Math.pow((T.B.y-N),2))/(2*(T.B.x-T.A.x));
		double R=Math.sqrt(Math.pow((T.A.x-M),2)+Math.pow((T.A.y-N),2)); 			//counting radius of triangle
		if ((R-0.0001)>(Math.sqrt(Math.pow((A.x-M),2)+Math.pow((A.y-N),2))))		//toleration for two triangles which have all points on the same circle
			return true;
		else{
			if (R>0){
				return false;
			}
			else{
				return delaunay(new TriangleDT(T.B,T.C,T.A), A);
			}	
		}
	}

	/****************************************************************
	 * The method for computing TIN
	 * @param numberOfPoints - number of random points
	 */
	protected void countTIN(int numberOfPoints){
		 
		 
		 long before = getTimeMillis();
		 for (int i=0;i<numberOfPoints;i++){
			 Coordinate x=new Coordinate((Math.random()*1000000),(Math.random()*1000000),0);
			 points.add(x);
			// System.out.println(i+" X"+x);
			 triangulace.insertPoint(x);
		 }
		 System.out.println("");
		 System.out.println("time>  " +(getTimeMillis()-before));
		 System.out.println("triangulation is OK:");
		 System.out.println("number of triangles: "+triangulace.number_Triangles);
		 System.out.println("");
		 
	}
	
	/****************************************************************
	 * The test which tests all circum circle of triangles, if they not contain
	 * another point
	 */
	public void testDelaunayCircles(){
		countTIN(1000);
		Iterator itPoints = points.iterator();
		int number = triangles.numberOfTriangles;
		
		for (int i = 0; i < number; i++){
			TriangleDT T = triangles.getTriangle(i);
			while (itPoints.hasNext()){
				PointDT P = new PointDT((Coordinate)itPoints.next());
				if (!T.containsPointAsVertex(P)){
					assertEquals(delaunay(T,P),false);
				}
			}	

		}
	}
	
	/****************************************************************
	 * The test which tests rightness counting all circum circle
	 */
	public void testRightCountCircle(){
		int number = triangles.numberOfTriangles;	
		
		for (int i = 0; i < number; i++){
			TriangleDT T = triangles.getTriangle(i);
			double N=((Math.pow(T.C.x,2)-Math.pow(T.A.x,2)+Math.pow(T.C.y,2)-Math.pow(T.A.y,2))*(T.B.x-T.A.x)-(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)+Math.pow(T.B.y,2)-Math.pow(T.A.y,2))*(T.C.x-T.A.x))/
			 (-2*((T.A.y-T.C.y)*(T.B.x-T.A.x)-(T.A.y-T.B.y)*(T.C.x-T.A.x)));
			double M=(Math.pow(T.B.x,2)-Math.pow(T.A.x,2)-Math.pow((T.A.y-N),2)+Math.pow((T.B.y-N),2))/(2*(T.B.x-T.A.x));
			double R=Math.sqrt(Math.pow((T.A.x-M),2)+Math.pow((T.A.y-N),2)); 			//vypocet polomeru

			assertEquals(R>0,true);
		}
		
	}
	
	/*******************************************************************
	 * The test which tests all triangles, if they are triangles, not line
	 */
	public void testAllIsTriangle(){
		int number = triangles.numberOfTriangles;	
		
		for (int i = 0; i < number; i++){
			TriangleDT T = triangles.getTriangle(i);
			assertEquals(T.isTriangle(),true);
		}

	}
	
	
	/**********************************************************************
	 * The test which test, if all input points are in triangulation as vertex
	 */
	public void testAllPointsExistInTIN(){
		int number = triangles.numberOfTriangles;	
		Iterator itPoints = points.iterator();
		boolean contain;
		
		while (itPoints.hasNext()){
			contain = false;
			PointDT P = new PointDT((Coordinate)itPoints.next());
			for (int i = 0; i < number; i++){
				TriangleDT T = triangles.getTriangle(i);
				if (T.containsPointAsVertex(P)){
					contain = true;
					break;
				}
			}
			assertEquals(contain, true);
		}
		
	}
	
	/********************************************************************
	 * the test which tests all triangles, if not exist duplicity triangle
	 */
	public void testDupliciteTriangles(){
		int number = triangles.numberOfTriangles;	
		for (int i = 0; i < number; i++){
			TriangleDT T = triangles.getTriangle(i);
			for (int j = 0; j < number; i++){
				TriangleDT TT = triangles.getTriangle(j);
				assertEquals(T.compare(TT), false);
			}
		}
	}
	
	
	
	public static Test suite(){
		return new TestSuite(IncrementalDTTestHDD.class);
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
