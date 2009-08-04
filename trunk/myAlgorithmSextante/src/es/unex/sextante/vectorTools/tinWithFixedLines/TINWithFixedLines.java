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

package es.unex.sextante.vectorTools.tinWithFixedLines;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.RTree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.index.strtree.STRtree;

import es.unex.sextante.vectorTools.delaunay.Triangulation;
import es.unex.sextante.vectorTools.delaunay.Triangulation.Triangle;

public class TINWithFixedLines {
	RTree trianglesIdx;
	LinkedList fixedLines;	// list of hard lines
	LineDT line = null;
	ArrayList triangles;
	Data data = null;
	DataDefinition dd = new DataDefinition("US-ASCII");

	/********************************************************************************
	 * Constructor
	 * @param triangles - triangles of original TIN
	 * @param trinaglesIdx - triangles index (RTree)
	 * @param fixedLines - fixed lines 
	 */
	public TINWithFixedLines (ArrayList triangles, RTree trianglesIdx, LinkedList fixedLines){
		this.triangles = triangles;
		this.trianglesIdx = trianglesIdx;
		this.fixedLines = fixedLines;
	}
	
	/***************************************************************************
	 * The private method for searching a triangle which are intersect by new hard line
	 */			
	private LinkedList getTrianglesIntersectLine(){
		int index = 0;
		LinkedList trianglesToChange = new LinkedList();
		List trianglesOverEnvelopeIdx = null;
		boolean containA = false;
		boolean containB = false;
		
		Coordinate[] newPoints={line.A,line.B}; 	// creating new geometry of line
		CoordinateArraySequence newPointP=new CoordinateArraySequence(newPoints);
		LineString newL=new LineString(newPointP,new GeometryFactory());
		
		try{
			trianglesOverEnvelopeIdx = (List) trianglesIdx.search(newL.getEnvelopeInternal());

		Iterator iter = trianglesOverEnvelopeIdx.iterator();
		while (iter.hasNext()){
			index = (Integer)((Data) iter.next()).getValue(0);
			
			if (triangles.get(index)!=null){
				TriangleDT T = (TriangleDT) triangles.get(index);
				if (!containA&&(T.containsPointAsVertex(line.A))){
					containA = true;
					line.A.z = setZ(line.A,T);
				}
				if (!containB&&(T.containsPointAsVertex(line.B))){
					containB = true;
					line.B.z = setZ(line.B,T);

				}
				if (T.containsLine(newL)){	// test if line intersect triangle
					triangles.set(index, null);
					trianglesToChange.add(T);
				}
				else{	// test if line or vertex is containg in triangle
					if (lineContainsPoint(newL, T.A)&&!T.A.equals2D(line.A)&&!T.A.equals2D(line.B)){
						triangles.set(index, null);
						trianglesToChange.add(T);
					}
					else{
						if (lineContainsPoint(newL, T.B)&&!T.B.equals2D(line.A)&&!T.B.equals2D(line.B)){
							triangles.set(index, null);
							trianglesToChange.add(T);
						}
						else{
							if (lineContainsPoint(newL, T.C)&&!T.C.equals2D(line.A)&&!T.C.equals2D(line.B)){
								triangles.set(index, null);;
								trianglesToChange.add(T);
							}
						}	
					}
					if (T.containsPointAsVertex(line.A)&&T.containsPointAsVertex(line.B)){
						triangles.set(index, null);;
						trianglesToChange.add(T);
					}
				}	
		
			}
		}	
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if ((containA == false)||(containB == false)){
			try{
				int i = triangles.size();
				Iterator iter = trianglesToChange.iterator();
				while(iter.hasNext()){
					
					TriangleDT T = (TriangleDT) iter.next();
					data = new Data(dd);
					data.addValue(i);
					trianglesIdx.insert(T.getEnvelope(), data);
					triangles.add(i, T);				//vlozeni do celkove triangulace
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			return null;
			
		}
		return trianglesToChange;
		
		
	}
	
	/********************************************************************************
	 * Private function sets Z coordinate
	 * @param A - coordinate of fixed line
	 * @param T - triangle with corect elevation
	 * @return value of Z coordinate of triangle
	 */
	private double setZ(Coordinate A, TriangleDT T){
		if (T.A.equals2D(A))
			return T.A.z;
		if (T.B.equals2D(A))
			return T.B.z;
		return T.C.z;
		
		
	}
	
	/********************************************************************************
	 * The private method, which finds out if the line contains the point P
	 * @param line - line for testing
	 * @param PointDT - coordinates of the point P
	 * @return boolean - true : when the line contains the point P
	 *                   false: when the line doesn't contain the point P
	 */		
	
	private boolean lineContainsPoint(LineString line, Coordinate P){
		Coordinate[] newPoint={P}; 
		CoordinateArraySequence newPointP=new CoordinateArraySequence(newPoint);
		Point newP=new Point(newPointP,new GeometryFactory());

		return line.covers(newP);
	}
	
	/*********************************************************************************
	 * The private method for testing, if the LinkedList contains point P
	 * @param LinkedList - List of existing points
	 * @param PointDT - coordinates of the point P
	 * @return boolean - true : when the list contains the point P
	 *                   false: when the list doesn't contain the point P
     */		
	
	private boolean listContainsPoint(LinkedList points, Coordinate P){
		Iterator iter = points.iterator();
		while (iter.hasNext()){		
			if (((Coordinate)iter.next()).equals2D(P))
				return true;
		}
		return false;
	}
	
	/*********************************************************************************
	 * The private method for testing, if the LinkedList contains point P
	 * @param LinkedList - List of existing TIN triangles
	 * @param PointDT A- coordinates of start point of line
	 * @param PointDT B- coordinates of end point of line
	 * @return LinkedList - return List of point, which generate new TIN around hard line
	 * 						without start and end point of hard line
	 */		
	
	private LinkedList getPoints(LinkedList trianglesT, Coordinate A, Coordinate B){
		Iterator iter = trianglesT.iterator();
		LinkedList points = new LinkedList();
		TriangleDT T = (TriangleDT) iter.next();
		points.add(T.A);
		points.add(T.B);
		points.add(T.C);
		while (iter.hasNext()){  //test duplicity bodu v seznamu
			T = (TriangleDT) iter.next();
			if (!listContainsPoint(points,T.A))
				points.add(T.A);
			if (!listContainsPoint(points,T.B))
				points.add(T.B);
			if (!listContainsPoint(points,T.C))
				points.add(T.C);
		}
		//vymazani bodu A a B
		iter = points.iterator();
		Coordinate P;
		while (iter.hasNext()){	//vymazani yacatku a konce linie ze seznamu bodu
			P = (Coordinate)iter.next();
			if (P.equals2D(A)||P.equals2D(B))
					iter.remove();
		}
		return points;
	}

	
	/*************************************************************************************************
	 * The private method for testing, if the old triangles contains new triangle
	 * @param TriangleDT - triangle for test
	 * @param LinkedList - List of triangles of old triangulation,which will be deleted
	 * @return boolean - true : when the triangles contains the new triangle
	 *                   false: when the triangles don't contains the new triangle
	 */		
	private boolean testIsInside(TriangleDT T, LinkedList trians){
		Iterator iter = trians.iterator();
		while (iter.hasNext()){			//testovani zda teziste noveho trojuhelnika je uvnitr zrusenych trojuhelniku
			if (((TriangleDT)iter.next()).contains(T.getCentroid())){
				return true;
			}
		}
		return false;
	}
	
	/*************************************************************************************************
	 * The private method gets tringle
	 * @param triangle - triangle
	 * @param pointTriangulated - triangulated points
	 * @return trinagle
	 */		
	protected TriangleDT getTriangle(Triangle triangle, ArrayList<Coordinate> pointsTriangulated){
		Coordinate[] coords = new Coordinate[3];

		for (int i = 0; i < 3; i++) {
			try{
				coords[i] = (Coordinate)pointsTriangulated.get(triangle.ppp[i].i);
			}catch (Exception e){
				return null;
			}
			
		}
		return new TriangleDT(coords);
	}
	
	/************************************************************************************************
	 * The method for creating new triangles around the fixed lines in existing triangulation
	 * @return LinkedList - list of new triangles, which generate TIN	
	 */		
	
	
	public ArrayList countTIN(){
					// list pevnych hran
		
		Iterator iter = fixedLines.iterator();
		ArrayList leftPoints;				// body nad primkou
		ArrayList rightPoints;			// body pod primkou
							// pomocna linie
		LinkedList trianglesToChange;  //nalezene trojuhelniky pres ktere prochazi linie
		LinkedList points; 				// body kterych se budou nove triangulovat
		Triangle[] leftTIN;				// TIN nad primkou 
		Triangle[] rightTIN;			// TIN pod primkou
		Coordinate P;						// pomocny bod
		double alfa;					// uhel, ktery svira pevna hrana s osou x
		double yTrans,xTrans;			// transfomovane souradnice (shodnostni transformace
										// uhel pooteceni alfa, posunuti 0 v x-ove i v y-ove ose
		dd.addField(Integer.class);
		while (iter.hasNext()){			//cyklus pro pevne hrany
			points = new LinkedList();
			leftTIN = null;
			rightTIN = null;
			leftPoints = new ArrayList();
			rightPoints = new ArrayList();
			line = (LineDT) iter.next();
			trianglesToChange = getTrianglesIntersectLine();	// getting triangles which intersect fixed line
			
			
			if ((trianglesToChange!=null)&&trianglesToChange.size() != 0){
				points = getPoints(trianglesToChange, line.A, line.B);	// getting points from intersect triangles
			
																	// counting angle of turning, identity transformation
				if ((line.B.x - line.A.x)!= 0)
					alfa = -1*(Math.atan((line.B.y-line.A.y)/(line.B.x - line.A.x)));
				else
					alfa = Math.PI/2;
			
				double yTransNullPoints = Math.cos(alfa)*line.A.y + Math.sin(alfa)*line.A.x;	// getting value y which sorting left and righ triangulation
				Iterator it = points.iterator();
				
				while (it.hasNext()){			
					P = (Coordinate)it.next();
					yTrans = Math.cos(alfa)*P.y + Math.sin(alfa)*P.x;		// identity transformation
					if (yTrans>=yTransNullPoints-0.0000001){
						leftPoints.add(new Coordinate(P.x,P.y,P.z));
					}
					if (yTrans<=yTransNullPoints+0.0000001){
						rightPoints.add(new Coordinate(P.x,P.y,P.z));
					}
				}
				
				int i = triangles.size();
				if (!leftPoints.isEmpty()){
					//System.out.println("Line isn't empty");
					leftPoints.add(new Coordinate(line.A.x, line.A.y, line.A.z));
					leftPoints.add(new Coordinate(line.B.x, line.B.y, line.B.z));
					Coordinate[] coordsLeft = new Coordinate[leftPoints.size()];
					it = leftPoints.iterator();
					int index = 0;
					while (it.hasNext()){
						coordsLeft[index] = (Coordinate)it.next();
						index++;
					}
					
					Triangulation triangulation = new Triangulation(coordsLeft, new int[0][0]);
					triangulation.triangulate();
					leftTIN = triangulation.getTriangles();
					
					for (int j=0; j<leftTIN.length; j++){
						TriangleDT T = getTriangle(leftTIN[j], leftPoints);
						if (T!=null && T.isTriangle()){
						if (testIsInside(T, trianglesToChange)){
								try{
									data = new Data(dd);
									data.addValue(i);
									trianglesIdx.insert(T.getEnvelope(), data);
								}
								catch(Exception e){
									e.printStackTrace();
								}
								setTypeOfBreakLine(T);
								
								triangles.add(i, T);				//vlozeni do celkove triangulace
								i++;
							}
						}
					}
				}	
				if (!rightPoints.isEmpty()){
					rightPoints.add(new Coordinate(line.A.x, line.A.y, line.A.z));
					rightPoints.add(new Coordinate(line.B.x, line.B.y, line.B.z));
					Coordinate[] coordsRight = new Coordinate[rightPoints.size()];
					int index = 0;
					it = rightPoints.iterator();
					while (it.hasNext()){
						coordsRight[index] = (Coordinate)it.next();
						index++;
					}
					
	
					
					Triangulation triangulation = new Triangulation(coordsRight, new int[0][0]);
					triangulation.triangulate();
					rightTIN = triangulation.getTriangles();
					for (int j=0; j<rightTIN.length; j++){
						TriangleDT T = getTriangle(rightTIN[j], rightPoints);
						if (T!=null && T.isTriangle()){
							
							if (testIsInside(T, trianglesToChange)){
								try{
									data = new Data(dd);
									data.addValue(i);
									trianglesIdx.insert(T.getEnvelope(), data);
								}
								catch(Exception e){
									e.printStackTrace();
								}
								setTypeOfBreakLine(T);
								
								triangles.add(i, T);				//vlozeni do celkove triangulace
								i++;
							}
						}
					}
				}
			}
		}
		return triangles;
	}
	
	/*************************************************************************************************
	 * The protected method seting attribute type of break line in triangle
	 * @param Triangle - triangle to setse
	 */		
	protected void setTypeOfBreakLine (TriangleDT T){
		T.normalizePolygon();
		Iterator iterFixedLines = fixedLines.iterator();
		while (iterFixedLines.hasNext()){
			
			LineDT line = (LineDT) iterFixedLines.next();
			if (line.isHardBreakLine){
				if ((T.A.equals2D(line.A)&&T.B.equals2D(line.B))||(T.A.equals2D(line.B)&&T.B.equals2D(line.A))){
					//System.out.println("prvni");
					if (!T.haveBreakLine){
						T.typeBreakLine = 0;
						T.haveBreakLine = true;
					}
					else{
						if (T.typeBreakLine == 1){
							T.typeBreakLine = 4;
						}
						else{
							if (T.typeBreakLine == 2){
								T.typeBreakLine = 3;	
							}
							else{
								if (T.typeBreakLine == 5){
									T.typeBreakLine = 6;
								}
							}
						}
					}
				}
				else
					if ((T.B.equals2D(line.A)&&T.C.equals2D(line.B))||(T.C.equals2D(line.A)&&T.B.equals2D(line.B))){
						//	System.out.println("druhy");
						if (!T.haveBreakLine){
							T.typeBreakLine = 1;
							T.haveBreakLine = true;
						}
						else{
							if (T.typeBreakLine == 0){
								T.typeBreakLine = 4;
							}	
							else{
								if (T.typeBreakLine == 2){
									T.typeBreakLine = 5;	
								}
								else{
									if (T.typeBreakLine == 3)
										T.typeBreakLine = 6;
								}
							}	
						}
					}
					else{
						if ((T.A.equals2D(line.A)&&T.C.equals2D(line.B))||(T.C.equals2D(line.A)&&T.A.equals2D(line.B))){
							//	System.out.println("treti");
							if (!T.haveBreakLine){
								T.typeBreakLine = 2;
								T.haveBreakLine = true;
							}
							else{
								if (T.typeBreakLine == 0){
									T.typeBreakLine = 3;
								}
								else{
									if (T.typeBreakLine == 1){
										T.typeBreakLine = 5;	
									}
									else{
										if (T.typeBreakLine == 4)
											T.typeBreakLine = 6;
									}
								}
							}
						}
					}	
				}	
			}
	}
}
