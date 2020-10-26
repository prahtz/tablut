import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        
        byte[][] pawns = new byte[9][9];
        /*
        pawns[3][2] = TablutState.WHITE;
        pawns[2][6] = TablutState.BLACK;
        pawns[1][5] = TablutState.WHITE;
        pawns[0][8] = TablutState.BLACK;
        */
        pawns[1][2] = TablutState.BLACK;
        pawns[2][2] = TablutState.WHITE;
        pawns[3][6] = TablutState.BLACK;
        pawns[3][8] = TablutState.BLACK;
        pawns[4][6] = TablutState.WHITE;

        /*
        TablutAction a = new TablutAction(new Coordinates(2, 6), new Pawn(TablutState.WHITE, new Coordinates(2, 2)));
        a.addCapture(new Capture(new Pawn(TablutState.BLACK, new Coordinates(3,6))));
        TablutState state = new TablutState(TablutState.WHITE, pawns);
        LinkedList<TablutAction> actions = state.getLegalActions(TablutState.WHITE);
        actions.addAll(state.getLegalActions(TablutState.BLACK));
        state.printList(actions);
        System.out.println(state.toString());

        state.makeAction(a);
        actions = state.getLegalActions(TablutState.WHITE);
        actions.addAll(state.getLegalActions(TablutState.BLACK));
        state.printList(actions);
        System.out.println(state.toString());
        */
        
        TablutState state = new TablutState(TablutState.WHITE);
        TablutGame game = new TablutGame(new int[]{0});
        
        MonteCarloTreeSearch<TablutState, TablutAction> mcts = new MonteCarloTreeSearch<>(game, 5);
        while(!state.isBlackWin() && !state.isWhiteWin() && !state.isDraw()) {
            TablutAction a = mcts.monteCarloTreeSearch(state);
            LinkedList<TablutAction> list = state.getBestActionFirst(new int[]{0});
            for(TablutAction action : list) {
                System.out.print(action + " - " + action.getValue() + " oldCap: " + action.oldCaptures + " newCap: " 
                    + action.newCaptures + " oldLoss: " + action.oldLoss + " newLoss: " + action.newLoss + " willBeCap: " + action.isWillBeCaptured() + "\n");
            }

            state = state.clone();
            if(a == null)
                break;
            state.makeAction(a);

            

            LinkedList<TablutAction> actions = state.getLegalActions();
            actions.addAll(state.getLegalActions(state.getPlayerTurn() == TablutState.BLACK ? TablutState.WHITE : TablutState.BLACK));
            HashMap<Capture, Integer> capturesMap = new HashMap<>();
            for(TablutAction a1 : actions) 
                for(Capture c : a1.getCaptured())
                    state.updateCaptureMap(capturesMap, c, true);
            for(Map.Entry<Capture, Integer> e : capturesMap.entrySet()) {
                System.out.println(e.getKey().toString());
            }
            
            System.out.println(state.toString());
        }
        System.out.println(state.toString());
        System.out.println(state.isBlackWin() + " " + state.isWhiteWin() + " " + state.isDraw());

        
        /*
        //TablutState state = new TablutState(TablutState.WHITE);
        TablutAction a = new TablutAction(new Coordinates(1, 2), new Pawn(TablutState.WHITE, new Coordinates(4, 2)));
        long start = System.currentTimeMillis();
        TablutState state = new TablutState(TablutState.WHITE);
        TablutState s = state.clone();
        s.makeAction(a);
        state.getBestActionFirst(new int[]{0});
        long end = System.currentTimeMillis();
        
        
        System.out.println(s.toString());
        System.out.println(end - start);
        //System.out.println(s.getLegalActions(TablutState.WHITE).size());*/
        
    }
}