import java.io.FileNotFoundException;
import java.util.*;

public class SelectOptimisticUnchokedNeighbour extends TimerTask {

    List<RemotePeer> remotePeers;
    List<RemotePeer> candidates = new ArrayList();
    Random random = new Random();

    SelectOptimisticUnchokedNeighbour(Collection remotePeers) {
        this.remotePeers = new ArrayList<>(remotePeers);
    }

    @Override
    public void run() {
        System.out.println("OptimisticUnchokedNeighbour task is triggered.");
        //Optimistically unchoke a neighbour
        candidates.clear();
        for(RemotePeer peer: remotePeers) {
            if(peer.isInterested && peer.state==RemotePeerState.CHOKED) {
                candidates.add(peer);
            }
        }

        for (int i = 0; i < Math.min(Settings.NumberOfOptimisticUnchokedNeighbours, candidates.size()); i++) {
            RemotePeer peer = candidates.get(random.nextInt(candidates.size()));
            if(null != peer.sender) {
                peer.sender.send(new Message(MessageType.UNCHOKE));
                peer.state = RemotePeerState.UNCHOKED;
                peerProcess.logger.log("Peer " +peerProcess.myPeerId + " has optimistically unchoked neighbor " + peer.getRemotePeerId() + ".");
            }
        }
    }
}
