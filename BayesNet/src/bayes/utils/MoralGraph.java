package bayes.utils;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;

import java.util.*;
/**
 *
 * @author Iodice, Di Cianni, Barabno
 *
 */
public class MoralGraph {

    //{ Node, MoralNodesRelated}
    private BayesianNetwork bn;
    private HashMap<Node, Set<Node>> allVars;

    public MoralGraph(BayesianNetwork bn) {
        this.bn = bn;
        this.allVars = buildMoralGraph(bn);
    }

    public HashMap<Node, Set<Node>> getAllVars(){
        return allVars;
    }

    private HashMap<Node, Set<Node>> buildMoralGraph(BayesianNetwork bn) {
        HashMap<Node, Set<Node>> allVars = new HashMap<>();
        List<RandomVariable> variablesInTopologicalOrder = bn.getVariablesInTopologicalOrder();
        for (RandomVariable r : variablesInTopologicalOrder) {
            Node node = bn.getNode(r);
            Set<Node> relatedNodes = new HashSet<>();
            for (Node child : node.getChildren()) {
                relatedNodes.addAll(child.getParents());
                relatedNodes.add(child);
            }
            relatedNodes.addAll(node.getParents());

            if (relatedNodes.contains(node))
                relatedNodes.remove(node);
            allVars.put(node, relatedNodes);
        }
        return allVars;
    }

    public void pruneNetOnEvidence(Set<Node> nodesOfEvidences) {
        for(RandomVariable r : bn.getVariablesInTopologicalOrder()){
            if(nodesOfEvidences.contains(bn.getNode(r))){
                allVars.remove(bn.getNode(r));
            }
        }
        for(Set<Node> edge: allVars.values()){
            for(Node n : nodesOfEvidences){
                if(edge.contains(n)){
                    edge.remove(n);
                }
            }
        }
    }

    public LinkedList<LinkedList<Node>> getAllPaths(Node s, Node d) {
        LinkedList<LinkedList<Node>> allPaths = new LinkedList<LinkedList<Node>>();
        HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();

        for (RandomVariable r : bn.getVariablesInTopologicalOrder())
            visited.put(bn.getNode(r), false);

        LinkedList<Node> path = new LinkedList<>();
        path.add(s);

        //Get all path between source and destination
        getAllPathAux(s, d, visited, path, allPaths);
        //remove all path that contains at least one var in separator

        return allPaths;
    }

    private void getAllPathAux(Node u, Node d,
                               HashMap<Node, Boolean> visited,
                               LinkedList<Node> localPathList,
                               LinkedList<LinkedList<Node>> allPathsList) {
        if(visited.get(u)){
            return;
        }
        visited.put(u, true);
        if (u.equals(d)) {
            //return entry if exist and null otherwise
            //if(localPathList.contains())
            allPathsList.add(new LinkedList<>(localPathList));
            //System.out.println(localPathList);
            visited.put(u, false);
            return;
        }
        for (Node i : allVars.get(u)) {
            if (visited.get(i) == false) {
                localPathList.add(i);
                getAllPathAux(i, d, visited, localPathList, allPathsList);
                localPathList.remove(i);
            }
        }
        //visited.put(u, false);

    }


    //true if M is m-separated from X by E
    //Not working
    public boolean isMSeparated(Node testNode, Set<Node> X, Set<Node> evidence) {
        ArrayList<Boolean> notgoodPathsForSource = new ArrayList<>(X.size());
        for (Node x : X) {
            LinkedList<LinkedList<Node>> allPath = getAllPaths(testNode, x);
            ArrayList<Boolean> goodPaths = new ArrayList<>();
            //goodPath[i] = true when i-Path not contains any var of E
            boolean findVarOfEinPath = false;
            for (int index = 0; index < allPath.size(); index++) {
                LinkedList<Node> path = allPath.get(index);
                for (Node e : evidence) {
                    if (path.contains(e)) {
                        findVarOfEinPath = true;
                        break;
                    }
                }
                //If findVarOfEinPath is true there is a var of E in current
                //path so there is not a good path
                goodPaths.add(findVarOfEinPath);
                if(findVarOfEinPath){
                    index = allPath.size();
                }
                findVarOfEinPath = false;
            }
            notgoodPathsForSource.add(goodPaths.stream().reduce(Boolean::logicalAnd).orElse(false));
            //goodPathsForElem.add(atLeastOneGoodPath(goodPaths));
        }
        return !notgoodPathsForSource.stream().reduce(Boolean::logicalOr).orElse(false);
    }



}
