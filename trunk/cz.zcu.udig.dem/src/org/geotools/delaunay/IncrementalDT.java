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

import java.util.Collection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Envelope;

import java.util.Iterator;
import java.util.LinkedList;

import org.geotools.index.Data;

public class IncrementalDT {
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param dataStore -  type of DelaunayDataStore for saving trinagles
	 * 
	 */
	
	public IncrementalDT (DelaunayDataStore dataStore){
		this.path = path;
		triangles = dataStore;
	}

	/***************************************************************************
	 * Private class for sorting triangles
	 */
	
	private class TestingTriangles {
		TriangleDT trian;
		TestingTriangles next;

		TestingTriangles() {
		}

		TestingTriangles(TriangleDT A) {
			this.trian = A;
		}
	}
	// points which generate Convex geometry
	private LinearRing newConvexGeometry;
	// if new points is already used in triangulation 
	private boolean duplicitePoint; 
	// coordinate of convex points
	private Coordinate[] convex; 
	// path for creating data file for saving triangles
	private String path;
	// 
	public DelaunayDataStore triangles; 
	// number of points in triagulation
	private int number_Points = 0; 
	public int number_Triangles = 0;
	// list of first trhee points which waint on first triangles
	private LinkedList firstPointsXY = new LinkedList();
	private boolean firstTriangle = false;

	/***************************************************************************
	 * The method for imputing a new points to triangulation
	 * 
	 * @param point - Coordinate of point
	 * 
	 */

	public void insertPoint(Coordinate point) {
		PointDT P = new PointDT(point);
		duplicitePoint = false;
		number_Points++;
		// test for existing first triangle
		if (!firstTriangle) { 
			Iterator it = firstPointsXY.iterator();
			while (it.hasNext())
				// test of duplicity
				if (P.compare((PointDT) it.next()))
					duplicitePoint = true;
			if (!duplicitePoint)
				firstPointsXY.addFirst(P);
			else
				number_Points--;

			if (number_Points >= 3) {
				// creating first triangle
				createFirstTriangle(); 
			}
		} else { // first triangles exist
			Coordinate[] newPoint = { P };
			CoordinateArraySequence newPoints = new CoordinateArraySequence(
					newPoint);
			Point newP = new Point(newPoints, new GeometryFactory());
			// inside point
			if (newP.coveredBy(newConvexGeometry.convexHull())) { 																
				divideTriangle(P, newConvexGeometry.contains(newP));
				 // point is on the edge of convex
				if (newConvexGeometry.contains(newP) && !duplicitePoint) 
					convex = addPointToConvexGeometry(newP, convex);
			} else
				// point is out of convex
				createConvexPolygon(P); 
		}
	}

	/***************************************************************************
	 * The method for creating a first triangle
	 * 
	 * @param Coordinate[]
	 *            array of first three point's coordinate
	 * 
	 */

	private void createFirstTriangle() {
		Iterator it = firstPointsXY.iterator();
		TriangleDT T = new TriangleDT((PointDT) it.next(), (PointDT) it.next(),
				(PointDT) it.next());

		if (T.isTriangle()) {
			try { 
				triangles.insertToTree(T, T.key);
			} catch (Exception e) {
				e.printStackTrace();
			}
			number_Triangles++;
			convex = new Coordinate[4];
			convex[0] = T.A;
			convex[1] = T.B;
			convex[2] = T.C;
			convex[3] = T.A;
			//creating geometry of convex
			CoordinateArraySequence pointsWhichGenerateConvex = new CoordinateArraySequence(
					convex);
			newConvexGeometry = new LinearRing(pointsWhichGenerateConvex,
					new GeometryFactory());
			firstTriangle = true;

			// completion of first points which were in line
			if (firstPointsXY.size() > 3) { 
				it = firstPointsXY.iterator();
				it.next();
				it.next();
				it.next();

				while (it.hasNext()) {
					insertPoint((PointDT) it.next());
				}
			}
		}
	}

	/***************************************************************************
	 * The method for searching a tringle which contains new point P, inside
	 * convex hull
	 * 
	 * @param PointDT -
	 *            coordinate of new point
	 * 
	 * @return TriangleDT - founded triangle which contains new point P
	 * 
	 */

	private TriangleDT ringSearch(PointDT P) {

		LinkedList tested = new LinkedList();
		double[] A = { P.x, P.y };
		TriangleDT T = null;
		try {
			T = (TriangleDT) triangles.searchNearest(A);
		} catch (Exception e) {
			e.printStackTrace();
		}
		TriangleDT TT;
		int i;

		if (T.contains(P)) {
			return T;
		}
		// Rtree returns bad triangle
		tested.add(T);
		for (i = 0; i < 3; i++) {
			// testing neigbour
			TriangleDT Ti = triangles.getTriangle(T.neighbour_idx[i]);
			if (Ti != null) {
				if (Ti.contains(P))
					return Ti;
				tested.add(Ti);
				TriangleDT left = null;
				TriangleDT right = null;

				for (int j = 0; j < 3; j++) {
					// testing neighbour's neighbour
					TriangleDT Tij = triangles.getTriangle(Ti.neighbour_idx[j]);
					if (Tij != null
							&& !Tij.compare(T)) {
						if (left == null) {
							left = Tij;
							if (left.contains(P))
								return left;
							tested.add(left);
						} else {
							right = Tij;
							if (right.contains(P))
								return right;
							tested.add(right);
						}
					}
				}
				if (left != null) {
					TT = inSideSearch(T, i, left, P, tested);
					if (TT != null)
						return TT;
				}
				if (right != null) {
					TT = inSideSearch(T, i, right, P, tested);
					if (TT != null)
						return TT;
				}
			}

		}
		// if triangle isn't founded,program continues with preoder test
		return triangles.preorderTest(P);

	}
	


	/***************************************************************************
	 * The private method for searching around founded triangle
	 * 
	 * @param TriangleDT
	 *            T - founded triangle
	 * @param int
	 *            i - index in array of neighbour
	 * @param TriangleDT
	 *            left - neighbour of triangle T
	 * @param PointDT
	 *            P - new point
	 * @param LinkedList
	 *            tested - list of tested triangle
	 * 
	 * @return TriangleDT - founded triangle which contains new point P
	 * 
	 */

	private TriangleDT inSideSearch(TriangleDT T, int i, TriangleDT left,
			PointDT P, LinkedList tested) {

		TriangleDT helpTriangle = new TriangleDT();
		helpTriangle = triangles.getTriangle(T.neighbour_idx[i]);
		boolean change = true;
		while (change) {
			change = false;
			for (int j = 0; j < 3; j++) {
				TriangleDT Lj = triangles.getTriangle(left.neighbour_idx[j]);
				if ((Lj != null)
						&& Lj.contains(P))
					return Lj;

				if (Lj != null
						&& !wasTested(Lj, tested)
						&& Lj != helpTriangle
						&& Lj.containsOneSamePointWith(T)) {

					tested.add(Lj);
					helpTriangle = left;
					left = Lj;
					change = true;
				}
			}
		}
		return null;
	}

	/***************************************************************************
	 * The private method for control, if the triangle was tested
	 * 
	 * @param TriangleDT
	 *            T - triangle for testing
	 * @param LinkedList
	 *            tested - list of tested triangle
	 * 
	 * @return boolean true - the triangle was tested false - the triangle
	 *         wasn't tested
	 */

	private boolean wasTested(TriangleDT T, LinkedList tested) {
		Iterator iter = tested.iterator();
		while (iter.hasNext()) {
			if (((TriangleDT) iter.next()).compare(T))
				return true;
		}
		return false;
	}

	/***************************************************************************
	 * The method for searching a trinagle contains new point, which isn't
	 * inside convex hull
	 * 
	 * @param PointDT
	 *            A - first point on edge of convex hull PointDT B - second
	 *            point on edge of convex hull
	 * 
	 * @return TriangleDT - which contains PointDT A and PointDT B
	 */

	private TriangleDT ringSearchConvexTriangle(PointDT A, PointDT B) {
		PointDT P = new PointDT((A.x + B.x) / 2, (A.y + B.y) / 2, 0);
		double[] X = { P.x, P.y };
		LinkedList tested = new LinkedList();

		TriangleDT T = null;
		try {
			T = triangles.searchNearest(X);
		} catch (Exception e) {
			e.printStackTrace();
		}
		TriangleDT TT;
		int i;
		if (T.containsTwoPoints(A, B))
			return T;
		tested.add(T);
		for (i = 0; i < 3; i++) {
			TriangleDT Ti = triangles.getTriangle(T.neighbour_idx[i]);
			if (Ti != null) {
				if (Ti.containsTwoPoints(A, B))
					return Ti;
				tested.add(Ti);
				TriangleDT left = null;
				TriangleDT right = null;

				for (int j = 0; j < 3; j++) {
					TriangleDT Tij = triangles.getTriangle(Ti.neighbour_idx[j]);
					if (Tij != null
							&& !Tij.compare(T)) {
						if (left == null) {
							left = Tij;
							if (left.containsTwoPoints(A, B))
								return left;
							tested.add(left);
						} else {
							right = Tij;
							if (right.containsTwoPoints(A, B))
								return right;
							tested.add(right);
						}
					}
				}
				if (left != null) {
					TT = inSideConvexSearch(T, i, left, A, B, tested);
					if (TT != null)
						return TT;
				}
				if (right != null) {
					TT = inSideConvexSearch(T, i, right, A, B, tested);
					if (TT != null)
						return TT;
				}
			}

		}
		return triangles.preorderTestContainTwoPoint(A,B);

	}

	/***************************************************************************
	 * The private method for searching around founded triangle
	 * 
	 * @param TriangleDT
	 *            T - founded triangle
	 * @param int
	 *            i - index in array of neighbour
	 * @param TriangleDT
	 *            left - neighbour of triangle T
	 * @param PointDT
	 *            A - the point, which must be vertex
	 * @param PointDT
	 *            B - the point, which must be vertex
	 * @param LinkedList
	 *            tested - list of tested triangle
	 * 
	 * @return TriangleDT - founded triangle which contains new point P
	 * 
	 */

	private TriangleDT inSideConvexSearch(TriangleDT T, int i, TriangleDT left,
			PointDT A, PointDT B, LinkedList tested) {
		TriangleDT helpTriangle = new TriangleDT();
		helpTriangle = triangles.getTriangle(T.neighbour_idx[i]);
		boolean change = true;
		while (change) {
			change = false;
			for (int j = 0; j < 3; j++) {
				TriangleDT Lj = triangles.getTriangle(left.neighbour_idx[j]);
				if ((Lj != null)
						&& Lj.containsTwoPoints(A, B))
					return Lj;
				if (Lj != null
						&& !wasTested(Lj, tested)
						&& !Lj.compare(helpTriangle)
						&& Lj.containsOneSamePointWith(T)) {

					tested.add(Lj);
					helpTriangle = left;
					left = Lj;
					change = true;
				}
			}
		}
		return null;
	}

	/***************************************************************************
	 * The method for dividing a trinagle which contains new point
	 * 
	 * @param PointDT
	 *            A - new point boolean pointIsInConvex - true - if point is on
	 *            edge of the TriangleDT - false - if point is inside the
	 *            TriangleDT
	 */

	private void divideTriangle(PointDT A, boolean pointIsInConvex) {
		TriangleDT T = ringSearch(A);
		if ((T==null) || (T.containsPointAsVertex(A)))// testing of duplicity
			duplicitePoint = true;
		if ((!duplicitePoint)) { 
			TestingTriangles newTriangles = new TestingTriangles();
			//creating new three triangles
			newTriangles.trian = new TriangleDT(T.A, T.B, A); 
																
			newTriangles.next = new TestingTriangles();
			newTriangles.next.trian = new TriangleDT(T.B, T.C, A);
			newTriangles.next.next = new TestingTriangles();
			newTriangles.next.next.trian = new TriangleDT(T.A, A, T.C);
			// new point is inside of triangle
			if (!pointIsInConvex) {
				saveTriangleNeighbour(newTriangles.trian,
						newTriangles.next.trian);
				saveTriangleNeighbour(newTriangles.next.trian,
						newTriangles.next.next.trian); 
				saveTriangleNeighbour(newTriangles.trian,
						newTriangles.next.next.trian);
			
				TriangleDT T0 = triangles.getTriangle(T.neighbour_idx[0]);
				
				// saving neighbours of deleted triangle
				if (T0 != null){
					saveTriangleNeighbour(T0, newTriangles.trian);
					triangles.delete(T0.key);
					triangles.insertToTree(T0, T0.key);
				}
				TriangleDT T1 = triangles.getTriangle(T.neighbour_idx[1]);
				if (T1 != null){
					saveTriangleNeighbour(T1,
							newTriangles.next.trian);
					triangles.delete(T1.key);
					triangles.insertToTree(T1, T1.key);
				}
				TriangleDT T2 = triangles.getTriangle(T.neighbour_idx[2]);
				if (T2 != null){
					saveTriangleNeighbour(T2,
							newTriangles.next.next.trian);
					triangles.delete(T2.key);
					triangles.insertToTree(T2, T2.key);
				}
				if (!newTriangles.next.trian.isTriangle()){
					TriangleDT Thelp = newTriangles.next.trian;
					newTriangles.next.trian = newTriangles.trian;
					newTriangles.trian = Thelp;
				}if (!newTriangles.next.next.trian.isTriangle()){
					TriangleDT Thelp = newTriangles.next.next.trian;
					newTriangles.next.next.trian = newTriangles.trian;
					newTriangles.trian = Thelp;
				}
				try {
					triangles.delete(T.key);
					triangles.insertToTree(newTriangles.trian,
							newTriangles.trian.key);
					triangles.insertToTree(newTriangles.next.trian,
							newTriangles.next.trian.key);
					triangles.insertToTree(newTriangles.next.next.trian,
							newTriangles.next.next.trian.key);
				} catch (Exception e) {
					e.printStackTrace();
				}

				number_Triangles += 2;
			testNewTriangles(newTriangles, newTriangles.next.next);
			} else { 
				if (!newTriangles.trian.isTriangle())
					newTriangles = newTriangles.next;
				if (!newTriangles.next.trian.isTriangle())
					newTriangles.next = newTriangles.next.next;
				saveTriangleNeighbour(newTriangles.trian,
						newTriangles.next.trian);
				for (int i = 0; i < 3; i++){
					TriangleDT Ti = triangles.getTriangle(T.neighbour_idx[i]);
					if (Ti != null) {
						saveTriangleNeighbour(Ti,
								newTriangles.next.trian);
						saveTriangleNeighbour(Ti,
								newTriangles.trian);
						triangles.delete(Ti.key);
						triangles.insertToTree(Ti, Ti.key);
					}
				}	
				try {
					triangles.delete(T.key);
					triangles.insertToTree(newTriangles.trian,
							newTriangles.trian.key);
					triangles.insertToTree(newTriangles.next.trian,
							newTriangles.next.trian.key);
				} catch (Exception e) {
					e.printStackTrace();
				}
				number_Triangles++;
				testNewTriangles(newTriangles, newTriangles.next);
			}
		}
	}

	/***************************************************************************
	 * The method for saving triangle's neighbours between two triangles
	 * 
	 * @param TriangleDT
	 *            T - first triangle
	 * @param TriangleDT
	 *            TT - second triangle
	 */

	private void saveTriangleNeighbour(TriangleDT T, TriangleDT TT) {
		if ((T.A.compare(TT.A) && T.B.compare(TT.B))
				|| (T.A.compare(TT.B) && T.B.compare(TT.A))) {
			T.neighbour_idx[0] = TT.key;
			TT.neighbour_idx[0] = T.key;
		} else if ((T.A.compare(TT.A) && T.B.compare(TT.C))
				|| (T.A.compare(TT.C) && T.B.compare(TT.A))) {
			T.neighbour_idx[0] = TT.key;
			TT.neighbour_idx[2] = T.key;
		} else if ((T.A.compare(TT.B) && T.B.compare(TT.C))
				|| (T.A.compare(TT.C) && T.B.compare(TT.B))) {
			T.neighbour_idx[0] = TT.key;
			TT.neighbour_idx[1] = T.key;
		} else if ((T.A.compare(TT.A) && T.C.compare(TT.B))
				|| (T.A.compare(TT.B) && T.C.compare(TT.A))) {
			T.neighbour_idx[2] = TT.key;
			TT.neighbour_idx[0] = T.key;
		} else if ((T.A.compare(TT.A) && T.C.compare(TT.C))
				|| (T.A.compare(TT.C) && T.C.compare(TT.A))) {
			T.neighbour_idx[2] = TT.key;
			TT.neighbour_idx[2] = T.key;
		} else if ((T.A.compare(TT.B) && T.C.compare(TT.C))
				|| (T.A.compare(TT.C) && T.C.compare(TT.B))) {
			T.neighbour_idx[2] = TT.key;
			TT.neighbour_idx[1] = T.key;
		} else if ((T.B.compare(TT.A) && T.C.compare(TT.B))
				|| (T.B.compare(TT.B) && T.C.compare(TT.A))) {
			T.neighbour_idx[1] = TT.key;
			TT.neighbour_idx[0] = T.key;
		} else if ((T.B.compare(TT.A) && T.C.compare(TT.C))
				|| (T.B.compare(TT.C) && T.C.compare(TT.A))) {
			T.neighbour_idx[1] = TT.key;
			TT.neighbour_idx[2] = T.key;
		} else if ((T.B.compare(TT.B) && T.C.compare(TT.C))
				|| (T.B.compare(TT.C) && T.C.compare(TT.B))) {
			T.neighbour_idx[1] = TT.key;
			TT.neighbour_idx[1] = T.key;
		}

	}

	/***************************************************************************
	 * The method for testing new inserting triangles into triangulation
	 * 
	 * @param TestingTriangles -
	 *            private class of the incrementalDT, is it AbstractDateType
	 *            "spojovy seznam TriangleDT" newTriangles - is pointer which
	 *            pointed on start of ADT endOfListNewTriangles - is pointer
	 *            which pointed on end of ADT
	 */

	private void testNewTriangles(TestingTriangles newTriangles,
			TestingTriangles endOfListNewTriangles) {
		PointDT freePointA, freePointB;
		while (newTriangles != null) { 
			for (int i = 0; i < 3; i++) {
										
				TriangleDT Tni = triangles.getTriangle(newTriangles.trian.neighbour_idx[i]);
				if (Tni != null) {
					freePointB = searchFreePointOfTwoTriangles(
							newTriangles.trian, Tni);
					freePointA = searchFreePointOfTwoTriangles(
							Tni, newTriangles.trian);
					if (delaunay(Tni, freePointB)) { 
						endOfListNewTriangles.next = new TestingTriangles();
						endOfListNewTriangles = endOfListNewTriangles.next;
						endOfListNewTriangles.next = new TestingTriangles();
						if (i == 0) { 
							endOfListNewTriangles.trian = new TriangleDT(
									newTriangles.trian.A, newTriangles.trian.C,
									freePointA);
							endOfListNewTriangles.next.trian = new TriangleDT(
									newTriangles.trian.B, newTriangles.trian.C,
									freePointA);
						} else {
							if (i == 1) {
								endOfListNewTriangles.trian = new TriangleDT(
										newTriangles.trian.A,
										newTriangles.trian.B, freePointA);
								endOfListNewTriangles.next.trian = new TriangleDT(
										newTriangles.trian.A,
										newTriangles.trian.C, freePointA);
						} else {
								endOfListNewTriangles.trian = new TriangleDT(
										newTriangles.trian.B,
										newTriangles.trian.A, freePointA);
								endOfListNewTriangles.next.trian = new TriangleDT(
										newTriangles.trian.B,
										newTriangles.trian.C, freePointA);
							}
						}
						for (int j = 0; j < 3; j++) { // ulozeni sousedu nove
														// vyniklych
														// trojuhelniku
							newTriangles.trian = triangles.getTriangle(newTriangles.trian.key);
							TriangleDT Tnj = triangles.getTriangle(newTriangles.trian.neighbour_idx[j]);
							if ((Tnj != null)
									&& (!Tnj.compare(Tni))) {
							
								saveTriangleNeighbour(
										endOfListNewTriangles.next.trian,
										Tnj);
							saveTriangleNeighbour(
										endOfListNewTriangles.trian,
										Tnj);
								triangles.delete(Tnj.key);
								triangles.insertToTree(Tnj, Tnj.key);
								Tnj = triangles.getTriangle(newTriangles.trian.neighbour_idx[j]);
						
							}
						}
						for (int j = 0; j < 3; j++) {
							TriangleDT Tnij = triangles.getTriangle(Tni.neighbour_idx[j]);
							newTriangles.trian = triangles.getTriangle(newTriangles.trian.key);
							if ((Tnij != null)
									&& (!Tnij.compare(newTriangles.trian))) {

								saveTriangleNeighbour(
										endOfListNewTriangles.next.trian,
										Tnij);
								saveTriangleNeighbour(
										endOfListNewTriangles.trian,
										Tnij);
							triangles.delete(Tnij.key);
								triangles.insertToTree(Tnij, Tnij.key);
							
							}
						}
						saveTriangleNeighbour(endOfListNewTriangles.trian,
								endOfListNewTriangles.next.trian);

						try { // vstup do datove struktury
							triangles.delete(newTriangles.trian.key);
							triangles.delete(Tni.key);
							triangles.insertToTree(endOfListNewTriangles.trian,
									endOfListNewTriangles.trian.key);
							triangles.insertToTree(
									endOfListNewTriangles.next.trian,
									endOfListNewTriangles.next.trian.key);
						} catch (Exception e) {
							e.printStackTrace();
						}
						endOfListNewTriangles = endOfListNewTriangles.next;
						i = 3;

					}
				}
			}
			newTriangles = newTriangles.next;
		}
	}

	/***************************************************************************
	 * The method for searching a free point from two triangles, which have two
	 * same points
	 * 
	 * @param TriangleDT -
	 *            the first triangle TriangleDT - the second triangle
	 */

	private PointDT searchFreePointOfTwoTriangles(TriangleDT firstTriangle,
			TriangleDT secondTriangle) {
	if ((!firstTriangle.A.compare(secondTriangle.A))&&
		(!firstTriangle.A.compare(secondTriangle.B))&&
		(!firstTriangle.A.compare(secondTriangle.C)))
		return firstTriangle.A;
	if ((!firstTriangle.B.compare(secondTriangle.A))&&
			(!firstTriangle.B.compare(secondTriangle.B))&&
			(!firstTriangle.B.compare(secondTriangle.C)))
		return firstTriangle.B;
	else
		return firstTriangle.C;
 	}

	/***************************************************************************
	 * The method for calculation delaunay's test
	 * 
	 * @param TriangleDT
	 *            T - testing triangle PointDT A - free point point
	 * 
	 * @return boolean - true - if the new point A is inside circle of triangles
	 *         T
	 */

	private boolean delaunay(TriangleDT T, PointDT A) {
		double N = ((Math.pow(T.C.x, 2) - Math.pow(T.A.x, 2)
				+ Math.pow(T.C.y, 2) - Math.pow(T.A.y, 2))
				* (T.B.x - T.A.x) - (Math.pow(T.B.x, 2) - Math.pow(T.A.x, 2)
				+ Math.pow(T.B.y, 2) - Math.pow(T.A.y, 2))
				* (T.C.x - T.A.x))
				/ (-2 * ((T.A.y - T.C.y) * (T.B.x - T.A.x) - (T.A.y - T.B.y)
						* (T.C.x - T.A.x)));
		double M = (Math.pow(T.B.x, 2) - Math.pow(T.A.x, 2)
				- Math.pow((T.A.y - N), 2) + Math.pow((T.B.y - N), 2))
				/ (2 * (T.B.x - T.A.x));
		double R = Math.sqrt(Math.pow((T.A.x - M), 2)
				+ Math.pow((T.A.y - N), 2)); 
		// tolerance for delaunay circle
		if ((R - 0.0001) > (Math.sqrt(Math.pow(( A.x - M), 2)
				+ Math.pow((A.y - N), 2)))) 
			return true;
		else {
			if (R > 0) {
				return false;
			} else {
				return delaunay(new TriangleDT(T.B, T.C, T.A), A);
			}
		}
	}

	/***************************************************************************
	 * The method for imputing new point into triangulation. The point isn't
	 * inside convex hull
	 * 
	 * @param PointDT
	 *            P - new point
	 */

	private void createConvexPolygon(PointDT P) {
		// distance between two point of convex hull
		double nextPointDistance = 0; 
		// points of convex
		Coordinate[] newConvex; 
		// vertexes of newly formed triangles 
		Coordinate[] vertexsNewTriangles; 
		int i, j;
		PointDT X;
		// direction for solving convex
		boolean directArrayPoints = true; 
		// inserting new point into convex
		newConvex = new Coordinate[convex.length + 1]; 
														
		for (i = 0; i < convex.length - 1; i++)
			newConvex[i] = convex[i];
		newConvex[newConvex.length - 1] = convex[0];
		newConvex[newConvex.length - 2] = P;
		// computing new convex
		CoordinateArraySequence pointsWhichGenerateConvex = new CoordinateArraySequence(
				newConvex);
		newConvexGeometry = new LinearRing(pointsWhichGenerateConvex,
				new GeometryFactory());
		newConvex = newConvexGeometry.convexHull().getCoordinates();
		// creating new convex geometry
		pointsWhichGenerateConvex = new CoordinateArraySequence(newConvex);
		newConvexGeometry = new LinearRing(pointsWhichGenerateConvex,
				new GeometryFactory());

		if (P.compare(newConvex[0])) {
			i = 1;
			j = newConvex.length - 2;
		} else {
			for (i = 1; i < newConvex.length; i++)
				if (P.compare(newConvex[i]))
					break;

			j = i + 1;
			i--;
		}

		boolean foundedI = false;
		boolean foundedJ = false;
		for (int w = 0; w < convex.length - 1; w++) {

			X = new PointDT(convex[w]);
			if (!foundedI && X.compare(newConvex[i])) {
				i = w;
				foundedI = true;
			}
			if (!foundedJ && X.compare(newConvex[j])) {
				j = w;
				foundedJ = true;
			}
			if (X.compare(P)) {
				duplicitePoint = true;
				break;
			}
		} 
		// indexes i,j detect breaking of convex hull

		if (!duplicitePoint) { 
			double k = (convex[i].y - convex[j].y)
					/ (convex[i].x - convex[j].x);
			// finding direction of breaking out 
			double q1 = convex[i].y - k * convex[i].x; 
			double minDistance = Math.abs((-k * P.x + P.y - q1)
					/ Math.sqrt(Math.pow(k, 2) + 1));
			if (convex[i].x == convex[j].x) {
				if (convex[i].x == 0) {
					if ((convex[i + 1].x - P.x) < (convex[j + 1].x - P.x))
						directArrayPoints = false;

				} else {
					minDistance = Math.abs(convex[j].x - P.x);
					if (new PointDT(convex[i + 1]).compare(convex[j])) {
						nextPointDistance = Math.abs(convex[j + 1].x - P.x);
						if (new TriangleDT(convex[i], convex[j], convex[j + 1])
								.isTriangle()) {
							if (minDistance > nextPointDistance)
								directArrayPoints = false;
						} else {
							nextPointDistance = Math.abs(convex[i + 1].x - P.x);
							if (minDistance <= nextPointDistance)// tyafa
																	// obracene
								directArrayPoints = false;

						}
					} else {
						nextPointDistance = Math.abs(convex[i + 1].x - P.x);
						if (new TriangleDT(convex[i], convex[j], convex[i + 1])
								.isTriangle()) {
							if (minDistance <= nextPointDistance)// tyafa
																	// obracene
								directArrayPoints = false;
						} else {
							nextPointDistance = Math.abs(convex[j + 1].x - P.x);
							if (minDistance > nextPointDistance)
								directArrayPoints = false;
						}
					}
				}
			} else {
				double q2;
				if (new PointDT(convex[i + 1]).compare(convex[j])) {

					q2 = convex[j + 1].y - k * convex[j + 1].x;
					nextPointDistance = Math.abs((-k * P.x + P.y - q2)
							/ Math.sqrt(Math.pow(k, 2) + 1));
					if (new TriangleDT(convex[i], convex[j], convex[j + 1])
							.isTriangle()) {
						if (minDistance > nextPointDistance)
							directArrayPoints = false;
					} else {
						q2 = convex[i + 1].y - k * convex[i + 1].x;
						nextPointDistance = Math.abs((-k * P.x + P.y - q2)
								/ Math.sqrt(Math.pow(k, 2) + 1));
						if (minDistance < nextPointDistance)
							directArrayPoints = false;
					}
				} else {
					q2 = convex[i + 1].y - k * convex[i + 1].x;
					nextPointDistance = Math.abs((-k * P.x + P.y - q2)
							/ Math.sqrt(Math.pow(k, 2) + 1));
					if (new TriangleDT(convex[i], convex[j], convex[i + 1])
							.isTriangle()) {
						if (minDistance < nextPointDistance)
							directArrayPoints = false;
					} else {
						q2 = convex[j + 1].y - k * convex[j + 1].x;
						nextPointDistance = Math.abs((-k * P.x + P.y - q2)
								/ Math.sqrt(Math.pow(k, 2) + 1));
						if (minDistance > nextPointDistance)
							directArrayPoints = false;
					}

				}
			}
			int index;

			if (!directArrayPoints) {
				index = i;
				i = j; 
				j = index;
			}
			index = 0;

			if (i < j) {
				vertexsNewTriangles = new Coordinate[j - i + 1];
				for (int w = i; w <= j; w++) {
					vertexsNewTriangles[index] = convex[w];
					index++;
				}
			} else {
				vertexsNewTriangles = new Coordinate[convex.length - i + j];
				for (int w = i; w < convex.length - 1; w++) {
					vertexsNewTriangles[index] = convex[w];
					index++;
				}
				for (int w = 0; w <= j; w++) {
					vertexsNewTriangles[index] = convex[w];
					index++;
				}
			}

			rebuildConvex(newConvex);
			createConvexTriangles(vertexsNewTriangles, P);
			convex = newConvex;
		}
	}

	/***************************************************************************
	 * The method for creating new convex triangles
	 * 
	 * @param Coordinate[] -
	 *            array of convex points, which generate new convex triangles
	 * @param PointDT
	 *            P - new point
	 */

	private void createConvexTriangles(Coordinate[] vertexsNewTriangles,
			PointDT P) {
		int i = 0;
		int j = vertexsNewTriangles.length - 1;
		TestingTriangles newTriangles = new TestingTriangles();
		TestingTriangles topOfNewTriangles = new TestingTriangles();

		while (!new TriangleDT(P, new PointDT(vertexsNewTriangles[i]),
				new PointDT(vertexsNewTriangles[i + 1])).isTriangle())
			i++;

		newTriangles.trian = new TriangleDT(P, new PointDT(
				vertexsNewTriangles[i]),
				new PointDT(vertexsNewTriangles[i + 1]));

		topOfNewTriangles = newTriangles;

		TriangleDT pomocnyS = ringSearchConvexTriangle(new PointDT(
				vertexsNewTriangles[i]),
				new PointDT(vertexsNewTriangles[i + 1]));

		saveTriangleNeighbour(newTriangles.trian, pomocnyS);
		triangles.delete(pomocnyS.key);
		triangles.insertToTree(pomocnyS, pomocnyS.key);
		i++;
		while (i != j) {
			pomocnyS = new TriangleDT(P, new PointDT(vertexsNewTriangles[i]),
					new PointDT(vertexsNewTriangles[i + 1]));
			if (pomocnyS.isTriangle()) {

				newTriangles.next = new TestingTriangles();
				newTriangles.next.trian = pomocnyS;
				pomocnyS = ringSearchConvexTriangle(new PointDT(
						vertexsNewTriangles[i]), new PointDT(
						vertexsNewTriangles[i + 1]));
				saveTriangleNeighbour(newTriangles.next.trian, pomocnyS);
				triangles.delete(pomocnyS.key);
				triangles.insertToTree(pomocnyS, pomocnyS.key);
				saveTriangleNeighbour(newTriangles.trian,
						newTriangles.next.trian);
				// newTriangles.next.trian.printTriangle();
				newTriangles = newTriangles.next;
			}
			i++;
		}

		newTriangles = topOfNewTriangles;
		try {// ulozeni do dat.struktury
			while (true) {
				triangles.insertToTree(newTriangles.trian,
						newTriangles.trian.key);
				number_Triangles++;
				if (newTriangles.next == null) {
					break;
				}
				newTriangles = newTriangles.next;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		testNewTriangles(topOfNewTriangles, newTriangles);
	}

	/***************************************************************************
	 * The method for creating new convex triangles
	 * 
	 * @param Coordinate[] -
	 *            array of new convex points, which must be getting full
	 * 
	 */

	private void rebuildConvex(Coordinate[] newConvex) {
		boolean isSame; // body newConvex,jsou body nejmensiho convexniho obalu,
		for (int w = 0; w < convex.length - 1; w++) {// zde se doplni body
														// ktere lezi na hrane
														// convexu
			isSame = false;
			for (int v = 0; v < newConvex.length - 1; v++)
				if (new PointDT(convex[w]).compare(newConvex[v]))
					isSame = true;
			if (!isSame) {
				Coordinate[] oldPoint = { convex[w] };
				CoordinateArraySequence oldPoints = new CoordinateArraySequence(
						oldPoint);
				Point oldP = new Point(oldPoints, new GeometryFactory());
				if (newConvexGeometry.contains(oldP))
					newConvex = addPointToConvexGeometry(oldP, newConvex);
			}
		}
	}

	/***************************************************************************
	 * The method for adding point, which is on edge of convex hull, but isn't
	 * in points which generate convex hull
	 * 
	 * @param PointDT
	 *            P - new point Coordinate[] - coordinate of convex hull's
	 *            points
	 * 
	 * @return Coordinate[] - coordinate of convex hull's points
	 */

	private Coordinate[] addPointToConvexGeometry(Point P,
			Coordinate[] pointsOfConvex) {
		Coordinate[] edgeOfConvex = new Coordinate[2];

		for (int i = 0; i < pointsOfConvex.length - 1; i++) {
			edgeOfConvex[0] = pointsOfConvex[i];
			edgeOfConvex[1] = pointsOfConvex[i + 1];
			CoordinateArraySequence q = new CoordinateArraySequence(
					edgeOfConvex);
			LineString L = new LineString(q, new GeometryFactory());

			if (L.contains(P)) {
				int j;
				Coordinate[] tempArray = new Coordinate[pointsOfConvex.length + 1];
				for (j = 0; j <= i; j++)
					tempArray[j] = pointsOfConvex[j];
				tempArray[j++] = P.getCoordinate();
				for (; j < tempArray.length; j++)
					tempArray[j] = pointsOfConvex[j - 1];

				return tempArray;
			}
		}
		return null;
	}

}
