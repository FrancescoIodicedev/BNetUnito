package bayes.struct;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.ConditionalProbabilityDistribution;
import aima.core.probability.bayes.ConditionalProbabilityTable;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;

public class FullCPTNodeEd extends EditableNode implements FiniteNode {
    private ConditionalProbabilityTable cpt;

    public FullCPTNodeEd(RandomVariable var, double[] distribution) {
        this(var, distribution, (Node[]) null);
    }

    public FullCPTNodeEd(RandomVariable var, double[] values, Node... parents) {
        super(var, parents);

        RandomVariable[] conditionedOn = new RandomVariable[getParents().size()];
        int i = 0;

        for (Node p : this.getParents()) {
            conditionedOn[i++] = p.getRandomVariable();
        }

        this.cpt = new EditableCPT(var, values, conditionedOn);
    }


    public ConditionalProbabilityDistribution getCPD() {
        return this.getCPT();
    }

    //CUSTOM METHOD

    @Override
    public ConditionalProbabilityTable getCPT() {
        return this.cpt;
    }



}


