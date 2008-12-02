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

 
package org.geotools.delaunay;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import org.geotools.delaunay.TriangleDT;
import java.nio.channels.FileChannel;

public class ObjectToFile {
	private File aFile = null;
	private FileInputStream inFile = null;
	private FileOutputStream outFile = null;
	private FileChannel inChannel = null;
	private FileChannel outChannel = null;
	private ByteBuffer buf = ByteBuffer.allocate(136);  
	private DoubleBuffer dBuf = null;
	
	/*********************************************************************
	 * Constructor for creating file for saving data of triangles
	 * @param pathFile - completly path plus name of file
	 */
	ObjectToFile(String pathFile){
		try{
			aFile = new File(pathFile);
			if (aFile.exists())
				aFile.delete();
			aFile.createNewFile();
				aFile.createNewFile();
			inFile = new FileInputStream(aFile); 
			outFile = new FileOutputStream(aFile, true); 

		}
		catch (Exception e){
			e.printStackTrace();
		}
	    inChannel = inFile.getChannel();
	    outChannel = outFile.getChannel();
	    
	}
	
	/*******************************************************************
	 * Protected method for reading triangles with definied index
	 * @param idx - index in file
	 * @return triangle
	 */
	protected TriangleDT read(int idx){
		double src[] = new double[17];
	
		try{
			buf.clear();
			inChannel.read(buf, idx*136);  
			buf.flip();
			
			buf.asDoubleBuffer().get(src);
			return new TriangleDT(src);
		}
		catch (Exception e){
			//System.out.println("READ " +e);
			e.printStackTrace();
		}
		return null;
	}
	
	/******************************************************************
	 * Protected method for writing tringlas on the end of file
	 * @param data - triangle convert to basic data type
	 */
	protected  void writeT(double [] data){
		try{
			buf.clear();
			for (int i = 0; i<17;i++)
				buf.putDouble(data[i]);
			buf.flip();
			outChannel.write(buf);
		}
		catch (Exception e){
			System.out.println("WRITE" + e);
			e.printStackTrace();
		}
	}

	/******************************************************************
	 * Protected method for writing triangles on the index to file
	 * @param data - triangle convert to basic data type
	 */
	protected  void writeT(double [] data, int idx){
		try{
			buf.clear();
			for (int i = 0; i<17;i++)
				buf.putDouble(data[i]);
			buf.flip();
			outChannel.write(buf,idx*136);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	protected void closeStrem(){
		
	}

}
