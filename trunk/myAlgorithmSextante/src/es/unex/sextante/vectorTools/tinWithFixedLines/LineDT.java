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

import com.vividsolutions.jts.geom.Coordinate;

public class LineDT {
	public Coordinate A;
	public Coordinate B;
	public boolean isHardBreakLine;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param A - start point
	 * @param B - end point
	 * 
	 */

	public LineDT (Coordinate A, Coordinate B, boolean isHardBreakLine) {
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

	public String toString(){
		return (A.toString()+"   "+B.toString()+"   "+isHardBreakLine);
	}

	
}
