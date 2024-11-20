import java.util.ArrayList;
import java.util.List;

public class AIPlayer 
{
    private int depth;
    
    public AIPlayer(int depth)
    {
        this.depth = depth;
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
            for(Piece[][] successor : getSuccesors(currentState))
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
            for(Piece[][] successor : getSuccesors(currentState))
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

    private List<Piece[][]> getSuccesors(Piece[][] currentState)
    {
        List<Piece[][]> successors = new ArrayList<>();

        for(int row = 0; row < currentState.length; row++)
        {
            for(int col = 0; col < currentState[row].length; col++)
            {
                Piece piece = currentState[row][col];
                if(piece != null && piece.isAIControlled())
                {
                    List<int[]> validMoves = getValidMoves(currentState, row, col);
                    for (int[] move : validMoves) 
                    {
                        int newRow = move[0];
                        int newCol = move[1];

                        Piece[][] newState = deepCopyBoard(currentState);
                        newState[newRow][newCol] = piece;
                        newState[row][col] = null;
                        successors.add(newState);
                    }
                }
            }
        }
        return successors;
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
