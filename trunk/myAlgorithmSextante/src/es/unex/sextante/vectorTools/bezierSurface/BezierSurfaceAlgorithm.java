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
		String[] sDistance = {"0","1","2","3","4","5","6","7","8","9"};
		
		try {
			m_Parameters.addInputVectorLayer(TIN,
											Sextante.getText( "TIN"),
											AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
											true);
		
			m_Parameters.addSelection(LoD,
					Sextante.getText( "   Level of Detail:"),
					sDistance);
			
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
		
		Class types[] = {Integer.class, String.class, Integer.class};
		String sNames[] = {"ID", "HardLines", "type"};
		m_TrianglesOut = getNewVectorLayer(TINB,
				m_Triangles.getName()+"_bezier",
				IVectorLayer.SHAPE_TYPE_POLYGON,
				types,
				sNames);

		
		i = 0;
		iShapeCount = m_Triangles.getShapesCount();
		triangles = new Coordinate[iShapeCount][3];
		IFeatureIterator iter = m_Triangles.iterator();
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
				
				triangles [i][0] = (Coordinate) trianglePolygon.getCoordinates()[0].clone();
				triangles [i][1] = (Coordinate) trianglePolygon.getCoordinates()[1].clone();
				triangles [i][2] = (Coordinate) trianglePolygon.getCoordinates()[2].clone();
				/////////////
				
			//		System.out.println(triangles[i][0]);
			//		System.out.println(triangles[i][1]);
			//		System.out.println(triangles[i][2]);
				
			//	System.out.println((Integer)breakLines.get(i));
		//////////////////		
				data = new Data(dd);
				data.addValue(i);
				trianglesIndex.insert(trianglePolygon.getEnvelopeInternal(), data);
	
				for (int k=0; k<2; k++){
					double diffZ = triangles[i][k].z - triangles[i][k+1].z;
					double diffXY = Math.sqrt(Math.pow((triangles[i][k].x-triangles[i][k+1].x),2)+
								Math.pow((triangles[i][k].y-triangles[i][k+1].y),2));
				
					if (scaleZ < Math.abs(diffZ/diffXY)){
						scaleZ = Math.abs(diffZ/diffXY);
						System.out.println("MERITKO----------------------"+scaleZ);
					}
				}	
				setProgress(i,2*iShapeCount);
				i++;
			}
			iter.close();
		}
		catch (Exception e){
			e.printStackTrace(); 
		}	
		//scaleZ = 4.2;
		//
		m_Triangles = null;
		iter = null;
		BezierSurface bezierSurface = new BezierSurface(triangles, trianglesIndex, breakLines, scaleZ, m_LoD);
		
		
		int indexOfInterpolatedTriangles = 0;
		while (bezierSurface.hasNext()) {
			setProgress(i++,2*iShapeCount);
			Coordinate newTin[][] = bezierSurface.nextTrinagle();
			for (int l = 0; l<newTin.length; l++){
				Object[] record = {new Integer(indexOfInterpolatedTriangles),"",-1};
				GeometryFactory gf = new GeometryFactory();
				Coordinate[] coords = new Coordinate[4];
				for (int m=0; m<3; m++){
					coords[m] = newTin[l][m];
				}
				coords[3] = newTin[l][0];
				LinearRing ring = gf.createLinearRing(coords);
				m_TrianglesOut.addFeature(gf.createPolygon(ring, null), record);
				indexOfInterpolatedTriangles++;
			}
			
		}	
		return !m_Task.isCanceled();
	}
		
}

