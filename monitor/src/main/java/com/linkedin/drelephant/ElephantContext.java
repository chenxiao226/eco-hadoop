package com.linkedin.drelephant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.linkedin.drelephant.analysis.ApplicationType;
import com.linkedin.drelephant.analysis.ElephantFetcher;
import com.linkedin.drelephant.analysis.HadoopApplicationData;
import com.linkedin.drelephant.analysis.HadoopMetricsAggregator;
import com.linkedin.drelephant.analysis.Heuristic;
import com.linkedin.drelephant.analysis.HeuristicResult;
import com.linkedin.drelephant.analysis.JobType;
import com.linkedin.drelephant.configurations.aggregator.AggregatorConfiguration;
import com.linkedin.drelephant.configurations.aggregator.AggregatorConfigurationData;
import com.linkedin.drelephant.configurations.fetcher.FetcherConfiguration;
import com.linkedin.drelephant.configurations.fetcher.FetcherConfigurationData;
import com.linkedin.drelephant.configurations.heuristic.HeuristicConfiguration;
import com.linkedin.drelephant.configurations.heuristic.HeuristicConfigurationData;
import com.linkedin.drelephant.configurations.jobtype.JobTypeConfiguration;
import com.linkedin.drelephant.util.Utils;

public class ElephantContext {
	private static final Logger logger = Logger.getLogger(ElephantContext.class);

	private static ElephantContext INSTANCE;

	// 返回可用的启发式的名字按应用类型分组。
	private final Map<String, List<String>> _heuristicGroupedNames = new HashMap<String, List<String>>();

////	 启发式规则
	private static final String AGGREGATORS_CONF ="src/main/resources/AggregatorConf.xml";
	private static final String JOB_TYPES_CONF = "src/main/resources/JobTypeConf.xml";
	private static final String GENERAL_CONF = "src/main/resources/GeneralConf.xml";// 目录和别人也不一样
	private static final String FETCHERS_CONF = "src/main/resources/FetcherConf.xml";
	private static final String HEURISTICS_CONF = "src/main/resources/HeuristicConf.xml";// 启发式规则
	
//	private static final String AGGREGATORS_CONF = ElephantContext.class.getClassLoader().getResource("/").getPath()
//			+ "AggregatorConf.xml";
//	private static final String JOB_TYPES_CONF = ElephantContext.class.getClassLoader().getResource("/").getPath()
//			+ "JobTypeConf.xml";
//	private static final String GENERAL_CONF = ElephantContext.class.getClassLoader().getResource("/").getPath()
//			+ "GeneralConf.xml";// 目录和别人也不一样
//	private static final String FETCHERS_CONF = ElephantContext.class.getClassLoader().getResource("/").getPath()
//			+ "FetcherConf.xml";
//	private static final String HEURISTICS_CONF = ElephantContext.class.getClassLoader().getResource("/").getPath()
//			+ "HeuristicConf.xml";// 启发式规则

	private List<AggregatorConfigurationData> _aggregatorConfData;
	private Map<ApplicationType, List<JobType>> _appTypeToJobTypes = new HashMap<ApplicationType, List<JobType>>();
	private Configuration _generalConf; // 我觉得这个有点问题，没有看到 _generalConf数据的载入
	private List<FetcherConfigurationData> _fetchersConfData;
	private List<HeuristicConfigurationData> _heuristicsConfData;
	private final Map<String, ApplicationType> _nameToType = new HashMap<String, ApplicationType>();

	private final Map<ApplicationType, HadoopMetricsAggregator> _typeToAggregator = new HashMap<ApplicationType, HadoopMetricsAggregator>();
	private final Map<ApplicationType, ElephantFetcher> _typeToFetcher = new HashMap<ApplicationType, ElephantFetcher>();
	private final Map<ApplicationType, List<Heuristic>> _typeToHeuristics = new HashMap<ApplicationType, List<Heuristic>>();
	// private final Map<String, Html> _heuristicToView = new HashMap<String,
	// Html>();

	public static void init() {
		INSTANCE = new ElephantContext();
	}

	public static ElephantContext instance() {
		if (INSTANCE == null) {
			INSTANCE = new ElephantContext();
		}
		return INSTANCE;
	}

	// private on purpose
	private ElephantContext() {
		loadConfiguration();
	}

	private void loadConfiguration() {

		loadAggregators(); // 载入一个com.linkedin.drelephant.mapreduce.MapReduceMetricsAggregator,<used,waste,delay>

		loadFetchers(); // Fetcher ,获取Job的数据 ，打印了6条

		loadHeuristics();// 启发式算法的载入

		loadJobTypes();// Job类型的载入

		loadGeneralConf();// GeneralConf.xml中的配置信息

//		// It is important to configure supported types in the LAST step so that
//		// we could have information from all
//		// configurable components.
//		configureSupportedApplicationTypes(); // 支持的类型

	}

	public void print() {
		System.out.println("ElephantContext");
		logger.info("logger info .....");
	}

	public static void main(String[] args) {

		ElephantContext ec = new ElephantContext();
		ec.loadAggregators();
		ec.loadJobTypes();
		ec.loadGeneralConf();
		ec.loadFetchers();
		ec.loadHeuristics();
		ec.configureSupportedApplicationTypes();
	}

	/**
	 * Load configured in AggregatorConf.xml
	 */
	private void loadAggregators() {
		Document document = Utils.loadXMLDoc(AGGREGATORS_CONF);// 打印com.linkedin.drelephant.util.Utils

		System.out.println(document);
		_aggregatorConfData = new AggregatorConfiguration(document.getDocumentElement())
				.getAggregatorsConfigurationData();
		for (AggregatorConfigurationData data : _aggregatorConfData) {
			System.out.println(data.getClassName());

			try {
				Class<?> aggregatorClass = Class.forName(data.getClassName());

				Object instance = aggregatorClass.getConstructor(AggregatorConfigurationData.class).newInstance(data);

				if (!(instance instanceof HadoopMetricsAggregator)) {
					throw new IllegalArgumentException("Class " + aggregatorClass.getName()
							+ " is not an implementation of " + HadoopMetricsAggregator.class.getName());
				} else {
					logger.info("Class " + aggregatorClass.getName() + " is an implementation of "
							+ HadoopMetricsAggregator.class.getName());
				}

				ApplicationType type = data.getAppType();
				if (_typeToAggregator.get(type) == null) {
					_typeToAggregator.put(type, (HadoopMetricsAggregator) instance);
				}

				logger.info("Load Aggregator : " + data.getClassName()); // Load
																			// Aggregator
																			// :
																			// com.linkedin.drelephant.mapreduce.MapReduceMetricsAggregator

			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Could not find class " + data.getClassName(), e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Could not instantiate class " + data.getClassName(), e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Could not access constructor for class" + data.getClassName(), e);
			} catch (RuntimeException e) {
				throw new RuntimeException(data.getClassName() + " is not a valid Aggregator class.", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Could not invoke class " + data.getClassName(), e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Could not find constructor for class " + data.getClassName(), e);
			}
		}
	}

	/**
	 * Load all the job types configured in JobTypeConf.xml
	 */
	private void loadJobTypes() {
		Document document = Utils.loadXMLDoc(JOB_TYPES_CONF);
		JobTypeConfiguration conf = new JobTypeConfiguration(document.getDocumentElement());
		_appTypeToJobTypes = conf.getAppTypeToJobTypeList();
	}

	/**
	 * Load in the GeneralConf.xml file as a configuration object for other
	 * objects to access
	 */
	private void loadGeneralConf() {
		logger.info("Loading configuration file " + GENERAL_CONF);

		_generalConf = new Configuration();
		InputStream in = null;
		try {
			in = new FileInputStream(new File(GENERAL_CONF));
			_generalConf.addResource(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// InputStream in =
		// this.getClass().getClassLoader().getResourceAsStream(GENERAL_CONF);
		// if(in==null){
		// in =
		// this.getClass().getClassLoader().getResourceAsStream(ElephantContext.class.getClassLoader().getResource("/").getPath().substring(1,
		// ElephantContext.class.getClassLoader().getResource("/").getPath().length()-16)+"GeneralConf.xml");
		// }
  
		// _generalConf.addResource(this.getClass().getClassLoader().getResourceAsStream(GENERAL_CONF));
	}

	/**
	 * Load all the fetchers configured in FetcherConf.xml
	 */
	private void loadFetchers() {
		Document document = Utils.loadXMLDoc(FETCHERS_CONF); // Loading
																// configuration
																// file
																// FetcherConf.xml

		_fetchersConfData = new FetcherConfiguration(document.getDocumentElement()).getFetchersConfigurationData();

		for (FetcherConfigurationData data : _fetchersConfData) {
			try {
				Class<?> fetcherClass = Class.forName(data.getClassName());
				Object instance = fetcherClass.getConstructor(FetcherConfigurationData.class).newInstance(data);
				if (!(instance instanceof ElephantFetcher)) {
					throw new IllegalArgumentException("Class " + fetcherClass.getName()
							+ " is not an implementation of " + ElephantFetcher.class.getName());
				}

				ApplicationType type = data.getAppType();
				if (_typeToFetcher.get(type) == null) {
					_typeToFetcher.put(type, (ElephantFetcher) instance);
				}

				logger.info("Load Fetcher : " + data.getClassName());

			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Could not find class " + data.getClassName(), e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Could not instantiate class " + data.getClassName(), e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Could not access constructor for class" + data.getClassName(), e);
			} catch (RuntimeException e) {
				throw new RuntimeException(data.getClassName() + " is not a valid Fetcher class.", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Could not invoke class " + data.getClassName(), e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Could not find constructor for class " + data.getClassName(), e);
			}
		}
	}

	/**
	 * Load all the heuristics and their views configured in HeuristicConf.xml
	 */
	private void loadHeuristics() {
		Document document = Utils.loadXMLDoc(HEURISTICS_CONF);

		_heuristicsConfData = new HeuristicConfiguration(document.getDocumentElement())
				.getHeuristicsConfigurationData();
		for (HeuristicConfigurationData data : _heuristicsConfData) {
			// Load all the heuristic classes
			try {
				Class<?> heuristicClass = Class.forName(data.getClassName());

				Object instance = heuristicClass.getConstructor(HeuristicConfigurationData.class).newInstance(data);
				if (!(instance instanceof Heuristic)) {
					throw new IllegalArgumentException("Class " + heuristicClass.getName()
							+ " is not an implementation of " + Heuristic.class.getName());
				}
				ApplicationType type = data.getAppType();
				List<Heuristic> heuristics = _typeToHeuristics.get(type);
				if (heuristics == null) {
					heuristics = new ArrayList<Heuristic>();
					_typeToHeuristics.put(type, heuristics);
				}
				heuristics.add((Heuristic) instance);

				logger.info("Load Heuristic : " + data.getClassName());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Could not find class " + data.getClassName(), e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Could not instantiate class " + data.getClassName(), e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Could not access constructor for class" + data.getClassName(), e);
			} catch (RuntimeException e) {
				// More descriptive on other runtime exception such as
				// ClassCastException
				throw new RuntimeException(data.getClassName() + " is not a valid Heuristic class.", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Could not invoke class " + data.getClassName(), e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Could not find constructor for class " + data.getClassName(), e);
			}
			// Load all the heuristic views
			// 可能有问题

		}
		// Bind No_DATA heuristic to its helper pages, no need to add any real
		// configurations
		_heuristicsConfData.add(new HeuristicConfigurationData(HeuristicResult.NO_DATA.getHeuristicName(),
				HeuristicResult.NO_DATA.getHeuristicClassName(), "views.html.help.helpNoData", null, null));
	}

	/**
	 * Decides what application types can be supported.
	 *
	 * An application type is supported if all the below are true. 1. A Fetcher
	 * is defined in FetcherConf.xml for the application type. 2. At least one
	 * Heuristic is configured in HeuristicConf.xml for the application type. 3.
	 * At least one job type is configured in JobTypeConf.xml for the
	 * application type.
	 */
	private void configureSupportedApplicationTypes() {
		Set<ApplicationType> supportedTypes = Sets.intersection(_typeToFetcher.keySet(), _typeToHeuristics.keySet());
		supportedTypes = Sets.intersection(supportedTypes, _appTypeToJobTypes.keySet());
		supportedTypes = Sets.intersection(supportedTypes, _typeToAggregator.keySet());

		_typeToAggregator.keySet().retainAll(supportedTypes);
		_typeToFetcher.keySet().retainAll(supportedTypes);
		_typeToHeuristics.keySet().retainAll(supportedTypes);
		_appTypeToJobTypes.keySet().retainAll(supportedTypes);

		logger.info("Configuring ElephantContext...");
		for (ApplicationType type : supportedTypes) {
			_nameToType.put(type.getName(), type);

			List<String> classes = new ArrayList<String>();
			List<Heuristic> heuristics = _typeToHeuristics.get(type);
			for (Heuristic heuristic : heuristics) {
				classes.add(heuristic.getClass().getName());
			}

			List<JobType> jobTypes = _appTypeToJobTypes.get(type);
			logger.info("Supports " + type.getName() + " application type, using " + _typeToFetcher.get(type).toString()
					+ " fetcher class with Heuristics [" + StringUtils.join(classes, ", ")
					+ "] and following JobTypes [" + StringUtils.join(jobTypes, ", ") + "].");
		}
	}

	/**
	 * Given an application type, return the currently bound heuristics
	 *
	 * @param type
	 *            The application type
	 * @return The corresponding heuristics
	 */
	public List<Heuristic> getHeuristicsForApplicationType(ApplicationType type) {
		return _typeToHeuristics.get(type);
	}

	/**
	 * Return the heuristic names available grouped by application type.
	 *
	 * @return A map of application type name -> a list of heuristic names
	 */
	public Map<String, List<String>> getAllHeuristicNames() {
		if (_heuristicGroupedNames.isEmpty()) {
			for (Map.Entry<ApplicationType, List<Heuristic>> entry : _typeToHeuristics.entrySet()) {
				ApplicationType type = entry.getKey();
				List<Heuristic> list = entry.getValue();

				List<String> nameList = new ArrayList<String>();
				for (Heuristic heuristic : list) {
					nameList.add(heuristic.getHeuristicConfData().getHeuristicName());
				}

				Collections.sort(nameList);
				_heuristicGroupedNames.put(type.getName(), nameList);
			}
		}

		return _heuristicGroupedNames;
	}

	/**
	 * Get the heuristic configuration data
	 *
	 * @return The configuration data of heuristics
	 */
	public List<HeuristicConfigurationData> getHeuristicsConfigurationData() {
		return _heuristicsConfData;
	}

	/**
	 * Given an application type, return the currently ElephantFetcher that
	 * binds with the type.
	 *
	 * @param type
	 *            The application type
	 * @return The corresponding fetcher
	 */
	public ElephantFetcher getFetcherForApplicationType(ApplicationType type) {
		return _typeToFetcher.get(type);
	}

	public HadoopMetricsAggregator getAggregatorForApplicationType(ApplicationType type) {
		return _typeToAggregator.get(type);
	}

	/**
	 * Get the application type given a type name.
	 *
	 * @return The corresponding application type, null if not found
	 */
	public ApplicationType getApplicationTypeForName(String typeName) {
		return _nameToType.get(typeName.toUpperCase());
	}

	/**
	 * Get the general configuration object.
	 *
	 * @return the genral configuration object.
	 */
	public Configuration getGeneralConf() {
		return _generalConf;
	}

	/**
	 * Get the matched job type given a
	 *
	 * @param data
	 *            The HadoopApplicationData to check
	 * @return The matched job type
	 */
	public JobType matchJobType(HadoopApplicationData data) {
		if (data != null) {
			List<JobType> jobTypeList = _appTypeToJobTypes.get(data.getApplicationType());
			Properties jobProp = data.getConf();
			for (JobType type : jobTypeList) {
				if (type.matchType(jobProp)) {
					return type;
				}
			}
		}
		return null;
	}

	public Map<ApplicationType, List<JobType>> getAppTypeToJobTypes() {
		return ImmutableMap.copyOf(_appTypeToJobTypes);
	}

}
