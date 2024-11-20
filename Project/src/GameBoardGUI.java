import javax.swing.*;
import java.awt.*;

public class GameBoardGUI extends JFrame 
{
    private JPanel boardPanel;
    private JButton[][] boardButtons;
    private Piece[][] boardState;
    private Piece selectedPiece = null; 
    private int selectedRow = -1;       
    private int selectedCol = -1;    

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
        boardState = new Piece[7][7];

        for (int row = 0; row < 7; row++) 
        {
            for (int col = 0; col < 7; col++) 
            {
                boardButtons[row][col] = new JButton();
                boardButtons[row][col].setPreferredSize(new Dimension(80, 80));
                boardButtons[row][col].setBackground(Color.LIGHT_GRAY);

             
                if ((col == 0 && (row == 0 || row == 2 )) || (col == 6 && (row == 4 || row == 6 ))) 
                { 
                    TrianglePiece piece = new TrianglePiece();
                    setPieceOnButton(boardButtons[row][col], piece);
                    boardState[row][col] = piece;
                } 
                else if ((col == 0 && (row == 4 || row == 6 )) || (col == 6 && (row == 0 || row == 2 ))) 
                { 
                    CirclePiece piece = new CirclePiece();
                    setPieceOnButton(boardButtons[row][col], piece);
                    boardState[row][col] = piece;
                }
                else 
                {
                    boardState[row][col] = null;
                }
    

                int finalRow = row;
                int finalCol = col;
                boardButtons[row][col].addActionListener(e -> handleButtonClick(finalRow, finalCol));
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

    private void handleButtonClick(int row, int col) 
    {
        JButton clickedButton = boardButtons[row][col];
        Piece clickedPiece = (Piece) clickedButton.getClientProperty("piece");
    
        if (selectedPiece == null && clickedPiece != null) 
        {
            if (clickedPiece instanceof CirclePiece)
            {
                selectedPiece = clickedPiece;
                selectedRow = row;
                selectedCol = col;
                System.out.println("Piece selected at (" + row + ", " + col + ")");
            }
        } 
        else if (selectedPiece != null) 
        {
            if (isAdjacent(row, col, selectedRow, selectedCol) && boardState[row][col] == null)
            {
                System.out.println("Moving piece to (" + row + ", " + col + ")");
        
                boardButtons[selectedRow][selectedCol].setIcon(null);
                boardButtons[selectedRow][selectedCol].putClientProperty("piece", null);
                setPieceOnButton(clickedButton, selectedPiece);

                boardState[row][col] = selectedPiece;
                boardState[selectedRow][selectedCol] = null;
        
                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;
            }
        }
        printBoardState();
    }

    private void printBoardState() 
    {
        for (int row = 0; row < 7; row++) 
        {
            for (int col = 0; col < 7; col++) 
            {
                if (boardState[row][col] == null) 
                {
                    System.out.print("[ ] ");
                } 
                else if (boardState[row][col] instanceof CirclePiece) 
                {
                    System.out.print("[O] ");
                }
                 else if (boardState[row][col] instanceof TrianglePiece) 
                 {
                    System.out.print("[△] ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private boolean isAdjacent(int row, int col, int selectedRow, int selectedCol) 
    {
        return (Math.abs(row - selectedRow) == 1 && col == selectedCol) || (Math.abs(col - selectedCol) == 1 && row == selectedRow);
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