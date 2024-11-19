import javax.swing.*;
import java.awt.*;

public class GameBoardGUI extends JFrame 
{
    private JPanel boardPanel;
    private JButton[][] boardButtons;

    public GameBoardGUI() 
    {
        super("Strategic Board Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);

        initializeBoard();

        setVisible(true);
    }

    private void initializeBoard() 
    {
        boardPanel = new JPanel(new GridLayout(7, 7));
        boardButtons = new JButton[7][7];

        for (int row = 0; row < 7; row++) 
        {
            for (int col = 0; col < 7; col++) 
            {
                boardButtons[row][col] = new JButton();
                boardButtons[row][col].setPreferredSize(new Dimension(80, 80));
                boardButtons[row][col].setBackground(Color.LIGHT_GRAY);

             
                if ((col == 0 && (row == 0 || row == 2 )) || (col == 6 && (row == 4 || row == 6 ))) 
                { 
                    setPieceOnButton(boardButtons[row][col], new TrianglePiece());
                } 
                else if ((col == 0 && (row == 4 || row == 6 )) || (col == 6 && (row == 0 || row == 2 ))) 
                { 
                    setPieceOnButton(boardButtons[row][col], new CirclePiece());
                }

                boardPanel.add(boardButtons[row][col]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
    }

    private void setPieceOnButton(JButton button, Piece piece) 
    {
        button.setIcon(resizeIcon(piece.getIcon(), 60, 60));
        button.putClientProperty("piece", piece);
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) 
    {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(GameBoardGUI::new);
    }
}