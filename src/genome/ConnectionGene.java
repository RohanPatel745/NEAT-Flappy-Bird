package genome;

import neat.Neat;

public class ConnectionGene extends Gene {
    private NodeGene from;
    private NodeGene to;
    private int replaceIndex;
    private double weight;
    private boolean enabled = true;

    public ConnectionGene(NodeGene from, NodeGene to) {
        this.from = from;
        this.to = to;
    }

    public NodeGene getFrom() {
        return from;
    }

    public void setFrom(NodeGene from) {
        this.from = from;
    }

    public NodeGene getTo() {
        return to;
    }

    public void setTo(NodeGene to) {
        this.to = to;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ConnectionGene c)) return false;
        return (from.equals(c.from) && to.equals(c.to));
    }

    public int hashCode() {
        return from.getInnovationNumber() * Neat.MAX_NODES + to.getInnovationNumber();
    }

    public int getReplaceIndex() {
        return replaceIndex;
    }

    public void setReplaceIndex(int replaceIndex) {
        this.replaceIndex = replaceIndex;
    }
}
