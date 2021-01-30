import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class peerProcess {
    public static int myPeerId=0;
    public static RemotePeer self = null;
    public static Map<Integer, RemotePeer> remotePeerMap = new HashMap<>();
    private static Piece[] filePieces = null;
    private static Thread connectionListener = null;
    private static List<Thread> remotePeerConnections = new ArrayList<>();
    public static Set<RemotePeer> preferredNeighbours = new HashSet<>();
    public static Set<RemotePeer> optimisticUnchokedNeighbours = new HashSet<>();
    public static boolean isProcessComplete = false;
    public static Random random = new Random();
    public static Logger logger = null;
    private static final String DIRECTORY = System.getProperty("user.dir");

    public static void main(String[] args) throws InterruptedException, IOException {
        myPeerId = Integer.parseInt(args[0]);
        logger = new Logger(myPeerId);
        Settings.loadSettingsFromConfig();
        List<RemotePeer> remotePeers = PeerInfoUtils.loadPeerInfoFromFile();

        for(RemotePeer peer : remotePeers) {
            if (myPeerId == peer.getRemotePeerId()) {
                self = peer;
            } else {
                remotePeerMap.put(peer.remotePeerId, peer);
            }
        }
        remotePeers.remove(self);
        boolean shouldDownloadFile = !self.hasFile;
        if(self.hasFile) {
            self.bitField.set(0,Settings.NumberOfPieces);
            // Split the file into pieces and store them in filePieces.
            filePieces = FileUtil.split(DIRECTORY+"/peer_"+myPeerId+"/"+Settings.FileName);
        } else {
            filePieces = new Piece[Settings.NumberOfPieces];
//            for(int i=0;i<Settings.NumberOfPieces;i++) {
//                missingPieces.add(i);
//            }
        }
        ConnectionListener listenerRunnable = new ConnectionListener(self.listeningPort);
        connectionListener = new Thread(listenerRunnable);
        connectionListener.start();
        for (RemotePeer peer : remotePeers) {
            if(peer.remotePeerId < myPeerId) {
                remotePeerConnections.add(new Thread(new PeerConnector(peer)));
            }
        }
        for(Thread peerConnector:remotePeerConnections){
            peerConnector.start();
        }
        System.out.println("Connected to all peers");

        Timer timer = new Timer();
        // Start preferred neighbour selection
        timer.schedule(new SelectPreferredNeighbours(remotePeerMap.values()), 0, Settings.UnchokingInterval);

        // Start optimistic unchoking of neighbours.
        timer.schedule(new SelectOptimisticUnchokedNeighbour(remotePeers), 0, Settings.OptimisticUnchokingInterval);

        while(!isProcessComplete) {
            Thread.sleep(5000);
            isProcessComplete = self.hasFile;
            for(RemotePeer peer : remotePeers) {
                isProcessComplete &= peer.hasFile;
            }
        }
        // If the file was downloaded then merge the file.
        if (shouldDownloadFile) {
            FileUtil.merge(DIRECTORY+"/peer_"+myPeerId+"/"+Settings.FileName, filePieces);
        }

        // Stop all threads and end.
        // Stop timer tasks.
        timer.cancel();
        timer.purge();

        // Stop listener.
        listenerRunnable.stopThread();

        // Close connections and stop threads.
        for(RemotePeer p : remotePeers) {
            p.terminateThreads();
        }
        System.out.println("All peers finished downloading");
    }

    public static boolean hasMissingPiece(RemotePeer peer) {
        if(self.hasFile) {
            return false;
        }
        for(int i=peer.getBitField().nextSetBit(0);i>=0;i=peer.getBitField().nextSetBit(i+1)) {
            if(!self.getBitField().get(i)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMissingPiece(int pieceId) {
        return !self.bitField.get(pieceId);
    }

    public static int pickRandomPieceToReceive(RemotePeer peer) {
        List<Integer> downloadablePieces = new ArrayList<>();
        for(int i=peer.getBitField().nextSetBit(0);i>=0;i=peer.getBitField().nextSetBit(i+1)) {
            if(!self.getBitField().get(i)) {
                downloadablePieces.add(i);
            }
        }
//        synchronized (missingPieces) {
//            for (int i : missingPieces) {
//                if (peer.getBitField().get(i)) {
//                    downloadablePieces.add(i);
//                }
//            }

            if (downloadablePieces.isEmpty()) {
                return -1;
            }
            int randomIndex = random.nextInt(downloadablePieces.size());
//            missingPieces.remove(downloadablePieces.get(randomIndex));
            return downloadablePieces.get(randomIndex);
//        }
    }

//    public static void addMissingPiece(Integer requestedPiece) {
//        // This function is used when a requested piece is responded with a choke message.
//        if(null == requestedPiece) {
//            return;
//        }
//        synchronized (missingPieces) {
//            missingPieces.add(requestedPiece);
//        }
//    }

    public static boolean checkDownloadCompletion() {
        self.hasFile = self.getBitField().stream().count()==Settings.NumberOfPieces;
        return self.hasFile;
    }

    public static long hasNumberOfPieces() {
        return self.getBitField().stream().count();
    }

    public static synchronized void setFilePiece(Piece piece, int index) {
        filePieces[index] = piece;
    }

    public static Piece getFilePiece(int index) {
        return filePieces[index];
    }
}
