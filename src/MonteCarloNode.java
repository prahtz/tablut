import java.util.LinkedList;


public class MonteCarloNode<S, A> {
    private double utility;
    private int playoutsNumber;
    private boolean leaf;

    private S state;
    private A action;

    MonteCarloNode<S, A> parent = null;
    
    //keep oreded maybe
    //polymorphism for this set
    LinkedList<MonteCarloNode<S, A>> children = new LinkedList<>();;

    public MonteCarloNode(S state) {
        this.leaf = true;
        this.state = state;
        this.action = null;
    }
    
    public MonteCarloNode(S state, A action, MonteCarloNode<S, A> parent) {
        this.leaf = true;
        this.state = state;
        this.action = action;
        this.parent = parent;
    }
    
    public S getState() {
        return state;
    }

    public A getAction() {
        return action;
    }

	public LinkedList<MonteCarloNode<S, A>> getChildren() {
        return children;
    }

    public double getUtility() {
        return utility;
    }

    public int getPlayoutsNumber() {
        return playoutsNumber;
    }

	public MonteCarloNode<S, A> getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return leaf;
    }

    public void isLeaf(boolean leaf) {
        this.leaf = leaf;
	}

	public void addChild(MonteCarloNode<S, A> child) {
        children.addLast(child);
	}

	public MonteCarloNode<S, A> getFirstChild() {
		return children.getFirst();
	}

	public void updatePlayoutResults(double utility) {
        this.utility += utility;
        this.playoutsNumber++;
    }
    
    @Override
    public String toString() {
        if(state == null)
            return "NULL\n";
        return state.toString();
    }

	
}
