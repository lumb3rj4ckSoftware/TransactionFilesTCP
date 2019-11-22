package Debug;

public class Debug {

    public static void logLengthOfTabsInByteArray(byte[] bytes, int readOverall) {
        String string = new String(bytes);
        int count = 0;
        for(char currentChar : string.toCharArray()) {
            if(currentChar == '\t') {
                count++;
            }
        }
        System.out.println("ByteArrayLength: " + (readOverall - count));
        System.out.println("Tabs found: " + count);
    }
}
