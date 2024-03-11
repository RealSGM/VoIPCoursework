public class CyclicRedundancyCheck {

    private static final int DIVISOR = 0b10011;

    private int computeCRC(int data) {
        int intData = data & 0xFF;
        int crc = intData << 4;

        for (int i = 0; i < 4; i++) {
            if ((crc & (1 << 8)) != 0) {
                crc ^= DIVISOR;
            }
            crc <<= 1;
        }
        return crc & 0b1111;
    }

    public byte[] encode(byte[] block) {
        byte[] returnBlock = new byte[1024];
        int index = 0;
        for (byte b:
                block) {
            byte[] encodedPair = encodeByte(b);
            returnBlock[index] = encodedPair[0];
            returnBlock[index+1] = encodedPair[1];
            index += 2;
        }
        return returnBlock;
    }
    private byte[] encodeByte(byte data){
        byte leftHalf = (byte) ((data & 0xF0) >> 4);
        byte rightHalf = (byte) (data & 0x0F);

        int leftCRC = computeCRC(leftHalf);
        int rightCRC = computeCRC(rightHalf);

        byte leftByte = (byte) ((leftHalf << 4) | leftCRC);
        byte rightByte = (byte) ((rightHalf << 4) | rightCRC);
        byte[] returnPair = {leftByte,rightByte};
        return returnPair;
    }
    public byte[] decode(byte[] packet){
        byte[] decodedPacket = new byte[512];
        int index = 0;
        boolean alternating = false;
        byte temp = (byte) 0;
        for (byte b: packet) {
            byte decodedByte = decodeByte(b);
            if (alternating){
                temp = (byte) (temp << 4);
                byte joinedByte = (byte) (decodedByte | temp);
                decodedPacket[index] = (byte) (joinedByte);
                index++;
            } else {
                temp = decodedByte;
            }
            alternating = !alternating;
        }
        return decodedPacket;

    }

    private byte decodeByte(byte data){
        boolean hasError = checkForErrors(data);
        if (hasError) {
            return (byte) 0x00;
        } else {
            return (byte) ((data >>> 4) & 0x0F);
        }
    }

    private boolean checkForErrors(byte receivedMessageWithCRC) {
        int receivedOriginalData = receivedMessageWithCRC >>> 4;
        int receivedCRC = receivedMessageWithCRC & 0b1111;
        int calculatedCRC = computeCRC((byte) receivedOriginalData);
        return receivedCRC != calculatedCRC;
    }

}
