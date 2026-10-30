package neat;

import data_structure.RandomHashSet;
import data_structure.RandomSelector;
import game.Bird;
import game.Game;
import genome.ConnectionGene;
import genome.Genome;
import genome.NodeGene;
import visual.Frame;

import javax.swing.*;
import java.util.HashMap;

public class Neat {
    public static final int MAX_NODES = (int) Math.pow(2, 20);
    public static double[] inputs = new double[5];
    private static int inputSize = 5;
    private static int outputSize = 1;
    private static int numberOfClients = 250;
    private HashMap<ConnectionGene, ConnectionGene> allConnections = new HashMap<>();
    private RandomHashSet<NodeGene> allNodes = new RandomHashSet<>();
    private RandomHashSet<Client> clients = new RandomHashSet<>();
    private RandomHashSet<Species> species = new RandomHashSet<>();
    private int maxClients;
    private double C1 = 1, C2 = 1, C3 = 1;
    private double SURVIVORS = 0.8;
    private double WEIGHT_SHIFT_STRENGTH = 0.3;
    private double CP = 4;
    private double WEIGHT_RANDOM_STRENGTH = 1;
    private double PROBABILITY_MUTATE_LINK = 0.2;
    private double PROBABILITY_MUTATE_NODE = 0.075;
    private double PROBABILITY_MUTATE_WEIGHT_SHIFT = 0.2;
    private double PROBABILITY_MUTATE_WEIGHT_RANDOM = 0.2;
    private double PROBABILITY_MUTATE_TOGGLE_LINK = 0;

    public Neat(int inputSize, int outputSize, int clients) {
        reset(inputSize, outputSize, clients);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game(numberOfClients));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 1; i++) {
            Neat neat = new Neat(inputSize, outputSize, numberOfClients);
            Frame frame = new Frame(neat.getClient(0).getGenome());

            for (int generation = 0; generation < 2000000; generation++) {
                try {
                    double previousBestScore = 0;
                    for (Client client : neat.clients.getData())
                        if (client.getScore() > previousBestScore) previousBestScore = client.getScore();
                    Game.updateLabels(generation, previousBestScore);
                    while (!Game.gameFinished) {
                        for (Client client : neat.clients.getData()) {
                            int index = neat.clients.getData().indexOf(client);
                            inputs[0] = Game.getBirdY(index);
                            inputs[1] = Game.getBirdVelocity(index);
                            inputs[2] = Math.abs(100 - Game.getPipeX());
                            inputs[3] = Math.abs(Game.getBirdY(index) - Game.getTopPipeY());
                            inputs[4] = Math.abs(Game.getBirdY(index) - Game.getBottomPipeY());
                            int output = (int) Math.round(client.calculate(inputs)[0]);
                            if (output == 1) Game.birds.get(index).space();
                            Game.updateLabels(generation, previousBestScore);
                        }
                    }

                    for (Client client : neat.clients.getData()) {
                        client.setScore(calculateScore(neat.clients.getData().indexOf(client)));
                    }
                    neat.evolve();
                    Game.gameFinished = false;
                    Game.reset();
                    double highestScore = 0;
                    for (Client client : neat.clients.getData())
                        if (client.getScore() > highestScore) highestScore = client.getScore();
                    for (Client client : neat.clients.getData())
                        if (client.getScore() == highestScore) frame.reset(client.getGenome());
                } catch (Exception ignored) {
                }
//                System.out.println(generation);
            }

            // Testing
//            System.out.println("Testing:");
//            int counter = 0;
//            for (int[] input : inputs) {
//                double[] outputArray = neat.getClient(0).calculate(input);
//                int output = (int) Math.round(outputArray[0]);
//                int expected = input[0] * input[1] + input[2];
//                System.out.println(Arrays.toString(input) + " => Predicted: " + output + ", Expected: " + expected + " Score: " + neat.getClient(0).getScore());
//                counter++;
//                if (counter == 3) break;
//            }

        }
    }

    public static double calculateScore(int index) {
        Bird bird = Game.birds.get(index);
        if (bird.score > 0) {
            double averageY = (Game.getNextTopPipeY() + Game.getNextBottomPipeY()) / 2d;
            double difference = Math.abs(bird.y - averageY);
            if (difference < 100) return bird.score + (1d / (1d + difference)) * 50;
            else return bird.score - (1d / (1d + difference)) * 75;
        } else {
            double averageY = (Game.getTopPipeY() + Game.getBottomPipeY()) / 2d;
            return (1d / (1d + Math.abs(bird.y - averageY) * 100)) * 50;
        }
    }

    public static ConnectionGene getConnection(ConnectionGene con) {
        ConnectionGene c = new ConnectionGene(con.getFrom(), con.getTo());
        c.setInnovationNumber(con.getInnovationNumber());
        c.setWeight(con.getWeight());
        c.setEnabled(c.isEnabled());
        return c;
    }

    public void printSpecies() {
        System.out.println("######################################");
        for (Species s : this.species.getData()) System.out.println(s + "   " + s.getScore() + "   " + s.size());
    }

    public void evolve() {
        genSpecies();
        kill();
        removeExtinctSpecies();
        reproduce();
        mutate();
        for (Client c : clients.getData()) c.generateCalculator();
    }

    public void setReplaceIndex(NodeGene node1, NodeGene node2, int index) {
        allConnections.get(new ConnectionGene(node1, node2)).setReplaceIndex(index);
    }

    public int getReplaceIndex(NodeGene node1, NodeGene node2) {
        ConnectionGene con = new ConnectionGene(node1, node2);
        ConnectionGene data = allConnections.get(con);
        if (data == null) return 0;
        return data.getReplaceIndex();
    }

    public void mutate() {
        for (Client c : clients.getData()) c.mutate();
    }

    private void reproduce() {
        RandomSelector<Species> selector = new RandomSelector<>();
        for (Species s : species.getData()) selector.add(s, s.getScore());
        for (Client c : clients.getData()) {
            if (c.getSpecies() == null) {
                Species s = selector.random();
                c.setGenome(s.breed());
                s.forcePut(c);
            }
        }
    }

    private void removeExtinctSpecies() {
        for (int i = species.size() - 1; i >= 0; i--) {
            if (species.get(i).size() <= 1) {
                species.get(i).goExtinct();
                species.remove(i);
            }
        }
    }

    private void genSpecies() {
        for (Species s : species.getData()) s.reset();
        for (Client c : clients.getData()) {
            if (c.getSpecies() != null) continue;

            boolean found = false;
            for (Species s : species.getData()) {
                if (s.put(c)) {
                    found = true;
                    break;
                }
            }
            if (!found) species.add(new Species(c));
        }

        for (Species s : species.getData()) s.evaluateScore();
    }

    private void kill() {
        for (Species s : species.getData()) s.kill(1 - SURVIVORS);
    }

    public Genome emptyGenome() {
        Genome g = new Genome(this);
        for (int i = 0; i < inputSize + outputSize; i++) {
            g.getNodes().add(getNode(i + 1));
        }
        return g;
    }

    public void reset(int inputSize, int outputSize, int clients) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        maxClients = clients;

        allConnections.clear();
        allNodes.clear();
        this.clients.clear();

        for (int i = 0; i < inputSize; i++) {
            NodeGene n = getNode();
            n.setX(0.1);
            n.setY((i + 1) / (double) (inputSize + 1));
        }

        for (int i = 0; i < outputSize; i++) {
            NodeGene n = getNode();
            n.setX(0.9);
            n.setY((i + 1) / (double) (inputSize + 1));
        }

        for (int i = 0; i < maxClients; i++) {
            Client c = new Client();
            c.setGenome(emptyGenome());
            c.generateCalculator();
            this.clients.add(c);
        }
    }

    public Client getClient(int index) {
        return clients.get(index);
    }

    public ConnectionGene getConnection(NodeGene node1, NodeGene node2) {
        ConnectionGene connectionGene = new ConnectionGene(node1, node2);

        if (allConnections.containsKey(connectionGene))
            connectionGene.setInnovationNumber(allConnections.get(connectionGene).getInnovationNumber());
        else {
            connectionGene.setInnovationNumber(allConnections.size() + 1);
            allConnections.put(connectionGene, connectionGene);
        }

        return connectionGene;
    }

    public NodeGene getNode() {
        NodeGene n = new NodeGene(allNodes.size() + 1);
        allNodes.add(n);
        return n;
    }

    public NodeGene getNode(int id) {
        if (id <= allNodes.size()) return allNodes.get(id - 1);
        return getNode();
    }

    public double getC1() {
        return C1;
    }

    public double getC2() {
        return C2;
    }

    public double getC3() {
        return C3;
    }

    public double getWEIGHT_SHIFT_STRENGTH() {
        return WEIGHT_SHIFT_STRENGTH;
    }

    public double getWEIGHT_RANDOM_STRENGTH() {
        return WEIGHT_RANDOM_STRENGTH;
    }

    public double getPROBABILITY_MUTATE_LINK() {
        return PROBABILITY_MUTATE_LINK;
    }

    public double getPROBABILITY_MUTATE_NODE() {
        return PROBABILITY_MUTATE_NODE;
    }

    public double getPROBABILITY_MUTATE_WEIGHT_SHIFT() {
        return PROBABILITY_MUTATE_WEIGHT_SHIFT;
    }

    public double getPROBABILITY_MUTATE_WEIGHT_RANDOM() {
        return PROBABILITY_MUTATE_WEIGHT_RANDOM;
    }

    public double getPROBABILITY_MUTATE_TOGGLE_LINK() {
        return PROBABILITY_MUTATE_TOGGLE_LINK;
    }

    public double getCP() {
        return CP;
    }

    public void setCP(double CP) {
        this.CP = CP;
    }
}
