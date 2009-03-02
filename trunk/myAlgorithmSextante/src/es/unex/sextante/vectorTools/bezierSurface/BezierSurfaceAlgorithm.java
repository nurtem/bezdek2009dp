package es.unex.sextante.vectorTools.bezierSurface;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
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

public class BezierSurfaceAlgorithm extends GeoAlgorithm {
	
	public static final String TIN = "TIN";
	public static final String TINB = "TINB";
	public static final String LoD = "LoD";
	
	private IVectorLayer m_Triangles;
	private IVectorLayer m_TrianglesOut;
	private int m_LoD;
	
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
		double maxZValue = Double.NEGATIVE_INFINITY;
		double minZValue = Double.POSITIVE_INFINITY;
		
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

		
		BezierSurface bs = new BezierSurface(triangles);
		Coordinate [][] newTin = bs.getBezierTriangles(m_LoD);

		for (int j = 0; j < newTin.length; j++) {
			Object[] record = {new Integer(j)};
			
			GeometryFactory gf = new GeometryFactory();
			Coordinate[] coords = new Coordinate[4];
			for (int k=0; k<3; k++){
				coords[k] = newTin[j][k];
			}
			coords[3] = newTin[j][0];
			LinearRing ring = gf.createLinearRing(coords);
			m_TrianglesOut.addFeature(gf.createPolygon(ring, null), record);
		}
	
		return !m_Task.isCanceled();
	
		
	}	


}

