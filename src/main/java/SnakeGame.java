import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SnakeGame {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Snake Game");

            GamePanel gamePanel = new GamePanel();

            frame.add(gamePanel);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            frame.setResizable(false);

            frame.pack();

            frame.setLocationRelativeTo(null);

            frame.setVisible(true);

            gamePanel.requestFocusInWindow();
        });
    }
}