import java.util.LinkedList;
import java.util.function.Function;

public class TablutState {
    public static final byte EMPTY = 0;
    public static final byte WHITE = 1;
    public static final byte BLACK = 2;
    public static final byte KING = 3;
    public static final byte ESCAPE = 1;
    public static final byte CAMP = 2;
    public static final byte CITADEL = 3;

    public static final int BOARD_SIZE = 9;

    private static byte[][] board = new byte[BOARD_SIZE][BOARD_SIZE];

    private byte[][] pawns;
    private TablutAction previousAction;
    private byte playerTurn;

    private boolean whiteWin = false;
    private boolean blackWin = false;
    private boolean draw = false;

    public TablutState() {
        this.playerTurn = WHITE;
        initBoard();
    }

    public TablutState(byte[][] pawns, byte playerTurn) {
        this.pawns = pawns;
        this.playerTurn = playerTurn;
    }

    public byte[][] getBoard() {
        return pawns;
    }

    public void initBoard() {
        pawns = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 1; i < BOARD_SIZE - 1; i++) {
            if (i < 3 || i > 5) {
                if (i < 3) {
                    pawns[4][i + 1] = WHITE;
                    pawns[i + 1][4] = WHITE;
                    pawns[4][BOARD_SIZE - i - 2] = WHITE;
                    pawns[BOARD_SIZE - i - 2][4] = WHITE;
                }
                board[0][i] = ESCAPE;
                board[i][0] = ESCAPE;
                board[BOARD_SIZE - 1][i] = ESCAPE;
                board[i][BOARD_SIZE - 1] = ESCAPE;
            } else {
                board[0][i] = CAMP;
                pawns[0][i] = BLACK;
                board[i][0] = CAMP;
                pawns[i][0] = BLACK;
                board[BOARD_SIZE - 1][i] = CAMP;
                pawns[BOARD_SIZE - 1][i] = BLACK;
                board[i][BOARD_SIZE - 1] = CAMP;
                pawns[i][BOARD_SIZE - 1] = BLACK;
            }
        }
        board[1][4] = CAMP;
        pawns[1][4] = BLACK;
        board[4][1] = CAMP;
        pawns[4][1] = BLACK;
        board[BOARD_SIZE - 2][4] = CAMP;
        pawns[BOARD_SIZE - 2][4] = BLACK;
        board[4][BOARD_SIZE - 2] = CAMP;
        pawns[4][BOARD_SIZE - 2] = BLACK;

        pawns[4][4] = KING;
        board[4][4] = CITADEL;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pawns[i][j] == BLACK)
                    result = result + "B";
                else if (pawns[i][j] == WHITE)
                    result = result + "W";
                else if (pawns[i][j] == KING)
                    result = result + "K";
                else if (pawns[i][j] == EMPTY)
                    result = result + "-";
                else
                    result = result + "?";
            }
            result = result + "\n";
        }
        result = result + "\n";
        return result;
    }

    @Override
    public TablutState clone() {
        byte[][] newPawns = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                newPawns[i][j] = pawns[i][j];
        return new TablutState(newPawns, this.playerTurn);
    }

    public TablutAction getPreviousAction() {
        return previousAction;
    }

    public void setPreviousAction(TablutAction previousAction) {
        this.previousAction = previousAction;
    }

    public LinkedList<TablutAction> getLegalActions() {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pawns[i][j] == this.playerTurn)
                    actions.addAll(getPawnActions(new Coordinates(i, j), pawns[i][j]));
            }
        return actions;
    }

    private LinkedList<TablutAction> getPawnActions(Coordinates coord, byte pawn) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        actions.addAll(searchActions(coord, pawn, (Integer i) -> i >= 0, -1, true));
        actions.addAll(searchActions(coord, pawn, (Integer i) -> i >= 0, -1, false));
        actions.addAll(searchActions(coord, pawn, (Integer i) -> i < BOARD_SIZE, 1, true));
        actions.addAll(searchActions(coord, pawn, (Integer i) -> i < BOARD_SIZE, 1, false));
        return actions;
    }

    private LinkedList<TablutAction> searchActions(Coordinates coord, byte pawn, Function<Integer, Boolean> condition, int step, boolean row) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for(int i = (row ? coord.getRow() : coord.getColumn()) + step; condition.apply(i); i += step) {
            TablutAction action;
            Coordinates c = new Coordinates(row ? i : coord.getRow(), !row ? i : coord.getColumn());
            action = new TablutAction(c, new Pawn(pawn, coord));
            if(!isLegal(action))
                break;
            actions.add(action);
        }
        return actions;
    }


    private boolean isLegal(TablutAction action) {
        int nextRow = action.getCoordinates().getRow();
        int nextColumn = action.getCoordinates().getColumn();
        int row = action.getPawn().getPosition().getRow();
        int column = action.getPawn().getPosition().getColumn();
        byte pawn = action.getPawn().getPawnType();
        return pawns[nextRow][nextColumn] == EMPTY && (board[nextRow][nextColumn] == EMPTY || 
            board[nextRow][nextColumn] == ESCAPE ||
            (board[nextRow][nextColumn] == CAMP && board[row][column] == CAMP && pawn == BLACK));
    }

    public void makeAction(TablutAction action) {
        Coordinates pawnPosition = action.getPawn().getPosition();
        Coordinates nextPosition = action.getCoordinates();
        byte pawn = action.getPawn().getPawnType();
        pawns[pawnPosition.getRow()][pawnPosition.getColumn()] = EMPTY;
        pawns[nextPosition.getRow()][nextPosition.getColumn()] = pawn;

        if(pawn == KING && isOnPosition(nextPosition, ESCAPE)) {
            whiteWin = true;
            return;
        }
        capture(nextPosition, pawn, nextPosition.getRow() - 2, nextPosition.getColumn());
        capture(nextPosition, pawn, nextPosition.getRow() + 2, nextPosition.getColumn());
        capture(nextPosition, pawn, nextPosition.getRow(), nextPosition.getColumn() - 2);
        capture(nextPosition, pawn, nextPosition.getRow(), nextPosition.getColumn() + 2);

        this.previousAction = action;
        if(this.playerTurn == WHITE)
            this.playerTurn = BLACK;
        else
            this.playerTurn = WHITE;
    }

    private void capture(Coordinates pawnPosition, byte pawn, int row, int column) {
        Coordinates captured;
        captured = getCaptured(pawnPosition, pawn, row, column);
        if(captured != null) {
            if(pawns[captured.getRow()][captured.getColumn()] == KING)
                blackWin = true;
            pawns[captured.getRow()][captured.getColumn()] = EMPTY;
        }
    }
    
    private Coordinates getCaptured(Coordinates position, byte pawn, int row, int column) {
        if(row < 0 || column < 0 || row >= BOARD_SIZE || column >= BOARD_SIZE)
            return null;
        if(!isBlocker(row, column, pawn))
            return null;
        Coordinates captured = null;
        if(row == position.getRow()) 
            if(column > position.getColumn())
                captured = new Coordinates(row, column - 1);
            else 
                captured = new Coordinates(row, column + 1);
        else if(column == position.getColumn())
            if(row > position.getRow())
                captured = new Coordinates(row - 1, column);
            else
                captured = new Coordinates(row + 1, column);
        else
            return null;  
        if(!isEnemy(pawns[captured.getRow()][captured.getColumn()], pawn)) 
            return null;
        
        if(pawns[captured.getRow()][captured.getColumn()] == KING && !kingCaptured(captured)) 
            return null;
        return captured;
    }

    private boolean isBlocker(int row, int column, byte pawn) {
        byte blocker = pawns[row][column];
        if(blocker == KING)
            blocker = WHITE;
        if(pawn == KING)
            pawn = WHITE;
        byte cell = board[row][column];
        if(pawn == blocker)
            return true;
        if(cell == CAMP || cell == CITADEL)
            return true;
        return false;
    }

    private boolean isEnemy(byte enemy, byte pawn) {
        if(enemy == EMPTY)
            return false;
        if(enemy == KING)
            enemy = WHITE;
        if(pawn == KING)
            pawn = WHITE;
        return pawn != enemy;
    }

    private boolean kingCaptured(Coordinates captured) {
        int r = captured.getRow();
        int c = captured.getColumn();
        if(board[r][c] == CITADEL) 
            return pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK 
                && pawns[r][c + 1] == BLACK && pawns[r][c - 1] == BLACK; 
        else if(board[r + 1][c] == CITADEL) 
            return pawns[r - 1][c] == BLACK && pawns[r][c + 1] == BLACK && pawns[r][c - 1] == BLACK;
        else if(board[r - 1][c] == CITADEL) 
            return pawns[r + 1][c] == BLACK && pawns[r][c + 1] == BLACK && pawns[r][c - 1] == BLACK;
        else if(board[r][c + 1] == CITADEL) 
            return pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK && pawns[r][c - 1] == BLACK;
        else if(board[r][c - 1] == CITADEL) 
            return pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK && pawns[r][c + 1] == BLACK;
        return true;
    }

    private boolean isOnPosition(Coordinates coordinates, byte position) {
        return board[coordinates.getRow()][coordinates.getColumn()] == position;
    }

    public boolean isWhiteWin() {
        return whiteWin;
    }

    public boolean isBlackWin() {
        return blackWin;
    }

    public boolean isDraw() {
        return draw;
    }

    public byte[][] getPawns() {
        return pawns;
    }

	public byte getPlayerTurn() {
		return this.playerTurn;
	}
}