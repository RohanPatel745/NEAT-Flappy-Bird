package visual;

import genome.Genome;

import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {

    private Panel panel;
    private Genome genome;

    public Frame(Genome genome) {
        this();
        setGenome(genome);
        this.repaint();
    }

    public Frame() throws HeadlessException {
        this.setDefaultCloseOperation(3);

        this.setTitle("NEAT");
        this.setMinimumSize(new Dimension(1000, 700));
        this.setPreferredSize(new Dimension(1000, 700));

        this.setLayout(new BorderLayout());


        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        try {
            UIManager.setLookAndFeel(looks[3].getClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }


        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(1000, 100));
        menu.setLayout(new GridLayout(1, 6));

//        JButton buttonB = new JButton("random weight");
//        buttonB.addActionListener(e -> {
//            genome.mutateWeightRandom();
//            repaint();
//        });
//        menu.add(buttonB);
//
//        JButton buttonZ = new JButton("weight shift");
//        buttonZ.addActionListener(e -> {
//            genome.mutateWeightShift();
//            repaint();
//        });
//        menu.add(buttonZ);
//
//        JButton buttonC = new JButton("Link mutate");
//        buttonC.addActionListener(e -> {
//            genome.mutateLink();
//            repaint();
//        });
//        menu.add(buttonC);
//
//        JButton buttonD = new JButton("Node mutate");
//        buttonD.addActionListener(e -> {
//            genome.mutateNode();
//            repaint();
//        });
//        menu.add(buttonD);
//
//
//        JButton buttonE = new JButton("on/off");
//        buttonE.addActionListener(e -> {
//            genome.mutateLinkToggle();
//            repaint();
//        });
//        menu.add(buttonE);
//
//        JButton buttonF = new JButton("Mutate");
//        buttonF.addActionListener(e -> {
//            genome.mutate();
//            repaint();
//        });
//        menu.add(buttonF);
//
//        JButton buttonG = new JButton("Calculate");
//        buttonG.addActionListener(e -> {
//            Calculator c = new Calculator(genome);
////            System.out.println(Arrays.toString(c.calculate(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)));
//            repaint();
//        });
//        menu.add(buttonG);


        this.add(menu, BorderLayout.NORTH);

        this.panel = new Panel();
        this.add(panel, BorderLayout.CENTER);

        this.setVisible(true);
    }

    public void reset(Genome genome) {
        setGenome(genome);
        this.repaint();
    }

    public void setGenome(Genome genome) {
        panel.setGenome(genome);
        this.genome = genome;
    }

}
