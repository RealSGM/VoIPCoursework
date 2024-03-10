import java.util.ArrayList;
import java.util.List;

public class PacketBlock {
    private List<byte[]> packets;

    public PacketBlock() {
        packets = new ArrayList<>();
    }

    public void addPacket(byte[] packet) {
        packets.add(packet);
    }

    public List<byte[]> getPackets() {
        return packets;
    }

    public void setPackets(List<byte[]> deinterleavedPackets) {
        packets = deinterleavedPackets;
    }
}
