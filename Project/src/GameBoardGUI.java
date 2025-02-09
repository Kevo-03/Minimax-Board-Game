import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameBoardGUI extends JFrame 
{
    private JPanel boardPanel;
    private JButton[][] boardButtons;
    private JLabel moveCountLabel;
    private Piece[][] boardState;
    private Piece selectedPiece = null; 
    private int selectedRow = -1;       
    private int selectedCol = -1;
    private int moves = 50;
    private AIPlayer ai;    
    private boolean isAiTurn = true;
    private int humanMoveCount = 2;
    private List<Piece> movedPiecesByAI = new ArrayList<>();
    private List<Piece> movedPieces = new ArrayList<>();

    public GameBoardGUI() 
    {
        super("Strategic Board Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);

        initializeBoard();
        ai = new AIPlayer(3);

        if(isAiTurn)
        {
            aiMove();
        }

        setVisible(true);
    }

    private void initializeBoard() 
    {
        boardPanel = new JPanel(new GridLayout(7, 7));
        boardButtons = new JButton[7][7];
        boardState = new Piece[7][7];

        moveCountLabel = new JLabel();
        updateMoveCountLabel(); 
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(moveCountLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.NORTH);

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

    private void updateMoveCountLabel() 
    {
        String labelText = String.format("Total Moves: %d | Human Moves Left: %d", moves, humanMoveCount );
        moveCountLabel.setText(labelText);
    }

    private void handleButtonClick(int row, int col) 
    {
        if(isAiTurn || (humanMoveCount == 0))
            return;
        JButton clickedButton = boardButtons[row][col];
        Piece clickedPiece = (Piece) clickedButton.getClientProperty("piece");
    
        if (selectedPiece == null && clickedPiece != null && (!clickedPiece.isAIControlled()) && (!movedPieces.contains(clickedPiece))) 
        {
                selectedPiece = clickedPiece;
                selectedRow = row;
                selectedCol = col;
                movedPieces.add(selectedPiece);
                System.out.println("Piece selected at (" + row + ", " + col + ")");
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

                humanMoveCount--;
                moves--;
                updateMoveCountLabel();
                checkGameOver();
                if(humanMoveCount == 0)
                {
                    movedPieces.clear();
                    isAiTurn = true;
                    aiMove();
                }
            }
        }
        printBoardState();
    }

    private void checkCapture(Piece[][] board, int row, int col) 
    {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; 
        List<int[]> captures= new ArrayList<>();

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
                    captures.add(pos);
                }
            }
        }
    
        for (int[] dir : directions) 
        {
            List<int[]> captureGroup = new ArrayList<>();
            boolean wallFound = false;
            boolean opponentPieceFound = false;

            int adjRow = row + dir[0];
            int adjCol = col + dir[1];
            int oppRow = row - dir[0];
            int oppCol = col - dir[1];
            
            if(!isWithinBounds(adjRow, adjCol, board) || (board[adjRow][adjCol] != null && board[adjRow][adjCol].isAIControlled() != board[row][col].isAIControlled()))
            {
                while (isWithinBounds(oppRow, oppCol, board)) 
                {
                    if (board[oppRow][oppCol] == null) 
                    {
                        break; 
                    }
        
                    if (board[row][col] != null && board[oppRow][oppCol].isAIControlled() != board[row][col].isAIControlled()) 
                    {
                        opponentPieceFound = true; 
                        break;
                    }
        
                    captureGroup.add(new int[]{oppRow, oppCol}); 
                    oppRow -= dir[0];
                    oppCol -= dir[1];
                }

                if (!isWithinBounds(oppRow, oppCol, board)) 
                {
                    wallFound = true; 
                }
        
                if (wallFound || opponentPieceFound) 
                {
                    captureGroup.add(new int[]{row, col});
                    for (int[] pos : captureGroup) 
                    {
                        captures.add(pos);
                    }
                    break;
                }
            }
        }

        for (int[] pos : captures) 
        {
            board[pos[0]][pos[1]] = null;
        }
    }
    
    private boolean isWithinBounds(int row, int col, Piece[][] board) 
    {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }

    private void aiMove()
    {
        System.out.println("AI is making its move...");
        if(countPieces(rootPaneCheckingEnabled) > 1)
        {
            moves -= 2;
        }
        else 
        {
            moves--;
        }
        boardState = ai.makeMove(boardState, movedPiecesByAI);
        redrawBoard();  
        checkGameOver();
        movedPiecesByAI.clear();
        isAiTurn = false;
        humanMoveCount = (countPieces(false) > 1) ? 2 : 1;
        updateMoveCountLabel();
        System.out.println("AI completed its turn");
    }

    private void redrawBoard() 
    {
        for (int row = 0; row < boardState.length; row++) 
        {
            for (int col = 0; col < boardState[row].length; col++) 
            {
                if (boardState[row][col] == null) 
                {
                    boardButtons[row][col].setIcon(null);
                } 
                else 
                {
                    boardButtons[row][col].setIcon(resizeIcon(boardState[row][col].getIcon(), 60, 60)); 
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
    
    private int countPieces(boolean isAIControlled)
    {
        int count = 0;
        for (int row = 0; row < boardState.length; row++) 
        {
            for (int col = 0; col < boardState[row].length; col++) 
            {
                if(boardState[row][col] != null && boardState[row][col].isAIControlled() == isAIControlled)
                    count++;
            }
        }
        return count;
    }

    private void checkGameOver() 
    {
        int aiPieces = countPieces(true);
        int humanPieces = countPieces(false);
    
        if (aiPieces == 0) 
        {
            showGameOverMessage("Game Over! Human wins!");
        } 
        else if (humanPieces == 0) 
        {
            showGameOverMessage("Game Over! AI wins!");
        } 
        else if (moves == 0) 
        {
            if(humanPieces == aiPieces)
            {
                showGameOverMessage("Game Over! Draw!");
            }
            else if(humanPieces > aiPieces)
            {
                showGameOverMessage("Game Over! Human wins!");
            }
            else 
            {
                showGameOverMessage("Game Over! AI wins!");
            }
        }
    }

    private void showGameOverMessage(String message) 
    {
        int option = JOptionPane.showOptionDialog(
            this,
            message + "\nDo you want to restart the game?",
            "Game Over",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new Object[]{"Restart", "Exit"},
            "Restart"
        );
    
        if (option == JOptionPane.YES_OPTION) 
        {
            restartGame();
        } 
        else 
        {
            System.exit(0); 
        }
    }

    private void restartGame() 
    {
        for (int row = 0; row < boardState.length; row++) 
        {
            for (int col = 0; col < boardState[row].length; col++) 
            {
                boardState[row][col] = null;
                boardButtons[row][col].setIcon(null);
            }
        }
        for (int row = 0; row < 7; row++) 
        {
            for (int col = 0; col < 7; col++) 
            {
                if ((col == 0 && (row == 0 || row == 2)) || (col == 6 && (row == 4 || row == 6))) 
                {
                    TrianglePiece piece = new TrianglePiece();
                    setPieceOnButton(boardButtons[row][col], piece);
                    boardState[row][col] = piece;
                } 
                else if ((col == 0 && (row == 4 || row == 6)) || (col == 6 && (row == 0 || row == 2))) 
                {
                    CirclePiece piece = new CirclePiece();
                    setPieceOnButton(boardButtons[row][col], piece);
                    boardState[row][col] = piece;
                }
            }
        }
        moves = 50;
        humanMoveCount = 2;
        movedPieces.clear();
        movedPiecesByAI.clear();
        isAiTurn = true;
        updateMoveCountLabel();
        redrawBoard();
        if(isAiTurn)
        {
            aiMove();
        }
    }

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(GameBoardGUI::new);
    }
}