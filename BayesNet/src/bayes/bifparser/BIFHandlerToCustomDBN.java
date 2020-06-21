package bayes.bifparser;

import bayes.struct.CustomDBN;
import bayes.struct.EditableBayesianNetwork;
import bayes.struct.FullCPTNodeEd;

public class BIFHandlerToCustomDBN extends BIFHandlerToEBN<CustomDBN> {
    @Override
    protected CustomDBN getNetwork(FullCPTNodeEd... nodes) {
        return new CustomDBN(nodes);
    }
}
