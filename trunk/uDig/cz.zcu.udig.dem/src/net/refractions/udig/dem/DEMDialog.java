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
package net.refractions.udig.dem;

import java.util.LinkedList;

import net.refractions.udig.dem.izo.IZOManager;
import net.refractions.udig.dem.tin.TINManager;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geotools.feature.AttributeType;

/************************************************************
 * manage DEM dialog
 * @author josef Bezdek
 *
 */
public class DEMDialog extends Dialog {
	protected TINManager tinManager = new TINManager(); 
	protected IZOManager izoManager = new IZOManager();
	private String[] indexOfAttribute = null; 
	protected DEMComposite composite;  
	protected boolean levelWasSelected = false;
	protected boolean intervalWasSelected = false;
	

	/**
	 * Creates the dialog for DEM
	 * @param parentShell
	 */
	public DEMDialog(Shell parentShell) {
	   	super(parentShell);
	 
	}

	@Override
	protected void configureShell(Shell newShell) {
	   super.configureShell(newShell);
	   newShell.setText("Digital Elevation Model");
	}
	 
	@Override
	protected Control createDialogArea(Composite parent) {

        composite = new DEMComposite(this, parent, SWT.NONE);
        composite.setLayout( new FillLayout()); 
        setListener();
        
		return parent;
	}    
	/************************************************************
	 * sets Listener for every feature of dialog
	 */
	protected void setListener(){
        composite.sourcePointsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event
                    .getSelection();
                IMap map = ApplicationGIS.getActiveMap();
               tinManager.sourceLayer = (LayerImpl) selection.getFirstElement();
                LinkedList listOfAttr = new LinkedList();
                try{
                	AttributeType[] attT = tinManager.sourceLayer.getSchema().getAttributeTypes();           
                    indexOfAttribute = new String[attT.length];
                    int index = 0;
                	for (int i=0; i<attT.length; i++){
                    	if (attT[i].getClass().toString().compareTo("class org.geotools.feature.type.NumericAttributeType")==0){
                    		
                    		listOfAttr.add(attT[i].getName());
                    	}//System.out.println(attT[i].getClass().toString());
                    	indexOfAttribute[i] = attT[i].getName();
                    }
                    
                    composite.sourceZViewer.setInput(listOfAttr.toArray());
           
                }catch (Exception e){
                	 e.printStackTrace();
                }
           
                
               // composite.buttonTIN.setEnabled(true);
            }
        });
   

        composite.sourceZViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                for (int i = 0; i < indexOfAttribute.length; i++)
                	if (((String) selection.getFirstElement()).compareTo(indexOfAttribute[i])==0){
                		tinManager.unitZindex = i;
                		break;
                	}	
                composite.buttonTIN.setEnabled(true);
            }
        });
        
        composite.softBreakLinesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                tinManager.softBreakLLayer = (LayerImpl) selection.getFirstElement();
                
            }
        });

        composite.hardBreakLinesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                tinManager.hardBreakLLayer = (LayerImpl) selection.getFirstElement();
                
            }
        });

        
        composite.buttonTIN.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	tinManager.createTIN();
            	composite.setEnabledRadioButton(true);
            	composite.textBufferWidth.setEditable(true);
            	izoManager.sourceLayer = tinManager.sourceLayer;
            }
        });
        composite.buttonIZO.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	getBufferDistance();
            	izoManager.createIZO(tinManager.triangles);
            	composite.setEnabledRadioButton(true);
            }
        });
        
          
        composite.radioS0.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 0;
            	levelWasSelected = true;
           		composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 1;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 2;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 3;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 4;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS5.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 5;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS6.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 6;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS7.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 7;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });
        composite.radioS8.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	izoManager.levelOfSmoothing = 9;
            	levelWasSelected = true;
               	composite.buttonIZO.setEnabled(true);
            }
        });

        
        
	}
	
	/***********************************************************************
	 * reads text in textBufferWidth
	 */
   private void getBufferDistance() {
	        String text = composite.textBufferWidth.getText();
	        Double bufferDistance;
	        try{
	            bufferDistance = Double.parseDouble(text);
	        
	        } catch(NumberFormatException e){ 
	            bufferDistance = new Double(10);
	        }
	        // set the current and default width
	        izoManager.intervalIZO = bufferDistance;
	    }
  


}
