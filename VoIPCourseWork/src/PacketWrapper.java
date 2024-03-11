public record PacketWrapper(HeaderWrapper header, byte[] data) implements Comparable<PacketWrapper> {
    static final int dataSize = 512;

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
        return Integer.compare(header().getSequenceNumber(), o.header().getSequenceNumber());
    }
}
