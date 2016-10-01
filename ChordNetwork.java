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
        return null;
    }

    private void addNode(int id) {
        ChordNode newNode = new ChordNode(id);
        nodes.set(id, newNode);
        newNode.join(firstNode);
    }

    void removeNode(int id) {

    }

    void stabilization() {

    }

    private void addNodes(ArrayList<Integer> idsToCreate) {
        idsToCreate.forEach(this::addNode);
    }

    ChordNode get(int id) {
        return nodes.get(id);
    }

    private class ChordNode {
        private int id;
        private ArrayList<fingerTableEntry> finger;
        private ChordNode successor;
        private ChordNode predecessor;

        @Override
        public String toString() {
            return "ChordNode{" +
                    "id=" + id +
                    ", successor=" + successor +
                    ", predecessor=" + predecessor +
                    '}';
        }

        ChordNode(int id) {
            this.id = id;
            this.finger = new ArrayList<>(bits);
            for (int i = 1; i <= bits; i++) {
                this.finger.add(new fingerTableEntry(i, id, this));
            }
            try {
                successor = finger.get(0).node;
            } catch (Exception e) {
                successor = this;
            }
        }

        ChordNode findSuccessor(int id) {
            return findPredecessor(id).successor;
        }

        ChordNode closestPrecedingFinger(int id) {
            for (int i = bits - 1; i > 0; i--) {
                if (finger.get(i).node.id < id && finger.get(i).node.id > this.id) {
                    return finger.get(i).node;
                }
            }
            return this;
        }

        private ChordNode findPredecessor(int id) {
            ChordNode result = this;
            while (!(result.id < id && result.id <= result.successor.id)) {
                result = result.closestPrecedingFinger(id);
            }
            return result;
        }

        void initFingerTable(ChordNode randomNode) {
            successor = randomNode.findSuccessor(finger.get(0).start);
            finger.get(0).node = successor;
            predecessor = successor.predecessor;
            successor.predecessor = this;
            for (int i = 0; i < bits - 2; i++) {
                if (finger.get(i + 1).start <= this.id && finger.get(i + 1).start < finger.get(i).node.id) {
                    finger.get(i + 1).node = finger.get(i).node;
                } else {
                    finger.get(i + 1).node = randomNode.findSuccessor(finger.get(i + 1).start);
                }
            }
        }

        void updateOthers() {
            for (int i = 1; i <= bits; i++) {
                ChordNode p = findPredecessor(this.id - (int) pow(2, i - 1));
                p.updateFingerTable(this.id, i - 1);
            }
        }

        private void updateFingerTable(int id, int i) {
            if (id < finger.get(i).node.id && this.id <= id) {
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

        class fingerTableEntry {
            int start;
            int intervalEnd;
            ChordNode node;

            fingerTableEntry(int i, int n, ChordNode node) {
                this.start = (n + (int) (pow(2, i - 1))) % (int) pow(2, bits);
                this.intervalEnd = (n + (int) pow(2, i)) % (int) pow(2, bits);
                this.node = node;
            }
        }
    }

    public static void main(String[] args) {
        int bits = 5;
        int[] ids = {15, 3 };
        int firstBit = 18;
        ArrayList<Integer> e = new ArrayList<>(ids.length);
        for (int id : ids) {
            e.add(id);
        }
        Collections.sort(e);
        ChordNetwork net = new ChordNetwork(bits, firstBit);
        net.addNodes(e);
        System.out.println("done");
    }
}
