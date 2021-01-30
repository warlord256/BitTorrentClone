import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerConnector implements Runnable {
    RemotePeer peer;
    private static Logger logger = peerProcess.logger;
    PeerConnector(RemotePeer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        try {
            Socket connection=new Socket(peer.hostname,peer.listeningPort);
            performHandShake(connection);
            sendBitField(peer);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void performHandShake(Socket connection) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
        out.writeObject(new Handshake(peerProcess.myPeerId));
        System.out.println("Sent handshake");
        logger.log("Peer " +peerProcess.myPeerId + " makes a connection to Peer " + peer.remotePeerId + ".");
        ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
        Handshake hShake=(Handshake) in.readObject();
        System.out.println("Received handshake");
        if(hShake.peerId != peer.remotePeerId ) {
            System.err.println("Invalid handshake");
        }
//        System.out.println("All OK, setting connection");
        // Set the connection after the handshake is read or it'll cause a problem.
        peer.setConnection(connection);
        System.out.println("Done setting up, waiting for message");
    }

    private static void sendBitField(RemotePeer peer){
        peer.sender.send(new Message(MessageType.BITFIELD,peerProcess.self.bitField.toByteArray()));
    }
}
