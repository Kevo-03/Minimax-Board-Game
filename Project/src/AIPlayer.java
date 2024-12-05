import java.util.ArrayList;
import java.util.List;

public class AIPlayer 
{
    private int depth;
    
    public AIPlayer(int depth)
    {
        this.depth = depth;
    }

    public Piece[][] makeMove(Piece[][] boardState) 
    {
        int bestScore = Integer.MIN_VALUE;
        Piece[][] bestMove = null;
    
        for (Piece[][] successor : getSuccesors(boardState,true)) 
        {
            int score = minimax(successor, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE,false);
            if (score > bestScore) 
            {
                bestScore = score;
                bestMove = successor;
            }
        }
        return bestMove;
    }

    private int minimax(Piece [][] currentState, int depth, int alpha, int beta, boolean isMaximazing)
    {
        if(depth == 0 && isTerminalState(currentState))
        {
            return evaluate(currentState);
        }

        if(isMaximazing)
        {
            int v = Integer.MIN_VALUE;
            for(Piece[][] successor : getSuccesors(currentState,true))
            {
                int tempV = minimax(successor, depth - 1, alpha, beta, false);
                v = Math.max(tempV,v);
                if(v >= beta)
                    return v;
                alpha = Math.max(alpha, v);
            }
            return v;
        }
        else 
        {
            int v = Integer.MAX_VALUE;
            for(Piece[][] successor : getSuccesors(currentState,false))
            {
                int tempV = minimax(successor, depth - 1, alpha, beta, true);
                v = Math.min(tempV,v);
                if(v <= alpha)
                    return v;
                beta = Math.min(beta, v);
            }
            return v;
        }
    }

    private int evaluate(Piece [][] boardState)
    {
        return 1;
    }

    private boolean isTerminalState(Piece [][] boardState)
    {
        return false;
    }

    private List<Piece[][]> getSuccesors(Piece[][] currentState,boolean isAiTurn)
    {
        List<Piece[][]> successors = new ArrayList<>();

        for(int row = 0; row < currentState.length; row++)
        {
            for(int col = 0; col < currentState[row].length; col++)
            {
                Piece piece = currentState[row][col];
                if(piece != null && piece.isAIControlled() == isAiTurn)
                {
                    List<int[]> validMoves = getValidMoves(currentState, row, col);
                    for (int[] move : validMoves) 
                    {
                        int newRow = move[0];
                        int newCol = move[1];
                        Piece[][] newState = deepCopyBoard(currentState);
                        newState[newRow][newCol] = piece;
                        newState[row][col] = null;
                        checkCapture(newState, newRow, newCol);
                        successors.add(newState);
                    }
                }
            }
        }
        return successors;
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

    private List<int[]> getValidMoves(Piece[][] boardState, int row, int col) 
    {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) 
        {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < boardState.length && newCol >= 0 && newCol < boardState[0].length && boardState[newRow][newCol] == null) 
            {
                moves.add(new int[]{newRow, newCol});
            }
        }

        return moves;
    }

    private Piece[][] deepCopyBoard(Piece[][] boardState) 
    {
        Piece[][] newState = new Piece[boardState.length][boardState[0].length];
        for (int row = 0; row < boardState.length; row++) 
        {
            for (int col = 0; col < boardState[row].length; col++) 
            {
                newState[row][col] = boardState[row][col]; 
            }
        }
        return newState;
    }
}
