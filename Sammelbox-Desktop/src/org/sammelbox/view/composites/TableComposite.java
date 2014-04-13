package org.sammelbox.view.composites;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sammelbox.controller.filters.ItemFieldFilterPlusID;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.database.operations.DatabaseConstants;

public class TableComposite {
	private static final String ID_TABLE_DATA_KEY = "ID";
	private static final int CHECKBOX_COLUMN_WIDTH_PIXELS = 25;
	private static final int COLUMN_WIDTH_PIXELS = 125;

	public static Composite build(Composite parentComposite) {
		Composite tableComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout tableCompositeGridLayout = new GridLayout();
		tableComposite.setLayout(tableCompositeGridLayout);

		Table table = new Table(tableComposite, SWT.BORDER | SWT.CHECK);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		for (ItemField itemField : ItemFieldFilterPlusID.getValidItemFields(
				AlbumItemStore.getAlbumItems().get(0).getFields())) {
			
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
			
			if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
				tableColumn.setWidth(CHECKBOX_COLUMN_WIDTH_PIXELS);
			} else if (!itemField.getType().equals(FieldType.ID)) {
				tableColumn.setText(itemField.getName());
				tableColumn.setWidth(COLUMN_WIDTH_PIXELS);
			}
		}

		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems()) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			String[] values = new String[albumItem.getFields().size()];

			int i=0;
			for (ItemField itemField : ItemFieldFilterPlusID.getValidItemFields(albumItem.getFields())) {
				if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
					tableItem.setData(ID_TABLE_DATA_KEY, itemField.getValue());
					i++;
				} else if (!itemField.getType().equals(FieldType.ID)) {
					values[i++] = String.valueOf(itemField.getValue());
				}
			}
						
			tableItem.setText(values);
		}

		Composite buttonComposite = new Composite(tableComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(5, false));
		
		Button btnEdit = new Button(buttonComposite, SWT.PUSH);
		btnEdit.setText(Translator.toBeTranslated("Edit Row"));

		Button btnCloneRow = new Button(buttonComposite, SWT.PUSH);
		btnCloneRow.setText(Translator.toBeTranslated("Clone Row"));

		Button btnDelete = new Button(buttonComposite, SWT.PUSH);
		btnDelete.setText(Translator.toBeTranslated("Delete Row"));
		
		// min height griddata
		GridData minSizeGridData = new GridData(GridData.FILL_BOTH);
		minSizeGridData.widthHint = 10;
		minSizeGridData.heightHint = 10;
		// separator
		new Label(buttonComposite, SWT.SEPARATOR | SWT.VERTICAL).setLayoutData(minSizeGridData);
		
		Button btnAddItems = new Button(buttonComposite, SWT.PUSH);
		btnAddItems.setText(Translator.toBeTranslated("Add Items"));

		return tableComposite;
	}
}
