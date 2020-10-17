import java.util.LinkedList;

public class MonteCarloTreeSearch<S, A> {
    private MonteCarloGame<S, A> game;
    private long start;
    private long end;

    public MonteCarloTreeSearch(MonteCarloGame<S, A> game, int timeout) {
        this.game = game;
        this.end = timeout * 1000;
    }

    public A monteCarloTreeSearch(S state) {
        start = System.currentTimeMillis();
        MonteCarloNode<S, A> tree = new MonteCarloNode<>(state);
        while(isTimeRemaining()) {
            MonteCarloNode<S, A> leaf = select(tree);
            LinkedList<MonteCarloNode<S, A>> children = expand(leaf);
            if(children.isEmpty()) {
                children = new LinkedList<>();
                leaf.isLeaf(true);
                children.add(leaf);
            }
            for(MonteCarloNode<S, A> child : children) {
                double result = simulate(child);
                backPropagate(result, child);
            }
        }
        /*
        System.out.println("Root values: " + tree.getUtility() + "/" + tree.getPlayoutsNumber());
        for(MonteCarloNode<S, A> node : tree.getChildren())
            System.out.println(node.getAction().toString() + " " + node.getUtility() + "/" + node.getPlayoutsNumber() +
                " = " + node.getUtility()/node.getPlayoutsNumber());*/

        return bestAction(tree);   
    }

    //Generalize this
    private boolean isTimeRemaining() {  
        return System.currentTimeMillis() - start <= this.end;
    }

    private MonteCarloNode<S, A> select(MonteCarloNode<S, A> tree) {
        while(!tree.isLeaf()) { 
            double bestValue = Double.NEGATIVE_INFINITY;
            MonteCarloNode<S, A> bestChild= null;
            for(MonteCarloNode<S, A> child : tree.getChildren()) {
                double value = game.selectionPolicyValue(child);
                if(value > bestValue) {
                    bestValue = value;
                    bestChild = child;
                }
            }
            if(bestChild != null)
                tree = bestChild;
        }
        return tree;
    }

    private LinkedList<MonteCarloNode<S, A>> expand(MonteCarloNode<S, A> leaf) {
        leaf.isLeaf(false);
        for(A action : game.getActions(leaf.getState())) {
            MonteCarloNode<S, A> child = new MonteCarloNode<>(game.getNextState(leaf.getState(), action),
                action, leaf);
            leaf.addChild(child);
        }
        return leaf.getChildren();
    }

    private double simulate(MonteCarloNode<S, A> child) {
        return game.getPlayoutResult(child.getState());
    }

    private void backPropagate(double result, MonteCarloNode<S, A> child) {
        child.updatePlayoutResults(game.getUtility(child.getState(), result));
        while(child.getParent() != null) {
            child = child.getParent();
            child.updatePlayoutResults(game.getUtility(child.getState(), result));
        }
    }

    private A bestAction(MonteCarloNode<S, A> tree) {
        double bestValue = Double.NEGATIVE_INFINITY;
        A bestAction = null;
        for(MonteCarloNode<S, A> child : tree.getChildren()) {
            double value = child.getPlayoutsNumber();
            if(value > bestValue) {
                bestValue = value;
                bestAction = child.getAction();
            }
        }
        return bestAction;
    }

    //Generalize this
    public double selectionPolicyValue(MonteCarloNode<S, A> node) {
        double C = Math.sqrt(2);
        return (node.getUtility() / node.getPlayoutsNumber()) + C *
            Math.sqrt(Math.log(node.getParent().getPlayoutsNumber()) / node.getPlayoutsNumber());
    }

}
