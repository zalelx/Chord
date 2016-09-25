import java.util.ArrayList;
import java.util.Collections;

public class ChordNetwork {

    private final int bits;

    private ArrayList<ChordNode> nodes;

    ChordNode findById(int id) {
        return null;
    }

    void addNode(ChordNode newNode, int id) {

    }

    void removeNode(int id) {

    }


    void stabilization() {

    }

    int getNextNode(int id) {
        int currentId = id + 1;
        while (true) {
            try {
                if (nodes.get(currentId) != null) {
                    break;
                }
            } catch (IndexOutOfBoundsException ignored) {
                currentId = (currentId % (int) Math.pow(2, bits)) - 1;
            }
            currentId++;
        }
        return currentId;
    }

    private ChordNetwork(int mBits, ArrayList idsToCreate) {
        // todo добавить проверку на  соответствие битов и количества id
        this.nodes = new ArrayList<>();
        this.bits = mBits;
        for (int i = 0; i < (int) Math.pow(2, mBits); i++) {
            if (idsToCreate.indexOf(i) == -1) {
                nodes.add(null);
            } else {
                nodes.add(new ChordNode(i));
            }
        }
        for (int i = 0; i < idsToCreate.size(); i++) {
            nodes.get((Integer) idsToCreate.get(i)).initFingerTable();
        }
    }

    private class ChordNode {
        private ArrayList<fingerTableEntry> fingerTable;
        private int id;
        private int successor;
        private int predcessor;

        ChordNode(int id) {
            this.id = id;
            this.fingerTable = new ArrayList<>(bits);
        }

        void initFingerTable() {
            for (int i = 1; i <= bits; i++) {
                this.fingerTable.add(new fingerTableEntry(i, id));
            }
            successor = fingerTable.get(0).start;

        }


        class fingerTableEntry {
            int start;
            int intervalEnd;
            int node;

            fingerTableEntry(int i, int n) {
                this.start = (n + (int) (Math.pow(2, i - 1))) % (int) Math.pow(2, bits);
                this.intervalEnd = (n + (int) Math.pow(2, i)) % (int) Math.pow(2, bits);
                this.node = getNextNode(this.start);
            }
        }

    }

    public static void main(String[] args) {
        int bits = 3;
        int[] ids = {5, 3, 6, 7};
        ArrayList<Integer> e = new ArrayList<>(ids.length);
        for (int id : ids) {
            e.add(id);
        }
        Collections.sort(e);
        ChordNetwork net = new ChordNetwork(bits, e);
        System.out.println("done");
    }
}
