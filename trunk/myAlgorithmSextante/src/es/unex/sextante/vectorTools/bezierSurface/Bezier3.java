/*    Geotools2 - OpenSource mapping toolkit
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

import es.unex.sextante.vectorTools.tinWithFixedLines.PointDT;
import es.unex.sextante.vectorTools.tinWithFixedLines.TriangleDT;

class Bezier3 {
	Coordinate normalN0 = null;
	Coordinate normalN1 = null;
	Coordinate normalN2 = null;

	Coordinate P004;
	Coordinate P040;
	Coordinate P400;
	Coordinate P013;
	Coordinate P022;
	Coordinate P031;
	Coordinate P130;
	Coordinate P220;
	Coordinate P310;
	Coordinate P301;
	Coordinate P202;
	Coordinate P103;
	Coordinate P112;
	Coordinate P121;
	Coordinate P211;
	
	Coordinate[][] neighbour = new Coordinate[3][3];
	/****************************************************************
	 * Constructor
	 * @param T Trinagle fom original TIN
	 * @param listA - normal vectors of planes in point A of triangle T
	 * @param listB - normal vectors of planes in point B of triangle T
	 * @param listC - normal vectors of planes in point C of triangle T
	 */
	Bezier3(TriangleDT T, LinkedList listA, LinkedList listB, LinkedList listC){
		P400 = T.C;
		P040 = T.B;
		P004 = T.A;
		setNormalVector(listA,listB,listC);
		//System.out.println(listA.size()+" "+normalN1.toString());
		//System.out.println(normalN2.toString());
		//System.out.println(normalN3.toString());
		setControlPoints();
	}
	
	Bezier3(Coordinate[] coords){
		P400 = coords[2];
		P040 = coords[1];
		P004 = coords[0];
	}
	
	Bezier3(Coordinate[] coords,LinkedList listA, LinkedList listB, LinkedList listC){
		P400 = coords[2];
		P040 = coords[1];
		P004 = coords[0];
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
		normalN0 = countVector(listA, P004);
		normalN1 = countVector(listB, P040);
		normalN2 = countVector(listC, P400);
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
	//	System.out.println("Normaly rovin pro Bod"+ P.toString());
		double sumX = 0;
		double sumY = 0;
		double sumZ = 0;
		while (iter.hasNext()){
			Coordinate X = (Coordinate) iter.next();
		//	System.out.println("Normaly rovin"+X.toString());
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
	
	protected double countScalarProduct2D(Coordinate v1,Coordinate v2){
		double scalar =  v1.x*v2.x + v1.y*v2.y;// + v1.z*v2.z;
		//System.out.println(scalar);
		return scalar;
	}
	
	protected Coordinate countCrossProduct(Coordinate A, Coordinate B){
		Coordinate normal = new Coordinate(A.y*B.z-A.z*B.y, A.z*B.x-A.x*B.z, (A.x*B.y-A.y*B.x));
		double sum = Math.sqrt(Math.pow(normal.x,2)+Math.pow(normal.y, 2)+Math.pow(normal.z, 2));
		//double sum = 1;
		if (normal.z>0)
			return new Coordinate((normal.x/sum), (normal.y/sum), (normal.z/sum));
		else
			return new Coordinate((-1)*(normal.x/sum), (-1)*(normal.y/sum), (-1)*(normal.z/sum));
		
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
	
	protected double countNorma(Coordinate A){
		return Math.sqrt(Math.pow(A.x, 2)+Math.pow(A.y, 2)+Math.pow(A.z, 2));
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
	
	protected Coordinate setNormalVector(Coordinate A, Coordinate B){
		Coordinate normal = new Coordinate(A.y*B.z-A.z*B.y, A.z*B.x-A.x*B.z, (A.x*B.y-A.y*B.x));
		//double sum = Math.sqrt(Math.pow(normal.x,2)+Math.pow(normal.y, 2)+Math.pow(normal.z, 2));
		double sum = 1;
		if (normal.z>0)
			return new Coordinate((normal.x/sum), (normal.y/sum), (normal.z/sum));
		else
			return new Coordinate((-1)*(normal.x/sum), (-1)*(normal.y/sum), (-1)*(normal.z/sum));
	}
	
	
/*	protected void setQuadraticNormals(){
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
					2*n110.x*w*u + 2*n011.x*u*v + 2*n101.x*w*v;
		
		double y = n200.y * Math.pow(w, 2) + n020.y * Math.pow(u, 2) + n002.y * Math.pow(v, 2)+
		2*n110.y*w*u + 2*n011.y*u*v + 2*n101.y*w*v;

		double z = n200.z * Math.pow(w, 2) + n020.z * Math.pow(u, 2) + n002.z * Math.pow(v, 2)+
		2*n110.z*w*u + 2*n011.z*u*v + 2*n101.z*w*v;
		
		return  normalizeVect(new Coordinate(x,y,z));
	}
	*/
	/********************************************************************
	 * The method for setting control points of bezier triangle
	 */
	protected void setControlPoints(){

		Coordinate N = (setNormalVector(countDifferenceProduct(P400,P040),countDifferenceProduct(P400,P004)));
		
		double di = Math.sqrt(countScalarProduct(countDifferenceProduct(P040,P004),countDifferenceProduct(P040,P004)));
		double ai = countScalarProduct(normalN0,normalN1);
		Coordinate Ti;
		if (neighbour[0][0] != null){
			Coordinate Nadjacet = (setNormalVector(countDifferenceProduct(neighbour[0][1],neighbour[0][0]),countDifferenceProduct(neighbour[0][2],neighbour[0][0])));
			Ti = normalizeVect(countCrossProduct(N,Nadjacet));
		}
		else{
			Coordinate help1 = countDifferenceProduct(P040,P004);
			double help2 = countNorma(help1);
			Ti = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		}
		double ai0 = countScalarProduct(normalN0,Ti);
		double ai1 = countScalarProduct(normalN1,Ti);
		double roi = 6*(2*ai0 + ai*ai1)/(4 - Math.pow(ai, 2));
		double sii = 6*(2*ai1 + ai*ai0)/(4 - Math.pow(ai, 2));

		
		Coordinate v301 = new Coordinate( P004.x + di*(6*Ti.x - 2*roi*normalN0.x + sii*normalN1.x)/18,
										  P004.y + di*(6*Ti.y - 2*roi*normalN0.y + sii*normalN1.y)/18,
										  P004.z + di*(6*Ti.z - 2*roi*normalN0.z + sii*normalN1.z)/18);
		Coordinate v302 = new Coordinate(	P040.x - di*(6*Ti.x + roi*normalN0.x - 2*sii*normalN1.x)/18,
											P040.y - di*(6*Ti.y + roi*normalN0.y - 2*sii*normalN1.y)/18,
											P040.z - di*(6*Ti.z + roi*normalN0.z - 2*sii*normalN1.z)/18);
		
		P013 = new Coordinate((P004.x + 3*v301.x)/4, (P004.y + 3*v301.y)/4, (P004.z + 3*v301.z)/4);
		P022 = new Coordinate((v301.x + v302.x)/2, (v301.y + v302.y)/2, (v301.z + v302.z)/2);
		P031 = new Coordinate((3*v302.x + P040.x)/4,(3*v302.y + P040.y)/4,(3*v302.z + P040.z)/4);

		
		
		
		
		di = Math.sqrt(countScalarProduct(countDifferenceProduct(P400,P040),countDifferenceProduct(P400,P040)));
		ai = countScalarProduct(normalN1,normalN2);
		
		if (neighbour[1][0] != null){
			Coordinate Nadjacet = (setNormalVector(countDifferenceProduct(neighbour[1][1],neighbour[1][0]),countDifferenceProduct(neighbour[1][2],neighbour[1][0])));
			Ti = (countCrossProduct(N,Nadjacet));
		}	
		else{
			Coordinate help1 = countDifferenceProduct(P400,P040);
			double  help2 = countNorma(help1);
			Ti = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		}
		ai0 = countScalarProduct(normalN1,Ti);
		ai1 = countScalarProduct(normalN2,Ti);
		roi = 6*(2*ai0 + ai*ai1)/(4 - Math.pow(ai, 2));
		sii = 6*(2*ai1 + ai*ai0)/(4 - Math.pow(ai, 2));
		Coordinate v311 = new Coordinate( P040.x + di*(6*Ti.x - 2*roi*normalN1.x + sii*normalN2.x)/18,
										  P040.y + di*(6*Ti.y - 2*roi*normalN1.y + sii*normalN2.y)/18,
										  P040.z + di*(6*Ti.z - 2*roi*normalN1.z + sii*normalN2.z)/18);
		Coordinate v312 = new Coordinate( P400.x - di*(6*Ti.x + roi*normalN1.x - 2*sii*normalN2.x)/18,
										  P400.y - di*(6*Ti.y + roi*normalN1.y - 2*sii*normalN2.y)/18,
										  P400.z - di*(6*Ti.z + roi*normalN1.z - 2*sii*normalN2.z)/18);
		
		P130 = new Coordinate((P040.x + 3*v311.x)/4, (P040.y + 3*v311.y)/4, (P040.z + 3*v311.z)/4);
		P220 = new Coordinate((v311.x + v312.x)/2, (v311.y + v312.y)/2, (v311.z + v312.z)/2);
		P310 = new Coordinate((3*v312.x + P400.x)/4,(3*v312.y + P400.y)/4,(3*v312.z + P400.z)/4);

		di = Math.sqrt(countScalarProduct(countDifferenceProduct(P004,P400),countDifferenceProduct(P004,P400)));
		ai = countScalarProduct(normalN2,normalN0);
		
		if (neighbour[2][0] != null){
			Coordinate Nadjacet = (setNormalVector(countDifferenceProduct(neighbour[2][1],neighbour[2][0]),countDifferenceProduct(neighbour[2][2],neighbour[2][0])));
			Ti = normalizeVect(countCrossProduct(N,Nadjacet));
		}
		else{
			Coordinate help1 = countDifferenceProduct(P004, P400);
			double help2 = countNorma(help1);
			Ti = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		}
		
		ai0 = countScalarProduct(normalN2,Ti);
		ai1 = countScalarProduct(normalN0,Ti);
		roi = 6*(2*ai0 + ai*ai1)/(4 - Math.pow(ai, 2));
		sii = 6*(2*ai1 + ai*ai0)/(4 - Math.pow(ai, 2));
		Coordinate v321 = new Coordinate( P400.x + di*(6*Ti.x - 2*roi*normalN2.x + sii*normalN0.x)/18,
				                          P400.y + di*(6*Ti.y - 2*roi*normalN2.y + sii*normalN0.y)/18,
										  P400.z + di*(6*Ti.z - 2*roi*normalN2.z + sii*normalN0.z)/18);
		Coordinate v322 = new Coordinate( P004.x - di*(6*Ti.x + roi*normalN2.x - 2*sii*normalN0.x)/18,
										  P004.y - di*(6*Ti.y + roi*normalN2.y - 2*sii*normalN0.y)/18,
										  P004.z - di*(6*Ti.z + roi*normalN2.z - 2*sii*normalN0.z)/18);
		
		P301 = new Coordinate((P400.x + 3*v321.x)/4, (P400.y + 3*v321.y)/4, (P400.z + 3*v321.z)/4);
		P202 = new Coordinate((v321.x + v322.x)/2, (v321.y + v322.y)/2, (v321.z + v322.z)/2);
		P103 = new Coordinate((3*v322.x + P004.x)/4,(3*v322.y + P004.y)/4,(3*v322.z + P004.z)/4);

		

		
		Coordinate D00 = new Coordinate(P103.x - (P013.x+P004.x)/2,P103.y - (P013.y+P004.y)/2,P103.z - (P013.z+P004.z)/2);
		Coordinate D03 = new Coordinate(P130.x - (P040.x+P031.x)/2,P130.y - (P040.y+P031.y)/2,P130.z - (P040.z+P031.z)/2);
		Coordinate D10 = new Coordinate(P031.x - (P130.x+P040.x)/2,P031.y - (P130.y+P040.y)/2,P031.z - (P130.z+P040.z)/2);
		Coordinate D13 = new Coordinate(P301.x - (P400.x+P310.x)/2,P301.y - (P400.y+P310.y)/2,P301.z - (P400.z+P310.z)/2);
		Coordinate D20 = new Coordinate(P310.x - (P301.x+P400.x)/2,P310.y - (P301.y+P400.y)/2,P310.z - (P301.z+P400.z)/2);
		Coordinate D23 = new Coordinate(P013.x - (P004.x+P103.x)/2,P013.y - (P004.y+P103.y)/2,P013.z - (P004.z+P103.z)/2);		
		
		Coordinate w00 = countDifferenceProduct(v301,P004);
		Coordinate w01 = countDifferenceProduct(v302,v301);
		Coordinate w10 = countDifferenceProduct(v311,P040);
		Coordinate w11 = countDifferenceProduct(v312,v311);
		Coordinate w21 = countDifferenceProduct(v322,v321);
		Coordinate w20 = countDifferenceProduct(v321,P400);
		Coordinate w02 = countDifferenceProduct(P040,v302);
		Coordinate w12 = countDifferenceProduct(P400,v312);
		Coordinate w22 = countDifferenceProduct(P004,v322);
		
		Coordinate help1 = countCrossProduct(normalN0,w00);
		double help2 = countNorma(w00);
		Coordinate A00 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		help1 = countCrossProduct(normalN1,w10);
		help2 = countNorma(w10);
		Coordinate A10 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		help1 = countCrossProduct(normalN2,w20);
		help2 = countNorma(w20);
		Coordinate A20 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		help1 = countCrossProduct(normalN1,w02);
		help2 = countNorma(w02);
		Coordinate A02 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		help1 = countCrossProduct(normalN2,w12);
		help2 = countNorma(w12);
		Coordinate A12 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		help1 = countCrossProduct(normalN0,w22);
		help2 = countNorma(w22);
		Coordinate A22 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		
		help1 = countSumProduct(A00, A02);
		help2 = countNorma(help1);
		Coordinate A01 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		help1 = countSumProduct(A10, A12);
		help2 = countNorma(help1);
		Coordinate A11 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		help1 = countSumProduct(A20, A22);
		help2 = countNorma(help1);
		Coordinate A21 = new Coordinate(help1.x/help2, help1.y/help2, help1.z/help2);
		
		
		double l00 = countScalarProduct(D00,w00)/countScalarProduct(w00,w00);
		double l10 = countScalarProduct(D10,w10)/countScalarProduct(w10,w10);
		double l20 = countScalarProduct(D20,w20)/countScalarProduct(w20,w20);
		double l01 = countScalarProduct(D03,w02)/countScalarProduct(w02,w02);
		double l11 = countScalarProduct(D13,w12)/countScalarProduct(w12,w12);
		double l21 = countScalarProduct(D23,w22)/countScalarProduct(w22,w22);
		
		double m00 = countScalarProduct(D00,A00);
		double m10 = countScalarProduct(D10,A10);
		double m20 = countScalarProduct(D20,A20);
		double m01 = countScalarProduct(D03,A02);
		double m11 = countScalarProduct(D13,A12);
		double m21 = countScalarProduct(D23,A22);
			
		Coordinate G01 = new Coordinate ((P004.x + 5*v301.x+2*v302.x)/8 + 2*l00*w01.x/3 + l01*w00.x/3 + 2*m00*A01.x/3 +  m01*A00.x/3,
		                                 (P004.y + 5*v301.y+2*v302.y)/8 + 2*l00*w01.y/3 + l01*w00.y/3 + 2*m00*A01.y/3 +  m01*A00.y/3,
		                                 (P004.z + 5*v301.z+2*v302.z)/8 + 2*l00*w01.z/3 + l01*w00.z/3 + 2*m00*A01.z/3 +  m01*A00.z/3);
		Coordinate G11 = new Coordinate ((P040.x + 5*v311.x+2*v312.x)/8 + 2*l10*w11.x/3 + l11*w10.x/3 + 2*m10*A11.x/3 +  m11*A10.x/3,
                                         (P040.y + 5*v311.y+2*v312.y)/8 + 2*l10*w11.y/3 + l11*w10.y/3 + 2*m10*A11.y/3 +  m11*A10.y/3,
                                         (P040.z + 5*v311.z+2*v312.z)/8 + 2*l10*w11.z/3 + l11*w10.z/3 + 2*m10*A11.z/3 +  m11*A10.z/3);
		Coordinate G21 = new Coordinate ((P400.x + 5*v321.x+2*v322.x)/8 + 2*l20*w21.x/3 + l21*w20.x/3 + 2*m20*A21.x/3 +  m21*A20.x/3,
										 (P400.y + 5*v321.y+2*v322.y)/8 + 2*l20*w21.y/3 + l21*w20.y/3 + 2*m20*A21.y/3 +  m21*A20.y/3,
                                         (P400.z + 5*v321.z+2*v322.z)/8 + 2*l20*w21.z/3 + l21*w20.z/3 + 2*m20*A21.z/3 +  m21*A20.z/3);
		Coordinate G02 = new Coordinate ((2*v301.x + 5*v302.x + P040.x)/8 + l00*w02.x/3 + 2*l01*w01.x/3 + m00*A02.x/3 + 2*m01*A01.x/3,
										 (2*v301.y + 5*v302.y + P040.y)/8 + l00*w02.y/3 + 2*l01*w01.y/3 + m00*A02.y/3 + 2*m01*A01.y/3,
										 (2*v301.z + 5*v302.z + P040.z)/8 + l00*w02.z/3 + 2*l01*w01.z/3 + m00*A02.z/3 + 2*m01*A01.z/3);
		Coordinate G12 = new Coordinate ((2*v311.x + 5*v312.x + P400.x)/8 + l10*w12.x/3 + 2*l11*w11.x/3 + m10*A12.x/3 + 2*m11*A11.x/3,
										 (2*v311.y + 5*v312.y + P400.y)/8 + l10*w12.y/3 + 2*l11*w11.y/3 + m10*A12.y/3 + 2*m11*A11.y/3,
										 (2*v311.z + 5*v312.z + P400.z)/8 + l10*w12.z/3 + 2*l11*w11.z/3 + m10*A12.z/3 + 2*m11*A11.z/3);
		Coordinate G22 = new Coordinate ((2*v321.x + 5*v322.x + P004.x)/8 + l20*w22.x/3 + 2*l21*w21.x/3 + m20*A22.x/3 + 2*m21*A21.x/3,
				 						 (2*v321.y + 5*v322.y + P004.y)/8 + l20*w22.y/3 + 2*l21*w21.y/3 + m20*A22.y/3 + 2*m21*A21.y/3,
				 						 (2*v321.z + 5*v322.z + P004.z)/8 + l20*w22.z/3 + 2*l21*w21.z/3 + m20*A22.z/3 + 2*m21*A21.z/3);
	
		
		
		P112 = new Coordinate(0.5*(G22.x + G01.x),
							  0.5*(G22.y + G01.y),
					  		  0.5*(G22.z + G01.z));
		P121 = new Coordinate(0.5*(G02.x + G11.x),
							  0.5*(G02.y + G11.y),
							  0.5*(G02.z + G11.z));
		P211 = new Coordinate(0.5*(G12.x + G21.x),
							  0.5*(G12.y + G21.y),
							  0.5*(G12.z + G21.z));
	
		
		//	setQuadraticNormals();
		double k = 1D;
//		double u 
/*
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



		System.out.println("normala> "+getNormal(0.3333333D, 0D).toString());
		b210 = new Coordinate(	(2*b300.x + b030.x -  k*countScalarProduct(countDifferenceProduct(b030,b300), getNormal(0.3333333D, 0D))* normalN1.x)/3,
				(2*b300.y + b030.y  -  k*countScalarProduct(countDifferenceProduct(b030,b300), getNormal(0.3333333D, 0D))* normalN1.y)/3,
				(2*b300.z + b030.z -  k*countScalarProduct(countDifferenceProduct(b030,b300),  getNormal(0.3333333D, 0D))* normalN1.z)/3);
		//countDifferenceProduct(b030,b300).toString();
		b120 = new Coordinate(	(2*b030.x + b300.x - k* countScalarProduct(countDifferenceProduct(b300,b030),  getNormal(0.666666D, 0D))*normalN2.x)/3,
								(2*b030.y +  b300.y  -   k*countScalarProduct(countDifferenceProduct(b300,b030), getNormal(0.666666D, 0D))*normalN2.y)/3,
				(2*b030.z + b300.z -   k*countScalarProduct(countDifferenceProduct(b300,b030), getNormal(0.666666D, 0D))*normalN2.z)/3);
	//	System.out.println("b120"+b120.toString());
		b021 = new Coordinate(	(2*b030.x + b003.x -   k*countScalarProduct(countDifferenceProduct(b003,b030), getNormal(0.666666D, 0.3333333D))*normalN2.x)/3,
				(2*b030.y + b003.y -  k*countScalarProduct(countDifferenceProduct(b003,b030), getNormal(0.666666D, 0.3333333D))* normalN2.y)/3,
				(2*b030.z + b003.z -  k*countScalarProduct(countDifferenceProduct(b003,b030), getNormal(0.666666D, 0.3333333D))* normalN2.z)/3);
	//	System.out.println("b021"+b021.toString());
		b012 = new Coordinate(	(2*b003.x + b030.x -k* countScalarProduct(countDifferenceProduct(b030,b003), getNormal(0.33333333D, 0.6666666D))* normalN3.x)/3,
				(2*b003.y + b030.y -  k*countScalarProduct(countDifferenceProduct(b030,b003), getNormal(0.33333333D, 0.6666666D))* normalN3.y)/3,
				(2*b003.z + b030.z -  k*countScalarProduct(countDifferenceProduct(b030,b003), getNormal(0.33333333D, 0.6666666D))* normalN3.z)/3);
		//System.out.println("b012"+b012.toString());
		b102 = new Coordinate(	(2*b003.x + b300.x - k* countScalarProduct(countDifferenceProduct(b300,b003), getNormal(0D, 0.6666666D))*normalN3.x)/3,
				(2*b003.y + b300.y -  k*countScalarProduct(countDifferenceProduct(b300,b003), getNormal(0D, 0.6666666D))*normalN3.y)/3,
				(2*b003.z + b300.z -  k*countScalarProduct(countDifferenceProduct(b300,b003), getNormal(0D, 0.6666666D))*normalN3.z)/3);
		//System.out.println("b102"+b102.toString());
		b201 = new Coordinate( (2*b300.x + b003.x -  k*countScalarProduct(countDifferenceProduct(b003,b300), getNormal(0D, 0.33333333D))*normalN1.x)/3,
				(2*b300.y + b003.y -  k*countScalarProduct(countDifferenceProduct(b003,b300), getNormal(0D, 0.33333333D))*normalN1.y)/3,
				(2*b300.z + b003.z -  k*countScalarProduct(countDifferenceProduct(b003,b300), getNormal(0D, 0.33333333D))*normalN1.z)/3);
		//System.out.println("b201"+b201.toString());



		
		
		System.out.println("normala> "+getNormal(0.3333333D, 0D).toString());
		b210 = new Coordinate(	(2*b300.x + b030.x -  k*countScalarProduct(countDifferenceProduct(b030,b300), getNormal(0.3333333D, 0D)) * getNormal(0.3333333D, 0D).x)/3,
				(2*b300.y + b030.y  -  k*countScalarProduct(countDifferenceProduct(b030,b300),  getNormal(0.3333333D, 0D)) * getNormal(0.3333333D, 0D).y)/3,
				(2*b300.z + b030.z -  k*countScalarProduct(countDifferenceProduct(b030,b300),  getNormal(0.3333333D, 0D)) * getNormal(0.3333333D, 0D).z)/3);
		//countDifferenceProduct(b030,b300).toString();
		b120 = new Coordinate(	(2*b030.x + b300.x - k* countScalarProduct(countDifferenceProduct(b300,b030),  getNormal(0.666666D, 0D)) * getNormal(0.666666D, 0D).x)/3,
				(2*b030.y +  b300.y  -   k*countScalarProduct(countDifferenceProduct(b300,b030), getNormal(0.666666D, 0D))* getNormal(0.666666D, 0D).y)/3,
				(2*b030.z + b300.z -   k*countScalarProduct(countDifferenceProduct(b300,b030), getNormal(0.666666D, 0D)) * getNormal(0.666666D, 0D).z)/3);
	//	System.out.println("b120"+b120.toString());
		b021 = new Coordinate(	(2*b030.x + b003.x -   k*countScalarProduct(countDifferenceProduct(b003,b030), getNormal(0.666666D, 0.3333333D)) * getNormal(0.666666D, 0.3333333D).x)/3,
				(2*b030.y + b003.y -  k*countScalarProduct(countDifferenceProduct(b003,b030), getNormal(0.666666D, 0.3333333D)) * getNormal(0.666666D, 0.3333333D).y)/3,
				(2*b030.z + b003.z -  k*countScalarProduct(countDifferenceProduct(b003,b030), getNormal(0.666666D, 0.3333333D)) * getNormal(0.666666D, 0.3333333D).z)/3);
	//	System.out.println("b021"+b021.toString());
		b012 = new Coordinate(	(2*b003.x + b030.x -k* countScalarProduct(countDifferenceProduct(b030,b003), getNormal(0.33333333D, 0.6666666D))* getNormal(0.33333333D, 0.6666666D).x)/3,
				(2*b003.y + b030.y -  k*countScalarProduct(countDifferenceProduct(b030,b003), getNormal(0.33333333D, 0.6666666D)) * getNormal(0.33333333D, 0.6666666D).y)/3,
				(2*b003.z + b030.z -  k*countScalarProduct(countDifferenceProduct(b030,b003), getNormal(0.33333333D, 0.6666666D)) * getNormal(0.33333333D, 0.6666666D).z)/3);
		//System.out.println("b012"+b012.toString());
		b102 = new Coordinate(	(2*b003.x + b300.x - k* countScalarProduct(countDifferenceProduct(b300,b003), getNormal(0D, 0.6666666D)) * getNormal(0D, 0.6666666D).x)/3,
				(2*b003.y + b300.y -  k*countScalarProduct(countDifferenceProduct(b300,b003), getNormal(0D, 0.6666666D)) * getNormal(0D, 0.6666666D).y)/3,
				(2*b003.z + b300.z -  k*countScalarProduct(countDifferenceProduct(b300,b003), getNormal(0D, 0.6666666D)) * getNormal(0D, 0.6666666D).z)/3);
		//System.out.println("b102"+b102.toString());
		b201 = new Coordinate( (2*b300.x + b003.x -  k*countScalarProduct(countDifferenceProduct(b003,b300), getNormal(0D, 0.33333333D)) * getNormal(0D, 0.33333333D).x)/3,
				(2*b300.y + b003.y -  k*countScalarProduct(countDifferenceProduct(b003,b300), getNormal(0D, 0.33333333D)) * getNormal(0D, 0.33333333D).y)/3,
				(2*b300.z + b003.z -  k*countScalarProduct(countDifferenceProduct(b003,b300), getNormal(0D, 0.33333333D)) * getNormal(0D, 0.33333333D).z)/3);
		//System.out.println("b201"+b201.toString());
		
		System.out.println("normala> "+getNormal(0.3333333D, 0D).toString());
		b210 = new Coordinate(	(2*b300.x + b030.x -  countScalarProduct2D(countDifferenceProduct(b030,b300), normalN1) * getNormal(0.3333333D, 0D).x)/3,
				(2*b300.y + b030.y  -  countScalarProduct2D(countDifferenceProduct(b030,b300),  normalN1) * getNormal(0.3333333D, 0D).y)/3,
				(2*b300.z + b030.z -  countScalarProduct2D(countDifferenceProduct(b030,b300),  normalN1) * getNormal(0.3333333D, 0D).z)/3);
		//countDifferenceProduct(b030,b300).toString();
		b120 = new Coordinate(	(2*b030.x + b300.x -  countScalarProduct2D(countDifferenceProduct(b300,b030),  normalN2) * getNormal(0.666666D, 0D).x)/3,
				(2*b030.y +  b300.y  -   countScalarProduct2D(countDifferenceProduct(b300,b030), normalN2)* getNormal(0.666666D, 0D).y)/3,
				(2*b030.z + b300.z -   countScalarProduct2D(countDifferenceProduct(b300,b030), normalN2) * getNormal(0.666666D, 0D).z)/3);
	//	System.out.println("b120"+b120.toString());
		b021 = new Coordinate(	(2*b030.x + b003.x -   countScalarProduct2D(countDifferenceProduct(b003,b030), normalN2) * getNormal(0.666666D, 0.3333333D).x)/3,
				(2*b030.y + b003.y -  countScalarProduct2D(countDifferenceProduct(b003,b030), normalN2) * getNormal(0.666666D, 0.3333333D).y)/3,
				(2*b030.z + b003.z -  countScalarProduct2D(countDifferenceProduct(b003,b030), normalN2) * getNormal(0.666666D, 0.3333333D).z)/3);
	//	System.out.println("b021"+b021.toString());
		b012 = new Coordinate(	(2*b003.x + b030.x - countScalarProduct2D(countDifferenceProduct(b030,b003), normalN3)* getNormal(0.33333333D, 0.6666666D).x)/3,
				(2*b003.y + b030.y -  countScalarProduct2D(countDifferenceProduct(b030,b003), normalN3) * getNormal(0.33333333D, 0.6666666D).y)/3,
				(2*b003.z + b030.z -  countScalarProduct2D(countDifferenceProduct(b030,b003), normalN3) * getNormal(0.33333333D, 0.6666666D).z)/3);
		//System.out.println("b012"+b012.toString());
		b102 = new Coordinate(	(2*b003.x + b300.x -  countScalarProduct2D(countDifferenceProduct(b300,b003), normalN3) * getNormal(0D, 0.6666666D).x)/3,
				(2*b003.y + b300.y -  countScalarProduct2D(countDifferenceProduct(b300,b003), normalN3) * getNormal(0D, 0.6666666D).y)/3,
				(2*b003.z + b300.z -  countScalarProduct2D(countDifferenceProduct(b300,b003), normalN3) * getNormal(0D, 0.6666666D).z)/3);
		//System.out.println("b102"+b102.toString());
		b201 = new Coordinate( (2*b300.x + b003.x -  countScalarProduct2D(countDifferenceProduct(b003,b300), normalN1) * getNormal(0D, 0.33333333D).x)/3,
				(2*b300.y + b003.y -  countScalarProduct2D(countDifferenceProduct(b003,b300), normalN1) * getNormal(0D, 0.33333333D).y)/3,
				(2*b300.z + b003.z -  countScalarProduct2D(countDifferenceProduct(b003,b300), normalN1) * getNormal(0D, 0.33333333D).z)/3);
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
		
		Coordinate helpE = new Coordinate((b210.x+b120.x+b021.x+b012.x+b102.x+b201.x)/6,
									      (b210.y+b120.y+b021.y+b012.y+b102.y+b201.y)/6,
									      (b210.z+b120.z+b021.z+b012.z+b102.z+b201.z)/6);
		Coordinate helpV = new Coordinate((b300.x+b030.x+b003.x)/3,
									      (b300.y+b030.y+b003.y)/3,
									      (b300.z+b030.z+b003.z)/3);
		b111 = new Coordinate( (helpE.x + (helpE.x-helpV.x)/2)*k,
				 			(helpE.y + (helpE.y-helpV.y)/2)*k,
				 			((helpE.z + (helpE.z-helpV.z)/2))*k);
		*/
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
/*		double x = b300.x*w + b030.x*u + b003.x*v;

		double y = b300.y*w + b030.y*u + b003.y*v;
*/
		double x = P400.x*Math.pow(u,4) + P040.x*Math.pow(v, 4) + P004.x*Math.pow(w, 4)+
				   4*P310.x*Math.pow(u, 3)*v + 4*P130.x*Math.pow(v, 3)*u + 4*P031.x*Math.pow(v, 3)*w + 4*P013.x*Math.pow(w, 3)*v + 4*P301.x*Math.pow(u, 3)*w + 4*P103.x*Math.pow(w, 3)*u +
				   6*P220.x*Math.pow(u, 2)*Math.pow(v, 2) + 6*P202.x*Math.pow(u, 2)*Math.pow(w, 2) + 6*P022.x*Math.pow(w, 2)*Math.pow(v, 2) + 
				   12*P112.x*Math.pow(w, 2)*u*v + 12*P121.x*Math.pow(v, 2)*u*w + 12*P211.x*Math.pow(u, 2)*w*v;
			

		double y = P400.y*Math.pow(u,4) + P040.y*Math.pow(v, 4) + P004.y*Math.pow(w, 4)+
				   4*P310.y*Math.pow(u, 3)*v + 4*P130.y*Math.pow(v, 3)*u + 4*P031.y*Math.pow(v, 3)*w + 4*P013.y*Math.pow(w, 3)*v + 4*P301.y*Math.pow(u, 3)*w + 4*P103.y*Math.pow(w, 3)*u +
				   6*P220.y*Math.pow(u, 2)*Math.pow(v, 2) + 6*P202.y*Math.pow(u, 2)*Math.pow(w, 2) + 6*P022.y*Math.pow(w, 2)*Math.pow(v, 2) + 
		   	      12*P112.y*Math.pow(w, 2)*u*v + 12*P121.y*Math.pow(v, 2)*u*w + 12*P211.y*Math.pow(u, 2)*w*v;
		
		double z = P400.z*Math.pow(u,4) + P040.z*Math.pow(v, 4) + P004.z*Math.pow(w, 4)+
		   4*P310.z*Math.pow(u, 3)*v + 4*P130.z*Math.pow(v, 3)*u + 4*P031.z*Math.pow(v, 3)*w + 4*P013.z*Math.pow(w, 3)*v + 4*P301.z*Math.pow(u, 3)*w + 4*P103.z*Math.pow(w, 3)*u +
		   6*P220.z*Math.pow(u, 2)*Math.pow(v, 2) + 6*P202.z*Math.pow(u, 2)*Math.pow(w, 2) + 6*P022.z*Math.pow(w, 2)*Math.pow(v, 2) + 
		   12*P112.z*Math.pow(w, 2)*u*v + 12*P121.z*Math.pow(v, 2)*u*w + 12*P211.z*Math.pow(u, 2)*w*v;
		
		return new Coordinate(x,y,z);
	}
	
	/******************************************************************
	 * The method to print Bezier triangle to console 
	 */
	protected void toStringa(){
		System.out.println("======================================");
		System.out.println(P004.toString());
		System.out.println(P040.toString());
		System.out.println(P400.toString());
		System.out.println("Normals:");
		if (normalN1 != null){
			System.out.println(normalN0.toString());
			System.out.println(normalN1.toString());
			System.out.println(normalN2.toString());
			System.out.println("Koeficients");
			System.out.println(P013);
			System.out.println(P022);
			System.out.println(P031);
			System.out.println(P130);
			System.out.println(P220);
			System.out.println(P310);
			System.out.println(P301);
			System.out.println(P202);
			System.out.println(P103);
			System.out.println(P112);
			System.out.println(P121);
			System.out.println(P211);
			System.out.println("Neigbour 1");
			if (neighbour[0]!=null){
				System.out.println(neighbour[0][0]);
				System.out.println(neighbour[0][1]);
				System.out.println(neighbour[0][2]);
			}
			System.out.println("Neigbour 2");
			if (neighbour[1]!=null){
				System.out.println(neighbour[1][0]);
				System.out.println(neighbour[1][1]);
				System.out.println(neighbour[1][2]);
			}
			System.out.println("Neigbour 1");
			if (neighbour[2]!=null){
				System.out.println(neighbour[2][0]);
				System.out.println(neighbour[2][1]);
				System.out.println(neighbour[2][2]);
			}
		}	
		System.out.println("======================================");
	}
	
	/******************************************************************
	 * The method which compare points
	 * @param P - points for comparing
	 * @return index A,B,C which point is same or N if point P not exist in triangle
	 */
	public char compareReturnIndex(Coordinate P){
		if (P.equals2D(P004))
			return 'A';
		if (P.equals2D(P040))
			return 'B';
		if (P.equals2D(P400))
			return 'C';
		return 'N';
		
	}
	
	/************************************************************************
	 * Protected method for getting envelope of triangle
	 * @return envelope of triangle
	 */
	public Envelope getEnvelope() {
		Coordinate[] newPoint = new Coordinate[4];
		newPoint[0] = P400;
		newPoint[1] = P040;
		newPoint[2] = P004;
		newPoint[3] = P400;
		CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(
				newPoint);

		LinearRing trianglesPoints = new LinearRing(newPointsTriangle,
				new GeometryFactory());

		return trianglesPoints.getEnvelopeInternal();
	}
	
	public boolean compare(Bezier3 T) {
		if ((T.P400.equals2D(P400) || T.P400.equals2D(P040) || T.P400.equals2D(P004))
				&& (T.P040.equals2D(P400) || T.P040.equals2D(P040) || T.P040.equals2D(P004))
				&& (T.P004.equals2D(P400) || T.P004.equals2D(P040) || T.P004.equals2D(P004))) {

			return true;
		}

		return false;
	}
	
	public boolean containTwoPoints(Coordinate P1, Coordinate P2) {
		if ((P004.equals2D(P1) || P040.equals2D(P1) || P400.equals2D(P1))
				&& (P004.equals2D(P2) || P040.equals2D(P2) || P400.equals2D(P2)))
			return true;
		return false;
	}

	

}

