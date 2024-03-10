import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class VoiceDuplex {

    static InetAddress clientIP = null;
    static int port = 55555;
    static Scanner scanner = new Scanner(System.in);

    static int socketNum = 2    ;

    public static void main(String[] args) throws UnknownHostException {

//        clientIP = getValidIPAddress();
//        port = getValidPORT();
        clientIP = InetAddress.getLocalHost();
        port = 55555;

        VoiceProcessor processor =  new VoiceProcessor(socketNum);
        new VoiceSender(clientIP, port, socketNum);
        new VoiceReceiver(port, socketNum, processor);
    }

    public static InetAddress getValidIPAddress() {
        InetAddress tempIP = null;

        while (tempIP == null) {
            System.out.print("Enter IP: ");
            String ipString = scanner.next();

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

    public static int getValidPORT(){

        int tempPort = -1;

        while (tempPort == -1){
            System.out.print("Enter PORT: ");
            String portString = scanner.next();

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
