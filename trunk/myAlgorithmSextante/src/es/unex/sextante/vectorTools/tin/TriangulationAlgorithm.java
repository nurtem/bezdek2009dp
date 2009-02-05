package es.unex.sextante.vectorTools.tin;
import java.util.Iterator;
import java.util.LinkedList;

import org.geotools.delaunay.DelaunayDataStore;
import org.geotools.delaunay.DelaunayDataStoreRAM;
import org.geotools.delaunay.IncrementalDT;
import org.geotools.delaunay.TriangleDT;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;



public class TriangulationAlgorithm extends GeoAlgorithm {

	public static final String POINTS = "POINTS";
	public static final String TRIANGLES = "TRIANGLES";

	private IVectorLayer m_Points;
	private IVectorLayer m_Triangles;
	private Coordinate[] m_Coords;

	public void defineCharacteristics() {

		setName(Sextante.getText( "Delaunay Triangulation BEZDA"));
		setGroup(Sextante.getText("Herramientas_capas_puntos"));
		setGeneratesUserDefinedRasterOutput(false);

		try {
			m_Parameters.addInputVectorLayer(POINTS,
											Sextante.getText( "Capa_de_puntos"),
											AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
											true);

			addOutputVectorLayer(TRIANGLES,
											Sextante.getText( "Resultado"),
											OutputVectorLayer.SHAPE_TYPE_POINT);
		} catch (RepeatedParameterNameException e) {
			Sextante.addErrorToLog(e);
		}

	}

	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

		int i;
		int iShapeCount;

		m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);

		Class types[] = {Integer.class};
		String sNames[] = {"ID"};
		m_Triangles = getNewVectorLayer(TRIANGLES,
										m_Points.getName() + "[" + Sextante.getText("triangulado") + "]",
										IVectorLayer.SHAPE_TYPE_POLYGON,
										types,
										sNames);

		i = 0;

		iShapeCount = m_Points.getShapesCount();
		m_Coords = new Coordinate[iShapeCount];
		IFeatureIterator iter = m_Points.iterator();
	
	
		 
		DelaunayDataStore triangles = new DelaunayDataStoreRAM();
		 
		 
		 
		 IncrementalDT triangulace=new IncrementalDT(triangles);
			 
		 
		while(iter.hasNext() && setProgress(i, iShapeCount)){
			IFeature feature = iter.next();
			triangulace.insertPoint(feature.getGeometry().getCoordinate());
			System.out.println(feature.getGeometry().getCoordinate());
		}
		iter.close();

		for (int j=0; j<triangles.getNumberOfTriangles(); j++){
		//	System.out.println(j);
			Object[] record = {new Integer(j)};
		//	triangles.getTriangle(j).toStringa();
			Geometry triangle = getPolygon(triangles.getTriangle(j));
			if (triangle != null){
				m_Triangles.addFeature(triangle, record);
			}
		}
		System.out.println("AHOJ");
		return !m_Task.isCanceled();

	}

	private Geometry getPolygon(TriangleDT triangle) {

		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coords = new Coordinate[4];
		//triangle.toStringa();
		coords[0] = (Coordinate) triangle.A;
		coords[1] = (Coordinate) triangle.B;
		coords[2] = (Coordinate) triangle.C;
		coords[3] = (Coordinate) triangle.A;
				
		LinearRing ring = gf.createLinearRing(coords);
		return gf.createPolygon(ring, null);

	}

}
