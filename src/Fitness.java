import java.util.List;

import aima.core.search.local.FitnessFunction;
import aima.core.search.local.Individual;

public class Fitness implements FitnessFunction<Integer> {

    private final int PLAYOUTS_NUMBER = 10;

    @Override
    public double apply(Individual<Integer> individual) {
        List<Integer> weightsList = individual.getRepresentation();

        int[] weights = new int[weightsList.size()];
        for(int i = 0; i < weightsList.size(); i++) 
            weights[i] = weightsList.get(i);

        /*
        for(int i = 0; i < 5; i++) {
            System.out.println("IND: " + weights[i]);
        }*/
        GeneticClient gc = new GeneticClient(weights, TablutState.WHITE, 10);
        Metrics metrics = null;
        int wins = 0;
        int draws = 0;
        for(int i = 0; i < PLAYOUTS_NUMBER; i++) {
            metrics = gc.run();
            if(metrics.getResult() == Metrics.WIN)
                wins++;
            else if(metrics.getResult() == Metrics.DRAW)
                draws++;
        }

        double result = (wins + 0.5 * draws) / PLAYOUTS_NUMBER;
        return result;
    }

    
}
