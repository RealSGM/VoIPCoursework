import java.util.Random;

public class DiffieHellman {
    public long P, G;
    private final long private_key, public_key;

    public DiffieHellman(long P, long G){
      this.P = P; //GeneratePrimeLong(); // Large Prime Number
        Random random = new Random();
//      this.G = random.nextLong(BOUND); // Primitive root modulo p
        this.G = G;
      this.private_key = random.nextLong(this.P); // Private Key
      this.public_key = generateKey(this.G, this.private_key, this.P);
//    this.b = new Random().nextLong(BOUND);
    }

    public long getPublic_key() {
        return public_key;
    }

    public boolean isPrime(long num){
        int n = (int)num;
        if (num <=1){
            return false;
        }
        for (int i = 2; i <= Math.sqrt(n); i++){
            if (num % i == 0){
                return false;
            }
        }
        return true;
    }

    private long GeneratePrimeLong(){
        long randomPrime;
        do {
            randomPrime = new Random().nextLong();
        } while (!isPrime(randomPrime));
        return randomPrime;
    }


    public long generateKey (long a, long b, long p){
        if (b == 1){
            return a;
        } else {
            return (((long) Math.pow(a, b)) % p);
        }
    }

    public long generateSecretKey (long otherPublicKey){
        if (this.private_key == 1){
            return otherPublicKey;
        } else {
            return (((long) Math.pow(otherPublicKey, this.private_key)) % this.P);
        }
    }



    public static void main(String[] args) {

        long P = 1125899839733759L;
        long G = 179424691L;

        DiffieHellman dh = new DiffieHellman(P, G);
        SenderKey sender = new SenderKey(dh);
        long senderPublicKey = sender.sendPublicKey();

        ReceiverKey receiver = new ReceiverKey(dh);
        long receiverPublicKey = receiver.sendPublicKey();

        sender.receivePublicKey(receiverPublicKey);
        receiver.receivePublicKey(senderPublicKey);
        System.out.println("Shared secret key (Sender): " + sender.getShared_key());
        System.out.println("Shared secret key (Receiver): " + receiver.getShared_key());
    }

}

class SenderKey{
    private final DiffieHellman dh;
    private long shared_key;

    public SenderKey(DiffieHellman dh){
        this.dh = dh;
    }

    public long sendPublicKey(){
        return this.dh.getPublic_key();
    }

    public void receivePublicKey(long otherPublicKey) {
        this.shared_key = this.dh.generateSecretKey(otherPublicKey);
    }

    public long getShared_key(){
        return this.shared_key;
    }
}

class ReceiverKey {
    private final DiffieHellman dh;
    private long shared_key;

    public ReceiverKey(DiffieHellman dh) {
        this.dh = dh;
    }

    public long sendPublicKey() {
        return this.dh.getPublic_key();
    }

    public void receivePublicKey(long otherPublicKey) {
        this.shared_key = this.dh.generateSecretKey(otherPublicKey);
    }

    public long getShared_key() {
        return this.shared_key;
    }
}