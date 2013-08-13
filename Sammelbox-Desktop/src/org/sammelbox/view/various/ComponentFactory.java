/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.view.various;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tracker;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.sidepanes.EmptySidepane;

public class ComponentFactory {
	/** Returns a styled label which is small bold and italic 
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @return a new label */
	public static Label getSmallBoldItalicLabel(Composite parentComposite, String textForLabel) {
		Label label = new Label(parentComposite, SWT.NONE);
		label.setText(textForLabel);

		FontData fontData = label.getFont().getFontData()[0];
		fontData.setStyle(SWT.BOLD | SWT.ITALIC);
		fontData.setHeight(fontData.getHeight() - 1);

		label.setFont(new Font(parentComposite.getDisplay(), fontData));

		return label;
	}

	/** Returns a styled label which is small and italic 
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @return a new label */
	public static Label getSmallItalicLabel(Composite parentComposite, String textForLabel) {
		Label label = new Label(parentComposite, SWT.NONE);
		label.setText(textForLabel);

		FontData fontData = label.getFont().getFontData()[0];
		fontData.setStyle(SWT.ITALIC);
		fontData.setHeight(fontData.getHeight() - 1);

		label.setFont(new Font(parentComposite.getDisplay(), fontData));

		return label;
	}

	/** Returns a styled label which is small bold and italic. Additionally a label style can be provided.
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @param style a SWT style (E.g SWT.BORDER)
	 * @return a new label */
	public static Label getSmallBoldItalicLabel(Composite parentComposite, String textForLabel, int style) {
		Label label = new Label(parentComposite, style);
		label.setText(textForLabel);

		FontData fontData = label.getFont().getFontData()[0];
		fontData.setStyle(SWT.BOLD | SWT.ITALIC);
		fontData.setHeight(fontData.getHeight() - 1);

		label.setFont(new Font(parentComposite.getDisplay(), fontData));

		return label;
	}

	/** Returns a H1 label similar to the HTML H1 tag 
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @return a new label */
	public static Label getH1Label(Composite parentComposite, String textForLabel) {
		return getLabel(parentComposite, textForLabel, 16); // big
	}

	/** Returns a H2 label similar to the HTML H2 tag 
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @return a new label */
	public static Label getH2Label(Composite parentComposite, String textForLabel) {
		return getLabel(parentComposite, textForLabel, 14); // medium
	}

	/** Returns a H3 label similar to the HTML H3 tag 
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @return a new label */
	public static Label getH3Label(Composite parentComposite, String textForLabel) {
		return getLabel(parentComposite, textForLabel, 12); // small
	}

	/** Returns a H4 label similar to the HTML H3 tag 
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @return a new label */
	public static Label getH4Label(Composite parentComposite, String textForLabel) {
		return getLabel(parentComposite, textForLabel, 10); // smaller
	}

	/** Returns a standard SWT label
	 * @param parentComposite the parent of the label 
	 * @param textForLabel the text for the label 
	 * @param fontSize the font size for the label
	 * @return a new SWT label */
	private static Label getLabel(Composite parentComposite, String textForLabel, int fontSize) {
		Label label = new Label(parentComposite, SWT.NONE);
		label.setText(textForLabel);
		label.setFont(new Font(parentComposite.getDisplay(), label.getFont().getFontData()[0].getName(), fontSize, SWT.BOLD));

		return label;
	}

	/** Returns a messageBox
	 * @param parentComposite The parent of the message box 
	 * @param title The title/caption of the message box
	 * @param text The text shown within the message box
	 * @param style A SWT style constant (E.g. SWT.ICON_ERROR)
	 * @return A new message box */
	public static MessageBox getMessageBox(Composite parentComposite, String title, String text, int style) {
		MessageBox messageBox = new MessageBox(parentComposite.getShell(), style);
		messageBox.setText(title);
		messageBox.setMessage(text);

		return messageBox;
	}
	
	/**
	 * Convenience method to open a simple error message box.
	 * @param parentComposite The parent of the message box 
	 * @param titleText he title/caption of the message box
	 * @param messageText The text shown within the message box
	 */
	public static void showErrorDialog(Composite parentComposite, String titleText, String messageText) {
		int errorMessageBoxStyle = SWT.ERROR;
		MessageBox errorMessageBox = getMessageBox(parentComposite, titleText, messageText, errorMessageBoxStyle);
		errorMessageBox.open();
	}
	
	/**
	 * Convenience method to open a simple message box with a yes and no button.
	 * @param parentComposite The parent of the message box 
	 * @param titleText he title/caption of the message box
	 * @param messageText The text shown within the message box
	 */
	public static boolean showYesNoDialog(Composite parentComposite, String titleText, String messageText) {
		int questionMessageBoxStyle = SWT.ICON_QUESTION |SWT.YES | SWT.NO;
		MessageBox questionMessageBox= getMessageBox(parentComposite, titleText, messageText, questionMessageBoxStyle);
		int messageBoxResultFlag = questionMessageBox.open();
		if ( messageBoxResultFlag  == SWT.YES) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * Returns a panel header which is normally used for the right panel of the three-panel-solution
	 * @param panelComposite the panel (right panel) to which the header should be added
	 * @param headerLabelString the string to be shown within the header
	 * @return a composite containing the panel header
	 */
	public static Composite getPanelHeaderComposite(final Composite panelComposite, String headerLabelString) {
		Composite headerComposite = new Composite(panelComposite, SWT.NONE);
		headerComposite.setLayout(new GridLayout(4, false));
		GridData headerGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		headerGridData.minimumHeight = 35;
		
		headerComposite.setLayoutData(headerGridData);

		ComponentFactory cf = new ComponentFactory();

		InputStream istream = cf.getClass().getClassLoader().getResourceAsStream("graphics/resize.png");
		Image resizeImage = new Image(Display.getCurrent(), istream);
		Label resizeLabel = new Label(headerComposite, SWT.NONE);
		resizeLabel.setImage(resizeImage);
		resizeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		resizeLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {}

			@Override
			public void mouseDown(MouseEvent arg0) {
				Tracker tracker = new Tracker(panelComposite.getParent(), SWT.RESIZE | SWT.LEFT);
				tracker.setStippled(false);
				Rectangle originalPanelRectangle = panelComposite.getBounds();
				tracker.setRectangles(new Rectangle[] { originalPanelRectangle });
				if (tracker.open()) {
					Rectangle panelRectangleAfterMovement = tracker.getRectangles()[0];
					ApplicationUI.resizeRightCompositeTo(panelRectangleAfterMovement.width);
				}
				tracker.dispose();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});
		resizeLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Label headerLabel = ComponentFactory.getH2Label(headerComposite, headerLabelString);
		headerLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		headerLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		
		Label fillerLabel = ComponentFactory.getH1Label(headerComposite, "");
		fillerLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		istream = cf.getClass().getClassLoader().getResourceAsStream("graphics/close.png");
		Image closeImage = new Image(Display.getCurrent(), istream);
		Button closeButton = new Button(headerComposite, SWT.PUSH);  
		closeButton.setImage(closeImage);
		closeButton.setToolTipText(Translator.get(DictKeys.BUTTON_TOOLTIP_CLOSE));
		closeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		closeButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		closeButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				ApplicationUI.changeRightCompositeTo(PanelType.Empty, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
			}

			@Override
			public void mouseDown(MouseEvent e) {}

			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});

		// min height griddata
		GridData minHeightGridData = new GridData(GridData.FILL_BOTH);
		minHeightGridData.minimumHeight = 10;

		// separator
		new Label(panelComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(minHeightGridData);

		return headerComposite;
	}
}
