package es.unex.sextante.vectorTools.tinWithFixedLines;

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

public class LineDT {
	public PointDT A;
	public PointDT B;
	public boolean isHardBreakLine;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param A - start point
	 * @param B - end point
	 * 
	 */

	public LineDT(PointDT A, PointDT B, boolean isHardBreakLine) {
		this.A = A;
		this.B = B;
		this.isHardBreakLine = isHardBreakLine;
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param A - start point
	 * @param B - end point
	 * 
	 */
	public LineDT(Coordinate A, Coordinate B, boolean isHardBreakLine) {
		this.A = new PointDT(A);
		this.B = new PointDT(B);
		this.isHardBreakLine = isHardBreakLine;
	}
	
	public String toString(){
		return (A.toString()+"   "+B.toString()+"   "+isHardBreakLine);
	}

	
}
