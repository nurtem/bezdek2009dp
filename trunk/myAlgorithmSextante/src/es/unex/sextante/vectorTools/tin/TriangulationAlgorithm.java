package es.unex.sextante.vectorTools.tin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;



public class TriangulationAlgorithm extends GeoAlgorithm{

	public static final String LAYER = "LAYER";
	public static final String RESULT = "RESULT";

	public boolean processAlgorithm() throws GeoAlgorithmExecutionException{

		int i = 0;

		IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
		IVectorLayer driver = getNewVectorLayer(RESULT,
				Sextante.getText( "Triagulation"),
				IVectorLayer.SHAPE_TYPE_POINT,
				layerIn.getFieldTypes(),
				layerIn.getFieldNames());

		IFeatureIterator iter = layerIn.iterator();
		int iTotal = layerIn.getShapesCount();
		while (iter.hasNext() && setProgress(i, iTotal)){
			IFeature feature = iter.next();
			Point centroid = getCentroid(feature.getGeometry());
			driver.addFeature(centroid, feature.getRecord().getValues());
			setProgress(i, iTotal);
			i++;
		}
		iter.close();

		return !m_Task.isCanceled();
	}

	public void defineCharacteristics(){

		setName(Sextante.getText( "TriangulationMY"));
		setGroup(Sextante.getText("Herramientas_capas_puntos"));

		try {
			m_Parameters.addInputVectorLayer(LAYER,
					Sextante.getText( "Poligonos"),
					AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
			addOutputVectorLayer(RESULT,
								Sextante.getText( "Centroides"),
								OutputVectorLayer.SHAPE_TYPE_POINT);
		} catch (RepeatedParameterNameException e) {
			Sextante.addErrorToLog(e);
		}

	}

	private Point getCentroid(Geometry geometry){

		return geometry.getCentroid();

	}

}
