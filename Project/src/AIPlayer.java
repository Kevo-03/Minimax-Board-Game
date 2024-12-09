import java.util.ArrayList;
import java.util.List;

public class AIPlayer 
{
    private int depth;
    
    public AIPlayer(int depth)
    {
        this.depth = depth;
    }

    public Piece[][] makeMove(Piece[][] boardState, List<Piece> movedPieces) 
    {
        int bestScore = Integer.MIN_VALUE;
        Piece[][] bestMove = null;
    
        for (Piece[][] successor : getSuccesors(boardState,true)) 
        {
            if (movedPieces.contains(getMovedPiece(boardState, successor))) 
            {
                continue;
            }
            int score = minimax(successor, this.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE,false);
            if (score > bestScore) 
            {
                bestScore = score;
                bestMove = successor;
            }
        }
        Piece moved = getMovedPiece(boardState, bestMove);
        movedPieces.add(moved);
        printBoardState(bestMove);
        System.out.println("AI move chosen. Best score: " + bestScore);
        return bestMove;
    }

    private int minimax(Piece [][] currentState, int depth, int alpha, int beta, boolean isMaximazing)
    {
        if(depth == 0 || isTerminalState(currentState))
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
    /* private int minimax(Piece[][] currentState, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || isTerminalState(currentState)) {
            return evaluate(currentState);
        }
    
        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Piece[][] successor : getSuccesors(currentState, true)) {
                int eval = minimax(successor, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Beta cutoff
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Piece[][] successor : getSuccesors(currentState, false)) {
                int eval = minimax(successor, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }
            return minEval;
        }
    } */

    private int evaluate(Piece [][] boardState)
    {
        int aiScore = 0, humanScore = 0;

        for (int row = 0; row < boardState.length; row++) {
            for (int col = 0; col < boardState[row].length; col++) {
                Piece piece = boardState[row][col];
                if (piece != null) {
                    if (piece.isAIControlled()) {
                        aiScore += 10; // AI piece value
                        //aiScore += getPositionalValue(row, col); // Positional advantage
                    } else {
                        humanScore += 10; // Human piece value
                        //humanScore += getPositionalValue(row, col); // Positional advantage
                    }
                }
            }
        }

        return aiScore - humanScore; // Higher score means better for AI
    }

    private int getPositionalValue(int row, int col) 
    {
        // Example: Encourage central positions or proximity to opponent's side
        return 7 - Math.abs(row - 3) - Math.abs(col - 3); // Center of the board is most valuable
    }

    private boolean isTerminalState(Piece[][] boardState) 
    {
        int aiPieces = 0, humanPieces = 0;
    
        for (Piece[] row : boardState) 
        {
            for (Piece piece : row) 
            {
                if (piece != null) 
                {
                    if (piece.isAIControlled()) aiPieces++;
                    else humanPieces++;
                }
            }
        }
    
        // Terminal conditions
        return aiPieces == 0 || humanPieces == 0 || (aiPieces == 1 && humanPieces == 1);
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

    private Piece getMovedPiece(Piece[][] previousState, Piece[][] currentState) 
    {
        for (int row = 0; row < previousState.length; row++) 
        {
            for (int col = 0; col < previousState[row].length; col++) 
            {
                if (previousState[row][col] != currentState[row][col] && currentState[row][col] != null) 
                {
                    return currentState[row][col];
                }
            }
        }
        return null;
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

    private void printBoardState(Piece[][] boardState) 
    {
        for (Piece[] row : boardState) 
        {
            for (Piece piece : row) 
            {
                if (piece == null) 
                    System.out.print("[ ] ");
                else if (piece instanceof CirclePiece) 
                    System.out.print("[O] ");
                else if (piece instanceof TrianglePiece) 
                    System.out.print("[△] ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
