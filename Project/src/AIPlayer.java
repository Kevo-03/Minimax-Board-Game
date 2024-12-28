import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class AIPlayer 
{
    private int depth;
    private Map<String, TranspositionEntry> transpositionTable = new HashMap<>();
    
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

    private int minimax(Piece[][] currentState, int depth, int alpha, int beta, boolean isMaximizing) 
    {
        String boardHash = hashBoard(currentState);
        if (transpositionTable.containsKey(boardHash)) 
        {
            TranspositionEntry entry = transpositionTable.get(boardHash);
            if (entry.depth >= depth) 
            {
                if (entry.isExact) return entry.value; 
                if (isMaximizing && entry.value > alpha) alpha = entry.value; 
                if (!isMaximizing && entry.value < beta) beta = entry.value; 
                if (alpha >= beta) return entry.value;
            }
        }
    
        if (depth == 0 || isTerminalState(currentState)) 
        {
            int eval = evaluate(currentState);
            transpositionTable.put(boardHash, new TranspositionEntry(eval, depth, true));
            return eval;
        }
    
        int value;
        if (isMaximizing) 
        {
            value = Integer.MIN_VALUE;
            for (Piece[][] successor : getSuccesors(currentState, true)) 
            {
                int tempValue = minimax(successor, depth - 1, alpha, beta, false);
                value = Math.max(value, tempValue);
                if (value >= beta) 
                    break;
                alpha = Math.max(alpha, value);
            }
        } 
        else 
        {
            value = Integer.MAX_VALUE;
            for (Piece[][] successor : getSuccesors(currentState, false)) 
            {
                int tempValue = minimax(successor, depth - 1, alpha, beta, true);
                value = Math.min(value, tempValue);
                if (value <= alpha) 
                    break;
                beta = Math.min(beta, value);
            }
        }
        boolean isExact = value > alpha && value < beta;
        transpositionTable.put(boardHash, new TranspositionEntry(value, depth, isExact));
    
        return value;
    }

    private int evaluate(Piece[][] boardState) 
    {
        int aiScore = 0;
        int humanScore = 0;
    
        for (int row = 0; row < boardState.length; row++) 
        {
            for (int col = 0; col < boardState[row].length; col++) 
            {
                Piece piece = boardState[row][col];
                if (piece != null) 
                {
                    if (piece.isAIControlled()) 
                    {
                        aiScore += calculatePieceScore(piece, row, col, boardState);
                    } 
                    
                    else 
                    {
                        humanScore += calculatePieceScore(piece, row, col, boardState);
                    }
                }
            }
        }
    
        return aiScore - humanScore;
    }

    private int calculatePieceScore(Piece piece, int row, int col, Piece[][] boardState) 
    {
        int score = 100; 
        int centerRow = boardState.length / 2;
        int centerCol = boardState[0].length / 2;
        int distanceFromCenter = Math.abs(row - centerRow) + Math.abs(col - centerCol);
        score += (10 - distanceFromCenter); 
        score += evaluateFutureCaptures(boardState, row, col) * 30;
        score += getDefensiveSupport(boardState, row, col) * 10;
    
        return score;
    }

    private int getDefensiveSupport(Piece[][] boardState, int row, int col) 
    {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int defense = 0;
    
        for (int[] dir : directions) 
        {
            int r = row + dir[0];
            int c = col + dir[1];
    
            if (isWithinBounds(r, c, boardState)) 
            {
                Piece adjacent = boardState[r][c];
                if (adjacent != null && adjacent.isAIControlled() == boardState[row][col].isAIControlled()) 
                {
                    defense++;
                }
            }
        }
    
        return defense;
    }

    private int evaluateFutureCaptures(Piece[][] boardState, int row, int col) 
    {
        int captureCount = 0;
        List<int[]> validMoves = getValidMoves(boardState, row, col);
        for (int[] move : validMoves) 
        {
            int newRow = move[0];
            int newCol = move[1];
    
            Piece[][] newState = deepCopyBoard(boardState);
            newState[newRow][newCol] = boardState[row][col];
            newState[row][col] = null;
    
            List<int[]> capturedPieces = checkCaptureWithoutModify(newState, newRow, newCol);
            captureCount += capturedPieces.size();
        }
    
        return captureCount;
    }

    private  List<int[]> checkCaptureWithoutModify(Piece[][] board, int row, int col) 
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

        return captures;
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
    
        return aiPieces == 0 || humanPieces == 0 || (aiPieces == 1 && humanPieces == 1);
    }

    private List<Piece[][]> getSuccesors(Piece[][] currentState, boolean isAiTurn) 
    {
        List<Piece[][]> successors = new ArrayList<>();
        int pieceCount = countPieces(currentState, isAiTurn);
    
        for (int row1 = 0; row1 < currentState.length; row1++) 
        {
            for (int col1 = 0; col1 < currentState[row1].length; col1++) 
            {
                Piece piece1 = currentState[row1][col1];
                if (piece1 != null && piece1.isAIControlled() == isAiTurn) 
                {
                    List<int[]> validMoves1 = getValidMoves(currentState, row1, col1);
    
                    for (int[] move1 : validMoves1) 
                    {
                        int newRow1 = move1[0];
                        int newCol1 = move1[1];
    
                        Piece[][] newState1 = deepCopyBoard(currentState);
                        newState1[newRow1][newCol1] = piece1;
                        newState1[row1][col1] = null;
                        checkCapture(newState1, newRow1, newCol1);
                        if (pieceCount <= 1) 
                        {
                            successors.add(newState1);
                        } 
                        else 
                        {
                            for (int row2 = 0; row2 < newState1.length; row2++) 
                            {
                                for (int col2 = 0; col2 < newState1[row2].length; col2++) 
                                {
                                    Piece piece2 = newState1[row2][col2];
                                    if (piece2 != null && piece2.isAIControlled() == isAiTurn && (row2 != newRow1 || col2 != newCol1)) 
                                    {
                                        List<int[]> validMoves2 = getValidMoves(newState1, row2, col2);
    
                                        for (int[] move2 : validMoves2) 
                                        {
                                            int newRow2 = move2[0];
                                            int newCol2 = move2[1];

                                            Piece[][] newState2 = deepCopyBoard(newState1);
                                            newState2[newRow2][newCol2] = piece2;
                                            newState2[row2][col2] = null;
                                            checkCapture(newState2, newRow2, newCol2);
    
                                            successors.add(newState2);
                                        }
                                    }
                                }
                            }
                        }
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

    private int countPieces(Piece[][] boardState, boolean isAIControlled)
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

    private String hashBoard(Piece[][] boardState) 
    {
        StringBuilder sb = new StringBuilder();
        for (Piece[] row : boardState) 
        {
            for (Piece piece : row) 
            {
                if (piece == null) 
                {
                    sb.append("0");
                }
                else if (piece.isAIControlled()) 
                {
                    sb.append("1");
                } 
                else 
                {
                    sb.append("2");
                }
            }
        }
        return sb.toString();
    }
}
