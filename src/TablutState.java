import java.util.ArrayList;
import java.util.Collections;
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
    private HashMap<Capture, LinkedList<Coordinates>> capturesMap;

    private int newActiveCaptures;
    private boolean firstMove = false;

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
        if(playerTurn == WHITE)
            this.firstMove = true;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.add(this);
        initPawns();
        initActionMap();
    }

    public TablutState(byte playerTurn, byte[][] pawns) {
        this.pawns = pawns;
        this.playerTurn = playerTurn;
        this.kingPosition = getKingPosition();
        this.whitePawns = WHITE_PAWNS;
        this.blackPawns = BLACK_PAWNS;
        this.firstMove = false;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.add(this);
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

    private TablutState(TablutState state, byte[][] pawns, Coordinates kingPosition, LinkedList<TablutState> drawConditions, 
        HashMap<Coordinates, ArrayList<LinkedList<TablutAction>>> actionsMap) {
        this.pawns = pawns;
        this.playerTurn = state.getPlayerTurn();
        this.blackPawns = state.getBlackPawns();
        this.whitePawns = state.getWhitePawns();
        this.draw = state.isDraw();
        this.kingPosition = kingPosition;
        this.blackWin = state.isBlackWin();
        this.whiteWin = state.isWhiteWin();
        this.drawConditions = new LinkedList<>();
        this.drawConditions.addAll(drawConditions);
        this.actionsMap = actionsMap;
    }

    private Coordinates getKingPosition() {
        for(int i = 0; i < BOARD_SIZE; i++)
            for(int j = 0; j < BOARD_SIZE; j++)
                if(pawns[i][j] == KING)
                    return new Coordinates(i, j);
        return null;
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
        this.capturesMap = new HashMap<>();
        this.actionsMap = new HashMap<>();
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pawns[i][j] != EMPTY)
                    addActions(new Coordinates(i, j), pawns[i][j]);
            }
    }

    //TODO - change .add to .set in some way -> .add is not constant time!!!
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

    public TablutState copySimulation() {
        byte[][] newPawns = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                newPawns[i][j] = pawns[i][j];
        TablutState newState = new TablutState(this, newPawns, kingPosition, drawConditions, actionsMap);
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

    public void printList(LinkedList<TablutAction> l) {
        for(TablutAction a : l)
            System.out.println(a.toString());
    }

    private void updateActionMap(TablutAction action, boolean temp) {  
        if(temp) {
            captureMapCopy.remove(new Capture(action.pawn));
            ArrayList<LinkedList<TablutAction>> previousActions = actionsMap.get(action.pawn.position);
            for(LinkedList<TablutAction> list : previousActions) {
                for(TablutAction a : list) {
                    for(Capture c : a.getCaptured()) {
                        updateCaptureMap(captureMapCopy, c, a.pawn.position, false);
                    }
                }
            }
            for(Capture c: action.getCaptured()) {
                newActiveCaptures++;
                captureMapCopy.remove(c);
                for(LinkedList<TablutAction> list : actionsMap.get(c.getCaptured().position)) {
                    for(TablutAction a : list) {
                        for(Capture c1 : a.getCaptured()) {
                            updateCaptureMap(captureMapCopy, c1, a.pawn.position, false);
                        }
                    }
                }
                Directions d = getDirection(action.coordinates, c.getCaptured().position);
                actionsTowardsCoordinates(c.getCaptured().position, getOtherAxisDirections(d), temp); 
                capturesTowardsCoordinates(c.getCaptured().position, c.getCaptured().getPawnType(), temp);
                removeCapturesAssumingCoordinatesBlocker(c.getCaptured().position, c.getCaptured().getPawnType(), oppositeDirection(d), temp);
            }
            countCaptures(action.coordinates, action.pawn.getPawnType(), Directions.UP);
            countCaptures(action.coordinates, action.pawn.getPawnType(), Directions.RIGHT);
            countCaptures(action.coordinates, action.pawn.getPawnType(), Directions.DOWN);
            countCaptures(action.coordinates, action.pawn.getPawnType(), Directions.LEFT); 
        }
        else {
            this.actionsMap.remove(action.pawn.position);
            for(Capture c: action.getCaptured()) {
                this.actionsMap.remove(c.getCaptured().position);
                Directions d = getDirection(action.coordinates, c.getCaptured().position);
                actionsTowardsCoordinates(c.getCaptured().position, getOtherAxisDirections(d), temp); 
                capturesTowardsCoordinates(c.getCaptured().position, c.getCaptured().getPawnType(), temp);
                removeCapturesAssumingCoordinatesBlocker(c.getCaptured().position, c.getCaptured().getPawnType(), oppositeDirection(d), temp);
            }
            ArrayList<LinkedList<TablutAction>> list = new ArrayList<>(4);
            list.add(Directions.UP.value(), searchActions(action.coordinates, action.pawn.getPawnType(), -1, true));
            list.add(Directions.RIGHT.value(), searchActions(action.coordinates, action.pawn.getPawnType(), 1, false));
            list.add(Directions.DOWN.value(), searchActions(action.coordinates, action.pawn.getPawnType(), 1, true));
            list.add(Directions.LEFT.value(), searchActions(action.coordinates, action.pawn.getPawnType(), -1, false));
            this.actionsMap.remove(action.coordinates);
            this.actionsMap.put(action.coordinates, list);
        }

        actionsTowardsCoordinates(action.coordinates, Directions.values(), temp);
        capturesTowardsCoordinates(action.coordinates, getValue(pawns, action.coordinates), temp);

        Directions direction = getDirection(action.pawn.position, action.coordinates);
        actionsTowardsCoordinatesNotOnDirection(action.pawn.position, direction, temp);
        capturesTowardsCoordinates(action.pawn.position, getValue(pawns, action.coordinates), temp);

        capturesAssumingCoordinatesBlocker(action.coordinates, temp);
        removeCapturesAssumingCoordinatesBlocker(action.pawn.position, getValue(pawns, action.coordinates), null, temp);
    }

    public void updateCaptureMap(HashMap<Capture, LinkedList<Coordinates>> capMap, Capture cap, Coordinates attacker, boolean add) {
        LinkedList<Coordinates> attackerList = capMap.get(cap);
        if(attackerList == null) {
            attackerList = new LinkedList<>();
            capMap.put(cap, attackerList);
        }
        if(add)
            attackerList.add(attacker);
        else
            attackerList.remove(attacker);
        if(attackerList.isEmpty())
            capMap.remove(cap);
    }

    private Directions[] getOtherAxisDirections(Directions dir) {
        if(dir == Directions.DOWN || dir == Directions.UP)
            return new Directions[]{Directions.RIGHT, Directions.LEFT};
        else 
            return new Directions[]{Directions.UP, Directions.DOWN};
    }

    private void capturesAssumingCoordinatesBlocker(Coordinates coordinates, boolean temp) {
        int row = coordinates.row;
        int column = coordinates.column;
        if ((row == 0 && column == 4) || (row == 4 && column == 0) || (row == BOARD_SIZE - 1 && column == 4)
            || (row == 4 && column == BOARD_SIZE - 1)) {
            return;
        }
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return isEnemy(getValue(pawns, c), getValue(pawns, coordinates) == BLACK ? WHITE : BLACK);}; //??
        Coordinates result;
        if(isBoardBlocker(coordinates))
            return;
        if(row + 2 < BOARD_SIZE && isEnemy(pawns[row + 1][column], getValue(pawns, coordinates)) && pawns[row + 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row + 2, column);
            Coordinates captured = new Coordinates(row + 1, column);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.RIGHT, getValue(pawns, captured), temp);  
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.LEFT, getValue(pawns, captured), temp);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.DOWN, getValue(pawns, captured), temp);     
        }
        if(row - 2 >= 0 && isEnemy(pawns[row - 1][column], getValue(pawns, coordinates)) && pawns[row - 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row - 2, column);
            Coordinates captured = new Coordinates(row - 1, column);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.RIGHT, getValue(pawns, captured), temp);     
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.LEFT, getValue(pawns, captured), temp);   
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.UP, getValue(pawns, captured), temp);  
        }
        if(column + 2 < BOARD_SIZE && isEnemy(pawns[row][column + 1], getValue(pawns, coordinates)) && pawns[row][column + 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column + 2);
            Coordinates captured = new Coordinates(row, column + 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.UP, getValue(pawns, captured), temp);  
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.DOWN, getValue(pawns, captured), temp);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.RIGHT, getValue(pawns, captured), temp);  
        }
        if(column - 2 >= 0 && isEnemy(pawns[row][column - 1], getValue(pawns, coordinates)) && pawns[row][column - 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column - 2);
            Coordinates captured = new Coordinates(row, column - 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.UP, getValue(pawns, captured), temp);   
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.DOWN, getValue(pawns, captured), temp);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(captured, result, emptyPos, Directions.LEFT, getValue(pawns, captured), temp);  
        }
    }

    private void removeCapturesAssumingCoordinatesBlocker(Coordinates coordinates, byte oldPawn, 
            Directions direction, boolean temp) {
        int row = coordinates.row;
        int column = coordinates.column;

        if ((row == 0 && column == 4) || (row == 4 && column == 0) || (row == BOARD_SIZE - 1 && column == 4)
                    || (row == 4 && column == BOARD_SIZE - 1)) {
                return;
        }
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return isEnemy(getValue(pawns, c), oldPawn == BLACK ? WHITE : BLACK);}; //??
        if(isBoardBlocker(coordinates))
            return;
        Coordinates result;
        if(direction != Directions.DOWN && row + 2 < BOARD_SIZE && isEnemy(pawns[row + 1][column], oldPawn) && pawns[row + 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row + 2, column);
            Coordinates captured = new Coordinates(row + 1, column);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.RIGHT, temp);  
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.LEFT, temp);  
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.DOWN, temp);   
        }
        if(direction != Directions.UP && row - 2 >= 0 && isEnemy(pawns[row - 1][column], oldPawn) && pawns[row - 2][column] == EMPTY){
            Coordinates emptyPos = new Coordinates(row - 2, column);
            Coordinates captured = new Coordinates(row - 1, column);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.RIGHT, temp);     
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.LEFT, temp);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.UP, temp);   
        }
        if(direction != Directions.RIGHT && column + 2 < BOARD_SIZE && isEnemy(pawns[row][column + 1], oldPawn) && pawns[row][column + 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column + 2);
            Coordinates captured = new Coordinates(row, column + 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.UP, temp);   
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.DOWN, temp);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.RIGHT, temp);
        }
        if(direction != Directions.LEFT && column - 2 >= 0 && isEnemy(pawns[row][column - 1], oldPawn) && pawns[row][column - 2] == EMPTY){
            Coordinates emptyPos = new Coordinates(row, column - 2);
            Coordinates captured = new Coordinates(row, column - 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.UP, temp);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.DOWN, temp);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) removeCaptures(captured, result, emptyPos, Directions.LEFT, temp);
        }
    }

    private void actionsTowardsCoordinatesNotOnDirection(Coordinates position, Directions direction, boolean temp) {
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return getValue(pawns, c) != EMPTY;};
        Coordinates result;
        if(direction == Directions.UP || direction == Directions.DOWN) {
            result = searchByDirection(position, Directions.LEFT, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.LEFT, position, temp);
            result = searchByDirection(position, Directions.RIGHT, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.RIGHT, position, temp);            
        }
        else {
            result = searchByDirection(position, Directions.UP, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.UP, position, temp);
            result = searchByDirection(position, Directions.DOWN, condition);
            if(result != null) updateActionsByCoordinates(result, Directions.DOWN, position, temp);    
        }
    }

    private void actionsTowardsCoordinates(Coordinates coordinates, Directions[] directions, boolean temp) {
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return getValue(pawns, c) != EMPTY;};
        Coordinates result;
        for(Directions dir : directions) {
            result = searchByDirection(coordinates, dir, condition);
            if(result != null) 
                updateActionsByCoordinates(result, dir, coordinates, temp);
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

    private void capturesTowardsCoordinates(Coordinates coordinates, byte captured, boolean temp) {
        int row = coordinates.row;
        int column = coordinates.column;
        if ((row == 0 && column == 4) || (row == 4 && column == 0) || (row == BOARD_SIZE - 1 && column == 4)
                    || (row == 4 && column == BOARD_SIZE - 1)) {
                return ;
        }
        byte enemy = captured == BLACK ? WHITE : BLACK;
        Coordinates result;
        Function<Coordinates, Boolean> condition = (Coordinates c) -> {return isEnemy(getValue(pawns, c), captured);}; //??
        if(column - 1 >= 0 && isBlocker(row, column - 1, enemy) && column + 1 < BOARD_SIZE && pawns[row][column + 1] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row, column + 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.UP, captured, temp);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.DOWN, captured, temp);     
        }
        if(column + 1 < BOARD_SIZE && isBlocker(row, column + 1, enemy) && column - 1 >= 0 && pawns[row][column - 1] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row, column - 1);
            result = searchByDirection(emptyPos, Directions.UP, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.UP, captured, temp);
            result = searchByDirection(emptyPos, Directions.DOWN, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.DOWN, captured, temp);
        }
        if(row - 1 >= 0 && isBlocker(row - 1, column, enemy) && row + 1 < BOARD_SIZE && pawns[row + 1][column] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row + 1, column);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.LEFT, captured, temp);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.RIGHT, captured, temp);
        }
        if(row + 1 < BOARD_SIZE && isBlocker(row + 1, column, enemy) && row - 1 >= 0 && pawns[row - 1][column] == EMPTY) {
            Coordinates emptyPos = new Coordinates(row - 1, column);
            result = searchByDirection(emptyPos, Directions.LEFT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.LEFT, captured, temp);
            result = searchByDirection(emptyPos, Directions.RIGHT, condition);
            if(result != null) updateCaptures(coordinates, result, emptyPos, Directions.RIGHT, captured, temp);
        }
    }

    

    private void updateCaptures(Coordinates capturedPos, Coordinates enemyPos, Coordinates emptyPos, Directions direction, byte captured ,boolean temp) {
        if(temp) {
            Capture c = new Capture(new Pawn(getValue(pawns, capturedPos), capturedPos));
            if(c.getCaptured().getPawnType() != EMPTY && !isCapture(c, getValue(pawns, enemyPos), emptyPos, enemyPos))
                return;
            
            if(c.getCaptured().getPawnType() == EMPTY) {
                if(!(captured == KING && !kingCaptured(capturedPos))) {
                    updateCaptureMap(captureMapCopy, c, enemyPos, false);
                }
            }
            else {
                updateCaptureMap(captureMapCopy, c, enemyPos, true);
                if(tempAction.coordinates.equals(capturedPos)) {
                    this.willBeCaptured = true;
                }
            }
        }
        else {
            TablutAction tempAction = new TablutAction(new Coordinates(emptyPos.row, emptyPos.column), new Pawn(getValue(pawns, enemyPos), enemyPos));
            Capture c = new Capture(new Pawn(getValue(pawns, capturedPos), capturedPos));
            if(c.getCaptured().getPawnType() != EMPTY && !isCapture(c, getValue(pawns, enemyPos), emptyPos, enemyPos))
                return;
            ArrayList<LinkedList<TablutAction>> actionContainer = this.actionsMap.get(enemyPos);
            if(actionContainer == null) {
                actionContainer = new ArrayList<>(4);
                for(int j = 0; j < 4; j++) 
                    actionContainer.add(j, new LinkedList<>());
            }
            
            for(TablutAction a : actionContainer.get(oppositeDirection(direction).value())) {
                if(a.equals(tempAction)) {
                    if(c.getCaptured().getPawnType() == EMPTY) {
                        if(!(captured == KING && !kingCaptured(capturedPos))) {
                            a.removeCapture(c);
                        }
                    }
                    else {
                        a.addCapture(c);
                    }
                    break;
                }
            }
            this.actionsMap.remove(enemyPos);
            this.actionsMap.put(enemyPos, actionContainer);
        }
    }

    private void removeCaptures(Coordinates captured, Coordinates enemyPos, Coordinates emptyPos, Directions direction, boolean temp) {
        if(temp) {
            Capture c = new Capture(new Pawn(getValue(pawns, captured), captured));     
            if(!isCapture(c, getValue(pawns, enemyPos), emptyPos, enemyPos))
                return;
            updateCaptureMap(captureMapCopy, c, enemyPos, false);
        }
        else {
            TablutAction tempAction = new TablutAction(new Coordinates(emptyPos.row, emptyPos.column), new Pawn(getValue(pawns, enemyPos), enemyPos));
            Capture c = new Capture(new Pawn(getValue(pawns, captured), captured));     
            if(!isCapture(c, getValue(pawns, enemyPos), emptyPos, enemyPos))
                return;
            ArrayList<LinkedList<TablutAction>> actionContainer = this.actionsMap.get(enemyPos);
            if(actionContainer == null) {
                actionContainer = new ArrayList<>(4);
                for(int j = 0; j < 4; j++) 
                    actionContainer.add(j, new LinkedList<>());
            }
            for(TablutAction a : actionContainer.get(oppositeDirection(direction).value())) {
                if(a.equals(tempAction)) {
                    a.removeCapture(c);
                    break;
                }
            }
            this.actionsMap.remove(enemyPos);
            this.actionsMap.put(enemyPos, actionContainer);
        }
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
            else if((getValue(board, coord) == EMPTY || getValue(board, coord) == ESCAPE) && (camp || citadel))
                break; 
            if(condition.apply(coord)) {
                return coord;
            }
            if(getValue(pawns, coord) != EMPTY) {
                break;
            }
        }
        return null;
    }

    private void updateActionsByCoordinates(Coordinates toUpdate, Directions direction, Coordinates towards, boolean temp) {
        int step = direction == Directions.UP || direction == Directions.LEFT ? 1 : -1;
        if(temp) {
            countCaptures(toUpdate, getValue(pawns, toUpdate), oppositeDirection(direction));
        }
        else{
            ArrayList<LinkedList<TablutAction>> actionContainer = this.actionsMap.get(toUpdate);
            if(actionContainer == null) {
                actionContainer = new ArrayList<>(4);
                for(int j = 0; j < 4; j++) 
                    actionContainer.add(j, new LinkedList<>());
            }
            actionContainer.set(oppositeDirection(direction).value(), searchActions(toUpdate, getValue(pawns, toUpdate), step, direction == Directions.UP || direction == Directions.DOWN ? true : false));
            this.actionsMap.remove(toUpdate);
            this.actionsMap.put(toUpdate, actionContainer);
        }
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

    private boolean isCapture(Capture captured, byte playerCapturing, Coordinates emptyPos, Coordinates enemyPos) {
        if(!isLegal(new TablutAction(emptyPos, new Pawn(playerCapturing, enemyPos))))
            return false;
        if (!isEnemy(pawns[captured.getCaptured().position.row][captured.getCaptured().position.column], playerCapturing))
            return false;
        if (pawns[captured.getCaptured().position.row][captured.getCaptured().position.column] == KING 
            && !kingCaptured(captured.getCaptured().position))
            return false;
        return true;
    }

    public LinkedList<TablutAction> getSimulatingActions() {
        if(whiteWin || blackWin || draw)
            return new LinkedList<>();
        LinkedList<TablutAction> actions = getLegalActions();
        LinkedList<TablutAction> result = new LinkedList<>();
        boolean loosing = false;
        boolean stop = false;
        TablutAction kingAction = null;
        if (actions.isEmpty()) {
            if (playerTurn == WHITE)
                blackWin = true;
            else
                whiteWin = true;
            return result;
        }
        int minHash = Integer.MAX_VALUE;
        if (playerTurn == BLACK) {
            for(LinkedList<TablutAction> list : actionsMap.get(kingPosition)) {
                for(TablutAction ka : list) {
                    if(isOnPosition(ka.coordinates, ESCAPE)) {
                        if(ka.coordinates.hashCode() < minHash) {
                            kingAction = ka;
                            minHash = ka.coordinates.hashCode();
                        //stop = true;
                        }
                        //break;
                    }
                }
                //if(stop) break;
            }
        }
        else if(firstMove) {
            firstMove = false;
            return whiteOpenings();
        }

        boolean win = false;
        minHash = Integer.MAX_VALUE;
        for (TablutAction action : actions) {
            if (isWin(action)) {
                if(action.hashCode() < minHash) {
                    minHash = action.hashCode();
                    result = new LinkedList<>();
                    result.add(action);
                    win = true;
                    //break;
                }
            } else if (!win && isPreventingLoose(action, kingAction)) {
                if(!loosing)
                    result = new LinkedList<>();
                result.add(action);
                loosing = true;
            } else if (!win && !loosing)
                result.addLast(action);
        }
        if (result.isEmpty()) {
            return actions;
        }
        return result;
	}


    public LinkedList<TablutAction> getBestActionFirst(int[] weights) {
        if(whiteWin || blackWin || draw)
            return new LinkedList<>();

        LinkedList<TablutAction> actions = getLegalActions();
        LinkedList<TablutAction> result = new LinkedList<>();
        boolean loosing = false;
        boolean stop = false;
        TablutAction kingAction = null;

        if (actions.isEmpty()) {
            if (playerTurn == WHITE)
                blackWin = true;
            else
                whiteWin = true;
            return result;
        }
        
        int minHash = Integer.MAX_VALUE;
        if (playerTurn == BLACK) {
            for(LinkedList<TablutAction> list : actionsMap.get(kingPosition)) {
                for(TablutAction ka : list) {
                    if(isOnPosition(ka.coordinates, ESCAPE)) {
                        if(ka.coordinates.hashCode() < minHash) {
                            kingAction = ka;
                            minHash = ka.coordinates.hashCode();
                        }
                        //stop = true;
                        //break;
                    }
                }
                //if(stop) break;
            }
        }
        else if(firstMove) {
            firstMove = false;
            return whiteOpenings();
        }
        boolean win = false;
        minHash = Integer.MAX_VALUE;
        for (TablutAction action : actions) {
            if (isWin(action)) {
                if(action.hashCode() < minHash) {
                    minHash = action.hashCode();
                    result = new LinkedList<>();
                    result.add(action);
                    win = true;
                }
                //break;
            } else if (!win && isPreventingLoose(action, kingAction)) {
                if(!loosing)
                    result = new LinkedList<>();
                result.add(action);
                loosing = true;
            } else if (!win && !loosing)
                result.addLast(action);
        }
        if(!(result.size() == 1 || loosing))
            result = getFinalActions(result, weights);
        if(result.isEmpty())
            return actions;
        return result;
    }

    public LinkedList<TablutAction> whiteOpenings() {
        LinkedList<TablutAction> result = new LinkedList<>();
        if(this.playerTurn == WHITE) {
            for(LinkedList<TablutAction> l : actionsMap.get(new Coordinates(2, 4))) 
                result.addAll(l);
            for(LinkedList<TablutAction> l : actionsMap.get(new Coordinates(3, 4))) 
                result.addAll(l);
        }
        return result;
    }

    private ArrayList<Integer> getKingPaths() {
        ArrayList<Integer> kingPaths = new ArrayList<>(Directions.values().length);
        ArrayList<LinkedList<TablutAction>> kingMoves = actionsMap.get(kingPosition);
        if(kingMoves != null) 
            for(Directions dir : Directions.values()) 
                kingPaths.add(dir.value(), kingMoves.get(dir.value()).size());
        return kingPaths;
    }

    //TODO
    private ArrayList<Integer> kingPaths;

    private LinkedList<TablutAction> getFinalActions(LinkedList<TablutAction> actions, int[] weights) {
        LinkedList<TablutAction> result = new LinkedList<>();
        int oldCaptures = 0;
        int oldLoss = 0;
        int oldKingMoves = 0;
        
        capturesMap = new HashMap<>();
        
        for(TablutAction a : actions) 
            for(Capture c : a.getCaptured())
                updateCaptureMap(capturesMap, c, a.pawn.position, true);
        for(TablutAction a : getLegalActions(this.playerTurn == WHITE ? BLACK : WHITE))
            for(Capture c : a.getCaptured())
                updateCaptureMap(capturesMap, c, a.pawn.position, true);

        for(Map.Entry<Capture, LinkedList<Coordinates>> e : capturesMap.entrySet()) 
            if(isEnemy(e.getKey().getCaptured().getPawnType(), this.playerTurn))
                oldCaptures++;
            else
                oldLoss++;
        //DEBUG -> old cap/loss
        
        int oldCapturesDebug = 0;
        int oldLossDebug = 0;
        LinkedList<Capture> debCaptures = new LinkedList<>();
        LinkedList<Capture> debLoss = new LinkedList<>();
        for(TablutAction a : getLegalActionsDebug()) {
            for(Capture c : a.getCaptured()) {
                if(!debCaptures.contains(c)) {
                    debCaptures.add(c);
                    oldCapturesDebug++;
                }
            }
        }

        for(TablutAction a : getLegalActionsDebug(this.playerTurn == WHITE ? BLACK : WHITE)) {
            for(Capture c : a.getCaptured()) {
                if(!debLoss.contains(c)) {
                    debLoss.add(c);
                    oldLossDebug++;
                }
            }
        }

        if(oldCaptures != oldCapturesDebug || oldLoss != oldLossDebug) {
            System.out.println("RealOldCap: " + oldCaptures + "\nDebugOldCap: " + oldCapturesDebug);
            System.out.println("RealOldLoss: " + oldLoss + "\nDebugOldLoss: " + oldLossDebug);
            throw new RuntimeException();
        }
        //END DEBUG -> old cap/loss

        for(Integer i : getKingPaths())
            oldKingMoves += i;


        LinkedList<TablutAction> first = new LinkedList<>();
        for (TablutAction action : actions) {
            action.setValue(evaluateAction(action, weights, oldCaptures, oldLoss, oldKingMoves));
                if(action.pawn.getPawnType() == KING)
                    first.addFirst(action);
                else if(!action.getCaptured().isEmpty())
                    first.addFirst(action);
                else if(action.getValue() >= -1000)
                    result.add(action);   
        }
        Collections.sort(result);
        for(TablutAction action : first)
            result.addFirst(action);
        return result;
    }

    private TablutAction tempAction;
    private boolean willBeCaptured;
    private HashMap<Capture, LinkedList<Coordinates>> captureMapCopy;
    private boolean kingCheckmate;

    private double evaluateAction(TablutAction action, int[] weights, int oldCaptures, int oldLoss, int oldKingMoves) {
        int newCaptures = 0;
        int newLoss = 0;
        this.newActiveCaptures = 0;
        this.tempAction = action;
        this.willBeCaptured = false;
        this.captureMapCopy = new HashMap<>();
        this.kingPaths = getKingPaths();
        this.kingCheckmate = false;
        
        for(Map.Entry<Capture, LinkedList<Coordinates>> entry : this.capturesMap.entrySet()) {
            captureMapCopy.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        }
        if(action.pawn.getPawnType() == KING)
            return 0;
        makeTemporaryAction(action);
        
        //DEBUG -> new cap/loss
        int newCapturesDebug = 0;
        int newLossDebug = 0;
        LinkedList<Capture> debCaptures = new LinkedList<>();
        LinkedList<Capture> debLoss = new LinkedList<>();

        for(TablutAction a : getLegalActionsDebug()) {
            for(Capture c : a.getCaptured()) {
                if(!debCaptures.contains(c)) {
                    debCaptures.add(c);
                    newCapturesDebug++;
                }
            }
        }
        boolean willBeCapturedDebug = false;
        for(TablutAction a : getLegalActionsDebug(this.playerTurn == WHITE ? BLACK : WHITE)) {
            for(Capture c : a.getCaptured()) {
                if(c.getCaptured().position.equals(action.coordinates))
                    willBeCapturedDebug = true;
                if(!debLoss.contains(c)) {
                    debLoss.add(c);
                    newLossDebug++;
                }
            }
        }
        
        LinkedList<TablutAction> kingActionsDebug = getPawnActions(kingPosition, KING);
        int newKingMovesDebug = kingActionsDebug.size();
        LinkedList<Coordinates> escapeList = new LinkedList<>();

        boolean kingCheckmateDebug = false;
        for(TablutAction ka : kingActionsDebug) {
            int escapes = 0;
            escapeList = new LinkedList<>();
            for(TablutAction ea : getPawnActions(ka.coordinates, KING)) {
                if(isOnPosition(ea.coordinates, ESCAPE)) {
                    escapeList.add(ea.coordinates);
                    escapes++;
                    if(escapes > 1) {
                        kingCheckmateDebug = true;
                        break;
                    }
                }
            }
            if(kingCheckmateDebug)
                break;
        }




        updateActionMap(action, true);
        undoTemporaryAction(action);

        int newKingMoves = 0;
        for(Directions dir : Directions.values()) {
            newKingMoves += kingPaths.get(dir.value());
        }

        for(Map.Entry<Capture, LinkedList<Coordinates>> entry : captureMapCopy.entrySet()) {
            if(isEnemy(entry.getKey().getCaptured().getPawnType(), this.playerTurn))
                newCaptures++;
            else
                newLoss++;
        }

        if(willBeCapturedDebug != willBeCaptured) {
            System.out.println(action);
            System.out.println("debug: " + willBeCapturedDebug);
            System.out.println("real: " + willBeCaptured);
            System.out.println(this);
            throw new RuntimeException();
        }

        
        if(newKingMoves != newKingMovesDebug) {
            throw new RuntimeException();
        }


        if(newCaptures != newCapturesDebug || newLoss != newLossDebug) {
            makeTemporaryAction(action);
            //printList(getLegalActionsDebug());
            //printList(getLegalActionsDebug(this.playerTurn == WHITE ? BLACK : WHITE));
            System.out.println(action);
            System.out.println("RealNewCap: " + newCaptures + "\nDebugNewCap: " + newCapturesDebug);
            System.out.println("RealNewLoss: " + newLoss + "\nDebugNewLoss: " + newLossDebug);
            System.out.println("Old Captures: ");
            for(Map.Entry<Capture, LinkedList<Coordinates>> entry : capturesMap.entrySet()) 
                System.out.println(entry.getKey() + " -> " + entry.getValue());
                System.out.println("New Captures: ");
            for(Map.Entry<Capture, LinkedList<Coordinates>> entry : captureMapCopy.entrySet()) 
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            System.out.println(this);
            throw new RuntimeException();
        }
        //END DEBUG -> new cap/loss*/

        
        int capturesDiff = (newCaptures + newActiveCaptures - oldCaptures);
        int lossDiff = (newLoss - oldLoss);
        int kingMovesDiff = newKingMoves - oldKingMoves;

        action.capturesDiff = capturesDiff;
        action.lossDiff = lossDiff;
        action.kingMovesDiff = kingMovesDiff;
        action.kingCheckmate = kingCheckmate;
        action.willBeCaptured = willBeCaptured;

        if(!action.getCaptured().isEmpty())
            return 0;
        if(willBeCaptured) {
            newCaptures = 0;
        }
        if(playerTurn == WHITE && kingCheckmate)
            return 1000;
        else if(playerTurn == BLACK && kingCheckmate)
            return -1000;

        return capturesDiff - lossDiff;
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
            if (row == krk && ((column > kck && column <= kce) || (column < kck && column >= kce)))
                return true;
            if (column == kck && ((row > krk && row <= kre) || (row < krk && row >= kre)))
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
        if (blackWin || whiteWin || draw)
            return new LinkedList<TablutAction>();
        return getLegalActions(this.playerTurn);
    }

    public LinkedList<TablutAction> getLegalActions(byte player) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for(Map.Entry<Coordinates, ArrayList<LinkedList<TablutAction>>> entry : this.actionsMap.entrySet()) {
            Coordinates c = entry.getKey();
            if(getValue(pawns, c) == player || (player == WHITE && getValue(pawns, c) == KING))
                for(LinkedList<TablutAction> l : entry.getValue()) {
                    actions.addAll(l);
                }
        }
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
            if (captured != null) {
                action.addCapture(captured);
            }
            captured = getCaptured(c, pawn, c.row - 2, c.column);
            if (captured != null) {
                action.addCapture(captured);
            }
            captured = getCaptured(c, pawn, c.row, c.column + 2);
            if (captured != null) {
                action.addCapture(captured);
            }
            captured = getCaptured(c, pawn, c.row, c.column - 2);
            if (captured != null) {
                action.addCapture(captured);
            }
            actions.add(action);
        }
        return actions;
    }

    private boolean searchEscape(Coordinates position, Directions dir) {
        int step = dir == Directions.UP || dir == Directions.LEFT ? -1 : 1;
        boolean row = dir == Directions.UP || dir == Directions.DOWN;
        Coordinates c = new Coordinates(position.row, position.column);
        for(int i = (row ? position.row : position.column) + step; step > 0 ? i < BOARD_SIZE : i >= 0; i += step) {
            if(row) c.row = i; 
            else c.column = i;
            if(getValue(pawns, c) != EMPTY)
                return false;
            if(getValue(board, c) == ESCAPE)
                return true;
            if(getValue(board, c) != EMPTY)
                return false;
        }
        return false;
    }

    //change name, updateTempActions
    private void countCaptures(Coordinates coord, byte pawn, Directions dir) {
        if(actionsMap.get(coord) != null)
            for(TablutAction a : actionsMap.get(coord).get(dir.value())) {
                for(Capture c : a.getCaptured()) 
                    updateCaptureMap(captureMapCopy, c, a.pawn.position,false);
            }
        int step = dir == Directions.UP || dir == Directions.LEFT ? -1 : 1;
        boolean row = dir == Directions.UP || dir == Directions.DOWN;
        int moves = 0;
        for (int i = (row ? coord.row : coord.column) + step; (step > 0 ? i < BOARD_SIZE : i >= 0) ; i += step) {
            TablutAction action;
            Coordinates c = new Coordinates(row ? i : coord.row, !row ? i : coord.column);
            action = new TablutAction(c, new Pawn(pawn, coord));
            if (!isLegal(action))
                break;
            moves++;
            Capture captured;
            captured = getCaptured(c, pawn, c.row + 2, c.column);
            if (captured != null) {
                if(captured.getCaptured().position.equals(tempAction.coordinates))
                    this.willBeCaptured = true;
                updateCaptureMap(captureMapCopy, captured, coord, true);
            }
            captured = getCaptured(c, pawn, c.row - 2, c.column);
            if (captured != null) {
                if(captured.getCaptured().position.equals(tempAction.coordinates))
                    this.willBeCaptured = true;
                updateCaptureMap(captureMapCopy, captured, coord, true);
            }
            captured = getCaptured(c, pawn, c.row, c.column + 2);
            if (captured != null) {
                if(captured.getCaptured().position.equals(tempAction.coordinates))
                    this.willBeCaptured = true;
                updateCaptureMap(captureMapCopy, captured, coord, true);
            }
            captured = getCaptured(c, pawn, c.row, c.column - 2);
            if (captured != null) {
                if(captured.getCaptured().position.equals(tempAction.coordinates))
                    this.willBeCaptured = true;
                updateCaptureMap(captureMapCopy, captured, coord, true);
            }
        }
        if(pawn == KING) {
            kingPaths.set(dir.value(), moves);
        }
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
        /*
        for(TablutAction a1 : getLegalActions()) {
            System.out.println(a1);
        }
        for(TablutAction a1 : getLegalActions(this.playerTurn == WHITE ? BLACK : WHITE)) {
            System.out.println(a1);
        }*/
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
        updateActionMap(action, false);
        this.drawConditions.add(this);
        if (this.playerTurn == WHITE)
            this.playerTurn = BLACK;
        else
            this.playerTurn = WHITE;
    }


    public void makeTemporaryAction(TablutAction action) {
        pawns[action.pawn.position.row][action.pawn.position.column] = EMPTY;
        pawns[action.coordinates.row][action.coordinates.column] = action.pawn.getPawnType();
        for(Capture capture : action.getCaptured()) {
            pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] = EMPTY;
        }
    }

    public void undoTemporaryAction(TablutAction action) {
        pawns[action.coordinates.row][action.coordinates.column] = EMPTY;
        pawns[action.pawn.position.row][action.pawn.position.column] = action.pawn.getPawnType();
        for(Capture capture : action.getCaptured()) {
            pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] = capture.getCaptured().getPawnType(); 
        }
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

    public boolean isBoardBlocker(Coordinates blocker) {
        byte cell = getValue(board, blocker);
        int row = blocker.row;
        int column = blocker.column;
        if (cell == CITADEL)
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
        if (enemy == EMPTY || pawn == EMPTY)
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

    public LinkedList<TablutAction> getLegalActionsDebug() {
        if (blackWin || whiteWin || draw)
            return new LinkedList<TablutAction>();
        LinkedList<TablutAction> actions = getLegalActionsDebug(this.playerTurn);
        return actions;
    }

    public LinkedList<TablutAction> getLegalActionsDebug(byte player) {
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
        for (int i = (row ? coord.row : coord.column) + step; condition.apply(i); i += step) {
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
    
    public LinkedList<TablutAction> getBestActionsDebug(boolean first) {
        if(whiteWin || blackWin || draw)
            return new LinkedList<>();
        LinkedList<TablutAction> actions = getLegalActionsDebug();
        LinkedList<TablutAction> result = new LinkedList<>();
        boolean loosing = false;
        TablutAction kingAction = null;

        if (actions.isEmpty()) {
            if (playerTurn == WHITE)
                blackWin = true;
            else
                whiteWin = true;
            return result;
        }

        int minHash = Integer.MAX_VALUE;
        if (playerTurn == BLACK) {
            for (TablutAction ka : getPawnActions(kingPosition, KING))
                if (isOnPosition(ka.coordinates, ESCAPE)) {
                    if(ka.coordinates.hashCode() < minHash) {
                        kingAction = ka;
                        minHash = ka.coordinates.hashCode();
                    }
                }
        }
        else if(first) {
            return whiteOpeningsDebug();
        }

        boolean win = false;
        minHash = Integer.MAX_VALUE;
        for (TablutAction action : actions) {
            if (isWin(action)) {
                if(action.hashCode() < minHash) {
                    minHash = action.hashCode();
                    result = new LinkedList<>();
                    result.add(action);
                    win = true;
                    //break;
                }
            } else if (!win && isPreventingLoose(action, kingAction)) {
                if(!loosing)
                    result = new LinkedList<>();
                result.add(action);
                loosing = true;
            } else if (!win && !loosing)
                result.addLast(action);
        }
        if(result.isEmpty())
            return actions;
        return result;
    }
    
    public LinkedList<TablutAction> whiteOpeningsDebug() {
        LinkedList<TablutAction> result = new LinkedList<>();
        if(this.playerTurn == WHITE) {
            result.addAll(getPawnActions(new Coordinates(2, 4), WHITE));
            result.addAll(getPawnActions(new Coordinates(3, 4), WHITE));
        }
        return result;
    }

    public boolean isFirstMove() {
        return firstMove;
    }
}