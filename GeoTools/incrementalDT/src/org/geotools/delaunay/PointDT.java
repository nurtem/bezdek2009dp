package org.geotools.delaunay;

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

import com.vividsolutions.jts.geom.Coordinate;

public class PointDT extends Coordinate {

	/***************************************************************************
	 * Constructor
	 * 
	 * @param x - coordinate of x-axis
	 * @param y - coordinate of y-axis
	 * @param z - coordinate of z-axis
	 */

	public PointDT(double x, double y, double z) {
		super(x, y, z);
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param x - coordinate
	 * 
	 */

	public PointDT(Coordinate P) {
		super(P.x, P.y, P.z);
	}

	/***************************************************************************
	 * The method for converting to String
	 * 
	 * @return String - "PointDT: x y z
	 * 
	 */

	public String toString() {
		return ("PointDT: :" + x + " " + y + "   " + z + " ");
	}

	/***************************************************************************
	 * The method for comparing two points
	 * 
	 * @param P - the point which is compare
	 * 
	 * @return boolean - true - are same false - are different
	 */

	public boolean compare(Coordinate P) {
		if (x == P.x && y == P.y)
			return true;
		return false;
	}
	
	/***************************************************************************
	 * The method for comparing two points
	 * 
	 * @param P - the point which is compare
	 * 
	 * @return boolean - true - are same false - are different
	 */

	public boolean compare(PointDT P) {
		if (x == P.x && y == P.y)
			return true;
		return false;
	}

	
}
