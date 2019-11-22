package StandardCommandsAndNorms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class CommandsAndNorms {
    public final static int DEFAULT_SIZE = 2048;
    public final static String END_FILE_COPY = "END";
    private final static int DEFAULT_ACK_SIZE = 10;

    public static void sendAck(boolean success, OutputStream outputStream) throws IOException {
        byte[] byteAnswer = new byte[DEFAULT_ACK_SIZE];
        if(success) {
            fillByteArrayWithString(byteAnswer, "OK");
        } else {
            fillByteArrayWithString(byteAnswer, "NOPE");
        }

        outputStream.write(byteAnswer);
    }

    public static boolean receiveAck(InputStream inputStream) {
        byte[] buffer = readNBytes(DEFAULT_ACK_SIZE, inputStream);
        if(buffer == null) {
            System.out.println("PROTOCOL ERROR - receiveAck could not be read");
            return false;
        }
        String ack = new String(buffer).replace("\t", "");
        return ack.equalsIgnoreCase("OK");
    }

    public static void fillByteArrayWithString(byte[] byteArray, String content) {
        byte[] contentBytes = content.getBytes();
        Arrays.fill(byteArray, (byte)'\t');
        System.arraycopy(contentBytes, 0, byteArray, 0, contentBytes.length);
    }

    public static byte[] readNBytes(int numberOfBytes, InputStream inputStream) {
        byte[] bytes = new byte[numberOfBytes];
        int posBytesWritten = 0;
        try {
            int read = inputStream.read(bytes, 0, numberOfBytes);
            //System.out.println("Read: " + read);
            if( read == numberOfBytes ) {
                return bytes;
            }

            posBytesWritten += read;

            while( posBytesWritten < numberOfBytes) {
                byte[] lastBytes = new byte[ (numberOfBytes-posBytesWritten) ];
                read = inputStream.read(lastBytes, 0, lastBytes.length);
                //System.out.println("Read: " + read);

                //EOS might happen here
                if(read == -1 ) {
                    return null;
                }

                //No Bytes are read
                if(read == 0) {
                    continue;
                }

                //Bytes are read and ready to be stored in the original array
                System.arraycopy(lastBytes, 0, bytes, posBytesWritten, read);
                posBytesWritten += read;
            }

            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
