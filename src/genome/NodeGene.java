package genome;

public class NodeGene extends Gene{
    private double x, y;

    public NodeGene(int innovationNumber) {
        super(innovationNumber);
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean equals(Object o) {
        if (!(o instanceof NodeGene)) return false;
        return innovationNumber == ((NodeGene) o).getInnovationNumber();
    }

    public int hashCode() {
        return innovationNumber;
    }
}
