import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

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

    private void removeNode(int id) {
        ChordNode toRemove = get(id);
        nodes.remove(id);
        if (firstNode.equals(toRemove)) {
            if (nodes.keySet().size() == 0) {
                firstNode = null;
            } else {
                firstNode = nodes.get(new Random().nextInt(nodes.keySet().size()));
            }
        }
        ChordNode pred = toRemove.predecessor;
        ChordNode succ = toRemove.successor();
        pred.setSuccessor(toRemove.successor());
        succ.predecessor = toRemove.predecessor;
        toRemove.leaveUpdateOthers();
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
        StringBuffer res = new StringBuffer();
        for (Integer i: nodes.keySet()
             ) {
            res.append(nodes.get(i).toString() + '\n');
        }

        return res.toString();
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
            for (int i = bits - 1; i >= 0; i--) {
                int start = this.id;
                int finish = id;
                if ((start >= finish && (start < finger.get(i).node.id ^ finger.get(i).node.id < finish))
                        || start < finish && finger.get(i).node.id > start && finger.get(i).node.id < finish) {
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

        void joinUpdateOthers() {
            for (int i = 1; i <= bits; i++) {
                int index = this.id - (int) pow(2, i - 1);
                index = (index >= 0) ? index : index + (int) pow(2, bits);
                ChordNode p = (get(index) != null) ? get(index) : findPredecessor(index); // если существует узел с id == index, то использовать его
                p.joinUpdateFingerTable(this.id, i - 1);
            }
        }

        void leaveUpdateOthers() {
            for (int i = 1; i <= bits; i++) {
                int index = this.id - (int) pow(2, i - 1);
                index = (index >= 0) ? index : index + (int) pow(2, bits);
                ChordNode p = (get(index) != null) ? get(index) : findPredecessor(index); // если существует узел с id == index, то использовать его
                p.leaveUpdateFingerTable(this.id, i - 1);
            }
        }

        private void joinUpdateFingerTable(int id, int i) { // if id is i'th finger of this node
            FingerTableEntry entry = finger.get(i);
            int start = (this.id == id) ? this.id : entry.start;
            int finish = entry.node.id;
            if ((start > finish && (id < finish ^ start <= id))
                    || (start <= finish && id < finish && start <= id)) {
                if (this.id != id) {
                    entry.node = get(id);
                }
                predecessor.joinUpdateFingerTable(id, i);
            }
        }

        private void leaveUpdateFingerTable(int id, int i) {
            FingerTableEntry entry = finger.get(i);
            int start = (this.id == id) ? this.id : entry.start;
            int finish = entry.node.id;
            if ((start > finish && (id < finish ^ start <= id))
                    || (start <= finish && id < finish && start <= id)) {
                if (this.id != id) {
                    entry.node = findSuccessor(id);
                }
                predecessor.leaveUpdateFingerTable(id, i);
            }
        }


        void join(ChordNode chordNode) {
            if (chordNode != null) {
                initFingerTable(chordNode);
                joinUpdateOthers();
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
        int bits = 10;
        int[] ids = {3, 6, 1};
        int firstId = 0;
        ArrayList<Integer> e = new ArrayList<>(ids.length);
        for (int id : ids) {
            e.add(id);
        }
        ChordNetwork net = new ChordNetwork(bits, firstId);
        net.addNodes(e);

        net.addNode(999);
        net.addNode(700);
        net.addNode(300);
        System.out.println(net.findById(2));
        net.removeNode(1);
        System.out.println(net);
    }
}
