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
import com.vividsolutions.jts.geom.Envelope;

public interface DelaunayDataStore {

	/*****************************************************************
	 * The predefinied method for inserting into data structure
	 * @param T - input triangle
	 * @param key - key of triangle
	 */	
	public void insertToTree(TriangleDT T, double[] key);

	/*****************************************************************
	 * The predefinied method for deleting triangle in data structure
	 * @param key - key of triangle
	 */	
	public void delete(double[] key);

	/*****************************************************************
	 * The predefinied method for setting searched envelope
	 * @param key - key of triangle
	 * @param allDataEnvelope - envelope of all points
	 * @param numberOfPoints - number of points which are in triangulation
	 * @param number - number of points which are searched
	 * @param koeficient - coefficient for setting change of envelope
	 * @return new envelope
	 */	
//	public Envelope setSearchedEnvelope(double[] key,	Envelope allDataEnvelope, int numberOfPoints, int number,
//			double koeficient);

	/*****************************************************************
	 * The predefinied method for searching nearest triangles
	 * @param key - key of triangle 
	 * @param numberSearchedPoints - number of searched points
	 * @return Collection of searched triangles
	 */	
//	public Collection nearest(double[] key, int numberSearchedPoints);

	/*****************************************************************
	 * The predefinied method for sorting triangles. Criterion is nearest
	 * @param P - coordinate of starting points 
	 * @param res - collection of returning triangles from Rtree 
	 * @param number - number of points which are searched
	 * @return sorted array of triangles, criterion is nearest form P 
	 */	
//	public TriangleDT [] countN_Nearest(Coordinate P, Collection res, int number);

	/*****************************************************************
	 * The predefinied method for searching nearest triangles
	 * @param key - key of triangle 
	 * @return nearest triangle from key. Key is as coordinate (key[0] is x, key[1] is y 
	 */	
	public TriangleDT searchNearest(double[] key);

	/*****************************************************************
	 * The predefinied method for searching nearest triangles
	 * @param key - key of triangle 
	 * @param number - number of searched points
	 * @return sorted array of nearest triangle from key. Key is as coordinate (key[0] is x, key[1] is y 
	 */	
//	public Collection searchNearest(double[] key, int numberOfTriangles);

	/*****************************************************************
	 * The predefinied method for getting triangle with definied key
	 * @param key - key of triangle. Key is as coordinate (key[0] is x, key[1] is y)
	 * @return triangle with key
	 */
	public TriangleDT getTriangle(double []key);

	/*****************************************************************
	 * The predefinied method for getting triangle with definied key
	 * @param index - index in data structure
	 * @return triangle with index
	 */
	public TriangleDT getTriangle(int index);
	
	/******************************************************************
	 * The predefinied method for preorder testing 
	 * @param P - new point
	 * @return - triangles which contain new P
	 */
	public TriangleDT preorderTest(Coordinate P);
	
	/******************************************************************
	 * The predefinied method for getting number of triangles 
	 * @return - number of triangles
	 */
	public int getNumberOfTriangles();
	
	/*******************************************************************
 	 * The predefinied method for setting number of triangles 
	 * @param number - new number of triangles
	 */
	public void setNumberOfTrinagles(int number);
	
	/******************************************************************
	 * The predefinied method for preorder testing 
	 * @param A - vertex 
	 * @param B - vertex
	 * @return - triangles which contain point A and point B
	 */
	public TriangleDT preorderTestContainTwoPoint(PointDT A, PointDT B);
	
	/******************************************************************
	 * The predefined method for creating shapefile 
	 * @param path - vertex 
	 * @param File - vertex
	 * @param EPSG - EPSG code (EPSG:2165)
	 */
	public void createShapefile(String path, String File, String EPSG);
	
	/*****************************************************************
	 * The predefined method for close inserting 
	 */
	public void closeInserting();
	
	/*****************************************************************
	 * The predefined method for getting status of close inserting
	 * @return true if the triangles are close inserting
	 */
	public boolean getStatusInserting();
	
	/*****************************************************************
	 * The predefined method for getting collection of triangles which are 
	 * intersected by envelope
	 * @param env - Envelope for intersect triangles
	 * @return collection with intersected triangles
	 */
	public Collection getIntersectEnvelope(Envelope env);

}
