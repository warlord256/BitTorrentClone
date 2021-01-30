import java.io.IOException;
import java.net.Socket;
import java.util.BitSet;

public class RemotePeer {
    int remotePeerId;
    int listeningPort;
    boolean hasFile;
    String hostname;
    double downloadSpeed = 0.0;
    boolean isInterested = false;
    BitSet bitField = new BitSet(Settings.NumberOfPieces);
    RemotePeerState state = RemotePeerState.NOT_CONNECTED;
    Socket connection = null;
    Thread messageReceiver=null;
    MessageSender sender=null;
    Integer requestedPiece = null;
    private MessageReceiver receiverRunnable=null;


    RemotePeer(int id, String hostname, int port, boolean hasFile) {
        this.remotePeerId = id;
        this.hostname = hostname;
        this.listeningPort = port;
        this.hasFile = hasFile;
    }

    RemotePeer(String[] line) {
        this.remotePeerId = Integer.parseInt(line[0]);
        this.hostname = line[1];
        this.listeningPort = Integer.parseInt(line[2]);
        this.hasFile = "1".equals(line[3]);
    }

    public int getRemotePeerId() {
        return remotePeerId;
    }

    public void setRemotePeerId(int remotePeerId) {
        this.remotePeerId = remotePeerId;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    }

    public boolean isHasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public BitSet getBitField() {
        return bitField;
    }

    public void setBitField(BitSet bitField) {
        this.bitField = bitField;
    }

    public void setBitField(byte[] bitField) {
        this.bitField = new BitSet();
    }

    public synchronized RemotePeerState getState() {
        return state;
    }

    public void setState(RemotePeerState state) {
        this.state = state;
    }

    public Socket getConnection() {
        return connection;
    }

    public void setConnection(Socket connection) throws IOException {
        // Do not call this method before receiving and sending the handshake through the socket.
        this.connection = connection;
        this.state = RemotePeerState.CONNECTED;
        // Start the message receiver and sender.
        this.sender=new MessageSender(this);
        this.receiverRunnable = new MessageReceiver(connection, this);
        this.messageReceiver=new Thread(this.receiverRunnable);
        sender.start();
        messageReceiver.start();
    }

    public String toString() {
        return Integer.toString(this.remotePeerId);
    }

    public void updateHasFile(){
        this.hasFile=this.bitField.stream().count()==Settings.NumberOfPieces;
    }

    public void terminateThreads() throws IOException {
        if(null != connection) {
            sender.stopThread();
            receiverRunnable.stopThread();
            connection.close();
        }
    }

    public void setDownloadSpeed(double downloadSpeed) {
        // Calculating the average download speed
        this.downloadSpeed = this.downloadSpeed==0?downloadSpeed:((this.downloadSpeed+downloadSpeed)/2);
    }
}
