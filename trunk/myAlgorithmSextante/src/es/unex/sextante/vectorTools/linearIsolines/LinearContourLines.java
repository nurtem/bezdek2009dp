/****************************************************************************
 *	Sextante - Geospatial analysis tools
 *  www.sextantegis.com
 *  (C) 2009
 *    
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * 	You should have received a copy of the GNU General Public License
 *	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 *    @author      	Josef Bezdek, ZCU Plzen
 *	  @version     	1.0
 *    @since 		JDK1.5 
 */

package es.unex.sextante.vectorTools.linearIsolines;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;

public class LinearContourLines {

	ArrayList finalIsolines  = null;
	double elevatedStep;
	double clusterTol;
	double minIso;
	double maxIso;
	int numberOfIsolines;
	TreeMap treeIndex;
	
	/************************************************************************
	 * Constructor 
	 * @param equidistance - equidistance between isolines
	 * @param clusterTol - minimum diference in X,Y coordinates between points where the points are indetical 
	 */
	public LinearContourLines (double equiDistance, double clusterTol){
		elevatedStep = equiDistance;
		int coeficient = 1;
		while (clusterTol<1){
			coeficient *= 10;
			clusterTol *= coeficient;
		}
		this.clusterTol = coeficient;
		finalIsolines = new ArrayList();
		treeIndex = new TreeMap<Integer, BinaryTree>();
	}

	
	/************************************************************************
	 * Private function which generetes izolines from triangle with defined elevated Step
	 * @param T - triangle
	 */
	private void trianglesIsoLines(Coordinate[] triangle){
		Double minZ = new Double(0);
		Double maxZ = new Double(0);
		Coordinate startIZO = null;
		Coordinate stopIZO = null;
		double elev = Double.NEGATIVE_INFINITY;
	
		//TEST OF SINGULAR POINTS
		if (triangle[0].z/elevatedStep == (int)(triangle[0].z/elevatedStep))
			triangle[0].z = triangle[0].z + elevatedStep*0.01;//Float.MIN_VALUE;
		if (triangle[1].z/elevatedStep == (int)(triangle[1].z/elevatedStep))
			triangle[1].z = triangle[1].z +  elevatedStep*0.01;//Float.MIN_VALUE;
		if (triangle[2].z/elevatedStep == (int)(triangle[2].z/elevatedStep))
			triangle[2].z = triangle[2].z +  elevatedStep*0.01;//Float.MIN_VALUE;

		minZ = triangle[0].z;
		maxZ = triangle[0].z;
		if (minZ > triangle[1].z)
			minZ = triangle[1].z;
		if (minZ > triangle[2].z)
			minZ = triangle[2].z;
		if (maxZ < triangle[1].z)
			maxZ = triangle[1].z;
		if (maxZ < triangle[2].z)
			maxZ = triangle[2].z;
		if (minZ >= 0)
			elev = ((int)(minZ/elevatedStep+1))*elevatedStep;
		else
			elev = ((int)(minZ/elevatedStep))*elevatedStep;
		
		if (elev <= minZ)
			elev += elevatedStep;
	
		while (elev < maxZ){
			if (((triangle[0].z<elev)&(triangle[1].z>elev))||((triangle[0].z>elev)&(triangle[1].z<elev))){
				startIZO = solveLinearInterpolation(triangle[0], triangle[1], elev);
			}
			if (((triangle[0].z<elev)&(triangle[2].z>elev))||((triangle[0].z>elev)&(triangle[2].z<elev))){
				if (startIZO == null)
					startIZO = solveLinearInterpolation(triangle[0], triangle[2], elev);
				else
					stopIZO = solveLinearInterpolation(triangle[0], triangle[2], elev);
			}
			if (((triangle[1].z<elev)&(triangle[2].z>elev))||((triangle[1].z>elev)&(triangle[2].z<elev))){
				if (startIZO == null)
					startIZO = solveLinearInterpolation(triangle[1], triangle[2], elev);
				if (stopIZO == null)
					stopIZO = solveLinearInterpolation(triangle[1], triangle[2], elev);
			}
			
			startIZO.x = Math.round(startIZO.x*clusterTol)/clusterTol;
			startIZO.y = Math.round(startIZO.y*clusterTol)/clusterTol;
			stopIZO.x = Math.round(stopIZO.x*clusterTol)/clusterTol;
			stopIZO.y = Math.round(stopIZO.y*clusterTol)/clusterTol;
			
			if (!startIZO.equals2D(stopIZO))
				sortIsolines(startIZO,stopIZO, elev);	//
				
			startIZO = null;
			stopIZO = null;
			elev += elevatedStep;
				
		}
				
	}
	
	
	
	

	/********************************************************************************
	 * Private function which computes  point on line, (Linear interpolation)
	 * @param A - start point of line
	 * @param B - end point of line
	 * @param elev - defined elevation
	 * @return - coordinate of point with definied elevation
	 */
	private  Coordinate solveLinearInterpolation(Coordinate A, Coordinate B, double elev){
		double koef;
		double rate;
		if (B.z>A.z){
			rate = (elev - A.z) / (B.z-A.z);
			return  new Coordinate((A.x+(B.x-A.x)*rate),(A.y+(B.y-A.y)*rate),elev);
		}
		else{
			rate = (elev - B.z) / (A.z-B.z);
			return  new Coordinate((B.x+(A.x-B.x)*rate),(B.y+(A.y-B.y)*rate),elev);
		}
	}
	
	/************************************************************************
	 * The method for counting isolines from TIN 
	 * @param triangles - array of vertexes of triangles
	 */
	public void countIsolines(Coordinate[][] triangles){
		for (int i = 0; i<triangles.length; i++ ){
			trianglesIsoLines(triangles[i]);
			triangles[i] = null;
		}
	}
	
	/************************************************************************
	 * The method gets list of isolines 
	 * @return ArrayList of isolines
	 */
	public ArrayList getIsolines(){
		return finalIsolines;
	}

	/************************************************************************
	 * The method sorts segment of isoline to LineString 
	 * @param coordA - start vertex of isoline's segment
	 * @param coordB - stop vertex of isoline's segment
	 * @param elevation - elevation of isoline's segment
	 */
	private void sortIsolines(Coordinate coordA, Coordinate coordB,  double elevation){
		DVertex izoA = null;
		DVertex izoB = null;
		int indexA = 0;
		int indexB = 0;
		BinaryTree tree = null;
		
		int elevIndex = new Double((elevation - minIso)/elevatedStep).intValue();
		
		if (treeIndex.containsKey(elevIndex)){
			tree  = (BinaryTree)treeIndex.get(elevIndex);
		}
		else{
			tree = new BinaryTree();
			treeIndex.put(elevIndex, tree);
		}
		
			izoA = (DVertex)  tree.search(coordA);
			izoB = (DVertex) tree.search(coordB);
			
			if (izoA != null)
				indexA = 1;
			if (izoB != null)
				indexB = 2;
			
			switch (indexA + indexB){
				case 0:{
					LinkedList izoList = new LinkedList();
					izoList.add(coordA);
					izoList.add(coordB);
					
					tree.insert(coordA, new Integer(finalIsolines.size()));
					tree.insert(coordB, new Integer(finalIsolines.size()));
					finalIsolines.add(finalIsolines.size(), izoList);
					break;
				}
				case 1:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
					if (izoList == null)
						break;
					tree.remove(coordA);
					if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
						izoList.addFirst(coordB);
						tree.insert(coordB, izoA.data);
					}
					else{
						
						izoList.addLast(coordB);
						tree.insert(coordB, izoA.data);
					}
					break;
				}
				case 2:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoB.data);
					if (izoList == null)
						break;
					tree.remove(coordB);
						if (((Coordinate)izoList.getFirst()).equals2D(coordB)){
							izoList.addFirst(coordA);
							tree.insert(coordA, izoB.data);
							
						}	
						else{
							izoList.addLast(coordA);
							tree.insert(coordA, izoB.data);
						}
						break;
				}
				case 3:{
					LinkedList izoList = (LinkedList) finalIsolines.get(izoA.data);
					if (izoList == null)
						break;
					if ((izoA.data.intValue() == izoB.data.intValue())){
							tree.remove(coordA);
							tree.remove(coordB);
							if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
								izoList.addLast(coordA);
							}
							else{
								izoList.addFirst(coordA);
							}
						}
						else{
							LinkedList izoListB = (LinkedList) finalIsolines.get(izoB.data);
							if (izoListB == null)
								break;
							if (((Coordinate)izoList.getFirst()).equals2D(coordA)){
								if (((Coordinate)izoListB.getFirst()).equals2D(coordB)){
									Iterator iterIzoB = izoListB.iterator();
									while (iterIzoB.hasNext()){
										izoList.addFirst(iterIzoB.next());
									}
								}
								else{
									Iterator iterIzoB = izoListB.descendingIterator();
									while (iterIzoB.hasNext()){
										izoList.addFirst(iterIzoB.next());
									}
								}
							}
							else{
								if (((Coordinate)izoListB.getFirst()).equals2D(coordB)){
									Iterator iterIzoB = izoListB.iterator();
									while (iterIzoB.hasNext()){
										izoList.addLast(iterIzoB.next());
									}
								}
								else{
								Iterator iterIzoB = izoListB.descendingIterator();
								while (iterIzoB.hasNext()){
									izoList.addLast(iterIzoB.next());
								}
							}
						
						}
						finalIsolines.set(izoB.data, null);
						((DVertex) tree.search((Coordinate)izoList.getLast())).data = izoA.data;
						((DVertex) tree.search((Coordinate)izoList.getFirst())).data = izoA.data;

					}
					tree.remove(coordA);
					tree.remove(coordB);
	
				}
			}	

	}
	
}
