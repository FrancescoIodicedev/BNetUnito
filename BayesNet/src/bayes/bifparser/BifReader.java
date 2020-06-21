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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import bayes.bifparser.BIFHandlerToEBN;
import bayes.bifparser.BIFHandlerToEditableBN;
import org.xml.sax.SAXException;

import aima.core.probability.bayes.BayesianNetwork;


/**
 * A utility class to read and write Bayesian networks in BIF format.
 * 
 * http://www.heatonresearch.com/wiki/Bayesian_Interchange_Format
 */
public class BifReader {

	/**
	 * Read a BIF file.
	 * 
	 * @param f The BIF file.
	 * @return The Bayesian network that was read.
	 */
	public static BayesianNetwork readBIF(BIFHandlerToEBN h, String f) {
		return readBIF(h, new File(f));
	}

	public static BayesianNetwork readBIF(BIFHandlerToEBN h, File f) {
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(f);
			return readBIF(h, fis);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ex) {
					// who cares at this point.
				}
			}
		}
	}

	/**
	 * Read a BIF file from a stream.
	 * 
	 * @param is The stream to read from.
	 * @return The Bayesian network read.
	 */
	public static BayesianNetwork readBIF(BIFHandlerToEBN h, InputStream is) {
		try {
			//BIFHandlerToEditableBN h = new BIFHandlerToEditableBN();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(is, h);
			return h.getNetwork();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
