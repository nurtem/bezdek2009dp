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

package es.unex.sextante.vectorTools.bezierSurface;

import java.util.Iterator;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import es.unex.sextante.vectorTools.tinWithFixedLines.TriangleDT;

class Bezier2 {
	Coordinate normalN1 = null;
	Coordinate normalN2 = null;
	Coordinate normalN3 = null;
	Coordinate b300;
	Coordinate b030;
	Coordinate b003;
	Coordinate b012 = null;
	Coordinate b021 = null;
	Coordinate b102 = null;
	Coordinate b120 = null;
	Coordinate b210 = null;
	Coordinate b201 = null;
	Coordinate b111 = null;
	
	Coordinate n200 = null;
	Coordinate n020 = null;
	Coordinate n002 = null;
	Coordinate n110 = null;
	Coordinate n011 = null;
	Coordinate n101 = null;
	/****************************************************************
	 * Constructor
	 * @param T Trinagle fom original TIN
	 * @param listA - normal vectors of planes in point A of triangle T
	 * @param listB - normal vectors of planes in point B of triangle T
	 * @param listC - normal vectors of planes in point C of triangle T
	 */
	Bezier2(TriangleDT T, LinkedList listA, LinkedList listB, LinkedList listC){
		b300 = T.A;
		b030 = T.B;
		b003 = T.C;
		setNormalVector(listA,listB,listC);
		//System.out.println(listA.size()+" "+normalN1.toString());
		//System.out.println(normalN2.toString());
		//System.out.println(normalN3.toString());
		setControlPoints();
	}
	
	Bezier2(Coordinate[] coords){
		b300 = coords[0];
		b030 = coords[1];
		b003 = coords[2];
	}
	
	Bezier2(Coordinate[] coords,LinkedList listA, LinkedList listB, LinkedList listC){
		b300 = coords[0];
		b030 = coords[1];
		b003 = coords[2];
		setNormalVector(listA,listB,listC);
		setControlPoints();
	}
	
	/*******************************************************************
	 * Protected method for setting one normals for each vertex of T
	 * @param listA - normal vectors of planes in point A of triangle T
	 * @param listB - normal vectors of planes in point B of triangle T
	 * @param listC - normal vectors of planes in point C of triangle T
	 */
	protected void setNormalVector(LinkedList listA, LinkedList listB, LinkedList listC){
		normalN1 = countVector(listA, b300);
		normalN2 = countVector(listB, b030);
		normalN3 = countVector(listC, b003);
	}
	
	/*******************************************************************
	 * Private method counts one normal vector from normal vectors of every plane in vertex of triangle
	 * @param list - normal vectors of planes in point of triangle T
	 * @param P / vertex of T
	 * @return normal vector
	 */
	private Coordinate countVector(LinkedList list, Coordinate P){
		Iterator iter = list.iterator();
		double koeficient = 1D;
		//System.out.println("Normaly rovin");
		double sumX = 0;
		double sumY = 0;
		double sumZ = 0;
		while (iter.hasNext()){
			Coordinate X = (Coordinate) iter.next();
			//System.out.println("Normaly rovin"+X.toString());
			sumX += X.x;
			sumY += X.y;
			sumZ += X.z;
		}
		double sum = Math.sqrt(Math.pow(sumX,2)+Math.pow(sumY, 2)+Math.pow(sumZ, 2));
	//	double sum = 1;
		return new Coordinate((sumX/sum)*koeficient, (sumY/sum)*koeficient, (sumZ/sum)*koeficient);
		//return new PointDT((sumX), (sumY), (sumZ));
	}
	
	/******************************************************************
	 * Protected method counts Scalar product of two vectors v1,v2
	 * @param v1 - vector
	 * @param v2 - vector
	 * @return - scalar product
	 */
	protected double countScalarProduct(Coordinate v1,Coordinate v2){
		double scalar =  v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
		//System.out.println(scalar);
		return scalar;
	}
	
	/********************************************************************
	 * Protected method counts difference of two vectors v1, v2
	 * @param v1 - vector
	 * @param v2 - vector
	 * @return differnce vector
	 */
	protected Coordinate countDifferenceProduct (Coordinate v1, Coordinate v2){
		return new Coordinate(v1.x-v2.x,v1.y-v2.y,v1.z-v2.z);
	}
	
	protected Coordinate countSumProduct (Coordinate v1, Coordinate v2){
		return new Coordinate(v1.x+v2.x,v1.y+v2.y,v1.z+v2.z);
	}
	
	protected Coordinate  normalizeVect (Coordinate v){
		double sum = Math.sqrt(Math.pow(v.x,2)+Math.pow(v.y, 2)+Math.pow(v.z, 2));
		//double sum = 1;
		return new Coordinate((v.x/sum), (v.y/sum), (v.z/sum));
	}
	
	protected double helpCount(Coordinate Pi, Coordinate Pj, Coordinate Ni, Coordinate Nj){
		return 2*(countScalarProduct(countDifferenceProduct(Pj,Pi), countSumProduct(Ni,Nj))/
				countScalarProduct(countDifferenceProduct(Pj,Pi), countDifferenceProduct(Pj,Pi)));
	}
	
	protected void setQuadraticNormals(){
		n200 = normalN1;
		n020 = normalN2;
		n002 = normalN3;
		
		
		double help = helpCount(b300, b030, normalN1, normalN2);
		Coordinate dif = countDifferenceProduct(b030, b300);
		dif.x = dif.x * help;
		dif.y = dif.y * help;
		dif.z = dif.z * help;
		n110 = countDifferenceProduct(countSumProduct(normalN1,normalN2),dif);
		//n110 = normalizeVect(n110);
		help = helpCount(b030, b003, normalN2, normalN3);
		dif = countDifferenceProduct(b003, b030);
		dif.x = dif.x * help;
		dif.y = dif.y * help;
		dif.z = dif.z * help;
		n011 = countDifferenceProduct(countSumProduct(normalN2,normalN3),dif);
		//n011 = normalizeVect(n011);
		help = helpCount(b003, b300, normalN3, normalN1);
		dif = countDifferenceProduct(b300, b003);
		dif.x = dif.x * help;
		dif.y = dif.y * help;
		dif.z = dif.z * help;
		n101 = countDifferenceProduct(countSumProduct(normalN3,normalN1),dif);
		//n101 = normalizeVect(n101);
		
		
	}
	
	protected Coordinate getNormal(double u , double v){
		double w = 1-(u+v);
		double x = n200.x * Math.pow(w, 2) + n020.x * Math.pow(u, 2) + n002.x * Math.pow(v, 2)+
					n110.x*w*u + n011.x*u*v + n101.x*w*v;
		
		double y = n200.y * Math.pow(w, 2) + n020.y * Math.pow(u, 2) + n002.y * Math.pow(v, 2)+
		n110.y*w*u + n011.y*u*v + n101.y*w*v;

		double z = n200.z * Math.pow(w, 2) + n020.z * Math.pow(u, 2) + n002.z * Math.pow(v, 2)+
		n110.z*w*u + n011.z*u*v + n101.z*w*v;
		
		return new Coordinate(x,y,z);
	}
	
	/********************************************************************
	 * The method for setting control points of bezier triangle
	 */
	protected void setControlPoints(){
		setQuadraticNormals();
		double koeficient = 1D;
		//b210 = new Coordinate(1.799998632)
		
/*
		b210 = new Coordinate(	(2*b300.x + b030.x )/3,
				                (2*b300.y + b030.y )/3,
				                (2*b300.z + b030.z )/3);
		//countDifferenceProduct(b030,b300).toString();
		b120 = new Coordinate(	(2*b030.x + b300.x )/3,
				                (2*b030.y + b300.y )/3,
				                (2*b030.z + b300.z )/3);
	//	System.out.println("b120"+b120.toString());
		b021 = new Coordinate(	(2*b030.x + b003.x )/3,
				                (2*b030.y + b003.y )/3,
				                (2*b030.z + b003.z )/3);
	//	System.out.println("b021"+b021.toString());
		b012 = new Coordinate(	(2*b003.x + b030.x )/3,
				                (2*b003.y + b030.y )/3,
			                 	(2*b003.z + b030.z )/3);
		//System.out.println("b012"+b012.toString());
		b201 = new Coordinate(	(2*b300.x + b003.x )/3,
				                (2*b300.y + b003.y )/3,
				                (2*b300.z + b003.z )/3);
		//System.out.println("b102"+b102.toString());
		b102 = new Coordinate( (2*b003.x + b300.x )/3,
				               (2*b003.y + b300.y )/3,
		               		   (2*b003.z + b300.z )/3);
		*/	
		b210 = new Coordinate(	(2*b300.x + b030.x - countScalarProduct(countDifferenceProduct(b030,b300),normalN1)*normalN1.x)/3,
                (2*b300.y + b030.y - countScalarProduct(countDifferenceProduct(b030,b300),normalN1) * normalN1.y)/3,
                (2*b300.z + b030.z - countScalarProduct(countDifferenceProduct(b030,b300),normalN1) * normalN1.z)/3);
//countDifferenceProduct(b030,b300).toString();
b120 = new Coordinate(	(2*b030.x + b300.x - countScalarProduct(countDifferenceProduct(b300,b030), normalN2) * normalN2.x)/3,
                (2*b030.y + b300.y - countScalarProduct(countDifferenceProduct(b300,b030), normalN2) * normalN2.y)/3,
                (2*b030.z + b300.z - countScalarProduct(countDifferenceProduct(b300,b030), normalN2) * normalN2.z)/3);
//	System.out.println("b120"+b120.toString());
b021 = new Coordinate(	(2*b030.x + b003.x - countScalarProduct(countDifferenceProduct(b003,b030), normalN2) * normalN2.x)/3,
                (2*b030.y + b003.y - countScalarProduct(countDifferenceProduct(b003,b030), normalN2) * normalN2.y)/3,
                (2*b030.z + b003.z - countScalarProduct(countDifferenceProduct(b003,b030), normalN2) * normalN2.z)/3);
//	System.out.println("b021"+b021.toString());
b012 = new Coordinate(	(2*b003.x + b030.x - countScalarProduct(countDifferenceProduct(b030,b003), normalN3) * normalN3.x)/3,
                (2*b003.y + b030.y - countScalarProduct(countDifferenceProduct(b030,b003), normalN3) * normalN3.y)/3,
             	(2*b003.z + b030.z - countScalarProduct(countDifferenceProduct(b030,b003), normalN3) * normalN3.z)/3);
//System.out.println("b012"+b012.toString());
b201 = new Coordinate(	(2*b300.x + b003.x - countScalarProduct(countDifferenceProduct(b003,b300), normalN1) * normalN1.x)/3,
                (2*b300.y + b003.y - countScalarProduct(countDifferenceProduct(b003,b300), normalN1) * normalN1.y)/3,
                (2*b300.z + b003.z - countScalarProduct(countDifferenceProduct(b003,b300), normalN1) * normalN1.z)/3);
//System.out.println("b102"+b102.toString());
b102 = new Coordinate( (2*b003.x + b300.x - countScalarProduct(countDifferenceProduct(b300,b003), normalN3) * normalN3.x)/3,
               (2*b003.y + b300.y - countScalarProduct(countDifferenceProduct(b300,b003), normalN3) * normalN3.y)/3,
       		   (2*b003.z + b300.z - countScalarProduct(countDifferenceProduct(b300,b003), normalN3) * normalN3.z)/3);



		
		
/*		
		b210 = new Coordinate(	(2*b300.x + b030.x - countDifferenceProduct(b030,b300).x * getNormal(0.3333333D, 0D).x*koeficient)/3,
				(2*b300.y + b030.y  - countDifferenceProduct(b030,b300).y * getNormal(0.3333333D, 0D).y*koeficient)/3,
				(2*b300.z + b030.z - countDifferenceProduct(b030,b300).z * getNormal(0.33333333D, 0D).z*koeficient)/3);
		//countDifferenceProduct(b030,b300).toString();
		b120 = new Coordinate(	(2*b030.x + b300.x - countDifferenceProduct(b300,b030).x * getNormal(0.666666D, 0D).x*koeficient)/3,
				(2*b030.y +  b300.y  -  countDifferenceProduct(b300,b030).y * getNormal(0.666666D, 0D).y*koeficient)/3,
				(2*b030.z + b300.z -  countDifferenceProduct(b300,b030).z * getNormal(0.666666D, 0D).z*koeficient)/3);
	//	System.out.println("b120"+b120.toString());
		b021 = new Coordinate(	(2*b030.x + b003.x -  countDifferenceProduct(b003,b030).x * getNormal(0.666666D, 0.3333333D).x*koeficient)/3,
				(2*b030.y + b003.y - countDifferenceProduct(b003,b030).y * getNormal(0.666666D, 0.33333333D).y*koeficient)/3,
				(2*b030.z + b003.z - countDifferenceProduct(b003,b030).z * getNormal(0.666666D, 0.33333333D).z*koeficient)/3);
	//	System.out.println("b021"+b021.toString());
		b012 = new Coordinate(	(2*b003.x + b030.x - countDifferenceProduct(b030,b003).x * getNormal(0.33333333D, 0.6666666D).x*koeficient)/3,
				(2*b003.y + b030.y - countDifferenceProduct(b030,b003).y *getNormal(0.3333333D, 0.6666666D).y*koeficient)/3,
				(2*b003.z + b030.z - countDifferenceProduct(b030,b003).z *getNormal(0.33333333D, 0.6666666D).z*koeficient)/3);
		//System.out.println("b012"+b012.toString());
		b102 = new Coordinate(	(2*b003.x + b300.x - countDifferenceProduct(b300,b003).x *getNormal(0D, 0.6666666D).x*koeficient)/3,
				(2*b003.y + b300.y - countDifferenceProduct(b300,b003).y *getNormal(0D, 0.6666666D).y*koeficient)/3,
				(2*b003.z + b300.z - countDifferenceProduct(b300,b003).z *getNormal(0D, 0.6666666D).z*koeficient)/3);
		//System.out.println("b102"+b102.toString());
		b201 = new Coordinate( (2*b300.x + b003.x - countDifferenceProduct(b003,b300).x *getNormal(0D, 0.33333333D).x*koeficient)/3,
				(2*b300.y + b003.y - countDifferenceProduct(b003,b300).y *getNormal(0D, 0.33333333D).y*koeficient)/3,
				(2*b300.z + b003.z - countDifferenceProduct(b003,b300).z *getNormal(0D, 0.33333333D).z*koeficient)/3);
		//System.out.println("b201"+b201.toString());

	/*	
		b210 = new PointDT(	(2*b300.x + b030.x - countScalarProduct(countDifferenceProduct(b030,b300),normalN1)*normalN1.x*koeficient)/3,
							(2*b300.y + b030.y - countScalarProduct(countDifferenceProduct(b030,b300),normalN1)*normalN1.y*koeficient)/3,
							(2*b300.z + b030.z - countScalarProduct(countDifferenceProduct(b030,b300),normalN1)*normalN1.z*koeficient)/3);
		b120 = new PointDT(	(2*b030.x + b300.x - countScalarProduct(countDifferenceProduct(b300,b030),normalN2)*normalN2.x*koeficient)/3,
							(2*b030.y + b300.y - countScalarProduct(countDifferenceProduct(b300,b030),normalN2)*normalN2.y*koeficient)/3,
							(2*b030.z + b300.z - countScalarProduct(countDifferenceProduct(b300,b030),normalN2)*normalN2.z*koeficient)/3);
		b021 = new PointDT(	(2*b030.x + b003.x - countScalarProduct(countDifferenceProduct(b003,b030),normalN2)*normalN2.x*koeficient)/3,
							(2*b030.y + b003.y - countScalarProduct(countDifferenceProduct(b003,b030),normalN2)*normalN2.y*koeficient)/3,
							(2*b030.z + b003.z - countScalarProduct(countDifferenceProduct(b003,b030),normalN2)*normalN2.z*koeficient)/3);
		b012 = new PointDT(	(2*b003.x + b030.x - countScalarProduct(countDifferenceProduct(b030,b003),normalN3)*normalN3.x*koeficient)/3,
							(2*b003.y + b030.y - countScalarProduct(countDifferenceProduct(b030,b003),normalN3)*normalN3.y*koeficient)/3,
							(2*b003.z + b030.z - countScalarProduct(countDifferenceProduct(b030,b003),normalN3)*normalN3.z*koeficient)/3);
		b102 = new PointDT(	(2*b003.x + b300.x - countScalarProduct(countDifferenceProduct(b300,b003),normalN3)*normalN3.x*koeficient)/3,
							(2*b003.y + b300.y - countScalarProduct(countDifferenceProduct(b300,b003),normalN3)*normalN3.y*koeficient)/3,
							(2*b003.z + b300.z - countScalarProduct(countDifferenceProduct(b300,b003),normalN3)*normalN3.z*koeficient)/3);
		b201 = new PointDT( (2*b300.x + b003.x - countScalarProduct(countDifferenceProduct(b003,b300),normalN1)*normalN1.x*koeficient)/3,
							(2*b300.y + b003.y - countScalarProduct(countDifferenceProduct(b003,b300),normalN1)*normalN1.y*koeficient)/3,
							(2*b300.z + b003.z - countScalarProduct(countDifferenceProduct(b003,b300),normalN1)*normalN1.z*koeficient)/3);
		*/
		Coordinate helpE = new Coordinate((b210.x+b120.x+b021.x+b012.x+b102.x+b201.x)/6,
									      (b210.y+b120.y+b021.y+b012.y+b102.y+b201.y)/6,
									      (b210.z+b120.z+b021.z+b012.z+b102.z+b201.z)/6);
		Coordinate helpV = new Coordinate((b300.x+b030.x+b003.x)/3,
									      (b300.y+b030.y+b003.y)/3,
									      (b300.z+b030.z+b003.z)/3);
		b111 = new Coordinate( helpE.x + (helpE.x-helpV.x)/2,
				 			helpE.y + (helpE.y-helpV.y)/2,
				 			(helpE.z + (helpE.z-helpV.z)/2)*koeficient);
		
		//System.out.println("b111"+b111.toString());
	}
	
	/************************************************************************
	 * Protected method for counting elevation of triangle's point with coordinates u,v 
	 * @param u - barycentric koeficient u
	 * @param v - barycentric koeficient v
	 * @return new point 
	 */
	protected Coordinate getElevation(double u, double v){
		//System.out.println("vypocet> "+u+"  "+v);
		//toStringa();
		double w = 1 - u - v;
/*		double x = b300.x*u + b030.x*v + b003.x*w;

		//double y = b300.y*u + b030.y*v + b003.y*w;

*/		double x = b300.x*Math.pow(w, 3) + b030.x*Math.pow(u,3) + b003.x*Math.pow(v,3)+
					3*b210.x*Math.pow(w, 2)*u +  3*b120.x*Math.pow(u,2)*w + 3*b201.x*Math.pow(w, 2)*v +
					3*b021.x*Math.pow(u, 2)*v + 3*b102.x*Math.pow(v, 2)*w + 3*b012.x*u*Math.pow(v,2)+ 6*b111.x*u*v*w;
		
		double y = b300.y*Math.pow(w, 3) + b030.y*Math.pow(u,3) + b003.y*Math.pow(v,3)+
		3*b210.y*Math.pow(w, 2)*u +  3*b120.y*Math.pow(u,2)*w + 3*b201.y*Math.pow(w, 2)*v +
		3*b021.y*Math.pow(u, 2)*v + 3*b102.y*Math.pow(v, 2)*w + 3*b012.y*u*Math.pow(v,2)+ 6*b111.y*u*v*w;

		double z = b300.z*Math.pow(w, 3) + b030.z*Math.pow(u,3) + b003.z*Math.pow(v,3)+
		3*b210.z*Math.pow(w, 2)*u +  3*b120.z*Math.pow(u,2)*w + 3*b201.z*Math.pow(w, 2)*v +
		3*b021.z*Math.pow(u, 2)*v + 3*b102.z*Math.pow(v, 2)*w + 3*b012.z*u*Math.pow(v,2)+ 6*b111.z*u*v*w;

		//	System.out.println(b210.z);
	//	System.out.println(y);
	//	System.out.println(z);
	//	System.out.println((new PointDT(x,y,z)).toString());
		return new Coordinate(x,y,z);
	}
	
	/******************************************************************
	 * The method to print Bezier triangle to console 
	 */
	protected void toStringa(){
		System.out.println("======================================");
		System.out.println(b300.toString());
		System.out.println(b030.toString());
		System.out.println(b003.toString());
		System.out.println("Normals:");
		if (normalN1 != null){
			System.out.println(normalN1.toString());
			System.out.println(normalN2.toString());
			System.out.println(normalN3.toString());
			System.out.println("Koeficients");
			
				System.out.println(b012.toString());
				System.out.println(b021.toString());
				System.out.println(b102.toString());
				System.out.println(b120.toString());
				System.out.println(b210.toString());
				System.out.println(b201.toString());
				System.out.println(b111.toString());
			
		}	
		System.out.println("======================================");
	}
	
	/******************************************************************
	 * The method which compare points
	 * @param P - points for comparing
	 * @return index A,B,C which point is same or N if point P not exist in triangle
	 */
	public char compareReturnIndex(Coordinate P){
		if (P.equals2D(b300))
			return 'A';
		if (P.equals2D(b030))
			return 'B';
		if (P.equals2D(b003))
			return 'C';
		return 'N';
		
	}
	
	/************************************************************************
	 * Protected method for getting envelope of triangle
	 * @return envelope of triangle
	 */
	public Envelope getEnvelope() {
		Coordinate[] newPoint = new Coordinate[4];
		newPoint[0] = b003;
		newPoint[1] = b030;
		newPoint[2] = b300;
		newPoint[3] = b003;
		CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(
				newPoint);

		LinearRing trianglesPoints = new LinearRing(newPointsTriangle,
				new GeometryFactory());

		return trianglesPoints.getEnvelopeInternal();
	}

}

