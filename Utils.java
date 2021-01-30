import java.nio.ByteBuffer;

public class Utils {

    public static byte[] intToByteArray(int value) {
        return ByteBuffer
                .allocate(4)
                .putInt(value)
                .array();
    }
    public static int byteArrayToInt(byte[] bytes) {
        return ByteBuffer
                .wrap(bytes)
                .getInt();
    }

    public static byte[] pieceToByteArray(Piece piece) {
        return ByteBuffer
                .allocate(4+piece.content.length)
                .putInt(piece.id)
                .put(piece.content)
                .array();
    }

    public static Piece byteArrayToPiece(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int id = buffer.getInt();
        byte[] content = new byte[Settings.PieceSize];
        buffer.get(content,0, Math.min(Settings.PieceSize, buffer.remaining()));
        return new Piece(id, content);
    }
}
