package org.sammelbox.controller.filesystem;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sammelbox.controller.i18n.Language;
import org.sammelbox.controller.managers.AlbumViewManager.AlbumView;
import org.sammelbox.controller.settings.ApplicationSettingsManager.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLStorageWrapper {
	private final static Logger LOGGER = LoggerFactory.getLogger(XMLStorageWrapper.class);
	
	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}
	
	public static void storeSettings(ApplicationSettings applicationSettings) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<settings>\n");
		xmlOutput.append("\t<userDefinedLanguage>" + applicationSettings.getUserDefinedLanguage().toString() + "</userDefinedLanguage>\n");
		xmlOutput.append("</settings>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemConstants.SETTINGS_FILE);
	}
	
	public static void storeAlbums(Collection<String> albums) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<albums>\n");
		
		for (String album : albums) {
			xmlOutput.append("\t<album>\n");
			xmlOutput.append("\t\t<name><![CDATA[" + album + "]]></name>\n");
			xmlOutput.append("\t</album>\n");
		}
		
		xmlOutput.append("</albums>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemConstants.ALBUM_FILE);
	}
	
	public static void storeViews(Map<String, List<AlbumView>> albumNamesToAlbumViews) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<views>\n");
		
		for (String albumName : albumNamesToAlbumViews.keySet()) {
			for (AlbumView albumView : albumNamesToAlbumViews.get(albumName)) {
				xmlOutput.append("\t<view>\n");
				xmlOutput.append("\t\t<name><![CDATA[" + albumView.getName() + "]]></name>\n");
				xmlOutput.append("\t\t<album><![CDATA[" + albumView.getAlbum() + "]]></album>\n");
				xmlOutput.append("\t\t<sqlQuery><![CDATA[" + albumView.getSqlQuery() + "]]></sqlQuery>\n");
				xmlOutput.append("\t</view>\n");
			}
		}
		
		xmlOutput.append("</views>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemConstants.VIEW_FILE);
	}
	
	public static void storeWelcomePageManagerInformation(
			Map<String, Integer> albumAndViewsToClicks,
			Map<String, Long> albumToLastModified) {
		
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<welcomePageInformation>\n");
		
		for (String albumOrViewName : albumAndViewsToClicks.keySet()) {
			xmlOutput.append("\t<albumAndViewsToClicks>\n");
			xmlOutput.append("\t\t<name><![CDATA[" + albumOrViewName + "]]></name>\n");
			xmlOutput.append("\t\t<clicks><![CDATA[" + albumAndViewsToClicks.get(albumOrViewName) + "]]></clicks>\n");
			xmlOutput.append("\t</albumAndViewsToClicks>\n");
		}
		
		for (String albumName : albumToLastModified.keySet()) {
			xmlOutput.append("\t<albumToLastModified>\n");
			xmlOutput.append("\t\t<albumName><![CDATA[" + albumName + "]]></albumName>\n");
			xmlOutput.append("\t\t<lastModified><![CDATA[" + albumToLastModified.get(albumName) + "]]></lastModified>\n");
			xmlOutput.append("\t</albumToLastModified>\n");
		}
		
		xmlOutput.append("</welcomePageInformation>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemConstants.WELCOME_PAGE_FILE);
	}
	
	public static ApplicationSettings retrieveSettings() {
		String applicationSettingsAsXml = FileSystemAccessWrapper.readFileAsString(
				FileSystemConstants.SETTINGS_FILE);
		
		ApplicationSettings applicationSettings = new ApplicationSettings();
		
		if (applicationSettingsAsXml.isEmpty()) {
			return applicationSettings;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(applicationSettingsAsXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("settings")) {
				throw new Exception("Invalid Settings File");
			} else {
				NodeList settingNodes = root.getChildNodes();
								
				for (int i = 0; i < settingNodes.getLength(); i++) {
					Node node = settingNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						if (element.getNodeName().equals("userDefinedLanguage")) {
							applicationSettings.setUserDefinedLanguage(Language.valueOf(element.getFirstChild().getNodeValue()));
						}
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.error("An error occured while parsing the settings XML file");
		}
		
		return applicationSettings;
	}

	public static List<String> retrieveAlbums() {
		String albumsAsXml = FileSystemAccessWrapper.readFileAsString(FileSystemConstants.ALBUM_FILE);
		
		List<String> albumToPosition = new LinkedList<String>();
		
		if (albumsAsXml.isEmpty()) {
			return albumToPosition;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(albumsAsXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("albums")) {
				throw new Exception("Invalid Album File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("album");
				
				String name = "";
				
				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						
						albumToPosition.add(name);
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.error("An error occured while parsing the albums XML file");
		}
		
		return albumToPosition;
	}
	
	public static Map<String, List<AlbumView>> retrieveViews() {
		String albumViewsAsXml = FileSystemAccessWrapper.readFileAsString(FileSystemConstants.VIEW_FILE);
		
		Map<String, List<AlbumView>> albumNamesToAlbumViews = new HashMap<String, List<AlbumView>>();
		
		if (albumViewsAsXml.isEmpty()) {
			return albumNamesToAlbumViews;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(albumViewsAsXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("views")) {
				throw new Exception("Invalid Album View File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("view");
				
				String name = "";
				String album = "";
				String sqlQuery = "";
				
				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						album = getValue("album", element);
						sqlQuery = getValue("sqlQuery", element);
						
						if (albumNamesToAlbumViews.get(album) == null) {
							List<AlbumView> albumViews = new LinkedList<>();
							albumViews.add(new AlbumView(name, album, sqlQuery));
							albumNamesToAlbumViews.put(album, albumViews);
						} else {
							List<AlbumView> albumViews = albumNamesToAlbumViews.get(album);
							albumViews.add(new AlbumView(name, album, sqlQuery));
							albumNamesToAlbumViews.put(album, albumViews);
						}
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.error("An error occured while parsing the album views XML file");
		}
		
		return albumNamesToAlbumViews;
	}

	public static Map<String, Long> retrieveAlbumToLastModified() {
		String welcomePageInformationXml = FileSystemAccessWrapper.readFileAsString(FileSystemConstants.WELCOME_PAGE_FILE);
		
		Map<String, Long> albumToLastModified = new HashMap<String, Long>();
		
		if (welcomePageInformationXml.isEmpty()) {
			return albumToLastModified;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(welcomePageInformationXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("welcomePageInformation")) {
				throw new Exception("Invalid Welcome Page File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("albumToLastModified");
				
				String name = "";
				Long lastModified = 0L;

				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("albumName", element);
						lastModified = Long.parseLong(getValue("lastModified", element));
						
						albumToLastModified.put(name, lastModified);
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.error("An error occured while parsing the welcome page XML file");
		}
		
		return albumToLastModified;
	}

	public static Map<String, Integer> retrieveAlbumAndViewsToClicks() {
		String welcomePageInformationXml = FileSystemAccessWrapper.readFileAsString(FileSystemConstants.WELCOME_PAGE_FILE);
		
		Map<String, Integer> albumAndViewsToClicks = new HashMap<String, Integer>();
		
		if (welcomePageInformationXml.isEmpty()) {
			return albumAndViewsToClicks;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(welcomePageInformationXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("welcomePageInformation")) {
				throw new Exception("Invalid Welcome Page File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("albumAndViewsToClicks");
				
				String name = "";
				Integer clicks = 0;

				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						clicks = Integer.parseInt(getValue("clicks", element));
						
						albumAndViewsToClicks.put(name, clicks);
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.error("An error occured while parsing the welcome page XML file");
		}
		
		return albumAndViewsToClicks;
	}
}
