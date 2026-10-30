package game;

public class Pipe {
    public static int numberOfPipes = 0;
    public double x;
    public double topPipeY;
    public double verticalDistanceBetweenPipes = 150;
    public boolean isDead = false;
    public boolean shouldCreatePipe = true;
    public boolean shouldIncreaseScore = true;

    public Pipe() {
        createPipe();
    }

    public void createPipe() {
        x = 900;
        topPipeY = Math.random() * 200 + 200;
        numberOfPipes++;
    }

    public void run() {
        x -= 4;
        if (x < -100) {
            numberOfPipes--;
            isDead = true;
        }
    }
}
