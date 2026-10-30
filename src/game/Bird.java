package game;

public class Bird {
    public double y;
    public double velocity;
    public int score;
    public boolean isDead = false;

    public Bird() {
        y = 400;
        velocity = 0;
    }

    public void space() {
        velocity = 7;
    }

    public void run() {
        velocity -= 0.5;
        if ((y < 670 || velocity > 0) && (y > 10 || velocity < 0) && !isDead) y -= velocity;
    }
}
