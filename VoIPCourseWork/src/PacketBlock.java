import java.util.ArrayList;
import java.util.List;

public class PacketBlock {
    private final int blockSize = 4;
    private List<List<byte[]>> block = new ArrayList<>();
    private int currentBlock = 0;

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
        block = interleavedBlock;

    }

    public boolean addPacketToBlock(byte[] packetData) {
        // Add packet to PacketBlock
        if (block.isEmpty()) {
            block.add(new ArrayList<>());
        }
        block.get(currentBlock).add(packetData);

        // If the block is full, create a new block
        if (block.get(currentBlock).size() == blockSize) {
            // Check if the block is full
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
