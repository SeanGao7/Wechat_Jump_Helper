import com.sun.awt.AWTUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A helper for Wechat Mini-Program "Jump"
 */
public class Wechat_Jump_Helper {
    private static final int SCALE = 3;
    private static final int BORDER = 2;
    private static JFrame mFrame;
    private static int x;
    private static int y;

    /**
     * Initiate a transparent frame
     */
    private static void setUpFrame() {
        mFrame = new JFrame("Wechat Jump");
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrame.setLayout(new GridBagLayout());
        mFrame.setSize(new Dimension(x, y));
        mFrame.getRootPane().setBorder(BorderFactory.createMatteBorder(BORDER, BORDER, BORDER, BORDER, Color.BLACK));
        JPanel panel = new MotionPanel(mFrame);
        mFrame.setContentPane(panel);
    }

    /**
     * Main Function
     * @param args 1st and 2nd argument as the horizontal / vertical pixel number of the cell phone screen
     */
    public static void main(String[] args){
        // Set up the size of the frame
        x = (int) (Integer.parseInt(args[0]) / SCALE + 2 * BORDER);
        y = (int) (Integer.parseInt(args[1]) / SCALE + 2 * BORDER);
        setUpFrame();
        mFrame.setUndecorated(true);
        mFrame.setOpacity(0.1f);
        mFrame.setVisible(true);
    }

    /**
     * A customized panel class which is both draggable and also clickable for
     */
    static class MotionPanel extends JPanel {
        private Point initialClick;
        private Point effectiveClick;
        private boolean isEffective;
        private JFrame parent;

        MotionPanel(final JFrame parent){
            this.parent = parent;
            effectiveClick = null;

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint();
                    getComponentAt(initialClick);
                    System.out.println("x: " + e.getX());
                    System.out.println("y: " + e.getY());
                    if (isEffective){
                        if (effectiveClick == null){
                            effectiveClick = e.getPoint();
                        } else{
                            // Do the jump
                            System.out.println("Jump");
                            double distance = Math.sqrt(
                                    Math.pow(initialClick.getX() - effectiveClick.getX(), 2)
                                    + Math.pow(initialClick.getY() - effectiveClick.getY(), 2)
                            );
                            distance = distance * SCALE;
                            int time = (int) (distance * 1.35);
                            int tempX = (int) (Math.random() * 100 + 540);
                            int tempY = (int) (Math.random() * 100 + 1110);
                            String jumpCommand = "adb shell input swipe "
                                    + tempX + " " + tempY + " " + tempX + " " + tempY + " " + time;
                            System.out.println(distance);
                            System.out.println(jumpCommand);
                            Cmd_Handler.executeCommand(jumpCommand);
                        }
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {

                    // get location of Window
                    int thisX = parent.getLocation().x;
                    int thisY = parent.getLocation().y;

                    // Determine how much the mouse moved since the initial click
                    int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
                    int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

                    // Move window to this position
                    int X = thisX + xMoved;
                    int Y = thisY + yMoved;
                    parent.setLocation(X, Y);
                }
            });

            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    // Do nothing
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_J){
                        isEffective = true;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_J){
                        isEffective = false;
                        // Reset the last click
                        effectiveClick = null;
                    }
                }
            });
            setFocusable(true);
            requestFocus();
        }
    }
}

/**
 * A Java class which takes cmd commands as input and execute them
 */
class Cmd_Handler {
    static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }
}
