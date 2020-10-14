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

    private static final byte[][] board = initBoard();

    private byte[][] pawns;
    private byte playerTurn;
    private Coordinates kingPosition;

    private boolean whiteWin = false;
    private boolean blackWin = false;
    private boolean draw = false;
    private int blackPawns;
    private int whitePawns;
    private LinkedList<TablutState> drawConditions;

    public TablutState() {
        this.kingPosition = new Coordinates(4, 4);
        this.whitePawns = 9;
        this.blackPawns = 16;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.add(this);
        initPawns();
    }

    public TablutState(byte[][] pawns, byte playerTurn, LinkedList<TablutState> drawConditions) {
        this.pawns = pawns;
        this.playerTurn = playerTurn;
        this.blackPawns = 0;
        this.whitePawns = 0;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.addAll(drawConditions);
        this.drawConditions.add(this);
        initState();
    }

    private TablutState(byte[][] pawns, byte playerTurn, int blackPawns, int whitePawns, Coordinates kingPosition,
            boolean blackWin, boolean whiteWin, LinkedList<TablutState> drawConditions) {
        this.pawns = pawns;
        this.playerTurn = playerTurn;
        this.blackPawns = blackPawns;
        this.whitePawns = whitePawns;
        this.kingPosition = kingPosition;
        this.blackWin = blackWin;
        this.whiteWin = whiteWin;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.addAll(drawConditions);
    }

    public static byte[][] initBoard() {
        byte[][] board = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 1; i < BOARD_SIZE - 1; i++) {
            if (i < 3 || i > 5) {
                board[0][i] = ESCAPE;
                board[i][0] = ESCAPE;
                board[BOARD_SIZE - 1][i] = ESCAPE;
                board[i][BOARD_SIZE - 1] = ESCAPE;
            } else {
                board[0][i] = CAMP;
                board[i][0] = CAMP;
                board[BOARD_SIZE - 1][i] = CAMP;
                board[i][BOARD_SIZE - 1] = CAMP;
            }
        }
        board[1][4] = CAMP;
        board[4][1] = CAMP;
        board[BOARD_SIZE - 2][4] = CAMP;
        board[4][BOARD_SIZE - 2] = CAMP;
        board[4][4] = CITADEL;
        return board;
    }

    private void initState() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pawns[i][j] == BLACK)
                    blackPawns++;
                else if (pawns[i][j] == WHITE)
                    whitePawns++;
                else if (pawns[i][j] == KING) {
                    whitePawns++;
                    kingPosition = new Coordinates(i, j);
                }
            }
        }
        if (kingPosition == null)
            blackWin = true;
        else if (isOnPosition(kingPosition, ESCAPE))
            whiteWin = true;
    }

    public byte[][] getBoard() {
        return pawns;
    }

    public void initPawns() {
        pawns = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 1; i < BOARD_SIZE - 1; i++) {
            if (i < 3) {
                pawns[4][i + 1] = WHITE;
                pawns[i + 1][4] = WHITE;
                pawns[4][BOARD_SIZE - i - 2] = WHITE;
                pawns[BOARD_SIZE - i - 2][4] = WHITE;
            } else if (i <= 5) {
                pawns[0][i] = BLACK;
                pawns[i][0] = BLACK;
                pawns[BOARD_SIZE - 1][i] = BLACK;
                pawns[i][BOARD_SIZE - 1] = BLACK;
            }
        }
        pawns[1][4] = BLACK;
        pawns[4][1] = BLACK;
        pawns[BOARD_SIZE - 2][4] = BLACK;
        pawns[4][BOARD_SIZE - 2] = BLACK;
        pawns[4][4] = KING;
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
        TablutState newState = new TablutState(newPawns, playerTurn, blackPawns, 
            whitePawns, kingPosition, blackWin, whiteWin, drawConditions);
        return newState;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
        TablutState state = (TablutState) obj;
        for(int i = 0 ; i < BOARD_SIZE; i++) 
            for(int j = 0; j < BOARD_SIZE; j++)
                if(pawns[i][j] != state.getPawns()[i][j])
                    return false;
        return true;
    }

    public LinkedList<TablutAction> getBestActionFirst() {
        LinkedList<TablutAction> actions = getLegalActions();
        LinkedList<TablutAction> result = new LinkedList<>();
        boolean loosing = false;
        TablutAction kingAction = null;
        if (playerTurn == BLACK) {
            for (TablutAction ka : getPawnActions(kingPosition, KING))
                if (isOnPosition(ka.getCoordinates(), ESCAPE)) {
                    kingAction = ka;
                    break;
                }
        }
        for (TablutAction action : actions) {
            if (isWin(action)) {
                result = new LinkedList<>();
                result.add(action);
                break;
            } else if (isPreventingLoose(action, kingAction)) {
                result = new LinkedList<>();
                result.add(action);
                loosing = true;
            } else if (!loosing)
                result.addLast(action);
        }
        return result;
    }

    private boolean isPreventingLoose(TablutAction action, TablutAction kingAction) {
        byte player = action.getPawn().getPawnType();

        if (player == BLACK && kingAction != null) {
            int row = action.getCoordinates().getRow();
            int column = action.getCoordinates().getColumn();
            int kre = kingAction.getCoordinates().getRow();
            int krk = kingAction.getPawn().getPosition().getRow();
            int kce = kingAction.getCoordinates().getColumn();
            int kck = kingAction.getPawn().getPosition().getColumn();
            if (row == krk && ((column > kck && column < kce) || (column < kck && column > kce)))
                return true;
            if (column == kck && ((row > krk && row < kre) || (row < krk && row > kre)))
                return true;
        }
        return false;
    }

    private boolean isWin(TablutAction action) {
        byte player = action.getPawn().getPawnType();
        if (player == BLACK) {
            for (Coordinates capture : action.getCaptured())
                if (pawns[capture.getRow()][capture.getColumn()] == KING)
                    return true;
            return blackPawns - action.getCaptured().size() == 0;
        } else {
            if (player == KING && isOnPosition(action.getCoordinates(), ESCAPE))
                return true;
            return whitePawns - action.getCaptured().size() == 0;
        }
    }

    public LinkedList<TablutAction> getLegalActions() {
        if (blackWin || whiteWin)
            return new LinkedList<TablutAction>();
        LinkedList<TablutAction> actions = getLegalActions(this.playerTurn);
        if (actions.isEmpty()) {
            if (playerTurn == WHITE)
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

    private LinkedList<TablutAction> searchActions(Coordinates coord, byte pawn, Function<Integer, Boolean> condition,
            int step, boolean row) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for (int i = (row ? coord.getRow() : coord.getColumn()) + step; condition.apply(i); i += step) {
            TablutAction action;
            Coordinates c = new Coordinates(row ? i : coord.getRow(), !row ? i : coord.getColumn());
            action = new TablutAction(c, new Pawn(pawn, coord));
            if (!isLegal(action))
                break;
            Coordinates captured;
            captured = getCaptured(c, pawn, c.getRow() + 2, c.getColumn());
            if (captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.getRow() - 2, c.getColumn());
            if (captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.getRow(), c.getColumn() + 2);
            if (captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.getRow(), c.getColumn() - 2);
            if (captured != null)
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
        return pawns[nextRow][nextColumn] == EMPTY
                && (board[nextRow][nextColumn] == EMPTY || board[nextRow][nextColumn] == ESCAPE
                        || (board[nextRow][nextColumn] == CAMP && board[row][column] == CAMP && pawn == BLACK));
    }

    public void makeAction(TablutAction action) {
        Coordinates pawnPosition = action.getPawn().getPosition();
        Coordinates nextPosition = action.getCoordinates();
        byte pawn = action.getPawn().getPawnType();
        pawns[pawnPosition.getRow()][pawnPosition.getColumn()] = EMPTY;
        pawns[nextPosition.getRow()][nextPosition.getColumn()] = pawn;
        if (pawn == KING) {
            kingPosition = nextPosition;
        }
        if (pawn == KING && isOnPosition(nextPosition, ESCAPE)) {
            whiteWin = true;
        }
        LinkedList<Coordinates> captures = action.getCaptured();
        if(!captures.isEmpty()) {
            this.drawConditions = new LinkedList<>();
            for (Coordinates capture : captures) {
                if (pawns[capture.getRow()][capture.getColumn()] == KING)
                    blackWin = true;
                pawns[capture.getRow()][capture.getColumn()] = EMPTY;
                if (playerTurn == WHITE) {
                    blackPawns--;
                } else
                    whitePawns--;
            }
        }
        else {
            checkDraw();
        }
        this.drawConditions.add(this);
        if (this.playerTurn == WHITE)
            this.playerTurn = BLACK;
        else
            this.playerTurn = WHITE;
    }

    private void checkDraw() {
        for(TablutState state : drawConditions) {
            if(state.equals(this)) {
                draw = true;
                break;
            }
        }
    }

    private Coordinates getCaptured(Coordinates position, byte pawn, int row, int column) {
        if (row < 0 || column < 0 || row >= BOARD_SIZE || column >= BOARD_SIZE)
            return null;
        if (!isBlocker(row, column, pawn))
            return null;
        Coordinates captured = null;
        if (row == position.getRow())
            if (column > position.getColumn())
                captured = new Coordinates(row, column - 1);
            else
                captured = new Coordinates(row, column + 1);
        else if (column == position.getColumn())
            if (row > position.getRow())
                captured = new Coordinates(row - 1, column);
            else
                captured = new Coordinates(row + 1, column);
        else
            return null;
        if (!isEnemy(pawns[captured.getRow()][captured.getColumn()], pawn))
            return null;

        if (pawns[captured.getRow()][captured.getColumn()] == KING && !kingCaptured(captured))
            return null;
        return captured;
    }

    private boolean isBlocker(int row, int column, byte pawn) {
        byte blocker = pawns[row][column];
        if (blocker == KING)
            blocker = WHITE;
        if (pawn == KING)
            pawn = WHITE;
        byte cell = board[row][column];
        if (pawn == blocker || cell == CITADEL)
            return true;
        if (cell == CAMP) {
            if ((row == 0 && column == 4) || (row == 4 && column == 0) || (row == BOARD_SIZE - 1 && column == 4)
                    || (row == 4 && column == BOARD_SIZE - 1)) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isEnemy(byte enemy, byte pawn) {
        if (enemy == EMPTY)
            return false;
        if (enemy == KING)
            enemy = WHITE;
        if (pawn == KING)
            pawn = WHITE;
        return pawn != enemy;
    }

    private boolean kingCaptured(Coordinates captured) {
        int r = captured.getRow();
        int c = captured.getColumn();
        if (r < 3 || r > 5 || c < 3 || c > 5)
            return true;
        if (board[r][c] == CITADEL)
            return pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK && pawns[r][c + 1] == BLACK
                    && pawns[r][c - 1] == BLACK;
        else if (board[r + 1][c] == CITADEL)
            return pawns[r - 1][c] == BLACK && pawns[r][c + 1] == BLACK && pawns[r][c - 1] == BLACK;
        else if (board[r - 1][c] == CITADEL)
            return pawns[r + 1][c] == BLACK && pawns[r][c + 1] == BLACK && pawns[r][c - 1] == BLACK;
        else if (board[r][c + 1] == CITADEL)
            return pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK && pawns[r][c - 1] == BLACK;
        else if (board[r][c - 1] == CITADEL)
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

	public void setDraw(boolean draw) {
        this.draw = draw;
	}

	public LinkedList<TablutState> getDrawConditions() {
		return this.drawConditions;
	}
}