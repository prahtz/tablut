public class Main {    
    public static void main(String[] args) {
        TablutState state = new TablutState();
        TablutGame game = new TablutGame();
        MonteCarloTreeSearch<TablutState, TablutAction> mcts = new MonteCarloTreeSearch<>(game, 5);
        

        System.out.println(state.getDrawConditions().size());
        TablutAction action = new TablutAction(new Coordinates(3, 3), new Pawn(TablutState.WHITE, new Coordinates(3, 4)));
        state = state.clone();
        state.makeAction(action);
        System.out.println(state.isDraw());
        System.out.println(state.getDrawConditions().size());
        System.out.println(state.toString());

        action = new TablutAction(new Coordinates(2, 3), new Pawn(TablutState.WHITE, new Coordinates(2, 4)));
        state = state.clone();
        state.makeAction(action);
        System.out.println(state.isDraw());
        System.out.println(state.getDrawConditions().size());
        System.out.println(state.toString());

        action = new TablutAction(new Coordinates(2, 4), new Pawn(TablutState.BLACK, new Coordinates(1, 4)));
        state = state.clone();
        state.makeAction(action);
        System.out.println(state.isDraw());
        System.out.println(state.getDrawConditions().size());
        System.out.println(state.toString());

        action = new TablutAction(new Coordinates(3, 4), new Pawn(TablutState.WHITE, new Coordinates(3, 3)));
        action.addCapture(new Coordinates(2, 4));
        state = state.clone();
        state.makeAction(action);
        System.out.println(state.isDraw());
        System.out.println(state.getDrawConditions().size());
        System.out.println(state.toString());
        /*
        while(!state.isBlackWin() && !state.isWhiteWin()) {
            TablutAction action = mcts.monteCarloTreeSearch(state);
            state = state.clone();
            state.makeAction(action);
            System.out.println(state.toString());
        }*/
        /*
        byte[][] pawns = new byte[9][9];
        pawns[2][2] = TablutState.BLACK;
        pawns[2][3] = TablutState.BLACK;
        pawns[2][5] = TablutState.BLACK;

        pawns[3][3] = TablutState.KING;
        pawns[3][4] = TablutState.BLACK;

        pawns[4][6] = TablutState.BLACK;

        pawns[5][2] = TablutState.BLACK;
        pawns[5][4] = TablutState.WHITE;
        
        pawns[6][2] = TablutState.BLACK;
        pawns[6][3] = TablutState.BLACK;
        pawns[6][8] = TablutState.BLACK;

        state = new TablutState(pawns, TablutState.BLACK);
        TablutAction action = mcts.monteCarloTreeSearch(state);
        state = state.clone();
        state.makeAction(action);
        System.out.println(state.toString());
        */

    }
}