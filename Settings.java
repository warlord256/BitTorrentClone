import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class Settings {
    public static int NumberOfPreferredNeighbours;
    public static long UnchokingInterval;
    public static long OptimisticUnchokingInterval;
    public static String FileName;
    public static int FileSize;
    public static int PieceSize;
    public static int NumberOfPieces;
    public static final int NumberOfOptimisticUnchokedNeighbours = 1;
    public static final String HandshakeMessage = "P2PFILESHARINGPROJ";
    public static final String PEER_INFO_FILE = "PeerInfo.cfg";
    private static final String CONFIG_FILE = "Common.cfg";

    public static void loadSettingsFromConfig() {
        try (Scanner sc = new Scanner(new FileInputStream(CONFIG_FILE))) {
            while(sc.hasNextLine()) {
                String[] line = sc.nextLine().split(" ");
                if (line.length != 2) {
                    System.err.println("Bad config file.");
                }
                switch(line[0]) {
                    case "NumberOfPreferredNeighbors":
                        NumberOfPreferredNeighbours = Integer.parseInt(line[1]);
                        break;
                    case "UnchokingInterval":
                        UnchokingInterval = Long.parseLong(line[1])*1000;
                        break;
                    case "OptimisticUnchokingInterval":
                        OptimisticUnchokingInterval = Long.parseLong(line[1])*1000;
                        break;
                    case "FileName":
                        FileName = line[1];
                        break;
                    case "FileSize":
                        FileSize = Integer.parseInt(line[1]);
                        break;
                    case "PieceSize":
                        PieceSize = Integer.parseInt(line[1]);
                }
            }

            NumberOfPieces =(int) Math.ceil(((float)FileSize)/PieceSize);
            System.out.println("Successfully read Common.cfg: File name: " + FileName + " Number of pieces: " + NumberOfPieces);

        } catch (IOException ex) {
            System.err.println("Cannot find configuration file.");
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }

    }
}
