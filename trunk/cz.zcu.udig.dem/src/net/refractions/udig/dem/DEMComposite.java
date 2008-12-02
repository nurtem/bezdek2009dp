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

import java.awt.TextField;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/***************************************************************
 * The class for manage composite of dialog
 * @author Josef Bezdek
 *
 */
public class DEMComposite extends Composite{
	//private DEMDialog theApp;
	Group groupTIN = null;
	Group groupIZO = null;
	Group groupGrid = null;
	Composite compoInfo = null;
	private SashForm sashForm = null;
	private ViewForm viewForm = null;
	
	public Button radioS1;
	public Button radioS2;
	public Button radioS3;
	public Button radioS4;
	public Button radioS5;
	public Button radioS6;
	public Button radioS7;
	public Button radioS8;
	public Button radioS0;
	public ComboViewer sourcePointsViewer = null;
	public ComboViewer sourceZViewer = null;
	public ComboViewer hardBreakLinesViewer = null;
	public ComboViewer softBreakLinesViewer = null;
	public Button buttonTIN;
	public Button buttonIZO;
	Text textBufferWidth;
	
	/*******************************************************************
	 * Constructor
	 * @param dialog main dialog 
	 * @param parent main composite of dialog
	 * @param style type of style
	 */
	public DEMComposite( DEMDialog dialog, Composite parent, int style ) {
		super(parent, style);
        createContent();        
    }
	
	/*******************************************************************
	 * The method for creating main composite of dialog
	 */
	private void createContent() {
	    
	 	sashForm = new SashForm(this,SWT.NONE);
	     	            
	  	createCompositeTIN(sashForm);
	    	    	
	    viewForm = new ViewForm(sashForm, SWT.NONE);
	    viewForm.setLayout(new FillLayout());	
	        
	    Composite compositeInfo = createCompositeInfo(viewForm);
	    viewForm.setTopLeft(compositeInfo);
	
	    Composite compositeIZO =createCompositeIZO(viewForm);
	    viewForm.setContent(compositeIZO);
	
	    
	       //must go after populating the sash whit composites
	    sashForm.setWeights(new int[] { 25, 72 });
	}
	
	/*****************************************************************
	 * Private method for creating composite IZO of main composite parent
	 * @param parent main composite of dialog
	 * @return composite IZO
	 */
	private Composite createCompositeIZO(final Composite parent) {
	        GridLayout layout = new GridLayout();
	        //layout.numColumns = 1;
	        
	        GridData gridData = new GridData();
	        gridData.horizontalAlignment = GridData.FILL;
	        gridData.grabExcessHorizontalSpace = true;
	        gridData.grabExcessVerticalSpace = true;
	        gridData.widthHint = 50;
	        gridData.verticalAlignment = GridData.FILL;
	        
	        groupIZO = new Group(parent, SWT.NULL);
	        groupIZO.setLayoutData(gridData); 
	        groupIZO.setLayout(layout);
	        groupIZO.setText("Surface creator");
	       // createCompositeCommand(compositeTIN);
	        Group groupRadio = new Group(groupIZO, SWT.NULL);
	        groupRadio.setText("Izolines");      
	        groupRadio.setLayoutData(gridData);
	        GridLayout layout6 = new GridLayout();
	        layout6.numColumns = 2;
	        
	        Label label = new Label(groupRadio, SWT.SINGLE);
	        label.setText("set level of dividing triangles");
 
	        Composite radio = new Composite(groupRadio, SWT.NULL);
	        GridLayout layout3 = new GridLayout();
	        layout3.numColumns = 3;
	        groupRadio.setLayout(layout);
	        radio.setLayout(layout3);


	        
	        radioS0 = new Button(radio, SWT.RADIO);
	        radioS0.setText("0...   1 triangle ");
	        radioS0.setEnabled(false);        

	        radioS1 = new Button(radio, SWT.RADIO);
	        radioS1.setText("1...   4 triangles");
	        radioS1.setEnabled(false);

	        radioS2 = new Button(radio, SWT.RADIO);
	        radioS2.setText("2...   9 triangles");
	        radioS2.setEnabled(false);
	        
	        radioS3 = new Button(radio, SWT.RADIO);
	        radioS3.setText("3...  16 triangles");
	        radioS3.setEnabled(false);

	        radioS4 = new Button(radio, SWT.RADIO);
	        radioS4.setText("4...  25 triangles");
	        radioS4.setEnabled(false);

	        radioS5 = new Button(radio, SWT.RADIO);
	        radioS5.setText("5...  36 triangles");
	        radioS5.setEnabled(false);

	        radioS6 = new Button(radio, SWT.RADIO);
	        radioS6.setText("6...  49 triangles");
	        radioS6.setEnabled(false);
	        
	        radioS7 = new Button(radio, SWT.RADIO);
	        radioS7.setText("7...  64 triangles");
	        radioS7.setEnabled(false);

	        radioS8 = new Button(radio, SWT.RADIO);
	        radioS8.setText("9... 100 triangles");
	        radioS8.setEnabled(false);

	        Label label5 = new Label(groupRadio, SWT.SINGLE);
	        label5.setText("set elevated difference:  ");
	        textBufferWidth = new Text(groupRadio, SWT.BORDER | SWT.RIGHT);
	        textBufferWidth.setTextLimit(20);
	        textBufferWidth.setText("      10");
	        
	        
	        textBufferWidth.setEditable(false);
	        
	        buttonIZO = new Button(groupRadio, SWT.BUTTON2);
	        buttonIZO.setText("Count Izolines");
	        buttonIZO.setEnabled(false);
	
	        //setEnabledRadioButton(true);
	        
	 /*      
	        
	        Group groupGrid = new Group(groupIZO, SWT.NULL);
	        groupGrid.setText("Grid");      
	        groupGrid.setLayoutData(gridData);
	        Label label1 = new Label(groupGrid, SWT.SINGLE);
	        label1.setText("set level of dividing triangles");
        
*/
	        /*/radioAffine.addListener(SWT.Selection, listener);
	        radioLinear.addListener(SWT.Selection, listener);
	        radioRubber.addListener(SWT.Selection, listener);
	        radioProjective.addListener(SWT.Selection, listener);
	        refresh();
	    */
        
	        return groupIZO;

	    }
	 
	
	/****************************************************************
	 * Private method for creating composite Info of main composite parent
	 * @param parent main composite of dialog
	 * @return composite Info
	 */
	private Composite createCompositeInfo(final Composite parent) {
	        GridLayout layout = new GridLayout();
	        layout.numColumns = 1;
	        
	        GridData gridData = new GridData();
	        gridData.horizontalAlignment = GridData.FILL;
	        gridData.grabExcessHorizontalSpace = true;
	        gridData.grabExcessVerticalSpace = true;
	        gridData.widthHint = 20;
	        gridData.verticalAlignment = GridData.FILL;
	        
	        compoInfo = new Group(parent, SWT.BORDER);
	        compoInfo.setLayoutData(gridData); 
	        compoInfo.setLayout(layout);
	      // compoInfo.setText("Info:");
	        Label label1 = new Label(compoInfo, SWT.SINGLE);
	        label1.setText("Surface will be computed by a nonlinear interpolation as Bezier surface" );
	       // createCompositeCommand(compositeTIN);
	                
	        return compoInfo;

	    }
	
	
	   private Composite createCompositeGrid(final Composite parent) {
	        GridLayout layout = new GridLayout();
	        layout.numColumns = 1;
	        
	        GridData gridData = new GridData();
	        gridData.horizontalAlignment = GridData.FILL;
	        gridData.grabExcessHorizontalSpace = true;
	        gridData.grabExcessVerticalSpace = true;
	        gridData.widthHint = 20;
	        gridData.verticalAlignment = GridData.FILL;
	        
	        groupGrid = new Group(parent, SWT.BORDER);
	        groupGrid.setLayoutData(gridData); 
	        groupGrid.setLayout(layout);
	        groupGrid.setText("Grid");
	        final ComboViewer comboViewer = new ComboViewer(groupGrid, SWT.SINGLE);
	        comboViewer.setLabelProvider(new LayerLabelProvider());
	        comboViewer.setContentProvider(new ArrayContentProvider());
	   
	       // createCompositeCommand(compositeTIN);
	                
	        return groupGrid;

	    }
	
	/**************************************************************************
	 * Private method for creating composite TIN of main composite parent
	 * @param parent main composite of dialog
	 * @return composite TIN
	 */   
	private Composite createCompositeTIN(final Composite parent) {
	        GridLayout layout = new GridLayout();
	        layout.numColumns = 1;
	        
	        GridData gridData = new GridData();
	        gridData.horizontalAlignment = GridData.FILL;
	        gridData.grabExcessHorizontalSpace = true;
	        gridData.grabExcessVerticalSpace = true;
	        gridData.widthHint = 150;
	        gridData.verticalAlignment = GridData.FILL;
	        
	        groupTIN = new Group(parent, SWT.BORDER);
	        groupTIN.setLayoutData(gridData); 
	        groupTIN.setLayout(layout);
	        groupTIN.setText("TIN");
	       // createCompositeCommand(compositeTIN);

	        IMap map = ApplicationGIS.getActiveMap();
	        //IBlackboard blackboard = map.getBlackboard();
	        Label label1 = new Label(groupTIN, SWT.SINGLE);
	        Label label = new Label(groupTIN, SWT.SINGLE);
	        label1.setSize(28,20);
	        label1.setText("Source:");
	        label.setText("points layer:");
	        sourcePointsViewer = new ComboViewer(groupTIN, SWT.SINGLE);
	        sourcePointsViewer.setLabelProvider(new LayerLabelProvider());
	        sourcePointsViewer.setContentProvider(new ArrayContentProvider());
	        
	        sourcePointsViewer.setInput(LayerManager.getPointsLayers(map.getMapLayers()).toArray());

	        label = new Label(groupTIN, SWT.SINGLE);
	        label.setText("height source:");
	        sourceZViewer = new ComboViewer(groupTIN, SWT.SINGLE);
	        //sourceZViewer.setLabelProvider(new LayerLabelProvider());
	        sourceZViewer.setContentProvider(new ArrayContentProvider());
	        
	        
	        
	        label = new Label(groupTIN, SWT.LEFT);
	        label.setText("__________________");
	        label1 = new Label(groupTIN, SWT.SINGLE);
	        label1.setText("Break lines:");
	        
	        label = new Label(groupTIN, SWT.LEFT);
	        label.setText("hard break lines:");
	        hardBreakLinesViewer = new ComboViewer(groupTIN, SWT.RIGHT);
	        hardBreakLinesViewer.setLabelProvider(new LayerLabelProvider());
	        hardBreakLinesViewer.setContentProvider(new ArrayContentProvider());
	        hardBreakLinesViewer.setInput(LayerManager.getLineStringLayers(map.getMapLayers()).toArray());
	        
	        label = new Label(groupTIN, SWT.LEFT);
	        label.setText("soft break lines:");
	        softBreakLinesViewer = new ComboViewer(groupTIN, SWT.SINGLE);
	        softBreakLinesViewer.setLabelProvider(new LayerLabelProvider());
	        softBreakLinesViewer.setContentProvider(new ArrayContentProvider());
	        softBreakLinesViewer.setInput(LayerManager.getLineStringLayers(map.getMapLayers()).toArray());
	        
	
	        
	        buttonTIN = new Button(groupTIN, SWT.BUTTON2);
	        buttonTIN.setText("Count TIN");
	        buttonTIN.setEnabled(false);
	        return groupTIN;

	    }
	
	   public void setEnabledRadioButton(boolean enabled){
		   radioS0.setEnabled(enabled);
		   radioS1.setEnabled(enabled);
		   radioS2.setEnabled(enabled);
		   radioS3.setEnabled(enabled);
		   radioS4.setEnabled(enabled);
		   radioS5.setEnabled(enabled);
		   radioS6.setEnabled(enabled);
		   radioS7.setEnabled(enabled);
		   radioS8.setEnabled(enabled);
		   
		   
	   }

}
