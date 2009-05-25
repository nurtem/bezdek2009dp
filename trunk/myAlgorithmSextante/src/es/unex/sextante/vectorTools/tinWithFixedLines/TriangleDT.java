package es.unex.sextante.vectorTools.tinWithFixedLines;

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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class TriangleDT implements Serializable {
	public Coordinate A;
	public Coordinate B;
	public Coordinate C;
//	public double[] key;
//	public double[][] neighbour_idx = new double [3][2];
	public boolean haveBreakLine = false;
	public int typeBreakLine = -1;
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
		this.A = A;
		this.B = B;
		this.C = C;
	//	setKey();
	}

	public TriangleDT(Coordinate[] coords) {
		this.A = new Coordinate(coords[0].x, coords[0].y, coords[0].z);
		this.B = new Coordinate(coords[1].x, coords[1].y, coords[1].z);
		this.C = new Coordinate(coords[2].x, coords[2].y, coords[2].z);
	//	setKey();
	}


	/***************************************************************************
	 * implicit Constructor
	 */

	public TriangleDT() {
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
	
	protected Coordinate getCentroid() {
		return new Coordinate((A.x + B.x + C.x) / 3, (A.y + B.y + C.y) / 3);
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

	public boolean contains(Coordinate P) {
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

	public boolean containsPointAsVertex(Coordinate P) {
		if (A.equals2D(P) || B.equals2D(P) || C.equals2D(P))
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
		if (T.A.equals2D(A) || T.A.equals2D(B) || T.A.equals2D(C))
			return true;
		if (T.B.equals2D(A) || T.B.equals2D(B) || T.B.equals2D(C))
			return true;
		if (T.C.equals2D(A) || T.C.equals2D(B) || T.C.equals2D(C))
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

	public boolean containsTwoPoints(Coordinate P1, Coordinate P2) {
		if ((A.equals2D(P1) || B.equals2D(P1) || C.equals2D(P1))
				&& (A.equals2D(P2) || B.equals2D(P2) || C.equals2D(P2)))
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
		  System.out.println(" k:"+typeBreakLine);
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
		if ((T.A.equals2D(A) || T.A.equals2D(B) || T.A.equals2D(C))
				&& (T.B.equals2D(A) || T.B.equals2D(B) || T.B.equals2D(C))
				&& (T.C.equals2D(A) || T.C.equals2D(B) || T.C.equals2D(C))) {

			return true;
		}

		return false;
	}
	
	/******************************************************************
	 * The method which compare points
	 * @param P - points for comparing
	 * @return index A,B,C which point is same or N if point P not exist in triangle
	 */
	public char compareReturnIndex(Coordinate P){
		if (P.equals2D(A))
			return 'A';
		if (P.equals2D(B))
			return 'B';
		if (P.equals2D(C))
			return 'C';
		return 'N';
		
	}
	

	/************************************************************************
	 * Protected method for getting envelope of triangle
	 * @return envelope of triangle
	 */
	public Envelope getEnvelope() {
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
	
	public void normalizePolygon(){
		Coordinate[] coords = new Coordinate[4];
		GeometryFactory gf = new GeometryFactory();
		coords[0] = A;
		coords[1] = B;
		coords[2] = C;
		coords[3] = A;
		LinearRing ring = gf.createLinearRing(coords);
		Polygon poly = gf.createPolygon(ring, null);
		poly.normalize();
		coords = poly.getCoordinates();
		A = coords[0];
		B = coords[1];
		C = coords[2];
	}

}
