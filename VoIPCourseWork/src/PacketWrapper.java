public class PacketWrapper implements Comparable<PacketWrapper>{
    long timestamp;
    byte[] data;

    public PacketWrapper(long timestamp, byte[] data) {
        this.timestamp = timestamp;
        this.data = data;
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
