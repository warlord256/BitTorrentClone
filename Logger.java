import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Logger {
    PrintStream out;
    SimpleDateFormat formatter= new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss z] : ");

    Logger(int peerId) throws FileNotFoundException {
        out = new PrintStream("log_peer_"+peerId+".log");
    }

    public synchronized void log(String message) {
        out.println(formatter.format(new Date())+message);
    }

    public synchronized void logMessage(Message message, int remotePeerId, Object... params){
        switch (message.getMessageType()){
            case INTERESTED:
                log("Peer " +peerProcess.myPeerId + " received the 'interested' message from " + remotePeerId + ".");
                break;
            case NOT_INTERESTED:
                log("Peer " +peerProcess.myPeerId + " received the 'not interested' message from " + remotePeerId + ".");
                break;
            case CHOKE:
                log("Peer " +peerProcess.myPeerId + " is choked by " + remotePeerId + ".");
                break;
            case UNCHOKE:
                log("Peer " +peerProcess.myPeerId + " is unchoked by " + remotePeerId + ".");
                break;
            case HAVE:
                log("Peer " +peerProcess.myPeerId + " received the 'have' message from " + remotePeerId + " for the piece " + params[0] +".");
                break;
            case PIECE:
                log("Peer " +peerProcess.myPeerId + " has downloaded the piece " + params[0] + " from " + remotePeerId + ". Now the number of pieces it has is " + params[1] + ". Download speed(GBps): " + params[2]);
                break;
        }
    }
}
