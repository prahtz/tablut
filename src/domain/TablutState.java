package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    private boolean firstMove = false;
    private TablutAction firstAction = null;
    private TablutAction previousAction = null;

    private enum Directions {
        UP(0), RIGHT(1), DOWN(2), LEFT(3);

        private int value;

        private Directions(int value) {
            this.value = value;
        }
    }

    public TablutState(byte playerTurn) {
        this.playerTurn = playerTurn;
        this.kingPosition = new Coordinates(4, 4);
        this.whitePawns = WHITE_PAWNS;
        this.blackPawns = BLACK_PAWNS;
        this.firstMove = true;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.add(this);
        initPawns();
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
    }

    public TablutState(byte[][] pawns, byte playerTurn, boolean firstMove, TablutAction firstAction, LinkedList<TablutState> drawConditions) {
        this.pawns = pawns;
        this.playerTurn = playerTurn;
        this.blackPawns = 0;
        this.whitePawns = 0;
        this.drawConditions = new LinkedList<>();
        this.drawConditions.addAll(drawConditions);
        this.drawConditions.add(this);
        this.firstMove = firstMove;
        this.firstAction = firstAction;
        initState();
    }

    private TablutState(TablutState state, byte[][] pawns, Coordinates kingPosition,
            LinkedList<TablutState> drawConditions) {
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
        this.firstMove = state.isFirstMove();
        this.firstAction = state.getFirstAction();
        this.previousAction = state.getPreviousAction();
    }

    private Coordinates getKingPosition() {
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                if (pawns[i][j] == KING)
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

    private LinkedList<TablutAction> getPawnActions(Coordinates coord, byte pawn) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        actions.addAll(searchActions(coord, pawn, -1, true));
        actions.addAll(searchActions(coord, pawn, 1, false));
        actions.addAll(searchActions(coord, pawn, 1, true));
        actions.addAll(searchActions(coord, pawn, -1, false));
        return actions;
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
                    if(board[i][j] == CITADEL) 
                        result = result + "T";
                    else if(board[i][j] == CAMP)
                        result = result + "X";
                    else
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
        TablutState newState = new TablutState(this, newPawns, kingPosition, drawConditions);
        return newState;
    }

    public TablutState copySimulation() {
        byte[][] newPawns = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                newPawns[i][j] = pawns[i][j];
        TablutState newState = new TablutState(this, newPawns, kingPosition, drawConditions);
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
        for (TablutAction a : l)
            System.out.println(a.toString());
    }

    private Directions getDirection(Coordinates prev, Coordinates next) {
        if (prev.row == next.row) {
            if (prev.column > next.column)
                return Directions.LEFT;
            else
                return Directions.RIGHT;
        } else {
            if (prev.row > next.row)
                return Directions.UP;
            else
                return Directions.DOWN;
        }
    }

    public byte getValue(byte[][] b, Coordinates c) {
        return b[c.row][c.column];
    }

    public ArrayList<SimulateAction> getSimulatingActions(Integer[] weights) {
        if (whiteWin || blackWin || draw)
            return new ArrayList<>();
        LinkedList<TablutAction> actions = getLegalActions();
        ArrayList<SimulateAction> result = new ArrayList<>(actions.size());
        LinkedList<Coordinates> toProtect = new LinkedList<>();
        
        toProtect = getProtectionCoordinates();
        boolean loosing = false;
        TablutAction kingAction = null;
        if (actions.isEmpty()) {
            if (playerTurn == WHITE)
                blackWin = true;
            else
                whiteWin = true;
            return result;
        }
        if (playerTurn == BLACK) {
            for (TablutAction ka : getPawnActions(kingPosition, KING)) {
                if (isOnPosition(ka.coordinates, ESCAPE)) {
                    kingAction = ka;
                    break;
                }
            }
        }
        if (firstMove) {
            if (playerTurn == WHITE)
                result.add(new SimulateAction(whiteOpening(), 1));
            else {
                result.add(new SimulateAction(blackOpening(firstAction), 1));
                firstMove = false;
            }
            return result;
        }

        for (TablutAction action : actions) {
            if (isWin(action)) {
                result = new ArrayList<>();
                result.add(new SimulateAction(action, 1));
                break;
            } else if (isPreventingLoose(action, kingAction, toProtect)) {
                if (!loosing)
                    result = new ArrayList<>();
                result.add(new SimulateAction(action, 1));
                loosing = true;
                
            }
        }
        if (!result.isEmpty()) {
            return result;
        }

        boolean toMiddleGame = false;
        BoardSet boardSet = null;
        if(!isEarlyGame()) {
            if(kingPosition.row == 4 || kingPosition.column == 4)
                boardSet = Side.getSideFromMiddle(kingPosition);
            else 
                boardSet = Quadrant.getQuadrant(kingPosition);
        }
        for(TablutAction action : actions) { 
            if(!action.getCaptured().isEmpty() && ((this.playerTurn == BLACK && !isKingCheck(action)) || (this.playerTurn == WHITE && !willKingBeCaptured(action))) && !toMiddleGame) {
                if(action.pawn.getPawnType() != KING) {
                    for(Capture c : action.getCaptured()) {
                        if(c.getCaptured().position.equals(previousAction.coordinates)) {
                            result = new ArrayList<>();
                            result.add(new SimulateAction(action, weights[Weights.CAPTURE.value()]));
                            return result;
                        }
                    }
                    result.add(new SimulateAction(action, weights[Weights.CAPTURE.value()]));
                }
                else {
                    result.add(new SimulateAction(action, weights[Weights.CAPTURE.value()]));
                }
            }
            else if(action.pawn.getPawnType() == KING) {
                if(!willKingBeCaptured(action)) {
                    if(isEarlyGame()) {
                        if(isKingCovered(action)) {
                            if(!toMiddleGame)
                                result = new ArrayList<>();
                            result.add(new SimulateAction(action, weights[Weights.CAPTURE.value()]));
                            toMiddleGame = true;
                        }
                        else if(!toMiddleGame) {
                            result.add(new SimulateAction(action, 35));
                        }
                    }
                    else if(isKingCheck(action)) {
                        result.add(new SimulateAction(action, weights[Weights.KING_CHECK.value()]));
                    }
                    else { 
                        result.add(new SimulateAction(action, weights[Weights.STANDARD_ACTION.value()]));
                    }
                }
            }
            else if(action.pawn.getPawnType() == BLACK && !willBeCaptured(action)) {
                if(!isKingCheck(action)) {
                    if(isEarlyGame() && !toMiddleGame) {
                        if(Boards.isBlackCell(action))
                            result.add(new SimulateAction(action, weights[Weights.STANDARD_ACTION.value()]));
                    }
                    else /*if(boardSet != null && (boardSet.contains(action.coordinates) || boardSet.contains(action.pawn.position)))*/ {
                        if(isAttackingKing(action)) {
                            result.add(new SimulateAction(action, weights[Weights.BLACK_ATTACK.value()]));
                        }
                        else if(isBlockingKing(action)) {
                            result.add(new SimulateAction(action, weights[Weights.BLACK_ATTACK.value()]));
                        }
                        else {
                            result.add(new SimulateAction(action, weights[Weights.STANDARD_ACTION.value()]));
                        }
                    }
                }
            }
            else if(action.pawn.getPawnType() == WHITE && !willBeCaptured(action)) {
                if(!willKingBeCaptured(action)) {
                    if(isEarlyGame() && !toMiddleGame) {
                        if(action.coordinates.row != 4 && action.coordinates.column != 4) {
                            if(Boards.isValidPositioning(pawns, action)) {
                                result.add(new SimulateAction(action, weights[Weights.STANDARD_ACTION.value()]));
                            }
                        }
                    }
                    else /*if(boardSet != null && (boardSet.contains(action.coordinates) || boardSet.contains(action.pawn.position)))*/ {
                        if(boardSet != null && boardSet.contains(action.coordinates) && isToBorder(action)) {
                            result.add(new SimulateAction(action, weights[Weights.WHITE_BORDER.value()]));
                        }
                        else {
                            result.add(new SimulateAction(action, weights[Weights.STANDARD_ACTION.value()]));
                        }
                    }
                }
            }   
        }      

        if(result.isEmpty()) {
            return toSimulateActions(actions);
        }
        return result;
    }

    private boolean isBlockingKing(TablutAction action) {
        if(action.pawn.getPawnType() != BLACK) 
            return false;
        return action.coordinates.row == kingPosition.row || action.coordinates.column == kingPosition.column;
    } 

    private boolean isKingCovered(TablutAction action) {
        Side s = Side.getSideFromMiddle(action.coordinates);
        int whites = 0;
        boolean row = action.coordinates.row == 4;
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if((row ? j != 4 : i != 4) && pawns[i][j] == WHITE && s.contains(new Coordinates(i, j))) {
                    whites++;
                    if(whites == 3)  {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAttackingKing(TablutAction action) {
        if(action.pawn.getPawnType() != BLACK)
            return false;
        int r = action.coordinates.row;
        int c = action.coordinates.column;
        if(r + 1 < BOARD_SIZE && pawns[r + 1][c] == KING)
            return true;
        else if(r - 1 >= 0 && pawns[r - 1][c] == KING) 
            return true;
        else if(c + 1 < BOARD_SIZE && pawns[r][c + 1] == KING) 
            return true;
        else if(c - 1 >= 0 && pawns[r][c - 1] == KING) 
            return true;
        return false;
    }

    private boolean isToBorder(TablutAction action) {
        if((action.pawn.position.row == kingPosition.row || action.pawn.position.column == kingPosition.column) 
            && (action.coordinates.row == 0 || action.coordinates.row == BOARD_SIZE - 1 
            || action.coordinates.column == 0 || action.coordinates.column == BOARD_SIZE - 1))
            return true;
        return false;
    }

    private ArrayList<SimulateAction> toSimulateActions(LinkedList<TablutAction> actions) {
        ArrayList<SimulateAction> result = new ArrayList<>();
        for(TablutAction a : actions)
            result.add(new SimulateAction(a, 1));
        return result;
    }

    private boolean searchEscape(Coordinates pos, Directions dir) {
        boolean row = dir == Directions.UP || dir == Directions.DOWN;
        int step = dir == Directions.UP || dir == Directions.LEFT ? -1 : 1;
        boolean result = false;
        for(int i = (row ? pos.row : pos.column) + step ; step > 0 ? i < BOARD_SIZE : i >= 0; i += step) {
            Coordinates c = new Coordinates(row ? i : pos.row, row ? pos.column : i);
            if(getValue(board, c) == ESCAPE)
                result = true;
            else if(getValue(pawns, c) != EMPTY || getValue(board, c) != EMPTY)
                break;
        }
        return result;
    }

    private boolean isKingCheck(TablutAction action) {
        makeTemporaryAction(action);
        for(Directions dir : getOtherAxisDirections(getDirection(action.pawn.position, action.coordinates))) {
            if(searchEscape(kingPosition, dir)) {
                undoTemporaryAction(action);
                return true;
            }
        }
        undoTemporaryAction(action);
        return false;
    }

    private Directions[] getOtherAxisDirections(Directions dir) {
        if (dir == Directions.DOWN || dir == Directions.UP)
            return new Directions[] { Directions.RIGHT, Directions.LEFT };
        else
            return new Directions[] { Directions.UP, Directions.DOWN };
    }

    private Directions[] getOtherDirections(Directions dir) {
        if (dir == Directions.UP)
            return new Directions[] {Directions.RIGHT, Directions.DOWN, Directions.LEFT};
        else if(dir == Directions.RIGHT)
            return new Directions[] {Directions.UP, Directions.DOWN, Directions.LEFT};
        else if(dir == Directions.DOWN)
            return new Directions[] {Directions.UP, Directions.RIGHT, Directions.LEFT};
        else 
            return new Directions[] {Directions.UP, Directions.RIGHT, Directions.DOWN};
    }
    
    public LinkedList<TablutAction> getBestActionFirst() {
        if (whiteWin || blackWin || draw)
            return new LinkedList<>();

        LinkedList<TablutAction> actions = getLegalActions();
        Collections.shuffle(actions);
        LinkedList<TablutAction> result = new LinkedList<>();
        LinkedList<Coordinates> toProtect = new LinkedList<>();
        boolean loosing = false;
        TablutAction kingAction = null;

        toProtect = getProtectionCoordinates();

        if (actions.isEmpty()) {
            if (playerTurn == WHITE)
                blackWin = true;
            else
                whiteWin = true;
            return result;
        }
        if (playerTurn == BLACK) {
            for (TablutAction ka : getPawnActions(kingPosition, KING)) {
                if (isOnPosition(ka.coordinates, ESCAPE)) {
                    kingAction = ka;
                    break;
                }
            }
        }
        if (firstMove) {
            if (playerTurn == WHITE)
                result.add(whiteOpening());
            else {
                result.add(blackOpening(firstAction));
                firstMove = false;
            }
            return result;
        }
        for (TablutAction action : actions) {
            if (isWin(action)) {
                result = new LinkedList<>();
                result.add(action);
                break;
            } else if (isPreventingLoose(action, kingAction, toProtect)) {
                if (!loosing)
                    result = new LinkedList<>();
                result.add(action);
                loosing = true;
            }
        }
        if(!result.isEmpty()) 
            return result;

        for(TablutAction action : actions) {
            if(action.pawn.getPawnType() == KING)
                result.addFirst(action);
            else if(!action.getCaptured().isEmpty())
                result.addFirst(action);
            else if(isEarlyGame() && this.playerTurn == WHITE) {
                if((action.coordinates.row != 4 && action.coordinates.column != 4)) {
                    if(Boards.isValidPositioning(pawns, action))
                        result.add(action);
                }
            }
            else if(isEarlyGame() && this.playerTurn == BLACK) {
                if(Boards.isBlackCell(action))
                    result.add(action);
            }
            else {
                result.add(action);
            }
        }    
        if (result.isEmpty())
            return actions;
        return result;
    }

    private boolean isEarlyGame() {
        return pawns[4][4] == KING;
    }

    public TablutAction whiteOpening() {
        return new TablutAction(new Coordinates(2, 1), new Pawn(WHITE, new Coordinates(2, 4)));
    }

    public TablutAction blackOpening(TablutAction whiteOpening) {
        HashMap<TablutAction, TablutAction> responses = new HashMap<>();
        Coordinates whiteUp = new Coordinates(2, 4);
        Coordinates whiteDown = new Coordinates(3, 4);
        responses.put(new TablutAction(new Coordinates(2, 0), new Pawn(WHITE, whiteUp)),
                new TablutAction(new Coordinates(2, 5), new Pawn(BLACK, new Coordinates(0, 5))));
        responses.put(new TablutAction(new Coordinates(2, 1), new Pawn(WHITE, whiteUp)),
                new TablutAction(new Coordinates(2, 5), new Pawn(BLACK, new Coordinates(0, 5))));
        responses.put(new TablutAction(new Coordinates(2, 2), new Pawn(WHITE, whiteUp)),
                new TablutAction(new Coordinates(2, 5), new Pawn(BLACK, new Coordinates(0, 5))));
        responses.put(new TablutAction(new Coordinates(2, 3), new Pawn(WHITE, whiteUp)),
                new TablutAction(new Coordinates(1, 6), new Pawn(BLACK, new Coordinates(1, 4))));
        responses.put(new TablutAction(new Coordinates(3, 1), new Pawn(WHITE, whiteDown)),
                new TablutAction(new Coordinates(3, 4), new Pawn(BLACK, new Coordinates(3, 8))));
        responses.put(new TablutAction(new Coordinates(3, 2), new Pawn(WHITE, whiteDown)),
                new TablutAction(new Coordinates(3, 4), new Pawn(BLACK, new Coordinates(3, 8))));
        responses.put(new TablutAction(new Coordinates(3, 3), new Pawn(WHITE, whiteDown)),
                new TablutAction(new Coordinates(1, 6), new Pawn(BLACK, new Coordinates(1, 4))));

        if (responses.get(whiteOpening) != null) {
            TablutAction result = responses.get(whiteOpening);
            result.addCapture(getCaptured(result.coordinates, BLACK, result.coordinates.row + 2, result.coordinates.column));
            result.addCapture(getCaptured(result.coordinates, BLACK, result.coordinates.row - 2, result.coordinates.column));
            result.addCapture(getCaptured(result.coordinates, BLACK, result.coordinates.row, result.coordinates.column + 2));
            result.addCapture(getCaptured(result.coordinates, BLACK, result.coordinates.row, result.coordinates.column - 2));
            return result;
        }

        Coordinates position = new Coordinates(whiteOpening.pawn.position.row, whiteOpening.pawn.position.column);
        Coordinates destination = new Coordinates(whiteOpening.coordinates.row, whiteOpening.coordinates.column);

        boolean swap = false;
        boolean columnPos = false;
        if (position.row == 4) {
            int temp = position.row;
            position.row = position.column;
            position.column = temp;
            swap = true;
        }
        if (position.column > 4) {
            position.column = BOARD_SIZE - 1 - position.column;
            columnPos = true;
        }
        if (position.row > 4) {
            position.row = BOARD_SIZE - 1 - position.row;
            columnPos = true;
        }
        boolean mirror = true;

        Directions dir = getDirection(whiteOpening.pawn.position, whiteOpening.coordinates);
        if ((dir == Directions.UP && swap && columnPos) || (dir == Directions.DOWN && swap && !columnPos)
                || (dir == Directions.LEFT && !swap && !columnPos) || (dir == Directions.RIGHT && !swap && columnPos)) {
            mirror = false;
        }

        if(destination.row == whiteOpening.pawn.position.row) 
            destination.column = 4 - Math.abs(whiteOpening.pawn.position.column - destination.column);
        else 
            destination.column = 4 - Math.abs(whiteOpening.pawn.position.row - destination.row);
        destination.row = position.row;

        TablutAction response = responses.get(new TablutAction(destination, new Pawn(WHITE, position)));
        Coordinates resPos = response.pawn.position;
        Coordinates resDest = response.coordinates;
        if (mirror) {
            resPos.column = BOARD_SIZE - 1 - resPos.column;
            resDest.column = BOARD_SIZE - 1 - resDest.column;
        }
        if (swap || columnPos) {
            if (swap) {
                int temp = resPos.row;
                resPos.row = resPos.column;
                resPos.column = temp;
                temp = resDest.row;
                resDest.row = resDest.column;
                resDest.column = temp;
                if (columnPos) {
                    resPos.column = BOARD_SIZE - 1 - resPos.column;
                    resDest.column = BOARD_SIZE - 1 - resDest.column;
                } else {
                    resPos.row = BOARD_SIZE - 1 - resPos.row;
                    resDest.row = BOARD_SIZE - 1 - resDest.row;
                }
            } else {
                resPos.column = BOARD_SIZE - 1 - resPos.column;
                resPos.row = BOARD_SIZE - 1 - resPos.row;
                resDest.row = BOARD_SIZE - 1 - resDest.row;
                resDest.column = BOARD_SIZE - 1 - resDest.column;
            }
        }
        TablutAction result = new TablutAction(resDest, new Pawn(BLACK, resPos));
        result.addCapture(getCaptured(resDest, BLACK, resDest.row + 2, resDest.column));
        result.addCapture(getCaptured(resDest, BLACK, resDest.row - 2, resDest.column));
        result.addCapture(getCaptured(resDest, BLACK, resDest.row, resDest.column + 2));
        result.addCapture(getCaptured(resDest, BLACK, resDest.row, resDest.column - 2));
        return result;
    }

    private boolean isKingNearCitadel() {
        int r = kingPosition.row;
        int c = kingPosition.column;
        return (r == 4 && (c == 3 || c == 5)) || (c == 4 && (r == 3 || r == 5));
    }

    private LinkedList<Coordinates> getProtectionCoordinates() {
        if(this.playerTurn == WHITE) {
            return getWhiteProtection();
        }
        return getBlackProtection();
    }

    private LinkedList<Coordinates> getBlackProtection() {
        LinkedList<TablutAction> kingsActions = getPawnActions(kingPosition, KING);
        LinkedList<Coordinates> result = new LinkedList<>();
        Coordinates winningPos = null;
        Directions winningDir = null;
        for(TablutAction ka : kingsActions) {
            Coordinates dest = ka.coordinates;
            int escapes = 0;
            for(Directions dir : getOtherAxisDirections(getDirection(kingPosition, dest))) {
                if(searchEscape(dest, dir)) {
                    escapes++;
                    if(escapes == 2) {
                        winningPos = dest;
                        winningDir = dir;
                        break;
                    }
                        
                }
            }
            if(winningPos != null) break;
        }

        if(winningPos != null) {
            boolean row = winningDir == Directions.UP || winningDir == Directions.DOWN;
            for(int i = 0; i < BOARD_SIZE; i++)
                result.add(new Coordinates(row ? i : winningPos.row, row ? winningPos.column : i));
        }
        return result;
    }

    private LinkedList<Coordinates> getWhiteProtection() {
        int r = kingPosition.row;
        int c = kingPosition.column;
        LinkedList<Coordinates> result = new LinkedList<>();
        Function<Coordinates, Boolean> condition = (Coordinates coord) -> getValue(pawns, coord) == BLACK;
        Coordinates[] empties = new Coordinates[Directions.values().length];
        int blockers = 1;
        if(isEarlyGame() || isKingNearCitadel())
            blockers = 3;

        if(isBlocker(r + 1, c, BLACK))  
            empties[getDirection(kingPosition, new Coordinates(r + 1, c)).value] = new Coordinates(r - 1, c);
        if(isBlocker(r - 1, c, BLACK)) 
            empties[getDirection(kingPosition, new Coordinates(r - 1, c)).value] = new Coordinates(r + 1, c);
        if(isBlocker(r, c + 1, BLACK))
            empties[getDirection(kingPosition, new Coordinates(r, c + 1)).value] = new Coordinates(r, c - 1);
        if(isBlocker(r, c - 1, BLACK))
            empties[getDirection(kingPosition, new Coordinates(r, c - 1)).value] = new Coordinates(r, c + 1);
        int i = 0;
        for(Coordinates coord : empties)
            if(coord != null)
                i++;
        if(i < blockers)
            return result;
        for(Directions dir : Directions.values()) {
            if(empties[dir.value] != null && getValue(pawns, empties[dir.value]) == EMPTY) {
                for(Directions d : getOtherDirections(dir)) {
                    Coordinates enemyPos = searchByDirection(empties[dir.value], d, condition);
                    if(enemyPos != null) {
                        if(result.isEmpty())
                            result = insideCoordinates(empties[dir.value], enemyPos);
                        else {
                            result = new LinkedList<>();
                            result.add(empties[dir.value]);
                            return result;
                        }
                    }
                }
                if(!result.isEmpty())
                    return result;
            }
        }
        return result;
    }

    private boolean willBeCaptured(TablutAction action) {
        makeTemporaryAction(action);
        int r = action.coordinates.row;
        int c = action.coordinates.column;
        byte enemy = action.pawn.getPawnType() == BLACK ? WHITE : BLACK;
        Function<Coordinates, Boolean> condition = (Coordinates coord) -> isEnemy(getValue(pawns, coord), action.pawn.getPawnType());
        Coordinates[] empties = new Coordinates[Directions.values().length];

        if(isBlocker(r + 1, c, enemy) && r - 1 >= 0)  
            empties[getDirection(action.coordinates, new Coordinates(r + 1, c)).value] = new Coordinates(r - 1, c);
        if(isBlocker(r - 1, c, enemy) && r + 1 < BOARD_SIZE) 
            empties[getDirection(action.coordinates, new Coordinates(r - 1, c)).value] = new Coordinates(r + 1, c);
        if(isBlocker(r, c + 1, enemy) && c - 1 >= 0)
            empties[getDirection(action.coordinates, new Coordinates(r, c + 1)).value] = new Coordinates(r, c - 1);
        if(isBlocker(r, c - 1, enemy) && c + 1 < BOARD_SIZE)
            empties[getDirection(action.coordinates, new Coordinates(r, c - 1)).value] = new Coordinates(r, c + 1);

        for(Directions dir : Directions.values()) {
            if(empties[dir.value] != null && getValue(pawns, empties[dir.value]) == EMPTY) {
                for(Directions d : getOtherDirections(dir)) {
                    Coordinates enemyPos = searchByDirection(empties[dir.value], d, condition);
                    if(enemyPos != null)  {
                        undoTemporaryAction(action);
                        return true;
                    }
                }
            }
        }
        undoTemporaryAction(action);
        return false;
    }

    private boolean willKingBeCaptured(TablutAction action) {
        makeTemporaryAction(action);
        int r = kingPosition.row;
        int c = kingPosition.column;
        Function<Coordinates, Boolean> condition = (Coordinates coord) -> getValue(pawns, coord) == BLACK;
        Coordinates[] empties = new Coordinates[Directions.values().length];
        int blockers = 1;
        if(isEarlyGame() || isKingNearCitadel())
            blockers = 3;

        if(isBlocker(r + 1, c, BLACK) && r - 1 >= 0)  
            empties[getDirection(kingPosition, new Coordinates(r + 1, c)).value] = new Coordinates(r - 1, c);
        if(isBlocker(r - 1, c, BLACK) && r + 1 < BOARD_SIZE) 
            empties[getDirection(kingPosition, new Coordinates(r - 1, c)).value] = new Coordinates(r + 1, c);
        if(isBlocker(r, c + 1, BLACK) && c - 1 >= 0)
            empties[getDirection(kingPosition, new Coordinates(r, c + 1)).value] = new Coordinates(r, c - 1);
        if(isBlocker(r, c - 1, BLACK) && c + 1 < BOARD_SIZE)
            empties[getDirection(kingPosition, new Coordinates(r, c - 1)).value] = new Coordinates(r, c + 1);
        int i = 0;
        for(Coordinates coord : empties) {
            if(coord != null) {
                i++;
            }
        }
        
        if(i >= blockers) {
            for(Directions dir : Directions.values()) {
                if(empties[dir.value] != null && getValue(pawns, empties[dir.value]) == EMPTY) {
                    for(Directions d : getOtherDirections(dir)) {
                        Coordinates enemyPos = searchByDirection(empties[dir.value], d, condition);
                        if(enemyPos != null)  {
                            undoTemporaryAction(action);
                            return true;
                        }
                    }
                }
            }
        }
        undoTemporaryAction(action);
        return false;
    }

    private LinkedList<Coordinates> insideCoordinates(Coordinates c1, Coordinates c2) {
        LinkedList<Coordinates> result = new LinkedList<>();
        if(c1.equals(c2))
            return result;
        result.add(c1);
        Directions dir = getDirection(c1, c2);
        boolean row = dir == Directions.UP || dir == Directions.DOWN ;
        int step = dir == Directions.UP || dir == Directions.LEFT ? -1: 1;
        for(int i = row ? c1.row + step : c1.column + step; row ? c2.row != i : c2.column != i ; i += step) 
            result.add(new Coordinates(row ? i : c1.row, row ? c1.column : i));
        return result;
    }

    private Coordinates searchByDirection(Coordinates pos, Directions direction, Function<Coordinates, Boolean> condition) {
        int row = pos.row;
        int column = pos.column;
        Coordinates coord = new Coordinates(row, column);
        boolean camp = false, citadel = false;
        int step = direction == Directions.UP || direction == Directions.LEFT ? -1 : 1;
        for (int i = (direction == Directions.UP || direction == Directions.DOWN ? row: column); step > 0
                ? i < BOARD_SIZE : i >= 0; i += step) {
            if (direction == Directions.UP || direction == Directions.DOWN)
                coord.row = i;
            else
                coord.column = i;
            if (getValue(board, coord) == CAMP)
                camp = true;
            else if (getValue(board, coord) == CITADEL)
                citadel = true;
            else if ((getValue(board, coord) == EMPTY || getValue(board, coord) == ESCAPE) && (camp || citadel))
                break;
            if (condition.apply(coord)) {
                return coord;
            }
            if (getValue(pawns, coord) != EMPTY) {
                break;
            }
        }
        return null;
    }


    private boolean isPreventingLoose(TablutAction action, TablutAction kingAction, LinkedList<Coordinates> toProtect) {
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
        else if(player == BLACK && !toProtect.isEmpty()) {
            for(Coordinates c : toProtect) {
                if(action.coordinates.equals(c)) {
                    return true;
                }
            }
        }
        else if(player == WHITE && !toProtect.isEmpty() && !willKingBeCaptured(action)) {
            if(!action.getCaptured().isEmpty())
                return true;
            for(Coordinates c : toProtect) {
                if(action.coordinates.equals(c)) {
                    return true;
                }
            }
        }
        else if(player == KING && !toProtect.isEmpty() && !willKingBeCaptured(action))
            return true;
        return false;
    }

    private boolean isWin(TablutAction action) {
        byte player = action.pawn.getPawnType();
        if (player == BLACK) {
            for (Capture capture : action.getCaptured())
                if (capture.getCaptured().getPawnType() == KING)
                    return true;
        } else {
            if (player == KING && isOnPosition(action.coordinates, ESCAPE))
                return true;
            return blackPawns - action.getCaptured().size() == 0;
        }
        return false;
    }

    public LinkedList<TablutAction> getLegalActions() {
        if (blackWin || whiteWin || draw)
            return new LinkedList<TablutAction>();
        return getLegalActions(this.playerTurn);
    }

    public LinkedList<TablutAction> getLegalActions(byte player) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for(int i = 0; i < BOARD_SIZE; i++) 
            for(int j = 0; j < BOARD_SIZE; j++)
                if(pawns[i][j] == player || (player == WHITE && pawns[i][j] == KING))
                    actions.addAll(getPawnActions(new Coordinates(i, j), pawns[i][j]));
        return actions;
    }

    private LinkedList<TablutAction> searchActions(Coordinates coord, byte pawn, int step, boolean row) {
        LinkedList<TablutAction> actions = new LinkedList<>();
        for (int i = (row ? coord.row : coord.column) + step; (step > 0 ? i < BOARD_SIZE : i >= 0); i += step) {
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
        if (firstMove)
            firstAction = action;

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
        this.drawConditions.add(this);
        this.previousAction = action;
        if (this.playerTurn == WHITE)
            this.playerTurn = BLACK;
        else
            this.playerTurn = WHITE;
        
        
    }

    public void makeTemporaryAction(TablutAction action) {
        if(action.pawn.getPawnType() == KING)
            kingPosition = action.coordinates;
        pawns[action.pawn.position.row][action.pawn.position.column] = EMPTY;
        pawns[action.coordinates.row][action.coordinates.column] = action.pawn.getPawnType();
        for (Capture capture : action.getCaptured()) {
            pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] = EMPTY;
        }
    }

    public void undoTemporaryAction(TablutAction action) {
        if(action.pawn.getPawnType() == KING)
            kingPosition = action.pawn.position;
        pawns[action.coordinates.row][action.coordinates.column] = EMPTY;
        pawns[action.pawn.position.row][action.pawn.position.column] = action.pawn.getPawnType();
        for (Capture capture : action.getCaptured()) {
            pawns[capture.getCaptured().position.row][capture.getCaptured().position.column] = capture.getCaptured()
                    .getPawnType();
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
                && !kingCaptured(captured.getCaptured().position, position))
            return null;
        return captured;
    }

    private boolean isBlocker(int row, int column, byte pawn) {
        if(row < 0 || row >= BOARD_SIZE || column < 0 || column >= BOARD_SIZE)
            return false;
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

    private boolean kingCaptured(Coordinates captured, Coordinates emptyPos) {
        int r = captured.row;
        int c = captured.column;
        if (r < 3 || r > 5 || c < 3 || c > 5)
            return true;
        boolean result = true;
        pawns[emptyPos.row][emptyPos.column] = BLACK;
        if (board[r][c] == CITADEL)
            result = pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK && pawns[r][c + 1] == BLACK
                    && pawns[r][c - 1] == BLACK;
        else if (board[r + 1][c] == CITADEL)
            result = pawns[r - 1][c] == BLACK && pawns[r][c + 1] == BLACK && pawns[r][c - 1] == BLACK;
        else if (board[r - 1][c] == CITADEL)
            result = pawns[r + 1][c] == BLACK && pawns[r][c + 1] == BLACK && pawns[r][c - 1] == BLACK;
        else if (board[r][c + 1] == CITADEL)
            result = pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK && pawns[r][c - 1] == BLACK;
        else if (board[r][c - 1] == CITADEL)
            result = pawns[r + 1][c] == BLACK && pawns[r - 1][c] == BLACK && pawns[r][c + 1] == BLACK;
        pawns[emptyPos.row][emptyPos.column] = EMPTY;
        return result;
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

    public LinkedList<TablutState> getDrawConditions() {
        return this.drawConditions;
    }

    public int getBlackPawns() {
        return blackPawns;
    }

    public int getWhitePawns() {
        return whitePawns;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public TablutAction getFirstAction() {
        return firstAction;
    }

    public void setPreviousAction(TablutAction previousAction) {
        this.previousAction = previousAction;
    }

    public TablutAction getPreviousAction() {
        return previousAction;
    }

    public Quadrant getQuadrant(Coordinates c) {
        for(Quadrant q : Quadrant.quadrants)
            if(q.contains(c))
                return q;
        return null;
    }
}