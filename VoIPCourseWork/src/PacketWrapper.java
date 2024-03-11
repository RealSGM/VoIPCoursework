public final class PacketWrapper implements Comparable<PacketWrapper> {
    static final int dataSize = 1024;
    private final HeaderWrapper header;
    private final byte[] data;

    public PacketWrapper(HeaderWrapper header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public int calculatePacketSize() {
        return header.calculateHeaderSize() + data.length;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int compareTo(PacketWrapper o) {
        return Integer.compare(getHeader().getSequenceNumber(), o.getHeader().getSequenceNumber());
    }

    public HeaderWrapper getHeader() {
        return header;
    }

    public byte[] data() {
        return data;
    }
}
