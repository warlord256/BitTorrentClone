import java.nio.ByteBuffer;

public class Piece {
    int id;
    byte[] content;

    Piece (int id, byte[] content) {
        this.id = id;
        this.content = content;
    }

    Piece (byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        id = buffer.getInt();
        content = new byte[buffer.remaining()];
        buffer.get(content,0, Math.min(Settings.PieceSize, buffer.remaining()));
    }

    public byte[] toByteArray() {
        return ByteBuffer
                .allocate(4+content.length)
                .putInt(id)
                .put(content)
                .array();
    }
}
