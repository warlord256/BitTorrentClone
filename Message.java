import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message implements Serializable {
    public int length = 1;
    private MessageType messageType;
    private byte[] payload = new byte[0];

    Message(MessageType type) {
        this.setMessageType(type);
    }

    Message(MessageType type, String payload) {
        this.setMessageType(type);
        this.setPayload(payload);
    }

    Message(MessageType type, byte[] payload) {
        this.setMessageType(type);
        this.setPayload(payload);
    }

    Message(byte[] messageByteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(messageByteArray);
        this.length = buffer.getInt();
        String msgTypeValue = new String(new byte[] { buffer.get() }, StandardCharsets.UTF_8);
        this.messageType = MessageType.getMessageTypeByValue(msgTypeValue);
        if(this.length>1) {
            this.payload = new byte[this.length-1];
            buffer.get(this.payload);
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void setPayload(String payload) {
        this.payload = payload.getBytes();
        this.length = this.payload.length+1;
    }

    public byte[] toByteArray() {
        return ByteBuffer
                .allocate(length+4)
                .putInt(length)
                .put(messageType.value.getBytes())
                .put(payload)
                .array();
    }

    public String toString() {
        return "Type: "+messageType;
    }
}
