package bnparser;/*
 * Encog(tm) Core v3.4 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2017 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bnparser.bif.BIFDefinition;
import bnparser.bif.BIFVariable;
import bnparser.bif.FileSection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.bayes.impl.FullCPTNode;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.domain.FiniteIntegerDomain;
import aima.core.probability.util.RandVar;

/**
 * Handler, used to parse the XML BIF files.
 */
public class MyBIFHandler extends DefaultHandler {
	/**
	 * The current section.
	 */
	private final List<FileSection> currentSection = new ArrayList<FileSection>();

	/**
	 * BIF variables.
	 */
	private final List<BIFVariable> bifVariables = new ArrayList<BIFVariable>();

	/**
	 * BIF definitions.
	 */
	private final List<BIFDefinition> bifDefinitions = new ArrayList<BIFDefinition>();

	/**
	 * The current variable.
	 */
	private BIFVariable currentVariable;

	/**
	 * The current definition.
	 */
	private BIFDefinition currentDefinition;

	/**
	 * THe current string.
	 */
	private String currentString;

	private Map<String, RandomVariable> rvs = new LinkedHashMap<String, RandomVariable>();
	private Map<RandomVariable, Node> nds = new LinkedHashMap<RandomVariable, Node>();
	private List<Node> rootNodes = new ArrayList<Node>();

	/**
	 * The network bing loaded.
	 */
	private BayesianNetwork network;

	/**
	 * Constructor.
	 */
	public MyBIFHandler() {
		this.network = null;
	}

	/**
	 * Handle the beginning of the BIF tag.
	 * 
	 * @param qName      The name of the tag.
	 * @param attributes The attributes.
	 */
	private void handleBeginBIF(String qName, Attributes attributes) {
		if (qName.equals("NETWORK")) {
			this.currentSection.add(FileSection.NETWORK);
		}
	}

	/**
	 * Handle the beginning of the BIF tag.
	 * 
	 * @param qName      The name of the tag.
	 * @param attributes The attributes.
	 */
	private void handleBeginNETWORK(String qName, Attributes attributes) {
		if (qName.equals("VARIABLE")) {
			this.currentSection.add(FileSection.VARIABLE);
			this.currentVariable = new BIFVariable();
			this.bifVariables.add(this.currentVariable);
		} else if (qName.equals("DEFINITION")) {
			this.currentSection.add(FileSection.DEFINITION);
			this.currentDefinition = new BIFDefinition();
			this.bifDefinitions.add(this.currentDefinition);
		}
	}

	/**
	 * Handle the beginning of the BIF tag.
	 * 
	 * @param qName      The name of the tag.
	 * @param attributes The attributes.
	 */
	private void handleBeginVARIABLE(String qName, Attributes attributes) {
		if (qName.equals("VARIABLE")) {
			this.currentVariable = new BIFVariable();
			this.bifVariables.add(this.currentVariable);
		}
	}

	/**
	 * Handle the beginning of the DEFINITION tag.
	 * 
	 * @param qName The name of the tag.
	 */
	private void handleBeginDEFINITION(String qName, Attributes attributes) {
		if (qName.equals("DEFINITION")) {
			this.currentDefinition = new BIFDefinition();
			this.bifDefinitions.add(this.currentDefinition);
		}
	}

	/**
	 * Handle the end of the BIF tag.
	 * 
	 * @param qName The name of the tag.
	 */
	private void handleEndBIF(String qName) {
		if (qName.equals("BIF")) {
			this.currentSection.remove(this.currentSection.size() - 1);
		}
	}

	/**
	 * Handle the end of the NETWORK tag.
	 * 
	 * @param qName The name of the tag.
	 */
	private void handleEndNETWORK(String qName) {
		if (qName.equals("NETWORK")) {
			this.currentSection.remove(this.currentSection.size() - 1);
		}
	}

	/**
	 * Handle the end of the VARIABLE tag.
	 * 
	 * @param qName The name of the tag.
	 */
	private void handleEndVARIABLE(String qName) {
		if (qName.equals("NAME")) {
			this.currentVariable.setName(this.currentString);
		} else if (qName.equals("OUTCOME")) {
			this.currentVariable.addOption(this.currentString);
		} else if (qName.equals("VARIABLE")) {
			this.currentSection.remove(this.currentSection.size() - 1);
		}
	}

	/**
	 * Handle the end of the DEFINITION tag.
	 * 
	 * @param qName The name of the tag.
	 */
	private void handleEndDEFINITION(String qName) throws Exception {
		if (qName.equals("FOR")) {
			this.currentDefinition.setForDefinition(this.currentString);
		} else if (qName.equals("GIVEN")) {
			this.currentDefinition.addGiven(this.currentString);
		} else if (qName.equals("TABLE")) {
			this.currentDefinition.setTable(this.currentString);
		} else if (qName.equals("DEFINITION")) {
			this.currentSection.remove(this.currentSection.size() - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (this.currentSection.size() == 0 && qName.equals("BIF")) {
			this.currentSection.add(FileSection.BIF);
		}

		else if (this.currentSection.size() > 0) {
			switch (this.currentSection.get(this.currentSection.size() - 1)) {
			case BIF:
				handleBeginBIF(qName, attributes);
				break;
			case DEFINITION:
				handleBeginDEFINITION(qName, attributes);
				break;
			case NETWORK:
				handleBeginNETWORK(qName, attributes);
				break;
			case VARIABLE:
				handleBeginVARIABLE(qName, attributes);
				break;
			}
		}
	}

	/**
	 * @return The network being parsed.
	 */
	public BayesianNetwork getNetwork() {
		return network;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);

		if (this.currentSection.size() > 0) {
			switch (this.currentSection.get(this.currentSection.size() - 1)) {
			case BIF:
				handleEndBIF(qName);
				break;
			case DEFINITION:
                                try {
                                    handleEndDEFINITION(qName);
                                } catch (Exception e) {
                                    throw new SAXException();
                                }                             
                                break;
			case NETWORK:
				handleEndNETWORK(qName);
				break;
			case VARIABLE:
				handleEndVARIABLE(qName);
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();

		// define variables
		createRandVariables();

		// define relations and tables
		List<RandomVariable> tmpList = new ArrayList<RandomVariable>(rvs.values());
		for (BIFDefinition d : this.bifDefinitions) {
			if (d.getGivenDefinitions().size() == 0) {
				Node node = new FullCPTNode(this.rvs.get(d.getForDefinition()), d.getTable());
//				String a = "<";
//				for (int i = 0; i < d.getTable().length; i++) {
//					a = a + " " + d.getTable()[i];
//				}
//				a = a + ">";
//				System.out.println(a);
				rootNodes.add(node);
				nds.put(this.rvs.get(d.getForDefinition()), node);
				tmpList.remove(this.rvs.get(d.getForDefinition()));
			}
		}
		RandomVariable old = null;
		while (!tmpList.isEmpty()) {

			RandomVariable next = tmpList.get(0);

			if (next.getName().equals("STROKEVOLUME")) {
				int g = 0;
			}
			tmpList.remove(0);
			BIFDefinition bDefinition = searchBIFDef(next.getName());
			List<Node> parents = new ArrayList<Node>();
			boolean check = true;
			for (String parent : bDefinition.getGivenDefinitions()) {
				if (!nds.containsKey(rvs.get(parent))) {
					check = false;
					break;
				} else {
					parents.add(nds.get(rvs.get(parent)));
				}
			}
			if (check) {
				nds.put(next, new FullCPTNode(next, bDefinition.getTable(), parents.toArray(new Node[parents.size()])));
//				String a = "<";
//				for (int i = 0; i < bDefinition.getTable().length; i++) {
//					a = a + " " + bDefinition.getTable()[i];
//				}
//				a = a + ">";
//				System.out.println(a);
			} else {
				tmpList.add(tmpList.size(), next);
			}

		}

		this.network = new BayesNet(rootNodes.toArray(new FullCPTNode[rootNodes.size()]));
	}

	private BIFDefinition searchBIFDef(String rvName) {
		BIFDefinition res = null;
		for (BIFDefinition d : this.bifDefinitions) {
			if (d.getForDefinition().equals(rvName)) {
				res = d;
				break;
			}
		}
		return res;
	}

	private void createRandVariables() {
		for (BIFVariable v : this.bifVariables) {
			boolean boolDomainCheck = false;
			boolean[] intDomainChecks = new boolean[v.getOptions().size()];
			int i = 0;
			for (String a : v.getOptions()) {
				if (a.equals("TRUE") || a.equals("FALSE")) {
					boolDomainCheck = true;
					i++;
					continue;
				}
				try {
					Integer.parseInt(a);
				} catch (NumberFormatException e) {
					intDomainChecks[i] = false;
				} catch (NullPointerException e) {
					intDomainChecks[i] = false;
				}
				i++;
			}
			boolean intDomainCheck = true;
			Integer[] intDomain = new Integer[v.getOptions().size()];
			i = 0;
			for (boolean ch : intDomainChecks) {
				if (!ch) {
					intDomainCheck = false;
					break;
				} else {
					intDomain[i] = Integer.parseInt(v.getOptions().get(i));
					i++;
				}
			}
			if (boolDomainCheck) {
				rvs.put(v.getName(), new RandVar(v.getName(), new BooleanDomain()));
				// System.out.println("- " + v.getName() + ", domain: " + new BooleanDomain());
			} else if (intDomainCheck) {
				rvs.put(v.getName(), new RandVar(v.getName(), new FiniteIntegerDomain(intDomain)));
				// System.out.println("- " + v.getName() + ", domain: " + new
				// FiniteIntegerDomain(intDomain));
			} else {
				rvs.put(v.getName(), new RandVar(v.getName(), new ArbitraryTokenDomain(v.getOptions().toArray())));
				// System.out.println(
				// "- " + v.getName() + ", domain: " + new
				// ArbitraryTokenDomain(v.getOptions().toArray()));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		this.currentString = new String(ch, start, length);
	}
}