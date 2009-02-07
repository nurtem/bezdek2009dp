package org.geotools.delaunay.beziersurface;

import org.geotools.delaunay.TriangleDT;
import org.geotools.delaunay.PointDT;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.LinkedList;
import java.util.Iterator;

class TriangleBezier {
	PointDT normalN1 = null;
	PointDT normalN2 = null;
	PointDT normalN3 = null;
	PointDT b300;
	PointDT b030;
	PointDT b003;
	PointDT b012 = null;
	PointDT b021 = null;
	PointDT b102 = null;
	PointDT b120 = null;
	PointDT b210 = null;
	PointDT b201 = null;
	PointDT b111 = null;
	
	/****************************************************************
	 * Constructor
	 * @param T Trinagle fom original TIN
	 * @param listA - normal vectors of planes in point A of triangle T
	 * @param listB - normal vectors of planes in point B of triangle T
	 * @param listC - normal vectors of planes in point C of triangle T
	 */
	TriangleBezier(TriangleDT T, LinkedList listA, LinkedList listB, LinkedList listC){
		b300 = T.A;
		b030 = T.B;
		b003 = T.C;
		setNormalVector(listA,listB,listC);
		//System.out.println(listA.size()+" "+normalN1.toString());
		//System.out.println(normalN2.toString());
		//System.out.println(normalN3.toString());
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
	private PointDT countVector(LinkedList list, PointDT P){
		Iterator iter = list.iterator();
		
		//System.out.println("Normaly rovin");
		double sumX = 0;
		double sumY = 0;
		double sumZ = 0;
		while (iter.hasNext()){
			PointDT X = (PointDT) iter.next();
			//System.out.println("Normaly rovin"+X.toString());
			sumX += X.x;
			sumY += X.y;
			sumZ += X.z;
		}
		double sum = Math.sqrt(Math.pow(sumX,2)+Math.pow(sumY, 2)+Math.pow(sumZ, 2));
		//double sum = 1;
		return new PointDT((sumX/sum), (sumY/sum), (sumZ/sum));
		//return new PointDT((sumX), (sumY), (sumZ));
	}
	
	/******************************************************************
	 * Protected method counts Scalar product of two vectors v1,v2
	 * @param v1 - vector
	 * @param v2 - vector
	 * @return - scalar product
	 */
	protected double countScalarProduct(PointDT v1, PointDT v2){
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
	protected PointDT countDifferenceProduct (PointDT v1, PointDT v2){
		return new PointDT(v1.x-v2.x,v1.y-v2.y,v1.z-v2.z);
	}
	
	/********************************************************************
	 * The method for setting control points of bezier triangle
	 */
	protected void setControlPoints(){
		double koeficient = 1D;
		b210 = new PointDT(	(2*b300.x + b030.x)/3,
				(2*b300.y + b030.y)/3,
				(2*b300.z + b030.z - countScalarProduct(countDifferenceProduct(b030,b300),normalN1)*normalN1.z*koeficient)/3);
		//countDifferenceProduct(b030,b300).toString();
		b120 = new PointDT(	(2*b030.x + b300.x)/3,
				(2*b030.y + b300.y)/3,
				(2*b030.z + b300.z - countScalarProduct(countDifferenceProduct(b300,b030),normalN2)*normalN2.z*koeficient)/3);
	//	System.out.println("b120"+b120.toString());
		b021 = new PointDT(	(2*b030.x + b003.x)/3,
				(2*b030.y + b003.y)/3,
				(2*b030.z + b003.z - countScalarProduct(countDifferenceProduct(b003,b030),normalN2)*normalN2.z*koeficient)/3);
	//	System.out.println("b021"+b021.toString());
		b012 = new PointDT(	(2*b003.x + b030.x)/3,
				(2*b003.y + b030.y)/3,
				(2*b003.z + b030.z - countScalarProduct(countDifferenceProduct(b030,b003),normalN3)*normalN3.z*koeficient)/3);
		//System.out.println("b012"+b012.toString());
		b102 = new PointDT(	(2*b003.x + b300.x)/3,
				(2*b003.y + b300.y)/3,
				(2*b003.z + b300.z - countScalarProduct(countDifferenceProduct(b300,b003),normalN3)*normalN3.z*koeficient)/3);
		//System.out.println("b102"+b102.toString());
		b201 = new PointDT( (2*b300.x + b003.x)/3,
				(2*b300.y + b003.y)/3,
				(2*b300.z + b003.z - countScalarProduct(countDifferenceProduct(b003,b300),normalN1)*normalN1.z*koeficient)/3);
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
		PointDT helpE = new PointDT((b210.x+b120.x+b021.x+b012.x+b102.x+b201.x)/6,
									(b210.y+b120.y+b021.y+b012.y+b102.y+b201.y)/6,
									(b210.z+b120.z+b021.z+b012.z+b102.z+b201.z)/6);
		PointDT helpV = new PointDT((b300.x+b030.x+b003.x)/3,
									(b300.y+b030.y+b003.y)/3,
									(b300.z+b030.z+b003.z)/3);
		b111 = new PointDT( helpE.x + (helpE.x-helpV.x)/2,
				 			helpE.y + (helpE.y-helpV.y)/2,
				 			helpE.z + (helpE.z-helpV.z)/2);
		
		//System.out.println("b111"+b111.toString());
	}
	
	/************************************************************************
	 * Protected method for counting elevation of triangle's point with coordinates u,v 
	 * @param u - barycentric koeficient u
	 * @param v - barycentric koeficient v
	 * @return new point 
	 */
	protected PointDT getElevation(double u, double v){
		//System.out.println("vypocet> "+u+"  "+v);
		//toStringa();
		double w = 1 - u - v;
		double x = b300.x*u + b030.x*v + b003.x*w;

		double y = b300.y*u + b030.y*v + b003.y*w;

/*		double x = b300.x*Math.pow(u, 3) + 3*b210.x*Math.pow(u, 2)*v + 3*b120.x*Math.pow(v,2)*u + b030.x*Math.pow(v,3)+ 3*b021.x*Math.pow(v, 2)*w
				+ 3*b012.x*v*Math.pow(w,2)+b003.x*Math.pow(w,3)+3*b102.x*u*Math.pow(w, 2)+3*b201.x*Math.pow(u, 2)*w + 6*b111.x*u*v*w;
		
		double y = b300.y*Math.pow(u, 3) + 3*b210.y*Math.pow(u, 2)*v + 3*b120.y*Math.pow(v,2)*u + b030.y*Math.pow(v,3)+ 3*b021.y*Math.pow(v, 2)*w
		+ 3*b012.y*v*Math.pow(w,2)+b003.y*Math.pow(w,3)+3*b102.y*u*Math.pow(w, 2)+3*b201.y*Math.pow(u, 2)*w + 6*b111.y*u*v*w;
*/
		double z = b300.z*Math.pow(u, 3) + 3*b210.z*Math.pow(u, 2)*v + 3*b120.z*Math.pow(v,2)*u + b030.z*Math.pow(v,3)+ 3*b021.z*Math.pow(v, 2)*w
		+ 3*b012.z*v*Math.pow(w,2)+b003.z*Math.pow(w,3)+3*b102.z*u*Math.pow(w, 2)+3*b201.z*Math.pow(u, 2)*w + 6*b111.z*u*v*w;
	//toStringa();
		//	System.out.println(b210.z);
	//	System.out.println(y);
	//	System.out.println(z);
	//	System.out.println((new PointDT(x,y,z)).toString());
		return new PointDT(x,y,z);
	}
	
	/******************************************************************
	 * The method to print Bezier triangle to console 
	 */
	protected void toStringa(){
		System.out.println("======================================");
		System.out.println(normalN1.toString());
		System.out.println(normalN2.toString());
		System.out.println(normalN3.toString());
		System.out.println(b300.toString());
		System.out.println(b030.toString());
		System.out.println(b003.toString());
		System.out.println(b012.toString());
		System.out.println(b021.toString());
		System.out.println(b102.toString());
		System.out.println(b120.toString());
		System.out.println(b210.toString());
		System.out.println(b201.toString());
		System.out.println(b111.toString());
		
		System.out.println("======================================");
	}
}
