package bayes.utils;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesInference;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import bayes.struct.EditableBayesianNetwork;
import bayes.struct.EditableCPT;
import bayes.struct.EditableNode;
import bayes.struct.FullCPTNodeEd;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class CustomEliminationAsk implements BayesInference {

    Set<RandomVariable> irrelevantNode = new LinkedHashSet<>();
    // RandomVarFather -> List(RandomVarChildren) : Probability Table reduced by pruning edges
    Map<RandomVariable, ArrayList<Map<RandomVariable, ProbabilityTable>>> arcIrrelevant = new HashMap<>();
    protected boolean pruneEdge;
    protected boolean pruneNode;
    protected boolean minDegreeOrder;
    protected boolean minFillOrder;


    private static final ProbabilityTable _identity = new ProbabilityTable(
            new double[]{1.0});

    // By defualt prune edges and nodes

    public CustomEliminationAsk() {
        pruneEdge = true;
        pruneNode = true;
    }

    // function ELIMINATION-ASK(X, e, bn) returns a distribution over X

    /**
     * The ELIMINATION-ASK algorithm in Figure 14.11.
     *
     * @param X  the query variables.
     * @param e  observed values for variables E.
     * @param bn a Bayes net with variables {X} &cup; E &cup; Y /* Y = hidden
     *           variables //
     * @return a distribution over the query variables.
     */
    public CategoricalDistribution eliminationAsk(final RandomVariable[] X,
                                                  final AssignmentProposition[] e, final BayesianNetwork bn) {

        Set<RandomVariable> hidden = new HashSet<RandomVariable>();
        List<RandomVariable> VARS = new ArrayList<RandomVariable>();

        calculateVariables(X, e, bn, hidden, VARS);
        // factors <- []
        List<Factor> factors = new ArrayList<Factor>();
        for (RandomVariable var : order(bn, VARS)) {
            // factors <- [MAKE-FACTOR(var, e) | factors]
            factors.add(0, makeFactor(var, e, bn));
        }
        List<RandomVariable> orderedVars = orderFactors(factors, bn, e);

        for (RandomVariable var : orderedVars) {
            // if var is hidden variable then factors <- SUM-OUT(var, factors)
            if (hidden.contains(var)) {
                factors = sumOut(var, factors, bn);
            }
        }
        // return NORMALIZE(POINTWISE-PRODUCT(factors))
        Factor product = pointwiseProduct(factors);
        // Note: Want to ensure the order of the product matches the
        // query variables
        return ((ProbabilityTable) product.pointwiseProductPOS(_identity, X))
                .normalize();
    }


    //
    // START-BayesInference
    public CategoricalDistribution ask(final RandomVariable[] X,
                                       final AssignmentProposition[] observedEvidence,
                                       final BayesianNetwork bn) {
        if (!(bn instanceof EditableBayesianNetwork)) {
            throw new IllegalArgumentException(
                    "Custom Elimination-Ask only works with Editable Bayes Net.");
        }
        //Pruning of nodes and edges
        System.out.println("N. var: " + bn.getVariablesInTopologicalOrder().size());
        optimazedNet((EditableBayesianNetwork) bn, X, observedEvidence);
        System.out.println("N. var optimized: " + bn.getVariablesInTopologicalOrder().size());

        Instant start = Instant.now();
        CategoricalDistribution categoricalDistribution = this.eliminationAsk(X, observedEvidence, bn);
        Instant finish = Instant.now();
        System.out.println("Execution Time : " + Duration.between(start, finish).toNanos()/1000 + " microseconds");
        return categoricalDistribution;
    }

    // END-BayesInference
    //

    //
    // PROTECTED METHODS
    //

    /**
     * <b>Note:</b>Override this method for a more efficient implementation as
     * outlined in AIMA3e pgs. 527-28. Calculate the hidden variables from the
     * Bayesian Network. The default implementation does not perform any of
     * these.<br>
     * <br>
     * Two calcuations to be performed here in order to optimize iteration over
     * the Bayesian Network:<br>
     * 1. Calculate the hidden variables to be enumerated over. An optimization
     * (AIMA3e pg. 528) is to remove 'every variable that is not an ancestor of
     * a query variable or evidence variable as it is irrelevant to the query'
     * (i.e. sums to 1). 2. The subset of variables from the Bayesian Network to
     * be retained after irrelevant hidden variables have been removed.
     * <p>
     * In this method a set of irrelevant nodes is filled for the query
     *
     * @param X      the query variables.
     * @param e      observed values for variables E.
     * @param bn     a Bayes net with variables {X} &cup; E &cup; Y /* Y = hidden
     *               variables //
     * @param hidden to be populated with the relevant hidden variables Y.
     * @param bnVARS to be populated with the subset of the random variables
     *               comprising the Bayesian Network with any irrelevant hidden
     *               variables removed.
     */
    protected void calculateVariables(final RandomVariable[] X,
                                      final AssignmentProposition[] e, final BayesianNetwork bn,
                                      Set<RandomVariable> hidden, Collection<RandomVariable> bnVARS) {
        bnVARS.addAll(bn.getVariablesInTopologicalOrder());
        hidden.addAll(bnVARS);

        for (RandomVariable x : X) {
            hidden.remove(x);
        }
        for (AssignmentProposition ap : e) {
            hidden.removeAll(ap.getScope());
        }
        return;
    }


    /**
     * <b>Note:</b>Override this method for a more efficient implementation as
     * outlined in AIMA3e pgs. 527-28. The default implementation does not
     * perform any of these.<br>
     *
     * @param bn   the Bayesian Network over which the query is being made. Note,
     *             is necessary to provide this in order to be able to determine
     *             the dependencies between variables.
     * @param vars a subset of the RandomVariables making up the Bayesian
     *             Network, with any irrelevant hidden variables alreay removed.
     * @return a possibly opimal ordering for the random variables to be
     * iterated over by the algorithm. For example, one fairly effective
     * ordering is a greedy one: eliminate whichever variable minimizes
     * the size of the next factor to be constructed.
     */
    protected List<RandomVariable> order(BayesianNetwork bn,
                                         Collection<RandomVariable> vars) {
        // Note: Trivial Approach:
        // For simplicity just return in the reverse order received,
        // i.e. received will be the default topological order for
        // the Bayesian Network and we want to ensure the network
        // is iterated from bottom up to ensure when hidden variables
        // are come across all the factors dependent on them have
        // been seen so far.
        List<RandomVariable> order = new ArrayList<RandomVariable>(vars);
        Collections.reverse(order);

        return order;
    }

    //
    // PRIVATE METHODS
    //
    private Factor makeFactor(RandomVariable var, AssignmentProposition[] e,
                              BayesianNetwork bn) {

        Node n = bn.getNode(var);
        if (!(n instanceof FullCPTNodeEd)) {
            throw new IllegalArgumentException(
                    "Custom Elimination-Ask only works with finite Editable.");
        }
        FullCPTNodeEd fn = (FullCPTNodeEd) n;
        List<AssignmentProposition> evidence = new ArrayList<AssignmentProposition>();
        for (AssignmentProposition ap : e) {
            if (fn.getCPT().contains(ap.getTermVariable())) {
                evidence.add(ap);
            }
        }

        return fn.getCPT().getFactorFor(
                evidence.toArray(new AssignmentProposition[evidence.size()]));
    }

    private List<Factor> sumOut(RandomVariable var, List<Factor> factors,
                                BayesianNetwork bn) {
        List<Factor> summedOutFactors = new ArrayList<Factor>();
        List<Factor> toMultiply = new ArrayList<Factor>();
        for (Factor f : factors) {
            if (f.contains(var)) {
                toMultiply.add(f);
            } else {
                // This factor does not contain the variable
                // so no need to sum out - see AIMA3e pg. 527.
                summedOutFactors.add(f);
            }
        }

        summedOutFactors.add(pointwiseProduct(toMultiply).sumOut(var));

        return summedOutFactors;
    }

    private Factor pointwiseProduct(List<Factor> factors) {

        Factor product = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            product = product.pointwiseProduct(factors.get(i));
        }

        return product;
    }

    /**
     *
     * From here all methods are implemented by :
     *  - Iodice Francesco
     *  - Valentino Di Cianni
     *  - Carlo Alberto Barbano
     *
     * ************************************************************************************
     *
     * Makes pruning of irrelevant nodes and egeds for P(X|e) from bayes net bn
     * if are set.
     *
     * @param bn bayes net to prune
     * @param X query var
     * @param e evidence var
     *
     */

    private void optimazedNet(EditableBayesianNetwork bn, RandomVariable[] X, AssignmentProposition[] e) {
        Set<Node> nodesOfEvidences = new LinkedHashSet<>();
        Set<Node> nodesOfQueryVar = new LinkedHashSet<>();

        for (RandomVariable rv : X) {
            nodesOfQueryVar.add(bn.getNode(rv));
        }
        for (AssignmentProposition ap : e) {
            nodesOfEvidences.add(bn.getNode(ap.getTermVariable()));
        }
        if (pruneNode) {
            pruneNodes(bn, nodesOfQueryVar, nodesOfEvidences);
        }
        if (pruneEdge) {
            pruneEdges(bn, e);
        }
    }

    /**
     *
     * Makes pruning of irrelevant nodes
     *
     * @param bn bayes net
     * @param nodesOfQueryVar vars of query
     * @param nodesOfEvidences vars of evidences
     */
    private void pruneNodes(EditableBayesianNetwork bn, Set<Node> nodesOfQueryVar, Set<Node> nodesOfEvidences) {
        System.out.println("Pruno i nodi..");

        Set<Node> relevantNodeForQuery = new LinkedHashSet<>();
        relevantNodeForQuery.addAll(nodesOfEvidences);
        relevantNodeForQuery.addAll(nodesOfQueryVar);

        //if(!bn.isMultiConnected()){
        findVarNotAncestors(relevantNodeForQuery, bn);
        System.out.println("Ho trovato " + irrelevantNode.size() + " nodi irrilevanti (ancestor based)");
        for (RandomVariable irr : irrelevantNode) {
            bn.removeNode(irr);
        }


        irrelevantNode = new HashSet<>();
        findVarMSeparated(nodesOfQueryVar, nodesOfEvidences, bn);
        System.out.println("Ho trovato " + irrelevantNode.size() + " nodi irrilevanti (m-separated based)");
        for (RandomVariable irr : irrelevantNode) {
            bn.removeNode(irr);
        }
        //}
    }


    /**
     *  This method fill the irrelevant node with all nodes not ancestor
     *
     * @param relevantNodeForQuery union of set E and X
     * @param bn bayes net
     */
    private void findVarNotAncestors(Set<Node> relevantNodeForQuery, EditableBayesianNetwork bn) {

        Set<Node> ancestors = getAllAncestorOfCurrentNodes(relevantNodeForQuery, new LinkedHashSet<>());
        //This loop remove all not ancesteror of n in relevantNodeForQuery
        for (RandomVariable r : bn.getVariablesInTopologicalOrder()) {
            if (!ancestors.contains(bn.getNode(r))) {
                irrelevantNode.add(r);
            }
        }

    }

    /**
     * This method find all ancestor of currentNodes
     *
     * @param currentNodes set of node on which you want to find the ancestors
     * @param allAncestor used as an accumulator
     *
     * @return allAncestor var filled of all ancestors of currentNodes
     */
    private Set<Node> getAllAncestorOfCurrentNodes(Set<Node> currentNodes, Set<Node> allAncestor) {
        for (Node n : currentNodes) {
            if (n!= null && n.getParents().size() > 0)
                allAncestor.addAll(getAllAncestorOfCurrentNodes(n.getParents(), allAncestor));
            allAncestor.add(n);
        }
        return allAncestor;
    }


    /**
     *  This method fill the irrelevant node with all nodes not ancestor
     *
     * @param nodesOfQueryVar query vars
     * @param nodesOfEvidences evidence vars
     * @param bn current net
     */
    private void findVarMSeparated(Set<Node> nodesOfQueryVar, Set<Node> nodesOfEvidences, BayesianNetwork bn) {
        //System.out.println("Cerco moral graph");
        MoralGraph graph = new MoralGraph(bn);
        graph.pruneNetOnEvidence(nodesOfEvidences);
       // System.out.println("moral graph ok");

        for (RandomVariable r : bn.getVariablesInTopologicalOrder()) {
            if (!nodesOfEvidences.contains(bn.getNode(r)) && !nodesOfQueryVar.contains(bn.getNode(r))) {
                if (isMSeparated(graph, bn.getNode(r), nodesOfQueryVar)) {
                    irrelevantNode.add(r);
                }
            }
        }
        //System.out.println("asd");
    }

    /**
     *
     * This method return true if irrelevant is m-separated from query var by evidence
     * If there is a path between with a var of evidences return false
     * evidence isn't in the param because the moral graph are removed all
     * vars in e
     *
     * @param graph moral graph
     * @param irrelevant var to check if is irrelevant
     * @param queryVar vars of query
     *
     * @return true if irrelevent is m-separated from
     *
     */
    public boolean isMSeparated(MoralGraph graph, Node irrelevant, Set<Node> queryVar) {

        ArrayList<Boolean> goodPathsForSource = new ArrayList<>(queryVar.size());
        for (Node x : queryVar) {
            LinkedList<LinkedList<Node>> paths = graph.getAllPaths(irrelevant, x);
            goodPathsForSource.add(paths.size() == 0);
        }
        return goodPathsForSource.stream().reduce(Boolean::logicalAnd).orElse(false);
    }


    /**
     *
     * Makes a pruning of arcs of the neighbors of vars in e
     * reduce the CPT of children of var in e making a factor by evidence
     *
     * @param bn bn
     * @param e evidencs
     */
    private void pruneEdges(EditableBayesianNetwork bn, AssignmentProposition[] e) {
        System.out.println("Cerco archi irrilevanti..");
        findArcIrrelevant(bn, e);
        System.out.println("Ho trovato " + arcIrrelevant.size() + " archi irrilevanti");

        for (RandomVariable evidence : arcIrrelevant.keySet()) {
            ArrayList<Map<RandomVariable, ProbabilityTable>> arcsList = arcIrrelevant.get(evidence);
            EditableNode fatherNode = (EditableNode) bn.getNode(evidence);
            for (int i = 0; i < arcsList.size(); i++) {
                Map<RandomVariable, ProbabilityTable> arc = arcsList.get(i);
                RandomVariable varOfChild = arc.keySet().iterator().next();
                EditableNode childNode = (EditableNode) bn.getNode(varOfChild);
                fatherNode.removeChild(childNode);
                childNode.removeParents(fatherNode);
                ((EditableCPT) childNode.getCPD()).setProbabilityTable(arc.get(varOfChild));
            }

        }

    }

    /**
     *
     * Fill the arcIrrelevant ( list of map<RandomVar,ReducedCPT> )
     * with all childs and its reduced cpt of nodes of evidences vars
     *
     * @param bn bn
     * @param e evidencs
     */
    private void findArcIrrelevant(EditableBayesianNetwork bn, AssignmentProposition[] e) {

        for (AssignmentProposition ap : e) {
            ArrayList<Map<RandomVariable, ProbabilityTable>> arcsOfCurrentAp = new ArrayList<>();
            EditableNode fatherNode = (EditableNode) bn.getNode(ap.getTermVariable());
            if (fatherNode == null) continue;
            Set<Node> childrenOfCurrentEvidence = fatherNode.getChildren();

            for (Node child : childrenOfCurrentEvidence) {
                Map<RandomVariable, ProbabilityTable> childToNewCpt = new HashMap<>();
                EditableNode childNode = (EditableNode) child;
                ProbabilityTable probabilityTable = bn.generateNewTableOnEvidence(childNode.getRandomVariable(), ap);
                childToNewCpt.put(child.getRandomVariable(), probabilityTable);
                arcsOfCurrentAp.add(childToNewCpt);
            }
            arcIrrelevant.put(ap.getTermVariable(), arcsOfCurrentAp);

        }
    }

    /**
     *
     * Sort by settings
     *
     * @param factors factors returned from VE
     * @param bn bayes net
     * @param e evidences
     *
     * @return sorting over vars in bn
     *
     */
    public List<RandomVariable> orderFactors(List<Factor> factors, BayesianNetwork bn, AssignmentProposition[] e) {
        List<RandomVariable> orderedVar = new LinkedList<>();
        List<RandomVariable> vars = bn.getVariablesInTopologicalOrder();
        if (minDegreeOrder) {
            orderedVar = minDegreeOrder(factors, e, vars);
        } else if (minFillOrder) {
            orderedVar = minFillOrder(factors,e , vars);
        } else if(!minDegreeOrder && !minFillOrder){
            orderedVar = order(bn, vars);
        }
        return orderedVar;
    }

    /**
     *
     * This method implement the MinDegreeOrder pseudocode
     * described in docs : Algorithm 5 MinDegreeOrder(BN, X)
     *
     * @param factors factors to sort
     * @param e vars of evidences
     * @param vars all vars of bayes net
     *
     * @return sorting over factors
     *
     */
    public static List<RandomVariable> minDegreeOrder(List<Factor> factors, AssignmentProposition[] e, Collection<RandomVariable> vars) {
        InterationGraph graph = new InterationGraph(factors, e);
        List<RandomVariable> order = new ArrayList<RandomVariable>();

        for (int index = 0; index < vars.size(); index++) {
            // 1 get var with smallest number of Neighbors in the interaction graph
            RandomVariable currRV = graph.getVarsWithSmallestNumberOfNeighbors();
            order.add(currRV);
            // 2 adding edges between every pair of non adjacent Neighbors
            List<RandomVariable> adjacenciesOfCurrRV = graph.getAdjacencies().get(currRV);
            if(adjacenciesOfCurrRV != null) {
                if (adjacenciesOfCurrRV.size() > 1) {
                    for (int i = 0; i < adjacenciesOfCurrRV.size(); i++) {
                        Iterator iter = adjacenciesOfCurrRV.iterator();
                        while (iter.hasNext()) {
                            RandomVariable dest = (RandomVariable) iter.next();
                            if (!adjacenciesOfCurrRV.get(i).equals(dest)) {
                                RandomVariable source = adjacenciesOfCurrRV.get(i);
                                if (!graph.getAdjacencies().get(source).contains(dest)) {
                                    graph.addEdges(source, dest);
                                }
                            }
                        }
                    }
                }
            }
            // 3 delete var in graph and in vars
            graph.removeVertex(currRV);
        }
        return order;
    }

    /**
     *
     * This method implement the mMnFillOrder pseudocode
     * described in docs : Algorithm 6 MinFillOrder(BN, X)
     *
     * @param factors factors to sort
     * @param e vars of evidences
     * @param vars all vars of bayes net
     *
     * @return sorting over factors
     *
     */

    public static List<RandomVariable> minFillOrder(List<Factor> factors, AssignmentProposition[] e, List<RandomVariable> vars) {
        InterationGraph graph = new InterationGraph(factors, e);
        List<RandomVariable> order = new LinkedList<>();
        Map<RandomVariable, Integer> varToEdgesToadd = new HashMap<>();

        for (int index = 0; index < vars.size(); index++) {
            int countEdgesToAdd = 0;
            RandomVariable currRV = vars.get(index);
            List<RandomVariable> adjacenciesOfCurrRV = graph.getAdjacencies().get(currRV);
            for (int i = 0; i < adjacenciesOfCurrRV.size(); i++) {
                Iterator iter = adjacenciesOfCurrRV.iterator();
                while (iter.hasNext()) {
                    RandomVariable dest = (RandomVariable) iter.next();
                    if (!adjacenciesOfCurrRV.get(i).equals(dest)) {
                        RandomVariable source = adjacenciesOfCurrRV.get(i);
                        if (graph.canAddEdge(source, dest)) {
                            countEdgesToAdd++;
                        }
                    }
                }
            }
            varToEdgesToadd.put(currRV, countEdgesToAdd);
            //graph.removeVertex(currRV);
        }
        for(int i = 0; i < vars.size(); i++){
            RandomVariable currRV = getVarWithLessNeigh(varToEdgesToadd);
            varToEdgesToadd.remove(currRV);
            order.add(currRV);
        }

        return order;
    }

    private static RandomVariable getVarWithLessNeigh(Map<RandomVariable, Integer> varToEdgesToadd) {
        RandomVariable target = null;
        RandomVariable tmp = varToEdgesToadd.keySet().iterator().next();
        int min = varToEdgesToadd.get(tmp);
        for(RandomVariable rv : varToEdgesToadd.keySet()){
            if(varToEdgesToadd.get(rv) < min){
                target = rv;
            }
        }
        if(target == null){
            return tmp;
        }
        return target;
    }

    /**
     *
     * Support methods customize optimizations
     *
     */

    public void setPruningNodes(Boolean v) {
        this.pruneNode = v;
    }

    public void setPruningEdges(Boolean v) {
        this.pruneEdge = v;
    }

    public void setMinDegreeOrder(Boolean v) {
        this.minDegreeOrder = v;
    }

    public void setTopologicOrder() {
        this.minDegreeOrder = false;
        this.minFillOrder = false;
    }

    public void setMinFillOrder(Boolean v) {
        this.minFillOrder = v;
        this.minDegreeOrder = !v;
    }

    public void getProperty(){
        System.out.println("Algoritmo eseguito con le seguenti ottimizzazioni :\t\n"
                + "\t Pruning Nodi : " + pruneNode
                + "\tPruning Archi : " + pruneEdge
                + "\tOrdinamento : " + getOrderMetrics());
    }

    private String getOrderMetrics() {
        if(minFillOrder){
            return "MinFillOrder";
        }
        if(minDegreeOrder){
            return "MinDegreeOrder";
        }
        return "Topologic Inverse";
    }


}