import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.gson.Gson;

public class TablutClient {
    private Gson gson;

    private static final String WHITE = "WHITE";
    private static final String BLACK = "BLACK";
    private static final String BLACKWIN = "BLACKWIN";
    private static final String WHITEWIN = "WHITEWIN";
    private static final String DRAW = "BLACKWIN";
    
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

    private byte getPlayerTurn() {
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
    
    private int getTimeout() {
        return this.timeout;
    }

    
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        if(args.length < 3) {
            System.out.println("Not enough arguments");
            System.exit(-1);
        }
        TablutClient taprut = new TablutClient(args[0], Integer.parseInt(args[1]), args[2]);
        taprut.declareName();

        TablutGame game = new TablutGame();
        MonteCarloTreeSearch<TablutState, TablutAction> mcts = new MonteCarloTreeSearch<>(game, taprut.getTimeout());
        String myTurn = args[0].toUpperCase();
        String turn = myTurn;
        while(!turn.equals(TablutClient.BLACKWIN) && !turn.equals(TablutClient.WHITEWIN) 
            && !turn.equals(TablutClient.DRAW)) {
            taprut.read();
            turn = taprut.currentState.getTurn();
            if(taprut.currentState.getTurn().equals(myTurn)) {
                TablutState s = new TablutState(taprut.currentState.getPawnsBoard(), taprut.getPlayerTurn());
                TablutAction a = mcts.monteCarloTreeSearch(s);
                taprut.write(taprut.toServerAction(a));
                s = s.clone();
                s.makeAction(a);
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

    private ServerAction toServerAction(TablutAction a) {
        String from = String.valueOf((char)(a.getPawn().getPosition().getColumn() + 65));
        String to = String.valueOf((char)(a.getCoordinates().getColumn() + 65));
        from = from + (a.getPawn().getPosition().getRow() + 1);
        to = to + (a.getCoordinates().getRow() + 1);
        System.out.println("FROM " + from + "TO " + to);
        return new ServerAction(from, to);
    }
}