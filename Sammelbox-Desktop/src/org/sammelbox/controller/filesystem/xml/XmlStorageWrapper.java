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

package org.sammelbox.controller.filesystem.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.Language;
import org.sammelbox.controller.managers.AlbumViewManager.AlbumView;
import org.sammelbox.controller.settings.SettingsManager;
import org.sammelbox.model.settings.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XmlStorageWrapper {
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlStorageWrapper.class);
	
	private XmlStorageWrapper() {
		// not needed
	}
	
	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}
	
	public static void storeSettings(ApplicationSettings applicationSettings) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<settings>\n");
		xmlOutput.append("\t<userDefinedLanguage>" + applicationSettings.getUserDefinedLanguage().toString() + "</userDefinedLanguage>\n");
		xmlOutput.append("\t<dateFormat>" + applicationSettings.getDateFormat() + "</dateFormat>\n");
		xmlOutput.append("\t<detailedViewIsDefault>" + applicationSettings.isDetailedViewDefault() + "</detailedViewIsDefault>\n");
		xmlOutput.append("\t<showDebugMenu>" + applicationSettings.showDebugMenu() + "</showDebugMenu>\n");
		xmlOutput.append("</settings>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemLocations.getSettingsXML());
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
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemLocations.getAlbumsXML());
	}
	
	public static void storeViews(Map<String, List<AlbumView>> albumNamesToAlbumViews) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<views>\n");
		
		for (Map.Entry<String, List<AlbumView>> mapEntry : albumNamesToAlbumViews.entrySet()) {
			for (AlbumView albumView : mapEntry.getValue()) {
				xmlOutput.append("\t<view>\n");
				xmlOutput.append("\t\t<name><![CDATA[" + albumView.getName() + "]]></name>\n");
				xmlOutput.append("\t\t<album><![CDATA[" + albumView.getAlbum() + "]]></album>\n");
				xmlOutput.append("\t\t<sqlQuery><![CDATA[" + albumView.getSqlQuery() + "]]></sqlQuery>\n");
				xmlOutput.append("\t</view>\n");
			}
		}
		
		xmlOutput.append("</views>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemLocations.getViewsXML());
	}
	
	public static void storeWelcomePageManagerInformation(
			Map<String, Integer> albumAndViewsToClicks,
			Map<String, Long> albumToLastModified) {
		
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<welcomePageInformation>\n");
		
		for (Map.Entry<String, Integer> mapEntry : albumAndViewsToClicks.entrySet()) {
			xmlOutput.append("\t<albumAndViewsToClicks>\n");
			xmlOutput.append("\t\t<name><![CDATA[" + mapEntry.getKey() + "]]></name>\n");
			xmlOutput.append("\t\t<clicks><![CDATA[" + mapEntry.getValue() + "]]></clicks>\n");
			xmlOutput.append("\t</albumAndViewsToClicks>\n");
		}
		
		for (Map.Entry<String, Long> mapEntry : albumToLastModified.entrySet()) {
			xmlOutput.append("\t<albumToLastModified>\n");
			xmlOutput.append("\t\t<albumName><![CDATA[" + mapEntry.getKey() + "]]></albumName>\n");
			xmlOutput.append("\t\t<lastModified><![CDATA[" + mapEntry.getValue() + "]]></lastModified>\n");
			xmlOutput.append("\t</albumToLastModified>\n");
		}
		
		xmlOutput.append("</welcomePageInformation>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemLocations.getWelcomeXML());
	}
	
	public static ApplicationSettings retrieveSettings() {
		String applicationSettingsAsXml = FileSystemAccessWrapper.readFileAsString(
				FileSystemLocations.getSettingsXML());
		
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
				throw new XmlParsingException("Invalid Settings File");
			} else {
				Element element = (Element) root;
				
				try {
					applicationSettings.setUserDefinedLanguage(Language.valueOf(getValue("userDefinedLanguage", element)));
					applicationSettings.setDateFormat(getValue("dateFormat", element));
					applicationSettings.setDetailedViewIsDefault(Boolean.valueOf(getValue("detailedViewIsDefault", element)));
					applicationSettings.setShowDebugMenu(Boolean.valueOf(getValue("showDebugMenu", element)));
				} catch (RuntimeException exception) {
					LOGGER.error("Could not properly load settings file. File will be recreated");
					SettingsManager.storeToSettingsFile();
					return retrieveSettings();
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occured while parsing the settings XML file", ex);
		}
		
		return applicationSettings;
	}

	public static List<String> retrieveAlbums() {
		String albumsAsXml = FileSystemAccessWrapper.readFileAsString(FileSystemLocations.getAlbumsXML());
		
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
				throw new XmlParsingException("Invalid Album File");
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
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occured while parsing the albums XML file");
		}
		
		return albumToPosition;
	}
	
	public static Map<String, List<AlbumView>> retrieveViews() {
		String albumViewsAsXml = FileSystemAccessWrapper.readFileAsString(FileSystemLocations.getViewsXML());
		
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
				throw new XmlParsingException("Invalid Album View File");
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
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occured while parsing the album views XML file");
		}
		
		return albumNamesToAlbumViews;
	}

	public static Map<String, Long> retrieveAlbumToLastModified() {
		String welcomePageInformationXml = FileSystemAccessWrapper.readFileAsString(FileSystemLocations.getWelcomeXML());
		
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
				throw new XmlParsingException("Invalid Welcome Page File");
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
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occured while parsing the welcome page XML file");
		}
		
		return albumToLastModified;
	}

	public static Map<String, Integer> retrieveAlbumAndViewsToClicks() {
		String welcomePageInformationXml = FileSystemAccessWrapper.readFileAsString(FileSystemLocations.getWelcomeXML());
		
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
				throw new XmlParsingException("Invalid Welcome Page File");
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
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occured while parsing the welcome page XML file");
		}
		
		return albumAndViewsToClicks;
	}
}
