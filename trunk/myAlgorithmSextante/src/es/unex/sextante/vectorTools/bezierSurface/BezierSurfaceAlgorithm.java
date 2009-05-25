package es.unex.sextante.vectorTools.bezierSurface;


import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.memory.MemoryPageStore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class BezierSurfaceAlgorithm extends GeoAlgorithm {
	

	private class Triangle{
		int index;
		Coordinate coord[] = new Coordinate[3];
		int typeOfBreakLine;
		Triangle (int index, Coordinate[] coord, int typeOfBreakLine){
			this.index = index;
			this.coord = coord;
			this.typeOfBreakLine = typeOfBreakLine;
		}
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
			System.out.println(typeOfBreakLine);
		}
	}
	
	public static final String TIN = "TIN";
	public static final String TINB = "TINB";
	public static final String LoD = "LoD";
	
	private IVectorLayer m_Triangles;
	private IVectorLayer m_TrianglesOut;
	private int m_LoD;
	
	private Data data;
	private DataDefinition dd = new DataDefinition("US-ASCII"); 
	
	private RTree trianglesIndex;
	Coordinate [][] triangles;
	TreeMap breakLines = new TreeMap();
	Bezier miniBezierTriangles[];
	
	public void defineCharacteristics() {

		setName(Sextante.getText( "TIN - Bezier Surface"));
		setGroup(Sextante.getText("Herramientas_capas_puntos"));
		setGeneratesUserDefinedRasterOutput(false);

		try {
			m_Parameters.addInputVectorLayer(TIN,
											Sextante.getText( "TIN"),
											AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
											true);
		
			m_Parameters.addNumericalValue(LoD,
					Sextante.getText( "Level of Detail"),
					AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
					10,
					0,
					Double.MAX_VALUE);
			
			addOutputVectorLayer(TINB,
											Sextante.getText( "Resultado"),
											OutputVectorLayer.SHAPE_TYPE_POLYGON);
		} catch (Exception e) {
			Sextante.addErrorToLog(e);
		}

	}

	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

		int i;
		int iShapeCount;
		double scaleZ = Double.NEGATIVE_INFINITY;
		
		
		m_Triangles = m_Parameters.getParameterValueAsVectorLayer(TIN);
		m_LoD = m_Parameters.getParameterValueAsInt(LoD);
		
		Class types[] = {Integer.class};
		String sNames[] = {"ID"};
		m_TrianglesOut = getNewVectorLayer(TINB,
				m_Triangles.getName()+"_bezier",
				IVectorLayer.SHAPE_TYPE_POLYGON,
				types,
				sNames);

		
		i = 0;
		iShapeCount = m_Triangles.getShapesCount();
		triangles = new Coordinate[iShapeCount][3];
		
		IFeatureIterator iter = m_Triangles.iterator();
		//List breakLines = new LinkedList();
		try{ 
			dd.addField(Integer.class);
			PageStore ps = new MemoryPageStore(dd);
			trianglesIndex = new RTree(ps);
			while(iter.hasNext()){
				IFeature feature = iter.next();
				Polygon trianglePolygon = (Polygon) feature.getGeometry();
				IRecord record = feature.getRecord();
				//System.out.println(record.getValue(2).toString());
				if (((String)record.getValue(1)) == "Y")
					breakLines.put(i, (Integer)record.getValue(2));
				
				triangles [i] = trianglePolygon.getCoordinates();
				/////////////
				
					System.out.println(triangles[i][0]);
					System.out.println(triangles[i][1]);
					System.out.println(triangles[i][2]);
				
				System.out.println((Integer)breakLines.get(i));
		//////////////////		
				data = new Data(dd);
				data.addValue(i);
				trianglesIndex.insert(trianglePolygon.getEnvelopeInternal(), data);
	
				for (int k=0; k<2; k++){
					double diffZ = triangles[i][k].z - triangles[i][k+1].z;
					double diffXY = Math.sqrt(Math.pow((triangles[i][k].x-triangles[i][k+1].x),2)+
								Math.pow((triangles[i][k].y-triangles[i][k+1].y),2));
				
					if (scaleZ < Math.abs(diffZ/diffXY))
						scaleZ = Math.abs(diffZ/diffXY)*10;
					System.out.println(scaleZ);
				}	
				i++;
			}
			iter.close();
		}
		catch (Exception e){
			e.printStackTrace(); 
		}	
		//scaleZ = 4.2;
		//
		
		for (i = 0; i<triangles.length; i++)
			for (int j=0; j<3;j++)
				triangles[i][j].z /= scaleZ;
		

		
		int indexOfInterpolatedTriangles = 0;
		for (int j = 0; j < triangles.length; j++) {
			System.out.println("Zaciname"+j);
			Bezier2 newBezierTriangles = new Bezier2(triangles[j]);
			newBezierTriangles.setNormalVector(searchVectors(newBezierTriangles,newBezierTriangles.b300, j),
											   searchVectors(newBezierTriangles,newBezierTriangles.b030, j),
											   searchVectors(newBezierTriangles,newBezierTriangles.b003, j)); 
			if (breakLines.containsKey(j))
					newBezierTriangles.setControlPoints((Integer)breakLines.get(j));
			else
					newBezierTriangles.setControlPoints(-1);
			for (int k = 0; k<3; k++){
				//newBezierTriangles.getBezierPatch(k).toStringa();
				Coordinate newTin[][] = getInterpolatedTriangles(newBezierTriangles.getBezierPatch(k));
			
				for (int l = 0; l<newTin.length; l++){
					Object[] record = {new Integer(indexOfInterpolatedTriangles)};
					GeometryFactory gf = new GeometryFactory();
					Coordinate[] coords = new Coordinate[4];
					for (int m=0; m<3; m++){
						coords[m] = newTin[l][m];
						coords[m].z *= scaleZ;
					//	System.out.println(coords[m]);
					//	System.out.println("scale"+scaleZ);
					}
					coords[3] = newTin[l][0];
					//coords[3].z *= scaleZ;
					LinearRing ring = gf.createLinearRing(coords);
					m_TrianglesOut.addFeature(gf.createPolygon(ring, null), record);
					indexOfInterpolatedTriangles++;
				}
			}
		}	
		return !m_Task.isCanceled();
	}
	
	
	public Coordinate[][] getInterpolatedTriangles(Bezier bezierTriangles2){
		Coordinate[][] newTriangles = new Coordinate[(int)Math.pow(m_LoD+1,2)][3];
		int indexOfNewTriangles = 0;
		
			//System.out.println("index"+k);
		//	bezierTriangles2[k].toStringa();
			double [] indexes = new double[m_LoD+2];
			double koeficient = 1/((double)m_LoD+1);
			for (int i = 0; i<=m_LoD+1; i++){
				indexes[i] = koeficient*i;
			//	System.out.println("ooo"+indexes[i]);
			}
			int maxTi = m_LoD+1;
			int maxTj = m_LoD;
			for (int i = 0; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					newTriangles[indexOfNewTriangles][0] = bezierTriangles2.getElevation(indexes[i], indexes[j]);
					newTriangles[indexOfNewTriangles][1] = bezierTriangles2.getElevation(indexes[i], indexes[j+1]);
		            newTriangles[indexOfNewTriangles++][2] = bezierTriangles2.getElevation(indexes[i+1], indexes[j]);
					//TTT.toStringa();
	//				System.out.println("Troju"+ indexes[i]+","+ indexes[j]+"  "+ indexes[i]+","+  indexes[j+1]+"  "+ indexes[i+1]+","+  indexes[j]);
	//				System.out.println();				
				}
				maxTj --;
			}
	//		System.out.println("hotovo");
			maxTj = m_LoD-1;
			for (int i = 1; i<=maxTi;i++){
				for (int j = 0; j<=maxTj;j++){
					newTriangles[indexOfNewTriangles][0] = bezierTriangles2.getElevation(indexes[i], indexes[j]);
					newTriangles[indexOfNewTriangles][1] = bezierTriangles2.getElevation(indexes[i], indexes[j+1]);
					newTriangles[indexOfNewTriangles++][2] = bezierTriangles2.getElevation(indexes[i-1], indexes[j+1]);
					//trianglesDTBezier.insertToTree(TTT, TTT.key);
	//				System.out.println("Troju"+ indexes[i]+","+ indexes[j]+"  "+ indexes[i]+","+  indexes[j+1]+"  "+ indexes[j+1]+","+  indexes[i-1]);
	//				System.out.println();				
				}
				maxTj --;
			}
//			
		
	//	System.out.println(indexOfNewTriangles+" POCTY NOVEJCH  "+newTriangles.length);
		return newTriangles;
	}
	


		/*************************************************************************
	 * The method searchs normals vector for vertex P of triangle T
	 * @param T - triangle
	 * @param P - vertex of triangle
	 * @return - linked list of vectors
	 */
	private LinkedList searchVectors(Bezier2 bezierT, Coordinate P, int indexOfBezierT){
		//System.out.println("PRO BOD> "+P.toString());
		LinkedList vectors = new LinkedList();
		//LinkedList points = new LinkedList();
		//searsching of triangles by envelope P
		
		List listOfTrianglesIndex = null;
		try{
			listOfTrianglesIndex  = trianglesIndex.search(new Envelope(P));
		}
		catch(Exception e){
			e.printStackTrace();
		}	

		Iterator iterTrianglesIndex = listOfTrianglesIndex.iterator();
	

		
		//Iterator iterBreakLines = breakLines.iterator();
	

		
		//	System.out.println("SIYE / vypisuju trojuhelniky " +listOfTrianglesIndex.size());
		
	//	System.out.println("POINT"+P.toString());
		boolean haveBreakLine = false;
		boolean testingBreakLine = true;
		while (iterTrianglesIndex.hasNext()){
			int index = (Integer)((Data)iterTrianglesIndex.next()).getValue(0);
			Coordinate[] TT = triangles[index];
			//TT.toStringa();
			
			switch (compareReturnIndex(TT, P)){
				case 'A':{
							if (testingBreakLine)
								haveBreakLine = testOfBreakLine('A', index);
					//		System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
							//TT.toStringa();
							Coordinate v1 = Bezier2.setVector(P,TT[1]);
							Coordinate v2 = Bezier2.setVector(P,TT[2]);
							double scalar = Bezier2.countScalarProduct(v1,v2);
							double alfa = Math.acos(scalar/(Bezier2.countScalarProduct(v1,v1)*Bezier2.countScalarProduct(v2,v2)));
							//sumAlfa += alfa;
		//					System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+alfa);
							Coordinate normal = Bezier2.setNormalVector(v1,v2);
							vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
						
							break;
						}
						case 'B':{
							if (testingBreakLine)
								haveBreakLine = testOfBreakLine('B', index);
						//	System.out.println("Jsem v BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
				//			TT.toStringa();
							Coordinate v1 = Bezier2.setVector(P,TT[0]);
							Coordinate v2 = Bezier2.setVector(P,TT[2]);
							double scalar = Bezier2.countScalarProduct(v1,v2);
							double alfa = Math.acos(scalar/(Bezier2.countScalarProduct(v1,v1)*Bezier2.countScalarProduct(v2,v2)));
							//sumAlfa += alfa;
			//				System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+alfa);
							Coordinate normal = Bezier2.setNormalVector(v1,v2);
							vectors.add(new Coordinate(normal.x*alfa, normal.y*alfa, normal.z*alfa));
							break;
						}
						case 'C':{
							if (testingBreakLine)
								haveBreakLine = testOfBreakLine('C', index);
						//	System.out.println("Jsem v CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
					//		TT.toStringa();
							Coordinate v1 = Bezier2.setVector(P,TT[0]);
							Coordinate v2 = Bezier2.setVector(P,TT[1]);
					//		System.out.println(v1);
					//		System.out.println(v2);
							double scalar = Bezier2.countScalarProduct(v1,v2);
							double alfa = Math.acos(scalar/(Bezier2.countScalarProduct(v1,v1)*Bezier2.countScalarProduct(v2,v2)));
				//			//sumA += alfa;
					//		System.out.println("Jsem v AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+alfa);
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
		//System.out.println("LIST"+vectors.size());
		return vectors;
		
	}
	
	
	protected LinkedList setCorectTrianglesIndex(List listOfTrianglesIndex, Bezier2 bezierT, Coordinate P, int indexOfBezierT){
		System.out.println("jsem v Ceorect+" +indexOfBezierT);
		TreeMap allTriangles = new TreeMap();
		TreeMap newTriangles = new TreeMap();
		Iterator iterOfTrianglesIndex = listOfTrianglesIndex.iterator();
		int typeOfBreakLine;
		while (iterOfTrianglesIndex.hasNext()){
			Data data = (Data)iterOfTrianglesIndex.next();
			int index = (Integer)(data).getValue(0);
			Coordinate[] TT = triangles[index];
			Object typeOfBreakL = breakLines.get(index);
			if (typeOfBreakL!=null)
				typeOfBreakLine = ((Integer)typeOfBreakL).intValue();
			else
				typeOfBreakLine = -1;
			
			switch (compareReturnIndex(TT, P)){
				case 'A':{
					System.out.println("A");
					allTriangles.put(index, new Triangle(index,TT, typeOfBreakLine));
					((Triangle)(allTriangles.get(index))).toStringa();
					break;
				}	
				case 'B':{
					System.out.println("B");
					if (typeOfBreakLine != -1){
						if ((typeOfBreakLine < 3))
							typeOfBreakLine = (typeOfBreakLine+2)%3;
						else
							if (typeOfBreakLine != 6){
								typeOfBreakLine = (typeOfBreakLine+2)%3 + 3;
							}
					}
					allTriangles.put(index, new Triangle(index, TT[1], TT[2], TT[0], typeOfBreakLine));
					((Triangle)(allTriangles.get(index))).toStringa();
					break;
				}
				case 'C':{
					System.out.println("C");
					if (typeOfBreakLine != -1){
						if (typeOfBreakLine < 3)
							typeOfBreakLine = (typeOfBreakLine+1)%3;
						else
							if (typeOfBreakLine != 6){
								typeOfBreakLine = (typeOfBreakLine+1)%3 + 3;
							}
					}
					allTriangles.put(index, new Triangle(index,TT[2], TT[0], TT[1], typeOfBreakLine));
					((Triangle)(allTriangles.get(index))).toStringa();
					break;
				}
			}
		}
		Triangle T = (Triangle)allTriangles.get(indexOfBezierT);
		System.out.println("ZaCINAME ---------------");
		T.toStringa();
		
		newTriangles.put(indexOfBezierT, T);
		allTriangles.remove(indexOfBezierT);
		
		//TEST OF TRIANGLES ON THE RIGHT SIDE
		Triangle rightT = T;
		boolean change = true;
		while ((rightT.typeOfBreakLine != 6)&&(rightT.typeOfBreakLine != 3)&&(rightT.typeOfBreakLine != 2)&&(rightT.typeOfBreakLine != 5)&&!allTriangles.isEmpty()&&change){
			System.out.println("RIGHT");
			rightT.toStringa();
			change = false;
			Iterator iterAllTriangles = allTriangles.values().iterator();
			while (iterAllTriangles.hasNext()){
				T = (Triangle)iterAllTriangles.next();
				T.toStringa();
				if (rightT.coord[2].equals2D(T.coord[1])){
					change = true;
					newTriangles.put(T.index, T);
					allTriangles.remove(T.index);
					rightT = T;
					System.out.println("MAMHO");
					break;
				}
			}
		}
		//TEST OF TRIANGLES ON THE LEFT SIDE
		Triangle leftT = (Triangle)newTriangles.get(indexOfBezierT);
		change = true;
		while ((leftT.typeOfBreakLine != 6)&&(leftT.typeOfBreakLine != 3)&&(leftT.typeOfBreakLine != 0)&&(leftT.typeOfBreakLine != 4)&&!allTriangles.isEmpty()&&change){
			System.out.println("LEFT");
			leftT.toStringa();
			change = false;
			Iterator iterAllTriangles = allTriangles.values().iterator();
			while (iterAllTriangles.hasNext()){
				T = (Triangle)iterAllTriangles.next();
				T.toStringa();
				if (leftT.coord[1].equals2D(T.coord[2])){
					change = true;
					newTriangles.put(T.index, T);
					allTriangles.remove(T.index);
					leftT = T;
					System.out.println("MAMHO");
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
	
	protected boolean testOfBreakLine(char vertex, int indexTT){
		if (breakLines.containsKey(indexTT)){
			//Iterator iterBreakLines = breakLines.iterator();
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

