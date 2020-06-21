package bayes.struct;

import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;
import bayes.utils.CustomEliminationAsk;

import java.util.*;

public class CustomDBN extends EditableBayesianNetwork implements BayesianNetwork {
    // Mappa per sapere la successione dei nodi
    // Es. Rain_tm1 -> Rain_t
    //     Wind_tm1 -> Wind_t
    private Map<String, String> X0_to_X1;
    public String ordering = "TI";

    public CustomDBN(Node... rootNodes) {
        super(rootNodes);
    }

    public CustomDBN( Map<String, String> X0_to_X1, Node... rootNodes) {
        super(rootNodes);
        this.X0_to_X1 = X0_to_X1;
    }

    public Map<String, String> getX0_to_X1() {
        return X0_to_X1;
    }

    public void setX0_to_X1(Map<String, String> x0_to_X1) {
        X0_to_X1 = x0_to_X1;
    }

    /**
     * Metodo di forward per una rete bayesiana dinamica. Consiste in due step:
     * 1. la creazione dei fattori per il passo al tempo t basandosi sullo stato della
     * rete al tempo t-1.
     * 2. Aggiornamento dei prior belief della rete al tempo t per la prossima iterazione.
     *
     * @param aps array con le variabili di evidenza della rete al tempo t-1
     */
    public List<Factor> forward(AssignmentProposition[] aps) {
        ArrayList<Factor> factors = makeFactors(aps);
        updatePrior(factors);
        return factors;
    }

    /**
     * Metodo che esegue l'update della prior belief dei nodi root della rete.
     * Dopo aver calcolato i fattori dell'iterazione t-1 vengono aggiornate le CPT
     * dei nodi radice che interessano i fattori.
     *
     * @param factors  fattori calcolati all'iterazione t-1 */
    private void updatePrior(ArrayList<Factor> factors) {
        for (FullCPTNodeEd node : getRootNodes()) {
            String targetName = this.X0_to_X1.get(node.getRandomVariable().getName());
            if (targetName == null)
                continue;

            for (Factor factor : factors) {
                Set<RandomVariable> vars = factor.getArgumentVariables();
                boolean contains = false;
                for(RandomVariable var : vars)
                    if (var.getName().equals(targetName))
                        contains = true;
                if (!contains) continue;

                EditableCPT pt = (EditableCPT) node.getCPD();
                ProbabilityTable probabilityTable = pt.getProbabilityTable();

                for (int i = 0; i < factor.getValues().length; i++)
                    probabilityTable.setValue(i, factor.getValues()[i]);
                pt.setProbabilityTable(probabilityTable);
                //System.out.println("New prior for " + targetName + ": " + probabilityTable.toString());
            }
        }
    }

    /**
     * Crea la lista di fattori per le variabili di stato da da assegnare al passo t.
     * Dopo aver calcolato i fatori tramite VE, viene fatta una marginalizzazione su ogni
     * singolo fattore per ottenere la sua distribuzione. Infine i fattori vengono
     * moltiplicati per le eventuali evidenze corrispondenti.
     *
     * @param aps array con le variabili di evidenza della rete al tempo t-1
     */
    private ArrayList<Factor> makeFactors(AssignmentProposition[] aps) {
        Set<RandomVariable> hidden = new HashSet<>();
        List<RandomVariable> VARS = new ArrayList<>();

        //calculateVariables(new RandomVariable[]{getStateVariables()[0]}, aps, hidden, VARS);
        calculateVariables(getStateVariables(), aps, hidden, VARS);

        List<Factor> factors = new ArrayList<>();
        for (RandomVariable var : order(VARS)) {
            // factors <- [MAKE-FACTOR(var, e) | factors]
            factors.add(0, makeFactor(var, aps));
        }

        // Ottengo i fattori con Dalviche
        // factors <- []
        //List<Factor> factors = new ArrayList<>();
        // for each var in ORDER(bn.VARS) do
        List<RandomVariable> orderedVars = new LinkedList<>();

        if (ordering.equals("TI"))
            orderedVars = order(VARS);
        else if (ordering.equals("MD"))
            orderedVars = CustomEliminationAsk.minDegreeOrder(factors, aps, VARS);
        else if (ordering.equals("MF"))
            orderedVars = CustomEliminationAsk.minFillOrder(factors, aps, VARS);
        else
            System.out.println("Unkown ordering " + ordering);

        for (RandomVariable var : orderedVars) {
            //factors.add(0, makeFactor(var, aps));
            if (hidden.contains(var))
                factors = sumOut(var, factors);
        }

        // Per ogni fattore faccio sumout delle altre variabili,
        // Cosi da ottenere la distribuzione per la variabile singola
        List<Factor> summedOutFactors = new ArrayList<>();
        for (Factor factor: factors) {
            Set<RandomVariable> rvs = factor.getArgumentVariables();
            for (RandomVariable rv : rvs) {
                Set<RandomVariable> toSumOut = new HashSet<>(rvs);
                toSumOut.remove(rv);
                Factor summedOut = factor.sumOut(toSumOut.toArray(RandomVariable[]::new));
                summedOutFactors.add(summedOut);
            }
        }

        // Moltiplico distribuzioni di variabili di stato
        // per eventuale evidenze corrispondenti
        ArrayList<Factor> priors = new ArrayList<>();
        for (RandomVariable var : getStateVariables()) {
            Factor prior = null;
            for (Factor f : summedOutFactors) {
                if (!f.contains(var)) continue;
                if (prior == null)
                    prior = f;
                else {
                    prior = prior.pointwiseProduct(f);
                }
            }
            if (prior != null)
                priors.add(((ProbabilityTable)(prior)).normalize());
        }

        return priors;
    }

    public RandomVariable[] getStateVariables() {
        Set<RandomVariable> qrv = new HashSet<>();

        Set<FullCPTNodeEd> roots = this.getRootNodes();
        for (EditableNode root : roots) {
            for (Node child : root.getChildren())
                qrv.add(child.getRandomVariable());
        }
        return qrv.toArray(RandomVariable[]::new);
    }


    protected void calculateVariables(final RandomVariable[] X, final AssignmentProposition[] e, Set<RandomVariable> hidden, Collection<RandomVariable> bnVARS) {
        bnVARS.addAll(this.getVariablesInTopologicalOrder());
        hidden.addAll(bnVARS);

        for (RandomVariable x : X) {
            hidden.remove(x);
        }
        for (AssignmentProposition ap : e) {
            hidden.removeAll(ap.getScope());
        }
    }

    protected List<RandomVariable> order(Collection<RandomVariable> vars) {
        List<RandomVariable> order = new ArrayList<>(vars);
        Collections.reverse(order);
        return order;
    }


    /********************
    * PRIVATE METHODS   *
    *********************/
    private Factor makeFactor(RandomVariable var, AssignmentProposition[] e) {
        Node n = this.getNode(var);
        if (!(n instanceof FiniteNode)) {
            throw new IllegalArgumentException(
                    "Elimination-Ask only works with finite Nodes.");
        }
        FiniteNode fn = (FiniteNode) n;
        List<AssignmentProposition> evidence = new ArrayList<AssignmentProposition>();
        for (AssignmentProposition ap : e) {
            if (fn.getCPT().contains(ap.getTermVariable())) {
                evidence.add(ap);
            }
        }

        return fn.getCPT().getFactorFor(
                evidence.toArray(new AssignmentProposition[evidence.size()]));
    }


    private List<Factor> sumOut(RandomVariable var, List<Factor> factors) {
        List<Factor> summedOutFactors = new ArrayList<>();
        List<Factor> toMultiply = new ArrayList<>();
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

}
