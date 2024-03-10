import java.util.ArrayList;
import java.util.List;

public class PacketBlock {
    private List<byte[]> packets = new ArrayList<>();

    public PacketBlock() {

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

    public void interleavePackets(){
        int maxLength = packets.stream().mapToInt(arr -> arr.length).max().orElse(0);
        List<byte[]> interleavedPackets = new ArrayList<>();

        for (int i = 0; i < maxLength; i++) {
            for (byte[] packet : packets) {
                if (i < packet.length) {
                    if (interleavedPackets.size() <= i) {
                        interleavedPackets.add(new byte[packets.size()]);
                    }
                    interleavedPackets.get(i)[packets.indexOf(packet)] = packet[i];
                }
            }
        }

        packets = interleavedPackets;
    }
}
