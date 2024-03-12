import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * VoiceDuplex class manages the main functionality for a voice duplex system.
 */
public class VoiceDuplex {
    public static void main(String[] args) throws UnknownHostException {
        //Get valid IP address and port from user
        Scanner scanner = new Scanner(System.in);
        InetAddress clientIP = getValidIPAddress(scanner);
        int port = getValidPort(scanner);
        int socketNum = getSocketNum(scanner);

        DiffieHellman dh = new DiffieHellman();
        HeaderWrapper headerWrapper = new HeaderWrapper(1L, 1);

        PacketWrapper dummyPacket = new PacketWrapper(headerWrapper, new byte[(socketNum == 4) ? PacketWrapper.dataSize * 2 : PacketWrapper.dataSize]);

        // Initialize VoiceProcessor, VoiceSender, and VoiceReceiver
        VoiceProcessor processor =  new VoiceProcessor(socketNum, dummyPacket, dh);
        VoiceSender sender = new VoiceSender(clientIP, port, socketNum, dummyPacket, dh);
        new VoiceReceiver(port, socketNum, processor, dummyPacket);

        // Encryption Key
        long senderPublicKey = sender.sendPublicKey();
        long receiverPublicKey = processor.sendPublicKey();
        sender.receivePublicKey(receiverPublicKey);
        processor.receivePublicKey(senderPublicKey);

    }

    // Method to get a valid IP address from the user
    public static InetAddress getValidIPAddress(Scanner scanner) throws UnknownHostException {
        InetAddress tempIP = null;

        // Loop until a valid IP address is entered
        while (true) {
            System.out.print("Enter IP (default:localhost): ");
            String ipString = scanner.nextLine();

            // Set the IP address to localhost if the input is empty
            if (ipString.isEmpty()) {
                return InetAddress.getLocalHost();
            }

            try {
                return InetAddress.getByName(ipString);
            } catch (UnknownHostException e) {
                System.out.println("ERROR: Invalid IP Address");
            }
        }
    }

    // Method to get a valid port number from the user
    public static int getValidPort(Scanner scanner) {
        while (true) {
            System.out.print("Enter PORT (Default: 5555): ");
            String portString = scanner.nextLine();

            if (portString.isEmpty()) {
                return 5555;
            }

            try {
                return Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid port entered.");
            }
        }
    }

    //Get valid socket number
    public static int getSocketNum(Scanner scanner) {
        int userInput;
        do {
            System.out.print("Please enter a socket (1, 2, 3, 4): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a valid socket (1, 2, 3, 4): ");
                scanner.next(); // Consume the invalid input
            }
            userInput = scanner.nextInt();
        } while (userInput < 1 || userInput > 4);
        return userInput;
    }
}

