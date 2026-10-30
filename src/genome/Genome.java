package genome;

import data_structure.RandomHashSet;
import neat.Neat;

public class Genome {
    private RandomHashSet<ConnectionGene> connections = new RandomHashSet<>();
    private RandomHashSet<NodeGene> nodes = new RandomHashSet<>();
    private Neat neat;

    public Genome(Neat neat) {
        this.neat = neat;
    }

    /**
     * creates a new genome
     * g1 should have the higher score
     * -take all the genes of a
     * -if there is a genome in a that is also in b, choose randomly
     * do not take disjoint genes of b
     * take excess genes of a if they exist
     *
     * @param g1
     * @param g2
     * @return
     */
    public static Genome crossOver(Genome g1, Genome g2) {
        Neat neat = g1.getNeat();
        Genome genome = neat.emptyGenome();

        int indexG1 = 0;
        int indexG2 = 0;

        while (indexG1 < g1.getConnections().size() && indexG2 < g2.getConnections().size()) {
            ConnectionGene gene1 = g1.getConnections().get(indexG1);
            ConnectionGene gene2 = g2.getConnections().get(indexG2);

            int in1 = gene1.getInnovationNumber();
            int in2 = gene2.getInnovationNumber();

            if (in1 == in2) { //matching gene

                if (Math.random() > 0.5) genome.getConnections().add(neat.getConnection(gene1));
                else genome.getConnections().add(neat.getConnection(gene2));

                indexG1++;
                indexG2++;
            } else if (in1 > in2) {//disjoint gene of b
//                genome.getConnections().add(neat.getConnection(gene2)); - unnecessary
                indexG2++;
            } else { //disjoint gene of a
                genome.getConnections().add(neat.getConnection(gene1));
                indexG1++;
            }
        }

        while (indexG1 < g1.getConnections().size()) {
            genome.getConnections().add(neat.getConnection(g1.getConnections().get(indexG1)));
            indexG1++;
        }

        for (ConnectionGene c : genome.getConnections().getData()) {
            genome.getNodes().add(c.getFrom());
            genome.getNodes().add(c.getTo());
        }

        return genome;
    }

    /**
     * calculated the distance between this genome g1 and a second genome g2
     * -g1 must have the highest innovation number
     *
     * @param g2
     * @return
     */
    public double distance(Genome g2) {
        Genome g1 = this;

        int highestInnovationGene1 = 0;
        if (g1.getConnections().size() != 0)
            highestInnovationGene1 = g1.getConnections().get(g1.getConnections().size() - 1).getInnovationNumber();

        int highestInnovationGene2 = 0;
        if (g2.getConnections().size() != 0)
            highestInnovationGene2 = g2.getConnections().get(g2.getConnections().size() - 1).getInnovationNumber();

        if (highestInnovationGene1 < highestInnovationGene2) {
            Genome g = g1;
            g1 = g2;
            g2 = g;
        }

        int indexG1 = 0;
        int indexG2 = 0;

        int disjoint = 0;
        int excess;
        double weightDiff = 0;
        int similar = 0;

        while (indexG1 < g1.getConnections().size() && indexG2 < g2.getConnections().size()) {
            ConnectionGene gene1 = g1.getConnections().get(indexG1);
            ConnectionGene gene2 = g2.getConnections().get(indexG2);

            int in1 = gene1.getInnovationNumber();
            int in2 = gene2.getInnovationNumber();

            if (in1 == in2) { //matching gene
                similar++;
                weightDiff += Math.abs(gene1.getWeight() - gene2.getWeight());
                indexG1++;
                indexG2++;
            } else if (in1 > in2) { //disjoint gene of b
                indexG2++;
                disjoint++;
            } else { //disjoint gene of a
                indexG1++;
                disjoint++;
            }
        }

        weightDiff /= Math.max(1, similar);
        excess = g1.getConnections().size() - indexG1;

        double N = Math.max(g1.getConnections().size(), g2.getConnections().size());
        if (N < 20) N = 1;

        return neat.getC1() * disjoint / N + neat.getC2() * excess / N + neat.getC3() * weightDiff;
    }

    public void mutate() {
        if (neat.getPROBABILITY_MUTATE_LINK() > Math.random()) {
            mutateLink();
        }
        if (neat.getPROBABILITY_MUTATE_NODE() > Math.random()) {
            mutateNode();
        }
        if (neat.getPROBABILITY_MUTATE_WEIGHT_SHIFT() > Math.random()) {
            mutateWeightShift();
        }
        if (neat.getPROBABILITY_MUTATE_WEIGHT_RANDOM() > Math.random()) {
            mutateWeightRandom();
        }
        if (neat.getPROBABILITY_MUTATE_TOGGLE_LINK() > Math.random()) {
            mutateLinkToggle();
        }
    }

    public void mutateLink() {
        for (int i = 0; i < 100; i++) {
            NodeGene a = nodes.randomElement();
            NodeGene b = nodes.randomElement();

            if (a.getX() == b.getX()) continue;

            ConnectionGene con;
            if (a.getX() < b.getX()) con = new ConnectionGene(a, b);
            else con = new ConnectionGene(b, a);

            if (connections.contains(con)) continue;

            con = neat.getConnection(con.getFrom(), con.getTo());
            con.setWeight((Math.random() * 2 - 1) * neat.getWEIGHT_RANDOM_STRENGTH());

            connections.addSorted(con);
            return;
        }
    }

    public void mutateNode() {
        ConnectionGene con = connections.randomElement();
        if (con == null) return;

        NodeGene from = con.getFrom();
        NodeGene to = con.getTo();

        int replaceIndex = neat.getReplaceIndex(from, to);
        NodeGene middle;

        if (replaceIndex == 0) {
            middle = neat.getNode();
            middle.setX((from.getX() + to.getX()) / 2);
            middle.setY((from.getY() + to.getY()) / 2 + Math.random() * 0.1 - 0.05);
            neat.setReplaceIndex(from, to, middle.getInnovationNumber());
        } else middle = neat.getNode(replaceIndex);

        ConnectionGene con1 = neat.getConnection(from, middle);
        ConnectionGene con2 = neat.getConnection(middle, to);

        con1.setWeight(1);
        con2.setWeight(con.getWeight());
        con2.setEnabled(con.isEnabled());

        connections.remove(con);
        connections.add(con1);
        connections.add(con2);

        nodes.add(middle);

    }

    public void mutateWeightShift() {
        ConnectionGene con = connections.randomElement();
        if (con != null) con.setWeight(con.getWeight() + (Math.random() * 2 - 1) * neat.getWEIGHT_SHIFT_STRENGTH());
    }

    public void mutateWeightRandom() {
        ConnectionGene con = connections.randomElement();
        if (con != null) con.setWeight((Math.random() * 2 - 1) * neat.getWEIGHT_RANDOM_STRENGTH());
    }

    public void mutateLinkToggle() {
        ConnectionGene con = connections.randomElement();
        if (con != null) con.setEnabled(!con.isEnabled());
    }

    public RandomHashSet<ConnectionGene> getConnections() {
        return connections;
    }

    public RandomHashSet<NodeGene> getNodes() {
        return nodes;
    }

    public Neat getNeat() {
        return neat;
    }
}
