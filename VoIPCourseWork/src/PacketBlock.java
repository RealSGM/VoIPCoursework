import java.util.ArrayList;
import java.util.List;

public class PacketBlock {
    public static final int blockSize = 2;
    private final List<List<byte[]>> block = new ArrayList<>();
    private int currentBlock = 0;

    /**
     * Interleaves packets within the block.
     */
    public void interleavePackets() {
        List<List<byte[]>> interleavedBlock = new ArrayList<>();
        for (int i = 0; i < blockSize; i++) {
            interleavedBlock.add(new ArrayList<>());
        }
        for (List<byte[]> block : block) {
            for (int i = 0; i < blockSize; i++) {
                interleavedBlock.get(i).add(block.get(i));
            }
        }
        block.clear();
        block.addAll(interleavedBlock);
    }

    /**
     * Adds a packet to the block and interleaves if the block is full.
     * @param packetData The data of the packet to add
     * @return True if the block is full after adding the packet, false otherwise
     */
    public boolean addPacketToBlock(byte[] packetData) {
        if (block.isEmpty()) {
            block.add(new ArrayList<>());
        }
        block.get(currentBlock).add(packetData);

        if (block.get(currentBlock).size() == blockSize) {
            if (block.size() == blockSize) {
                interleavePackets();
                return true;
            }
            block.add(new ArrayList<>());
            currentBlock++;
        }
        return false;
    }

    public List<byte[]> getPackets(){
        List<byte[]> packets = new ArrayList<>();
        for (List<byte[]> blockSection : block) {
            packets.addAll(blockSection);
        }
        return packets;
    }
}
