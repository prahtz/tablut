public class Main {
    public static void main(String[] args) {
        TablutState t = new TablutState();
        byte[][] board = t.getPawns();
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                board[i][j] = 0;
            }
        }

        board[2][5] = TablutState.KING;
        board[4][3] = TablutState.WHITE;
        board[3][2] = TablutState.WHITE;
        board[2][2] = TablutState.WHITE;
        board[5][2] = TablutState.WHITE;
        board[6][2] = TablutState.BLACK;

        board[4][0] = TablutState.BLACK;
        t = new TablutState(board);

        Coordinates move = new Coordinates(4, 2);
        Coordinates position = new Coordinates(4, 0);
        Pawn p = new Pawn(TablutState.BLACK, position);
        TablutAction a = new TablutAction(move, p);

        System.out.println(t.toString());

        t.makeAction(a);
        System.out.println(t.toString());
        

    }
}