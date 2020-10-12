import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.gson.Gson;

public class TablutClient {
    private Gson gson;
    
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
        role = role.toLowerCase();
        int port = whitePort;
        this.playerTurn = TablutState.WHITE;
        if(role.equals("black")) {
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
    
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        if(args.length < 3) {
            System.out.println("Not enough arguments");
            System.exit(-1);
        }
        TablutClient taprut = new TablutClient(args[0], Integer.parseInt(args[1]), args[2]);
        taprut.declareName();

        while(true) {
			taprut.read();
            TablutState s = new TablutState(taprut.currentState.getPawnsBoard(), taprut.getPlayerTurn());
            System.out.println(s.toString());
            ServerAction ac = new ServerAction("E3", "D3");
            taprut.write(ac);
        }
    }
}
