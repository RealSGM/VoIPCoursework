import java.util.HashMap;
import java.util.Map;

public class HeaderWrapper {
    final long timestamp;
    final int sequenceNumber;
    static final short authenticationNumber = 10;

    Map<String, Integer> byteMap = new HashMap<>();

    public HeaderWrapper(long timestamp, int sequenceNumber) {
        this.timestamp = timestamp;
        this.sequenceNumber = sequenceNumber;
        byteMap.put("timestamp",8);
        byteMap.put("sequenceNumber",4);
        byteMap.put("authenticationNumber",2);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public short getAuthenticationNumber() {
        return authenticationNumber;
    }

    public int calculateHeaderSize() {
        return byteMap.get("timestamp") + byteMap.get("sequenceNumber") + byteMap.get("authenticationNumber");

    }
}