import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.pow;

public class ChordNetwork {

    private int bits;
    ChordNode firstNode;

    private ArrayList<ChordNode> nodes = new ArrayList<>();

    private ChordNetwork(int bits, int id) {
        this.bits = bits;
        for (int i = 0; i < (int) pow(2, bits); i++) {
            nodes.add(null);
        }
        addNode(id);
        firstNode = get(id);
    }

    ChordNode findById(int id) {
        return firstNode.findById(id);
    }

    private void addNode(int id) {
        ChordNode newNode = new ChordNode(id);
        nodes.set(id, newNode);
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

        nodes.set(id, null);
        toRemove.updateOthers();
    }

    void stabilization() {

    }

    private void addNodes(ArrayList<Integer> idsToCreate) {
        idsToCreate.forEach(this::addNode);
    }

    ChordNode get(int id) {
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
        private ArrayList<fingerTableEntry> finger;
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
                    '}';
        }

        ChordNode(int id) {
            this.id = id;
            this.finger = new ArrayList<>(bits);
            for (int i = 1; i <= bits; i++) {
                this.finger.add(new fingerTableEntry(i, id));
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
            while (!(
                    (result.id >= result.successor().id && (result.id < id ^ id <= result.successor().id)) // переход через 0
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
                if (
                        (this.id >= finger.get(i).node.id && (this.id <= finger.get(i + 1).start ^ finger.get(i + 1).start < finger.get(i).node.id))
                                || (this.id < finger.get(i).node.id && this.id <= finger.get(i + 1).start && finger.get(i + 1).start < finger.get(i).node.id)) {
                    finger.get(i + 1).node = finger.get(i).node;
                } else {
                    finger.get(i + 1).node = randomNode.findSuccessor(finger.get(i + 1).start);
                }
            }
        }

        void updateOthers() {
            for (int i = 1; i <= bits; i++) {
                ChordNode p = findPredecessor((this.id - (int) pow(2, i - 1)) % (int) pow(2, bits));
                p.updateFingerTable(this.id, i-1);
            }
        }

        private void updateFingerTable(int id, int i) {
            if (
                    (this.id >= finger.get(i).node.id && (id < finger.get(i).node.id ^ this.id <= id))
                            || (this.id < finger.get(i).node.id && id < finger.get(i).node.id && this.id <= id)) {
                finger.get(i).node = get(id);
                predecessor.updateFingerTable(id, i);
            }
        }

        public void join(ChordNode chordNode) {
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

        public ChordNode findById(int id) {
            return findSuccessor(id);
        }

        class fingerTableEntry {
            int start;
            int intervalEnd;
            ChordNode node;

            fingerTableEntry(int i, int n) {
                this.start = (n + (int) (pow(2, i - 1))) % (int) pow(2, bits);
                this.intervalEnd = (n + (int) pow(2, i)) % (int) pow(2, bits);
            }

            @Override
            public String toString() {
                return "fingerTableEntry{" +
                        "start=" + start +
                        ", intervalEnd=" + intervalEnd +
                        ", node=" + node +
                        '}';
            }
        }
    }

    public static void main(String[] args) {
        int bits = 4;
        int[] ids = {6};
        int firstId = 3;
        ArrayList<Integer> e = new ArrayList<>(ids.length);
        for (int id : ids) {
            e.add(id);
        }
        Collections.sort(e);
        ChordNetwork net = new ChordNetwork(bits, firstId);
        net.addNodes(e);
//        net.removeNode(3);
    }
}
