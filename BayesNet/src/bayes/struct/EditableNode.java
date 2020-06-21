package bayes.struct;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.ConditionalProbabilityDistribution;
import aima.core.probability.bayes.Node;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class EditableNode implements Node {
    private RandomVariable variable = null;
    private Set<Node> parents = new LinkedHashSet<Node>();
    private Set<Node> children = new LinkedHashSet<Node>();

    public EditableNode(RandomVariable var) {
        this(var, (Node[]) null);
    }

    public EditableNode(RandomVariable var, Node... parents) {
        if (null == var) {
            throw new IllegalArgumentException(
                    "Random Variable for Node must be specified.");
        }
        this.variable = var;
        this.parents = new LinkedHashSet<Node>();
        if (null != parents) {
            for (Node p : parents) {
                ((EditableNode) p).addChild(this);
                this.parents.add(p);
            }
        }
    }

    //
    // START-Node
    @Override
    public RandomVariable getRandomVariable() {
        return variable;
    }

    @Override
    public boolean isRoot() {
        return 0 == getParents().size();
    }

    @Override
    public Set<Node> getParents() {
        return parents;
    }

    @Override
    public Set<Node> getChildren() {
        return children;
    }

    @Override
    public Set<Node> getMarkovBlanket() {
        LinkedHashSet<Node> mb = new LinkedHashSet<Node>();
        // Given its parents,
        mb.addAll(getParents());
        // children,
        mb.addAll(getChildren());
        // and children's parents
        for (Node cn : getChildren()) {
            mb.addAll(cn.getParents());
        }

        return mb;
    }

    public abstract ConditionalProbabilityDistribution getCPD();

    // END-Node
    //

    @Override
    public String toString() {
        return getRandomVariable().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (o == this) {
            return true;
        }

        if (o instanceof Node) {
            Node n = (Node) o;

            return getRandomVariable().equals(n.getRandomVariable());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return variable.hashCode();
    }


    protected void addChild(Node childNode) {
        children.add(childNode);
    }

    /**
     * From here all methods are implemented by :
     * - Iodice Francesco
     * - Valentino Di Cianni
     * - Carlo Alberto Barbano
     *
     **/

    public void removeChild(EditableNode childNode){
        this.children.remove(childNode);
    }

    public void removeParents(EditableNode parent){
        this.parents.remove(parent);
    }

}
