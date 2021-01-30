import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    CHOKE("0"),
    UNCHOKE("1"),
    INTERESTED("2"),
    NOT_INTERESTED("3"),
    HAVE("4"),
    BITFIELD("5"),
    REQUEST("6"),
    PIECE("7");

    public final String value;
    private static final Map<String, MessageType> map = new HashMap<>();
    static {
        for (MessageType type : values()) {
            map.put(type.value, type);
        }
    }

    MessageType(String s) {
        value = s;
    }

    public static MessageType getMessageTypeByValue(String s) {
        return map.get(s);
    }
}
