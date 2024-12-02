import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
            if (clickedPiece instanceof CirclePiece || clickedPiece instanceof TrianglePiece)
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

                checkCapture(boardState, row, col);

                redrawBoard();
            }
        }
        printBoardState();
    }

    private void checkCapture(Piece[][] board, int row, int col) 
    {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; 
    
        for (int[] dir : directions) 
        {
            List<int[]> captureGroup = new ArrayList<>();
            boolean wallFound = false;
            boolean friendlyPieceFound = false;
    
            int r = row + dir[0];
            int c = col + dir[1];
    
            while (isWithinBounds(r, c, board)) 
            {
                if (board[r][c] == null) 
                {
                    break; 
                }
    
                if (board[row][col] != null && board[r][c].isAIControlled() == board[row][col].isAIControlled()) 
                {
                    friendlyPieceFound = true; 
                    break;
                }
    
                captureGroup.add(new int[]{r, c}); 
                r += dir[0];
                c += dir[1];
            }
    
            if (!isWithinBounds(r, c, board)) 
            {
                wallFound = true; 
            }
    
            if (wallFound || friendlyPieceFound) 
            {
                for (int[] pos : captureGroup) 
                {
                    board[pos[0]][pos[1]] = null;
                }
            }
        }
    
        boolean movedPieceCaptured = checkSelfCapture(board, row, col);
        if (movedPieceCaptured) 
        {
            board[row][col] = null; 
        }
    
        for (int[] dir : directions) 
        {
            int adjRow = row + dir[0];
            int adjCol = col + dir[1];
    
            if (isWithinBounds(adjRow, adjCol, board) && board[adjRow][adjCol] != null &&
                board[row][col] != null && board[adjRow][adjCol].isAIControlled() == board[row][col].isAIControlled()) 
                {
                boolean allyCaptured = checkSelfCapture(board, adjRow, adjCol);
    
                if (allyCaptured) 
                {
                    board[adjRow][adjCol] = null;
                }
            }
        }
    }
    
    private boolean checkSelfCapture(Piece[][] board, int row, int col) 
    {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) 
        {
            int r1 = row + dir[0];
            int c1 = col + dir[1];
            int r2 = row - dir[0];
            int c2 = col - dir[1];
    
            boolean isTrapped = isWithinBounds(r1, c1, board) &&
                                board[r1][c1] != null &&
                                board[r1][c1].isAIControlled() != board[row][col].isAIControlled() &&
                                (!isWithinBounds(r2, c2, board) || 
                                 (board[r2][c2] != null && board[r2][c2].isAIControlled() != board[row][col].isAIControlled()));
    
            if (isTrapped) 
            {
                return true; 
            }
        }
        return false; 
    }

    private boolean isWithinBounds(int row, int col, Piece[][] board) 
    {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }

    private void redrawBoard() 
    {
        for (int row = 0; row < boardState.length; row++) 
        {
            for (int col = 0; col < boardState[row].length; col++) 
            {
                if (boardState[row][col] == null) 
                {
                    boardButtons[row][col].setIcon(null); // Clear the icon
                    //setPieceOnButton(boardButtons[row][col], null);
                } 
                else 
                {
                    //Piece piece = boardState[row][col];
                    //setPieceOnButton(boardButtons[row][col], piece);
                    boardButtons[row][col].setIcon(resizeIcon(boardState[row][col].getIcon(), 60, 60)); // Update with the piece's icon
                }
            }
        }
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