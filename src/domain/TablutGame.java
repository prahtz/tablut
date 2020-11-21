package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import aima.core.util.Util;
import montecarlo.*;


public class TablutGame implements MonteCarloGame<TablutState, TablutAction> {
    private final double WHITE_WIN = 0;
    private final double BLACK_WIN = 1;
    private final double DRAW = 0.5;

    private final double WIN_WEIGHT = 1;
    private final double LOOSE_WEIGHT = 0;

    private final int MAX_MOVES = 100;


    private Integer[] weights;

    public TablutGame(Integer[] weights) {
        this.weights = weights;
    }

    @Override
    public LinkedList<TablutAction> getActions(TablutState state) {
        return state.getBestActionFirst();
    }

    @Override
    public TablutState getNextState(TablutState state, TablutAction action) {
        TablutState newState = state.clone();
        newState.makeAction(action);
        return newState;
    }

    @Override
    public double getPlayoutResult(TablutState state) {
        state = state.clone();
        ArrayList<SimulateAction> actions = new ArrayList<>();
        byte player = state.getPlayerTurn();
        int moves = 0;
        boolean abortSimulation = false;
        boolean print = false;
        
        while (!state.isWhiteWin() && !state.isBlackWin() && !state.isDraw() && !abortSimulation) {
            actions = state.getSimulatingActions(weights);
            Collections.shuffle(actions);
            if (!actions.isEmpty()) {
                double[] probDist = new double[actions.size()];
                int i = 0;
                for(SimulateAction sa : actions) {
                    probDist[i] = sa.getProb();
                    i++;
                }

                probDist = Util.normalize(probDist);
                double prob = ThreadLocalRandom.current().nextDouble();
                double totalSoFar = 0.0;
                TablutAction action = actions.get(actions.size() - 1);
                for (i = 0; i < probDist.length; i++) {
                    totalSoFar += probDist[i]; 
                    if (prob <= totalSoFar) {
                        action = actions.get(i);
                        break;
                    }
                }
                
                state = state.copySimulation();
                state.makeAction(action);
                player = state.getPlayerTurn();
                moves++;
                if(moves >= MAX_MOVES)
                    abortSimulation = true;
            }
            else
                break;
        }

        if(print && state.isWhiteWin())
            print = true;

        double result = DRAW;
        if(abortSimulation)
            return result;
        if (state.isWhiteWin()) 
            result = WHITE_WIN;
        else if (state.isBlackWin())
            result = BLACK_WIN;
        else if(state.isDraw())
            result = DRAW;
        else if(actions.isEmpty()) {
            if(player == TablutState.WHITE) 
                result = BLACK_WIN;
            else
                result = WHITE_WIN;
        }

        return result;
    }

    @Override
    public double getUtility(TablutState state, double result) {
        byte playerTurn = state.getPlayerTurn();
        if (result == DRAW)
            return result;
        if ((result == WHITE_WIN && playerTurn == TablutState.WHITE)
                || (result == BLACK_WIN && playerTurn == TablutState.BLACK))
            return LOOSE_WEIGHT;
        return WIN_WEIGHT;
    }

    @Override
    public double selectionPolicyValue(MonteCarloNode<TablutState, TablutAction> node) {
        double C = Math.sqrt(2);
        return (node.getUtility() / node.getPlayoutsNumber())
                + C * Math.sqrt(Math.log(node.getParent().getPlayoutsNumber()) / node.getPlayoutsNumber());
    }
}
