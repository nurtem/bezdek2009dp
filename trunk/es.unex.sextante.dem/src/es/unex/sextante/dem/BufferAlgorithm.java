package es.unex.sextante.dem;

import es.unex.sextante.additionalInfo.*;
import es.unex.sextante.core.*;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.*;
import es.unex.sextante.outputs.OutputVectorLayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import java.util.ArrayList;
import java.util.Stack;


public class BufferAlgorithm extends GeoAlgorithm{

	public static final byte BUFFER_INSIDE_POLY = 1;
	public static final byte BUFFER_OUTSIDE_POLY = 0;
	public static final byte BUFFER_INSIDE_OUTSIDE_POLY = 2;

	public static final String RESULT = "RESULT";
	public static final String NOTROUNDED = "NOTROUNDED";
	public static final String RINGS = "RINGS";
	public static final String TYPE = "TYPES";
	public static final String METHOD = "METHOD";
	public static final String DISTANCE = "DISTANCE";
	public static final String FIELD = "FIELD";
	public static final String LAYER = "LAYER";

	private IVectorLayer m_Output;
	private boolean m_bRounded;
	private int m_iRings;
	private int m_iType;
	private int numProcessed = 0;

	public boolean processAlgorithm() throws GeoAlgorithmExecutionException{

		int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
		double dDistance = m_Parameters.getParameterValueAsDouble(DISTANCE);
		int iField = m_Parameters.getParameterValueAsInt(FIELD);
		IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);

		m_bRounded = !m_Parameters.getParameterValueAsBoolean(NOTROUNDED);
		m_iRings = m_Parameters.getParameterValueAsInt(RINGS) + 1;
		m_iType = m_Parameters.getParameterValueAsInt(TYPE);

		Class types[] = {Long.class, Double.class};
		String[] sFieldNames = {"ID", "DIST"} ;


		if (layerIn.getShapeType() != IVectorLayer.SHAPE_TYPE_POLYGON){
			m_iType = BUFFER_OUTSIDE_POLY;
		}

		if(m_iType == BUFFER_INSIDE_OUTSIDE_POLY){
			Class _types[] = {Long.class, Double.class, Double.class};
			types = _types;
			String fieldNames[] = {"ID", "FROM", "TO"} ;
			sFieldNames = fieldNames;
		}

		m_Output = getNewVectorLayer("RESULT", "Buffer",
				IVectorLayer.SHAPE_TYPE_POLYGON,
				types,
				sFieldNames);

		int i = 0;
		int iTotal = layerIn.getShapesCount();
		IFeatureIterator iter = layerIn.iterator();
		while (iter.hasNext() && setProgress(i, iTotal)){
			IFeature feature = iter.next();
			if (iMethod == 1){
				try{

					Number num = (Number) feature.getRecord().getValue(iField);
					dDistance = num.doubleValue();
				}
				catch(Exception e){
					continue;
				}
			}
			computeBuffer(feature.getGeometry(), dDistance);
			i++;
		}
		iter.close();

		return !m_Task.isCanceled();

	}

	public void defineCharacteristics(){

		String[] sDistance = {Sextante.getText( "AADbvistancia_fija"),
							Sextante.getText( "AADistannbvcia_en_campo")};

		String[] sRings = {Integer.toString(1), Integer.toString(2), Integer.toString(3)};

		String[] sType = {Sextante.getText( "AAHacia_fuera"),
							Sextante.getText( "AAHacia_dentro"),
							Sextante.getText( "AADentro_y_fuera"),};

		setName(Sextante.getText( "Triangulation"));
		setGroup(Sextante.getText("MyAlgorithm"));

		try {
			m_Parameters.addInputVectorLayer(LAYER,
					Sextante.getText( "AAACapa_de_entrada"),
					AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
			m_Parameters.addTableField(FIELD,
					Sextante.getText( "AAACampo_para_distancia"),
					"LAYER");
			m_Parameters.addNumericalValue(DISTANCE,
					Sextante.getText( "AAADistancia"),
					AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100,
					0, Double.MAX_VALUE);
			m_Parameters.addSelection(METHOD,
					Sextante.getText( "AAATipo_de_distancia"),
					sDistance);
			m_Parameters.addSelection(TYPE,
					Sextante.getText( "AAATipo_de_buffer"),
					sType);
			m_Parameters.addSelection(RINGS,
									Sextante.getText( "AAAANumero_anillos_concentricos"),
									sRings);
			m_Parameters.addBoolean(NOTROUNDED, Sextante.getText( "AAAANo_usar_buffer_redondeado"), false);

			addOutputVectorLayer(RESULT,Sextante.getText( "AAAABuffer"),
					OutputVectorLayer.SHAPE_TYPE_POLYGON);
			AlgorithmsAndResources.addAlgorithm(
		     "es.unex.sextante.dem.BufferAlgorithm");
			Sextante.initialize();
		} catch (RepeatedParameterNameException e) {
			
		}catch (UndefinedParentParameterNameException e) {
			
		} catch (OptionalParentParameterException e) {
			
		}

	}


	//*********** code adapted from BufferVisitor class, by Alvaro Zabala*******************//

	public void computeBuffer(Geometry originalGeometry, double bufferDistance){
		Geometry solution = null;
		Geometry inputParam = originalGeometry;
		/*
		 * When we try to apply large buffer distances, we could get OutOfMemoryError
		 * exceptions. Explanation in
		 * http://lists.jump-project.org/pipermail/jts-devel/2005-February/000991.html
		 * http://lists.jump-project.org/pipermail/jts-devel/2005-September/001292.html
		 * This problems hasnt been resolved in JTS 1.7.
		 */
		if(originalGeometry.getDimension() != 0){
			inputParam =
				TopologyPreservingSimplifier.
					simplify(originalGeometry,
							bufferDistance / 10d);
		}
		int cap = BufferOp.CAP_ROUND;
		if(!m_bRounded){
			cap = BufferOp.CAP_SQUARE;
		}

		//this two references are necessary to compute radial rings
		Geometry previousExteriorRing = null;
		Geometry previousInteriorRing = null;
		if(m_iType == BUFFER_INSIDE_POLY){
				//if we have radial internal buffers, we start by
				//most interior buffer
				for(int i = m_iRings; i >= 1; i--){
					double distRing = i * bufferDistance;
					BufferOp bufOp = new BufferOp(inputParam);
					bufOp.setEndCapStyle(cap);
					Geometry newGeometry = bufOp.getResultGeometry(-1 * distRing);
					if(verifyNilGeometry(newGeometry)){
						//we have collapsed original geometry
						return;
					}
					if(previousInteriorRing != null){
						solution = newGeometry.difference(previousInteriorRing);
					}else{
						solution = newGeometry;
					}
					numProcessed++;
					addFeature(solution, distRing);
					previousInteriorRing = newGeometry;
				}
		}else if(m_iType == BUFFER_OUTSIDE_POLY){
			for(int i = 1; i <= m_iRings; i++){
				double distRing = i * bufferDistance;
				BufferOp bufOp = new BufferOp(inputParam);
				bufOp.setEndCapStyle(cap);
				Geometry newGeometry = bufOp.getResultGeometry(distRing);
				if(previousExteriorRing != null){
					solution = newGeometry.difference(previousExteriorRing);
				}else{
					solution = newGeometry;
				}
				numProcessed++;
				addFeature(solution, distRing);
				previousExteriorRing = newGeometry;
			}
		}else if(m_iType == BUFFER_INSIDE_OUTSIDE_POLY){
			GeometryFactory geomFact = new GeometryFactory();
			for(int i = 1; i <= m_iRings; i++){
				double distRing = i * bufferDistance;
				BufferOp bufOp = new BufferOp(inputParam);
				bufOp.setEndCapStyle(cap);
				Geometry out = bufOp.getResultGeometry(distRing);
				Geometry in = bufOp.getResultGeometry(-1 * distRing);
				boolean collapsedInterior = verifyNilGeometry(in);
				if(previousExteriorRing == null || previousInteriorRing == null){
					if(collapsedInterior)
						solution = out;
					else
						solution = out.difference(in);
				}else{
					if(collapsedInterior){
						solution = out.difference(previousExteriorRing);
					}else{
						Geometry outRing = out.difference(previousExteriorRing);
						Geometry inRing = previousInteriorRing.difference(in);
						Geometry[] geomArray = new Geometry[]{outRing, inRing};
						solution = geomFact.createGeometryCollection(geomArray);
						ArrayList polygons = new ArrayList();
						Stack stack = new Stack();
						stack.push(solution);
						while(stack.size() != 0){
							GeometryCollection geCol =
								(GeometryCollection) stack.pop();
							for(int j = 0; j < geCol.getNumGeometries(); j++){
								Geometry geometry = geCol.getGeometryN(j);
								if(geometry instanceof GeometryCollection)
									stack.push(geometry);
								if(geometry instanceof Polygon)
									polygons.add(geometry);
							}
						}
						Polygon[] pols = new Polygon[polygons.size()];
						polygons.toArray(pols);
						MultiPolygon newSolution = geomFact.createMultiPolygon(pols);
						solution = newSolution;
					}
				}
				numProcessed++;
				addFeature(solution, -1 * distRing, distRing);
				previousExteriorRing = out;
				if(!collapsedInterior)
					previousInteriorRing = in;
			}
		}

	}

	protected void addFeature(Geometry geom, double distance){

		Object[] values = new Object[2];
		values[0] = new Long(numProcessed);
		values[1] = new Double(distance);
		m_Output.addFeature(geom, values);

	}

	protected void addFeature(Geometry geom, double distanceFrom, double distanceTo){

		Object[] values = new Object[3];
		values[0] = new Long(numProcessed);
		values[1] = new Double(distanceFrom);
		values[2] = new Double(distanceTo);
		m_Output.addFeature(geom, values);

	}

	public boolean verifyNilGeometry(Geometry newGeometry){

		if(newGeometry instanceof GeometryCollection){
			if(((GeometryCollection)newGeometry).getNumGeometries() == 0){
				//we have collapsed initial geometry
				return true;
			}
		}
		return false;
	}


}