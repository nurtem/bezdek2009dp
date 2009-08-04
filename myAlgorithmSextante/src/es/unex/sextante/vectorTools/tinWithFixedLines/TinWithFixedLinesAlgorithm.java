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

import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.memory.MemoryPageStore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

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
	private IVectorLayer m_TrianglesOut;
	
	private IVectorLayer m_HardLines;
	private IVectorLayer m_SoftLines;
	private int m_iClass;
	private boolean m_useHardLines;
	private boolean m_useSoftLines;
	private LinkedList breakLines = new LinkedList();

	
	public void defineCharacteristics() {

		setName(Sextante.getText( "TIN - modified by fixed lines"));
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
		
		
		Class types[] = {Integer.class, String.class, Integer.class};
		String sNames[] = {"ID", "hardBreak", "typeOfBreak"};
		m_TrianglesOut = getNewVectorLayer(TRIANGLES,
										m_Triangles.getName()+"_modified",
										IVectorLayer.SHAPE_TYPE_POLYGON,
										types,
										sNames);

		
		i = 0;
		iShapeCount = m_Triangles.getShapesCount();
		ArrayList triangles = new ArrayList();
		Data data;
		DataDefinition dd = new DataDefinition("US-ASCII"); 
		dd.addField(Integer.class);
		IFeatureIterator iter = m_Triangles.iterator();
		RTree trianglesIdx = null;
		
		try{ 
			PageStore ps = new MemoryPageStore(dd);
			trianglesIdx = new RTree(ps);
			while(iter.hasNext()){
				IFeature feature = iter.next();
				Polygon trianglePolygon = (Polygon) feature.getGeometry();
				Coordinate[] coords = trianglePolygon.getCoordinates();
				TriangleDT triangle = new TriangleDT(coords);
				triangles.add(i, triangle);
				data = new Data(dd);
				data.addValue(i);
				trianglesIdx.insert(triangle.getEnvelope(), data);
	        	i++;
			}
			iter.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}	
	
				
		if (m_useSoftLines){
			
			m_SoftLines = m_Parameters.getParameterValueAsVectorLayer(SOFTLINES);
			iShapeCount = m_SoftLines.getShapesCount();
			iter = m_SoftLines.iterator();
			i = 0;	
			
			while(iter.hasNext() && setProgress(i, iShapeCount)){
				IFeature feature = iter.next();
				LineString lineString = (LineString) feature.getGeometry();
				Coordinate[] coords = lineString.getCoordinates();
				LineDT softLine = null;
				for (int j=1; j< coords.length; j++){
					softLine = new LineDT(new Coordinate(coords[j-1].x,coords[j-1].y,0),
							new Coordinate(coords[j].x,coords[j].y,0), false);
					breakLines.add(softLine);
				}
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
				LineDT hardLine = null;
				for (int j=1; j< coords.length; j++){
					hardLine = new LineDT(new Coordinate(coords[j-1].x,coords[j-1].y,0),
							new Coordinate(coords[j].x,coords[j].y,0), true);
					breakLines.add(hardLine);
				}
				i++;
			}
			iter.close();
		}

		if (!(breakLines==null)){
			TINWithFixedLines newTriangles = new TINWithFixedLines(triangles, trianglesIdx, breakLines);
			triangles = newTriangles.countTIN();
			Iterator iterJ = triangles.iterator();
			int j = 0;
						
			while(iterJ.hasNext()){
				TriangleDT trian = (TriangleDT) iterJ.next();
				if (trian!=null){
					Object[] record = {new Integer(j),"", trian.typeBreakLine};
					if (trian.haveBreakLine){
						record[0] = new Integer(j);
						record[1] = "Y";
						record[2] = trian.typeBreakLine;
					}
					Geometry triangle = getPolygon(trian);
					m_TrianglesOut.addFeature(triangle, record);
					j++;
				}
			}
		}
		return !m_Task.isCanceled();

		
	}	

		private Geometry getPolygon(TriangleDT triangle) {

			GeometryFactory gf = new GeometryFactory();
			Coordinate[] coords = new Coordinate[4];
			coords[0] = (Coordinate) triangle.A;
			coords[1] = (Coordinate) triangle.B;
			coords[2] = (Coordinate) triangle.C;
			coords[3] = (Coordinate) triangle.A;
					
			LinearRing ring = gf.createLinearRing(coords);
			return gf.createPolygon(ring, null);

		}

}

