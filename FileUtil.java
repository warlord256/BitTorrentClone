import java.io.*;

public class FileUtil {
    /**
     * Splits the input file into Pieces
     * @param filePath
     * @return file in the form of Piece[]
     */
    public static Piece[] split(String filePath)
    {
        Piece[] dataPieces= new Piece[Settings.NumberOfPieces];
        try(FileInputStream fileInputStream=new FileInputStream(filePath))
        {
            int pieceId = 0;
            while(fileInputStream.available()>0)
            {
                int size=Math.min(fileInputStream.available(),Settings.PieceSize);
                byte[] piece=new byte[size];
                fileInputStream.read(piece,0,size);
                dataPieces[pieceId]=new Piece(pieceId,piece);
                pieceId++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("The file you are trying to read from is unavailable!!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O Exception");
            e.printStackTrace();
        }
        return dataPieces;
    }

    public static void merge(String filePath,Piece[] dataPieces)
    {
        try(FileOutputStream fileOutputStream=new FileOutputStream(filePath))
        {
            for(Piece itr:dataPieces)
            {
                fileOutputStream.write(itr.content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
