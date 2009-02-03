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
package net.refractions.udig.dem.izo;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.dem.LayerManager;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.delaunay.DelaunayDataStore;
import org.geotools.delaunay.DelaunayDataStoreRAM;
import org.geotools.delaunay.TriangleDT;
import org.geotools.delaunay.beziersurface.BezierSurface;
import org.geotools.delaunay.contourlines.Izolines;
import org.geotools.delaunay.contourlines.LinearContourLines;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKTReader;

/*************************************************
 * Bulider for Izolines. It's active feature of IZOManager
 * @author Josef Bezdek
 *
 */
public class IZOBuilder  implements IRunnableWithProgress {
	IZOManager izoManager;
	
	/***************************************************************
	 * Constructor
	 * @param izoManager its manager
	 */
	public IZOBuilder(IZOManager izoManager){
		this.izoManager = izoManager;
	}

	/*********************************************************
	 * The method for counting IZO with IProgressMonitor
	 */
	public void run(IProgressMonitor monitor)throws InvocationTargetException, InterruptedException {
	       try {
	           FeatureSource source = (FeatureSource) izoManager.sourceLayer
               .getResource(FeatureSource.class, null);
	           Map map = (Map) ApplicationGIS.getActiveMap();

	           monitor.beginTask("Creating TIN ....", source.getFeatures().size());
	           DelaunayDataStore trianglesNew = new DelaunayDataStoreRAM();
	           System.out.println("level"+izoManager.levelOfSmoothing);
	  		   if (izoManager.levelOfSmoothing!=0){
	  			   izoManager.triangles.closeInserting();
	  			   BezierSurface.countSurface(trianglesNew, izoManager.triangles, izoManager.levelOfSmoothing);
	  		 
	  		   }
	  		   else
	  			   trianglesNew = izoManager.triangles;
	  		   System.out.println("YIN"+trianglesNew.getNumberOfTriangles());
	  	       monitor.subTask("Counting surface...");
	           
			   AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom",
		    			LineString.class, true, 0, null, izoManager.sourceLayer.getCRS());    
		    	
			   AttributeType idx = AttributeTypeFactory.newAttributeType(
		    			"elevation", Integer.class);
			   FeatureType ftTriangles = FeatureTypeFactory.newFeatureType(
		    			new AttributeType[] { geom, idx }, "izoline");

			   WKTReader wktReader = new WKTReader();
		       Feature aNewFeature = null;
		       LinkedList izoLinesList = new LinkedList();
		       int number = 0;
		       FeatureCollection  collection = new MemoryFeatureCollection(ftTriangles);
		       
		       for (int j =0; j<trianglesNew.getNumberOfTriangles(); j++){

		    	   TriangleDT T = (TriangleDT)trianglesNew.getTriangle(j);
		    	   //System.out.println(j);T.toStringa();
		    		if (T!=null){
		    			izoLinesList = LinearContourLines.countIzoLines(T, ((Double)izoManager.intervalIZO).intValue());
		    			Iterator iter = izoLinesList.iterator();
		    			while (iter.hasNext()){
		    				Izolines izo = (Izolines) iter.next();
		    				LineString geometry = (LineString) wktReader
		    				.read("LINESTRING ("+(izo.A.x)+" "+(izo.A.y)+"," +(izo.B.x)+" "+(izo.B.y)+")");
		    				//System.out.println("IZO"+ ((Integer)j).toString());
		    				aNewFeature = ftTriangles.create(new Object[]{(Object)geometry,(Object)izo.elevation}, ((Integer) number).toString());
		    				number++;
		    				collection.add(aNewFeature);
		    				
		    				
		    			}
		    		}	
		    		monitor.worked(1);
		    	}
		      // System.out.println("SIYE:"+collection.size());
			   
	          IGeoResource resource = (LayerManager.createResource(izoManager.sourceLayer.getName()+"_IZO", ftTriangles.getAttributeTypes()));
	          resource.resolve(FeatureStore.class, null).addFeatures(collection);
	          ApplicationGIS.addLayersToMap(map, Collections.singletonList(resource), 0);

	          monitor.done();
	 
	  		   
	  		   
		       } catch (Exception e){
		    	   e.printStackTrace();
		       }
	       }
}