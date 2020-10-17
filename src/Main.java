public class Main {    
    public static void main(String[] args) {
        
        TablutState state = new TablutState(TablutState.WHITE);
        System.out.println(state.toString());
        TablutGame game = new TablutGame(new int[]{0});
        MonteCarloTreeSearch<TablutState, TablutAction> mcts = new MonteCarloTreeSearch<>(game, 5);
        mcts.monteCarloTreeSearch(state);

    }
}