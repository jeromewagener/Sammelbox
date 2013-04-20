package collector.desktop.networking;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import collector.desktop.database.ItemField;
import collector.desktop.database.MetaItemField;

public class XmlMessageBuilders {
	/** Creates a XML message that is used for advertising the host
	 * @param hostNameString the name of the host (E.g. MyLinuxMachine) 
	 * @return the XML document as string */
	public static String getBeaconAsXml(String hostNameString) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println(XmlMessageBuilders.class + ": " + e.getClass() + " - " + e.getMessage());
		}

		Document document = docBuilder.newDocument();
		Element root = document.createElement("collector");
		document.appendChild(root);

		Element hostName = document.createElement("hostname");
		root.appendChild(hostName);

		Text hostNameText = document.createTextNode(hostNameString);
		hostName.appendChild(hostNameText);
		document.normalize();

		return transformDocumentToString(document);
	}
	
	/** Creates an XML message containing all albums together with their schema version UUID
	 * @param albumNameToVersionMap A map containing all the albums of the host together with the according schema version UUID
	 * @return the XML document as string. */
	public static String getAlbumListResponseAsXml(Map<String, UUID> albumNameToVersionMap) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println(XmlMessageBuilders.class + ": " + e.getClass() + " - " + e.getMessage());
		}

		Document document = docBuilder.newDocument();
		Element rootElement = document.createElement("collector");
		document.appendChild(rootElement);

		// Response
		Element responseElement = document.createElement("response");
		Attr responseTypeAttribute =  document.createAttribute("type");
		responseTypeAttribute.setNodeValue("getAlbumList");
		responseElement.setAttributeNode(responseTypeAttribute);
		rootElement.appendChild(responseElement);
		
		Element albumsElement = document.createElement("albums");
		responseElement.appendChild(albumsElement);

		for (Entry<String, UUID> albumInfo : albumNameToVersionMap.entrySet()) {
			Element albumElement = document.createElement("album");

			Element albumName = document.createElement("album-name");
			Text albumNameString = document.createTextNode(albumInfo.getKey());
			albumName.appendChild(albumNameString);
			albumElement.appendChild(albumName);

			Element albumVersion = document.createElement("schema-version-uuid");
			Text albumVersionString = document.createTextNode(albumInfo.getValue().toString());
			albumVersion.appendChild(albumVersionString);
			albumElement.appendChild(albumVersion);
			
			albumsElement.appendChild(albumElement);
		}
		document.normalize();

		return transformDocumentToString(document);
	}

	/** Creates a XML message containing the name of an album, together with the corresponding schema version UUID and all metaItemFields
	 * of the provided album (column names and types) 
	 * @param albumName the name of the album 
	 * @param schemaVersion the schema version UUID of the album 
	 * @param fieldInfos a list of all the metaItemFields for the provided album
	 * @return the XML document as string. */
	public static String getAlbumSchemaResponseAsXml(String albumName, UUID schemaVersion, List<MetaItemField> fieldInfos) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println(XmlMessageBuilders.class + ": " + e.getClass() + " - " + e.getMessage());
		}

		Document document = docBuilder.newDocument();
		Element rootElement = document.createElement("collector");
		document.appendChild(rootElement);

		// Response
		Element responseElement = document.createElement("response");
		Attr responseTypeAttribute =  document.createAttribute("type");
		responseTypeAttribute.setNodeValue("getAlbumSchema");
		responseElement.setAttributeNode(responseTypeAttribute);
		rootElement.appendChild(responseElement);

		// Album name
		Element albumNameElement = document.createElement("album-name");
		Text albumNameText = document.createTextNode(albumName);
		albumNameElement.appendChild(albumNameText);
		responseElement.appendChild(albumNameElement);

		// Album schema version
		Element albumVersionElement = document.createElement("schema-version-uuid");
		Text albumVersionText = document.createTextNode(schemaVersion.toString());
		albumVersionElement.appendChild(albumVersionText);
		responseElement.appendChild(albumVersionElement);

		// Schema
		Element schemaElement = document.createElement("schema");
		responseElement.appendChild(schemaElement);

		// For each column
		for (MetaItemField metaItemField : fieldInfos) {
			Element columnElement = document.createElement("field");

			Element columnNameElement = document.createElement("field-name");
			Text columnNameText = document.createTextNode(metaItemField.getName());
			columnNameElement.appendChild(columnNameText);
			columnElement.appendChild(columnNameElement);

			Element columnTypeElement = document.createElement("field-type");
			Text columnTypeNameText = document.createTextNode(metaItemField.getType().toString());
			columnTypeElement.appendChild(columnTypeNameText);
			columnElement.appendChild(columnTypeElement);

			schemaElement.appendChild(columnElement);
		}
		document.normalize();

		return transformDocumentToString(document);
	}

	/** Creates a XML message containing the name of an album, as well as a listing of all item IDs together with their according 
	 * content version UUID
	 * @param albumName the name of the album 
	 * @param albumIdToContentVersionUUIDMap a mapping of all the album IDs to their according content version UUIDs
	 * @return the XML document as string. */
	public static String getItemListResponseAsXml(String albumName, Map<Integer, UUID> albumIdToContentVersionUUIDMap) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println(XmlMessageBuilders.class + ": " + e.getClass() + " - " + e.getMessage());
		}

		Document document = docBuilder.newDocument();
		Element rootElement = document.createElement("collector");
		document.appendChild(rootElement);

		// Response
		Element responseElement = document.createElement("response");
		Attr responseTypeAttribute =  document.createAttribute("type");
		responseTypeAttribute.setNodeValue("getItemList");
		responseElement.setAttributeNode(responseTypeAttribute);
		rootElement.appendChild(responseElement);

		// Album name
		Element albumNameElement = document.createElement("album-name");
		Text albumNameText = document.createTextNode(albumName);
		albumNameElement.appendChild(albumNameText);
		responseElement.appendChild(albumNameElement);

		// Items
		Element itemsElement = document.createElement("items");
		responseElement.appendChild(itemsElement);
		// For each column
		for (Entry<Integer, UUID> item : albumIdToContentVersionUUIDMap.entrySet()) {

			Element itemElement = document.createElement("item");

			Element itemIdElement = document.createElement("item-id");
			Text itemIdText = document.createTextNode(item.getKey().toString());
			itemIdElement.appendChild(itemIdText);
			itemElement.appendChild(itemIdElement);

			Element itemVersionElement = document.createElement("content-version-uuid");
			Text itemVersionText = document.createTextNode(item.getValue().toString());
			itemVersionElement.appendChild(itemVersionText);
			itemElement.appendChild(itemVersionElement);

			itemsElement.appendChild(itemElement);
		}
		document.normalize();

		return transformDocumentToString(document);
	}

	/** Creates an XML message containing the name of an album, and all the information of a specific item (id / contentVersion / fields) 
	 * @param albumName the name of the album 
	 * @param id the id of the item to be included within the message
	 * @param contentVersion the UUID specifying the version of the content
	 * @return the XML document as string. */
	public static String getItemContentResponseAsXml(String albumName, int id, UUID contentVersion, List<ItemField> fields) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println(XmlMessageBuilders.class + ": " + e.getClass() + " - " + e.getMessage());
		}

		Document document = docBuilder.newDocument();
		Element rootElement = document.createElement("collector");
		document.appendChild(rootElement);

		// Response
		Element responseElement = document.createElement("response");
		Attr responseTypeAttribute =  document.createAttribute("type");
		responseTypeAttribute.setNodeValue("getItemContent");
		responseElement.setAttributeNode(responseTypeAttribute);
		rootElement.appendChild(responseElement);

		// Album name
		Element albumNameElement = document.createElement("album-name");
		Text albumNameText = document.createTextNode(albumName);
		albumNameElement.appendChild(albumNameText);
		responseElement.appendChild(albumNameElement);

		// Album item id
		Element albumItemIdElement = document.createElement("id");
		Text albumItemIdText = document.createTextNode(Integer.toString(id));
		albumItemIdElement.appendChild(albumItemIdText);
		responseElement.appendChild(albumItemIdElement);
		
		// Album content version
		Element albumVersionElement = document.createElement("content-version-uuid");
		Text albumVersionText = document.createTextNode(albumName);
		albumVersionElement.appendChild(albumVersionText);
		responseElement.appendChild(albumVersionElement);

		// Album content version
		Element contentElement = document.createElement("content");
		responseElement.appendChild(contentElement);

		// For each column
		for (ItemField item : fields) {

			Element fieldElement = document.createElement("field");

			Element itemIdElement = document.createElement("field-id");
			Text itemIdText = document.createTextNode(item.getName());
			itemIdElement.appendChild(itemIdText);
			fieldElement.appendChild(itemIdElement);

			Element itemTypeElement = document.createElement("field-type");
			Text itemTypeText = document.createTextNode(item.getType().toString());
			itemTypeElement.appendChild(itemTypeText);
			fieldElement.appendChild(itemTypeElement);
			
			Element itemValueElement = document.createElement("field-Value");
			Text itemValueText = document.createTextNode(item.getValue().toString());//FIXME: transform internal object Value with type into proper string representation
			itemValueElement.appendChild(itemValueText);
			fieldElement.appendChild(itemValueElement);

			contentElement.appendChild(fieldElement);
		}
		document.normalize();

		return transformDocumentToString(document);
	}


	/** This method transforms a given XML document into a human readable string.
	 * @param document an XML document object 
	 * @return the XML document object transformed into a human readable string */
	private static String transformDocumentToString(Document document) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		} catch (TransformerConfigurationException e) {
			System.err.println(XmlMessageBuilders.class + ": " + e.getClass() + " - " + e.getMessage());
			return null;
		}

		Source source = new DOMSource(document);
		StringWriter stringWriter = new StringWriter();
		Result dest = new StreamResult(stringWriter);
		try {
			transformer.transform(source, dest);
		} catch (TransformerException e) {
			System.err.println(XmlMessageBuilders.class + ": " + e.getClass() + " - " + e.getMessage());
			return null;
		}
		String xmlString = stringWriter.toString();

		return xmlString;
	}
}
