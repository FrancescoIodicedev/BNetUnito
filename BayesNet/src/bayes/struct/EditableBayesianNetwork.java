package bayes.struct;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.*;
import aima.core.probability.domain.AbstractFiniteDomain;
import aima.core.probability.domain.Domain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;

import java.util.*;

public class EditableBayesianNetwork implements BayesianNetwork {
    protected Set<FullCPTNodeEd> rootNodes = new LinkedHashSet<>();
    protected List<RandomVariable> variables = new ArrayList<RandomVariable>();
    protected Map<RandomVariable, FullCPTNodeEd> varToNodeMap = new HashMap<>();
    protected boolean multiconnected;

    public EditableBayesianNetwork(Node... rootNodes) {
        if (null == rootNodes) {
            throw new IllegalArgumentException(
                    "Root Nodes need to be specified.");
        }
        for (Node n : rootNodes) {
            this.rootNodes.add((FullCPTNodeEd) n);
        }
        if (this.rootNodes.size() != rootNodes.length) {
            throw new IllegalArgumentException(
                    "Duplicate Root Nodes Passed in.");
        }
        // Ensure is a DAG
        checkIsDAGAndCollectVariablesInTopologicalOrder();
    }

    @Override
    public List<RandomVariable> getVariablesInTopologicalOrder() {
        return variables;
    }

    @Override
    public Node getNode(RandomVariable rv) {
        return varToNodeMap.get(rv);
    }



    public Set<FullCPTNodeEd> getRootNodes() {
        return rootNodes;
    }

// END-BayesianNetwork
    //

    //
    // PRIVATE METHODS
    //
    private void checkIsDAGAndCollectVariablesInTopologicalOrder() {

        // Topological sort based on logic described at:
        // http://en.wikipedia.org/wiki/Topoligical_sorting
        Set<Node> seenAlready = new HashSet<Node>();
        Map<Node, List<Node>> incomingEdges = new HashMap<Node, List<Node>>();
        Set<Node> s = new LinkedHashSet<Node>();
        for (Node n : this.rootNodes) {
            walkNode(n, seenAlready, incomingEdges, s);
        }
        while (!s.isEmpty()) {
            Node n = s.iterator().next();
            s.remove(n);
            variables.add(n.getRandomVariable());
            varToNodeMap.put(n.getRandomVariable(), (FullCPTNodeEd) n);
            for (Node m : n.getChildren()) {
                List<Node> edges = incomingEdges.get(m);
                edges.remove(n);
                if (edges.isEmpty()) {
                    s.add(m);
                }
            }
        }

        for (List<Node> edges : incomingEdges.values()) {
            if (!edges.isEmpty()) {
                throw new IllegalArgumentException(
                        "Network contains at least one cycle in it, must be a DAG.");
            }
        }
    }

    private void walkNode(Node n, Set<Node> seenAlready,
                          Map<Node, List<Node>> incomingEdges, Set<Node> rootNodes) {
        if (!seenAlready.contains(n)) {
            seenAlready.add(n);
            // Check if has no incoming edges
            if (n.isRoot()) {
                rootNodes.add(n);
            }
            incomingEdges.put(n, new ArrayList<Node>(n.getParents()));
            for (Node c : n.getChildren()) {
                walkNode(c, seenAlready, incomingEdges, rootNodes);
            }
        }
    }

    /**
     * From here all methods are implemented by :
     * - Iodice Francesco
     * - Valentino Di Cianni
     * - Carlo Alberto Barbano
     * ************************************************************************************
     *
     *  Removing a child node represented by var rv in
     *  the net
     *
     * @param rv RandomVar to delete
     */
    public void removeNode(RandomVariable rv) {
        EditableNode currentNode = varToNodeMap.get(rv);

        //System.out.println("Pruno il nodo: " + currentNode);

        variables.remove(currentNode.getRandomVariable());
        varToNodeMap.remove(currentNode.getRandomVariable());
        if (rootNodes.contains(currentNode)) {
            rootNodes.remove(currentNode);
        }
        //deleting arc that contains rv
        for (Node nod : currentNode.getParents()) {
            EditableNode parent = (EditableNode) nod;
            parent.removeChild(currentNode);
        }

        for (Node nod : currentNode.getChildren()) {
            EditableNode child = (EditableNode) nod;
            child.removeParents(currentNode);
            reduceCPTofChild(child, currentNode);
        }
    }

    /**
     * Generate a Factor of child var give
     * the assignment of father
     *
     * @param child var to reduce cpt
     * @param ap assignment of father
     *
     * @return the reduced CPT give the evidence
     *
     */
    public ProbabilityTable generateNewTableOnEvidence(RandomVariable child, AssignmentProposition ap) {
        EditableNode childNode = varToNodeMap.get(child);
        EditableCPT pt = (EditableCPT) childNode.getCPD();
        ProbabilityTable newPt = (ProbabilityTable) pt.getFactorFor(ap);
        return newPt;
    }

    /**
     * Reduced by cpt of child with evidence.
     * Summing out RandomVar of parent and
     * normalizing the result (same method used in samiam)
     *
     * @param child child node
     * @param currentNode father node
     */
    private void reduceCPTofChild(EditableNode child, EditableNode currentNode) {

        Domain d = currentNode.getRandomVariable().getDomain();
        if (!(d instanceof AbstractFiniteDomain)) {
            throw new IllegalArgumentException(
                    "Cannot have an infinite domain for a variable in this calculation:"
                            + d);
        } else {
            EditableCPT pt = (EditableCPT) child.getCPD();
            ProbabilityTable probabilityTable = pt.getProbabilityTable();
            ProbabilityTable reducedCPT = probabilityTable.sumOut(currentNode.getRandomVariable());
            for (int i = 0; i < reducedCPT.getValues().length; i++) {
                double value = reducedCPT.getValues()[i];
                reducedCPT.setValue(i, value / 2);
            }
            pt.setProbabilityTable(reducedCPT);

        }

    }

    public boolean isMultiConnected() {
        return multiconnected;
    }

    public void setMultiConnectedTrue() {
        multiconnected = true;
    }
}
