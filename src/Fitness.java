import java.util.List;

import aima.core.search.local.FitnessFunction;
import aima.core.search.local.Individual;

public class Fitness implements FitnessFunction<Integer> {

    @Override
    public double apply(Individual<Integer> individual) {
        List<Integer> weightsList = individual.getRepresentation();

        int[] weights = new int[weightsList.size()];
        for(int i = 0; i < weightsList.size(); i++) 
            weights[i] = weightsList.get(i);
        for(int i = 0; i < 5; i++) {
            System.out.println("IND: " + weights[i]);
        }
        GeneticClient gc = new GeneticClient(weights, TablutState.WHITE, 5);
        Metrics metrics = gc.run();
        
        int sign = 1;
        if(metrics.getResult() == Metrics.LOOSE)
            sign = -1;
        if(metrics.getMovesNumber() == 0) {
            System.out.println("Value: -2000");
            return -2000;
        }
        
        System.out.println("RESULT: " + metrics.getResult() + " CAPTURED: " + metrics.getPawnsCaptured() + " PLOST: " + metrics.getPawnsLost() + " MNUMB: " + metrics.getMovesNumber());
        System.out.print("Value: ");
        System.out.println(1000 * metrics.getResult() + 10 * metrics.getPawnsCaptured() - 10 * metrics.getPawnsLost() - (sign * metrics.getMovesNumber()));
        
        return 1000 * metrics.getResult() + 10 * metrics.getPawnsCaptured() - 10 * metrics.getPawnsLost() - (sign * metrics.getMovesNumber());
    }

    
}
