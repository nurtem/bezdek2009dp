/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.dem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.internal.impl.LayerImpl;

import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;

import com.vividsolutions.jts.geom.Geometry;


/***************************************************************
 * Class for managing source uDig's layer
 * @author Josef Bezdek
 *
 */
public class LayerManager {
	/***********************************************************************
	 * The method reads points of layer
	 * @param list of source points layer
	 * @return points layer
	 */
	public static List getPointsLayers(List list){
		Iterator iter = list.iterator();
		LinkedList listF = new LinkedList();
		try{
			
			while (iter.hasNext()){
				LayerImpl layer = (LayerImpl) iter.next();
				FeatureSource source = (FeatureSource) layer.getResource(FeatureSource.class, null);
        
				FeatureCollection collection = new MemoryFeatureCollection(layer.getSchema());
				collection.addAll(source.getFeatures());
                  
				Iterator j = collection.iterator();
				Feature feature = (Feature) j.next();
				Geometry geom = feature.getDefaultGeometry();
				if (geom.getGeometryType()=="Point"){
					listF.add(layer);
				}
				
            }
		}catch (Exception e){
		    e.printStackTrace();
		}
		return listF;
	}

	/***********************************************************************
	 * The method reads lines of layer
	 * @param list of source lines layer
	 * @return lines layer
	 */
	public static List getLineStringLayers(List list){
		Iterator iter = list.iterator();
		LinkedList listF = new LinkedList();
		try{
			
			while (iter.hasNext()){
				LayerImpl layer = (LayerImpl) iter.next();
				FeatureSource source = (FeatureSource) layer.getResource(FeatureSource.class, null);
        
				FeatureCollection collection = new MemoryFeatureCollection(layer.getSchema());
				collection.addAll(source.getFeatures());
                  
				Iterator j = collection.iterator();
				Feature feature = (Feature) j.next();
				Geometry geom = feature.getDefaultGeometry();
				if (geom.getGeometryType()=="MultiLineString"){
					listF.add(layer);
				}
				
            }
		}catch (Exception e){
		    e.printStackTrace();
		}
		return listF;
	}
	
	
	/*********************************************************************
	 * creates new name for new layer
	 * @param name new name of layer
	 * @param atrributetype of layer
	 */
	public static IGeoResource createResource(String name, AttributeType[] atrributetype  ) {
	        // String orig = new String(name);    	
	        IGeoResource resource = null;

	        FeatureType feature; //	pom.setName(name+"_trans_"+i);
	                             //newLayer.setName(name);

	        String oldname = name;
	        int i = 1;
	        String newname = " ";

	        while (newname != oldname) {
	            newname = oldname;

	            try {
	                feature = FeatureTypeBuilder.newFeatureType(atrributetype,
	                        newname);
	                resource = (CatalogPlugin.getDefault().getLocalCatalog()
	                                         .createTemporaryResource(feature));

	               // Map map = (Map) ApplicationGIS.getActiveMap();
	                                

	                //newLayer = map.getLayerFactory().createLayer(resource);
	            } catch (Exception e) {
	                oldname = name + i;
	                i++;

	                // e.printStackTrace();                 
	                // newLayer =createLayer2(this.name +"new", Modellayer);                    
	            }
	        }

	        return resource;
	    }
}

