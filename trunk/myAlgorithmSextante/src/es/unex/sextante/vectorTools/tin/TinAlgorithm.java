/*******************************************************************************
DelaunayAlgorithm.java
Copyright (C) Victor Olaya

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*******************************************************************************/
package es.unex.sextante.vectorTools.tin;

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
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.vectorTools.delaunay.Triangulation;
import es.unex.sextante.vectorTools.delaunay.Triangulation.Triangle;



public class TinAlgorithm extends GeoAlgorithm {

	public static final String POINTS = "POINTS";
	public static final String TRIANGLES = "TRIANGLES";
	public static final String HEIGHT = "HEIGHT";
	public static final String GEOMETRY_Z = "GEOMETRY_Z";

	private IVectorLayer m_Points;
	private IVectorLayer m_Triangles;
	private Coordinate[] m_Coords;
	private int m_iClass;
	private boolean m_useGeometry_Z;

	public void defineCharacteristics() {

		setName(Sextante.getText( "TIN creator"));
		setGroup(Sextante.getText("Herramientas_capas_puntos"));
		setGeneratesUserDefinedRasterOutput(false);

		try {
			m_Parameters.addInputVectorLayer(POINTS,
											Sextante.getText( "Capa_de_puntos"),
											AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
											true);

			m_Parameters.addTableField(HEIGHT, "Height source:", "POINTS");
	
			m_Parameters.addBoolean(GEOMETRY_Z,
					Sextante.getText( "Use Z value from geometry of point"),
					false);
	
			addOutputVectorLayer(TRIANGLES,
											Sextante.getText( "Resultado"),
											OutputVectorLayer.SHAPE_TYPE_POINT);
		} catch (Exception e) {
			Sextante.addErrorToLog(e);
		}

	}

	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

		int i;
		int iShapeCount;

		m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);
		m_iClass = m_Parameters.getParameterValueAsInt(HEIGHT);
		m_useGeometry_Z = m_Parameters.getParameterValueAsBoolean(GEOMETRY_Z);
		
		
		Class types[] = {Integer.class};
		String sNames[] = {"ID"};
		m_Triangles = getNewVectorLayer(TRIANGLES,
										"TIN_"+m_Points.getName(),
										IVectorLayer.SHAPE_TYPE_POLYGON,
										types,
										sNames);

		i = 0;

		iShapeCount = m_Points.getShapesCount();
		m_Coords = new Coordinate[iShapeCount];
		IFeatureIterator iter = m_Points.iterator();
		
		IFeature feature = iter.next();
		if (m_useGeometry_Z || !(feature.getRecord().getValue(m_iClass).getClass().toString().compareTo("class org.geotools.feature.type.NumericAttributeType")==0)){
			iter = m_Points.iterator();
			while(iter.hasNext() && setProgress(i, iShapeCount)){
				feature = iter.next();
				m_Coords[i] = feature.getGeometry().getCoordinate();
				i++;
			}
			iter.close();
		}
		else{
			iter = m_Points.iterator();
			while(iter.hasNext() && setProgress(i, iShapeCount)){
				feature = iter.next();
				Coordinate coord = feature.getGeometry().getCoordinate();
				String Z = feature.getRecord().getValue(m_iClass).toString();
				coord.z = Double.valueOf(Z);
				m_Coords[i] = coord;
				i++;
			}
			iter.close();
		}
			
		
		
		Triangulation triangulation = new Triangulation(m_Coords, new int[0][0]);
		triangulation.triangulate();
		Triangle[] triangles = triangulation.getTriangles();

		for (int j = 0; j < triangles.length; j++) {
			Object[] record = {new Integer(j)};
			Geometry triangle = getPolygon(triangles[j]);
			if (triangle != null){
				m_Triangles.addFeature(triangle, record);
			}
		}

		return !m_Task.isCanceled();

	}

	private Geometry getPolygon(Triangle triangle) {

		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coords = new Coordinate[4];

		for (int i = 0; i < 3; i++) {
			try{
				coords[i] = m_Coords[triangle.ppp[i].i];
			}catch (Exception e){
				return null;
			}
		}
		coords[3] = coords[0];

		LinearRing ring = gf.createLinearRing(coords);
		return gf.createPolygon(ring, null);

	}

}
