package Server;

import Server.ServerMaintainer.ServerHandler.ServerHandlerImpl;
import org.json.JSONObject;

import java.io.*;

import static StandardCommandsAndNorms.CommandsAndNorms.*;

public class FileCopyServerHandler extends ServerHandlerImpl {

	/**
	 * basePath must end with a / for example: /home/dir1/dir2/
	 */
	private static String basePath;

	public FileCopyServerHandler() {}

	public FileCopyServerHandler(String basePath) {
		System.out.println("FileCopyServerHandler started");
		FileCopyServerHandler.basePath = basePath;
		System.out.println("Basepath set to " + basePath);
	}

	/**
	 * New Connection established in new Thread. Now you can start your workflow
	 * */
	@Override
	public void action(InputStream inputStream, OutputStream outputStream) throws IOException {

		while(true) {
			System.out.println("Trying to read file description...");

			//eigentliches Ziel
			final JSONObject fileDescription = readFileDescription(inputStream);
			if( fileDescription == null) {
				System.out.println("SENDING ACK");
				sendAck(false, outputStream);
				break;
			}

			sendAck(true, outputStream);

			if( ! copyFileFromClientAndSafeIt(fileDescription, inputStream) ) {
				sendAck(false, outputStream);
				break;
			}

			sendAck(true, outputStream);
		}
	}

	private JSONObject readFileDescription(InputStream inputStream) {
		byte[] buffer = readNBytes(DEFAULT_SIZE, inputStream);

		if(isEndOfTransaction(buffer)) {
			return null;
		}

		return tryCreateNewFile(buffer);
	}

	private boolean copyFileFromClientAndSafeIt(JSONObject fileDescription, InputStream inputStream) throws IOException {

		String pathNewFile = extractPathNewFile(fileDescription);
		int chunkSize = extractChunkSize(fileDescription);
		long size = extractSize(fileDescription);

		long readBytes = 0;
		FileOutputStream fileOutputStream = new FileOutputStream(pathNewFile);

		while(readBytes < size) {

			long diffToFileEnd = size - readBytes;
			byte[] bytesOfLastRead;

			if(diffToFileEnd < chunkSize) {
				bytesOfLastRead = readNBytes((int) diffToFileEnd, inputStream);
			} else {
				bytesOfLastRead = readNBytes(chunkSize, inputStream);
			}

			if(bytesOfLastRead == null) {
				System.out.println("PROTOCOL ERROR - FILE COULD NOT BE READ SUCCESSFULLY");
				return false;
			}

			readBytes += bytesOfLastRead.length;
			fileOutputStream.write(bytesOfLastRead);
		}

		System.out.println("File fully read...");
		fileOutputStream.close();

		if(readBytes != size) {
			System.out.println("PROTOCOL ERROR - read bytes does not match with file size");
		}

		return true;
	}

	private JSONObject tryCreateNewFile(byte[] buffer) {
		String json = extractJsonString(buffer);

		if ( ! isJsonValid(json)) {
			return null;
		}

//		System.out.println("json: " + json);

		JSONObject jsonObject = new JSONObject(json);
		String name = extractName(jsonObject);
		String path = extractPath(jsonObject);
		String session = extractSession(jsonObject);
		long size = extractSize(jsonObject);
		int chunkSize = extractChunkSize(jsonObject);

		if( ! areFileDescriptionValuesValid(name, path, size, session, chunkSize)) {
			System.out.println("FileDescriptionValues are wrong");
			return null;
		}

		File sessionDir = createSessionDir(session);
		if(sessionDir == null) {
			System.out.println("SessionDir is wrong");
			return null;
		}

		if( ! path.isEmpty()) {
			File pathDir = createPathDir(session, path);
			if(pathDir == null) {
				System.out.println("Creating path dir failed");
				return null;
			}
		}

		path = correctPathIfNecessary(path);

		final String pathNewFile = sessionDir.getAbsolutePath() + "/" + path + name;
		File newFile = new File(pathNewFile); //maybe path contain the name aswell. then this should work
		if(newFile.isDirectory()) {
			System.out.println("newFile is directory");
			return null;
		}

		//add the path of the newfile
		jsonObject.put("pathNewFile", pathNewFile);
		return jsonObject;
	}

	private String correctPathIfNecessary(String path) {
		if( ! ( path.isEmpty() ) && ! ( path.charAt(path.length() - 1) == '/') ) {
			path = path + "/";
		}
		return path;
	}

	private boolean isJsonValid(String json) {

		if( ! (json.contains("{") && json.contains("}")) ) {
			System.out.println("PROTOCOL ERROR 1 - INVALID DATA READ");
			return false;
		}

		if( json.indexOf("{") != 0 ) {
			System.out.println("PROTOCOL ERROR 2 - NO JSON FORMAT IN ");
			return false;
		}
		return true;
	}

	private File createSessionDir(String session) {
		File sessionDir = new File(basePath + "/" + session);

		if( sessionDir.exists() ) {
			return sessionDir;
		}

		if( ! sessionDir.mkdir()) {
			System.out.println("Could not create sessionDir: " + session);
			return null;
		}

		return sessionDir;
	}

	private File createPathDir(String session, String path) {
		File pathDir = new File(basePath + "/" + session + "/" + path);

		if( pathDir.exists() && pathDir.isDirectory()) {
			return pathDir;
		}

		if( pathDir.exists() && pathDir.isFile()) {
			return null;
		}

		if( ! pathDir.setWritable(true)) {
			System.out.println("Could not set writable to pathDir - no problem at all normally");
		}

		if( ! pathDir.mkdirs()) {
			System.out.println("Could not create pathDir: " + basePath + "/" + session + "/" + path);
			return null;
		}

		return pathDir;
	}

	private boolean areFileDescriptionValuesValid(String name, String path, long size, String session, int chunkSize) {
		return isNotNull(name) && isNotNull(path) && isNotNull(session) && size > 0 && chunkSize > 0;
	}

	private boolean isEndOfTransaction(byte[] buffer) {
		return removeFillMaterialFromReceivedInput(buffer).equals(END_FILE_COPY);
	}

	private String removeFillMaterialFromReceivedInput(byte[] buffer) {
		return new String(buffer).replace("\t", "");
	}

	private boolean isNotNull(Object object) {
		return object != null;
	}

	private String extractSession(JSONObject jsonObject) {
		return jsonObject.getString("session");
	}

	private String extractPath(JSONObject jsonObject) {
		return jsonObject.getString("path");
	}

	private String extractName(JSONObject jsonObject) {
		return jsonObject.getString("name");
	}

	private String extractPathNewFile(JSONObject jsonObject) {
		return jsonObject.getString("pathNewFile");
	}

	private int extractChunkSize(JSONObject jsonObject) {
		return jsonObject.getInt("chunkSize");
	}

	private long extractSize(JSONObject jsonObject) {
		return jsonObject.getLong("size");
	}

	private String extractJsonString(byte[] buffer) {
		return removeFillMaterialFromReceivedInput(buffer);
	}
}
