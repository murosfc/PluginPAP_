import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;

import java.awt.AWTEvent;

import ij.IJ;
import ij.process.ImageProcessor;

public class PluginPAP_ implements PlugIn, DialogListener {	
	private GenericDialog gui;
	private ImagePlus image, bkpImage;
	private ImageProcessor processor, processorBkp;
	private double bri,con,sol,sat;
	
	public void run(String arg) {
		bkpImage = IJ.getImage().duplicate();
		bkpImage.setTitle(IJ.getImage().getTitle());
		image = IJ.getImage();
		processor = image.getProcessor();
		processorBkp = this.bkpImage.getProcessor();	
		if (bkpImage.getType() != ImagePlus.COLOR_RGB) {
			IJ.error("In order to run this plugin, the image must be Type RGB");
		}
		else this.showGUI();			
	}
	
	public void showGUI(){
		bri = con = sol = 0;
		sat = 1;
		this.gui = new GenericDialog("Image adjust");
		this.gui.addDialogListener(this);
		gui.addSlider("Bright",-255,255,this.bri,1);		
		gui.addSlider("Contrast",-255,255,this.con,1);
		gui.addSlider("Solarization",0,255,this.sol,1);
		gui.addSlider("Desaturation",0,1,this.sat,0.01);
		gui.showDialog();
		if (gui.wasOKed()) {			
			IJ.log("Saved");
		}
		if (gui.wasCanceled()) {
			IJ.getImage().close();
			this.bkpImage.show();			
			IJ.showMessage("No changes saved");
		}
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {		
		bri = this.gui.getNextNumber();
		con = this.gui.getNextNumber();
		sol = this.gui.getNextNumber();
		sat = this.gui.getNextNumber();		
		changeImage();	
		return true;
	}
	
	private int pixelValidation(int pixel) {
		if (pixel > 255) {
			return 255;
		}
		else if (pixel < 0) {
			return 0;
		}
		else return pixel;
	}

	private void changeImage() {		
		int x, y, pixelValue[] = {0,0,0};
		double factor = (259 * (con + 255))/(255 * ( 259 - con));
		for (x = 0; x < image.getWidth(); x++) {
			for (y = 0; y < image.getHeight(); y++) {
				pixelValue = processorBkp.getPixel(x, y, pixelValue);	
				for (int i=0 ;i<3; i++) {
					//bright
					if(bri != 0) {
						pixelValue[i] = pixelValidation(pixelValue[i] + (int) bri);
					}
					//contrast
					if(con != 0) {						
						pixelValue[i] = pixelValidation((int) (factor * ((pixelValue[i]-128)+128)));						
					}
					//solarization
					if(sol != 0 && pixelValue[i] != sol) {					
						pixelValue[i] = 255 - pixelValue[i];
					}
					//saturation
					double Y = 0.299 * pixelValue[0] + 0.587 * pixelValue[1] + 0.114 * pixelValue[2];					
					pixelValue[i] = (int) (Y + sat * (pixelValue[i] - Y));							
				}
				processor.putPixel(x, y, pixelValue);
			}
		}
		image.updateAndDraw();
	} 	
}
	