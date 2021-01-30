import java.io.FileNotFoundException;
import java.util.*;

public class SelectPreferredNeighbours extends TimerTask{

    List<RemotePeer> remotePeers;
    TreeSet<RemotePeer> interested;

    SelectPreferredNeighbours(Collection remotePeers) {
        this.remotePeers = new ArrayList<>(remotePeers);
        this.interested = new TreeSet<>(new Comparator<RemotePeer>() {
            @Override
            public int compare(RemotePeer o1, RemotePeer o2) {
                int res = Double.compare(o2.downloadSpeed,o1.downloadSpeed);
                return res==0?o2.remotePeerId - o1.remotePeerId:res;
            }
        });
    }

    @Override
    public void run() {
        //Select preffered neighbours
        interested.clear();
        for (RemotePeer peer : remotePeers) {
            if(peerProcess.optimisticUnchokedNeighbours.contains(peer)) {
                continue;
            }
            if (peer.isInterested) {
                interested.add(peer);
            }
            if(null != peer.sender) {
                peer.state = RemotePeerState.CHOKED;
                peer.sender.send(new Message(MessageType.CHOKE));
            }
        }
        peerProcess.preferredNeighbours.clear();
        int selectedNeighbours = 0;
        for(RemotePeer peer : interested) {
            peerProcess.preferredNeighbours.add(peer);
            peer.sender.send(new Message(MessageType.UNCHOKE));
            peer.state = RemotePeerState.UNCHOKED;
            selectedNeighbours++;
            if(selectedNeighbours == Settings.NumberOfPreferredNeighbours) {
                break;
            }
        }
        peerProcess.logger.log("Peer " +peerProcess.myPeerId + " has the preferred neighbors " + peerProcess.preferredNeighbours + ".");
    }
}
