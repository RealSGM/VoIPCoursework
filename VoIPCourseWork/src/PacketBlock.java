import java.util.ArrayList;
import java.util.List;

public class PacketBlock {
    private final int blockSize = 4;
    private final List<byte[]> packets = new ArrayList<>();
    private byte[][] interleavedPackets = new byte[blockSize][blockSize];

    public void addPacket(byte[] packet) {
        packets.add(packet);
    }

    public List<byte[]> getPackets() {
        return packets;
    }

    public void interleavePackets() {
        // Add code
    }
}
