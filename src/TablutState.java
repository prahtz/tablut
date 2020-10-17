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

    public static final int WHITE_PAWNS = 8;
    public static final int BLACK_PAWNS = 16;

    public static final int THRESHOLD = 0;
    public static final int CAPTURED = 1;
    public static final int KING_WHITE = 2;
    public static final int KING_BLACK = 3;
    public static final int KING_EMPTY = 4;

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

    public TablutState(byte playerTurn) {
        this.playerTurn = playerTurn;
        this.kingPosition = new Coordinates(4, 4);
        this.whitePawns = WHITE_PAWNS;
        this.blackPawns = BLACK_PAWNS;
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
        TablutState newState = new TablutState(newPawns, playerTurn, blackPawns, whitePawns, kingPosition, blackWin,
                whiteWin, drawConditions);
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
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                if (pawns[i][j] != state.getPawns()[i][j])
                    return false;
        return true;
    }

    public LinkedList<TablutAction> getBestActionFirst(int[] weights) {
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
        if(!(result.size() == 1 || loosing == true))
            result = getFinalActions(result, weights);
        if (result.isEmpty()) {
            if (playerTurn == WHITE)
                blackWin = true;
            else
                whiteWin = true;
        }
        return result;
    }

    private LinkedList<TablutAction> getFinalActions(LinkedList<TablutAction> actions, int[] weights) {
        LinkedList<TablutAction> result = new LinkedList<>();
        for (TablutAction action : actions) {
            if (evaluateAction(action, weights))
                result.add(action);
        }
        return result;
    }

    private boolean evaluateAction(TablutAction action, int[] weights) {
        Coordinates newPosition = action.getCoordinates();
        Coordinates oldPosition = action.getPawn().getPosition();
        makeTemporaryAction(action);

        f(action);
        f(action);
        undoTemporaryAction(action);
        return true;
    }

    public void f(TablutAction action) {
        LinkedList<Coordinates> empty = new LinkedList<>();
        Coordinates pos = action.getCoordinates();
        byte enemy = this.playerTurn == WHITE ? BLACK : WHITE;
        int losses = 0;
        if(pos.getRow() > 0 && pos.getRow() < BOARD_SIZE - 1) {
            if(isBlocker(pos.getRow() - 1, pos.getColumn(), enemy) && pawns[pos.getRow() + 1][pos.getColumn()] == EMPTY) {
                losses = losses + g(pos.getRow() + 1, pos.getColumn(), 1, true);
                if(board[pos.getRow() + 1][pos.getColumn()] == EMPTY)
                    empty.add(new Coordinates(pos.getRow() + 1, pos.getColumn()));
            }
            else if(isBlocker(pos.getRow() + 1, pos.getColumn(), enemy) && pawns[pos.getRow() - 1][pos.getColumn()] == EMPTY) {
                losses = losses + g(pos.getRow() - 1, pos.getColumn(), -1, true);
                if(board[pos.getRow() - 1][pos.getColumn()] == EMPTY)
                    empty.add(new Coordinates(pos.getRow() - 1, pos.getColumn()));
            }
        }
        else if(pos.getColumn() > 0 && pos.getColumn() < BOARD_SIZE - 1) {
            if(isBlocker(pos.getRow(), pos.getColumn() - 1, enemy) && pawns[pos.getRow()][pos.getColumn() + 1] == EMPTY) {
                losses = losses + g(pos.getRow(), pos.getColumn() + 1, 1, false);
                if(board[pos.getRow()][pos.getColumn() + 1] == EMPTY)
                    empty.add(new Coordinates(pos.getRow(), pos.getColumn() + 1));
            }
            else if(isBlocker(pos.getRow(), pos.getColumn() + 1, enemy) && pawns[pos.getRow()][pos.getColumn() - 1] == EMPTY) {
                losses = losses + g(pos.getRow(), pos.getColumn() - 1, -1, false);
                if(board[pos.getRow()][pos.getColumn() - 1] == EMPTY)
                    empty.add(new Coordinates(pos.getRow(), pos.getColumn() - 1));
            }
        }
        
        for(Coordinates e : empty) {
            if(h(e, pos, enemy)) {
                losses++;
                break;
            }
        }
        int captures = 0;

        for(int i = pos.getRow() - 1; i >= 0; i--) {
            if(pawns[i][pos.getColumn()] == enemy)
                if(getCaptured(new Coordinates(i + 1, pos.getColumn()), this.playerTurn, i - 1, pos.getColumn()) != null) {
                    captures++;
                    break;
                }
            else if(board[i][pos.getColumn()] != EMPTY)
                break;
        }
        for(int i = pos.getRow() + 1; i < BOARD_SIZE; i++) {
            if(pawns[i][pos.getColumn()] == enemy)
                if(getCaptured(new Coordinates(i - 1, pos.getColumn()), this.playerTurn, i + 1, pos.getColumn()) != null) {
                    captures++;
                    break;
                }
            else if(board[i][pos.getColumn()] != EMPTY)
                break;
        }

        for(int i = pos.getColumn() - 1; i >= 0; i--) {
            if(pawns[pos.getRow()][i] == enemy)
                if(getCaptured(new Coordinates(pos.getRow(), i + 1), this.playerTurn, pos.getRow(), i - 1) != null) {
                    captures++;
                    break;
                }
            else if(board[i][pos.getColumn()] != EMPTY)
                break;
        }

        for(int i = pos.getColumn() + 1; i < BOARD_SIZE; i++) {
            if(pawns[pos.getRow()][i] == enemy)
                if(getCaptured(new Coordinates(pos.getRow(), i - 1), this.playerTurn, pos.getRow(), i + 1) != null) {
                    captures++;
                    break;
                }
            else if(board[i][pos.getColumn()] != EMPTY)
                break;
        }
    }

    public boolean h(Coordinates e, Coordinates pos, byte enemy) {     
        if(!(e.getRow() - 1 == pos.getRow() && e.getColumn() == pos.getColumn())) {
            for(int i = e.getRow() - 1; i >= 0; i--) {
                if(pawns[i][e.getColumn()] == enemy || (this.playerTurn == BLACK && pawns[i][e.getColumn()] == KING)) 
                    return true;
                else if(board[i][e.getColumn()] != EMPTY)
                    break;
            }
        }
        if(!(e.getRow() + 1 == pos.getRow() && e.getColumn() == pos.getColumn())) {
            for(int i = e.getRow() + 1; i < BOARD_SIZE; i++) {
                if(pawns[i][e.getColumn()] == enemy || (this.playerTurn == BLACK && pawns[i][e.getColumn()] == KING)) 
                    return true;
                else if(board[i][e.getColumn()] != EMPTY)
                    break;
            }
        }
        if(!(e.getRow() == pos.getRow() && e.getColumn() - 1 == pos.getColumn())) {
            for(int i = e.getColumn() - 1; i >= 0; i--) {
                if(pawns[e.getRow()][i] == enemy || (this.playerTurn == BLACK && pawns[e.getRow()][i] == KING)) 
                    return true;
                else if(board[e.getRow()][i] != EMPTY)
                    break;
            }
        }
        if(!(e.getRow() == pos.getRow() && e.getColumn() + 1 == pos.getColumn())) {
            for(int i = e.getColumn() + 1; i < BOARD_SIZE; i++) {
                if(pawns[e.getRow()][i]  == enemy || (this.playerTurn == BLACK && pawns[e.getRow()][i] == KING)) 
                    return true;
                else if(board[e.getRow()][i] != EMPTY)
                    break;
            }
        }
        return false;
    }

    public int g(int i, int j, int sign, boolean row) {
        int losses = 0;
        if(board[i][j] == CAMP) {
            if(row) {
                int k = i + sign*2;
                while(k >= 0 && k < BOARD_SIZE && board[k][j] == CAMP && pawns[k][j] == EMPTY) 
                    k = k + sign*1;
                if(k >= 0 && k < BOARD_SIZE && board[k][j] == CAMP)
                    losses++;
            }
            else {
                int k = i + sign*2;
                while(k >= 0 && k < BOARD_SIZE && board[i][k] == CAMP && pawns[i][k] == EMPTY) 
                    k = k + sign*1;
                if(k >= 0 && k < BOARD_SIZE && board[i][k] == CAMP)
                    losses++;
            }  
        }
        return losses;
    }

    private int kingPaths(TablutAction action, byte target) {
        int kingRow = kingPosition.getRow();
        int kingColumn = kingPosition.getColumn();
        pawns[action.getCoordinates().getRow()][action.getCoordinates().getColumn()] = action.getPawn().getPawnType();
        pawns[action.getPawn().getPosition().getRow()][action.getPawn().getPosition().getColumn()] = EMPTY;
        int result = 0;
        if (kingRow > 0 && pawns[kingRow - 1][kingColumn] == target)
            result++;
        else if (kingRow < BOARD_SIZE - 1 && pawns[kingRow + 1][kingColumn] == target)
            result++;
        else if (kingColumn >= 1 && pawns[kingRow][kingColumn - 1] == target)
            result++;
        else if (kingColumn < BOARD_SIZE - 1 && pawns[kingRow][kingColumn + 1] == target)
            result++;

        pawns[action.getCoordinates().getRow()][action.getCoordinates().getColumn()] = EMPTY;
        pawns[action.getPawn().getPosition().getRow()][action.getPawn().getPosition().getColumn()] = action.getPawn().getPawnType();
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
            for (Capture capture : action.getCaptured())
                if (pawns[capture.getCaptured().getPosition().getRow()][capture.getCaptured().getPosition().getColumn()] == KING)
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

    public LinkedList<TablutAction> getAllActions() {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pawns[i][j] != EMPTY)
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
            Capture captured;
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
        LinkedList<Capture> captures = action.getCaptured();
        if (!captures.isEmpty()) {
            this.drawConditions = new LinkedList<>();
            for (Capture capture : captures) {
                if (pawns[capture.getCaptured().getPosition().getRow()][capture.getCaptured().getPosition().getColumn()] == KING)
                    blackWin = true;
                pawns[capture.getCaptured().getPosition().getRow()][capture.getCaptured().getPosition().getColumn()] = EMPTY;
                if (playerTurn == WHITE) {
                    blackPawns--;
                } else
                    whitePawns--;
            }
        } else {
            checkDraw();
        }
        this.drawConditions.add(this);
        if (this.playerTurn == WHITE)
            this.playerTurn = BLACK;
        else
            this.playerTurn = WHITE;
    }

    public void makeTemporaryAction(TablutAction action) {
        pawns[action.getPawn().getPosition().getRow()][action.getPawn().getPosition().getColumn()] = EMPTY;
        pawns[action.getCoordinates().getRow()][action.getCoordinates().getColumn()] = action.getPawn().getPawnType();
        for(Capture capture : action.getCaptured()) 
            pawns[capture.getCaptured().getPosition().getRow()][capture.getCaptured().getPosition().getColumn()] = EMPTY;
    }

    public void undoTemporaryAction(TablutAction action) {
        pawns[action.getCoordinates().getRow()][action.getCoordinates().getColumn()] = EMPTY;
        pawns[action.getPawn().getPosition().getRow()][action.getPawn().getPosition().getColumn()] = action.getPawn().getPawnType();
        for(Capture capture : action.getCaptured()) 
            pawns[capture.getCaptured().getPosition().getRow()][capture.getCaptured().getPosition().getColumn()] = capture.getCaptured().getPawnType(); 
    }

    private void checkDraw() {
        for (TablutState state : drawConditions) {
            if (state.equals(this)) {
                draw = true;
                break;
            }
        }
    }

    private Capture getCaptured(Coordinates position, byte pawn, int row, int column) {
        if (row < 0 || column < 0 || row >= BOARD_SIZE || column >= BOARD_SIZE)
            return null;
        if (!isBlocker(row, column, pawn))
            return null;
        Capture captured = null;
        if (row == position.getRow())
            if (column > position.getColumn())
                captured = new Capture(new Pawn(pawns[row][column - 1], new Coordinates(row, column - 1)));
            else
                captured = new Capture(new Pawn(pawns[row][column + 1], new Coordinates(row, column + 1)));
        else if (column == position.getColumn())
            if (row > position.getRow())
                captured = new Capture(new Pawn(pawns[row - 1][column], new Coordinates(row - 1, column)));
            else
                captured = new Capture(new Pawn(pawns[row + 1][column], new Coordinates(row + 1, column)));
        else
            return null;
        captured.setBlocker(new Pawn(pawn, new Coordinates(row, column)));
        if (!isEnemy(pawns[captured.getCaptured().getPosition().getRow()][captured.getCaptured().getPosition().getColumn()], pawn))
            return null;

        if (pawns[captured.getCaptured().getPosition().getRow()][captured.getCaptured().getPosition().getColumn()] == KING 
            && !kingCaptured(captured.getCaptured().getPosition()))
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

    public int getBlackPawns() {
        return blackPawns;
    }

    public int getWhitePawns() {
        return whitePawns;
    }
}