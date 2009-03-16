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

package es.unex.sextante.vectorTools.bezierSurface;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.memory.MemoryPageStore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class BezierSurface {
	Coordinate[][] triangles;
	Bezier3[] bezierTriangles;
	RTree trianglesIndex;
	//int LoL;
	Data data;
	DataDefinition dd = new DataDefinition("US-ASCII"); 
	
	public BezierSurface(Coordinate[][] triangles){
		//this.LoL = LoL;
		this.triangles = triangles;
		bezierTriangles = new Bezier3[triangles.length];
		createTrianglesIndex();
		createSurface();
	}
	//static DelaunayDataStore trianglesDToriginal;
	
	/******************************************************************
	 * The method for setting normal vectors of two vectors
	 * @param A - vector A
	 * @param B - vector B
	 * @return normal vector
	 */
	protected Coordinate setNormalVector(Coordinate A, Coordinate B){
		Coordinate normal = new Coordinate(A.y*B.z-A.z*B.y, A.z*B.x-A.x*B.z, (A.x*B.y-A.y*B.x));
		//double sum = Math.sqrt(Math.pow(normal.x,2)+Math.pow(normal.y, 2)+Math.pow(normal.z, 2));
		double sum = 1;
		if (normal.z>0)
			return new Coordinate((normal.x/sum), (normal.y/sum), (normal.z/sum));
		else
			return new Coordinate((-1)*(normal.x/sum), (-1)*(normal.y/sum), (-1)*(normal.z/sum));
	}
	
	private void setNeighbour(Bezier3 T){
		Coordinate[] P = {new Coordinate ((T.P004.x+T.P040.x)/2,(T.P004.y+T.P040.y)/2),
							new Coordinate ((T.P040.x+T.P400.x)/2,(T.P040.y+T.P400.y)/2),
							new Coordinate ((T.P400.x+T.P004.x)/2,(T.P400.y+T.P004.y)/2)};
		Coordinate[] PT = {new Coordinate (T.P004), new Coordinate (T.P040), new Coordinate (T.P400), new Coordinate (T.P004)};


		
		for (int i=0; i<3; i++){
			List listOfTrianglesIndex = null;
			try{
				listOfTrianglesIndex  = trianglesIndex.search(new Envelope(P[i]));
			}
			catch(Exception e){
				e.printStackTrace();
			}	
			System.out.println("VELIKOST====================================================="+listOfTrianglesIndex.size());
			System.out.println("pro bod"+ P[i]);
			Iterator iterTrianglesIndex = listOfTrianglesIndex.iterator();
			while (iterTrianglesIndex.hasNext()){
				Bezier3 TT = bezierTriangles[(Integer)((Data)iterTrianglesIndex.next()).getValue(0)];
				if (!TT.compare(T)){
					if (TT.containTwoPoints(PT[i], PT[i+1])){
						T.neighbour[i][0] = TT.P004;
						T.neighbour[i][1] = TT.P040;
						T.neighbour[i][2] = TT.P400;
					}
				}		
			}
		}			
	}
	
	/*************************************************************************
	 * The method searchs normals vector for vertex P of triangle T
	 * @param T - triangle
	 * @param P - vertex of triangle
	 * @return - linked list of vectors
	 */
	private Coordinate setNormalVector(Bezier3 bezierT, Coordinate P){
		//System.out.println("PRO BOD> "+P.toString());
		LinkedList vectors = new LinkedList();
	//	LinkedList points = new LinkedList();
		//searsching of triangles by envelope P
		
		List listOfTrianglesIndex = null;
		try{
			listOfTrianglesIndex  = trianglesIndex.search(new Envelope(P));
			//setNeighbour(bezierT, listOfTrianglesIndex);
		}
		catch(Exception e){
			e.printStackTrace();
		}	

		
		Iterator iterTrianglesIndex = listOfTrianglesIndex.iterator();
	//	System.out.println("SIYE / vypisuju trojuhelniky " +listOfTrianglesIndex.size());
		
		double sumAlfa = 0;
		//System.out.println("POINT---------------------------------------------------------------------"+P.toString());
		while (iterTrianglesIndex.hasNext()){
			Bezier3 TT = bezierTriangles[(Integer)((Data)iterTrianglesIndex.next()).getValue(0)];
		//	TT.toStringa();
			char index = TT.compareReturnIndex(P);
			switch (index){
				case 'A':{
			//		System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
					//TT.toStringa();
					Coordinate v1 = setVector(P,TT.P040);
					Coordinate v2 = setVector(P,TT.P400);
					double scalar = countScalarProduct(v1,v2);
					double alfa = Math.acos(scalar/(countScalarProduct(v1,v1)*countScalarProduct(v2,v2)));
					sumAlfa += alfa;
					System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+alfa);
					Coordinate normal = setNormalVector(v1,v2);
					vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
				
					break;
				}
				case 'B':{
				//	System.out.println("Jsem v BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		//			TT.toStringa();
					Coordinate v1 = setVector(P,TT.P400);
					Coordinate v2 = setVector(P,TT.P004);
					double scalar = countScalarProduct(v1,v2);
					double alfa = Math.acos(scalar/(countScalarProduct(v1,v1)*countScalarProduct(v2,v2)));
					sumAlfa += alfa;
					System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+alfa);
					Coordinate normal = setNormalVector(v1,v2);
					vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
					break;
				}
				case 'C':{
				//	System.out.println("Jsem v CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
			//		TT.toStringa();
					Coordinate v1 = setVector(P,TT.P040);
					Coordinate v2 = setVector(P,TT.P004);
			//		System.out.println(v1);
			//		System.out.println(v2);
					double scalar = countScalarProduct(v1,v2);
					double alfa = Math.acos(scalar/(countScalarProduct(v1,v1)*countScalarProduct(v2,v2)));
					sumAlfa += alfa;
					System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+alfa);
					Coordinate normal = setNormalVector(v1,v2);
					vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
				}	
			}
		}

		Iterator iter = vectors.iterator();
		double koeficient = 1D;
		//	System.out.println("Normaly rovin");
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
		//double sum = Math.sqrt(Math.pow(sumX, 2)+Math.pow(sumY, 2) + Math.pow(sumZ, 2));
		//Coordinate norm = new Coordinate((sumX/vectors.size())*koeficient, (sumY/vectors.size())*koeficient, (sumZ/vectors.size())*koeficient);
		
		Coordinate norm = new Coordinate(sumX*koeficient, sumY*koeficient, sumZ*koeficient);
		double sum = Math.sqrt(Math.pow(norm.x, 2)+Math.pow(norm.y, 2) + Math.pow(norm.z, 2));
		//double sum = 1;
		return  new Coordinate(norm.x/sum, norm.y/sum, norm.z/sum);
		//return new PointDT((sumX), (sumY), (sumZ));
		//System.out.println("LIST"+vectors.size());
				
	}
	
	protected double countScalarProduct(Coordinate v1,Coordinate v2){
		double scalar =  v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
		//System.out.println(scalar);
		return scalar;
	}
	
	
	
	/*********************************************************************
	 * The method which sets new vecter between points A and B
	 * @param A - start point
	 * @param B - stop point
	 * @return vector AB
	 */ 
	protected static Coordinate setVector(Coordinate A, Coordinate B){
		return new Coordinate(B.x-A.x,B.y-A.y,B.z-A.z);
		
	}

	private void createTrianglesIndex(){
		
		dd.addField(Integer.class);
		try{ 
			PageStore ps = new MemoryPageStore(dd);
			trianglesIndex = new RTree(ps);
			for (int i=0; i<triangles.length; i++){
				bezierTriangles[i] = new Bezier3(triangles[i]);
				//bezierTriangles[i].toStringa();
				data = new Data(dd);
				data.addValue(i);
				trianglesIndex.insert(bezierTriangles[i].getEnvelope(), data);
	        }
		}
		catch (Exception e){
			e.printStackTrace();
		}	
		triangles = null;
		
	}
	
	public Coordinate[][] getBezierTriangles(int LoL){
		Coordinate[][] newTriangles = new Coordinate[bezierTriangles.length * (int)Math.pow(LoL+1,2)][3];
		int indexOfNewTriangles = 0;
		
		for (int k = 0; k<bezierTriangles.length; k++){
			System.out.println("index BEZIER TRIANGLES"+k);
			bezierTriangles[k].toStringa();
			double [] indexes = new double[LoL+2];
			double koeficient = 1/((double)LoL+1);
			for (int i = 0; i<=LoL+1; i++){
				indexes[i] = koeficient*i;
			//	System.out.println("ooo"+indexes[i]);
			}
			int maxTi = LoL+1;
			int maxTj = LoL;
			for (int i = 0; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					newTriangles[indexOfNewTriangles][0] = bezierTriangles[k].getElevation(indexes[i], indexes[j]);
					newTriangles[indexOfNewTriangles][1] = bezierTriangles[k].getElevation(indexes[i], indexes[j+1]);
		            newTriangles[indexOfNewTriangles++][2] = bezierTriangles[k].getElevation(indexes[i+1], indexes[j]);
					//TTT.toStringa();
	//				System.out.println("Troju"+ indexes[i]+","+ indexes[j]+"  "+ indexes[i]+","+  indexes[j+1]+"  "+ indexes[i+1]+","+  indexes[j]);
	//				System.out.println();				
				}
				maxTj --;
			}
	//		System.out.println("hotovo");
			maxTj = LoL-1;
			for (int i = 1; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					newTriangles[indexOfNewTriangles][0] = bezierTriangles[k].getElevation(indexes[i], indexes[j]);
					newTriangles[indexOfNewTriangles][1] = bezierTriangles[k].getElevation(indexes[i], indexes[j+1]);
					newTriangles[indexOfNewTriangles++][2] = bezierTriangles[k].getElevation(indexes[i-1], indexes[j+1]);
					//trianglesDTBezier.insertToTree(TTT, TTT.key);
	//				System.out.println("Troju"+ indexes[i]+","+ indexes[j]+"  "+ indexes[i]+","+  indexes[j+1]+"  "+ indexes[j+1]+","+  indexes[i-1]);
	//				System.out.println();				
				}
				maxTj --;
			}
//			
		}
	//	System.out.println(indexOfNewTriangles+" POCTY NOVEJCH  "+newTriangles.length);
		return newTriangles;
	}
	
	
	/*********************************************************************
	 * Public method for determining new small triangles of original TIN
	 * @param trianglesDTBezier - new triangles will be saved here
	 * @param trianglesDT - original TIN
	 * @param indexDensity - index of density (level of smoothing)
	 */
	public void createSurface(){
		
	
		//int numberOfTriangles = trianglesDTBezier.size();
		for (int trianglesIndex=0; trianglesIndex<bezierTriangles.length; trianglesIndex++){
			System.out.println("/////////////////////////index"+trianglesIndex);
			//TriangleDT T = new TriangleDT(trianglesDToriginal.getTriangle(index));
			//bezierTriangles[trianglesIndex].toStringa();
		//	System.out.println(searchVectors(bezierTriangles[trianglesIndex],bezierTriangles[trianglesIndex].b300));
		//	System.out.println(searchVectors(bezierTriangles[trianglesIndex],bezierTriangles[trianglesIndex].b030));
		//	System.out.println(searchVectors(bezierTriangles[trianglesIndex],bezierTriangles[trianglesIndex].b003));
			
		//	System.out.println(" TROJUHELNIK   ");
			bezierTriangles[trianglesIndex].normalN0 = setNormalVector(bezierTriangles[trianglesIndex],bezierTriangles[trianglesIndex].P004);
			bezierTriangles[trianglesIndex].normalN1 = setNormalVector(bezierTriangles[trianglesIndex],bezierTriangles[trianglesIndex].P040);
			bezierTriangles[trianglesIndex].normalN2 = setNormalVector(bezierTriangles[trianglesIndex],bezierTriangles[trianglesIndex].P400);
			bezierTriangles[trianglesIndex].setControlPoints();
			setNeighbour(bezierTriangles[trianglesIndex]);
			bezierTriangles[trianglesIndex].toStringa();
		}
		
		
		
		
		
	//	for (int index=0; index <= numberOfTriangles; index++){
	//		triangles.delete(T.key);
	//	}
	//	System.out.println(".............................."+trianglesDTBezier.getNumberOfTriangles());
	//	return trianglesDTBezier;
	}
}
