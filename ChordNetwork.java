import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import static java.lang.Math.pow;

public class ChordNetwork {

    private int bits;
    private ChordNode firstNode;

    private Map<Integer, ChordNode> nodes = new Hashtable<>();

    private ChordNetwork(int bits, int id) {
        this.bits = bits;
        addNode(id);
        firstNode = get(id);
    }

    ChordNode findById(int id) {
        return firstNode.findById(id);
    }

    private void addNode(int id) {
        ChordNode newNode = new ChordNode(id);
        nodes.put(id, newNode);
        if (firstNode != null && firstNode.predecessor.id == firstNode.id && firstNode.successor().id == firstNode.id) {
            for (int i = 0; i < bits; i++) {
                newNode.finger.get(i).node = firstNode;
                firstNode.finger.get(i).node = newNode;
            }
            firstNode.predecessor = newNode;
            newNode.predecessor = firstNode;
        }
        newNode.join(firstNode);
    }

    void removeNode(int id) {
        ChordNode toRemove = get(id);
        toRemove.predecessor.setSuccessor(toRemove.successor());
        toRemove.successor().predecessor = toRemove.predecessor;

        nodes.put(id, null);
        toRemove.updateOthers();
    }

    void stabilization() {

    }

    private void addNodes(ArrayList<Integer> idsToCreate) {
        idsToCreate.forEach(this::addNode);
    }

    private ChordNode get(int id) {
        return nodes.get(id);
    }

    @Override
    public String toString() {
        return "ChordNetwork{" +
                "nodes=" + nodes +
                '}';
    }

    private class ChordNode {
        private int id;
        private ArrayList<FingerTableEntry> finger;
        private ChordNode predecessor;

        private ChordNode successor() {
            return finger.get(0).node;
        }

        private void setSuccessor(ChordNode newNode) {
            finger.get(0).node = newNode;
        }

        @Override
        public String toString() {
            return "ChordNode{" +
                    "id=" + id +
                    ", finger=" + finger +
                    '}';
        }

        ChordNode(int id) {
            this.id = id;
            this.finger = new ArrayList<>(bits);
            for (int i = 1; i <= bits; i++) {
                this.finger.add(new FingerTableEntry(i, id));
            }
        }

        ChordNode findSuccessor(int id) {
            return findPredecessor(id).successor();
        }

        ChordNode closestPrecedingFinger(int id) {
            for (int i = bits - 1; i > 0; i--) {
                if ((this.id >= id && (this.id < finger.get(i).node.id ^ finger.get(i).node.id < id))
                        || this.id < id && finger.get(i).node.id > this.id && finger.get(i).node.id < id) {
                    return finger.get(i).node;
                }
            }
            return this;
        }

        private ChordNode findPredecessor(int id) {
            ChordNode result = this;
            while (!((result.id >= result.successor().id && (result.id < id ^ id <= result.successor().id)) // переход через 0
                    || (result.id < result.successor().id && result.id < id && id <= result.successor().id) // без перехода
            )) {
                result = result.closestPrecedingFinger(id);
            }
            return result;
        }

        void initFingerTable(ChordNode randomNode) {
            finger.get(0).node = randomNode.findSuccessor(finger.get(0).start);
            predecessor = (successor().predecessor.id == this.id) ? successor() : successor().predecessor;
            successor().predecessor = this;
            for (int i = 0; i < bits - 1; i++) {
                if ((this.id >= finger.get(i).node.id && (this.id <= finger.get(i + 1).start ^ finger.get(i + 1).start < finger.get(i).node.id))
                        || (this.id < finger.get(i).node.id && this.id <= finger.get(i + 1).start && finger.get(i + 1).start < finger.get(i).node.id)) {
                    finger.get(i + 1).node = finger.get(i).node;
                } else {
                    finger.get(i + 1).node = randomNode.findSuccessor(finger.get(i + 1).start);
                }
            }
        }

        void updateOthers() {
            for (int i = 1; i <= bits; i++) {
                int index = this.id - (int) pow(2, i - 1);
                index = (index >= 0) ? index : index + (int) pow(2, bits);
                ChordNode p = (get(index) != null) ? get(index) : findPredecessor(index); // если существует узел с id == index, то использовать его
                p.updateFingerTable(this.id, i - 1);
            }
        }

        private void updateFingerTable(int id, int i) { // if id is i'th finger of this node
            FingerTableEntry entry = finger.get(i);
            int start = (this.id == id) ? this.id : entry.start;
            int finish = entry.node.id;
            if ((start > finish && (id < finish ^ start <= id))
                    || (start <= finish && id < finish && start <= id)) {
                if (this.id != id) {
                    entry.node = get(id);
                }
                    predecessor.updateFingerTable(id, i);
            }

        }

        void join(ChordNode chordNode) {
            if (chordNode != null) {
                initFingerTable(chordNode);
                updateOthers();
            } else {
                for (int i = 0; i < bits; i++) {
                    finger.get(i).node = this;
                }
                predecessor = this;
            }
        }

        ChordNode findById(int id) {
            return findSuccessor(id);
        }

        class FingerTableEntry {
            int start;
            int end;
            ChordNode node;

            FingerTableEntry(int i, int n) {
                this.start = (n + (int) (pow(2, i - 1))) % (int) pow(2, bits);
                this.end = (n + (int) pow(2, i)) % (int) pow(2, bits);
            }

            @Override
            public String toString() {
                return "FingerTableEntry{" +
                        "start=" + start +
                        ", end=" + end +
                        ", node=" + node.id +
                        '}';
            }
        }
    }

    public static void main(String[] args) {
        int bits = 3;
        int[] ids = {3, 6, 1};
        int firstId = 0;
        ArrayList<Integer> e = new ArrayList<>(ids.length);
        for (int id : ids) {
            e.add(id);
        }
//        Collections.sort(e);
        ChordNetwork net = new ChordNetwork(bits, firstId);
        net.addNodes(e);
        System.out.println(net);
//        net.removeNode(3);
    }
}
