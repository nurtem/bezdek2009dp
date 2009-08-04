
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.geotools.delaunay.TriangleDT;
//import org.geotools.delaunay.DelaunayDataStoreHDD.Element;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.shapefile.ShapefileDataStore; 
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.fs.FileSystemPageStore;
import org.geotools.index.rtree.memory.MemoryPageStore;
import org.geotools.referencing.CRS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.index.quadtree.*;
import com.vividsolutions.jts.index.strtree.*;


public class Testing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		STRtree qt = new STRtree();
		Coordinate P1 = new Coordinate(0,0,0);
		Coordinate P2 = new Coordinate(10,10,10);
		Coordinate P3 = new Coordinate(20,0,5);
		
		TriangleDT T = new TriangleDT(P1,P2,P3);
		qt.insert(T.setEnvelope(), 1);
		System.out.println(T.setEnvelope().toString());
		Coordinate P4 = new Coordinate(30,30,30);
		TriangleDT TT = new TriangleDT(P2,P3,P4);
		qt.insert(TT.setEnvelope(), 2);
		System.out.println(TT.setEnvelope().toString());
		System.out.println("get Triangle");
		double[] key = new double[2];
		key[0] = 20;
		key[1] = 20;
		List list = qt.query(new Envelope(new Coordinate(key[0],key[1])));
		System.out.println(list.size());
		Iterator iter = list.iterator();
		int idx = -1;
		while(iter.hasNext()){
			idx = (Integer) iter.next();
			System.out.println(idx);
		}
		 for (int i=0;i<1000;i++){
//			 System.out.println(i+"=======================================pocet"+triangles.getNumberOfTriangles());
			 Coordinate x=new Coordinate(i,(Math.random()*1000000),(Math.random()*100));
			 Coordinate x1=new Coordinate(i+2,(Math.random()*100));
			 Coordinate x2=new Coordinate(i+3,(Math.random()*1000000),(Math.random()*100));
				TriangleDT TTT = new TriangleDT(x,x1,x2);
				qt.insert(TTT.setEnvelope(), i);		
		 }
		list = qt.query(new Envelope(new Coordinate(key[0],key[1])));
			System.out.println(list.size());
		iter = list.iterator();
		idx = -1;
			while(iter.hasNext()){
				idx = (Integer) iter.next();
				//System.out.println(idx);
			}
		

	}

}
