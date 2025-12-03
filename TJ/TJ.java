package TJ;

import javax.swing.JFrame;

public class TJ {

    private JFrame mFrame = null;
    private TJCanvas2D mCanvas2D = null;
    public TJCanvas2D getCanvas2D() {
        return this.mCanvas2D;
    }

    public TJ() {
        // create the main frame
        this.mFrame = new JFrame("Travel Journal");
        this.mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mFrame.setSize(800, 600);

        // create the 2D canvas
        this.mCanvas2D = new TJCanvas2D();
        this.mFrame.getContentPane().add(this.mCanvas2D);

        // make the frame visible
        this.mFrame.setVisible(true);
    }
    public static void main(String[] args) {
        // create a JSI instance
        new TJ();
    }
}

