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

import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.Language;
import org.sammelbox.controller.managers.SavedSearchManager.SavedSearch;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.album.Album;
import org.sammelbox.model.database.QueryComponent;
import org.sammelbox.model.database.QueryOperator;
import org.sammelbox.model.settings.ApplicationSettings;
import org.sammelbox.view.SammelView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

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
		xmlOutput.append("\t<defaultView>" + applicationSettings.getDefaultView() + "</defaultView>\n");
		xmlOutput.append("\t<showDebugMenu>" + applicationSettings.showDebugMenu() + "</showDebugMenu>\n");
		xmlOutput.append("\t<isFullSynchronizationEnabled>" + applicationSettings.isFullSynchronizationEnabled() + "</isFullSynchronizationEnabled>\n");
		xmlOutput.append("</settings>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemLocations.getSettingsXML());
	}
	
	public static void storeAlbums(Collection<Album> albums) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<albums>\n");
		
		for (Album album : albums) {
			xmlOutput.append("\t<album>\n");
			xmlOutput.append("\t\t<name><![CDATA[" + album.getAlbumName() + "]]></name>\n");
			xmlOutput.append("\t\t<sortByField><![CDATA[" + album.getSortByField() + "]]></sortByField>\n");
			xmlOutput.append("\t</album>\n");
		}
		
		xmlOutput.append("</albums>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemLocations.getAlbumsXML());
	}
	
	public static void storeSavedSearches(Map<String, List<SavedSearch>> albumNamesToSavedSearches) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<savedSearches>\n");
		
		for (Map.Entry<String, List<SavedSearch>> mapEntry : albumNamesToSavedSearches.entrySet()) {
			for (SavedSearch savedSearch : mapEntry.getValue()) {
				xmlOutput.append("\t<savedSearch>\n");
				xmlOutput.append("\t\t<name><![CDATA[" + savedSearch.getName() + "]]></name>\n");
				xmlOutput.append("\t\t<album><![CDATA[" + savedSearch.getAlbum() + "]]></album>\n");
				xmlOutput.append("\t\t<orderByField><![CDATA[" + savedSearch.getOrderByField() + "]]></orderByField>\n");
				xmlOutput.append("\t\t<isOrderAscending><![CDATA[" + savedSearch.isOrderAscending() + "]]></isOrderAscending>\n");
				
				for (QueryComponent queryComponent : savedSearch.getQueryComponents()) {
					xmlOutput.append("\t\t<queryComponent>\n");
					xmlOutput.append("\t\t\t<fieldName><![CDATA[" + queryComponent.getFieldName() + "]]></fieldName>\n");
					xmlOutput.append("\t\t\t<operator><![CDATA[" + queryComponent.getOperator() + "]]></operator>\n");
					xmlOutput.append("\t\t\t<value><![CDATA[" + queryComponent.getValue() + "]]></value>\n");
					xmlOutput.append("\t\t</queryComponent>\n");
				}
				
				xmlOutput.append("\t\t<isConnectedByAnd><![CDATA[" + savedSearch.isConnectedByAnd() + "]]></isConnectedByAnd>\n");
				xmlOutput.append("\t</savedSearch>\n");
			}
		}
		
		xmlOutput.append("</savedSearches>\n");
		
		FileSystemAccessWrapper.writeToFile(xmlOutput.toString(), FileSystemLocations.getSavedSearchesXML());
	}
	
	public static void storeWelcomePageManagerInformation(Map<String, Long> albumToLastModified) {
		
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<welcomePageInformation>\n");

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
					applicationSettings.setDefaultView(SammelView.valueOf(getValue("defaultView", element)));
					applicationSettings.setShowDebugMenu(Boolean.valueOf(getValue("showDebugMenu", element)));
					applicationSettings.setFullSynchronizationEnabled(Boolean.valueOf(getValue("isFullSynchronizationEnabled", element)));
				} catch (RuntimeException exception) {
					LOGGER.error("Could not properly load settings file. File will be recreated");
					SettingsManager.storeToSettingsFile();
					return retrieveSettings();
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occurred while parsing the settings XML file", ex);
		}
		
		return applicationSettings;
	}

	public static List<Album> retrieveAlbums() {
		String albumsAsXml = FileSystemAccessWrapper.readFileAsString(FileSystemLocations.getAlbumsXML());
		
		List<Album> albumToPosition = new LinkedList<Album>();
		
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
				NodeList albumNodes = document.getElementsByTagName("album");
				
				for (int i = 0; i < albumNodes.getLength(); i++) {
					Node node = albumNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						Album album = new Album();
						album.setAlbumName(getValue("name", element));
						album.setSortByField(getValue("sortByField", element));
						
						albumToPosition.add(album);
					}
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occurred while parsing the albums XML file");
		}
		
		return albumToPosition;
	}
	
	public static Map<String, List<SavedSearch>> retrieveSavedSearches() {
		String savedSearchesXml = FileSystemAccessWrapper.readFileAsString(FileSystemLocations.getSavedSearchesXML());
		
		Map<String, List<SavedSearch>> albumNamesToSavedSearches = new HashMap<String, List<SavedSearch>>();
		
		if (savedSearchesXml.isEmpty()) {
			return albumNamesToSavedSearches;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(savedSearchesXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("savedSearches")) {
				throw new XmlParsingException("Invalid Saved Searches File");
			} else {
				NodeList savedSearchesNodes = document.getElementsByTagName("savedSearch");
				
				String name = "";
				String album = "";
				String orderByField = "";
				String orderAscending = "";
				
				for (int i = 0; i < savedSearchesNodes.getLength(); i++) {
					Node node = savedSearchesNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						album = getValue("album", element);
						orderByField = getValue("orderByField", element);
						orderAscending = getValue("isOrderAscending", element);
						List<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
						String connectedByAnd = getValue("isConnectedByAnd", element);
						
						NodeList queryComponentNodes = element.getElementsByTagName("queryComponent");
						for (int j=0; j<queryComponentNodes.getLength(); j++) {
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								Element queryComponentElement = (Element) queryComponentNodes.item(j);
								
								String fieldName = getValue("fieldName", queryComponentElement);
								String operator = getValue("operator", queryComponentElement);
								String value = getValue("value", queryComponentElement);
								
								queryComponents.add(new QueryComponent(fieldName, QueryOperator.valueOf(operator), value));
							}
						}
						
						if (albumNamesToSavedSearches.get(album) == null) {
							List<SavedSearch> savedSearches = new LinkedList<>();
							savedSearches.add(new SavedSearch(name, album, orderByField, Boolean.valueOf(orderAscending), queryComponents, Boolean.valueOf(connectedByAnd)));
							albumNamesToSavedSearches.put(album, savedSearches);
						} else {
							List<SavedSearch> savedSearches = albumNamesToSavedSearches.get(album);
							savedSearches.add(new SavedSearch(name, album, orderByField, Boolean.valueOf(orderAscending), queryComponents, Boolean.valueOf(connectedByAnd)));
							albumNamesToSavedSearches.put(album, savedSearches);
						}
					}
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occurred while parsing the saved search XML file");
		}
		
		return albumNamesToSavedSearches;
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
				NodeList albumToLastModifiedNodes = document.getElementsByTagName("albumToLastModified");
				
				String name = "";
				Long lastModified = 0L;

				for (int i = 0; i < albumToLastModifiedNodes.getLength(); i++) {
					Node node = albumToLastModifiedNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("albumName", element);
						lastModified = Long.parseLong(getValue("lastModified", element));
						
						albumToLastModified.put(name, lastModified);
					}
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occurred while parsing the welcome page XML file");
		}
		
		return albumToLastModified;
	}

	public static Map<String, Integer> retrieveAlbumAndSavedSearchesToClicks() {
		String welcomePageInformationXml = FileSystemAccessWrapper.readFileAsString(FileSystemLocations.getWelcomeXML());
		
		Map<String, Integer> albumAndSavedSearchesToClicks = new HashMap<String, Integer>();
		
		if (welcomePageInformationXml.isEmpty()) {
			return albumAndSavedSearchesToClicks;
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
				NodeList albumAndSavedSearchesToClicksNodes = document.getElementsByTagName("albumAndViewsToClicks");
				
				String name = "";
				Integer clicks = 0;

				for (int i = 0; i < albumAndSavedSearchesToClicksNodes.getLength(); i++) {
					Node node = albumAndSavedSearchesToClicksNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						clicks = Integer.parseInt(getValue("clicks", element));
						
						albumAndSavedSearchesToClicks.put(name, clicks);
					}
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException | XmlParsingException ex) {
			LOGGER.error("An error occurred while parsing the welcome page XML file");
		}
		
		return albumAndSavedSearchesToClicks;
	}
}
