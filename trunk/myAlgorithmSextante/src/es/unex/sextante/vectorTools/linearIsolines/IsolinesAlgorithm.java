package es.unex.sextante.vectorTools.linearIsolines;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class IsolinesAlgorithm extends GeoAlgorithm {
	
	public static final String TIN = "TIN";
	public static final String ISOLINES = "ISOLINES";
	public static final String EQUIDISTANCE = "EQUIDISTANCE";
	
	private IVectorLayer m_Triangles;
	private IVectorLayer m_Isolines;
	private double m_EquiDistance;
	
	public void defineCharacteristics() {

		setName(Sextante.getText( "TIN - extracting isolines"));
		setGroup(Sextante.getText("Herramientas_capas_puntos"));
		setGeneratesUserDefinedRasterOutput(false);

		try {
			m_Parameters.addInputVectorLayer(TIN,
											Sextante.getText( "TIN"),
											AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
											true);
		
			m_Parameters.addNumericalValue(EQUIDISTANCE,
					Sextante.getText( "Equidistance"),
					AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
					10,
					0,
					Double.MAX_VALUE);
			
			addOutputVectorLayer(ISOLINES,
											Sextante.getText( "Resultado"),
											OutputVectorLayer.SHAPE_TYPE_POLYGON);
		} catch (Exception e) {
			Sextante.addErrorToLog(e);
		}

	}

	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

		int i;
		int iShapeCount;
		double maxZValue = Double.NEGATIVE_INFINITY;
		double minZValue = Double.POSITIVE_INFINITY;
		
		m_Triangles = m_Parameters.getParameterValueAsVectorLayer(TIN);
		m_EquiDistance = m_Parameters.getParameterValueAsDouble(EQUIDISTANCE);
		
		Class types[] = {Integer.class, Double.class};
		String sNames[] = {"ID","Value"};
		m_Isolines = getNewVectorLayer(ISOLINES,
										m_Triangles.getName()+"_Isolines",
										IVectorLayer.SHAPE_TYPE_POLYGON,
										types,
										sNames);

		
		i = 0;
		iShapeCount = m_Triangles.getShapesCount();
		Coordinate [][] triangles = new Coordinate[iShapeCount][3];
		
		IFeatureIterator iter = m_Triangles.iterator();
		try{ 
			while(iter.hasNext()){
				IFeature feature = iter.next();
				Polygon trianglePolygon = (Polygon) feature.getGeometry();
				triangles [i] = trianglePolygon.getCoordinates();
				for (int j=0; j<3; j++){
					if (triangles[i][j].z>maxZValue)
						maxZValue = triangles[i][j].z;
					if (triangles[i][j].z<minZValue)
						minZValue = triangles[i][j].z;
				}
				i++;
			}
			iter.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}	
	
			//double elevatedStep = 1;	
			ArrayList isolines = LinearContourLines.countIsolines(triangles,m_EquiDistance, minZValue, maxZValue);
			Iterator iterIso = isolines.iterator();
			int j = 0;
			
			for (int l=0; l < isolines.size(); l++){
				Object o = isolines.get(l);
				if (o != null){
					
					//	triangles.getTriangle(j).toStringa();
					GeometryFactory gf = new GeometryFactory();
					Iterator isoL = ((LinkedList)o).iterator();
					Coordinate[] coords = new Coordinate[((LinkedList)o).size()];
					int k = 0;
					while(isoL.hasNext()){
						coords[k] = (Coordinate)isoL.next();
						k++;
					}	
					Object[] record = {new Integer(j),coords[0].z};
					LineString isoline = gf.createLineString(coords);
					m_Isolines.addFeature(isoline, record);
					j++;
							
				}
			}
			
		
			System.out.println("AHOJ");
		return !m_Task.isCanceled();

		
	}	


}

