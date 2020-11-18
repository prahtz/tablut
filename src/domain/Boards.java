package domain;

import java.util.HashMap;

public class Boards {
    private static final HashMap<Coordinates, Coordinates> escapesToPeak = initEscapesToPeak();

    static final byte[][] whiteBoard = initWhiteBoard();
    static final byte[][] blackBoard = initBlackBoard();
    static final byte OTHER = 0;
    static final byte CAMPCELL = 1;
    static final byte CITCELL = 2;
    static final byte BOTH = 3;
    static final byte CAMPFAR = 4;

    static final byte BCELL = 1;

    private interface GetValue {
        public byte getValue(byte[][] b, Coordinates c, int i);
    }

    private interface GetDirection {
        public int getDirection(Coordinates c);
    }

    private static byte[][] initWhiteBoard() {
        return new byte[][] { 
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER },
            { OTHER, OTHER, CAMPFAR, OTHER, OTHER, OTHER, CAMPFAR, OTHER, OTHER },
            { OTHER, CAMPFAR, CAMPCELL, BOTH, OTHER, BOTH, CAMPCELL, CAMPFAR, OTHER },
            { OTHER, OTHER, BOTH, CITCELL, OTHER, CITCELL, BOTH, OTHER, OTHER },
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER },
            { OTHER, OTHER, BOTH, CITCELL, OTHER, CITCELL, BOTH, OTHER, OTHER },
            { OTHER, CAMPFAR, CAMPCELL, BOTH, OTHER, BOTH, CAMPCELL, CAMPFAR, OTHER },
            { OTHER, OTHER, CAMPFAR, OTHER, OTHER, OTHER, CAMPFAR, OTHER, OTHER },
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER } 
        };
    }

    private static byte[][] initBlackBoard() {
        return new byte[][] { 
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER },
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER },
            { OTHER, OTHER, BCELL, BCELL, OTHER, BCELL, BCELL, OTHER, OTHER },
            { OTHER, OTHER, BCELL, OTHER, OTHER, OTHER, BCELL, OTHER, OTHER },
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER },
            { OTHER, OTHER, BCELL, OTHER, OTHER, OTHER, BCELL, OTHER, OTHER },
            { OTHER, OTHER, BCELL, BCELL, OTHER, BCELL, BCELL, OTHER, OTHER },
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER },
            { OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER, OTHER }
        };
    }

    private static HashMap<Coordinates, Coordinates> initEscapesToPeak() {
        HashMap<Coordinates, Coordinates> escapesToPeak = new HashMap<>();
        escapesToPeak.put(new Coordinates(0, 2), new Coordinates(4, 1));
        escapesToPeak.put(new Coordinates(0, 6), new Coordinates(4, 7));
        escapesToPeak.put(new Coordinates(2, 0), new Coordinates(1, 4));
        escapesToPeak.put(new Coordinates(2, 8), new Coordinates(1, 4));
        escapesToPeak.put(new Coordinates(6, 0), new Coordinates(7, 4));
        escapesToPeak.put(new Coordinates(6, 8), new Coordinates(7, 4));
        escapesToPeak.put(new Coordinates(8, 2), new Coordinates(4, 1));
        escapesToPeak.put(new Coordinates(8, 6), new Coordinates(4, 7));
        return escapesToPeak;
    }

    public static Coordinates getPeakBlack(Coordinates c) {
        return escapesToPeak.get(c);
    }

    public static boolean isValidPositioning(byte[][] pawns, TablutAction action) {
        Coordinates pos = action.pawn.position;
        Coordinates dest = action.coordinates;
        if (whiteBoard[dest.row][dest.column] == OTHER)
            return false;
        pawns[pos.row][pos.column] = TablutState.EMPTY;
        boolean result = isValidPositioning(pawns, dest, (byte[][] p, Coordinates d, int i) -> p[d.row][i], (Coordinates c) -> c.column)
                || isValidPositioning(pawns, dest, (byte[][] p, Coordinates d, int i) -> p[i][dest.column], (Coordinates c) -> c.row);
        pawns[pos.row][pos.column] = action.pawn.getPawnType();
        return result;
    }

    private static boolean isValidPositioning(byte[][] pawns, Coordinates dest, GetValue gv, GetDirection gd) {
        boolean result = true;
        int whiteFound = 0;
        if (whiteBoard[dest.row][dest.column] == CAMPFAR && gv.getValue(whiteBoard, dest, 2) == OTHER)
            return false;
        for (int i = 0; i < TablutState.BOARD_SIZE; i++) {
            if (i != 4 && gv.getValue(whiteBoard, dest, i) != OTHER && (gv.getValue(pawns, dest, i) == TablutState.WHITE
                    || gv.getValue(pawns, dest, i) == TablutState.KING)) {
                if (i < 4 && gd.getDirection(dest) < 4 || i > 4 && gd.getDirection(dest) > 4) {
                    result = false;
                    break;
                } else
                    whiteFound++;
                if (i < 4)
                    i = 4;
                else if (whiteFound > 1) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

	public static boolean isBlackCell(TablutAction action) {
		return blackBoard[action.coordinates.row][action.coordinates.column] == BCELL;
	}
}
