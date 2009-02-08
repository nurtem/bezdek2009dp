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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
 
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.memory.MemoryPageStore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class DelaunayDataStoreHDD implements DelaunayDataStore {
	// data structure which contain index into data structure with triangles
	private RTree rt = null;
	private Data data;
	private DataDefinition dd = new DataDefinition("US-ASCII"); 
	private ObjectToFile triangles = null;
	// list with deleted indexes in data structure with triangles
	private LinkedList deletedIdx = new LinkedList();													// org.geotools.index.Data
	private Envelope env;
	public int numberOfTriangles = 0;
	public boolean statusInserting = true;
	
	/***********************************************************************
	 * Constructor - set path and file to saving
	 * @param path - path 
	 * @param file - name of file
	 */
	public DelaunayDataStoreHDD(String path, String file) {
		dd.addField(Integer.class);
		triangles = new ObjectToFile(path+file);		
		try {
			PageStore ps = new MemoryPageStore(dd);
			rt = new RTree(ps); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	/*********************************************************************
	 * private class for sorting triangles
	 * @author pepitoX
	 *
	 */
	private static class Element implements Comparable { 
														
		double distance; 
		TriangleDT T; 
		
		Element(double distance, TriangleDT T) {
			this.distance = distance;
			this.T = T;
		}

		public int compareTo(Object element) {
			return distance > ((Element) element).distance ? 1 : -1;
		}
	}


	/*****************************************************************
	 * The predefinied method for inserting into data structure
	 * @param T - input triangle
	 * @param key - key of triangle
	 */	
	public void insertToTree(TriangleDT T, double[] key) {
		env = new Envelope(new Coordinate(key[0], key[1])); 
		data = new Data(dd);
		try {
			if (deletedIdx.size()>0){
				data.addValue((Integer)deletedIdx.getFirst());
				rt.insert(env, data);
				triangles.writeT(T.triangleToBasicType(),(Integer)deletedIdx.getFirst());
				deletedIdx.removeFirst();
			}
			else{
				data.addValue(numberOfTriangles);
				rt.insert(env, data);
				triangles.writeT(T.triangleToBasicType());
				numberOfTriangles++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*****************************************************************
	 * The predefinied method for deleting in data structure
	 * @param key - key of triangle
	 */	
	public void delete(double[] key) {
		env = new Envelope(new Coordinate(key[0], key[1]));
		try {
			Iterator res = rt.search(env).iterator();
			Data data = (Data) res.next(); 
			deletedIdx.add(data.getValue(0));
			rt.delete(env);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*****************************************************************
	 * The predefinied method for setting searched envelope
	 * @param key - key of triangle
	 * @param allDataEnvelope - envelope of all points
	 * @param numberOfPoints - number of points which are in triangulation
	 * @param number - number of points which are searched
	 * @param koeficient - coefficient for setting change of envelope
	 * @return new envelope
	 */	
	public Envelope setSearchedEnvelope(double[] key,
			Envelope allDataEnvelope, int numberOfPoints, int number,
			double koeficient) {
		double envHeight; 
		if (allDataEnvelope.getHeight() > allDataEnvelope.getWidth())
			envHeight = allDataEnvelope.getHeight() / numberOfPoints * 26
					* number;
		else
			envHeight = allDataEnvelope.getWidth() / numberOfPoints * 26
					* number;
		envHeight = envHeight / 2 * koeficient;
		if (envHeight==0){
			envHeight=Double.POSITIVE_INFINITY;
		}
		return new Envelope(key[0] - envHeight, key[0] + envHeight, key[1]
				- envHeight, key[1] + envHeight);
	}

	/*****************************************************************
	 * The predefinied method for searching nearest triangles
	 * @param key - key of triangle 
	 * @param numberSearchedPoints - number of searched points
	 * @return Collection of searched triangles
	 */	
	public Collection nearest(double[] key, int numberSearchedPoints) {
		Collection res = null;
		
		try {
			for (int i = 1;; i++) {
				Envelope searchedEnvelope = setSearchedEnvelope(key, rt.getBounds()
						, numberOfTriangles, numberSearchedPoints, Math.pow(i,2));
				
				res = rt.search(searchedEnvelope);
				
				if (res.size() >= numberSearchedPoints)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/*****************************************************************
	 * The predefinied method for getting triangle with definied key
	 * @param key - key of triangle. Key is as coordinate (key[0] is x, key[1] is y)
	 * @return triangle with key
	 */
	public TriangleDT getTriangle(double []key){
		Data data = null;
		try{
			List list = rt.search(new Envelope(new Coordinate(key[0], key[1])));
			Iterator iter = list.iterator();
			if (iter.hasNext())
				data = (Data) iter.next();
			
		}
		catch (Exception e){
			System.out.println(e);
			System.exit(1);
		}
		if (data != null)
			return triangles.read((Integer)data.getValue(0));
		else
			return null;
	}
	
	/*****************************************************************
	 * The predefinied method for getting triangle with definied key
	 * @param index - index in data structure
	 * @return triangle with index
	 */
	public TriangleDT getTriangle(int index){
		if (deletedIdx.contains(index)){
			return null;
		}
		return triangles.read(index);
	}
	

	/*****************************************************************
	 * The predefinied method for sorting triangles. Criterion is nearest
	 * @param P - coordinate of starting points 
	 * @param res - collection of returning triangles from Rtree 
	 * @param number - number of points which are searched
	 * @return sorted array of triangles, criterion is nearest form P 
	 */	
	public TriangleDT[] countN_Nearest(Coordinate P, Collection res, int number) {
		Iterator it = res.iterator();
		Vector vector = new Vector(); // vector for sorting
		int pocet_kroku = 0;
		while (it.hasNext()) { // count distance
			Data data = (Data) it.next();
			TriangleDT T = triangles.read((Integer)data.getValue(0));
			Coordinate N = new Coordinate(T.key[0],T.key[1]);
			double distance = N.distance(P);
			Element element = new Element(distance, T);
			vector.add(element); // save to vector
			pocet_kroku++;
		}
		Collections.sort(vector); // sorting vector
		it = vector.iterator();
		TriangleDT[] sortedArray = new TriangleDT[number]; // creating output array
		for (int i = 0; i < number; i++) {
			sortedArray[i] = ((Element) it.next()).T;
		}
		return sortedArray;
	}

	/*****************************************************************
	 * The predefinied method for searching nearest triangles
	 * @param key - key of triangle 
	 * @return nearest triangle from key. Key is as coordinate (key[0] is x, key[1] is y 
	 */	
	public TriangleDT searchNearest(double[] key) {
		TriangleDT[] sortedArray = countN_Nearest(new Coordinate(key[0], key[1]),
				nearest(key, 1), 1);
		return (TriangleDT)sortedArray[0];
	}

	/*****************************************************************
	 * The predefinied method for searching nearest triangles
	 * @param key - key of triangle 
	 * @param number - number of searched points
	 * @return sorted array of nearest triangle from key. Key is as coordinate (key[0] is x, key[1] is y 
	 */	
	public TriangleDT[] searchNearest(double[] key, int number) {
		TriangleDT[] sortedArray = countN_Nearest(new Coordinate(key[0], key[1]),
				nearest(key, number), number);
		return sortedArray;
	}

	/******************************************************************
	 * The predefinied method for preorder testing 
	 * @param P - new point
	 * @return - triangles which contain new P
	 */
	public TriangleDT preorderTest(Coordinate P) {
		for (int i = 0; i<numberOfTriangles;i++){
			TriangleDT T = triangles.read(i);
			if (T.contains(new PointDT(P))||T.containsPointAsVertex(new PointDT(P)))
				return T;
		}
		return null;

	}
	
	/******************************************************************
	 * The predefinied method for preorder testing 
	 * @param A - vertex 
	 * @param B - vertex
	 * @return - triangles which contain point A and point B
	 */
	public TriangleDT preorderTestContainTwoPoint(PointDT A, PointDT B){
		for (int i = 0; i<numberOfTriangles;i++){
			TriangleDT T = triangles.read(i);
			if (T.containsTwoPoints(A, B))
				return T;
		}
		return null;

	}

	/******************************************************************
	 * The predefinied method for creating shapefile 
	 * @param path - vertex 
	 * @param File - vertex
	 * @param EPSG - EPSG code (EPSG:2165)
	 */
	
	
	public void closeStrem(){
		
	}
	
	/******************************************************************
	 * The predefinied method for getting number of triangles 
	 * @return - number of triangles
	 */
	public int getNumberOfTriangles(){
		return numberOfTriangles;
	}
	
	/*******************************************************************
 	 * The predefinied method for setting number of triangles 
	 * @param number - new number of triangles
	 */
	public void setNumberOfTrinagles(int number){
		numberOfTriangles = number;
	}
	
	/*****************************************************************
	 * The predefined method for getting status of close inserting
	 * @return true if the triangles are close inserting
	 */

	public boolean getStatusInserting(){
		return statusInserting;
	}
	
	
	/*****************************************************************
	 * The method which get all intersect envelope
	 * @param env - envelope
	 * @return Collection which contain all envelope intersected by env 
	 */		
	public Collection getIntersectEnvelope(Envelope env) {
		LinkedList list = new LinkedList();
		try{
			List listOfTriangles = rt.search(env);
			Iterator iter = listOfTriangles.iterator();
			while (iter.hasNext()){
				data = (Data)iter.next();
				list.add(triangles.read((Integer)data.getValue(0)));
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	/*****************************************************************
	 * method for close inserting 
	 */
	public void closeInserting(){
		statusInserting = false;
		try{
			PageStore ps = new MemoryPageStore(dd);
			for (int i = 0; i<numberOfTriangles; i++){
				rt = new RTree(ps);
				TriangleDT T = getTriangle(i);
				if (T!=null){
					data = new Data(dd);
					data.addValue(numberOfTriangles);
					rt.insert(T.getEnvelope(), data);
				}
			}	
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}


}
