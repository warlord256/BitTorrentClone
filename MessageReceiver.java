import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.BitSet;

public class MessageReceiver implements Runnable{
    private ObjectInputStream in;
    private RemotePeer peer;
    private boolean running = true;
    private static Logger logger = peerProcess.logger;


    MessageReceiver(Socket socket, RemotePeer peer) throws IOException {
        in=new ObjectInputStream(socket.getInputStream());
        this.peer = peer;
    }

    private void handleMessage(Message message, long downloadTime) {
        // Handle the message
//        System.out.println(peer.getRemotePeerId()+""+message);
        switch(message.getMessageType()) {
            case BITFIELD:
                // Set the bitfield value for the remote peer.
                peer.setBitField(BitSet.valueOf(message.getPayload()));
                // Compare bitfields and send appropriate message.
                if (peerProcess.hasMissingPiece(peer)){
                    peer.sender.send(new Message(MessageType.INTERESTED));
                } else {
                    peer.sender.send(new Message(MessageType.NOT_INTERESTED));
                }
                peer.updateHasFile();
                break;
            case INTERESTED:
                peer.isInterested = true;
                logger.logMessage(message,peer.remotePeerId);
                break;
            case NOT_INTERESTED:
                peer.isInterested = false;
                logger.logMessage(message,peer.remotePeerId);
                break;
            case CHOKE:
                logger.logMessage(message,peer.remotePeerId);
                break;
            case REQUEST:
                // Send Piece if still a neighbour.
                if (peer.getState() == RemotePeerState.UNCHOKED) {
                    int requestingPiece = Utils.byteArrayToInt(message.getPayload());
                    Piece pieceToSend = peerProcess.getFilePiece(requestingPiece);
                    peer.sender.send(new Message(MessageType.PIECE, pieceToSend.toByteArray()));
                }
                break;
            case PIECE:
                // Receive the piece.
                Piece receivedPiece = new Piece(message.getPayload());
                if(!peerProcess.self.getBitField().get(receivedPiece.id)) {
                    peerProcess.setFilePiece(receivedPiece, receivedPiece.id);
                    // Update self.Bitfield
                    peerProcess.self.getBitField().set(receivedPiece.id);
                    // Calculates and updates this peer's download rate
                    // TODO: Make sure download time is not zero
                    peer.setDownloadSpeed((double) receivedPiece.content.length / downloadTime);
                    // Log piece download.
                    logger.logMessage(message, peer.remotePeerId,receivedPiece.id,peerProcess.hasNumberOfPieces(),peer.downloadSpeed);
                    // Check if the complete file has downloaded.
                    if(peerProcess.checkDownloadCompletion()){
                        logger.log("Peer " +peerProcess.myPeerId + " has downloaded the complete file.");
                    }
                    // Send have messages to all peers.
                    broadcastHaveMessage(receivedPiece.id);
                }
                // If still a neighbour request for more pieces.
            case UNCHOKE:
                int pieceId = peerProcess.pickRandomPieceToReceive(peer);
                if (pieceId >= 0) {
                    peer.sender.send(new Message(MessageType.REQUEST, Utils.intToByteArray(pieceId)));
                } else {
                    peer.sender.send(new Message(MessageType.NOT_INTERESTED));
                }
                if(message.getMessageType().equals(MessageType.UNCHOKE)) {
                    logger.logMessage(message, peer.remotePeerId);
                }
                break;
            case HAVE:
                // Update peers bitfield to include the piece.
                int newPieceId = Utils.byteArrayToInt(message.getPayload());
                peer.bitField.set(newPieceId);
                // If this piece is missing from the array then send interested message
                if (peerProcess.isMissingPiece(newPieceId)) {
                    peer.sender.send(new Message(MessageType.INTERESTED));
                }
                logger.logMessage(message,peer.remotePeerId,newPieceId);
                // If the RemotePeer has all the pieces then set hasFile
                peer.updateHasFile();
                break;
        }
    }

    private void broadcastHaveMessage(int pieceId){
        for(RemotePeer peer:peerProcess.remotePeerMap.values()){
            if(peer.getState()!=RemotePeerState.NOT_CONNECTED){
                peer.sender.send(new Message(MessageType.HAVE,Utils.intToByteArray(pieceId)));
            }
        }
    }

    public void stopThread() {
        this.running = false;
    }

    @Override
    public void run() {
        try {
            while(this.running && !peer.connection.isClosed()) {
                long start = System.nanoTime();
                Message message= (Message) in.readObject();
                long end = System.nanoTime();
                handleMessage(message,end-start);
            }
            in.close();
        } catch (EOFException | SocketException e) {
//            System.out.println("Seems like I'm almost done...");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("Quitting the Receiver");
        }
    }
}
