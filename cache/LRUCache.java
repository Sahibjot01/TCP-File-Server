package cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LRUCache {
    private final int capacity;
    private final Map<String, Node> map;
    private Node head;
    private Node tail;
    private final ReentrantLock lock;

    private long hits = 0;
    private long misses = 0;
    private long putCount = 0;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>(capacity);
        lock = new ReentrantLock(false);
        this.head = null;
        this.tail = null;
    }

    public byte[] get(String key) {
        // check if content is present
        // if present move it to head
        lock.lock();
        try {
            Node node = map.get(key);

            if (node == null) {
                misses++;
                return null;
            }

            // found
            hits++;
            // move node to front of ddL
            addToHead(node);

            // return copy
            // byte[] copy = Arrays.copyOf(node.value, node.size);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public void put(String key, byte[] value) {
        // when LRU didnt had the key or wanted to update the content of value then user
        // read or reread the file in server then give it to us so we can save it
        // for safe side first check if it exist
        lock.lock();
        try {
            Node existing = map.get(key);
            // if it exist
            if (existing != null) {
                existing.updateValue(value);
                // now
                addToHead(existing);
                putCount++;
                return;
            }
            // if not found
            Node node = new Node(key, value);
            map.put(key, node);

            addToHead(node);
            putCount++;
            // size check
            if (map.size() > capacity) {
                // if size above capacity remove least recent used from ddl ie remove tail
                Node leastUsed = removeTail();
                // why checking for !null i think it will always exist?
                if (leastUsed != null) {
                    // now remove it from map
                    map.remove(leastUsed.key);

                }
            }
        } finally {
            lock.unlock();
        }

    }

    public boolean contains(String key) {
        lock.lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            map.clear();
            head = tail = null;
            hits = 0;
            misses = 0;
            putCount = 0;
        } finally {
            lock.unlock();
        }
    }

    public void printState() {
        lock.lock();
        try {
            System.out.println("map size " + map.size());
            System.out.println("Keys in map: " + map.keySet());
            System.out.print("MRU -> ");
            for (Node node = head; node != null; node = node.next) {
                System.out.print("[" + node.key + ", " + node.getSize() + "] -> ");
            }
            System.out.println("LRU");

            System.out.println("Hits: " + hits);
            System.out.println("Misses: " + misses);
            System.out.println("Puts: " + putCount);
            System.out.println("-----------------------------");
        } finally {
            lock.unlock();
        }
    }

    // Removes the least recently used (LRU) node — the tail — and returns it.
    private Node removeTail() {
        if (tail == null)
            return null;

        Node oldTail = tail;
        removeNode(oldTail);
        return oldTail;
    }

    // Moves an existing node to the head, or inserts a new one there.
    // The node becomes the "most recently used".
    private void addToHead(Node node) {
        if (node == null)
            return;

        // If the node is already the head, nothing to do.
        if (node == head)
            return;

        // Only remove if it's already in the list (has prev or next links)
        if (node.prev != null || node.next != null) {
            removeNode(node);
        }

        // Insert node at the front.
        node.next = head; // current head becomes second
        node.prev = null;

        if (head != null) {
            head.prev = node;
        }

        head = node;

        // If the list was empty (tail == null), this new node is both head and tail.
        if (tail == null) {
            tail = node;
        }
    }

    // Removes a node from its current position in the doubly linked list.
    // Works for head, tail, or a middle node.
    private void removeNode(Node node) {
        if (node == null)
            return;

        // If the node has a previous node, link that previous node to the node after
        // this one.
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            // If prev == null, this node *was* the head.
            head = node.next;
        }

        // Same logic for next pointer.
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            // If next == null, this node *was* the tail.
            tail = node.prev;
        }

        // Fully detach node (optional but helps prevent bugs / memory leaks)
        node.prev = null;
        node.next = null;
    }

}
