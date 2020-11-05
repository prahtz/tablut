import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import com.google.gson.Gson;

public class TablutClient {
    private Gson gson;

    public static final String WHITE = "WHITE";
    public static final String BLACK = "BLACK";
    public static final String BLACKWIN = "BLACKWIN";
    public static final String WHITEWIN = "WHITEWIN";
    public static final String DRAW = "DRAW";
    
    private ServerState currentState;
    private Socket playerSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private int timeout;
    private byte playerTurn;

    private final String name = "TaPrut";
    private final int whitePort = 5800;
    private final int blackPort = 5801;
    
    public TablutClient(String role, int timeout, String serverIp) throws UnknownHostException, IOException {
        this.gson = new Gson();
        role = role.toUpperCase();
        int port = whitePort;
        this.playerTurn = TablutState.WHITE;
        if(role.equals("BLACK")) {
            port = blackPort;
            this.playerTurn = TablutState.BLACK;
        }
        this.timeout = timeout;
        this.playerSocket = new Socket(serverIp, port);
		this.out = new DataOutputStream(playerSocket.getOutputStream());
		this.in = new DataInputStream(playerSocket.getInputStream());
    }

    public byte getPlayerTurn() {
        return this.playerTurn;
    }

    public void write(ServerAction action) throws IOException, ClassNotFoundException {
		StreamUtils.writeString(out, this.gson.toJson(action));
	}

	public void declareName() throws IOException, ClassNotFoundException {
		StreamUtils.writeString(out, this.gson.toJson(this.name));
	}

	public void read() throws ClassNotFoundException, IOException {
		this.currentState = this.gson.fromJson(StreamUtils.readString(in), ServerState.class);
    }
    
    public int getTimeout() {
        return this.timeout;
    }

    public ServerAction toServerAction(TablutAction a) {
        String from = String.valueOf((char)(a.pawn.position.column + 65));
        String to = String.valueOf((char)(a.coordinates.column + 65));
        from = from + (a.pawn.position.row + 1);
        to = to + (a.coordinates.row + 1);
        System.out.println("FROM " + from + "TO " + to);
        return new ServerAction(from, to);
    }

    public TablutAction searchFirstAction() {
        byte[][] pawns = currentState.getPawnsBoard();
        Coordinates toPos = null;
        for(int i = 0; i < TablutState.BOARD_SIZE; i++) {
            for(int j = 0; j < TablutState.BOARD_SIZE; j++) {
                if(pawns[i][j] == TablutState.WHITE && i != 4 && j != 4) {
                    toPos = new Coordinates(i, j);
                    break;
                }
            }
        }
        Coordinates fromPos = null;
        for(int k = 2; k < TablutState.BOARD_SIZE - 2; k++) {
            if(pawns[4][k] == TablutState.EMPTY) {
                fromPos = new Coordinates(4, k);
                break;
            }
            if(pawns[k][4] == TablutState.EMPTY) {
                fromPos = new Coordinates(k, 4);
                break;
            }
        }
        return new TablutAction(toPos, new Pawn(TablutState.WHITE, fromPos));
    }
    
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        if(args.length < 3) {
            System.out.println("Not enough arguments");
            System.exit(-1);
        }
        TablutClient taprut = new TablutClient(args[0], Integer.parseInt(args[1]), args[2]);
        taprut.declareName();

        int[] weights = new int[]{0,0,0,0,0};
        TablutGame game = new TablutGame(weights);
        MonteCarloTreeSearch<TablutState, TablutAction> mcts = new MonteCarloTreeSearch<>(game, taprut.getTimeout());
        String myTurn = args[0].toUpperCase();
        String turn = myTurn;
        boolean firstMove = true;
        TablutAction firstAction = null;
        LinkedList<TablutState> drawConditions = new LinkedList<>();
        while(!turn.equals(TablutClient.BLACKWIN) && !turn.equals(TablutClient.WHITEWIN) 
            && !turn.equals(TablutClient.DRAW)) {
            taprut.read();
            turn = taprut.currentState.getTurn();
            if(taprut.currentState.getTurn().equals(myTurn)) {
                if(firstMove && taprut.getPlayerTurn() == TablutState.BLACK) 
                    firstAction = taprut.searchFirstAction();
                TablutState s = new TablutState(taprut.currentState.getPawnsBoard(), taprut.getPlayerTurn(), firstMove, firstAction, drawConditions);
                System.out.println(s.toString());
                TablutAction a = mcts.monteCarloTreeSearch(s);
                System.out.println(s.getDrawConditions().size());
                taprut.write(taprut.toServerAction(a));
                s = s.clone();
                s.makeAction(a);
                firstMove = false;
                drawConditions = s.getDrawConditions();
                System.out.println(s.toString());
            }
        }
        
        if(turn.equals(TablutClient.DRAW)) 
            System.out.println("It's a draw!");
        else if(turn.equals(TablutClient.WHITEWIN) && myTurn.equals(TablutClient.WHITE)
            || turn.equals(TablutClient.BLACKWIN) && myTurn.equals(TablutClient.BLACK))
            System.out.println("You win!");
        else 
            System.out.println("You loose!");
    }

    public ServerState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ServerState currentState) {
        this.currentState = currentState;
    }
}