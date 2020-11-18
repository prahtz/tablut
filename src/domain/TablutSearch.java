package domain;

import java.util.LinkedList;

import montecarlo.*;

public class TablutSearch extends MonteCarloTreeSearch<TablutState, TablutAction> {

    public TablutSearch(TablutGame game, double timeout) {
        super(game, timeout);
        if(timeout > 2)
            this.end = this.end - 2000;
    }

    @Override
    public TablutAction monteCarloTreeSearch(TablutState state) {
        long start = System.currentTimeMillis();
        MonteCarloNode<TablutState, TablutAction> tree = new MonteCarloNode<>(state);
        boolean first = true;
        while(isTimeRemaining(start)) {
            LinkedList<MonteCarloNode<TablutState, TablutAction>> children = null;
            MonteCarloNode<TablutState, TablutAction> leaf = null;
            leaf = select(tree);
            children = expand(leaf);
            if(children.isEmpty()) {
                children = new LinkedList<>();
                leaf.isLeaf(true);
                children.add(leaf);
            }
            if(first && children.size() == 1) 
                break;
            for(MonteCarloNode<TablutState, TablutAction> child : children) {
                double result = simulate(child);
                backPropagate(result, child);
            }
            first = false;
        }
        
        System.out.println("Root values: " + tree.getUtility() + "/" + tree.getPlayoutsNumber());
        
        for(MonteCarloNode<TablutState, TablutAction> node : tree.getChildren())
            System.out.println(node.getAction().toString() + " " + node.getUtility() + "/" + node.getPlayoutsNumber() +
                " = " + node.getUtility()/node.getPlayoutsNumber());

        return bestAction(tree);   
    }

    @Override
    protected TablutAction bestAction(MonteCarloNode<TablutState, TablutAction> tree) {
        double bestValue = Double.NEGATIVE_INFINITY;
        TablutAction bestAction = null;
        for(MonteCarloNode<TablutState, TablutAction> child : tree.getChildren()) {
            double value = child.getPlayoutsNumber();
            if(value > bestValue) {
                bestValue = value;
                bestAction = child.getAction();
            }
        }
        return bestAction;
    }
}
