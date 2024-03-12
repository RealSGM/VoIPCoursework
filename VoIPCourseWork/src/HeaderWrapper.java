import java.util.HashMap;
import java.util.Map;

public class HeaderWrapper {
    private final long timestamp;
    private final int sequenceNumber;
    private static final short authenticationNumber = 10;

    Map<String, Integer> byteMap = new HashMap<>();

    public HeaderWrapper(long timestamp, int sequenceNumber) {
        this.timestamp = timestamp;
        this.sequenceNumber = sequenceNumber;
        byteMap.put("timestamp", 8);
        byteMap.put("sequenceNumber", 4);
        byteMap.put("authenticationNumber", 2);
    }

    // Getters
    public long getTimestamp() {
        return timestamp;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public short getAuthenticationNumber() {
        return authenticationNumber;
    }

    /**
     * Calculates the total size of the header in bytes.
     * @return The total size of the header in bytes
     */
    public int calculateHeaderSize() {
        return byteMap.get("timestamp") + byteMap.get("sequenceNumber") + byteMap.get("authenticationNumber");
    }
}
