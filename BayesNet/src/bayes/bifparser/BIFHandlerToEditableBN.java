package bayes.bifparser;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.domain.FiniteIntegerDomain;
import aima.core.probability.util.RandVar;
import bnparser.bif.BIFDefinition;
import bnparser.bif.BIFVariable;
import bnparser.bif.FileSection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bayes.struct.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler, used to parse the XML BIF files.
 * This Handler it was customized to work with class in bayes.struct package;
 *
 */
public class BIFHandlerToEditableBN extends BIFHandlerToEBN<EditableBayesianNetwork> {

	@Override
	protected EditableBayesianNetwork getNetwork(FullCPTNodeEd... nodes) {
		return new EditableBayesianNetwork(nodes);
	}
}