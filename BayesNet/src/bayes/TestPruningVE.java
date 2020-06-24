package bayes;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesInference;
import aima.core.probability.bayes.Node;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.RandVar;
import bayes.bifparser.BIFHandlerToEditableBN;
import bayes.struct.CustomDBN;
import bnparser.BifReader;
import bayes.struct.EditableBayesianNetwork;
import bayes.struct.EditableNode;
import bayes.struct.FullCPTNodeEd;
import bayes.utils.BayesNetFactory;
import bayes.utils.CustomEliminationAsk;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TestPruningVE {

    public static void main(String[] args) {
        //Chain-net
        testChainNet();

        //Multiconnected-netowork
        testOnMultiNetwork20nodes();

        //Singly Connected Net
        testOnNetwork20nodes();
        testOnNetwork60nodes();
        testOnNetwork100nodes();
        testOnNetwork200nodes();


    }


    private static void testOnNetwork200nodes() {
        System.out.println("\n" + "------Testing performance of Optimaized VE on the net ---------" + "\n");
        EditableBayesianNetwork polyNet = constructSinglyBigNet2();
        System.out.println("Var in topologic order : " + polyNet.getVariablesInTopologicalOrder());

        CustomEliminationAsk[] allbi = instanceCustomVE();

        int index = 0;
        for (CustomEliminationAsk inference: allbi) {
            polyNet = constructSinglyBigNet2();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : polyNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(Node80|Node1=State1)
            AssignmentProposition[] ap = new AssignmentProposition[1];
            ap[0] = new AssignmentProposition(rvsmap.get("Node1"), "State1");
            RandomVariable[] qrv = new RandomVariable[1];
            qrv[0] = rvsmap.get("Node80");

            printQuery(qrv, ap);
            makeInference(inference, qrv, ap, polyNet, index);
            index++;
        }

        //P(Node30, Node80 | Node1 = State1, Node10 = State0 )
        index = 0;
        for (CustomEliminationAsk inference: allbi) {
            polyNet = constructSinglyBigNet2();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : polyNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            AssignmentProposition[] ap2 = new AssignmentProposition[2];
            ap2[0] = new AssignmentProposition(rvsmap.get("Node1"), "State1");
            ap2[1] = new AssignmentProposition(rvsmap.get("Node10"), "State0");
            RandomVariable[] qrv2 = new RandomVariable[2];
            qrv2[0] = rvsmap.get("Node30");
            qrv2[1] = rvsmap.get("Node80");

            printQuery(qrv2, ap2);
            makeInference(inference, qrv2, ap2, polyNet, index);
            index++;
        }

        //P(Node1, Node10 | Node30 = State1, Node80 = State0 )
        index = 0;
        for (CustomEliminationAsk inference: allbi) {
            polyNet = constructSinglyBigNet2();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : polyNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            AssignmentProposition[] ap3 = new AssignmentProposition[2];
            ap3[0] = new AssignmentProposition(rvsmap.get("Node80"), "State1");
            ap3[1] = new AssignmentProposition(rvsmap.get("Node30"), "State0");
            RandomVariable[] qrv3 = new RandomVariable[2];
            qrv3[0] = rvsmap.get("Node1");
            qrv3[1] = rvsmap.get("Node10");

            printQuery(qrv3, ap3);
            makeInference(inference, qrv3, ap3, polyNet, index);
            index++;
        }
    }

    private static void testOnNetwork100nodes() {
        System.out.println("\n" + "------Testing performance of Optimaized VE on the net ---------" + "\n");
        EditableBayesianNetwork polyNet = constructSinglyBigNet1();
        System.out.println("Var in topologic order : " + polyNet.getVariablesInTopologicalOrder());

        CustomEliminationAsk[] allbi = instanceCustomVE();

        int index = 0;
        for (CustomEliminationAsk inference: allbi) {
            polyNet = constructSinglyBigNet1();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : polyNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(Node80|Node56=State1)
            AssignmentProposition[] ap = new AssignmentProposition[1];
            ap[0] = new AssignmentProposition(rvsmap.get("Node56"), "State1");
            RandomVariable[] qrv = new RandomVariable[1];
            qrv[0] = rvsmap.get("Node80");

            printQuery(qrv,ap);
            makeInference(inference,qrv,ap,polyNet, index);
            index++;
        }

        index = 0;
        for (CustomEliminationAsk inference: allbi) {
            polyNet = constructSinglyBigNet1();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : polyNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(Node130, Node180 | Node1 = State1, Node100 = State0 )
            AssignmentProposition[] ap2 = new AssignmentProposition[2];
            ap2[0] = new AssignmentProposition(rvsmap.get("Node1"), "State1");
            ap2[1] = new AssignmentProposition(rvsmap.get("Node99"), "State0");
            RandomVariable[] qrv2 = new RandomVariable[2];
            qrv2[0] = rvsmap.get("Node43");
            qrv2[1] = rvsmap.get("Node78");

            printQuery(qrv2, ap2);
            makeInference(inference, qrv2, ap2, polyNet, index);
            index++;
        }
    }

    private static void testChainNet() {
        System.out.println("\n" + "------Testing performance of Optimaized VE on the net ---------" + "\n");
        EditableBayesianNetwork chainNet = constructChainNet();
        System.out.println("Var in topologic order : " + chainNet.getVariablesInTopologicalOrder());

        CustomEliminationAsk[] allbi = instanceCustomVE();

        int index = 0;
        for(CustomEliminationAsk inference: allbi) {
            chainNet = constructChainNet();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : chainNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }

            //P(X1|X50=true)
            AssignmentProposition[] ap = new AssignmentProposition[1];
            ap[0] = new AssignmentProposition(rvsmap.get("X50"), true);
            //Random Var
            RandomVariable[] qrv = new RandomVariable[1];
            qrv[0] = rvsmap.get("X1");

            printQuery(qrv, ap);
            makeInference(inference, qrv, ap, chainNet, index);
            index++;
        }

        //P(X1,X30|X50=true,X40=false)
        index = 0;
        for(CustomEliminationAsk inference: allbi) {
            chainNet = constructChainNet();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : chainNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }

            AssignmentProposition[] ap2 = new AssignmentProposition[2];
            ap2[0] = new AssignmentProposition(rvsmap.get("X40"), false);
            ap2[1] = new AssignmentProposition(rvsmap.get("X50"), true);
            //Random Var
            RandomVariable[] qrv2 = new RandomVariable[2];
            qrv2[0] = rvsmap.get("X1");
            qrv2[1] = rvsmap.get("X30");

            printQuery(qrv2, ap2);
            makeInference(inference, qrv2, ap2, chainNet, index);
            index++;
        }


        index = 0;
        for(CustomEliminationAsk inference: allbi) {
            chainNet = constructChainNet();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : chainNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(X1,X25,X50|X20=true,X30=false,X40=true)
            AssignmentProposition[] ap3 = new AssignmentProposition[3];
            ap3[0] = new AssignmentProposition(rvsmap.get("X20"), true);
            ap3[1] = new AssignmentProposition(rvsmap.get("X30"), false);
            ap3[2] = new AssignmentProposition(rvsmap.get("X40"), true);
            //Random Var
            RandomVariable[] qrv3 = new RandomVariable[3];
            qrv3[0] = rvsmap.get("X1");
            qrv3[1] = rvsmap.get("X25");
            qrv3[2] = rvsmap.get("X50");

            printQuery(qrv3, ap3);
            makeInference(inference, qrv3, ap3, chainNet, index);
            index++;
        }

        System.out.println("\n" + "---------------------------------------" + "\n");

    }

    private static void testOnNetwork60nodes() {
        System.out.println("\n" + "------Testing performance of Optimaized VE on the net ---------" + "\n");
        EditableBayesianNetwork Net40ndoes = BayesNetFactory.buildAlarmNetEvolved();
        System.out.println("Var in topologic order : " + Net40ndoes.getVariablesInTopologicalOrder());

        CustomEliminationAsk[] allbi = instanceCustomVE();

        int index = 0;
        for(CustomEliminationAsk inference: allbi) {
            Net40ndoes = BayesNetFactory.buildAlarmNetEvolved();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : Net40ndoes.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }

            //P(Node3|Node=State1)
            AssignmentProposition[] ap = new AssignmentProposition[1];
            ap[0] = new AssignmentProposition(rvsmap.get("VENTMACH"), true);
            //Random Var
            RandomVariable[] qrv = new RandomVariable[1];
            qrv[0] = rvsmap.get("PVSAT");

            printQuery(qrv, ap);
            makeInference(inference, qrv, ap, Net40ndoes, index);
            index++;
        }

        //P(JhonCalls | Alarm)
        index = 0;
        for(CustomEliminationAsk inference: allbi) {
            Net40ndoes = BayesNetFactory.buildAlarmNetEvolved();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : Net40ndoes.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            AssignmentProposition[] ap2 = new AssignmentProposition[2];
            ap2[0] = new AssignmentProposition(rvsmap.get("VENTLUNG"), true);
            ap2[1] = new AssignmentProposition(rvsmap.get("HREKG"), true);
            //Random Var
            RandomVariable[] qrv2 = new RandomVariable[2];
            qrv2[0] = rvsmap.get("CATECHOL");
            qrv2[1] = rvsmap.get("PAP");

            printQuery(qrv2, ap2);
            makeInference(inference, qrv2, ap2, Net40ndoes, index);
            index++;
        }


        index = 0;
        for(CustomEliminationAsk inference: allbi) {
            Net40ndoes = BayesNetFactory.buildAlarmNetEvolved();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : Net40ndoes.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(JhonCalls | Alarm)
            AssignmentProposition[] ap3 = new AssignmentProposition[2];
            ap3[0] = new AssignmentProposition(rvsmap.get("VENTLUNG"), true);
            ap3[1] = new AssignmentProposition(rvsmap.get("HREKG"), true);
            //Random Var
            RandomVariable[] qrv3 = new RandomVariable[3];
            qrv3[0] = rvsmap.get("CATECHOL");
            qrv3[1] = rvsmap.get("PAP");
            qrv3[2] = rvsmap.get("CO");

            printQuery(qrv3, ap3);
            makeInference(inference, qrv3, ap3, Net40ndoes, index);
            index++;
        }

        System.out.println("\n" + "---------------------------------------" + "\n");

    }

    private static void testOnNetwork20nodes() {
        System.out.println("\n" + "------Testing performance of Optimaized VE on follow net ---------" + "\n");
        EditableBayesianNetwork Net20ndoes = (EditableBayesianNetwork) construct20nodesNet();
        System.out.println("Var in topologic order : " + Net20ndoes.getVariablesInTopologicalOrder());

        CustomEliminationAsk[] allbi = instanceCustomVE();

        int index = 1;
        for (CustomEliminationAsk inference: allbi) {
            Net20ndoes = (EditableBayesianNetwork) construct20nodesNet();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : Net20ndoes.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(Node3|Node=State1)
            AssignmentProposition[] ap = new AssignmentProposition[1];
            ap[0] = new AssignmentProposition(rvsmap.get("Node1"), "State1");
            RandomVariable[] qrv = new RandomVariable[1];
            qrv[0] = rvsmap.get("Node3");

            printQuery(qrv, ap);
            makeInference(inference, qrv, ap, Net20ndoes, index);
            index++;
        }

        index = 0;
        for (CustomEliminationAsk inference: allbi) {
            Net20ndoes = (EditableBayesianNetwork) construct20nodesNet();
            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : Net20ndoes.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(Node3, Node8 | Node1 = State1, Node10 = State0 )
            AssignmentProposition[] ap2 = new AssignmentProposition[2];
            ap2[0] = new AssignmentProposition(rvsmap.get("Node1"), "State1");
            ap2[1] = new AssignmentProposition(rvsmap.get("Node10"), "State0");
            RandomVariable[] qrv2 = new RandomVariable[2];
            qrv2[0] = rvsmap.get("Node3");
            qrv2[1] = rvsmap.get("Node8");

            printQuery(qrv2, ap2);
            makeInference(inference, qrv2, ap2, Net20ndoes, index);

            index++;
        }
    }

    private static void testOnMultiNetwork20nodes() {
        System.out.println("\n" + "------Testing performance of Optimaized VE on the net ---------" + "\n");
        System.out.println("\n" + "------The follow Net if a polytree net ---------" + "\n");
        EditableBayesianNetwork multiNet = constructMultiNet();
        multiNet.setMultiConnectedTrue();
        System.out.println("Var in topologic order : " + multiNet.getVariablesInTopologicalOrder());

        CustomEliminationAsk[] allbi = instanceCustomVE();
        int index = 0;
        for(CustomEliminationAsk inference: allbi) {
            multiNet = constructMultiNet();
            multiNet.setMultiConnectedTrue();

            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : multiNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(Node3|Node=State1)
            AssignmentProposition[] ap = new AssignmentProposition[1];
            ap[0] = new AssignmentProposition(rvsmap.get("Node1"), "State1");
            RandomVariable[] qrv = new RandomVariable[1];
            qrv[0] = rvsmap.get("Node3");

            printQuery(qrv, ap);
            makeInference(inference, qrv, ap, multiNet, index);
            index++;
        }

        index = 0;
        for(CustomEliminationAsk inference: allbi) {
            multiNet = constructMultiNet();
            multiNet.setMultiConnectedTrue();

            HashMap<String, RandomVariable> rvsmap = new HashMap<>();
            for (RandomVariable randv : multiNet.getVariablesInTopologicalOrder()) {
                rvsmap.put(randv.getName(), randv);
            }
            //P(Node3, Node8 | Node1 = State1, Node10 = State0 )
            AssignmentProposition[] ap2 = new AssignmentProposition[2];
            ap2[0] = new AssignmentProposition(rvsmap.get("Node3"), "State1");
            ap2[1] = new AssignmentProposition(rvsmap.get("Node8"), "State0");
            RandomVariable[] qrv2 = new RandomVariable[2];
            qrv2[0] = rvsmap.get("Node1");
            qrv2[1] = rvsmap.get("Node4");

            printQuery(qrv2, ap2);
            makeInference(inference, qrv2, ap2, multiNet, index);
            index++;
        }
    }

    private static void testOnAlarmNetwork() {
        System.out.println("\n" + "------Testing performance on Burglary Network-----------" + "\n");
        EditableBayesianNetwork burglaryNet = BayesNetFactory.constructBurglaryAlarmNetworkEd();
        CategoricalDistribution cd;

        BayesInference[] allbi = instanceCustomVE();

        HashMap<String, RandomVariable> rvsmap = new HashMap<>();
        for (RandomVariable randv : burglaryNet.getVariablesInTopologicalOrder()) {
            rvsmap.put(randv.getName(), randv);
        }
        //P(Alarm|Jhon)
        AssignmentProposition[] ap = new AssignmentProposition[1];
        ap[0] = new AssignmentProposition(rvsmap.get("JohnCalls"), true);
        //Random Var
        RandomVariable[] qrv = new RandomVariable[1];
        qrv[0] = rvsmap.get("Alarm");

        printQuery(qrv, ap);
        //makeInference(allbi, qrv, ap, burglaryNet);

        //P(JhonCalls | Alarm)
        ap = new AssignmentProposition[1];
        ap[0] = new AssignmentProposition(rvsmap.get("Alarm"), true);
        //Random Var
        qrv = new RandomVariable[1];
        qrv[0] = rvsmap.get("JohnCalls");

        printQuery(qrv, ap);
        //makeInference(allbi, qrv, ap, burglaryNet);

    }

    private static EditableBayesianNetwork constructChainNet() {
        int size = 50;
        RandomVariable[] allrv = new RandomVariable[size];

        for (int i = 0; i < size; i++) {
            allrv[i] = new RandVar("X" + (i + 1), new BooleanDomain());
        }

        EditableNode fn = new FullCPTNodeEd(allrv[0], new double[]{0.5, 0.5});
        EditableNode fn0 = fn;
        for (int i = 1; i < size; i++) {
            fn = new FullCPTNodeEd(allrv[i], new double[]{0.5, 0.5, 0.5, 0.5}, fn);
        }
        return new EditableBayesianNetwork(fn0);

    }

    public static EditableBayesianNetwork construct20nodesNet() {
        EditableBayesianNetwork bayesianNetwork = (EditableBayesianNetwork) BifReader.readBIF(new BIFHandlerToEditableBN(), "resources/net20nodes1.xml");
        List<Node> rootNodes = new LinkedList<>();
        for (RandomVariable n : bayesianNetwork.getVariablesInTopologicalOrder()) {
            if (bayesianNetwork.getNode(n).isRoot()) {
                rootNodes.add(bayesianNetwork.getNode(n));
            }
        }
        return bayesianNetwork;
    }

    public static EditableBayesianNetwork constructMultiNet() {
        return (EditableBayesianNetwork) BifReader.readBIF(new BIFHandlerToEditableBN(),"resources/polyNet_1.xml");
    }

    public static EditableBayesianNetwork constructSinglyBigNet1() {
        return (EditableBayesianNetwork) BifReader.readBIF(new BIFHandlerToEditableBN(),"resources/s1.xml");
    }

    public static EditableBayesianNetwork constructSinglyBigNet2() {
        return (EditableBayesianNetwork) BifReader.readBIF(new BIFHandlerToEditableBN(),"resources/s2.xml");
    }

    public static CustomEliminationAsk[] instanceCustomVE() {
        CustomEliminationAsk[] allbi = new CustomEliminationAsk[12];
        for (int i = 0; i < 12; i++) {
            allbi[i] = new CustomEliminationAsk();
        }

        //MinFillOrder
        CustomEliminationAsk ask = (CustomEliminationAsk) allbi[0];
        ask.setPruningEdges(true);
        ask.setPruningNodes(true);
        ask.setMinFillOrder(true);

        CustomEliminationAsk cE3 = (CustomEliminationAsk) allbi[1];
        cE3.setPruningNodes(true);
        cE3.setPruningEdges(false);
        cE3.setMinFillOrder(true);

        CustomEliminationAsk cE5 = (CustomEliminationAsk) allbi[2];
        cE5.setPruningNodes(false);
        cE5.setPruningEdges(true);
        cE5.setMinFillOrder(true);

        CustomEliminationAsk cE10 = (CustomEliminationAsk) allbi[3];
        cE10.setPruningNodes(false);
        cE10.setPruningEdges(false);
        cE10.setMinFillOrder(true);

        //MinDegreeOrder
        CustomEliminationAsk cE = (CustomEliminationAsk) allbi[4];
        cE.setPruningEdges(true);
        cE.setPruningNodes(true);
        cE.setMinDegreeOrder(true);

        CustomEliminationAsk cE2 = (CustomEliminationAsk) allbi[5];
        cE2.setPruningNodes(true);
        cE2.setPruningEdges(false);
        cE2.setMinDegreeOrder(true);

        CustomEliminationAsk cE4 = (CustomEliminationAsk) allbi[6];
        cE4.setPruningNodes(false);
        cE4.setPruningEdges(true);
        cE4.setMinDegreeOrder(true);

        CustomEliminationAsk cE9 = (CustomEliminationAsk) allbi[7];
        cE9.setPruningNodes(false);
        cE9.setPruningEdges(false);
        cE9.setMinDegreeOrder(true);

        //Topologic order
        CustomEliminationAsk cE8 = (CustomEliminationAsk) allbi[8];
        cE8.setPruningNodes(true);
        cE8.setPruningEdges(true);
        cE8.setTopologicOrder();

        CustomEliminationAsk cE6 = (CustomEliminationAsk) allbi[9];
        cE6.setPruningNodes(false);
        cE6.setPruningEdges(true);
        cE6.setTopologicOrder();

        CustomEliminationAsk cE7 = (CustomEliminationAsk) allbi[10];
        cE7.setPruningNodes(true);
        cE7.setPruningEdges(false);
        cE7.setTopologicOrder();

        CustomEliminationAsk original = (CustomEliminationAsk) allbi[11];
        original.setPruningNodes(false);
        original.setPruningEdges(false);
        original.setTopologicOrder();


        return allbi;
    }

    private static void printQuery(RandomVariable[] qrv, AssignmentProposition[] ap) {
        System.out.print("Query is : P(");
        for (int i = 0; i < qrv.length; i++) {
            System.out.print(qrv[i].toString());
            if (i + 1 != qrv.length) {
                System.out.print(",");
            }
        }
        System.out.print("|");
        for (int i = 0; i < ap.length; i++) {
            System.out.print(ap[i].toString());
            if (i + 1 != ap.length) {
                System.out.print(",");
            }
        }
        System.out.println(")\n");
    }


    private static void makeInference2(BayesInference[] allbi, RandomVariable[] qrv, AssignmentProposition[] ap, EditableBayesianNetwork net) {
        int index = 1;
        for (BayesInference bi : allbi) {
            if (bi == null) {
                continue;
            }
            CustomEliminationAsk pt = (CustomEliminationAsk) bi;
            System.out.println("Test N: " + index);
            CategoricalDistribution cd;
            pt.getProperty();
            cd = bi.ask(qrv, ap, net);
            System.out.println("CD : " + cd);
            System.out.println(" With : " + bi.getClass());
            System.out.println("\n");
            index++;
        }
        System.out.println("--------------------------------------------------\n");
    }

    private static void makeInference(CustomEliminationAsk allbi, RandomVariable[] qrv, AssignmentProposition[] ap, EditableBayesianNetwork net, int index) {
        System.out.println("Test N: " + index);
        allbi.getProperty();
        CategoricalDistribution cd = allbi.ask(qrv, ap, net);
        System.out.println("CD : " + cd);
        System.out.println(" With : " + allbi.getClass());
        System.out.println("\n");
        System.out.println("--------------------------------------------------\n");
    }

}
