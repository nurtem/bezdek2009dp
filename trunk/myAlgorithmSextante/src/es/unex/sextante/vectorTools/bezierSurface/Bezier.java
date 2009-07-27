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

package es.unex.sextante.vectorTools.bezierSurface;

import java.awt.geom.GeneralPath;

import com.vividsolutions.jts.geom.Coordinate;

public class Bezier {
	
	Coordinate b300;
	Coordinate b030;
	Coordinate b003;
	Coordinate b012 = null;
	Coordinate b021 = null;
	Coordinate b102 = null;
	Coordinate b120 = null;
	Coordinate b210 = null;
	Coordinate b201 = null;
	Coordinate b111 = null;
	GeneralPath trianglePath = new GeneralPath();
	
	/************************************************************************
	 * Constructor 
	 * @param bxxx - control points of control mesh
	 */
	protected Bezier(Coordinate b300,Coordinate b030,Coordinate b003,Coordinate b210,Coordinate b120,
			Coordinate b021,Coordinate b012,Coordinate b102,Coordinate b201, Coordinate b111){
		this.b300 = b300;
		this.b030 = b030;
		this.b003 = b003;
		this.b012 = b012;
		this.b021 = b021;
		this.b102 = b102;
		this.b120 = b120;
		this.b210 = b210;
		this.b201 = b201;
		this.b111 = b111;
		trianglePath.moveTo( (float)b300.x, (float) b300.y);
		trianglePath.lineTo((float) b030.x, (float) b030.y);
		trianglePath.lineTo((float) b003.x, (float) b003.y);
		trianglePath.lineTo((float) b300.x, (float) b300.y);
		trianglePath.closePath();
		
	}
	
	/************************************************************************
	 * Protected method for counting elevation of triangle's point with coordinates u,v 
	 * @param u - barycentric koeficient u
	 * @param v - barycentric koeficient v
	 * @return new point 
	 */
	protected Coordinate getElevation(double u, double v, double scaleZ){
		double w = 1 - u - v;
		double x = b300.x*Math.pow(w, 3) + b030.x*Math.pow(u,3) + b003.x*Math.pow(v,3)+
					3*b210.x*Math.pow(w, 2)*u +  3*b120.x*Math.pow(u,2)*w + 3*b201.x*Math.pow(w, 2)*v +
					3*b021.x*Math.pow(u, 2)*v + 3*b102.x*Math.pow(v, 2)*w + 3*b012.x*u*Math.pow(v,2)+ 6*b111.x*u*v*w;
		
		double y = b300.y*Math.pow(w, 3) + b030.y*Math.pow(u,3) + b003.y*Math.pow(v,3)+
		3*b210.y*Math.pow(w, 2)*u +  3*b120.y*Math.pow(u,2)*w + 3*b201.y*Math.pow(w, 2)*v +
		3*b021.y*Math.pow(u, 2)*v + 3*b102.y*Math.pow(v, 2)*w + 3*b012.y*u*Math.pow(v,2)+ 6*b111.y*u*v*w;

		double z = (b300.z*Math.pow(w, 3) + b030.z*Math.pow(u,3) + b003.z*Math.pow(v,3)+
		3*b210.z*Math.pow(w, 2)*u +  3*b120.z*Math.pow(u,2)*w + 3*b201.z*Math.pow(w, 2)*v +
		3*b021.z*Math.pow(u, 2)*v + 3*b102.z*Math.pow(v, 2)*w + 3*b012.z*u*Math.pow(v,2)+ 6*b111.z*u*v*w)*scaleZ;

		return new Coordinate(x,y,z);
	}

	protected void printToConsole(){
		System.out.println("======================================");
		System.out.println(b300.toString());
		System.out.println(b030.toString());
		System.out.println(b003.toString());
		System.out.println("Normals:");
			
				System.out.println(b210.toString());
				System.out.println(b120.toString());
				System.out.println(b021.toString());
				System.out.println(b012.toString());
				System.out.println(b102.toString());
				System.out.println(b201.toString());
				System.out.println(b111.toString());
			
		System.out.println("======================================");
	}
	
	/***************************************************************************
	 * The method which testing, if the triangle contains the point
	 * 
	 * @param P - point which will be tested
	 * 
	 * @return boolean true - the triangle contains the point false - the
	 *         triangle doesn't contains point
	 * 
	 */

	public boolean contains(Coordinate P) {
		return trianglePath.contains(P.x, P.y);
	}
	
}
