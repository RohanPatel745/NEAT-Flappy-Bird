package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Game {
    public static final Object lock = new Object();
    public static ArrayList<Pipe> pipes;
    public static ArrayList<Bird> birds;
    public static boolean gameFinished = false;
    public static BufferedImage backgroundImage = new BufferedImage(600, 800, BufferedImage.TYPE_INT_RGB);
    public static JFrame frame;
    public static JPanel panel;
    public static JLabel background;
    public static JLabel score;
    public static JLabel generation;
    public static JLabel bestPreviousScore;
    public static Thread mainThread;
    public static int clients;

    public Game(int clients) {
        this.clients = clients;

        birds = new ArrayList<>();
        for (int i = 0; i < clients; i++) birds.add(new Bird());

        pipes = new ArrayList<>();
        pipes.add(new Pipe());

        frame = new JFrame("Flappy Bird");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(600, 800);
        frame.setLayout(null);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) birds.get(0).space();
            }
        });

        panel = new JPanel();
        panel.setBackground(new Color(0, 180, 255));
        panel.setBounds(0, 0, 600, 800);
        frame.add(panel);

//        score = new JLabel(String.valueOf(bird.score));
//        score.setBounds(300, 0, 100, 100);
//        score.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
//        score.setForeground(Color.white);
//        panel.add(score);

        generation = new JLabel();
        generation.setBounds(0, 0, 200, 100);
        generation.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        generation.setForeground(Color.white);
//        panel.add(generation);

        bestPreviousScore = new JLabel();
        bestPreviousScore.setBounds(250, 0, 100, 100);
        bestPreviousScore.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
        bestPreviousScore.setForeground(Color.white);
        panel.add(bestPreviousScore);

        background = new JLabel(new ImageIcon(backgroundImage));
        background.setBounds(0, 0, 600, 800);
        panel.add(background);

        mainThread = new Thread(() -> {
            while (true) run();
        });
        mainThread.start();
    }

    public static double getBirdY(int index) {
        return birds.get(index).y;
    }

    public static double getBirdVelocity(int index) {
        return birds.get(index).velocity;
    }

    public static double getPipeX() {
        if (pipes.get(0).x < 100 && pipes.size() > 1) return pipes.get(1).x;
        return pipes.get(0).x;
    }

    public static double getTopPipeY() {
        return pipes.get(0).topPipeY;
    }

    public static double getBottomPipeY() {
        return pipes.get(0).topPipeY + 150;
    }

    public static double getNextTopPipeY() {
        return pipes.get(1).topPipeY;
    }

    public static double getNextBottomPipeY() {
        return pipes.get(1).topPipeY + 150;
    }

    public static void updateLabels(int generationNumber, double score) {
        generation.setText("Generation: " + generationNumber);
//        bestPreviousScore.setText("Previous Score: " + Math.round(score * 1000) / 1000d);
        double highestScore = 0;
        for (Bird bird : birds) {
            if (bird.score > highestScore) highestScore = bird.score;
        }
        bestPreviousScore.setText("Score: " + highestScore);
    }

    public static void reset() {
        birds.clear();
        for (int i = 0; i < clients; i++) birds.add(new Bird());

        pipes.clear();
        pipes.add(new Pipe());

        synchronized (lock) {
            lock.notify();
        }
    }

    public void run() {
        if (pipes.get(0).isDead) pipes.remove(0);

        for (Bird bird : birds) bird.run();

        for (Pipe pipe : pipes) pipe.run();
        if (pipes.get(0).x < 500 && pipes.get(0).shouldCreatePipe) {
            pipes.add(new Pipe());
            pipes.get(0).shouldCreatePipe = false;
        }
        if (pipes.get(0).x + 100 < 100 && pipes.get(0).shouldIncreaseScore) {
            for (Bird bird : birds) {
                if (!bird.isDead) {
                    bird.score++;
                    pipes.get(0).shouldIncreaseScore = false;
//                    score.setText(String.valueOf(bird.score));
                }
            }
        }

        if (!gameFinished) updateBackground();
        else {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {

                }
            }
        }

        int deadBirds = 0;
        for (Bird bird : birds) if (bird.isDead) deadBirds++;
        if (deadBirds == birds.size()) gameFinished = true;

        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateBackground() {
        Graphics2D graphics = backgroundImage.createGraphics();

        graphics.setColor(new Color(0, 180, 255));
        graphics.fillRect(0, 0, 600, 800);

        for (Pipe pipe : pipes) {
            graphics.setColor(new Color(0, 255, 100));
            graphics.fillRect((int) pipe.x, 0, 100, (int) pipe.topPipeY);
            graphics.fillRect((int) pipe.x, (int) (pipe.topPipeY + pipe.verticalDistanceBetweenPipes), 100, 800);
        }


        for (Bird bird : birds) {
            if (!bird.isDead) {
                graphics.setColor(new Color(255, 255, 0));
                graphics.fillArc(100, (int) bird.y, 30, 30, 360, 360);
            }

            for (int x = 100; x < 100 + 30; x++) {
                for (int y = (int) bird.y; y < bird.y + 30; y++) {
                    if (backgroundImage.getRGB(x, y) == new Color(0, 255, 100).getRGB()) {
                        bird.isDead = true;
//                        graphics.setColor(Color.red);
//                        graphics.fillArc(100, (int) bird.y, 30, 30, 360, 360);
                        background.setIcon(new ImageIcon(backgroundImage));
                        break;
                    }
                }
            }
        }

        background.setIcon(new ImageIcon(backgroundImage));
    }
}
