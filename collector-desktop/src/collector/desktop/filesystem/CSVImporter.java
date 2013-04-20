package collector.desktop.filesystem;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import collector.desktop.database.AlbumItem;
import collector.desktop.database.DatabaseWrapper;
import collector.desktop.database.FieldType;
import collector.desktop.database.OptionType;
import collector.desktop.gui.ImageManipulator;

public class CSVImporter {
	// TODO make generic - currently only for testing purposes
	public static void importCSV() {
		try{
			FileInputStream fstream = new FileInputStream("/home/luxem/Desktop/Export_Postkaarten.csv");

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String strLine;

			int counter = 1;

			while ((strLine = br.readLine()) != null)   { // && counter < 2
				try {
					System.out.println(" Item #" + counter++);
					System.out.println (strLine);

					String[] fields = strLine.split("\\$");

					AlbumItem albumItem = new AlbumItem("Postkarten");

					albumItem.addField("Ortschaft", FieldType.Text, fields[0]);
					albumItem.addField("Motiv", FieldType.Text, fields[1]);
					albumItem.addField("Herausgeber", FieldType.Text, fields[2]);
					albumItem.addField("Photograph", FieldType.Text, fields[3]);

					if (fields[4].equals("ja") || fields[4].equals("Ja")) {
						albumItem.addField("Farbig", FieldType.Option, OptionType.Yes);
					} else if (fields[4].equals("nein") || fields[4].equals("Nein")) {
						albumItem.addField("Farbig", FieldType.Option, OptionType.No);
					} else {
						albumItem.addField("Farbig", FieldType.Option, OptionType.Option);
					}

					if (fields[5].equals("ja") || fields[5].equals("Ja")) {
						albumItem.addField("Gelaufen", FieldType.Option, OptionType.Yes);
					} else if (fields[5].equals("nein") || fields[5].equals("Nein")) {
						albumItem.addField("Gelaufen", FieldType.Option, OptionType.No);
					} else {
						albumItem.addField("Gelaufen", FieldType.Option, OptionType.Option);
					}


					SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yy");
					java.util.Date javaUtilDate = null;
					if (!fields[6].equals("") && !fields[6].equals("?")) {
						try {
							javaUtilDate = sdf.parse(fields[6]);
						} catch (ParseException pe) {
							pe.printStackTrace();
						}
					}

					if (javaUtilDate != null) {
						albumItem.addField("Datum", FieldType.Date, new Date(javaUtilDate.getTime()));
					} else {
						albumItem.addField("Datum", FieldType.Date, new Date(System.currentTimeMillis()));
					}
					
					fields[7] = fields[7].trim();
					if (fields[7].indexOf(" ") != -1) {
						albumItem.addField("Kaufpreis in €", FieldType.Number, Double.parseDouble(fields[7].substring(0, fields[7].indexOf(" "))));
					}
					
					sdf = new SimpleDateFormat("mm/dd/yy");
					javaUtilDate = null;
					if (!fields[8].equals("") && !fields[8].equals("?")) {
						try {
							javaUtilDate = sdf.parse(fields[8]);
						} catch (ParseException pe) {
							pe.printStackTrace();
						}
					}

					if (javaUtilDate != null) {
						albumItem.addField("Gekauft am", FieldType.Date, new Date(javaUtilDate.getTime()));
					} else {
						albumItem.addField("Gekauft am", FieldType.Date, new Date(System.currentTimeMillis()));
					}
					
					albumItem.addField("Kaufplatform", FieldType.Text, fields[9]);
					albumItem.addField("Verkäufer", FieldType.Text, fields[10]);

					if (fields[11].equals("ja") || fields[11].equals("Ja")) {
						albumItem.addField("Im Bestand", FieldType.Option, OptionType.Yes);
					} else if (fields[11].equals("nein") || fields[11].equals("Nein")) {
						albumItem.addField("Im Bestand", FieldType.Option, OptionType.No);
					} else {
						albumItem.addField("Im Bestand", FieldType.Option, OptionType.Option);
					}

					if (fields.length >= 13) {
						albumItem.addField("Verkauft an", FieldType.Text, fields[12]);
					}

					if (fields.length >= 14) {
						albumItem.addField("Bemerkungen", FieldType.Text, fields[13]);
					}

					if (fields.length >= 15) {
						albumItem.addField("Scan-Name", FieldType.Text, fields[14]);

						if (!fields[14].equals("Nur_Foto") && !fields[14].equals("pas de scans")) {

							fields[14] = fields[14].trim();
							String scan_name_pieces[] = fields[14].split("_");

							String shortcut = scan_name_pieces[0];
							String type = scan_name_pieces[1];

							String number = "";
							for (int i=2; i<scan_name_pieces.length; i++) {
								if (number.equals("")) {
									number += scan_name_pieces[i];
								} else {
									number += "_" + scan_name_pieces[i];
								}
							}

							albumItem.addField("collectorPicture" , FieldType.Picture, getImageURIs(shortcutToVillage.get(shortcut), type, number));
						} else {
							albumItem.addField("collectorPicture" , FieldType.Picture, new ArrayList<URI>());
						}

					} else {
						albumItem.addField("collectorPicture" , FieldType.Picture, new ArrayList<URI>());
					}

					DatabaseWrapper.addNewAlbumItem(albumItem, false, true);
				} catch (Exception ex) {
					ex.printStackTrace();
					break;
				}
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HashMap<String, String> shortcutToVillage = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("Aspelt", "Aspelt");
			put("Bast", "Bastogne");
			put("Beauf", "Beaufort");
			put("Beiler", "Beiler");
			put("Bivange", "Bivange");
			put("Boursch", "Bourscheid");
			put("Bt", "Bettembourg");
			put("Consd", "Consdorf");
			put("Cv", "Clervaux");
			put("Dk", "Diekirch");
			put("Diff", "Differdange");
			put("Divers", "Divers");
			put("Drauff", "Drauffelt");
			put("Dud", "Dudelange");
			put("Eb", "Ettelbruck");
			put("Echter", "Echternach");
			put("EschAlz", "Esch-Alzette");
			put("EschSure", "Esch-Sure");
			put("Es", "Esch-Sure");
			put("Goeb", "Goebelsmühle");
			put("Grundh", "Grundhof");
			put("Gy", "Gouvy");
			put("Helz", "Helzingen");
			put("Hesp", "Hesperange");
			put("Jungl", "Junglinster");
			put("Kleinb", "Kleinbettingen");
			put("Kt", "Kautenbach");
			put("Lux", "Luxembourg");
			put("Martel", "Martelange");
			put("Merk", "Merckholtz");
			put("Mersch", "Mersch");
			put("Mond", "Mondorf");
			put("Reichl", "Reichlange");
			put("Remich", "Remich");
			put("Rumel", "Rumelange");
			put("Trains", "Trains");
			put("Tv", "TV_Troisvierges");
			put("Vian", "Vianden");
			put("Viels", "Vielsalm");
			put("Wb", "Wasserbillig");
			put("Weilerb", "Weilerbach");
			put("Weisw", "Weiswampach");
			put("Wemp", "Wemperhardt");
			put("Worm", "Wormeldange");
			put("Ww", "Wilwerwiltz");
			put("Wz", "Wiltz");
		}
	};

	public static List<URI> getImageURIs(String village, String type, String number) {
		List<URI> uris = new LinkedList<URI>();

		File[] files = new File("/home/luxem/Desktop/postkarten/" + village).listFiles();

		System.out.println(" type " + type);
		System.out.println(" number " + number);

		for (File file : files) {
			if (!file.getName().endsWith(".psd") && !file.getName().endsWith(".PSD")) {

				String filePieces[] = file.getName().substring(0, file.getName().indexOf(".")) .split("_");
				//System.out.println(Arrays.toString(filePieces));

				boolean containsNumber = false;
				boolean containsType = false;
				boolean recto = false;

				for (String filePiece : filePieces) {
					if (filePiece.contains(number)) {
						if (!(filePiece.contains("dpi") || filePiece.contains("DPI"))) {
							containsNumber = true;
						}
					}

					if (filePiece.contains(type)) {
						containsType = true;
					}

					if (filePiece.contains("recto") || filePiece.contains("Recto")) {
						recto = true;
					}
				}

				if (containsNumber && containsType) {
					if (recto) {
						uris.add(0, ImageManipulator.adaptAndStoreImageForCollector(file.toURI(), "Postkarten"));
					} else {
						uris.add(ImageManipulator.adaptAndStoreImageForCollector(file.toURI(), "Postkarten"));
					}
				}
			}
		}

		return uris;
	}
}
