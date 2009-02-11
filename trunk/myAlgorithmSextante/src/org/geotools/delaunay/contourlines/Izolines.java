/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
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
 *    @author      Josef Bezdek
 *	  @version     %I%, %G%
 *    @since JDK1.3 
 */
package org.geotools.delaunay.contourlines;

import org.geotools.delaunay.PointDT;

import com.vividsolutions.jts.geom.LineString;

public class Izolines {
	// start point of triangle
	public PointDT A;
	// end point of triangle
	public PointDT B;
	// elevation
	public double elevation;
	
	/***********************************************************
	 * Constructor 
	 * @param A - start point
	 * @param B - end point
	 * @param elevation - elevation
	 */
	Izolines (PointDT A, PointDT B, int elevation){
		this.A = A;
		this.B = B;
		this.elevation = elevation;
	}
}
