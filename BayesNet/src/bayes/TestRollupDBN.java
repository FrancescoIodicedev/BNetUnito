package bayes;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.DynamicBayesianNetwork;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.approx.ParticleFiltering;
import aima.core.probability.example.DynamicBayesNetExampleFactory;
import aima.core.probability.example.ExampleRV;
import aima.core.probability.proposition.AssignmentProposition;
import bayes.struct.CustomDBN;
import bayes.utils.BayesNetFactory;
import bayes.utils.CustomEliminationAsk;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


public class TestRollupDBN {
    private static final int numPredizioni = 50;
    private static int numVariabiliEvidenza = 1;
    private static AssignmentProposition[][] aps = new AssignmentProposition[numPredizioni][numVariabiliEvidenza];
    private static final String ordering = "TI";

    static ArrayList<Long> unrolledTimes = new ArrayList<>();
    static ArrayList<Long> rollupTimes = new ArrayList<>();

    public static void rollup(CustomDBN dbn, Object[] values) {
        dbn.ordering = ordering;

        makeRandomPropositions(dbn, values);

        Instant t0 = Instant.now();
        for (int i = 0; i < numPredizioni - 2; i++) {
            //System.out.println("Time " + (i + 1));
            dbn.forward(aps[i]);
        }
        Instant t1 = Instant.now();
        List<Factor> stateDistribution = dbn.forward(aps[numPredizioni-1]);
        Instant t2 = Instant.now();

        System.out.println("Inferenza al tempo " + numPredizioni);
        System.out.println(String.format("Date evidenze (%d): ", aps[numPredizioni-1].length) + Arrays.deepToString(aps[numPredizioni-1]));

        //for (Factor factor: stateDistribution) {
        //    System.out.println(String.format("Distribution for state variable %s:", factor.getArgumentVariables()) + Arrays.toString(factor.getValues()));
        //}

        Long unrolledTime = Duration.between(t0, t2).toMillis();
        Long rollupTime = Duration.between(t1, t2).toMillis();
        System.out.println("Unrolled time: " + unrolledTime + "ms");
        System.out.println("Rollup-filtering time: " + rollupTime + "ms");

        unrolledTimes.add(unrolledTime);
        rollupTimes.add(rollupTime);
    }
    public static void rollup(String path, Object[] values) {
        System.out.println("\n------- Rollup su " + path);
        CustomDBN dbn = BayesNetFactory.makeDNB(path);
        rollup(dbn, values);
    }

    public static void test_rollups() {
        // net20nodes1 -> S20
        // polyNet_1 -> M20
        // s1 -> S100
        // S2 -> S200
        String[] networksN = { "resources/net20nodes1.xml", "resources/polyNet_1.xml", "resources/s1.xml", "resources/s2.xml"};
        List<String> networks = new ArrayList<>(Arrays.asList(networksN));

        for(String path: networks)
            rollup(path, new String[]{"State0", "State1"});

        networks.add("Umbrella");
        networks.add("WindUmbrella");

        rollup(BayesNetFactory.BuildUmbrellaRain(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});
        rollup(BayesNetFactory.getRainWindNetwork(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});

        System.out.println("ORDERING: " + ordering);
        for (int i = 0; i < unrolledTimes.size(); i++) {
            System.out.println(String.format("%s: %d %d", networks.get(i), unrolledTimes.get(i), rollupTimes.get(i)));
        }
    }
    public static void main(String[] args) {
        System.out.println("-------- ROLLUP TEST ---------");
        test_rollups();

        System.out.println("------- ROLLUP vs APPROX -------");
        checkApproxResults();
    }

    private static void checkApproxResults() {
        aima.core.probability.bayes.approx.ParticleFiltering pf = new ParticleFiltering(numPredizioni,  DynamicBayesNetExampleFactory.getUmbrellaWorldNetwork());
        CustomDBN dbn = BayesNetFactory.BuildUmbrellaRain();
        makeRandomPropositions(dbn, new Boolean[]{Boolean.FALSE, Boolean.TRUE});

        for (int i = 0; i < numPredizioni - 2; i++) {
            AssignmentProposition[][] approx = pf.particleFiltering(aps[i]);
            List<Factor> exact = dbn.forward(aps[i]);
            System.out.println(String.format("Step %d: given %s -> approx=%s, exact=%s", i, aps[i][0], approx[0][0], exact));
        }

        AssignmentProposition[][] S = pf.particleFiltering(aps[numPredizioni - 1]);
        List<Factor> stateDistribution = dbn.forward(aps[numPredizioni-1]);

        System.out.println("---------------");
        System.out.println("-> risultato approssimato: ");
        printSamples(S, numPredizioni);

        System.out.println("---------------");
        System.out.println("-> risultato Esatto: ");
        System.out.println(stateDistribution);

    }

    private static void getLeaves(Node node, Set<Node> leaves) {
        if (node.getChildren().size() == 0) {
            leaves.add(node);
        } else {
            for (Node child : node.getChildren())
                getLeaves(child, leaves);
        }
    }

    private static Set<Node> getEvidenceNodes(CustomDBN dbn) {
        Set<Node> leaves = new HashSet<>();
        for (Node root : dbn.getRootNodes())
            getLeaves(root, leaves);

        List<RandomVariable> stateVariables = Arrays.asList(dbn.getStateVariables());
        Set<Node> evidenceNodes = new HashSet<>();
        for (Node leaf : leaves) {
            if (!dbn.getX0_to_X1().containsKey(leaf.getRandomVariable().getName()) && !stateVariables.contains(leaf.getRandomVariable()))
                evidenceNodes.add(leaf);
        }
        return evidenceNodes;
    }

    private static void makeRandomPropositions(CustomDBN dbn, Object[] values) {
        Node evidenceNodes[] = getEvidenceNodes(dbn).toArray(Node[]::new);
        numVariabiliEvidenza = evidenceNodes.length;
        System.out.println("Evidenza " + numVariabiliEvidenza);

        aps = new AssignmentProposition[numPredizioni][numVariabiliEvidenza];

        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < numPredizioni; i++) {
            for (int j = 0; j < numVariabiliEvidenza; j++) {
                int value = r.nextDouble() >= 0.5 ? 1 : 0;
                aps[i][j] = new AssignmentProposition(evidenceNodes[j].getRandomVariable(), value == 0 ? values[0] : values[1]);
            }
        }
    }


    private static void printSamples(AssignmentProposition[][] S, int n) {
        HashMap<String, Integer> hm = new HashMap<String, Integer>();

        int nstates = S[0].length;

        for (int i = 0; i < n; i++) {
            String key = "";
            for (int j = 0; j < nstates; j++) {
                AssignmentProposition ap = S[i][j];
                key += ap.getValue().toString();
            }
            Integer val = hm.get(key);
            if (val == null) {
                hm.put(key, 1);
            } else {
                hm.put(key, val + 1);
            }
        }

        for (String key : hm.keySet()) {
            System.out.println(key + ": " + hm.get(key) / (double) n);
        }
    }

}


