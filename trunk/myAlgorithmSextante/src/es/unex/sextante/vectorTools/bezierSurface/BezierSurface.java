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

package es.unex.sextante.vectorTools.bezierSurface;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.RTree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class BezierSurface {
	private Data data;
	private DataDefinition dd = new DataDefinition("US-ASCII"); 
	private RTree trianglesIndex;
	Coordinate [][] triangles;
	TreeMap breakLines = new TreeMap();
	Bezier miniBezierTriangles[];
	double scaleZ;
	int index = 0;
	int m_LoD;
	byte[] trianIndex;
	float[][] barycentrCoor;
	
	/********************************************************************
	 * The private class for defining object triangle
	 */
	private class Triangle{
		int index;
		Coordinate coord[] = new Coordinate[3];
		int typeOfBreakLine;

		/********************************************************************
		 * Constructor
		 * @param index - index of triangle
		 * @param coord - array of vertexes of triangle
		 * @param typeOfBreakLine - type of break line which triangle contains
	     */
		Triangle (int index, Coordinate[] coord, int typeOfBreakLine){
			this.index = index;
			this.coord = coord;
			this.typeOfBreakLine = typeOfBreakLine;
		}
		
		/********************************************************************
		 * Constructor
		 * @param index - index of triangle
		 * @param A - vertex A of triangle
		 * @param B - vertex B of triangle
		 * @param C - vertex C of triangle
		 * @param typeOfBreakLine - type of break line which triangle contains
	     */
		Triangle (int index, Coordinate A, Coordinate B, Coordinate C, int typeOfBreakLine){
			this.index = index;
			coord[0] = A;
			coord[1] = B;
			coord[2] = C;
			this.typeOfBreakLine = typeOfBreakLine;
		}
		void toStringa(){
			System.out.println(coord[0]);
			System.out.println(coord[1]);
			System.out.println(coord[2]);  
			System.out.println(index);
			System.out.println(typeOfBreakLine);
		}
	}
	

	/********************************************************************
	 * Constructor
	 * @param triangles - array of triangle's vertexes
	 * @param trainglesIndex - RTree index of triangles
	 * @param breakLine - map of break lines
	 * @param scaleZ - scale of Z coordinate to optimize data
	 * @param m_LoD - Level of Detail
     */
	public BezierSurface(Coordinate[][] triangles, RTree trianglesIndex, TreeMap breakLines, double scaleZ, int m_LoD){
		this.trianglesIndex = trianglesIndex;
		this.triangles = triangles;
		this.breakLines = breakLines;
		this.scaleZ = scaleZ;
		this.m_LoD = m_LoD;
	
		for (int k=0; k<triangles.length; k++)
			for (int l=0;l<3; l++)
				triangles[k][l].z /= scaleZ;
		setBaryCoordinates();	
		dd.addField(Integer.class);
	}

	/*******************************************************************
	 * The method tests list of bezier triangle if has next
	 * @param true - hasNext, false hasn't Next
	 */
	public boolean hasNext(){
		if (index==triangles.length)
			return false;
		return true;
	}

	/************************************************************************
	 * The method gets next group of small triangles of main Bezier triangle 
	 * @return coordinates of vertexes of new small triangles 
	 */
	public Coordinate[][] nextTrinagle(){
		int indexOfInterpolatedTriangles = 0;
		Bezier2 newBezierTriangles = new Bezier2(triangles[index]);
		newBezierTriangles.setNormalVector(searchVectors(newBezierTriangles,newBezierTriangles.b300, index),
											   searchVectors(newBezierTriangles,newBezierTriangles.b030, index),
											   searchVectors(newBezierTriangles,newBezierTriangles.b003, index)); 
		if (breakLines.containsKey(index))
			newBezierTriangles.setControlPoints((Integer)breakLines.get(index));
		else
			newBezierTriangles.setControlPoints(-1);
		
		index++;
		return getInterpolatedTriangles2(newBezierTriangles);
	}		
	
	/************************************************************************
	 * The private method sets barycentric coordinates for current LoD (Level of Detail) 
	 */
	private void setBaryCoordinates(){
		switch (m_LoD){
			case 1:{
				byte[] tIndex = {0,0,2  ,0,1,2};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/2F,0},{1/2F,0},
											{1/2F,0},{1/2F,0},{1/2F,0}};
											
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;
			}
			case 2:{
				byte[] tIndex = {0,0,2  ,0,0,2,   0,0,0,  1,1,2};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/3F,0},{2/3F,0},
											{1/3F,0},{0,1},{2/3F,0},
											{0,1}, {1/3F,0},{2/3F,0},
											{0,1},{2/3F,0},{1/3F,0}
											};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
			case 3:{
				byte[] tIndex = {0,0,2  ,0,0,2,   0,0,0, 0,0,0, 0,0,0, 0,1,2};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/4F,0},{3/4F,0},
											{1/4F,0},{0,3/4F},{3/4F,0},
											{1/4F,0}, {2/4F,0},{0,3/4F},
											{0,3/4F},{2/4F,0},{1/4F,3/4F},
											{1/4F,3/4F},{2/4F,0},{3/4F,0},
											{0, 3/4F},{0, 3/4F},{0, 3/4F}
											
										};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
			case 4:{
				byte[] tIndex = {0,0,2  ,0,0,2, 0,0,2,  0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,1,2};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/5F,0},{4/5F,0},
											{1/5F,0},{0,3/5F},{4/5F,0},
											{1/5F,3/5F},{0,3/5F},{1/5F,3/5F},
											{0, 3/5F}, {1/5F,0},{2/5F,0},
											{0,3/5F},{2/5F,0},{1/5F,3/5F},
											{1/5F,3/5F},{2/5F,0},{3/5F,0},
											{1/5F,3/5F},{3/5F,0},{2/5F,3/5F},
											{2/5F,3/5F},{3/5F,0},{4/5F,0},
											{1/5F,3/5F},{1/5F,3/5F},{1/5F,3/5F},
											
										};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
			case 5:{
				byte[] tIndex = {0,0,2  ,0,0,2, 0,0,2, 0,0,2, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 4,4,4};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/6F,0},{5/6F,0},
											{1/6F,0},{0,1/2F},{5/6F,0},
											{0,1/2F},{1/6F,1/2F},{1/3F,1/2F},
											{1/6F,1/2F},{0,1},{1/3F,1/2F},
											
											{1/6F,0},{2/6F,0},{0,1/2F},
											{2/6F,0},{3/6F,0},{1/6F,1/2F},
											{3/6F,0},{4/6F,0},{1/3F,1/2F},
											{4/6F,0},{5/6F,0},{1/2F,1/2F},
											
											{2/6F,0},{0,1/2F},{1/6F,1/2F},
											{3/6F,0},{1/6F,1/2F},{1/3F,1/2F},
											{4/6F,0},{1/3F,1/2F},{1/2F,1/2F},
											{1/6F,1/2F},{1/3F,1/2F},{0,1},
											
											{1/6F,1/2F},{1/4F,1/2F},{0,1}//superfluous
											
										};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
			case 6:{
				byte[] tIndex = {0,0,2  ,0,0,2, 0,0,2, 0,0,2, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,1,2};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/7F,0},{6/7F,0},
											{1/7F,0},{0,3/7F},{6/7F,0},
											{0,3/7F},{1/7F,3/7F},{3/7F,3/7F},
											{0,6/7F},{1/7F,3/7F},{3/7F,3/7F},
										
											{1/7F,0},{2/7F,0},{0,3/7F},
											{2/7F,0},{3/7F,0},{1/7F,3/7F},
											{3/7F,0},{4/7F,0},{2/7F,3/7F},
											{4/7F,0},{5/7F,0},{3/7F,3/7F},
											{5/7F,0},{6/7F,0},{4/7F,3/7F},
											
											{2/7F,0},{1/7F,3/7F},{0   ,3/7F},
											{3/7F,0},{2/7F,3/7F},{1/7F,3/7F},
											{4/7F,0},{3/7F,3/7F},{2/7F,3/7F},
											{5/7F,0},{4/7F,3/7F},{3/7F,3/7F},
											
											{0,6/7F},{2/7F,3/7F},{1/7F,3/7F},
											{1/7F, 6/7F},{3/7F,3/7F},{2/7F,3/7F},
											{0, 6/7F},{1/7F,6/7F},{2/7F,3/7F},
											
											{0,6/7F},{0,6/7F},{0,6/7F},
											
											
										};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
			case 7:{
				byte[] tIndex = {0,0,2, 0,0,2, 0,0,2, 0,0,2, 0,0,2,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
						             0,0,0, 0,0,0, 0,0,0, 0,0,0, 0,1,2};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/8F,0},{7/8F,0},
											{1/8F,0},{0,3/8F},{7/8F,0},
											{0,3/8F},{1/8F,3/8F},{4/8F,3/8F},
											{0,6/8F},{1/8F,3/8F},{4/8F,3/8F},
											{0,6/8F},{1/8F,6/8F},{1/8F,6/8F},
											
											{1/8F,0},{2/8F,0},{0,3/8F},
											{2/8F,0},{3/8F,0},{1/8F,3/8F},
											{3/8F,0},{4/8F,0},{2/8F,3/8F},
											{4/8F,0},{5/8F,0},{3/8F,3/8F},
											{5/8F,0},{6/8F,0},{4/8F,3/8F},
											{6/8F,0},{7/8F,0},{5/8F,3/8F},
											
											{1/8F,3/8F},{2/8F,0},{0,3/8F},
											{2/8F,3/8F},{3/8F,0},{1/8F,3/8F},
											{3/8F,3/8F},{4/8F,0},{2/8F,3/8F},
											{4/8F,3/8F},{5/8F,0},{3/8F,3/8F},
											{5/8F,3/8F},{6/8F,0},{4/8F,3/8F},
											
											{2/8F,3/8F},{0,6/8F},{1/8F,3/8F},
											{3/8F,3/8F},{1/8F,6/8F},{2/8F,3/8F},
											{4/8F,3/8F},{2/8F,6/8F},{3/8F,3/8F},
											
											{2/8F,3/8F},{0,6/8F},{1/8F,6/8F},
											{3/8F,3/8F},{1/8F,6/8F},{2/8F,6/8F},
											
											{1/8F,6/8F},{1/8F,6/8F},{1/8F,6/8F},
										};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
			case 8:{
				byte[] tIndex = {0,0,2, 0,0,2, 0,0,2, 0,0,2, 0,0,2, 0,0,2,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
						             0,0,0, 0,0,0, 0,0,0, 0,0,0,
						             0,0,0, 4,4,4};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {	{0,0},{1/9F,0},{8/9F,0},
											{1/9F,0},{0,1/3F},{8/9F,0},
											{0,1/3F},{1/9F,1/3F},{5/9F,1/3F},
											{0,2/3F},{1/9F,1/3F},{5/9F,1/3F},
											{0,2/3F},{1/9F,2/3F},{2/9F,2/3F},
											{0,1},{1/9F,2/3F},{2/9F,2/3F},
											
											{1/9F,0},{2/9F,0},{0,1/3F},
											{2/9F,0},{3/9F,0},{1/9F,1/3F},
											{3/9F,0},{4/9F,0},{2/9F,1/3F},
											{4/9F,0},{5/9F,0},{3/9F,1/3F},
											{5/9F,0},{6/9F,0},{4/9F,1/3F},
											{6/9F,0},{7/9F,0},{5/9F,1/3F},
											{7/9F,0},{8/9F,0},{6/9F,1/3F},
											
											
											{1/9F,1/3F},{2/9F,0},{0,1/3F},
											{2/9F,1/3F},{3/9F,0},{1/9F,1/3F},
											{3/9F,1/3F},{4/9F,0},{2/9F,1/3F},
											{4/9F,1/3F},{5/9F,0},{3/9F,1/3F},
											{5/9F,1/3F},{6/9F,0},{4/9F,1/3F},
											{6/9F,1/3F},{7/9F,0},{5/9F,1/3F},
											
											{2/9F,1/3F},{0,2/3F},{1/9F,1/3F},
											{3/9F,1/3F},{1/9F,2/3F},{2/9F,1/3F},
											{4/9F,1/3F},{2/9F,2/3F},{3/9F,1/3F},
											{5/9F,1/3F},{1/3F,2/3F},{4/9F,1/3F},
											
											{2/9F,1/3F},{0,2/3F},{1/9F,2/3F},
											{3/9F,1/3F},{1/9F,2/3F},{2/9F,2/3F},
											{4/9F,1/3F},{2/9F,2/3F},{3/9F,2/3F},
											
											{0,1},{1/9F,2/3F},{2/9F,2/3F},
												
										};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
			case 9:{
				byte[] tIndex = {0,0,0, 0,0,2, 2,0,0, 0,0,0, 0,0,0, 0,0,2, 0,0,2, 0,0,2,0,0,2,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 
									 0,0,0, 0,0,0, 0,0,0, 
									 0,0,0, 0,0,0, 0,0,0, 
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
									 0,0,0, 0,0,0, 0,0,0, 0,0,0,
						             0,0,0, 0,0,0, 0,0,0, 0,0,0,
						             0,0,0, 4,4,4};
				//two barycentric coordinates for one trianIndex
				float[][] bCoor = {			{0,0},{1/9F,0},{0, 1/6F},
											{0,0},{0,1/6F},{8/9F,0},
						
											{8/9F,0},{0,1/3F},{0,1/6F},
											{1/9F,0},{1/18F,1/6F},{0,1/6F},
											{0,1/3F},{1/18F,1/6F},{0,1/6F},
											{0,1/3F},{1/9F,1/3F},{5/9F,1/3F},
											{0,2/3F},{1/9F,1/3F},{5/9F,1/3F},
											{0,2/3F},{1/9F,2/3F},{2/9F,2/3F},
											{0,1},{1/9F,2/3F},{2/9F,2/3F},
											
											{1/9F,0},{2/9F,0},{1/18F,1/6F},
											{2/9F,0},{0,1/3F},{1/18F,1/6F},
											{2/9F,0},{3/9F,0},{3/18F,1/6F},
											{3/9F,0},{1/9F,1/3F},{3/18F,1/6F},
											{3/9F,0},{4/9F,0},{5/18F,1/6F},
											{4/9F,0},{2/9F,1/3F},{5/18F,1/6F},
											{4/9F,0},{5/9F,0},{7/18F,1/6F},
											{5/9F,0},{3/9F,1/3F},{7/18F,1/6F},
											{5/9F,0},{6/9F,0},{9/18F,1/6F},
											{6/9F,0},{4/9F,1/3F},{9/18F,1/6F},
											{6/9F,0},{7/9F,0},{11/18F,1/6F},
											{7/9F,0},{5/9F,1/3F},{11/18F,1/6F},
											{7/9F,0},{8/9F,0},{13/18F,1/6F},
											{8/9F,0},{6/9F,1/3F},{13/18F,1/6F},
											
											
											{1/9F,1/3F},{3/18F,1/6F},{0,1/3F},
											{3/18F,1/6F},{2/9F,0},{0,1/3F},
											{2/9F,1/3F},{5/18F,1/6F},{1/9F,1/3F},
											{5/18F,1/6F},{3/9F,0},{1/9F,1/3F},
											{3/9F,1/3F},{7/18F,1/6F},{2/9F,1/3F},
											{7/18F,1/6F},{4/9F,0},{2/9F,1/3F},
											{4/9F,1/3F},{9/18F,1/6F},{3/9F,1/3F},
											{9/18F,1/6F},{5/9F,0},{3/9F,1/3F},
											{5/9F,1/3F},{11/18F,1/6F},{4/9F,1/3F},
											{11/18F,1/6F},{6/9F,0},{4/9F,1/3F},
											{6/9F,1/3F},{13/18F,1/6F},{5/9F,1/3F},
											{13/18F,1/6F},{7/9F,0},{5/9F,1/3F},
											
											
											{2/9F,1/3F},{0,2/3F},{1/9F,1/3F},
											{3/9F,1/3F},{1/9F,2/3F},{2/9F,1/3F},
											{4/9F,1/3F},{2/9F,2/3F},{3/9F,1/3F},
											{5/9F,1/3F},{1/3F,2/3F},{4/9F,1/3F},
											{2/9F,1/3F},{0,2/3F},{1/9F,2/3F},
											{3/9F,1/3F},{1/9F,2/3F},{2/9F,2/3F},
											{4/9F,1/3F},{2/9F,2/3F},{3/9F,2/3F},
											
											{0,1},{1/9F,2/3F},{2/9F,2/3F},
										};
				trianIndex = tIndex;
				barycentrCoor = bCoor;
				break;							
			}
		}
		
	}
	
	/************************************************************************
	 * The private method interpolates coordinates of new small triangles of main Bezier triangle 
	 * @param newBezierTriangles - three path of main Bezier triangle
	 * @return coordinate of vertxes of new small triangles 
	 */
	private Coordinate[][] getInterpolatedTriangles2(Bezier2 newBezierTriangles){
		Bezier[] bezierPatch = new Bezier[3];
		
		for (int i=0; i<3; i++){
			bezierPatch[i] = newBezierTriangles.getBezierPatch(i);
		}
		
		Coordinate[][] newTriangles = new Coordinate[(int)Math.pow(m_LoD+1,2)][3];
		///////////////////
		if (m_LoD == 9)
			newTriangles = new Coordinate[129][3];
		int indexOfNewTriangles = 0;
		for (int i=2; i<trianIndex.length-1;){
			for (int j =0; j<3; j++){
				newTriangles[indexOfNewTriangles][0] = bezierPatch[(trianIndex[i-2]+j)%3].getElevation(barycentrCoor[i-2][0],barycentrCoor[i-2][1], scaleZ);
				newTriangles[indexOfNewTriangles][1] = bezierPatch[(trianIndex[i-1]+j)%3].getElevation(barycentrCoor[i-1][0],barycentrCoor[i-1][1], scaleZ);
				newTriangles[indexOfNewTriangles][2] = bezierPatch[(trianIndex[i]+j)%3].getElevation(barycentrCoor[i][0],barycentrCoor[i][1], scaleZ);
				indexOfNewTriangles++;
			}
			i+=3;
		}
		if (m_LoD!=2 && m_LoD!=5 && m_LoD!=8 && m_LoD!=9){
			newTriangles[indexOfNewTriangles][0] = bezierPatch[0].getElevation(barycentrCoor[barycentrCoor.length-3][0],barycentrCoor[barycentrCoor.length-3][1], scaleZ);
			newTriangles[indexOfNewTriangles][1] = bezierPatch[1].getElevation(barycentrCoor[barycentrCoor.length-2][0],barycentrCoor[barycentrCoor.length-2][1], scaleZ);
			newTriangles[indexOfNewTriangles][2] = bezierPatch[2].getElevation(barycentrCoor[barycentrCoor.length-1][0],barycentrCoor[barycentrCoor.length-1][1], scaleZ);
		}
		return newTriangles;
	}
	
	/************************************************************************
	 * The private method for automatic generating barycentric coordinates for current LoD 
	 * method divides tree Bezier path of main Bezier triangle
	 * @param newBezierTriangles - three path of main Bezier triangle
	 * @return coordinate of vertxes of new small triangles 
	 */
	private Coordinate[][] getInterpolatedTriangles(Bezier2 newBezierTriangles){
		Coordinate[][] newTriangles = new Coordinate[(int)Math.pow(m_LoD+1,2)*3][3];
		int indexOfNewTriangles = 0;
		double [] indexes = new double[m_LoD+2];
		double koeficient = 1/((double)m_LoD+1);
		for (int i = 0; i<=m_LoD+1; i++){
			indexes[i] = koeficient*i;
		}
		for (int k=0; k<3; k++){
			Bezier bezierTriangles2 = newBezierTriangles.getBezierPatch(k);
		
			int maxTi = m_LoD+1;
			int maxTj = m_LoD;
			for (int i = 0; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					newTriangles[indexOfNewTriangles][0] = bezierTriangles2.getElevation(indexes[i], indexes[j], scaleZ);
					newTriangles[indexOfNewTriangles][1] = bezierTriangles2.getElevation(indexes[i], indexes[j+1], scaleZ);
		            newTriangles[indexOfNewTriangles++][2] = bezierTriangles2.getElevation(indexes[i+1], indexes[j], scaleZ);
				}
				maxTj --;
			}
			maxTj = m_LoD-1;
			for (int i = 1; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					newTriangles[indexOfNewTriangles][0] = bezierTriangles2.getElevation(indexes[i], indexes[j], scaleZ);
					newTriangles[indexOfNewTriangles][1] = bezierTriangles2.getElevation(indexes[i], indexes[j+1], scaleZ);
					newTriangles[indexOfNewTriangles++][2] = bezierTriangles2.getElevation(indexes[i-1], indexes[j+1], scaleZ);
				}
				maxTj --;
			}
		}
		return newTriangles;
	}
	


	/*************************************************************************
	 * The method searchs normals vector for vertex P of triangle T
	 * @param T - triangle
	 * @param P - vertex of triangle
	 * @return - linked list of vectors
	 */
	private LinkedList searchVectors(Bezier2 bezierT, Coordinate P, int indexOfBezierT){
		LinkedList vectors = new LinkedList();
		
		List listOfTrianglesIndex = null;
		try{
			listOfTrianglesIndex  = trianglesIndex.search(new Envelope(P));
		}
		catch(Exception e){
			e.printStackTrace();
		}	

		Iterator iterTrianglesIndex = listOfTrianglesIndex.iterator();

		boolean haveBreakLine = false;
		boolean testingBreakLine = true;
		while (iterTrianglesIndex.hasNext()){
			int index = (Integer)((Data)iterTrianglesIndex.next()).getValue(0);
			Coordinate[] TT = triangles[index];
			switch (compareReturnIndex(TT, P)){
				case 'A':{
							if (testingBreakLine)
								haveBreakLine = testOfBreakLine('A', index);
							Coordinate v1 = Bezier2.setVector(P,TT[1]);
							Coordinate v2 = Bezier2.setVector(P,TT[2]);
							double scalar = Bezier2.countScalarProduct(v1,v2);
							double alfa = Math.acos(scalar/(Bezier2.countScalarProduct(v1,v1)*Bezier2.countScalarProduct(v2,v2)));
							Coordinate normal = Bezier2.setNormalVector(v1,v2);
							vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
						
							break;
						}
						case 'B':{
							if (testingBreakLine)
								haveBreakLine = testOfBreakLine('B', index);
							Coordinate v1 = Bezier2.setVector(P,TT[0]);
							Coordinate v2 = Bezier2.setVector(P,TT[2]);
							double scalar = Bezier2.countScalarProduct(v1,v2);
							double alfa = Math.acos(scalar/(Bezier2.countScalarProduct(v1,v1)*Bezier2.countScalarProduct(v2,v2)));
							Coordinate normal = Bezier2.setNormalVector(v1,v2);
							vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
							break;
						}
						case 'C':{
							if (testingBreakLine)
								haveBreakLine = testOfBreakLine('C', index);
							Coordinate v1 = Bezier2.setVector(P,TT[0]);
							Coordinate v2 = Bezier2.setVector(P,TT[1]);
							double scalar = Bezier2.countScalarProduct(v1,v2);
							double alfa = Math.acos(scalar/(Bezier2.countScalarProduct(v1,v1)*Bezier2.countScalarProduct(v2,v2)));
							Coordinate normal = Bezier2.setNormalVector(v1,v2);
							vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
						}				
					}
				if (haveBreakLine){
					testingBreakLine = false;
					haveBreakLine = false;
					vectors = new LinkedList();
					iterTrianglesIndex = setCorectTrianglesIndex(listOfTrianglesIndex, bezierT,  P, indexOfBezierT).iterator();
					
				}	
		}
		return vectors;
		
	}
	
	/************************************************************************
	 * The private method sets corect list of triangles neighbour for current Bezier triangle
	 * The method divides two TIN with hardBreakLines
	 * @param listOfTrianglesIndex - index of trinagle's neighbours
	 * @param bezierT - current Bezier triangle
	 * @param P - vertex of current Bezier triangle
	 * @param indexOfBezierT - index of triangles bezierT
	 * @return list of corect neighbour 
	 */
	private LinkedList setCorectTrianglesIndex(List listOfTrianglesIndex, Bezier2 bezierT, Coordinate P, int indexOfBezierT){
		TreeMap allTriangles = new TreeMap();
		TreeMap newTriangles = new TreeMap();
		Iterator iterOfTrianglesIndex = listOfTrianglesIndex.iterator();
		int typeOfBreakLine;
		while (iterOfTrianglesIndex.hasNext()){
			Data data2 = (Data)iterOfTrianglesIndex.next();
			int index = (Integer)(data2).getValue(0);
			Coordinate[] TT = triangles[index];
			Object typeOfBreakL = breakLines.get(index);
			if (typeOfBreakL!=null)
				typeOfBreakLine = ((Integer)typeOfBreakL).intValue();
			else
				typeOfBreakLine = -1;
			
			switch (compareReturnIndex(TT, P)){
				case 'A':{
					allTriangles.put(index, new Triangle(index,TT, typeOfBreakLine));
					break;
				}	
				case 'B':{
					if (typeOfBreakLine != -1){
						if ((typeOfBreakLine < 3))
							typeOfBreakLine = (typeOfBreakLine+2)%3;
						else
							if (typeOfBreakLine != 6){
								typeOfBreakLine = (typeOfBreakLine+2)%3 + 3;
							}
					}
					allTriangles.put(index, new Triangle(index, TT[1], TT[2], TT[0], typeOfBreakLine));
					break;
				}
				case 'C':{
					if (typeOfBreakLine != -1){
						if (typeOfBreakLine < 3)
							typeOfBreakLine = (typeOfBreakLine+1)%3;
						else
							if (typeOfBreakLine != 6){
								typeOfBreakLine = (typeOfBreakLine+1)%3 + 3;
							}
					}
					allTriangles.put(index, new Triangle(index,TT[2], TT[0], TT[1], typeOfBreakLine));
					break;
				}
			}
		}
		Triangle T = (Triangle)allTriangles.get(indexOfBezierT);
		
		newTriangles.put(indexOfBezierT, T);
		allTriangles.remove(indexOfBezierT);
		
		//TEST OF TRIANGLES ON THE RIGHT SIDE
		Triangle rightT = T;
		boolean change = true;
		while ((rightT.typeOfBreakLine != 6)&&(rightT.typeOfBreakLine != 3)&&(rightT.typeOfBreakLine != 2)&&(rightT.typeOfBreakLine != 5)&&!allTriangles.isEmpty()&&change){
			change = false;
			Iterator iterAllTriangles = allTriangles.values().iterator();
			while (iterAllTriangles.hasNext()){
				T = (Triangle)iterAllTriangles.next();
				if (rightT.coord[2].equals2D(T.coord[1])){
					change = true;
					newTriangles.put(T.index, T);
					allTriangles.remove(T.index);
					rightT = T;
					break;
				}
			}
		}
		//TEST OF TRIANGLES ON THE LEFT SIDE
		Triangle leftT = (Triangle)newTriangles.get(indexOfBezierT);
		change = true;
		while ((leftT.typeOfBreakLine != 6)&&(leftT.typeOfBreakLine != 3)&&(leftT.typeOfBreakLine != 0)&&(leftT.typeOfBreakLine != 4)&&!allTriangles.isEmpty()&&change){
			change = false;
			Iterator iterAllTriangles = allTriangles.values().iterator();
			while (iterAllTriangles.hasNext()){
				T = (Triangle)iterAllTriangles.next();
				if (leftT.coord[1].equals2D(T.coord[2])){
					change = true;
					newTriangles.put(T.index, T);
					allTriangles.remove(T.index);
					leftT = T;
					break;
				}
			}
		}
		
		//Creating of list of indexes for computing normals
		LinkedList finalTriangles = new LinkedList();
		try{
			Iterator iterOfNewTriangles = newTriangles.values().iterator();
			while (iterOfNewTriangles.hasNext()){
				data = new Data(dd);
				
				
				
				data.addValue(((Triangle)iterOfNewTriangles.next()).index);
				finalTriangles.add(data);
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		
		return finalTriangles;
		
	}
	
	/************************************************************************
	 * The private method tests break line if passes current vertex of triangle 
	 * @param vertex - vertex of triangle
	 * @param indexTT - index of triangle in main data structure
	 * @return true - break line passes coordinate of vertxes of new small triangles 
	 */
	private boolean testOfBreakLine(char vertex, int indexTT){
		if (breakLines.containsKey(indexTT)){
			int typeOfBreakLine;
			Object typeOfBreakL = breakLines.get(indexTT);
			if (typeOfBreakL!=null)
				typeOfBreakLine = ((Integer)typeOfBreakL).intValue();
			else
				typeOfBreakLine = -1;
		
			switch (vertex){
				case 'A':{
					if (typeOfBreakLine == 1)
						return false;
					else
						return true;
				}
				case 'B':{
					if (typeOfBreakLine == 2)
						return false;
					else
						return true;
				}
				case 'C':{
					if (typeOfBreakLine == 0)
						return false;
					else
						return true;
				}
			}
		}	
		return false;
	}
	

	/******************************************************************
	 * The method which compare points
	 * @param P - points for comparing
	 * @return index A,B,C which point is same or N if point P not exist in triangle
	 */
	protected char compareReturnIndex(Coordinate[] triangle, Coordinate P){
		if (P.equals2D(triangle[0]))
			return 'A';
		if (P.equals2D(triangle[1]))
			return 'B';
		if (P.equals2D(triangle[2]))
			return 'C';
		return 'N';
		
	}
}