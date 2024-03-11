import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class VoiceDuplex {

    // Initialize variables
    static InetAddress clientIP = null;
    static int port = 55555;
    static Scanner scanner = new Scanner(System.in);
    static int socketNum = 4;
    static long P = 1125899839733759L;
    static long G = 179424691L;

    public static void main(String[] args) throws UnknownHostException {

        // Get valid IP address and port from user
//        clientIP = getValidIPAddress();
//        port = getValidPORT();
        clientIP = InetAddress.getLocalHost();
        port = 55555;

        DiffieHellman dh = new DiffieHellman(P,G);

        // Initialize VoiceProcessor, VoiceSender, and VoiceReceiver
        VoiceProcessor processor =  new VoiceProcessor(socketNum, dh);
        VoiceSender sender = new VoiceSender(clientIP, port, socketNum, dh);
        new VoiceReceiver(port, socketNum, processor);

        long senderPublicKey = sender.sendPublicKey();
        long receiverPublicKey = processor.sendPublicKey();

        sender.receivePublicKey(receiverPublicKey);
        processor.receivePublicKey(senderPublicKey);

    }

    // Method to get a valid IP address from the user
    public static InetAddress getValidIPAddress() {
        InetAddress tempIP = null;

        // Loop until a valid IP address is entered
        while (tempIP == null) {
            System.out.print("Enter IP: ");
            String ipString = scanner.next();

            // Exit if user enters 'E'
            if (ipString.equalsIgnoreCase("E")) {
                System.exit(0);
            }

            try {
                tempIP = InetAddress.getByName(ipString);

            } catch (UnknownHostException e) {
                System.out.println("ERROR: Invalid IP Address");
            }
        }
        return tempIP;
    }

    // Method to get a valid port number from the user
    public static int getValidPORT(){

        int tempPort = -1;

        // Loop until a valid port number is entered
        while (tempPort == -1){
            System.out.print("Enter PORT: ");
            String portString = scanner.next();

            // Exit if user enters 'E'
            if (portString.equalsIgnoreCase("E")){
                System.exit(0);
            }

            try {
                tempPort = Integer.parseInt(portString);

            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid port Entered.");
            }
        }
        return tempPort;
    }
}
