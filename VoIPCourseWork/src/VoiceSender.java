import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.*;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class VoiceSender implements Runnable {

    final int socketNum;
    final PacketWrapper dummyPacket;
    int port;
    InetAddress ip;
    private final DiffieHellman dh;

    DatagramSocket sendingSocket;

    int sequenceNumber = 0;
    double tempTime = 10;
    long elapsedTime = System.currentTimeMillis();

    private long shared_key;

    public VoiceSender(InetAddress clientIP, int clientPORT, int socketNumber, PacketWrapper dummyPacket, DiffieHellman dh) {
        this.ip = clientIP;
        this.port = clientPORT;
        this.socketNum = socketNumber;
        this.dummyPacket = dummyPacket;
        this.dh = dh;


        try {
            switch (socketNumber) {
                case 2 -> sendingSocket = new DatagramSocket2();
                case 3 -> sendingSocket = new DatagramSocket3();
                case 4 -> sendingSocket = new DatagramSocket4();
                default -> sendingSocket = new DatagramSocket();
            }
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            System.exit(0);
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        PacketBlock packetBlock = new PacketBlock();
        long startTime = System.currentTimeMillis();
        boolean running = true;
        CyclicRedundancyCheck encoder = new CyclicRedundancyCheck();
        try {
            AudioRecorder recorder = new AudioRecorder();

            while (running) {

                elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime > tempTime * 1000) {
                    running = false;
                }

                byte[] block = recorder.getBlock();
                byte[] encodedData;

                if (socketNum == 4) {
                     encodedData = encoder.encode(block);
                }else {
                    encodedData = block;
                }

                byte[] encryptedData = encryptData(encodedData);
                byte[] encryptedPacket = createPacket(encryptedData);

                if (socketNum != 3) {
                    sendPacket(encryptedPacket);
                } else {
                    if (packetBlock.addPacketToBlock(encryptedPacket)){
                        sendPacketBlock(packetBlock);
                        packetBlock = new PacketBlock();
                    }
                }
            }

            System.out.printf("Packets Sent: %d. %n", sequenceNumber);
            System.out.println("Bitrate: " + ((long) sequenceNumber * dummyPacket.calculatePacketSize() * 8 / (elapsedTime / 1000)));

        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encryptData(byte[] block) {
        // Initializing ByteBuffer for encryption
        long encryptionKey = this.getSharedKey();

        ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
        ByteBuffer plainText = ByteBuffer.wrap(block);

        for (int j = 0; j < block.length / 4; j++) {
            int fourByte = plainText.getInt();
            fourByte = fourByte ^ (int) encryptionKey; // XOR operation with key
            unwrapEncrypt.putInt(fourByte);
        }

        return unwrapEncrypt.array();
    }

    /**
     * Creates a voice packet with the provided encrypted data.
     * @param encryptedData The encrypted data to be included in the packet
     * @return The byte array representing the voice packet
     */
    private byte[] createPacket(byte[] encryptedData){
        long timestamp = System.currentTimeMillis();
        HeaderWrapper headerWrapper = new HeaderWrapper(timestamp, sequenceNumber);
        PacketWrapper packetWrapper = new PacketWrapper(headerWrapper, encryptedData);

        int size = packetWrapper.calculatePacketSize();
        ByteBuffer voicePacket = ByteBuffer.allocate(size);

        voicePacket.putShort(headerWrapper.getAuthenticationNumber());
        voicePacket.putInt(headerWrapper.getSequenceNumber());
        voicePacket.putLong(timestamp);
        voicePacket.put(encryptedData);

//        System.out.println(sequenceNumber+ " created at " + timestamp);
        sequenceNumber++;
        return voicePacket.array();
    }

    /**
     * Sends the packet block over the network.
     * @param packetBlock The packet block to be sent
     * @throws IOException If an I/O error occurs while sending the packet block
     */
    private void sendPacketBlock(PacketBlock packetBlock) throws IOException {
        for (byte[] packetData : packetBlock.getPackets()) {
            sendPacket(packetData);
        }
    }

    /**
     * Sends a single packet over the network.
     * @param packetData The byte array representing the packet to be sent
     * @throws IOException If an I/O error occurs while sending the packet
     */
    private void sendPacket(byte[] packetData) throws IOException {
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, ip, port);
        sendingSocket.send(packet);
    }

    public long sendPublicKey(){
        return this.dh.getPublic_key();
    }

    public void receivePublicKey(long otherPublicKey) {
        this.shared_key = this.dh.generateSecretKey(otherPublicKey);
    }

    public long getSharedKey(){
        return this.shared_key;
    }

}
