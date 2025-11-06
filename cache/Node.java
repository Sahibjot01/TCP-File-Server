package cache;

public class Node {
    String key;
    byte[] value;
    Node prev;
    Node next;
    Integer size;

    public Node(String key, byte[] value) {
        this.key = key;
        this.value = value;
        this.prev = null;
        this.next = null;
        this.size = value.length;
    }

    Integer getSize() {
        return size;
    }

    void updateValue(byte[] newValue) {
        this.value = newValue;
        this.size = newValue.length;
    }
}
