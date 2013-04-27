package collector.desktop.filesystem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;
import java.sql.Connection;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import collector.desktop.database.DatabaseWrapper;
import collector.desktop.gui.AlbumViewManager.AlbumView;

public class FileSystemAccessWrapper {
	public static final String COLLECTOR_HOME 					= System.getProperty("user.home") + File.separatorChar + ".collector";
	public static final String COLLECTOR_HOME_APPDATA 			= COLLECTOR_HOME + File.separatorChar + "app-data";
	public static final String ALBUM_PICTURES					= "album-pictures";
	public static final String COLLECTOR_HOME_ALBUM_PICTURES 	= COLLECTOR_HOME + File.separatorChar + ALBUM_PICTURES;
	public static final String PLACEHOLDERIMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder.png";
	public static final String DATABASE_NAME					= "collector.db";
	public static final String DATABASE_TO_RESTORE_NAME			= "collector.restore.db";
	public static final String DATABASE 						= COLLECTOR_HOME + File.separatorChar + DATABASE_NAME;
	public static final String DATABASE_TO_RESTORE				= COLLECTOR_HOME + File.separatorChar + DATABASE_TO_RESTORE_NAME;
	public static final String VIEW_FILE						= COLLECTOR_HOME_APPDATA + File.separatorChar + "views.xml";
	
	private static final boolean OVERWRITE_EXISITING_FILES = true;
	
	/**
	 * Instance to itself used in the process to located stored resources. 
	 */
	private static FileSystemAccessWrapper instance = new FileSystemAccessWrapper();

	/** This method is used to create and update the file-structure for the collector application.
	 *  During this process no existing directory or file is overwritten.
	 *  This method must be called during initialization of the program.*/
	public static boolean updateCollectorFileStructure() {
		File collectorDirectory = new File(COLLECTOR_HOME);

		if (!collectorDirectory.exists()) {
			if (!collectorDirectory.mkdir()) {
				System.err.println("Cannot create "+ COLLECTOR_HOME+ " directory although it seems that it does not exist");
				return false;
			}
		}

		// Create Album home directory containing all albums		
		File albumHomeDirectory = new File(COLLECTOR_HOME_ALBUM_PICTURES);

		if (!albumHomeDirectory.exists()) {
			if (!albumHomeDirectory.mkdir()) {
				System.err.println("Cannot create "+COLLECTOR_HOME_ALBUM_PICTURES+" although it seems that it does not exist");
				return false;
			}
		}

		// Create the application data directory
		File appData = new File(COLLECTOR_HOME_APPDATA);
		if (!appData.exists()) {
			if (!appData.mkdir()) {
				System.err.println("Cannot create collector app-data directory although it seems that it does not exist");
				return false;
			}
		}

		// Add presentation files to application data directory
		File placeholderPNG = new File(COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder.png");
		if (!placeholderPNG.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/placeholder.png", placeholderPNG);
		}

		File effectsJS = new File(COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js");
		if (!effectsJS.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("javascript/effects.js", effectsJS);
		}

		File styleCSS = new File(COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css");
		if (!styleCSS.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("stylesheets/style.css", styleCSS);
		}

		File loadingGIF = new File(COLLECTOR_HOME_APPDATA + File.separatorChar + "loading.gif");
		if (!loadingGIF.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/loading.gif", loadingGIF);
		}

		File loadingHTML = new File(COLLECTOR_HOME_APPDATA + File.separatorChar + "loading.html");
		if (!loadingHTML.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("htmlfiles/loading.html", loadingHTML);
		}

		return true;
	}

	// TODO comment
	public static void copyResourceToFile(String resource, File outputFile) {
		try {
			InputStream istream = instance.getClass().getClassLoader().getResourceAsStream(resource);
			OutputStream ostream = new FileOutputStream(outputFile);

			byte buffer[] = new byte[1024];
			int length;
			
			while((length=istream.read(buffer)) > 0) {
				ostream.write(buffer,0,length);
			}
			
			ostream.close();
			istream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** This method is used to create and update the file-structure for every album.
	 *  During this process no existing directory or file is overwritten.
	 *  This method must be called after a new album is created or altered.
	 *  It should be also called during initialization of the program after the collector file structure has been updated. */
	public static boolean updateAlbumFileStructure(Connection databaseConnection) {
		// Create inside the album home directory all album directories
		for (String albumName : DatabaseWrapper.listAllAlbums()) {
			File albumDirectory = new File(getFilePathForAlbum(albumName));

			if (!albumDirectory.exists()) {
				if (!albumDirectory.mkdir()) {
					System.err.println("Cannot create collector album directory although it seems that it does not exist");
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Gets the path to the respective picture folders.
	 * @param albumName The name of the album whose picture folder is requested.
	 * @return The path to the picture directory.
	 */
	public static String getFilePathForAlbum(String albumName) {
		return COLLECTOR_HOME_ALBUM_PICTURES + File.separatorChar + albumName;
	}

	/**
	 * Recursively copies a directory and its content from the source to the target location.
	 * @param sourceLocation The file handle to the source directory.
	 * @param targetLocation The file handle to the target directory.
	 * @throws IOException Exception raised if a problem is encountered during the copy process.
	 */
	public static void copyDirectory(File sourceLocation , File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	/**
	 * Provides a valid URI to the location on the file system. Invalid paths may result in raised exceptions
	 * @param fileNameWithExtension The name including file type extension of the picture.
	 * @param albumName The name of the album to which the picture
	 * @return The URI to the physical location of the picture.
	 */
	public static URI getURIToImageFile(String fileNameWithExtension, String albumName) {
		//System.out.println(new File(COLLECTOR_HOME + File.separatorChar + albumName + File.separatorChar + fileNameWithExtension).toURI().getPath());
		return new File(COLLECTOR_HOME_ALBUM_PICTURES + File.separatorChar + albumName + File.separatorChar + fileNameWithExtension).toURI();
	}

	/**
	 * Rename the old picture folder of the oldAlbumName to the new name. If it did no exist create the new folder anyway.
	 * @param oldAlbumName The old name of the album
	 * @param newAlbumName The new name of the album.
	 * @return True if either the folder was succesfully renamed or the new folder was successfully create. False if either operation failed.
	 */
	public static boolean renameAlbumPictureFolder(String oldAlbumName, String newAlbumName) {
		// Get the standard old picturefolderpath
		File oldalbumPicDir = new File (getFilePathForAlbum(oldAlbumName));
		// Get the standard new picturefolderpath
		File newalbumPicDir = new File(getFilePathForAlbum(newAlbumName));
		// Test if the old exists
		if (!oldalbumPicDir.exists()) {
			// Create the new picture folder anyway
			return newalbumPicDir.mkdir();
		}
		return oldalbumPicDir.renameTo(newalbumPicDir);
	}

	/**
	 * Internal method to add folders to the zip file.
	 * @param zipOutputStream The output stream to which the zip file is written.
	 * @param parentName The name of the parent directory.
	 * @param locationFile The file handle to the source files.
	 */
	private static void addDirectory(ZipOutputStream zipOutputStream, String parentName, File locationFile) {
		// retrieve all sub directories and files from the file/folder
		File[] files = locationFile.listFiles();

		for (int i=0; i < files.length; i++) {
			// if the file is directory, call the function recursively
			if (files[i].isDirectory()) {
				String zipFolderPath = parentName;
				if (parentName.isEmpty()) {
					zipFolderPath = files[i].getName() + "/";//File.separatorChar;	
				} else {
					zipFolderPath += files[i].getName() + "/";// File.separatorChar;
				}
				addDirectory(zipOutputStream, zipFolderPath, files[i]);
				continue;
			}

			// being here means that we have a file and not a directory
			try {
				byte[] buffer = new byte[1024];
				FileInputStream fileInputStream = new FileInputStream(files[i]);
				zipOutputStream.putNextEntry(new ZipEntry(parentName + files[i].getName()));

				// Store the file inside the zip
				int length;
				while((length = fileInputStream.read(buffer)) > 0) {
					zipOutputStream.write(buffer, 0, length);
				}

				// close current zip entry and the input stream of the current file
				zipOutputStream.closeEntry();
				fileInputStream.close();
			}
			catch(IOException ioe){
				System.err.println("FileSystemAccessWrapper: " + ioe.getMessage());
			}
		}
	}

	/**
	 * Zips a folder recursively into a file.
	 * @param folderLocation The path to the source directory.
	 * @param zipLocation The path to the target zip file.
	 */
	public static void zipFolderToFile(String folderLocation, String zipLocation) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(zipLocation);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			File folderLocationFile = new File(folderLocation);
			addDirectory(zipOutputStream, "", folderLocationFile);

			zipOutputStream.close();
		}
		catch(IOException ioe) {
			System.err.println("FileSystemAccessWrapper: " + ioe.getMessage());
		}
	}

	/**
	 * Unzips a file to the specified location, recreating the original file structure whitin it
	 * @param zipLocation The path to the future location of the zip file.
	 * @param folderLocation The path to the source directory.
	 */
	public static void unzipFileToFolder(String zipLocation, String folderLocation) {		
		try {
			ZipFile zipFile = new ZipFile(new File(zipLocation), ZipFile.OPEN_READ);

			Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();

			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

				if (!entry.isDirectory()) {
					File destFile = new File(folderLocation, entry.getName());
					File destinationParent = destFile.getParentFile();

					destinationParent.mkdirs();

					FileOutputStream fileOutputStream = new FileOutputStream(destFile);
					InputStream inputStream = zipFile.getInputStream(entry);

					// Copy the bits from inputStream to fileOutputStream
					byte[] buf = new byte[1024];
					int len;

					while ((len = inputStream.read(buf)) > 0) {
						fileOutputStream.write(buf, 0, len);
					}

					fileOutputStream.close();
				}
			}
			
			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("FileSystemAccessWrapper: " + ioe.getMessage());
		}

	}

	/**
	 * Deletes all content within the specified directory.
	 * @param path The file to the directory which is to be deleted.
	 * @return
	 */
	public static boolean recursiveDeleteFSObject(File path) {
		if( path.exists() ) {
			if (path.isDirectory()) {
				File[] files = path.listFiles();
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						recursiveDeleteFSObject(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return(path.delete());
	}

	/**
	 * Convenience method to erase all content from the default application home directory.
	 * The .collector directory as well as the database will still exist after calling this method. 
	 */
	public static void clearCollectorHome() {	
		File[] files = new File(COLLECTOR_HOME).listFiles();

		for (int i=0; i<files.length; i++) {
			if (files[i].isDirectory()) {
				recursiveDeleteFSObject(files[i]);
			} else if (!files[i].getName().equals(DATABASE_NAME)){
				files[i].delete();
			}
		}
	}

	/**
	 * Convenience method to remove the default application home directory. 
	 */
	public static void removeCollectorHome() {
		File collectorHome = new File(COLLECTOR_HOME);

		if (collectorHome.exists()) {
			FileSystemAccessWrapper.clearCollectorHome();

			new File(FileSystemAccessWrapper.DATABASE).delete();

			collectorHome.delete();
		}
	}

	/**
	 * Convenience method to delete the temporary database file {@link #DATABASE_TO_RESTORE}
	 */
	public static void deleteDatabaseRestoreFile() {
		File file = new File(DATABASE_TO_RESTORE);
		file.delete();
	}

	/**
	 * Copies a file from the input stream to the file  out.
	 * @param fileInputStream The input from which the information to copy is read.
	 * @param outputFile The file where the input stream is being copied.
	 */
	private void copyFromInputstreamToFile(FileInputStream fileInputStream, File outputFile) {//TODO: Test this!
		FileOutputStream fileoutputStream = null; // Stream to write to destination
		try {
			fileoutputStream = new FileOutputStream(outputFile); // Create output stream
			byte[] buffer = new byte[4096]; // To hold file contents
			int bytesRead; // How many bytes in buffer

			// Read a chunk of bytes into the buffer, then write them out,
			// looping until we reach the end of the file (when read() returns
			// -1).
			while ((bytesRead = fileInputStream.read(buffer)) != -1)
				// Read until EOF
				fileoutputStream.write(buffer, 0, bytesRead); // write
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Close both input and output streams, even if exceptions were thrown
		finally {
			if (fileInputStream != null)
				try {
					fileInputStream.close();
				} catch (IOException e) {
					System.err.println("Closing the file input stream failed!");
				}
			if (fileoutputStream != null)
				try {
					fileoutputStream.close();
				} catch (IOException e) {
					System.err.println("Closing the file output stream failed!");
				}
		}
	}

	public static void writeToFile(String content, String filepath) {
		try {
			PrintWriter out = new PrintWriter(filepath);

			out.write(content);

			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void storeViews(Collection<AlbumView> albumViews) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<views>\n");
		
		for (AlbumView albumView : albumViews) {
			xmlOutput.append("\t<view>\n");
			xmlOutput.append("\t\t<name>" + albumView.getName() + "</name>\n");
			xmlOutput.append("\t\t<album>" + albumView.getAlbum() + "</album>\n");
			xmlOutput.append("\t\t<sqlQuery>" + albumView.getSqlQuery() + "</sqlQuery>\n");
			xmlOutput.append("\t</view>\n");
		}
		
		xmlOutput.append("</views>\n");
		
		writeToFile(xmlOutput.toString(), VIEW_FILE);
	}

	public static Collection<AlbumView> loadViews() {
		String albumViewsAsXml = readFileAsString(VIEW_FILE);
		
		Collection<AlbumView> albumViews = new LinkedList<AlbumView>();
		
		if (albumViewsAsXml.isEmpty()) {
			return albumViews;
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
						
						albumViews.add(new AlbumView(name, album, sqlQuery));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return albumViews;
	}

	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}

	
	/** Reads the specified file into a string
	 * @param filePath the path to the file that should be read
	 * @return the content of the specified file as a string or an empty string if the file does not exist */
	public static String readFileAsString(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return "";
		}

		byte[] buffer = new byte[(int) new File(filePath).length()];

		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
			bufferedInputStream.read(buffer);
			bufferedInputStream.close();

			return new String(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String(buffer);
	}
}
