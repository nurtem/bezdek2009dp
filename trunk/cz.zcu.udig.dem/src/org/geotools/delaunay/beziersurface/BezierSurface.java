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

package org.geotools.delaunay.beziersurface;

import org.geotools.delaunay.TriangleDT;
import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.DelaunayDataStore;
import java.util.LinkedList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.Collection;
import java.util.Iterator;

public class BezierSurface {
	static DelaunayDataStore trianglesDToriginal;
	
	/******************************************************************
	 * The method for setting normal vectors of two vectors
	 * @param A - vector A
	 * @param B - vector B
	 * @return normal vector
	 */
	protected static PointDT setNormalVector(PointDT A, PointDT B){
		PointDT normal = new PointDT(A.y*B.z-A.z*B.y, A.z*B.x-A.x*B.z, (A.x*B.y-A.y*B.x));
		//double sum = Math.sqrt(Math.pow(normal.x,2)+Math.pow(normal.y, 2)+Math.pow(normal.z, 2));
		double sum = 1;
		if (normal.z>0)
			return new PointDT((normal.x/sum), (normal.y/sum), (normal.z/sum));
		else
			return new PointDT((-1)*(normal.x/sum), (-1)*(normal.y/sum), (-1)*(normal.z/sum));
	}
	
	/*************************************************************************
	 * The method searchs normals vector for vertex P of triangle T
	 * @param T - triangle
	 * @param P - vertex of triangle
	 * @return - linked list of vectors
	 */
	protected static LinkedList searchVectors(TriangleDT T, PointDT P){
		//System.out.println("PRO BOD> "+P.toString());
		LinkedList vectors = new LinkedList();
		LinkedList points = new LinkedList();
		Collection listOfTriangles = trianglesDToriginal.getIntersectEnvelope(new Envelope((Coordinate)P));
		Iterator iterTriangles = listOfTriangles.iterator();
		//System.out.println("SIYE"+listOfTriangles.size());
		//System.out.println("POINT"+P.toString());
		while (iterTriangles.hasNext()){
			TriangleDT TT = (TriangleDT)iterTriangles.next();
			char index = TT.compareReturnIndex(P);
			switch (index){
				case 'A':{
					PointDT v1 = setVector(P,TT.B);
					PointDT v2 = setVector(P,TT.C);
					vectors.add(setNormalVector(v1,v2));
					break;
				}
				case 'B':{
					PointDT v1 = setVector(P,TT.A);
					PointDT v2 = setVector(P,TT.C);
					vectors.add(setNormalVector(v1,v2));
					break;
				}
				case 'C':{
					PointDT v1 = setVector(P,TT.B);
					PointDT v2 = setVector(P,TT.A);
					vectors.add(setNormalVector(v1,v2));
				}	
			}
		}
		//System.out.println("LIST"+vectors.size());
		return vectors;
		
	}
	
	/*********************************************************************
	 * The method which sets new vecter between points A and B
	 * @param A - start point
	 * @param B - stop point
	 * @return vector AB
	 */ 
	protected static PointDT setVector(PointDT A, PointDT B){
		return new PointDT(B.x-A.x,B.y-A.y,B.z-A.z);
		
	}

	/*********************************************************************
	 * Public method for determining new small triangles of original TIN
	 * @param trianglesDTBezier - new triangles will be saved here
	 * @param trianglesDT - original TIN
	 * @param indexDensity - index of density (level of smoothing)
	 */
	public static void countSurface(DelaunayDataStore trianglesDTBezier, DelaunayDataStore trianglesDT, int indexDensity){
		trianglesDToriginal = trianglesDT;
		
		int numberOfTriangles = trianglesDToriginal.getNumberOfTriangles();
		for (int index=0; index < numberOfTriangles; index++){
			//System.out.println("/////////////////////////index");
			TriangleDT T = new TriangleDT(trianglesDToriginal.getTriangle(index));
			//T.toStringa();
			Bezier2 bezierT = new Bezier2(T,
											searchVectors(T,T.A),
											searchVectors(T,T.B),
											searchVectors(T,T.C)); 
			double [] indexes = new double[indexDensity+2];
			double koeficient = 1/((double)indexDensity+1);
			for (int i = 0; i<=indexDensity+1; i++){
				indexes[i] = koeficient*i;
			//	System.out.println("ooo"+indexes[i]);
			}
			int maxTi = indexDensity+1;
			int maxTj = indexDensity;
			for (int i = 0; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					TriangleDT TTT = new TriangleDT(bezierT.getElevation(indexes[i], indexes[j]),
												   bezierT.getElevation(indexes[i], indexes[j+1]),
												   bezierT.getElevation(indexes[i+1], indexes[j]));
					trianglesDTBezier.insertToTree(TTT, TTT.key);
					//TTT.toStringa();
	//				System.out.println("Troju"+ indexes[i]+","+ indexes[j]+"  "+ indexes[i]+","+  indexes[j+1]+"  "+ indexes[i+1]+","+  indexes[j]);
	//				System.out.println();				
				}
				maxTj --;
			}
	//		System.out.println("hotovo");
			maxTj = indexDensity-1;
			for (int i = 1; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					TriangleDT TTT = new TriangleDT(bezierT.getElevation(indexes[i], indexes[j]),
												   bezierT.getElevation(indexes[i], indexes[j+1]),
												   bezierT.getElevation(indexes[i-1], indexes[j+1]));
					trianglesDTBezier.insertToTree(TTT, TTT.key);
	//				System.out.println("Troju"+ indexes[i]+","+ indexes[j]+"  "+ indexes[i]+","+  indexes[j+1]+"  "+ indexes[j+1]+","+  indexes[i-1]);
	//				System.out.println();				
				}
				maxTj --;
			}
//			System.out.println("hotovo");
			
			
			//T.toStringa();
	/*		TriangleDT T1 = new TriangleDT(bezierT.getElevation(0, 0), bezierT.getElevation(0, 0.33333D), bezierT.getElevation(0.33333D , 0));
			TriangleDT T2 = new TriangleDT(bezierT.getElevation(0, 0.33333D), bezierT.getElevation(0.33333D, 0.33333D), bezierT.getElevation(0, 0.666666D));
			TriangleDT T3 = new TriangleDT(bezierT.getElevation(0, 0.666666D), bezierT.getElevation(0.33333D, 0.666666D), bezierT.getElevation(0, 1));
			TriangleDT T4 = new TriangleDT(bezierT.getElevation(0, 0.33333D), bezierT.getElevation(0.33333D, 0), bezierT.getElevation(0.33333D, 0.33333D));
			TriangleDT T5 = new TriangleDT(bezierT.getElevation(0, 0.666666D), bezierT.getElevation(0.33333D, 0.33333D), bezierT.getElevation(0.33333D, 0.666666D));
			TriangleDT T6 = new TriangleDT(bezierT.getElevation(0.33333D, 0), bezierT.getElevation(0.33333D, 0.33333D), bezierT.getElevation(0.666666D, 0));
			TriangleDT T7 = new TriangleDT(bezierT.getElevation(0.33333D, 0.33333D), bezierT.getElevation(0.33333D, 0.666666D), bezierT.getElevation(0.666666D, 0.33333333D));
			TriangleDT T8 = new TriangleDT(bezierT.getElevation(0.666666D, 0.33333D), bezierT.getElevation(0.666666D, 0), bezierT.getElevation(0.33333D, 0.33333D));
			TriangleDT T9 = new TriangleDT(bezierT.getElevation(0.666666D, 0.33333D), bezierT.getElevation(0.666666D, 0), bezierT.getElevation(1, 0));
			trianglesDTBezier.insertToTree(T1, T1.key);
			trianglesDTBezier.insertToTree(T2, T2.key);
			trianglesDTBezier.insertToTree(T3, T3.key);
			trianglesDTBezier.insertToTree(T4, T4.key);
			trianglesDTBezier.insertToTree(T5, T5.key);
			trianglesDTBezier.insertToTree(T6, T6.key);
			trianglesDTBezier.insertToTree(T7, T7.key);
			trianglesDTBezier.insertToTree(T8, T8.key);
			trianglesDTBezier.insertToTree(T9, T9.key);
	*/
			
			
			
	/*		TriangleDT T1 = new TriangleDT(bezierT.getElevation(0, 0), bezierT.getElevation(0, 0.25D), bezierT.getElevation(0.25D , 0));
			TriangleDT T2 = new TriangleDT(bezierT.getElevation(0.25D, 0), bezierT.getElevation(0, 0.25D), bezierT.getElevation(0.25D , 0.25D));
			TriangleDT T3 = new TriangleDT(bezierT.getElevation(0.25D, 0.25D), bezierT.getElevation(0, 0.25D), bezierT.getElevation(0 , 0.5D));
			TriangleDT T4 = new TriangleDT(bezierT.getElevation(0, 0.5D), bezierT.getElevation(0.25D, 0.25D), bezierT.getElevation(0.25D , 0.5D));
			TriangleDT T5 = new TriangleDT(bezierT.getElevation(0, 0.5D), bezierT.getElevation(0, 0.75D), bezierT.getElevation(0.25D , 0.5D));
			TriangleDT T6 = new TriangleDT(bezierT.getElevation(0, 0.75D), bezierT.getElevation(0.25D, 0.75D), bezierT.getElevation(0.25D , 0.5D));
			TriangleDT T7 = new TriangleDT(bezierT.getElevation(0, 1D), bezierT.getElevation(0, 0.75D), bezierT.getElevation(0.25D , 0.75D));
			TriangleDT T8 = new TriangleDT(bezierT.getElevation(0.25D, 0D), bezierT.getElevation(0.25D, 0.25D), bezierT.getElevation(0.5D , 0));
			TriangleDT T9 = new TriangleDT(bezierT.getElevation(0.25D, 0.25D), bezierT.getElevation(0.5D, 0D), bezierT.getElevation(0.5D , 0.25D));
			TriangleDT T10 = new TriangleDT(bezierT.getElevation(0.25D, 0.25D), bezierT.getElevation(0.5D, 0.25D), bezierT.getElevation(0.25D , 0.5D));
			TriangleDT T11 = new TriangleDT(bezierT.getElevation(0.25D, 0.5D), bezierT.getElevation(0.5D, 0.25D), bezierT.getElevation(0.5D , 0.5D));
			TriangleDT T12 = new TriangleDT(bezierT.getElevation(0.25D, 0.5D), bezierT.getElevation(0.5D, 0.5D), bezierT.getElevation(0.25D , 0.75D));
			TriangleDT T13 = new TriangleDT(bezierT.getElevation(0.5D, 0D), bezierT.getElevation(0.75D, 0D), bezierT.getElevation(0.5D , 0.25D));
			TriangleDT T14 = new TriangleDT(bezierT.getElevation(0.5D, 0.25D), bezierT.getElevation(0.75D, 0D), bezierT.getElevation(0.75D , 0.25D));
			TriangleDT T15 = new TriangleDT(bezierT.getElevation(0.5D, 0.25D), bezierT.getElevation(0.75D, 0.25D), bezierT.getElevation(0.5D , 0.5D));
			TriangleDT T16 = new TriangleDT(bezierT.getElevation(0.75D, 0D), bezierT.getElevation(0.75D, 0.25D), bezierT.getElevation(1D , 0D));
						
			trianglesDTBezier.insertToTree(T1, T1.key);
			trianglesDTBezier.insertToTree(T2, T2.key);
			trianglesDTBezier.insertToTree(T3, T3.key);
			trianglesDTBezier.insertToTree(T4, T4.key);
			trianglesDTBezier.insertToTree(T5, T5.key);
			trianglesDTBezier.insertToTree(T6, T6.key);
			trianglesDTBezier.insertToTree(T7, T7.key);
			trianglesDTBezier.insertToTree(T8, T8.key);
			trianglesDTBezier.insertToTree(T9, T9.key);
			trianglesDTBezier.insertToTree(T10, T10.key);
			trianglesDTBezier.insertToTree(T11, T11.key);
			trianglesDTBezier.insertToTree(T12, T12.key);
			trianglesDTBezier.insertToTree(T13, T13.key);
			trianglesDTBezier.insertToTree(T14, T14.key);
			trianglesDTBezier.insertToTree(T15, T15.key);
			trianglesDTBezier.insertToTree(T16, T16.key);
		
		
		
			TriangleDT T1 = new TriangleDT(bezierT.getElevation(0, 0), bezierT.getElevation(0, 0.1666666D), bezierT.getElevation(0.1666666D , 0));
			TriangleDT T2 = new TriangleDT(bezierT.getElevation(0.1666666D, 0), bezierT.getElevation(0, 0.1666666D), bezierT.getElevation(0.1666666D , 0.1666666D));
			TriangleDT T3 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.1666666D), bezierT.getElevation(0, 0.1666666D), bezierT.getElevation(0 , 0.3333333D));
			TriangleDT T4 = new TriangleDT(bezierT.getElevation(0, 0.3333333D), bezierT.getElevation(0.1666666D, 0.1666666D), bezierT.getElevation(0.1666666D , 0.3333333D));
			TriangleDT T5 = new TriangleDT(bezierT.getElevation(0, 0.5D), bezierT.getElevation(0, 0.333333333D), bezierT.getElevation(0.1666666D , 0.3333333333D));
			TriangleDT T6 = new TriangleDT(bezierT.getElevation(0, 0.5D), bezierT.getElevation(0.1666666D, 0.333333333D), bezierT.getElevation(0.1666666D , 0.5D));
			TriangleDT T7 = new TriangleDT(bezierT.getElevation(0, 0.5D), bezierT.getElevation(0, 0.6666666D), bezierT.getElevation(0.1666666D , 0.5D));
			TriangleDT T8 = new TriangleDT(bezierT.getElevation(0, 0.66666666666D), bezierT.getElevation(0.1666666D, 0.5D), bezierT.getElevation(0.1666666D , 0.666666666D));
			TriangleDT T9 = new TriangleDT(bezierT.getElevation(0, 0.66666666666D), bezierT.getElevation(0, 0.8333333333D), bezierT.getElevation(0.1666666D , 0.666666666D));
			TriangleDT T10 = new TriangleDT(bezierT.getElevation(0, 0.8333333333D), bezierT.getElevation(0.1666666D, 0.6666666666D), bezierT.getElevation(0.1666666D , 0.83333333D));
			TriangleDT T11 = new TriangleDT(bezierT.getElevation(0, 0.8333333333D), bezierT.getElevation(0, 1), bezierT.getElevation(0.1666666D , 0.833333D));
			TriangleDT T12 = new TriangleDT(bezierT.getElevation(0.1666666D, 0), bezierT.getElevation(0.1666666D, 0.1666666D), bezierT.getElevation(0.3333333D , 0));
			TriangleDT T13 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.1666666D), bezierT.getElevation(0.333333333D, 0.1666666D), bezierT.getElevation(0.33333333D , 0D));
			TriangleDT T14 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.1666666D), bezierT.getElevation(0.1666666D, 0.333333333D), bezierT.getElevation(0.33333333D , 0.1666666D));
			TriangleDT T15 = new TriangleDT(bezierT.getElevation(0.3333333D, 0.1666666D), bezierT.getElevation(0.3333333D, 0.333333333D), bezierT.getElevation(0.1666666D , 0.33333333D));
			TriangleDT T16 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.33333333D), bezierT.getElevation(0.1666666D, 0.5D), bezierT.getElevation(0.33333333333D , 0.3333333333D));
			TriangleDT T17 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.5D), bezierT.getElevation(0.333333333D, 0.33333333D), bezierT.getElevation(0.33333333D , 0.5D));
			TriangleDT T18 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.5D), bezierT.getElevation(0.1666666D, 0.666666666666D), bezierT.getElevation(0.33333333D , 0.5D));
			TriangleDT T19 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.6666666D), bezierT.getElevation(0.3333333D, 0.5D), bezierT.getElevation(0.33333333D, 0.66666666D));
			TriangleDT T20 = new TriangleDT(bezierT.getElevation(0.1666666D, 0.6666666D), bezierT.getElevation(0.1666666D, 0.8333333D), bezierT.getElevation(0.33333333333D , 0.66666666D));
			TriangleDT T21 = new TriangleDT(bezierT.getElevation(0.33333333D, 0D), bezierT.getElevation(0.33333333D, 0.1666666D), bezierT.getElevation(0.5D , 0D));
			TriangleDT T22 = new TriangleDT(bezierT.getElevation(0.33333333D, 0.1666666D), bezierT.getElevation(0.5D, 0.1666666D), bezierT.getElevation(0.5D , 0D));
			TriangleDT T23 = new TriangleDT(bezierT.getElevation(0.33333333D, 0.1666666D), bezierT.getElevation(0.33333333D, 0.33333333D), bezierT.getElevation(0.5D , 0.16666666D));
			TriangleDT T24 = new TriangleDT(bezierT.getElevation(0.33333333D, 0.3333333D), bezierT.getElevation(0.5D, 0.1666666D), bezierT.getElevation(0.5D , 0.33333333D));
			TriangleDT T25 = new TriangleDT(bezierT.getElevation(0.33333333D, 0.3333333D), bezierT.getElevation(0.33333333D, 0.5D), bezierT.getElevation(0.5D , 0.33333333D));
			TriangleDT T26 = new TriangleDT(bezierT.getElevation(0.5D, 0.3333333D), bezierT.getElevation(0.5D, 0.5D), bezierT.getElevation(0.33333333D , 0.5D));
			TriangleDT T27 = new TriangleDT(bezierT.getElevation(0.33333333D, 0.5D), bezierT.getElevation(0.33333333D, 0.666666D), bezierT.getElevation(0.5D , 0.5D));
			TriangleDT T28 = new TriangleDT(bezierT.getElevation(0.5D, 0D), bezierT.getElevation(0.6666666666D, 0D), bezierT.getElevation(0.5D , 0.16666666D));
			TriangleDT T29 = new TriangleDT(bezierT.getElevation(0.5D, 0.166666666D), bezierT.getElevation(0.6666666666D, 0D), bezierT.getElevation(0.6666666666D , 0.16666666666D));
			TriangleDT T30 = new TriangleDT(bezierT.getElevation(0.5D, 0.1666666666D), bezierT.getElevation(0.5D, 0.33333333D), bezierT.getElevation(0.6666666666D , 0.166666666D));
			TriangleDT T31 = new TriangleDT(bezierT.getElevation(0.5D, 0.33333333333D), bezierT.getElevation(0.6666666666D, 0.333333333D), bezierT.getElevation(0.6666666666D , 0.166666666D));
			TriangleDT T32 = new TriangleDT(bezierT.getElevation(0.5D, 0.33333333333D), bezierT.getElevation(0.5D, 0.5D), bezierT.getElevation(0.6666666666D , 0.3333333333D));
			TriangleDT T33 = new TriangleDT(bezierT.getElevation(0.66666666D, 0D), bezierT.getElevation(0.6666666666D, 0.16666666D), bezierT.getElevation(0.833333333D , 0D));
			TriangleDT T34 = new TriangleDT(bezierT.getElevation(0.66666666D, 0.1666666666D), bezierT.getElevation(0.8333333333D, 0D), bezierT.getElevation(0.8333333D , 0.166666666D));
			TriangleDT T35 = new TriangleDT(bezierT.getElevation(0.66666666D, 0.166666666D), bezierT.getElevation(0.6666666666D, 0.333333333D), bezierT.getElevation(0.8333333D , 0.166666666D));
			TriangleDT T36 = new TriangleDT(bezierT.getElevation(0.8333333D, 0D), bezierT.getElevation(1D, 0D), bezierT.getElevation(0.833333333D , 0.166666666D));

			
			
			
			
			
			
			trianglesDTBezier.insertToTree(T1, T1.key);
			trianglesDTBezier.insertToTree(T2, T2.key);
			trianglesDTBezier.insertToTree(T3, T3.key);
			trianglesDTBezier.insertToTree(T4, T4.key);
			trianglesDTBezier.insertToTree(T5, T5.key);
			trianglesDTBezier.insertToTree(T6, T6.key);
			trianglesDTBezier.insertToTree(T7, T7.key);
			trianglesDTBezier.insertToTree(T8, T8.key);
			trianglesDTBezier.insertToTree(T9, T9.key);
			trianglesDTBezier.insertToTree(T10, T10.key);
			trianglesDTBezier.insertToTree(T11, T11.key);
			trianglesDTBezier.insertToTree(T12, T12.key);
			trianglesDTBezier.insertToTree(T13, T13.key);
			trianglesDTBezier.insertToTree(T14, T14.key);
			trianglesDTBezier.insertToTree(T15, T15.key);
			trianglesDTBezier.insertToTree(T16, T16.key);
			trianglesDTBezier.insertToTree(T17, T17.key);
			trianglesDTBezier.insertToTree(T18, T18.key);
			trianglesDTBezier.insertToTree(T19, T19.key);
			trianglesDTBezier.insertToTree(T20, T20.key);
			trianglesDTBezier.insertToTree(T21, T21.key);
			trianglesDTBezier.insertToTree(T22, T22.key);
			trianglesDTBezier.insertToTree(T23, T23.key);
			trianglesDTBezier.insertToTree(T24, T24.key);
			trianglesDTBezier.insertToTree(T25, T25.key);
			trianglesDTBezier.insertToTree(T26, T26.key);
			trianglesDTBezier.insertToTree(T27, T27.key);
			trianglesDTBezier.insertToTree(T28, T28.key);
			trianglesDTBezier.insertToTree(T29, T29.key);
			trianglesDTBezier.insertToTree(T30, T30.key);
			trianglesDTBezier.insertToTree(T31, T31.key);
			trianglesDTBezier.insertToTree(T32, T32.key);
			trianglesDTBezier.insertToTree(T33, T33.key);
			trianglesDTBezier.insertToTree(T34, T34.key);
			trianglesDTBezier.insertToTree(T35, T35.key);
			trianglesDTBezier.insertToTree(T36, T36.key);
			
		*/
		
		}
		
		
		
		
		
	//	for (int index=0; index <= numberOfTriangles; index++){
	//		triangles.delete(T.key);
	//	}
	//	System.out.println(".............................."+trianglesDTBezier.getNumberOfTriangles());
	//	return trianglesDTBezier;
	}
}
