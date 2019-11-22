package Client;

import file.fileIO.Sn0xFileReader;
import file.fileStructure.PathUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static StandardCommandsAndNorms.CommandsAndNorms.*;

public class CopyFromClientToServer extends Sn0xFileReader {

	private OutputStream outToServer;
	private InputStream inFromServer;

	private String session = generateNewSession();

	private List<File> filesToCopy = new ArrayList<>();
	private ProgressView progressView;

	public CopyFromClientToServer(String ip, int port) {
		Socket clientSocket;
		try {
			clientSocket = new Socket(ip, port);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new IllegalStateException("clientSocket could not be established");
		}

		try {
			outToServer = clientSocket.getOutputStream();
			inFromServer = clientSocket.getInputStream();
		} catch (IOException e) {
			throw new IllegalStateException("streams could be assigned");
		}
	}

	@Override
	protected boolean doFileAction(File file) {
		//do the mechanic on every file
		filesToCopy.add(file);
		return true;
	}

	public void cpy(String path) {

		readFiles(new File(path)); //This starts the whole shit and fills filesToCopy

		if( progressView != null ) {
			progressView.setNumberOfFiles(filesToCopy.size());
			progressView.setTotalDataAmount(countDataAmount());
		}

		for(File file : filesToCopy) {
			if( ! copyFile(file) ) {
				System.out.println("Could not copy " + file.getAbsolutePath());
				break;
			}

			if( progressView != null ) {
				progressView.decNumberOfFiles();
				progressView.decNumberOfData(file.length());
			}
		}

		System.out.println("Finished copying all files");

		endFileCopy();
	}

	private boolean copyFile(File file) {
		//extract file infos to //TODO: maybe outsource
		String name = PathUtils.getFileName(file.getPath());

		if( ! sendFileDescription(file)) {
			System.out.println("Send filedescription " + name + " failed");
			return false;
		}

		if( ! sendFileInChunks(file)) {
			System.out.println("Send file " + name + " failed");
			return false;
		}

		if( ! confirmFileSend()) {
			System.out.println("Confirmed file " + name + " failed");
			return false;
		}

		return true;
	}

	private boolean sendFileDescription(File file) {
		JSONObject jsonObject = createFileDescription(file);
		String fileDescription = jsonObject.toString();

		//Send fileDescription
		if(fileDescription.getBytes().length > DEFAULT_SIZE) {
			System.out.println("Filedescription of " + jsonObject.getString("name") + " is too long");
			return false;
		}

		try {
			byte[] bufferFileDescription = new byte[DEFAULT_SIZE];
			fillByteArrayWithString(bufferFileDescription, fileDescription);

			outToServer.write(bufferFileDescription, 0, bufferFileDescription.length);

			return receiveAck(inFromServer);

		} catch (IOException e) {
			System.out.println("Could not write fileDescription to server");
			e.printStackTrace();
		}

		return false;
	}

	private boolean sendFileInChunks(File file) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);

			long allBytes = file.length();
			long readBytes = 0;

			while (readBytes < allBytes) {
				byte[] bytes = new byte[DEFAULT_SIZE]; //fileInputStream.readNBytes(chunkSize);
				int currentReadBytes = fileInputStream.read(bytes, 0, DEFAULT_SIZE);
				outToServer.write(bytes, 0, currentReadBytes);
				readBytes += currentReadBytes;
//				System.out.println("Wrote " + readBytes + "/" + allBytes + " Bytes to Server");
			}

			if(readBytes != allBytes) {
				System.out.println("PROTOCOL ERROR");
			}

			fileInputStream.close();
			System.out.println("File fully written to storage");
			return true;

		} catch (FileNotFoundException e) {
			System.out.println(PathUtils.getFileName(file.getAbsolutePath()) + " could not opened by fileInputStream");
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Some IO Exception happend to " + PathUtils.getFileName(file.getAbsolutePath()));
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	private boolean confirmFileSend() {
		return receiveAck(inFromServer);
	}

	public void setSession(String session) {
		this.session = session;
	}

	public void setProgressView(ProgressView progressView) {
		this.progressView = progressView;
	}

	public ProgressView getProgressView() {
		return this.progressView;
	}

	public static String generateNewSession() {
		Random random = new Random();
		String randomNumber = String.valueOf(random.nextInt(10000) + 1);
		return System.currentTimeMillis() + "&" + randomNumber;
	}

	private JSONObject createFileDescription(File file) {
		String name = PathUtils.getFileName(file.getPath());
		String path = PathUtils.getAbsolutePathWithoutFileName(file);

		JSONObject fileDescription = new JSONObject();
		fileDescription.put("name", name);
		fileDescription.put("path", path);
		fileDescription.put("size", file.length());
		fileDescription.put("session", session);
		fileDescription.put("chunkSize", DEFAULT_SIZE);

		String stringBuilder = "Name " + name + '\n' +
				"Path " + path + '\n' +
				"Size " + file.length() + '\n' +
				"Session " + session + '\n' +
				"ChunkSize " + DEFAULT_SIZE;
		System.out.println(stringBuilder);

		return fileDescription;
	}

	private void endFileCopy() {
		if(outToServer != null) {
			try {
				outToServer.write("END".getBytes());
			} catch (IOException e) {
				System.out.println("Could not send END");
			}
		}
	}

	@SuppressWarnings("IntegerDivisionInFloatingPointContext")
	private double countDataAmount() {
		if( filesToCopy == null ) {
			return 0D;
		}

		long fileSize = 0;
		for( File file : filesToCopy ) {
			fileSize += file.length();
		}

		return fileSize / 1_000_000;
	}
}