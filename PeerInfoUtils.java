import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PeerInfoUtils {
    public static List<RemotePeer> loadPeerInfoFromFile() {
        try (Scanner sc = new Scanner(new FileInputStream(Settings.PEER_INFO_FILE))) {
            List<RemotePeer> remotePeers = new ArrayList<>();
            while(sc.hasNextLine()) {
                String[] line = sc.nextLine().split(" ");
                if(line.length!=4) {
                    System.err.println("Bad PeerInfo file.");
                }
                remotePeers.add(new RemotePeer(line));
            }
            return remotePeers;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
