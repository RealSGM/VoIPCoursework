import CMPC3M06.AudioRecorder;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class VoiceSender implements Runnable {
    DatagramSocket sendingSocket;

    int port;
    InetAddress ip;
    boolean running = true;

    public VoiceSender(InetAddress clientIP, int clientPORT) {
        this.ip = clientIP;
        this.port = clientPORT;

        // Create and run the thread when class is made
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        try {
            sendingSocket = new DatagramSocket(); // Opening a UDP socket for sending data

        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("X : Stop Recording");

        while (running) {

            try {
                AudioRecorder recorder = new AudioRecorder(); // Creating an AudioRecorder instance

                byte[] block = recorder.getBlock(); // Get block of audio data
                int encryptionKey = 15;
                short authenticationKey = 10;


                // Initializing ByteBuffer for encryption
                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
                ByteBuffer plainText = ByteBuffer.wrap(block);

                for (int j = 0; j < block.length / 4; j++) {
                    int fourByte = plainText.getInt();
                    fourByte = fourByte ^ encryptionKey; // XOR operation with key
                    unwrapEncrypt.putInt(fourByte);
                }
                byte[] encryptedBlock = unwrapEncrypt.array();

                // Creating a ByteBuffer for the voice packet
                ByteBuffer voicePacket = ByteBuffer.allocate(514);
                voicePacket.putShort(authenticationKey); // Adding authentication key
                voicePacket.put(encryptedBlock); // Adding encrypted audio data

                byte[] voiceArray = voicePacket.array(); // Getting the byte array representation of the voice packet

                // Creating a DatagramPacket with the voice data and sending it
                DatagramPacket packet = new DatagramPacket(voiceArray, voiceArray.length, ip, port);
                sendingSocket.send(packet);

                // Check if the user has pressed 'X' to stop recording
                if (System.in.available() > 0 && System.in.read() == 'X') {
                    stop(); // Stop recording if 'X' is pressed
                }


            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        running = false;
        sendingSocket.close(); // Close the socket
        System.exit(1);
    }
}
