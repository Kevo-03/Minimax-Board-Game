import javax.swing.*;
import java.awt.*;

public class GameBoardGUI extends JFrame {
    private JPanel boardPanel;
    private JButton[][] boardButtons;

    public GameBoardGUI() {
        // Set up the JFrame
        super("Strategic Board Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);

        // Initialize the GUI components
        initializeBoard();

        // Make the JFrame visible
        setVisible(true);
    }

    private void initializeBoard() {
        // Create the board panel with a 7x7 grid
        boardPanel = new JPanel(new GridLayout(7, 7));
        boardButtons = new JButton[7][7];

        // Initialize the board buttons
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boardButtons[row][col] = new JButton();
                boardButtons[row][col].setPreferredSize(new Dimension(80, 80));
                boardButtons[row][col].setBackground(Color.LIGHT_GRAY);

             
                if ((col == 0 && (row == 0 || row == 2 )) || (col == 6 && (row == 4 || row == 6 ))) 
                { 
                    boardButtons[row][col].setText("△");
                    boardButtons[row][col].setForeground(Color.RED);
                } 
                else if ((col == 0 && (row == 4 || row == 6 )) || (col == 6 && (row == 0 || row == 2 ))) 
                { 
                    boardButtons[row][col].setText("○");
                    boardButtons[row][col].setForeground(Color.BLUE);
                }

                boardPanel.add(boardButtons[row][col]);
            }
        }

        // Add the board panel to the JFrame
        add(boardPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameBoardGUI::new);
    }
}