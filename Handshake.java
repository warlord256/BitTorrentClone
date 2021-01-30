import java.io.Serializable;

public class Handshake implements Serializable {
    String header="P2PFILESHARINGPROJ";
    byte[] zeroBits= new byte[10];
    int peerId;

    Handshake(int peerId){
        this.peerId=peerId;
    }
}
