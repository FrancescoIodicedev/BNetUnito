package bayes.utils;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.proposition.AssignmentProposition;

import java.util.*;
import java.util.stream.Collectors;
/**
 *
 * @author Iodice, Di Cianni, Barabno
 *
 */
public class InterationGraph {
    public HashSet<RandomVariable> vertex = new HashSet<>();
    public HashMap<RandomVariable, List<RandomVariable>> adjacencies;

    public InterationGraph(List<Factor> factors, AssignmentProposition[] ap) {
        this.adjacencies = buildInterationGraph(factors, ap);
    }

    public HashMap<RandomVariable, List<RandomVariable>> getAdjacencies(){
        return adjacencies;
    }

    private HashMap<RandomVariable, List<RandomVariable>> buildInterationGraph(List<Factor> factors, AssignmentProposition[] ap) {
        HashMap<RandomVariable, List<RandomVariable>> adjacencies = new HashMap<>();
        for(int j = 0;j < ap.length;j++ ){
            vertex.add(ap[j].getTermVariable());
        }
        for(int i = 0; i < factors.size(); i++){
            Factor f = factors.get(i);
            for(RandomVariable rv : f.getArgumentVariables()){
                vertex.add(rv);
            }
        }
        for(RandomVariable RV : vertex){
            adjacencies.put(RV, findArcToRv(RV, factors));
        }

        return adjacencies;
    }

    private List<RandomVariable> findArcToRv(RandomVariable rv, List<Factor> factors) {
        Set<RandomVariable> relatedToRv = new HashSet<>();
        for(Factor f : factors){
            Set<RandomVariable> argumentVariables = f.getArgumentVariables();
            if(argumentVariables.contains(rv) && argumentVariables.size() > 1){
                for(RandomVariable parent : argumentVariables){
                    if(!parent.equals(rv)){
                        relatedToRv.add(parent);
                    }
                }
            }
        }
        return new LinkedList<>(relatedToRv);
    }

    public RandomVariable getVarsWithSmallestNumberOfNeighbors(){
        RandomVariable target = null;
        RandomVariable tmp = adjacencies.keySet().iterator().next();
        List<RandomVariable> randomVariables = adjacencies.get(tmp);
        int NumberOfNeighbors = randomVariables.size();

        for(RandomVariable n : adjacencies.keySet()){
            List<RandomVariable> relatedToN = adjacencies.get(n);
            if(relatedToN.size() < NumberOfNeighbors){
                NumberOfNeighbors = relatedToN.size();
                target = n;
            }
        }
        if(target == null){
            return tmp;
        }
        return target;
    }

    public void removeVertex(RandomVariable rv){
        vertex.remove(rv);
        for(List<RandomVariable> adjacent: adjacencies.values()){
            if(adjacent.contains(rv)){
                adjacent.remove(rv);
            }
        }
        adjacencies.remove(rv);
    }

    public void addEdges(RandomVariable source, RandomVariable dest){
        adjacencies.get(source).add(dest);
        adjacencies.get(dest).add(source);
    }

    public boolean canAddEdge(RandomVariable source, RandomVariable dest){
        return !(adjacencies.get(source).contains(dest) && adjacencies.get(dest).contains(source));
    }

    public static Map<String, Integer> sortByValue(Map<String, Integer> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
