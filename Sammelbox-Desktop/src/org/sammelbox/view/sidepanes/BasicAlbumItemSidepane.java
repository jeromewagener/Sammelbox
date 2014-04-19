/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.view.sidepanes;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.WelcomePageManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.image.ImageDropAndManagementComposite;
import org.sammelbox.view.various.ComponentFactory;
import org.sammelbox.view.various.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BasicAlbumItemSidepane {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAlbumItemSidepane.class);
	private static final int BASIC_ALBUM_SIDEPANE_WIDTH_IN_PIXELS = 200;

	private BasicAlbumItemSidepane() {
		// use build method instead
	}
	
	/** Returns a "basic album item" composite. This composite provides the fields (field names and value input fields)
	 *  needed by the add item composite.
	 * @param parentComposite the parent composite
	 * @param album the name of the album to which an item should be added
	 * @param caption the caption/header of the basic album item composite.
	 * @return a new "basic album item" composite */
	static Composite build(Composite parentComposite, final String album) {
		return build(parentComposite, album, 0, false);
	}

	/** Returns a "basic album item" composite. This composite provides the fields (field names and value input fields) needed by the 
	 * update item composite. The content from the specified album item is used to fill the different album fields.
	 * @param parentComposite the parent composite
	 * @param album the name of the album to which the item should should be updated
	 * @param albumItemId the id of the album item which should be used to fill the fields
	 * @param caption the caption/header of the basic album item composite.
	 * @return a new "basic album item" composite */
	static Composite build(Composite parentComposite, final String album, final long albumItemId) {		
		return build(parentComposite, album, albumItemId, true);
	}

	/** Returns a "basic album item" composite. This composite provides the fields (field names and value input fields) needed by the
	 * add and update item composites.
	 * @param parentComposite the parent composite
	 * @param album the name of the album to which an item should be added, or an item should be updated
	 * @param albumItemId the id of the album item which should be used to fill the fields. In case of the "add" composite, this id is not
	 * required and can be set to any value. However the loadDataIntoFields should be set accordingly.
	 * @param caption the caption/header of the basic album item composite.
	 * @param loadDataIntoFields if the content of the specified album item should be loaded into the fields, then this should be true.
	 * If it should not be loaded (E.g. in case of the "add" composite, then this should be false
	 * @return a new "basic album item" composite */
	static Composite build(Composite parentComposite, final String album, final long albumItemId, boolean loadDataIntoFields) {
		// setup the basic composite
		final Composite basicAlbumItemComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		basicAlbumItemComposite.setLayout(gridLayout);
		basicAlbumItemComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		AlbumItem albumItem = null;
		java.util.List<MetaItemField> metaItemFields = new ArrayList<MetaItemField>();
		
		boolean addPictureComposite = false;
		try {
			// if data should be loaded, it must be fetched from the database
			if (loadDataIntoFields) {
				albumItem = DatabaseOperations.getAlbumItem(album, albumItemId);
			}	

			// fetch the field names and types from the database
			metaItemFields = DatabaseOperations.getMetaItemFields(album);
			
			// if the album contains pictures, show the picture composite
			addPictureComposite = DatabaseOperations.isPictureAlbum(album);
		} catch(DatabaseWrapperOperationException ex) {
			LOGGER.error("A database related error occured", ex);
		}

		for (MetaItemField metaItem : metaItemFields) {
			String fieldName = metaItem.getName();
			FieldType fieldType = metaItem.getType();

			// TODO is this check still necessary? see the switch below
			// Do not show the id field!
			if (fieldName.equals("id") || fieldName.equals("typeinfo")) {
				continue;
			}

			switch (fieldType) {
			case ID:
				// not shown in sidepane
				break;
			case UUID:
				// not shown in sidepane
				break;
			case TEXT: 
				addTextComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			case URL: 
				addURLComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			case DECIMAL:
				addDecimalNumberComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			case INTEGER:
				addIntegerNumberComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			case DATE:
				addDateComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			case TIME:
				addTimeComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			case STAR_RATING:
				addStarRatingComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			case OPTION:
				addOptionComponent(basicAlbumItemComposite, albumItem, fieldName, loadDataIntoFields);
				break;
			}
		}

		if (addPictureComposite) {
			try {
				ImageDropAndManagementComposite imageDropAndManagementComposite;
				List<AlbumItemPicture> pictures;
				
				if (loadDataIntoFields) {
					pictures = DatabaseOperations.getAlbumItemPictures(ApplicationUI.getSelectedAlbum(), albumItemId);
					imageDropAndManagementComposite = new ImageDropAndManagementComposite(basicAlbumItemComposite, pictures);
				} else {
					imageDropAndManagementComposite = new ImageDropAndManagementComposite(basicAlbumItemComposite);					
				}
				
				imageDropAndManagementComposite.setData(AlbumItemPicture.ALBUM_ITEM_PICTURE, AlbumItemPicture.ALBUM_ITEM_PICTURE);
				
			} catch (DatabaseWrapperOperationException ex) {
				LOGGER.error("An error occured while fetching the images for the album item #'" + albumItemId + 
						"' from the album '" + ApplicationUI.getSelectedAlbum() + "'", ex);
			}
		}

		return basicAlbumItemComposite;
	}

	// TODO extract to own class
	/** Returns a selection listener suitable for the add and update composite.
	 * @param composite the composite to which the listener should be attached
	 * @param isUpdateAlbumItemComposite if true, the listener is used for the update composite, otherwise for the add composite
	 * @param albumItemId the albumItemId is only used in case isUpdateAlbumItemComposite is set to true
	 * @return a new selection listener suitable for the add and update composite*/
	public static SelectionListener getSelectionListenerForAddAndUpdateAlbumItemComposite(final Composite composite, final boolean isUpdateAlbumItemComposite, final long albumItemId) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
					return;
				}
				
				AlbumItem albumItem = new AlbumItem(ApplicationUI.getSelectedAlbum());
				
				for (Control control : composite.getChildren()) {
					if (control.getData(AlbumItemPicture.ALBUM_ITEM_PICTURE) != null) {
						ImageDropAndManagementComposite imageDropAndManagementComposite = (ImageDropAndManagementComposite) control;
						albumItem.setPictures(imageDropAndManagementComposite.getAllPictures());
					
					} else if (control.getData("FieldType") != null) {
						FieldType fieldType = (FieldType) control.getData("FieldType");
						
						if(fieldType.equals(FieldType.TEXT)) {
							Text text = (Text) control;
							albumItem.addField((String) text.getData("FieldName"), (FieldType) text.getData("FieldType"), text.getText());
						
						} else if (fieldType.equals(FieldType.URL)) {
							Text url = (Text) control;
							albumItem.addField((String) url.getData("FieldName"), (FieldType) url.getData("FieldType"), url.getText());
						
						} else if (fieldType.equals(FieldType.DECIMAL)) {
							Text text = (Text) control;
							double number = 0.0;

							if (!text.getText().isEmpty()) {
								number = Double.parseDouble(text.getText());
							}

							albumItem.addField((String) text.getData("FieldName"), (FieldType) text.getData("FieldType"), number);
						
						} else if (fieldType.equals(FieldType.INTEGER)) {
							Text text = (Text) control;
							int integer = 0;

							if (!text.getText().isEmpty()) {
								integer = Integer.parseInt(text.getText());
							}

							albumItem.addField((String) text.getData("FieldName"), (FieldType) text.getData("FieldType"), integer);
						
						} else if (fieldType.equals(FieldType.DATE)) {
							Composite dateComposite = (Composite) control;
							
							for (Control dateControl : dateComposite.getChildren()) {
								if (dateControl.getData("FieldType") != null && ((FieldType) dateControl.getData("FieldType")).equals(FieldType.DATE)) {
									DateTime dateTime = (DateTime) dateControl;
									
									if (!dateTime.getEnabled()) {
										albumItem.addField((String) dateTime.getData("FieldName"), (FieldType) dateTime.getData("FieldType"), null);
									} else {
										Calendar calendar = Calendar.getInstance();
										calendar.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
										albumItem.addField((String) dateTime.getData("FieldName"), (FieldType) dateTime.getData("FieldType"), new Date(calendar.getTimeInMillis()));
									}
								}
							}
						
						} else if (fieldType.equals(FieldType.STAR_RATING)) {							
							Combo combo = (Combo) control;
							albumItem.addField((String) combo.getData("FieldName"), (FieldType) combo.getData("FieldType"), StarRating.values()[combo.getSelectionIndex()]);
						
						} else if (fieldType.equals(FieldType.OPTION)) {
							Composite yesNoComposite = (Composite) control;
							for (Control yesNoControl : yesNoComposite.getChildren()) {
								Button radioButton = (Button) yesNoControl;

								if (((Boolean) radioButton.getData("yesButton")) && radioButton.getSelection()) {
									albumItem.addField((String) control.getData("FieldName"), (FieldType) fieldType, OptionType.YES);
								} else if (((Boolean) radioButton.getData("noButton")) && radioButton.getSelection()) {
									albumItem.addField((String) control.getData("FieldName"), (FieldType) fieldType, OptionType.NO);
								} else if (((Boolean) radioButton.getData("unknownButton")) && radioButton.getSelection()) {
									albumItem.addField((String) control.getData("FieldName"), (FieldType) fieldType, OptionType.UNKNOWN);
								}
							}
						}
					}
				}
			
				try {
					if (isUpdateAlbumItemComposite) {
						albumItem.addField("id", FieldType.ID, albumItemId);
						
						DatabaseOperations.updateAlbumItem(albumItem);
						BrowserFacade.generateAlbumItemUpdatedPage(albumItemId);
					} else {						
						// Create album item
						BrowserFacade.generateAlbumItemAddedPage(DatabaseOperations.addAlbumItem(albumItem, true));
					}
					
					GuiController.getGuiState().setUnsavedAlbumItem(false);
					
					// Update GUI
					ApplicationUI.changeRightCompositeTo(PanelType.EMPTY, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
					WelcomePageManager.updateLastModifiedWithCurrentDate(ApplicationUI.getSelectedAlbum());
				} catch (DatabaseWrapperOperationException ex) {
					LOGGER.error("A database related error occured", ex);
				}
			}
		};
	}
	
	private static ModifyListener getModifyListener() {
		return new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				GuiController.getGuiState().setUnsavedAlbumItem(true);
			}
		};
	}
	
	// TODO extract all these into own builders
	private static void addTextComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		Text textText = new Text(
				basicAlbumItemComposite,
				SWT.WRAP
				| SWT.MULTI
				| SWT.BORDER
				| SWT.V_SCROLL
				);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.widthHint = BASIC_ALBUM_SIDEPANE_WIDTH_IN_PIXELS;
		textText.setLayoutData(gridData);
		// Override the normal tab behavior of a multiline text widget.
		// Instead of ctrl+tab a simple text changes focus.
		textText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});
		
		if (loadDataIntoFields) {
			String text = albumItem.getField(fieldName).getValue();
			if (text != null) {
				textText.setText(text);
			}
		}

		textText.setData("FieldType", FieldType.TEXT);
		textText.setData("FieldName", fieldName);
		
		textText.addModifyListener(getModifyListener());
	}
	
	private static void addURLComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		Text url = new Text(
				basicAlbumItemComposite,
				SWT.WRAP
				| SWT.MULTI
				| SWT.BORDER
				| SWT.V_SCROLL);

		url.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		if (loadDataIntoFields) {
			url.setText((String) albumItem.getField(fieldName).getValue());
		}

		url.setData("FieldType", FieldType.URL);
		url.setData("FieldName", fieldName);
		
		url.addModifyListener(getModifyListener());
	}
	
	private static void addDecimalNumberComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		final Text numberText = new Text(basicAlbumItemComposite, SWT.BORDER);
		numberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		if (loadDataIntoFields) {
			numberText.setText(((Double) albumItem.getField(fieldName).getValue()).toString());
		} 
		numberText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				boolean hasPoint = false;
				String newString = e.text;
				hasPoint = numberText.getText().contains(".");

				char[] chars = new char[newString.length()];
				newString.getChars(0, chars.length, chars, 0);

				for (int i = 0; i < chars.length; i++) {
					if (!hasPoint) {
						if (!('0' <= chars[i] && chars[i] <= '9'|| chars[i] == '.')) {
							e.doit = false;
							return;
						}
					} else {
						if (!('0' <= chars[i] && chars[i] <= '9')) {
							e.doit = false;
							return;
						}
					}
				}
			}
		});

		numberText.setData("FieldType", FieldType.DECIMAL);
		numberText.setData("FieldName", fieldName);
		
		numberText.addModifyListener(getModifyListener());
	}
	
	private static void addIntegerNumberComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		final Text integerText = new Text(basicAlbumItemComposite, SWT.BORDER);
		integerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		if (loadDataIntoFields) {
			integerText.setText(((Integer) albumItem.getField(fieldName).getValue()).toString());
		} 
		integerText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String newString = e.text;
				
				char[] chars = new char[newString.length()];
				newString.getChars(0, chars.length, chars, 0);

				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
				
				try {
					if (!integerText.getText().isEmpty()) {
						Integer.parseInt(integerText.getText() + e.text);
					}
				} catch (NumberFormatException nfe) {
					e.doit = false;
					return;
				}
			}
		});

		integerText.setData("FieldType", FieldType.INTEGER);
		integerText.setData("FieldName", fieldName);
		
		integerText.addModifyListener(getModifyListener());
	}
	
	private static void addDateComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		Composite dateComposite = new Composite(basicAlbumItemComposite, SWT.NULL);
		dateComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		dateComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		dateComposite.setData("FieldType", FieldType.DATE);
		dateComposite.setData("FieldName", fieldName);
		
		final Button enableDateButton = new Button(dateComposite, SWT.CHECK);
		enableDateButton.setSelection(true);
		enableDateButton.setText("");
						
		final DateTime datePicker = new DateTime(dateComposite, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
		
		enableDateButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				datePicker.setEnabled(enableDateButton.getSelection());
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});
		
		if (loadDataIntoFields) {
			Date date = albumItem.getField(fieldName).getValue();
			if (date != null) {
				Calendar calendarForDate = Calendar.getInstance();
				calendarForDate.setTimeInMillis(date.getTime());
				datePicker.setDate(calendarForDate.get(Calendar.YEAR), calendarForDate.get(Calendar.MONTH), calendarForDate.get(Calendar.DAY_OF_MONTH));
			} else {
				enableDateButton.setSelection(false);
				datePicker.setEnabled(false);
			}
		}

		datePicker.setData("FieldType", FieldType.DATE);
		datePicker.setData("FieldName", fieldName);
	}
	
	private static void addTimeComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		DateTime timePicker = new DateTime(basicAlbumItemComposite, SWT.BORDER | SWT.TIME | SWT.DROP_DOWN);
		timePicker.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		if (loadDataIntoFields) {
			Time time = albumItem.getField(fieldName).getValue();
			Calendar calendarForTime = Calendar.getInstance();
			calendarForTime.setTimeInMillis(time.getTime());

			timePicker.setTime(calendarForTime.get(Calendar.HOUR), calendarForTime.get(Calendar.MINUTE), calendarForTime.get(Calendar.SECOND));
		}

		timePicker.setData("FieldType", FieldType.TIME);
		timePicker.setData("FieldName", fieldName);
	}
	
	private static void addStarRatingComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		final Combo ratingCombo = new Combo(basicAlbumItemComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		ratingCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		// Fill the comboBox
		ratingCombo.setData("FieldType", FieldType.STAR_RATING);
		ratingCombo.setData("FieldName", fieldName);
		ratingCombo.setItems(StarRating.toComboBoxArray());
		
		if (loadDataIntoFields) {
			ratingCombo.select(((StarRating) albumItem.getField(fieldName).getValue()).getIntegerValue());
		} else {
			ratingCombo.select(0);
		}
	}
	
	private static void addOptionComponent(Composite basicAlbumItemComposite, AlbumItem albumItem, String fieldName, boolean loadDataIntoFields) {
		ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");

		Composite yesNoComposite = new Composite(basicAlbumItemComposite, SWT.NULL);
		yesNoComposite.setLayout(new RowLayout());
		yesNoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		yesNoComposite.setData("FieldType", FieldType.OPTION);
		yesNoComposite.setData("FieldName", fieldName);

		Button yesButton = new Button(yesNoComposite, SWT.RADIO);
		yesButton.setText(Translator.get(DictKeys.BUTTON_YES));
		yesButton.setData("yesButton", true);
		yesButton.setData("noButton", false);
		yesButton.setData("unknownButton", false);
		if (loadDataIntoFields) {
			if (albumItem.getField(fieldName).getValue() == OptionType.YES) {
				yesButton.setSelection(true);
			} else {
				yesButton.setSelection(false);
			}
		}

		Button noButton = new Button(yesNoComposite, SWT.RADIO);
		noButton.setText(Translator.get(DictKeys.BUTTON_NO));
		noButton.setData("yesButton", false);
		noButton.setData("noButton", true);
		noButton.setData("unknownButton", false);
		if (loadDataIntoFields) {
			if (albumItem.getField(fieldName).getValue() == OptionType.NO) {
				noButton.setSelection(true);
			} else {
				noButton.setSelection(false);
			}
		}

		Button unknownButton = new Button(yesNoComposite, SWT.RADIO);
		unknownButton.setText(Translator.get(DictKeys.BUTTON_UNKNOWN));
		unknownButton.setData("yesButton", false);
		unknownButton.setData("noButton", false);
		unknownButton.setData("unknownButton", true);
		if (loadDataIntoFields) {
			if (albumItem.getField(fieldName).getValue() == OptionType.UNKNOWN) {
				unknownButton.setSelection(true);
			} else {
				unknownButton.setSelection(false);
			}
		} else {
			unknownButton.setSelection(true);
		}
	}
}
