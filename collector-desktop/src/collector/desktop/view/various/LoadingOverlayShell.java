package collector.desktop.view.various;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import collector.desktop.view.internationalization.Translator;


public class LoadingOverlayShell {

	private Shell innerOverlay = null;
	private Canvas canvas = null;
	private Shell parent = null;
	private Font textFont = null;
	private Color textColor = null;
	private String displayedText = Translator.toBeTranslated("Loading ...");
	private boolean done = false;
	private boolean closeParentWhenDone = false;
	
	public LoadingOverlayShell(Shell parentShell, String loadingMessage) { 
		int style = SWT.APPLICATION_MODAL| SWT.NO_TRIM;
		parent = parentShell;
		innerOverlay  = new Shell (parentShell, style);
		innerOverlay.setLayout(new FillLayout());
		innerOverlay.setBackground(parentShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		innerOverlay.setAlpha(180);
		
		
		displayedText = loadingMessage;
		textFont = new Font(parentShell.getDisplay(),"Verdana",22,SWT.BOLD | SWT.ITALIC); 
		textColor = parentShell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);

		canvas = new Canvas(innerOverlay, SWT.DOUBLE_BUFFERED );
		canvas.addPaintListener(getPaintListener());
	}
	
	public LoadingOverlayShell(Shell parentShell, String loadingMessage, Font fontOfText, Color colorOfText) {
		int style = SWT.APPLICATION_MODAL| SWT.NO_TRIM;
		innerOverlay  = new Shell (parentShell, style);
		innerOverlay.setLayout(new FillLayout());
		innerOverlay.setBounds(parentShell.getDisplay().map(parentShell, null, parentShell.getClientArea()));
		innerOverlay.setBackground(parentShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		innerOverlay.setAlpha(180);
		parent = parentShell;

		displayedText = loadingMessage;
		textFont = fontOfText;
		textColor = colorOfText;

		canvas = new Canvas(innerOverlay, SWT.DOUBLE_BUFFERED );

		canvas.addPaintListener(getPaintListener());
	}
	
	public void start() {
		innerOverlay.setBounds(parent.getDisplay().map(parent, null, parent.getClientArea()));
		innerOverlay.open();
		innerOverlay.getDisplay().asyncExec(new Runnable(){
			@Override
			public void run() {	
				if (!innerOverlay.isDisposed()){
					canvas.redraw();
				}
			}
		});

		innerOverlay.open();
	}
	
	private void drawTextOnGC(String text,  GC gc) {
		gc.setFont(textFont);
		gc.setForeground(textColor);
		Point textSize = gc.stringExtent(text);
		int textPositionX = (innerOverlay.getClientArea().width - textSize.x)/2;
		int textPositionY = (innerOverlay.getClientArea().height - textSize.y)/2;
		gc.drawText(text,textPositionX, textPositionY, SWT.NO_BACKGROUND);
	}
	
	public void stop() {
		innerOverlay.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {				
				innerOverlay.close();
				done = true;
				if (closeParentWhenDone){
					parent.close();
				}
			}
		}); 
	}
	
	private PaintListener getPaintListener() {
		return new PaintListener() {			
			@Override
			public void paintControl(PaintEvent e) {
				drawTextOnGC(displayedText, e.gc);		
			}
		};
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void resetDone() {
		done = false;
	}
	
	public void setCloseParentWhenDone(boolean closeParentWhenDone) {
		this.closeParentWhenDone = closeParentWhenDone;
	}
}
