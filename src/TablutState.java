import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
    private HashMap<Coordinates, ArrayList<LinkedList<TablutAction>>> actionsMap;

    private enum Directions {
        UP(0),
        RIGHT(1),
        DOWN(2),
        LEFT(3);

        private int value;

        private Directions(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public TablutState(byte playerTurn) {
        this.playerTurn = playerTurn;
        this.kingPosition = new Coordinates(4, 4);
        this.whitePawns = WHITE_PAWNS;
        this.blackPawns = BLACK_PAWNS;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.add(this);
        initPawns();
        initActionMap();
    }

    public TablutState(byte playerTurn, byte[][] pawns) {
        this.playerTurn = playerTurn;
        this.kingPosition = new Coordinates(4, 4);
        this.whitePawns = WHITE_PAWNS;
        this.blackPawns = BLACK_PAWNS;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.add(this);
        //initPawns();
        this.pawns = pawns;
        initActionMap();
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

    private TablutState(TablutState state, byte[][] pawns, Coordinates kingPosition, LinkedList<TablutState> drawConditions, HashMap<Coordinates, ArrayList<LinkedList<TablutAction>>> actionsMap) {
        this.pawns = pawns;
        this.playerTurn = state.getPlayerTurn();
        this.blackPawns = state.getBlackPawns();
        this.whitePawns = state.getWhitePawns();
        this.kingPosition = kingPosition;
        this.blackWin = state.isBlackWin();
        this.whiteWin = state.isWhiteWin();
        //this.draw = state.isDraw();
        this.drawConditions = new LinkedList<>();
        this.drawConditions.addAll(drawConditions);
        this.actionsMap = actionsMap;
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

    private void initActionMap() {
        this.actionsMap = new HashMap<>();
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pawns[i][j] != EMPTY)
                    addActions(new Coordinates(i, j), pawns[i][j]);
            }
    }

    private void addActions(Coordinates coord, byte pawn) {
        ArrayList<LinkedList<TablutAction>> actionArray = new ArrayList<>(4);
        actionArray.add(Directions.UP.value(), searchActions(coord, pawn, -1, true));
        actionArray.add(Directions.RIGHT.value(), searchActions(coord, pawn, 1, false));
        actionArray.add(Directions.DOWN.value(), searchActions(coord, pawn, 1, true));
        actionArray.add(Directions.LEFT.value(), searchActions(coord, pawn, -1, false));     
        this.actionsMap.put(coord, actionArray);
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
        HashMap<Coordinates, ArrayList<LinkedList<TablutAction>>> newActionsMap = new HashMap<>();
        
        for (Map.Entry<Coordinates, ArrayList<LinkedList<TablutAction>>> entry : this.actionsMap.entrySet()) {
            ArrayList<LinkedList<TablutAction>> aList = new ArrayList<>(4);
            for(int i = 0; i < 4; i++) {
                LinkedList<TablutAction> list = new LinkedList<>();
                for(TablutAction a : entry.getValue().get(i)) 
                    list.add(a.copy());
                aList.add(i, list);
            }
            newActionsMap.put(entry.getKey(), aList);
        }
        TablutState newState = new TablutState(this, newPawns, kingPosition, drawConditions, newActionsMap);
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

    private void printList(LinkedList<TablutAction> l) {
        for(TablutAction a : l)
            System.out.println(a.toString());
    }

    private void updateActionMap(TablutAction action) {
        this.actionsMap.remove(action.pawn.position);
        for(Capture c: action.getCaptured()) {
            this.actionsMap.remove(c.getCaptured().position);
            //Directions d = getDirection(action.coordinates, c.getCaptured().position);
            updatePawnActionsByCoordinates(c.getCaptured().position);
        }

        ArrayList<LinkedList<TablutAction>> list = new ArrayList<>(4);
        list.add(Directions.UP.value(), searchActions(action.coordinates, action.pawn.getPawnType(), -1, true));
        list.add(Directions.RIGHT.value(), searchActions(action.coordinates, action.pawn.getPawnType(), 1, false));
        list.add(Directions.DOWN.value(), searchActions(action.coordinates, action.pawn.getPawnType(), 1, true));
        list.add(Directions.LEFT.value(), searchActions(action.coordinates, action.pawn.getPawnType(), -1, false));
        this.actionsMap.remove(action.coordinates);
        this.actionsMap.put(action.coordinates, list);

        updatePawnActionsByCoordinates(action.coordinates);


        updateOnPawnCapturesByCoordinates(action.coordinates);

        Directions direction = getDirection(action.pawn.position, action.coordinates);
        updateNewActionsByCoordinates(action.pawn.position, direction);

        updateNewCapturesByCoordinates(action.coordinates);
        removePlayerCapturesByCoordinates(action.pawn.position);
    }

    private void updateNewCapturesByCoordinates(Coordinates coordinates) {
        int row = coordinates.row;
        int column = coordinates.column;
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return isEnemy(getValue(pawns, c), this.playerTurn == WHITE ? BLACK : WHITE);}; //??
   
        Coordinates result;
        if(row + 2 < BOARD_SIZE && isEnemy(pawns[row + 1][column], this.playerTurn) && pawns[row + 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row + 2, column);
            Coordinates captured = new Coordinates(row + 1, column);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.RIGHT);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.DOWN);     
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.LEFT);     
        }
        if(row - 2 >= 0 && isEnemy(pawns[row - 1][column], this.playerTurn) && pawns[row - 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row - 2, column);
            Coordinates captured = new Coordinates(row - 1, column);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.RIGHT);     
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.LEFT);
        }
        if(column + 2 < BOARD_SIZE && isEnemy(pawns[row][column + 1], this.playerTurn) && pawns[row][column + 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column + 2);
            Coordinates captured = new Coordinates(row, column + 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.RIGHT);     
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.DOWN);
        }
        if(column - 2 >= 0 && isEnemy(pawns[row][column - 1], this.playerTurn) && pawns[row][column - 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column - 2);
            Coordinates captured = new Coordinates(row, column - 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.LEFT);     
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.DOWN);
        }
    }

    private void removePlayerCapturesByCoordinates(Coordinates coordinates) {
        int row = coordinates.row;
        int column = coordinates.column;
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return isEnemy(getValue(pawns, c), this.playerTurn == WHITE ? BLACK : WHITE);}; //??

        Coordinates result;
        if(row + 2 < BOARD_SIZE && isEnemy(pawns[row + 1][column], this.playerTurn) && pawns[row + 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row + 2, column);
            Coordinates captured = new Coordinates(row + 1, column);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.RIGHT);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.DOWN);     
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.LEFT);     
        }
        if(row - 2 >= 0 && isEnemy(pawns[row - 1][column], this.playerTurn) && pawns[row - 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row - 2, column);
            Coordinates captured = new Coordinates(row - 1, column);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.RIGHT);     
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.LEFT);
        }
        if(column + 2 < BOARD_SIZE && isEnemy(pawns[row][column + 1], this.playerTurn) && pawns[row][column + 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column + 2);
            Coordinates captured = new Coordinates(row, column + 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.RIGHT);     
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.DOWN);
        }
        if(column - 2 >= 0 && isEnemy(pawns[row][column - 1], this.playerTurn) && pawns[row][column - 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column - 2);
            Coordinates captured = new Coordinates(row, column - 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.LEFT);     
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.DOWN);
        }
    }

    private void updateNewActionsByCoordinates(Coordinates position, Directions direction) {
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return getValue(pawns, c) != EMPTY;};
        Coordinates result;
        if(direction == Directions.UP || direction == Directions.DOWN) {
            result = searchByDirection(position, Directions.LEFT, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.LEFT);
            result = searchByDirection(position, Directions.RIGHT, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.RIGHT);            
        }
        else {
            result = searchByDirection(position, Directions.UP, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.UP);
            result = searchByDirection(position, Directions.DOWN, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.DOWN);    
        }
    }

    private void updatePawnActionsByCoordinates(Coordinates coordinates) {
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return getValue(pawns, c) != EMPTY;};
        Coordinates result;
        for(Directions dir : Directions.values()) {
            result = searchByDirection(coordinates, dir, condition);
            if(result != null) updateActionsByCoordinates(result, dir);
        }
    }

    private Directions getDirection(Coordinates prev, Coordinates next) {
        if(prev.row == next.row) {
            if(prev.column > next.column)
                return Directions.LEFT;
            else
                return Directions.RIGHT;
        }
        else {
            if(prev.row > next.row)
                return Directions.UP;
            else
                return Directions.DOWN;
        }
    }

    private void updateOnPawnCapturesByCoordinates(Coordinates coordinates) {
        int row = coordinates.row;
        int column = coordinates.column;
        byte enemy = this.playerTurn == WHITE ? BLACK : WHITE;
        Coordinates result;
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return isEnemy(getValue(pawns, c), this.playerTurn);}; //??
        if(column - 1 >= 0 && isBlocker(row, column - 1, enemy) && column + 1 < BOARD_SIZE && pawns[row][column + 1] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row, column + 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.DOWN);     
        }
        if(column + 1 < BOARD_SIZE && isBlocker(row, column + 1, enemy) && column - 1 >= 0 && pawns[row][column - 1] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row, column - 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.UP);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.DOWN);
        }
        if(row - 1 >= 0 && isBlocker(row - 1, column, enemy) && row + 1 < BOARD_SIZE && pawns[row + 1][column] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row + 1, column);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.LEFT);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.RIGHT);
        }
        if(row + 1 < BOARD_SIZE && isBlocker(row + 1, column, enemy) && row - 1 >= 0 && pawns[row - 1][column] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row - 1, column);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.LEFT);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.RIGHT);
        }
    }

    

    private void updateCaptures(Coordinates captured, Coordinates enemyPos, Coordinates emptyPos, Directions direction) {
        TablutAction tempAction = new TablutAction(new Coordinates(emptyPos.row, emptyPos.column), new Pawn(getValue(pawns, enemyPos), enemyPos));
        ArrayList<LinkedList<TablutAction>> actionContainer = this.actionsMap.get(enemyPos);
        if(actionContainer == null) {
            actionContainer = new ArrayList<>(4);
            for(int j = 0; j < 4; j++) 
                actionContainer.add(j, new LinkedList<>());
        }
        for(TablutAction a : actionContainer.get(oppositeDirection(direction).value())) {
            if(a.equals(tempAction)) {
                Capture c = new Capture(new Pawn(getValue(pawns, captured), captured));
                if(isCapture(c))
                    a.addCapture(c);
                break;
            }
        }
        this.actionsMap.remove(enemyPos);
        this.actionsMap.put(enemyPos, actionContainer);
    }

    private void removeCaptures(Coordinates captured, Coordinates enemyPos, Coordinates emptyPos, Directions direction) {
        TablutAction tempAction = new TablutAction(new Coordinates(emptyPos.row, emptyPos.column), new Pawn(getValue(pawns, enemyPos), enemyPos));
        ArrayList<LinkedList<TablutAction>> actionContainer = this.actionsMap.get(enemyPos);
        if(actionContainer == null) {
            actionContainer = new ArrayList<>(4);
            for(int j = 0; j < 4; j++) 
                actionContainer.add(j, new LinkedList<>());
        }
        for(TablutAction a : actionContainer.get(oppositeDirection(direction).value())) {
            if(a.equals(tempAction)) {
                Capture c = new Capture(new Pawn(getValue(pawns, captured), captured));
                if(isCapture(c))
                    a.removeCapture(c);
                break;
            }
        }
        this.actionsMap.remove(enemyPos);
        this.actionsMap.put(enemyPos, actionContainer);
    }

    private Coordinates searchByDirection(Coordinates pos, Directions direction,
            Function<Coordinates, Boolean> condition) {
        int row = pos.row;
        int column = pos.column;
        Coordinates coord = new Coordinates(row, column);
        boolean camp = false, citadel = false;
        int step = direction == Directions.UP || direction == Directions.LEFT ? -1 : 1;
        for(int i = (direction == Directions.UP || direction == Directions.DOWN ? row + step : column + step); step > 0 ? i < BOARD_SIZE : i >= 0; i += step) {
            if(direction == Directions.UP || direction == Directions.DOWN)
                coord.row = i;
            else 
                coord.column = i; 
            if(getValue(board, coord) == CAMP) 
                camp = true;
            else if(getValue(board, coord) == CITADEL)
                citadel = true;
            else if(getValue(board, coord) == EMPTY && (camp || citadel))
                break; 
            if(condition.apply(coord)) {
                return coord;
            }
        }
        return null;
    }

    private void updateActionsByCoordinates(Coordinates coord, Directions direction) {
        int step = direction == Directions.UP || direction == Directions.LEFT ? 1 : -1;
        ArrayList<LinkedList<TablutAction>> actionContainer = this.actionsMap.get(coord);
        if(actionContainer == null) {
            actionContainer = new ArrayList<>(4);
            for(int j = 0; j < 4; j++) 
                actionContainer.add(j, new LinkedList<>());
        }
        actionContainer.set(oppositeDirection(direction).value(), searchActions(coord, getValue(pawns, coord), step, direction == Directions.UP || direction == Directions.DOWN ? true : false));
        this.actionsMap.remove(coord);
        this.actionsMap.put(coord, actionContainer);
    }

    private Directions oppositeDirection(Directions direction) {
        if(direction == Directions.UP)
            return Directions.DOWN;
        if(direction == Directions.DOWN)
            return Directions.UP;
        if(direction == Directions.LEFT)
            return Directions.RIGHT;
        return Directions.LEFT;
    }

    private byte getValue(byte[][] b, Coordinates c) {
        return b[c.row][c.column];
    }

    private boolean isCapture(Capture captured) {
        if (pawns[captured.getCaptured().position.row][captured.getCaptured().position.column] == KING 
            && !kingCaptured(captured.getCaptured().position))
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
                if (isOnPosition(ka.coordinates, ESCAPE)) {
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
        Coordinates newPosition = action.coordinates;
        Coordinates oldPosition = action.pawn.position;
        return true;
    }

    private boolean isPreventingLoose(TablutAction action, TablutAction kingAction) {
        byte player = action.pawn.getPawnType();

        if (player == BLACK && kingAction != null) {
            int row = action.coordinates.row;
            int column = action.coordinates.column;
            int kre = kingAction.coordinates.row;
            int krk = kingAction.pawn.position.row;
            int kce = kingAction.coordinates.column;
            int kck = kingAction.pawn.position.column;
            if (row == krk && ((column > kck && column < kce) || (column < kck && column > kce)))
                return true;
            if (column == kck && ((row > krk && row < kre) || (row < krk && row > kre)))
                return true;
        }
        return false;
    }

    private boolean isWin(TablutAction action) {
        byte player = action.pawn.getPawnType();
        if (player == BLACK) {
            for (Capture capture : action.getCaptured())
                if (pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] == KING)
                    return true;
            return blackPawns - action.getCaptured().size() == 0;
        } else {
            if (player == KING && isOnPosition(action.coordinates, ESCAPE))
                return true;
            return whitePawns - action.getCaptured().size() == 0;
        }
    }

    public LinkedList<TablutAction> getLegalActions() {
        if (blackWin || whiteWin)
            return new LinkedList<TablutAction>();
        return getLegalActions(this.playerTurn);
    }

    public LinkedList<TablutAction> getLegalActions(byte player) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for(Map.Entry<Coordinates, ArrayList<LinkedList<TablutAction>>> entry : this.actionsMap.entrySet()) {
            Coordinates c = entry.getKey();
            if(getValue(pawns, c) == player || (player == WHITE && getValue(pawns, c) == KING))
                for(LinkedList<TablutAction> l : entry.getValue()) {
                    if(l == null)
                        System.out.println("NULL");
                    actions.addAll(l);
                }
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
        actions.addAll(searchActions(coord, pawn, -1, true));
        actions.addAll(searchActions(coord, pawn, -1, false));
        actions.addAll(searchActions(coord, pawn, 1, true));
        actions.addAll(searchActions(coord, pawn, 1, false));
        return actions;
    }

    private LinkedList<TablutAction> searchActions(Coordinates coord, byte pawn, int step, boolean row) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for (int i = (row ? coord.row : coord.column) + step; (step > 0 ? i < BOARD_SIZE : i >= 0) ; i += step) {
            TablutAction action;
            Coordinates c = new Coordinates(row ? i : coord.row, !row ? i : coord.column);
            action = new TablutAction(c, new Pawn(pawn, coord));
            if (!isLegal(action))
                break;
            Capture captured;
            captured = getCaptured(c, pawn, c.row + 2, c.column);
            if (captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.row - 2, c.column);
            if (captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.row, c.column + 2);
            if (captured != null)
                action.addCapture(captured);
            captured = getCaptured(c, pawn, c.row, c.column - 2);
            if (captured != null)
                action.addCapture(captured);
            actions.add(action);
        }
        return actions;
    }

    private boolean isLegal(TablutAction action) {
        int nextRow = action.coordinates.row;
        int nextColumn = action.coordinates.column;
        int row = action.pawn.position.row;
        int column = action.pawn.position.column;
        byte pawn = action.pawn.getPawnType();
        return pawns[nextRow][nextColumn] == EMPTY
                && (board[nextRow][nextColumn] == EMPTY || board[nextRow][nextColumn] == ESCAPE
                        || (board[nextRow][nextColumn] == CAMP && board[row][column] == CAMP && pawn == BLACK));
    }

    public void makeAction(TablutAction action) {
        Coordinates pawnPosition = action.pawn.position;
        Coordinates nextPosition = action.coordinates;
        byte pawn = action.pawn.getPawnType();
        pawns[pawnPosition.row][pawnPosition.column] = EMPTY;
        pawns[nextPosition.row][nextPosition.column] = pawn;
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
                if (pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] == KING)
                    blackWin = true;
                pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] = EMPTY;
                if (playerTurn == WHITE) {
                    blackPawns--;
                } else
                    whitePawns--;
            }
        } else {
            checkDraw();
        }
        updateActionMap(action);
        this.drawConditions.add(this);
        if (this.playerTurn == WHITE)
            this.playerTurn = BLACK;
        else
            this.playerTurn = WHITE;
    }

    public void makeTemporaryAction(TablutAction action) {
        pawns[action.pawn.position.row][action.pawn.position.column] = EMPTY;
        pawns[action.coordinates.row][action.coordinates.column] = action.pawn.getPawnType();
        for(Capture capture : action.getCaptured()) 
            pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] = EMPTY;
    }

    public void undoTemporaryAction(TablutAction action) {
        pawns[action.coordinates.row][action.coordinates.column] = EMPTY;
        pawns[action.pawn.position.row][action.pawn.position.column] = action.pawn.getPawnType();
        for(Capture capture : action.getCaptured()) 
            pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] = capture.getCaptured().getPawnType(); 
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
        if (row == position.row)
            if (column > position.column)
                captured = new Capture(new Pawn(pawns[row][column - 1], new Coordinates(row, column - 1)));
            else
                captured = new Capture(new Pawn(pawns[row][column + 1], new Coordinates(row, column + 1)));
        else if (column == position.column)
            if (row > position.row)
                captured = new Capture(new Pawn(pawns[row - 1][column], new Coordinates(row - 1, column)));
            else
                captured = new Capture(new Pawn(pawns[row + 1][column], new Coordinates(row + 1, column)));
        else
            return null;
        captured.setBlocker(new Pawn(pawn, new Coordinates(row, column)));
        if (!isEnemy(pawns[captured.getCaptured().position.row][captured.getCaptured().position.column], pawn))
            return null;

        if (pawns[captured.getCaptured().position.row][captured.getCaptured().position.column] == KING 
            && !kingCaptured(captured.getCaptured().position))
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
        int r = captured.row;
        int c = captured.column;
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
        return board[coordinates.row][coordinates.column] == position;
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