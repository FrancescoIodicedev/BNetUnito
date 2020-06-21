package bayes.utils;

import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.example.ExampleRV;
import aima.core.probability.util.RandVar;
import bayes.bifparser.BIFHandlerToCustomDBN;
import bnparser.BifReader;
import bayes.struct.CustomDBN;
import bayes.dbn.UmbrellaParticle;
import bayes.struct.EditableBayesianNetwork;
import bayes.struct.EditableNode;
import bayes.struct.FullCPTNodeEd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BayesNetFactory {

    public static EditableBayesianNetwork buildAlarmNetEvolved() {
        /**
         * Number of nodes: 37
         * Number of arcs: 46
         */

        RandVar history = new RandVar("HISTORY", new BooleanDomain());

        RandVar cvp = new RandVar("CVP", new BooleanDomain());
        RandVar pcwp = new RandVar("PCWP", new BooleanDomain());
        RandVar hypovolemia = new RandVar("HYPOVOLEMIA", new BooleanDomain());
        RandVar lvedvolume = new RandVar("LVEDVOLUME", new BooleanDomain());
        RandVar lvfailure = new RandVar("LVFAILURE", new BooleanDomain());
        RandVar strokevolume = new RandVar("STROKEVOLUME", new BooleanDomain());
        RandVar errlowoutput = new RandVar("ERRLOWOUTPUT", new BooleanDomain());

        RandVar hrbp = new RandVar("HRBP", new BooleanDomain());
        RandVar hrekg = new RandVar("HREKG", new BooleanDomain());
        RandVar errcauter = new RandVar("ERRCAUTER", new BooleanDomain());
        RandVar hrsat = new RandVar("HRSAT", new BooleanDomain());
        RandVar insuffanesth = new RandVar("INSUFFANESTH", new BooleanDomain());
        RandVar anaphylaxis = new RandVar("ANAPHYLAXIS", new BooleanDomain());
        RandVar tpr = new RandVar("TPR", new BooleanDomain());
        RandVar expco2 = new RandVar("EXPCO2", new BooleanDomain());

        RandVar kinkedtube = new RandVar("KINKEDTUBE", new BooleanDomain());
        RandVar minvol = new RandVar("MINVOL", new BooleanDomain());
        RandVar fio2 = new RandVar("FIO2", new BooleanDomain());
        RandVar pvsat = new RandVar("PVSAT", new BooleanDomain());
        RandVar sao2 = new RandVar("SAO2", new BooleanDomain());
        RandVar pap = new RandVar("PAP", new BooleanDomain());
        RandVar pulmembolus = new RandVar("PULMEMBOLUS", new BooleanDomain());
        RandVar shunt = new RandVar("SHUNT", new BooleanDomain());

        RandVar intubation = new RandVar("INTUBATION", new BooleanDomain());
        RandVar press = new RandVar("PRESS", new BooleanDomain());
        RandVar disconnect = new RandVar("DISCONNECT", new BooleanDomain());
        RandVar minvolset = new RandVar("MINVOLSET", new BooleanDomain());
        RandVar ventmach = new RandVar("VENTMACH", new BooleanDomain());
        RandVar venttube = new RandVar("VENTTUBE", new BooleanDomain());
        RandVar ventlung = new RandVar("VENTLUNG", new BooleanDomain());
        RandVar ventalv = new RandVar("VENTALV", new BooleanDomain());

        RandVar artco2 = new RandVar("ARTCO2", new BooleanDomain());
        RandVar catechol = new RandVar("CATECHOL", new BooleanDomain());
        RandVar hr = new RandVar("HR", new BooleanDomain());
        RandVar co = new RandVar("CO", new BooleanDomain());
        RandVar bp = new RandVar("BP", new BooleanDomain());

        /**
         * [bayes.struct.FullCPTNodeEd]s declaration
         */
        FiniteNode minvolset_node = new FullCPTNodeEd(minvolset, new double[]{0.5, 0.5});

        FiniteNode hypovolemia_node = new FullCPTNodeEd(hypovolemia, new double[]{0.2, 0.8});
        FiniteNode lvfailure_node = new FullCPTNodeEd(lvfailure, new double[]{0.05, 0.95});
        FiniteNode errlowoutput_node = new FullCPTNodeEd(errlowoutput, new double[]{0.05, 0.95});
        FiniteNode errcauter_node = new FullCPTNodeEd(errcauter, new double[]{0.1, 0.9});
        FiniteNode insuffanesth_node = new FullCPTNodeEd(insuffanesth, new double[]{0.1, 0.9});
        FiniteNode anaphylaxis_node = new FullCPTNodeEd(anaphylaxis, new double[]{0.01, 0.99});
        FiniteNode kinkedtube_node = new FullCPTNodeEd(kinkedtube, new double[]{0.04, 0.96});
        FiniteNode fio2_node = new FullCPTNodeEd(fio2, new double[]{0.05, 0.95});
        FiniteNode pulmembolus_node = new FullCPTNodeEd(pulmembolus, new double[]{0.01, 0.99});
        FiniteNode intubation_node = new FullCPTNodeEd(intubation, new double[]{0.92, 0.08});
        FiniteNode disconnect_node = new FullCPTNodeEd(disconnect, new double[]{0.1, 0.9});

        FiniteNode catechol_node = new FullCPTNodeEd(catechol, new double[]{0.5, 0.5});
        FiniteNode artco2_node = new FullCPTNodeEd(artco2, new double[]{0.5, 0.5});
        FiniteNode ventlung_node = new FullCPTNodeEd(ventlung, new double[]{0.5, 0.5});


        FiniteNode history_node = new FullCPTNodeEd(history, new double[]{
                0.9, 0.1,
                0.01, 0.99
        }, lvfailure_node);

        FiniteNode lvedvolume_node = new FullCPTNodeEd(lvedvolume, new double[]{
                0.95, 0.05,
                0.98, 0.02,
                0.01, 0.99,
                0.05, 0.95
        }, hypovolemia_node, lvfailure_node);

        FiniteNode cvp_node = new FullCPTNodeEd(cvp, new double[]{
                0.85, 0.15,
                0.29, 0.71
        }, lvedvolume_node);

        FiniteNode pcwp_node = new FullCPTNodeEd(pcwp, new double[]{
                0.85, 0.15,
                0.05, 0.95
        }, lvedvolume_node);

        FiniteNode strokevolume_node = new FullCPTNodeEd(strokevolume, new double[]{
                0.98, 0.02,
                0.95, 0.05,
                0.45, 0.55,
                0.05, 0.95
        }, hypovolemia_node, lvfailure_node);

        FiniteNode hr_node = new FullCPTNodeEd(hr, new double[]{
                0.85, 0.15,
                0.05, 0.95
        }, history_node);

        FiniteNode hrbp_node = new FullCPTNodeEd(hrbp, new double[]{
                0.98, 0.02,
                0.40, 0.60,
                0.30, 0.70,
                0.02, 0.98
        }, errlowoutput_node, hr_node);

        FiniteNode hrekg_node = new FullCPTNodeEd(hrekg, new double[]{
                0.34, 0.66,
                0.34, 0.66,
                0.98, 0.02,
                0.01, 0.99
        }, errcauter_node, hr_node);

        FiniteNode hrsat_node = new FullCPTNodeEd(hrsat, new double[]{
                0.34, 0.66,
                0.34, 0.66,
                0.98, 0.02,
                0.01, 0.99
        }, errcauter_node, hr_node);

        FiniteNode tpr_node = new FullCPTNodeEd(tpr, new double[]{
                0.98, 0.02,
                0.3, 0.7
        }, anaphylaxis_node);

        FiniteNode expco2_node = new FullCPTNodeEd(expco2, new double[]{
                0.97, 0.03,
                0.98, 0.02,
                0.02, 0.98,
                0.01, 0.99
        }, artco2_node, ventlung_node);

        FiniteNode ventmach_node = new FullCPTNodeEd(ventmach, new double[]{
                0.98, 0.02,
                0.06, 0.94
        }, minvolset_node);

        FiniteNode venttube_node = new FullCPTNodeEd(venttube, new double[]{
                0.98, 0.02,
                0.95, 0.05,
                0.90, 0.1,
                0.69, 0.31
        }, disconnect_node, ventmach_node);

        ventlung_node = new FullCPTNodeEd(ventlung, new double[]{
                0.97, 0.03,
                0.31, 0.69,
                0.02, 0.98,
                0.98, 0.02,
                0.3, 0.7,
                0.25, 0.75,
                0.98, 0.02,
                0.39, 0.61
        }, intubation_node, kinkedtube_node, venttube_node);

        FiniteNode minvol_node = new FullCPTNodeEd(minvol, new double[]{
                0.97, 0.03,
                0.98, 0.02,
                0.02, 0.98,
                0.01, 0.99
        }, intubation_node, ventlung_node);

        FiniteNode ventalv_node = new FullCPTNodeEd(ventalv, new double[]{
                0.97, 0.03,
                0.98, 0.02,
                0.02, 0.98,
                0.12, 0.88
        }, intubation_node, ventlung_node);

        FiniteNode pvsat_node = new FullCPTNodeEd(pvsat, new double[]{
                1.00, 0.00,
                0.99, 0.01,
                1.00, 0.00,
                0.02, 0.98
        }, fio2_node, ventalv_node);

        FiniteNode shunt_node = new FullCPTNodeEd(shunt, new double[]{
                0.1, 0.9,
                0.1, 0.9,
                0.95, 0.05,
                0.05, 0.95
        }, intubation_node, pulmembolus_node);

        FiniteNode sao2_node = new FullCPTNodeEd(sao2, new double[]{
                0.98, 0.02,
                0.95, 0.05,
                0.90, 0.1,
                0.69, 0.31
        }, pvsat_node, shunt_node);

        FiniteNode pap_node = new FullCPTNodeEd(pap, new double[]{
                0.2, 0.8,
                0.05, 0.95
        }, pulmembolus_node);

        FiniteNode press_node = new FullCPTNodeEd(press, new double[]{
                0.97, 0.03,
                0.31, 0.69,
                0.02, 0.98,
                0.98, 0.02,
                0.3, 0.7,
                0.25, 0.75,
                0.98, 0.02,
                0.39, 0.61
        }, intubation_node, kinkedtube_node, venttube_node);

        artco2_node = new FullCPTNodeEd(artco2, new double[]{
                0.02, 0.98,
                0.90, 0.10
        }, ventalv_node);

        catechol_node = new FullCPTNodeEd(catechol, new double[]{
                0.97, 0.03,
                0.31, 0.69,
                0.02, 0.98,
                0.98, 0.02,
                0.3, 0.7,
                0.25, 0.75,
                0.98, 0.02,
                0.39, 0.61,
                0.55, 0.45,
                0.17, 0.83,
                0.02, 0.98,
                0.98, 0.02,
                0.44, 0.56,
                0.25, 0.75,
                0.98, 0.02,
                0.19, 0.81
        }, artco2_node, insuffanesth_node, sao2_node, tpr_node);

        FiniteNode co_node = new FullCPTNodeEd(co, new double[]{
                0.98, 0.02,
                0.95, 0.05,
                0.31, 0.69,
                0.02, 0.98
        }, hr_node, strokevolume_node);

        FiniteNode bp_node = new FullCPTNodeEd(bp, new double[]{
                0.98, 0.02,
                0.98, 0.02,
                0.15, 0.85,
                0.45, 0.55
        }, co_node, tpr_node);

        return new EditableBayesianNetwork(hypovolemia_node, lvfailure_node, errlowoutput_node, errcauter_node, insuffanesth_node,
                anaphylaxis_node, kinkedtube_node, fio2_node, pulmembolus_node, intubation_node,
                disconnect_node, minvolset_node);
    }

    public static EditableBayesianNetwork constructBurglaryAlarmNetworkEd() {
        FiniteNode burglary = new FullCPTNodeEd(ExampleRV.BURGLARY_RV,
                new double[]{0.001, 0.999});
        FiniteNode earthquake = new FullCPTNodeEd(ExampleRV.EARTHQUAKE_RV,
                new double[]{0.002, 0.998});
        FiniteNode alarm = new FullCPTNodeEd(ExampleRV.ALARM_RV, new double[]{
                // B=true, E=true, A=true
                0.95,
                // B=true, E=true, A=false
                0.05,
                // B=true, E=false, A=true
                0.94,
                // B=true, E=false, A=false
                0.06,
                // B=false, E=true, A=true
                0.29,
                // B=false, E=true, A=false
                0.71,
                // B=false, E=false, A=true
                0.001,
                // B=false, E=false, A=false
                0.999}, burglary, earthquake);
        @SuppressWarnings("unused")
        FiniteNode johnCalls = new FullCPTNodeEd(ExampleRV.JOHN_CALLS_RV,
                new double[]{
                        // A=true, J=true
                        0.90,
                        // A=true, J=false
                        0.10,
                        // A=false, J=true
                        0.05,
                        // A=false, J=false
                        0.95}, alarm);
        @SuppressWarnings("unused")
        FiniteNode maryCalls = new FullCPTNodeEd(ExampleRV.MARY_CALLS_RV,
                new double[]{
                        // A=true, M=true
                        0.70,
                        // A=true, M=false
                        0.30,
                        // A=false, M=true
                        0.01,
                        // A=false, M=false
                        0.99}, alarm);

        return new EditableBayesianNetwork(burglary, earthquake);
    }

    public static CustomDBN BuildUmbrellaRain() {

        // Prior belief state
        FiniteNode rain_tm1 = new FullCPTNodeEd(ExampleRV.RAIN_tm1_RV,
                new double[]{0.5, 0.5});

        FiniteNode rain_t = new FullCPTNodeEd(ExampleRV.RAIN_t_RV, new double[]{
                // R_t-1 = true, R_t = true
                0.7,
                // R_t-1 = true, R_t = false
                0.3,
                // R_t-1 = false, R_t = true
                0.3,
                // R_t-1 = false, R_t = false
                0.7}, rain_tm1);
        // Sensor Model
        @SuppressWarnings("unused")
        FiniteNode umbrealla_t = new FullCPTNodeEd(ExampleRV.UMBREALLA_t_RV,
                new double[]{
                        // R_t = true, U_t = true
                        0.9,
                        // R_t = true, U_t = false
                        0.1,
                        // R_t = false, U_t = true
                        0.2,
                        // R_t = false, U_t = false
                        0.8}, rain_t);

        Map<String, String> X0_to_X1 = new HashMap<>();
        X0_to_X1.put("Rain_t-1", "Rain_t");
        return new CustomDBN(X0_to_X1, rain_tm1);
    }

    public static CustomDBN getRainWindNetwork() {
        // Prior belief state
        FiniteNode rain_tm1 = new FullCPTNodeEd(ExampleRV.RAIN_tm1_RV, new double[]{0.5, 0.5});
        FiniteNode wind_tm1 = new FullCPTNodeEd(UmbrellaParticle.WIND_tm1_RV, new double[]{0.9, 0.1});

        // Transition Model
        FiniteNode rain_t = new FullCPTNodeEd(ExampleRV.RAIN_t_RV, new double[]{
                // R_t-1 = true, W_t-1 = true, R_t = true
                0.6,
                // R_t-1 = true, W_t-1 = true, R_t = false
                0.4,
                // R_t-1 = true, W_t-1 = false, R_t = true
                0.8,
                // R_t-1 = true, W_t-1 = false, R_t = false
                0.2,
                // R_t-1 = false, W_t-1 = true, R_t = true
                0.4,
                // R_t-1 = false, W_t-1 = true, R_t = false
                0.6,
                // R_t-1 = false, W_t-1 = false, R_t = true
                0.2,
                // R_t-1 = false, W_t-1 = false, R_t = false
                0.8
        }, rain_tm1, wind_tm1);

        FiniteNode wind_t = new FullCPTNodeEd(UmbrellaParticle.WIND_t_RV, new double[]{
                // W_t-1 = true, W_t = true
                0.7,
                // W_t-1 = true, W_t = false
                0.3,
                // W_t-1 = false, W_t = true
                0.3,
                // W_t-1 = false, W_t = false
                0.7}, wind_tm1);

        // Sensor Model
        @SuppressWarnings("unused")
        FiniteNode umbrealla_t = new FullCPTNodeEd(ExampleRV.UMBREALLA_t_RV,
                new double[]{
                        // R_t = true, U_t = true
                        0.9,
                        // R_t = true, U_t = false
                        0.1,
                        // R_t = false, U_t = true
                        0.2,
                        // R_t = false, U_t = false
                        0.8}, rain_t);

        //Set<RandomVariable> E_1 = new HashSet<RandomVariable>();
        //E_1.add(ExampleRV.UMBREALLA_t_RV);
        Map<String, String> X0_to_X1 = new HashMap<>();
        X0_to_X1.put("Rain_t-1", "Rain_t");
        X0_to_X1.put("Wind_t-1", "Wind_t");
        return new CustomDBN(X0_to_X1, rain_tm1, wind_tm1);

    }

    public static CustomDBN makeDNB(String path) {
        CustomDBN dbn = (CustomDBN) BifReader.readBIF(new BIFHandlerToCustomDBN(), path);
        Set<Node> children = new HashSet<>();

        for (EditableNode node: dbn.getRootNodes()) {
           children.addAll(node.getChildren());
        }

        Node[] childrenList = children.toArray(Node[]::new);
        int rootIdx = 0;
        Map<String, String> X0_to_X1 = new HashMap<>();
        for (EditableNode node: dbn.getRootNodes()) {
            X0_to_X1.put(node.getRandomVariable().getName(), childrenList[rootIdx].getRandomVariable().getName());
            rootIdx += 1;
        }
        dbn.setX0_to_X1(X0_to_X1);

        System.out.println(String.format("Loaded %s with %d root nodes", path, dbn.getRootNodes().size()));

        Set<Node> parents = new HashSet<>();
        for (Node child: children)
            parents.addAll(child.getParents());

        parents.removeAll(dbn.getRootNodes());
        System.out.println(String.format("N. hidden variabiles: %d", parents.size()));

        return dbn;
    }
}
