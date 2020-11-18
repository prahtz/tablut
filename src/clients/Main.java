package clients;

import java.io.IOException;

import domain.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {   
        TablutState state = new TablutState(TablutState.WHITE);
        TablutGame game = new TablutGame(Weights.getWeights());
        
        TablutSearch mcts = new TablutSearch(game, 10);
        while(!state.isBlackWin() && !state.isWhiteWin() && !state.isDraw()) {
            TablutAction a = mcts.monteCarloTreeSearch(state);
            
            state = state.clone();
            if(a == null)
                break;
            state.makeAction(a);
            System.out.println(state.toString());   
        }
        System.out.println(state.toString());
        System.out.println(state.isBlackWin() + " " + state.isWhiteWin() + " " + state.isDraw());
    }
}