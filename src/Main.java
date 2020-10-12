public class Main {    
    public static void main(String[] args) {
        TablutState state = new TablutState();
        TablutGame game = new TablutGame();
        MonteCarloTreeSearch<TablutState, TablutAction> mcts = new MonteCarloTreeSearch<>(game, 10);
        System.out.println(state.getLegalActions().size());
        while(!state.isBlackWin() && !state.isWhiteWin()) {
            TablutAction action = mcts.monteCarloTreeSearch(state);
            state = state.clone();
            state.makeAction(action);
            System.out.println(state.toString());
        }
    }
}