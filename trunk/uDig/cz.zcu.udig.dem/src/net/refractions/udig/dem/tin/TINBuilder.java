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
package net.refractions.udig.dem.tin;

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
import org.geotools.delaunay.DelaunayDataStoreRAM;
import org.geotools.delaunay.IncrementalDT;
import org.geotools.delaunay.LineDT;
import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;
import org.geotools.delaunay.fixedlines.TINWithFixedLines;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKTReader;

/*******************************************
 * Builder for TIN
 * @author Josef Bezdek
 *
 */
public class TINBuilder  implements IRunnableWithProgress {
	private TINManager tinManager;
	
	/**************************************************
	 * Constructor
	 * @param tinManager manager TIN
	 */
	public TINBuilder(TINManager tinManager){
		this.tinManager = tinManager;
	}
	
	/********************************************************
	 * the method for counting TIN
	 */
	public void run(IProgressMonitor monitor)throws InvocationTargetException, InterruptedException {
       try {
           FeatureSource source = (FeatureSource) tinManager.sourceLayer
               .getResource(FeatureSource.class, null);
           monitor.beginTask("Creating TIN ....", source.getFeatures().size());

           FeatureCollection collection = new MemoryFeatureCollection(source
                   .getSchema());
     
           Map map = (Map) ApplicationGIS.getActiveMap();

           collection.addAll(source.getFeatures());
           tinManager.triangles = new DelaunayDataStoreRAM();
           tinManager.tin = new IncrementalDT(tinManager.triangles);
 
           monitor.subTask("counting ...");
           Feature feature;
           
           for (Iterator j = collection.iterator(); j.hasNext();) {
               feature = (Feature) j.next();
               Coordinate[] coor = feature.getDefaultGeometry().getCoordinates();
               
               for (int k=0; k<coor.length; k++){
            	   String Zs = (feature.getAttribute(tinManager.unitZindex)).toString();
            	   
            	   tinManager.tin.insertPoint(new Coordinate(coor[k].x, coor[k].y, Double.valueOf(Zs)));
            	   monitor.worked(1);
               }
               if (monitor.isCanceled()) {
                   throw new InterruptedException(
                       "The counting TIN was cancelled.");
               }
           }
 
           if (tinManager.softBreakLLayer!=null){
        	   monitor.subTask("Counting soft break lines...");
        	   FeatureSource sourceSoft = (FeatureSource) tinManager.softBreakLLayer.getResource(FeatureSource.class, null);
        	   FeatureCollection collectionSoft = new MemoryFeatureCollection(sourceSoft.getSchema());
        	   collection.addAll(sourceSoft.getFeatures());
        	   LinkedList listLine = new LinkedList();
        	   for (Iterator j = collection.iterator(); j.hasNext();) {
        		   feature = (Feature) j.next();
        		   Coordinate[] coor = feature.getDefaultGeometry().getCoordinates();
           
        		   for (int k=1; k<coor.length; k++ ){
        			  // System.out.println("AHOJ");
        			   LineDT L = new LineDT(new PointDT(coor[k].x, coor[k].y, 0), new PointDT(coor[k-1].x, coor[k-1].y, 0), false);
        			   //System.out.println(L.A.toString()+"  "+ L.B.toString());
        			   listLine.add(L);
        			   monitor.worked(2);
        			   
        		   }
        		   if (monitor.isCanceled()) {
        			   throw new InterruptedException(
        			   	"The counting TIN was cancelled.");
        		   }
        	   }
        	   TINWithFixedLines.countTIN(tinManager.triangles, listLine);
           }
           
           
          
           
           if (tinManager.hardBreakLLayer!=null){
        	   monitor.subTask("Counting hard break lines...");  
        	   FeatureSource sourceHard = (FeatureSource) tinManager.hardBreakLLayer.getResource(FeatureSource.class, null);
        	   FeatureCollection collectionHard = new MemoryFeatureCollection(sourceHard.getSchema());
        	   collection.addAll(sourceHard.getFeatures());
        	   LinkedList listLine = new LinkedList();
        	   for (Iterator j = collection.iterator(); j.hasNext();) {
        		   feature = (Feature) j.next();
        		   Coordinate[] coor = feature.getDefaultGeometry().getCoordinates();
           
        		   for (int k=1; k<coor.length; k++ ){
        			  // System.out.println("AHOJ");
        			   LineDT L = new LineDT(new PointDT(coor[k].x, coor[k].y, 0), new PointDT(coor[k-1].x, coor[k-1].y, 0), true);
        			   //System.out.println(L.A.toString()+"  "+ L.B.toString());
        			   listLine.add(L);
        			   monitor.worked(2);
        			   
        		   }
        		   if (monitor.isCanceled()) {
        			   throw new InterruptedException(
        			   	"The counting TIN was cancelled.");
        		   }
        	   }
        	   TINWithFixedLines.countTIN(tinManager.triangles, listLine);
           }
           
           
           monitor.subTask("Adding features to new layer...");
           
		   AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom",
	    			LinearRing.class, true, 0, null, tinManager.sourceLayer.getCRS());    
	    	
		   AttributeType idx = AttributeTypeFactory.newAttributeType(
	    			"index", Integer.class);
		   FeatureType ftTriangles = FeatureTypeFactory.newFeatureType(
	    			new AttributeType[] { geom, idx }, "Triangle");

		   WKTReader wktReader = new WKTReader();
	       collection = new MemoryFeatureCollection(ftTriangles);
	       Feature aNewFeature = null;
	       for (int j =0; j<tinManager.triangles.getNumberOfTriangles(); j++){

	    	   TriangleDT T = (TriangleDT)tinManager.triangles.getTriangle(j);
	    		if (T!=null){
	    			LinearRing geometry = (LinearRing) wktReader
	    		   			.read("LINEARRING ("+(T.A.x)+" "+(T.A.y)+","+(T.B.x)+" "+(T.B.y)+"," +
	    		   					+(T.C.x)+" "+(T.C.y)+","+(T.A.x)+" "+(T.A.y)+")");
		    		//System.out.println( ((Integer)j).toString());
	    			aNewFeature = ftTriangles.create(new Object[]{(Object)geometry,(Object)j},  ((Integer)j).toString());
	    			collection.add(aNewFeature);
	    		}	
	    		
	    	}

		   
          IGeoResource resource = (LayerManager.createResource(tinManager.sourceLayer.getName()+"_TIN", ftTriangles.getAttributeTypes()));
           resource.resolve(FeatureStore.class, null).addFeatures(collection);
           ApplicationGIS.addLayersToMap(map,
               Collections.singletonList(resource), 0);

           monitor.done();
 
	  } catch (Exception e) {
           e.printStackTrace();
       }
    }
	
}