import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * VoiceDuplex class manages the main functionality for a voice duplex system.
 */
public class VoiceDuplex {
    public static void main(String[] args) throws UnknownHostException {
        // Initialize variables
        int socketNum = 1;
//        Scanner scanner = new Scanner(System.in);
//        InetAddress clientIP = getValidIPAddress(scanner);
//        int port = getValidPORT(scanner);

         //Get valid IP address and port from user
        clientIP = getValidIPAddress();
        port = getValidPORT();
        socketNum = getSocketNum();
//        clientIP = InetAddress.getLocalHost();
//        port = 55555;

        DiffieHellman dh = new DiffieHellman();

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
            System.out.print("Enter IP (default:localhost): ");
            String ipString = scanner.nextLine();

            // Exit if user enters 'E'
            if (ipString.equalsIgnoreCase("E")) {
                System.exit(0);
            }

            // Set the IP address to localhost if the input is empty
            if (ipString == "") {
                ipString = "localhost";
            }

            try {
                return InetAddress.getByName(ipString);
            } catch (UnknownHostException e) {
                System.out.println("ERROR: Invalid IP Address");
            }
        }
    }

    // Method to get a valid port number from the user
    public static int getValidPORT(){

        int tempPort = -1;

        // Loop until a valid port number is entered
        while (tempPort == -1){
            System.out.print("Enter PORT (default:5555): ");
            String portString = scanner.nextLine();

            // Exit if user enters 'E'
            if (portString.equalsIgnoreCase("E")){
                System.exit(0);
            }

            // Sets the port string to 5555 if empty
            if (portString == "") {
                portString = "5555";
            }

            try {
                return Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid port Entered.");
            }
        }
    }

    //Get valid socket number
    public static int getSocketNum() {
        Scanner scanner = new Scanner(System.in);
        int userInput;
        do {
            System.out.print("Please enter a socket (1,2,3,4): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a valid socket(1,2,3,4): ");
                scanner.next(); // Consume the invalid input
            }
            userInput = scanner.nextInt();

            if (userInput < 1 || userInput > 4) {
                System.out.print("Invalid input. Please enter a socket(1,2,3,4): ");
            }
        } while (userInput < 1 || userInput > 4);

        return userInput;
    }
}

