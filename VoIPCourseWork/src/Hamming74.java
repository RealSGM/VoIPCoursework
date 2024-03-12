public class Hamming74 {
    static int counter = 0;
    static int counter2 = 0;
    public byte[] encode(byte data){
        //Split byte into two
        byte leftHalf = (byte) ((data & 0xF0) >> 4);
        byte rightHalf = (byte) (data & 0x0F);

        byte leftByte = createHammingByte(leftHalf);
        byte rightByte = createHammingByte(rightHalf);
        byte[] returnPair = {leftByte,rightByte};
        return returnPair;
    }
    public byte decode(byte data){
        //get data bits
        boolean d3_ = ((data & 0b0000001) >> 0) == 1;
        boolean d2_ = ((data & 0b0000010) >> 1) == 1;
        boolean d1_ = ((data & 0b0000100) >> 2) == 1;
        boolean d0_ = ((data & 0b0010000) >> 4) == 1;
        //get parity bits
        boolean p2_ = ((data & 0b0001000) >> 3) == 1;
        boolean p1_ = ((data & 0b0100000) >> 5) == 1;
        boolean p0_ = ((data & 0b1000000) >> 6) == 1;
        //encsapsulate it
        boolean[] recieved = {p0_,p1_,d0_,p2_,d1_,d2_,d3_};
        //recalculate parity bits
        boolean p0 = !(d0_ ^ d1_ ^ d3_);
        boolean p1 = !(d0_ ^ d2_ ^ d3_);
        boolean p2 = !(d1_ ^ d2_ ^ d3_);
        //check parity bits and calculate a value for k
        int k = 0;
        if (p0 != p0_)
            k += 1;
        if (p1 != p1_)
            k += 2;
        if (p2 != p2_)
            k += 4;
        //returns corrected byte if needed

        if (k>0){
            //k points to the erroneous bit
            data = (byte) (data ^ (1 << k));
        }
        //re-get data bits
        boolean d3 = ((data & 0b0000001) >> 0) == 1;
        boolean d2 = ((data & 0b0000010) >> 1) == 1;
        boolean d1 = ((data & 0b0000100) >> 2) == 1;
        boolean d0 = ((data & 0b0010000) >> 4) == 1;
        byte result = (byte) (
                        ((d0 ? 1 : 0) << 3) |
                        ((d1 ? 1 : 0) << 2) |
                        ((d2 ? 1 : 0) << 1) |
                        ((d3 ? 1 : 0) << 0)
        );
        return result;
    }
    private byte createHammingByte(byte nibble){
        //Get 4 data bits
        boolean d3 = ((nibble & 0b0001) >> 0) == 1;
        boolean d2 = ((nibble & 0b0010) >> 1) == 1;
        boolean d1 = ((nibble & 0b0100) >> 2) == 1;
        boolean d0 = ((nibble & 0b1000) >> 3) == 1;
        //calculate odd parity bits
        boolean p0 = !(d0 ^ d1 ^ d3);
        boolean p1 = !(d0 ^ d2 ^ d3);
        boolean p2 = !(d1 ^ d2 ^ d3);
        //combine
        byte result = (byte) (
                        ((p0 ? 1 : 0) << 6) |
                        ((p1 ? 1 : 0) << 5) |
                        ((d0 ? 1 : 0) << 4) |
                        ((p2 ? 1 : 0) << 3) |
                        ((d1 ? 1 : 0) << 2) |
                        ((d2 ? 1 : 0) << 1) |
                        ((d3 ? 1 : 0) << 0)
        );
        return result;
    }
}

