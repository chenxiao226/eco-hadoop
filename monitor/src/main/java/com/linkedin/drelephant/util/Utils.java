package com.linkedin.drelephant.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.linkedin.drelephant.analysis.Severity;

public final class Utils {
	private static final Logger logger = Logger.getLogger(Utils.class);

	private static final String TRUNCATE_SUFFIX = "...";

	private Utils() {
		// do nothing
	}

	/**
	 * Load an XML document from a file path
	 * 
	 * @param filePath
	 *            The file path to load
	 * @return The loaded Document object
	 */
	public static Document loadXMLDoc(String filePath) {
		logger.info("Loading configuration file " + filePath);
		Document document = null;
		try {
			// 获得一个XML文件的解析器
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// 解析XML文件生成DOM文档的接口类，以便访问DOM。
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new File(filePath));
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("XML Parser could not be created.", e);
		} catch (SAXException e) {
			throw new RuntimeException(filePath + " is not properly formed", e);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read " + filePath, e);
		}

		return document;
	}

	/**
	 * Given a configuration element, extract the params map.
	 * 
	 * @param confElem
	 *            the configuration element
	 * @return the params map or an empty map if one can't be found
	 */
	public static Map<String, String> getConfigurationParameters(Element confElem) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		Node paramsNode = confElem.getElementsByTagName("params").item(0);
		if (paramsNode != null) {
			NodeList paramsList = paramsNode.getChildNodes();
			for (int j = 0; j < paramsList.getLength(); j++) {
				Node paramNode = paramsList.item(j);
				if (paramNode != null && !paramsMap.containsKey(paramNode.getNodeName())) {
					paramsMap.put(paramNode.getNodeName(), paramNode.getTextContent());
				}
			}
		}
		return paramsMap;
	}

	/**
	 * Given a mapreduce job's application id, get its corresponding job id
	 * 
	 * @param appId
	 *            The application id of the job
	 * @return the corresponding job id
	 */
	public static String getJobIdFromApplicationId(String appId) {
		return appId.replaceAll("application", "job");
	}

	/**
	 * Returns the configured thresholds after evaluating and verifying the
	 * levels.
	 * 
	 * @param rawLimits
	 *            A comma separated string of threshold limits
	 * @param thresholdLevels
	 *            The number of threshold levels
	 * @return The evaluated threshold limits
	 */
	public static double[] getParam(String rawLimits, int thresholdLevels) {
		double[] parsedLimits = null;

		if (rawLimits != null && !rawLimits.isEmpty()) {
			String[] thresholds = rawLimits.split(",");
			if (thresholds.length != thresholdLevels) {
				logger.error("Could not find " + thresholdLevels + " threshold levels in " + rawLimits);
				parsedLimits = null;
			} else {
				// Evaluate the limits
				parsedLimits = new double[thresholdLevels];
				ScriptEngineManager mgr = new ScriptEngineManager(null);
				ScriptEngine engine = mgr.getEngineByName("JavaScript");
				for (int i = 0; i < thresholdLevels; i++) {
					try {
						parsedLimits[i] = Double.parseDouble(engine.eval(thresholds[i]).toString());
					} catch (ScriptException e) {
						logger.error("Could not evaluate " + thresholds[i] + " in " + rawLimits);
						parsedLimits = null;
					}
				}
			}
		}

		return parsedLimits;
	}

	/**
	 * Compute the score for the heuristic based on the number of tasks and
	 * severity. This is applicable only to mapreduce applications.
	 * 
	 * Score = severity * num of tasks (where severity NOT in [NONE, LOW])
	 * 
	 * @param severity
	 *            The heuristic severity
	 * @param tasks
	 *            The number of tasks (map/reduce)
	 * @return
	 */
	public static int getHeuristicScore(Severity severity, int tasks) {
		int score = 0;
		if (severity != Severity.NONE && severity != Severity.LOW) {
			score = severity.getValue() * tasks;
		}
		return score;
	}

	/**
	 * Truncate the field by the specified limit
	 *
	 * @param field
	 *            the field to br truncated
	 * @param limit
	 *            the truncation limit
	 * @return The truncated field
	 */
	public static String truncateField(String field, int limit, String appId) {
		if (field != null && limit > TRUNCATE_SUFFIX.length() && field.length() > limit) {
			logger.info("Truncating " + field + " to " + limit + " characters for " + appId);
			field = field.substring(0, limit - 3) + "...";
		}
		return field;
	}

	/**
	 * Checks if the property is set
	 *
	 * @param property
	 *            The property to tbe checked.
	 * @return true if set, false otherwise
	 */
	public static boolean isSet(String property) {
		return property != null && !property.isEmpty();
	}

	/**
	 * Get non negative int value from Configuration.
	 *
	 * If the value is not set or not an integer, the provided default value is
	 * returned. If the value is negative, 0 is returned.
	 *
	 * @param conf
	 *            Configuration to be extracted
	 * @param key
	 *            property name
	 * @param defaultValue
	 *            default value
	 * @return non negative int value
	 */
	public static int getNonNegativeInt(Configuration conf, String key, int defaultValue) {
		try {
			int value = conf.getInt(key, defaultValue);
			if (value < 0) {
				value = 0;
				logger.warn("Configuration " + key + " is negative. Resetting it to 0");
			}
			return value;
		} catch (NumberFormatException e) {
			logger.error("Invalid configuration " + key + ". Value is " + conf.get(key)
					+ ". Resetting it to default value: " + defaultValue);
			return defaultValue;
		}
	}

	/**
	 * Get non negative long value from Configuration.
	 *
	 * If the value is not set or not a long, the provided default value is
	 * returned. If the value is negative, 0 is returned.
	 *
	 * @param conf
	 *            Configuration to be extracted
	 * @param key
	 *            property name
	 * @param defaultValue
	 *            default value
	 * @return non negative long value
	 */
	public static long getNonNegativeLong(Configuration conf, String key, long defaultValue) {
		try {
			long value = conf.getLong(key, defaultValue);
			if (value < 0) {
				value = 0;
				logger.warn("Configuration " + key + " is negative. Resetting it to 0");
			}
			return value;
		} catch (NumberFormatException e) {
			logger.error("Invalid configuration " + key + ". Value is " + conf.get(key)
					+ ". Resetting it to default value: " + defaultValue);
			return defaultValue;
		}
	}
}