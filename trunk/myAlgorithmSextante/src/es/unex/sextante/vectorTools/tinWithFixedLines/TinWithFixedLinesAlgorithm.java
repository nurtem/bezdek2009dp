package es.unex.sextante.vectorTools.tinWithFixedLines;

import java.util.Collection;
import java.util.LinkedList;

import org.geotools.delaunay.DelaunayDataStore;
import org.geotools.delaunay.LineDT;
import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;
import org.geotools.delaunay.fixedlines.TINWithFixedLines;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class TinWithFixedLinesAlgorithm extends GeoAlgorithm {
	
	public static final String HARDLINES = "HARDLINES";
	public static final String TRIANGLES = "TRIANGLES";
	public static final String SOFTLINES = "SOFTLINES";
	public static final String SOFTLINES_B = "SOFTLINES_B";
	public static final String HARDLINES_B = "HARDLINES_B";
	
	
	private IVectorLayer m_Triangles;
	private IVectorLayer m_HardLines;
	private IVectorLayer m_SoftLines;
	private DelaunayDataStore triangles;
	private int m_iClass;
	private boolean m_useHardLines;
	private boolean m_useSoftLines;
	private LinkedList breakLines = new LinkedList();
	
	public void defineCharacteristics() {

		setName(Sextante.getText( "TIN modify by fixed lines"));
		setGroup(Sextante.getText("Herramientas_capas_puntos"));
		setGeneratesUserDefinedRasterOutput(false);

		try {
			m_Parameters.addInputVectorLayer(TRIANGLES,
											Sextante.getText( "TIN"),
											AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
											true);

			m_Parameters.addInputVectorLayer(HARDLINES,
					Sextante.getText( "Hard break lines:"),
					AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
					true);

			m_Parameters.addInputVectorLayer(SOFTLINES,
					Sextante.getText( "Soft break lines"),
					AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
					true);

			m_Parameters.addBoolean(HARDLINES_B,
					Sextante.getText( "Use hardlines for correct TIN"),
					false);

			m_Parameters.addBoolean(SOFTLINES_B,
					Sextante.getText( "Use softlines for correct TIN"),
					false);

			
			addOutputVectorLayer(TRIANGLES,
											Sextante.getText( "Resultado"),
											OutputVectorLayer.SHAPE_TYPE_POLYGON);
		} catch (Exception e) {
			Sextante.addErrorToLog(e);
		}

	}

	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

		int i;
		int iShapeCount;

		m_Triangles = m_Parameters.getParameterValueAsVectorLayer(TRIANGLES);
		
		m_useSoftLines = m_Parameters.getParameterValueAsBoolean(SOFTLINES_B);
		m_useHardLines = m_Parameters.getParameterValueAsBoolean(HARDLINES_B);
		
		
		Class types[] = {Integer.class,char.class,byte.class};
		String sNames[] = {"ID","BreakLines","Type"};
		m_Triangles = getNewVectorLayer(TRIANGLES,
										m_Triangles.getName()+"_modified",
										IVectorLayer.SHAPE_TYPE_POLYGON,
										types,
										sNames);

		
		i = 0;
		iShapeCount = m_Triangles.getShapesCount();
		IFeatureIterator iter = m_Triangles.iterator();
		while(iter.hasNext() && setProgress(i, iShapeCount)){
			IFeature feature = iter.next();
			LinearRing trianglePolygon = (LinearRing) feature.getGeometry();
			Coordinate[] coords = trianglePolygon.getCoordinates();
			TriangleDT triangle = new TriangleDT(new PointDT(coords[0].x,coords[0].y,coords[0].z),
											new PointDT(coords[1].x,coords[1].y,coords[1].z),
											new PointDT(coords[2].x,coords[2].y,coords[2].z));
			triangles.insertToTree(triangle, triangle.key);
			i++;
		}
		iter.close();
		
		if (m_useSoftLines){
			
			m_SoftLines = m_Parameters.getParameterValueAsVectorLayer(SOFTLINES);
			iShapeCount = m_SoftLines.getShapesCount();
			iter = m_SoftLines.iterator();
			i = 0;	
			
			while(iter.hasNext() && setProgress(i, iShapeCount)){
				IFeature feature = iter.next();
				LineString lineString = (LineString) feature.getGeometry();
				Coordinate[] coords = lineString.getCoordinates();
				LineDT softLine = new LineDT(new PointDT(coords[0].x,coords[0].y,coords[0].z),
					new PointDT(coords[1].x,coords[1].y,coords[1].z), false);
				breakLines.add(softLine);
				i++;
			}
			iter.close();
		}
	
		if (m_useHardLines){
			
			m_HardLines = m_Parameters.getParameterValueAsVectorLayer(HARDLINES);
			iShapeCount = m_HardLines.getShapesCount();
			iter = m_HardLines.iterator();
			i = 0;
			
			while(iter.hasNext() && setProgress(i, iShapeCount)){
				IFeature feature = iter.next();
				LineString lineString = (LineString) feature.getGeometry();
				Coordinate[] coords = lineString.getCoordinates();
				LineDT hardLine = new LineDT(new PointDT(coords[0].x,coords[0].y,coords[0].z),
					new PointDT(coords[1].x,coords[1].y,coords[1].z), true);
				breakLines.add(hardLine);
				i++;
			}
			iter.close();
		}

		if (!(breakLines==null)){
			triangles = TINWithFixedLines.countTIN(triangles,breakLines);
		
		
		
		
			for (int j=0; j<triangles.getNumberOfTriangles(); j++){
			//	System.out.println(j);
				Object[] record = {new Integer(j)};
			//	triangles.getTriangle(j).toStringa();
				Geometry triangle = getPolygon(triangles.getTriangle(j));
				if (triangle != null){
					m_Triangles.addFeature(triangle, record);
				}
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

