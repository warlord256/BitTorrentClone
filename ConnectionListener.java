import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionListener implements Runnable {

    int port;
    boolean running = true;
    ServerSocket listener = null;

    ConnectionListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            listener = new ServerSocket(port);
            while(this.running) {
                Socket connection = listener.accept();
                RemotePeer peer=performHandshake(connection);
                sendBitField(peer);
            }
            listener.close();
        } catch (SocketException e) {
//            System.out.println("Closed the listener.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static RemotePeer performHandshake(Socket connection) throws IOException, ClassNotFoundException {
        //Receive handshake message.
        ObjectInputStream readIn=new ObjectInputStream(connection.getInputStream());
        // Read the string and zero bytes from the handshake.
        Handshake hShake=(Handshake) readIn.readObject();

        peerProcess.logger.log("Peer " +peerProcess.myPeerId + " is connected from Peer " + hShake.peerId+ ".");
        System.out.println("Got HS from "+hShake.peerId);

        // Verify message if required.
        if (!Settings.HandshakeMessage.equals(hShake.header)) {
            throw new RuntimeException("Protocol mismatch! Wrong handshake received.");
        }
        System.out.println("Started message handlers");

        // Send handshake message to the remote peer.
        ObjectOutputStream out=new ObjectOutputStream(connection.getOutputStream());
        out.writeObject(new Handshake(peerProcess.myPeerId));
        System.out.println("Sent out a handshake!");

        // Set the connection value to the respective remote peer object.
        RemotePeer peer = peerProcess.remotePeerMap.get(hShake.peerId);
        peer.setConnection(connection);
        return peer;
    }

    private static void sendBitField(RemotePeer peer){
        peer.sender.send(new Message(MessageType.BITFIELD,peerProcess.self.bitField.toByteArray()));
    }

    public void stopThread() {
        this.running = false;
        try {
            this.listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
