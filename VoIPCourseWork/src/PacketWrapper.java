public class PacketWrapper implements Comparable<PacketWrapper>{
    final long timestamp;
    final int sequenceNumber;
    final byte[] data;

    public PacketWrapper(long timestamp, byte[] data, int num) {
        this.timestamp = timestamp;
        this.data = data;
        this.sequenceNumber = num;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int compareTo(PacketWrapper other) {
        return Long.compare(this.timestamp, other.timestamp);
    }
}
