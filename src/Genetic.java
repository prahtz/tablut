import java.util.Collection;

import aima.core.search.local.GeneticAlgorithm;

public class Genetic extends GeneticAlgorithm<Integer> {

    public Genetic(int individualLength, Collection<Integer> finiteAlphabet, double mutationProbability) {
        super(individualLength, finiteAlphabet, mutationProbability);
    }

  
}
