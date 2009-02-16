package es.unex.sextante.vectorTools.linearIsolines;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;
import org.geotools.delaunay.contourlines.LinearContourLines;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class IsolinesAlgorithm extends GeoAlgorithm {
	
	public static final String TRIANGLES = "TRIANGLES";
	public static final String ISOLINES = "ISOLINES";
	
	
	private IVectorLayer m_Triangles;
	private IVectorLayer m_Isolines;

	
	public void defineCharacteristics() {

		setName(Sextante.getText( "TIN extracting isolines"));
		setGroup(Sextante.getText("Herramientas_capas_puntos"));
		setGeneratesUserDefinedRasterOutput(false);

		try {
			m_Parameters.addInputVectorLayer(TRIANGLES,
											Sextante.getText( "TIN"),
											AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
											true);
			
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

		m_Triangles = m_Parameters.getParameterValueAsVectorLayer(TRIANGLES);
		
		Class types[] = {Integer.class, Double.class};
		String sNames[] = {"ID","value"};
		m_Isolines = getNewVectorLayer(ISOLINES,
										m_Triangles.getName()+"_Isolines",
										IVectorLayer.SHAPE_TYPE_POLYGON,
										types,
										sNames);

		
		i = 0;
		iShapeCount = m_Triangles.getShapesCount();
		LinkedList triangles = new LinkedList();
		
		IFeatureIterator iter = m_Triangles.iterator();
		try{ 
			while(iter.hasNext()){
				IFeature feature = iter.next();
				Polygon trianglePolygon = (Polygon) feature.getGeometry();
				Coordinate[] coords = trianglePolygon.getCoordinates();
				TriangleDT triangle = new TriangleDT(new PointDT(coords[0].x,coords[0].y,coords[0].z),
						new PointDT(coords[1].x,coords[1].y,coords[1].z),
						new PointDT(coords[2].x,coords[2].y,coords[2].z));
			//	System.out.println(i);
			//	triangle.toStringa();
				triangles.add(i, triangle);
				i++;
			}
			iter.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}	
	
			double elevatedStep = 10;	
			ArrayList isolines = LinearContourLines.countIsolines(triangles,elevatedStep);
			Iterator iterIso = isolines.iterator();
			int j = 0;
			
			for (int l=0; l < isolines.size(); l++){
				Object o = isolines.get(l);
				if (o != null){
					Object[] record = {new Integer(j),1D};
					//	triangles.getTriangle(j).toStringa();
					GeometryFactory gf = new GeometryFactory();
					Iterator isoL = ((LinkedList)o).iterator();
					Coordinate[] coords = new Coordinate[((LinkedList)o).size()];
					
					int k = 0;
					while(isoL.hasNext()){
						coords[k] = (Coordinate)isoL.next();
						k++;
					}	
					LineString isoline = gf.createLineString(coords);
					m_Isolines.addFeature(isoline, record);
					j++;
							
				}
			}
			
		
			System.out.println("AHOJ");
		return !m_Task.isCanceled();

		
	}	


}

