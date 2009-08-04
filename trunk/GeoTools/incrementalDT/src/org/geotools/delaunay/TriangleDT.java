package org.geotools.delaunay;

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

import java.awt.geom.GeneralPath;
import java.io.Serializable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.geotools.index.Data;

public class TriangleDT implements Serializable {
	public PointDT A;
	public PointDT B;
	public PointDT C;
	public double[] key;
	public double[][] neighbour_idx = new double [3][2];
	public boolean haveBreakLine = false;
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param T - triangle will be cloned
	 * 
	 */

	public TriangleDT(TriangleDT T) {
		A = T.A;
		B = T.B;
		C = T.C;
		setKey();
		for (int i = 0; i<3; i++)
			for (int j = 0; j<2; j++)
				neighbour_idx[i][j] = T.neighbour_idx[i][j];
	}
	
	/**************************************************************************
	 * Constructor
	 * @param data - basic seriaziable type
	 */
	protected TriangleDT(double[] data){
		A = new PointDT(data[0],data[1],data[2]);
		B = new PointDT(data[3],data[4],data[5]);
		C = new PointDT(data[6],data[7],data[8]);
		key = new double[2];
		key[0] = data[9];
		key[1] = data[10];
		neighbour_idx = new double[3][2];
		neighbour_idx[0][0] = data[11];
		neighbour_idx[0][1] = data[12];
		neighbour_idx[1][0] = data[13];
		neighbour_idx[1][1] = data[14];
		neighbour_idx[2][0] = data[15]; 
		neighbour_idx[2][1] = data[16];
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param  A - first vertex
	 * @param  B - second vertex
	 * @param  C - third vertex
	 * 
	 */

	public TriangleDT(PointDT A, PointDT B, PointDT C) {
		this.A = A;
		this.B = B;
		this.C = C;
		setKey();
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param  A - first vertex
	 * @param  B - second vertex
	 * @param  C - third vertex
	 * 
	 */

	public TriangleDT(Coordinate A, Coordinate B, Coordinate C) {
		this.A = new PointDT(A.x, A.y, A.z);
		this.B = new PointDT(B.x, B.y, B.z);
		this.C = new PointDT(C.x, C.y, C.z);
		setKey();
	}


	/***************************************************************************
	 * implicit Constructor
	 */

	public TriangleDT() {
	}

	/***************************************************************************
	 * The method for setting key of triangle
	 * 
	 */

	private void setKey() {
		key = new double[2];
		key[0] = (A.x + B.x + C.x) / 3;
		key[1] = (A.y + B.y + C.y) / 3;
	}

	/***************************************************************************
	 * The method which testing, if the line intersect the triangle
	 * 
	 * @param newL - Geometry of line
	 * 
	 * @return boolean true - line intersect triangle false - line doesn't
	 *         intersect triangle
	 */

	public boolean containsLine(LineString newL) {

		Coordinate[] newPoints = { A, B, C, A };
		CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(
				newPoints);
		LinearRing trianglesPoints = new LinearRing(newPointsTriangle,
				new GeometryFactory());

		return newL.crosses(trianglesPoints.convexHull());
	}

	/***************************************************************************
	 * The method which testing, if the triangle contains the point
	 * 
	 * @param P - point which will be tested
	 * 
	 * @return boolean true - the triangle contains the point false - the
	 *         triangle doesn't contains point
	 * 
	 */

	public boolean contains(PointDT P) {
		GeneralPath triangle = new GeneralPath();
		
		triangle.moveTo((float) A.x, (float) A.y);
		triangle.lineTo((float) B.x, (float) B.y);
		triangle.lineTo((float) C.x, (float) C.y);
		triangle.lineTo((float) A.x, (float) A.y);
		triangle.closePath();
		//System.out.println("Je uvnitr + " +triangle.contains(P.x, P.y));
		return triangle.contains(P.x, P.y);
	}

	/***************************************************************************
	 * The method which testing, if the triangle contains the point
	 * 
	 * @param P - point which will be tested
	 * 
	 * @return boolean true - the triangle contains the point false - the
	 *         triangle doesn't contains point
	 * 
	 */

	public boolean containsPointAsVertex(PointDT P) {
		if (A.compare(P) || B.compare(P) || C.compare(P))
			return true;
		else
			return false;

	}

	/***************************************************************************
	 * The method which testing two triangles, if the triangles have one same
	 * point
	 * 
	 * @param T - triangle to test
	 * 
	 * @return boolean true - the triangles have one same point false - the
	 *         triangles haven't one same point
	 * 
	 */

	protected boolean containsOneSamePointWith(TriangleDT T) {
		if (T.A.compare(A) || T.A.compare(B) || T.A.compare(C))
			return true;
		if (T.B.compare(A) || T.B.compare(B) || T.B.compare(C))
			return true;
		if (T.C.compare(A) || T.C.compare(B) || T.C.compare(C))
			return true;
		else
			return false;

	}

	/***************************************************************************
	 * The method which testing two triangles, if the triangles have two same
	 * points
	 * 
	 * @param P1 - point for testing
	 * @param P2 - point for testing
	 * @return boolean true - the triangles have two same point false - the
	 *         triangles haven't two same point
	 * 
	 */

	public boolean containsTwoPoints(PointDT P1, PointDT P2) {
		if ((A.compare(P1) || B.compare(P1) || C.compare(P1))
				&& (A.compare(P2) || B.compare(P2) || C.compare(P2)))
			return true;
		return false;
	}

	/***************************************************************************
	 * The method for converting to String
	 * 
	 * 
	 */

	public void toStringa() {
		//return ("TriangleDT: " + A.toString() + B.toString() + C.toString()+" KEY "+key[0]+" "+key[1]+"  neighbour>"+neighbour_idx[0][0]+ " ");
		System.out.println("--------------------------------------------------------------------");
		  System.out.println("TDT: " + A.toString() + B.toString() + C.toString());
		  System.out.println(" k:"+key[0]+" "+key[1]);
		 System.out.println(); 
		 System.out.println(" Soused0 "+neighbour_idx[0][0]+"  "+neighbour_idx[0][1]);
		 System.out.println(" Soused0 "+neighbour_idx[1][0]+"  "+neighbour_idx[1][1]);
		 System.out.println(" Soused0 "+neighbour_idx[2][0]+"  "+neighbour_idx[2][1]);
		 System.out.println();
		System.out.println("--------------------------------------------------------------------"); 
	}	 
	/***************************************************************************
	 * The method which testing triangle, if the triangles is'nt line
	 * 
	 * @return boolean true - the triangle is triangle false - the triangle is
	 *         line
	 * 
	 */

	protected boolean isTriangle() {
		Coordinate[] newPoint = new Coordinate[4];
		newPoint[0] = A;
		newPoint[1] = B;
		newPoint[2] = C;
		newPoint[3] = A;
		CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(
				newPoint);

		LinearRing trianglesPoints = new LinearRing(newPointsTriangle,
				new GeometryFactory());

		if (trianglesPoints.convexHull().getGeometryType() == "Polygon")
			return true;
		else
			return false;
	}

	/***************************************************************************
	 * The method which comparing two triangles, if the triangles have same
	 * coordinates of vertexes
	 * 
	 * @param T -  triangle to test
	 * 
	 * @return boolean true - the triangles are same false - the triangles
	 *         aren't same
	 * 
	 */

	public boolean compare(TriangleDT T) {
		if ((T.A.compare(A) || T.A.compare(B) || T.A.compare(C))
				&& (T.B.compare(A) || T.B.compare(B) || T.B.compare(C))
				&& (T.C.compare(A) || T.C.compare(B) || T.C.compare(C))) {

			return true;
		}

		return false;
	}
	
	/******************************************************************
	 * The method which compare points
	 * @param P - points for comparing
	 * @return index A,B,C which point is same or N if point P not exist in triangle
	 */
	public char compareReturnIndex(PointDT P){
		if (P.compare(A))
			return 'A';
		if (P.compare(B))
			return 'B';
		if (P.compare(C))
			return 'C';
		return 'N';
		
	}
	
	/***********************************************************************
	 * Protected method for converting object triangle to basic data type
	 * @return - array of double
	 */
	protected double[] triangleToBasicType(){
		double []src = new double[17];
		src[0] = A.x;
		src[1] = A.y;
		src[2] = A.z;
		src[3] = B.x;
		src[4] = B.y;
		src[5] = B.z;
		src[6] = C.x;
		src[7] = C.y;
		src[8] = C.z;
		src[9] = key[0];
		src[10] = key[1];
		src[11] = neighbour_idx[0][0];
		src[12] = neighbour_idx[0][1];
		src[13] = neighbour_idx[1][0];
		src[14] = neighbour_idx[1][1];
		src[15] = neighbour_idx[2][0];
		src[16] = neighbour_idx[2][1];
		return src;
	}
	
	/************************************************************************
	 * Protected method for setting envelope of triangle
	 * @return envelope of triangle
	 */
	public Envelope setEnvelope() {
		Coordinate[] newPoint = new Coordinate[4];
		newPoint[0] = A;
		newPoint[1] = B;
		newPoint[2] = C;
		newPoint[3] = A;
		CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(
				newPoint);

		LinearRing trianglesPoints = new LinearRing(newPointsTriangle,
				new GeometryFactory());

		return trianglesPoints.getEnvelopeInternal();
	}

}
