package bayes.struct;

import aima.core.probability.RandomVariable;
import aima.core.probability.proposition.AssignmentProposition;

import java.util.Map;
import java.util.Set;

interface CustomFactor {
    //
    // START-Factor
    Set<RandomVariable> getArgumentVariables();

    public interface Factor {
        Set<RandomVariable> getArgumentVariables();

        boolean contains(RandomVariable var1);

        double[] getValues();

        aima.core.probability.Factor sumOut(RandomVariable... var1);

        aima.core.probability.Factor pointwiseProduct(aima.core.probability.Factor var1);

        aima.core.probability.Factor pointwiseProductPOS(aima.core.probability.Factor var1, RandomVariable... var2);

        void iterateOver(Iterator var1);

        void iterateOver(Iterator var1, AssignmentProposition... var2);

        public interface Iterator {
            void iterate(Map<RandomVariable, Object> var1, double var2);
        }
    }


}
