package domain;

import java.io.Serializable;

public class ServerState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    String[][] board;
    String turn;

    public ServerState() {
		super();
	}

    public ServerState(String[][] board, String turn) {
        this.board = board;
        this.turn = turn;
    }

    public String getTurn() {
        return this.turn;
    }

    public byte[][] getPawnsBoard() {
        byte[][] pawns = new byte[TablutState.BOARD_SIZE][TablutState.BOARD_SIZE];
        for(int i = 0; i < board.length; i++) 
            for(int j = 0; j < board.length; j++) {
                if(board[i][j].equals("BLACK"))
                    pawns[i][j] = TablutState.BLACK;
                else if(board[i][j].equals("WHITE"))
                    pawns[i][j] = TablutState.WHITE;
                else if(board[i][j].equals("KING"))
                    pawns[i][j] = TablutState.KING;
            }
        return pawns;
    }

    @Override
    public String toString() {
        String result = "";
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) 
                result += board[i][j];
            result += "\n";
        }
        result += "\n";
        return result;
    }
}
