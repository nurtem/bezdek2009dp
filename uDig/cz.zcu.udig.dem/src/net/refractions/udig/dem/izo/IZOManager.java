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

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geotools.delaunay.DelaunayDataStore;

/********************************************
 * for saving information of new Izolines
 * @author Josef Bezdek
 *
 */ 
public class IZOManager {
	public int levelOfSmoothing;
	public double intervalIZO = 10D;
	public DelaunayDataStore triangles;
	public LayerImpl sourceLayer;
	public IZOManager(){
	}
	
	/******************************************************
	 * creates Izolines of triangles
	 * @param triangles triangulation
	 */
	public void createIZO(DelaunayDataStore triangles){
		this.triangles = triangles;
	    IMap map = ApplicationGIS.getActiveMap();
	     try {
	 		 Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			                                    .getShell();
			 new ProgressMonitorDialog(shell).run(true, true,
			      new IZOBuilder(this));

	     } catch (Exception e) {
		     MessageDialog.openError(Display.getDefault().getActiveShell(),
			                "Error .... ", e.toString());
		 }
	}	
}
