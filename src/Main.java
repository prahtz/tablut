import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {/*
        byte[][] pawns = new byte[9][9];
        pawns[4][4] = TablutState.KING;
        pawns[4][3] = TablutState.WHITE;
        pawns[4][5] = TablutState.WHITE;
        pawns[3][4] = TablutState.WHITE;
        pawns[5][4] = TablutState.WHITE;
        pawns[0][0] = TablutState.BLACK;
        TablutState state = new TablutState(TablutState.WHITE, pawns);
        TablutAction a = new TablutAction(new Coordinates(2, 4), new Pawn(TablutState.WHITE, new Coordinates(3, 4)));
        System.out.println(state.toString());
        state = state.clone();
        state.makeAction(a);
        System.out.println(state.toString());
        */
        TablutState state = new TablutState(TablutState.WHITE);
        TablutGame game = new TablutGame(new int[]{0});
        MonteCarloTreeSearch<TablutState, TablutAction> mcts = new MonteCarloTreeSearch<>(game, 5);
        while(!state.isBlackWin() && !state.isWhiteWin() && !state.isDraw()) {
            TablutAction a = mcts.monteCarloTreeSearch(state);
            /*
            LinkedList<TablutAction> l = state.getLegalActions2();
            if(l.size() != state.getLegalActions().size()) {
                System.out.println("------------");
                for(TablutAction a1 : l) System.out.println(a1.toString());
                System.out.println(state.toString());
                break;
            }*/
            state = state.clone();
            if(a == null)
                break;
            state.makeAction(a);
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