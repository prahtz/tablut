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
    private Coordinates kingPosition;

    private boolean whiteWin = false;
    private boolean blackWin = false;
    private boolean draw = false;
    private int blackPawns = 16;
    private int whitePawns = 9;

    public TablutState() {
        this.playerTurn = WHITE;
        this.kingPosition = new Coordinates(4, 4);
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
        TablutState newState = new TablutState(newPawns, this.playerTurn);
        newState.setBlackPawns(this.blackPawns);
        newState.setWhitePawns(this.whitePawns);
        newState.setKingPosition(this.kingPosition);
        return newState;
    }

    private void setKingPosition(Coordinates kingPosition) {
        this.kingPosition = kingPosition;
    }

    private void setWhitePawns(int whitePawns) {
        this.whitePawns = whitePawns;
    }

    private void setBlackPawns(int blackPawns) {
        this.blackPawns = blackPawns;
    }

    public TablutAction getPreviousAction() {
        return previousAction;
    }

    public void setPreviousAction(TablutAction previousAction) {
        this.previousAction = previousAction;
    }

    public LinkedList<TablutAction> getBestActionFirst() {
        LinkedList<TablutAction> actions = getLegalActions();
        LinkedList<TablutAction> result = new LinkedList<>();
        boolean loosing = false;
        TablutAction kingAction = null;
        if(playerTurn == BLACK) {
            for(TablutAction ka : getPawnActions(kingPosition, KING)) 
                if(isOnPosition(ka.getCoordinates(), ESCAPE)) {
                    kingAction = ka;
                    break;
                }
        }
        for(TablutAction action : actions) {
            if(isWin(action)) {
                result = new LinkedList<>();
                result.add(action);
                break;
            }
            else if(isPreventingLoose(action, kingAction)) {
                result = new LinkedList<>();
                result.add(action);
                loosing = true;
            }
            else if(!loosing)
                result.addLast(action);
        }   
        return result;
    }

    private boolean isPreventingLoose(TablutAction action, TablutAction kingAction) {
        byte player = action.getPawn().getPawnType();
        
        if(player == BLACK && kingAction != null) {
            int row = action.getCoordinates().getRow();
            int column = action.getCoordinates().getColumn();
            int kre = kingAction.getCoordinates().getRow();
            int krk = kingAction.getPawn().getPosition().getRow();
            int kce = kingAction.getCoordinates().getColumn();
            int kck = kingAction.getPawn().getPosition().getColumn();
            if(row == krk && ((column > kck && column < kce) || (column < kck && column > kce)))
                return true;
            if(column == kck &&((row > krk && row < kre) || (row < krk && row > kre)))
                return true;
        }
        return false;
    }

    private boolean isWin(TablutAction action) {
        byte player = action.getPawn().getPawnType();
        if(player == BLACK) {
            for(Coordinates capture : action.getCaptured())
                if(pawns[capture.getRow()][capture.getColumn()] == KING)
                    return true;
            return blackPawns - action.getCaptured().size() == 0;
        }
        else {
            if(player == KING && isOnPosition(action.getCoordinates(), ESCAPE))
                return true;
            return whitePawns - action.getCaptured().size() == 0;
        }
    }

    public LinkedList<TablutAction> getLegalActions() {
        if(blackWin || whiteWin)
            return new LinkedList<TablutAction>();
        LinkedList<TablutAction> actions = getLegalActions(this.playerTurn);
        if(actions.isEmpty()) {
            if(playerTurn == WHITE)
                blackWin = true;
            else
                whiteWin = true;
        }
        return actions;
    }


    public LinkedList<TablutAction> getLegalActions(byte player) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pawns[i][j] == player || (player == WHITE && pawns[i][j] == KING))
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
            Coordinates captured;
            captured = getCaptured(c, pawn, c.getRow() + 2, c.getColumn());
            if(captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.getRow() - 2, c.getColumn());
            if(captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.getRow(), c.getColumn() + 2);
            if(captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.getRow(), c.getColumn() - 2);
            if(captured != null)
                action.addCapture(captured);
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
        if(pawn == KING) {
            kingPosition = nextPosition;
        }
        if(pawn == KING && isOnPosition(nextPosition, ESCAPE)) {
            whiteWin = true;
        }
        for(Coordinates capture : action.getCaptured()) {
            if(pawns[capture.getRow()][capture.getColumn()] == KING)
                blackWin = true;
            pawns[capture.getRow()][capture.getColumn()] = EMPTY;
            if(playerTurn == WHITE) {
                blackPawns--;
            }
            else
                whitePawns--;
        }
        this.previousAction = action;
        if(this.playerTurn == WHITE)
            this.playerTurn = BLACK;
        else
            this.playerTurn = WHITE;
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
        if(pawn == blocker || cell == CITADEL)
            return true;
        if(cell == CAMP) {
            if((row == 0 && column == 4) || (row == 4 && column == 0) 
                || (row == BOARD_SIZE - 1 && column == 4) || (row == 4 && column == BOARD_SIZE - 1)) {
                return false;
            }
            return true;   
        }
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
        if(r < 3 || r > 5 || c < 3 || c > 5)
            return true;
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