package genetic;

import java.util.List;

import aima.core.search.local.FitnessFunction;
import aima.core.search.local.Individual;

import clients.GeneticClient;
import domain.*;

public class TablutFitness implements FitnessFunction<Integer> {

    private final int PLAYOUTS_NUMBER = 10;

    @Override
    public double apply(Individual<Integer> individual) {
        List<Integer> weightsList = individual.getRepresentation();

        Integer[] weights = new Integer[weightsList.size()];
        for(int i = 0; i < weightsList.size(); i++) 
            weights[i] = weightsList.get(i);

        GeneticClient gc = new GeneticClient(weights, TablutState.WHITE, 10);
        TablutMetrics metrics = null;
        int wins = 0;
        int draws = 0;
        for(int i = 0; i < PLAYOUTS_NUMBER; i++) {
            metrics = gc.run();
            if(metrics.getResult() == TablutMetrics.WIN)
                wins++;
            else if(metrics.getResult() == TablutMetrics.DRAW)
                draws++;
        }

        double result = (wins + 0.5 * draws) / PLAYOUTS_NUMBER;
        return result;
    }

    
}
