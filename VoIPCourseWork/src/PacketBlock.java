import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a block of packets.
 */
public class PacketBlock {
    private List<byte[]> packets = new ArrayList<>();

    /**
     * Adds a packet to the block.
     *
     * @param packet The packet to be added.
     */
    public void addPacket(byte[] packet) {
        packets.add(packet);
    }

    /**
     * Retrieves the packets in the block.
     *
     * @return The list of packets in the block.
     */
    public List<byte[]> getPackets() {
        return packets;
    }

    /**
     * Interleaves the packets in the block.
     */
    public void interleavePackets() {
        int maxLength = packets.stream().mapToInt(arr -> arr.length).max().orElse(0);
        List<byte[]> interleavedPackets = new ArrayList<>();

        // Iterate over each byte position in the packets
        for (int i = 0; i < maxLength; i++) {
            // Iterate over each packet
            for (byte[] packet : packets) {
                // Check if the current byte position is within the length of the packet
                if (i < packet.length) {
                    // Ensure the interleaved packet list has enough space for the packet data
                    if (interleavedPackets.size() <= i) {
                        interleavedPackets.add(new byte[packets.size()]);
                    }
                    // Insert the byte from the current packet into the interleaved packet list
                    interleavedPackets.get(i)[packets.indexOf(packet)] = packet[i];
                }
            }
        }

        packets = interleavedPackets; // Update the packet list with interleaved packets
    }
}
