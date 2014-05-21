package org.sammelbox.view.sidepanes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO quickly implemented for testing purposes. needs a huge refactoring!
public class AlbumFunctionSidepane extends Composite {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUI.class);

	public AlbumFunctionSidepane(Composite parentComposite) {
		super(parentComposite, SWT.NONE);
		initialize();
	}

	private void initialize() {
		this.setLayout(new GridLayout(1, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ComponentFactory.getPanelHeaderComposite(this, Translator.get(DictKeys.LABEL_AGGREGATION_FUNCTIONS));
		
		Composite innerComposite = new Composite(this, SWT.NONE);
		innerComposite.setLayout(new GridLayout(2, false));

		Label albumFieldLabel = new Label(innerComposite, SWT.NONE);
		albumFieldLabel.setText(Translator.get(DictKeys.LABEL_ALBUM_FIELD));

		GridData gridData = new GridData();
		gridData.widthHint = 160;

		final Combo albumFieldCombo = new Combo(innerComposite, SWT.READ_ONLY|SWT.BORDER|SWT.H_SCROLL);
		albumFieldCombo.setLayoutData(gridData);

		final Map<String, FieldType> albumItemFieldNamesToTypes;
		try {
			albumItemFieldNamesToTypes = DatabaseOperations.getAlbumItemFieldNameToTypeMap(GuiController.getGuiState().getSelectedAlbum());
			List<String> functionFields = new ArrayList<String>();
			for (Entry<String, FieldType> fieldNameToType : albumItemFieldNamesToTypes.entrySet()) {
				if (fieldNameToType.getValue().equals(FieldType.DECIMAL) || fieldNameToType.getValue().equals(FieldType.INTEGER)) {
					functionFields.add(fieldNameToType.getKey());
				}
			}

			String[] functionFieldsArray = new String[functionFields.size()];
			for (int i=0; i<functionFields.size(); i++) {
				functionFieldsArray[i] = functionFields.get(i);
			}

			albumFieldCombo.setItems(functionFieldsArray);			

			Label functionLabel = new Label(innerComposite, SWT.NONE);
			functionLabel.setText(Translator.get(DictKeys.LABEL_ALBUM_FUNCTION));

			final Combo functionCombo = new Combo(innerComposite, SWT.READ_ONLY|SWT.BORDER|SWT.H_SCROLL);
			functionCombo.setLayoutData(gridData);
			functionCombo.setItems(new String[] { 
					Translator.get(DictKeys.COMBOBOX_CONTENT_SUM), 
					Translator.get(DictKeys.COMBOBOX_CONTENT_AVERAGE), 
					Translator.get(DictKeys.COMBOBOX_CONTENT_MAX),
					Translator.get(DictKeys.COMBOBOX_CONTENT_MIN) });

			new Label(innerComposite, SWT.NONE);
			new Label(innerComposite, SWT.NONE);
			new Label(innerComposite, SWT.NONE);
			Button calculateButton = new Button(innerComposite, SWT.NONE);
			calculateButton.setText(Translator.get(DictKeys.BUTTON_EXECUTE_AGGREGATION_FUNCTION));
			calculateButton.setLayoutData(gridData);
			calculateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (functionCombo.getSelectionIndex() == -1 || albumFieldCombo.getSelectionIndex() == -1) {
						ComponentFactory.showErrorDialog(ApplicationUI.getShell(), 
								Translator.get(DictKeys.ERROR_NO_FIELD_OR_FUNCTION_SELECTED_HEADER), 
								Translator.get(DictKeys.ERROR_NO_FIELD_OR_FUNCTION_SELECTED));
						return;
					}
					
					String function = functionCombo.getItem(functionCombo.getSelectionIndex());
					FieldType fieldType = albumItemFieldNamesToTypes.get(albumFieldCombo.getItem(albumFieldCombo.getSelectionIndex()));
										
					Double result = 0.0d;
					Double min = Double.MAX_VALUE;
					Double max = Double.MIN_VALUE;

					for (AlbumItem albumItem : AlbumItemStore.getAlbumItems()) {
						for (ItemField itemField : albumItem.getFields()) {
							if (itemField.getName().equals(albumFieldCombo.getItem(albumFieldCombo.getSelectionIndex()))) {							
								if (function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_SUM)) || 
										function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_AVERAGE))) 
								{
									if (fieldType.equals(FieldType.INTEGER)) {
										result += (Integer) itemField.getValue();
										break;
									} else if (fieldType.equals(FieldType.DECIMAL)) {
										result += (Double) itemField.getValue();
										break;
									}
								} else if (function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_MIN)) || 
										function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_MAX)))
								{
									if (fieldType.equals(FieldType.INTEGER)) {
										Double value = ((Integer) itemField.getValue()).doubleValue();
										if (value <= min) {
											min = value;
										}
										
										if (value >= max) {
											max = value;
										}
										
										break;
									} else if (fieldType.equals(FieldType.DECIMAL)) {
										Double value = (Double) itemField.getValue();
										if (value <= min) {
											min = value;
										}
										
										if (value >= max) {
											max = value;
										}
										
										break;
									}
								}
							}
						}
					}

					if (function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_AVERAGE))) {
						result /= AlbumItemStore.getAlbumItems().size();
					} else if (function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_MIN))) {
						result = min;
					} else if (function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_MAX))) {
						result = max;
					}

					if (fieldType.equals(FieldType.INTEGER) && (!function.equals(Translator.get(DictKeys.COMBOBOX_CONTENT_AVERAGE)))) {
						ComponentFactory.getMessageBox(
								Translator.get(DictKeys.DIALOG_TITLE_AGGREGATION_RESULT), 
								Translator.get(DictKeys.DIALOG_CONTENT_AGGREGATION_RESULT, String.valueOf(result.intValue())), 
								SWT.ICON_INFORMATION).open();
					} else {
						DecimalFormat df = new DecimalFormat("#.##");
						ComponentFactory.getMessageBox(
								Translator.get(DictKeys.DIALOG_TITLE_AGGREGATION_RESULT), 
								Translator.get(DictKeys.DIALOG_CONTENT_AGGREGATION_RESULT, df.format(result)), 
								SWT.ICON_INFORMATION).open();
					}
				}
			});
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("An error occurred.", e);
		}
	}
}
