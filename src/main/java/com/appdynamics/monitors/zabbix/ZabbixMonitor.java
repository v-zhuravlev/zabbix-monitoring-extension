package com.appdynamics.monitors.zabbix;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.util.metrics.Metric;
import com.appdynamics.extensions.util.metrics.MetricFactory;
import com.appdynamics.extensions.yml.YmlReader;
import com.appdynamics.monitors.zabbix.config.Configuration;
import com.appdynamics.monitors.zabbix.statsCollector.HistoryStatsCollector;
import com.appdynamics.monitors.zabbix.statsCollector.ITServiceStatsCollector;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import com.zabbix4j.ZabbixApi;
import com.zabbix4j.ZabbixApiException;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZabbixMonitor extends AManagedMonitor {

    private static final Logger logger = Logger.getLogger(ZabbixMonitor.class);

    public static final String METRIC_SEPARATOR = "|";
    private static final String CONFIG_ARG = "config-file";
    private static final String FILE_NAME = "monitors/ZabbixMonitor/config.yml";

    public ZabbixMonitor() {
        String details = ZabbixMonitor.class.getPackage().getImplementationTitle();
        String msg = "Using Monitor Version [" + details + "]";
        logger.info(msg);
        System.out.println(msg);
    }

    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        if (taskArgs != null) {
            logger.info("Starting the Zabbix Monitoring task.");
            String configFilename = getConfigFilename(taskArgs.get(CONFIG_ARG));
            try {
                Configuration config = YmlReader.readFromFile(configFilename, Configuration.class);
                Map<String, String> metrics = populateStats(config);
                //metric overrides
                MetricFactory<String> metricFactory = new MetricFactory<String>(config.getMetricOverrides());
                List<Metric> allMetrics = metricFactory.process(metrics);
                printStats(config, allMetrics);
                logger.info("Completed the Zabbix Monitoring Task successfully");
                return new TaskOutput("Zabbix Monitor executed successfully");
            } catch (Exception e) {
                logger.error("Metrics Collection Failed: ", e);
            }
        }
        throw new TaskExecutionException("Zabbix Monitor completed with failures");
    }

    private void printStats(Configuration config, List<Metric> metrics) {
        String metricPathPrefix = config.getMetricPathPrefix();
        for (Metric aMetric : metrics) {
            printMetric(metricPathPrefix + aMetric.getMetricPath(), aMetric.getMetricValue().toString(), aMetric.getAggregator(), aMetric.getTimeRollup(), aMetric.getClusterRollup());
        }
    }


    private void printMetric(String metricName, String metricValue, String aggType, String timeRollupType, String clusterRollupType) {
        MetricWriter metricWriter = getMetricWriter(metricName,
                aggType,
                timeRollupType,
                clusterRollupType
        );
        //System.out.println("Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
        //        + "] metric = " + metricName + " = " + metricValue);
        if (logger.isDebugEnabled()) {
            logger.debug("Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricName + " = " + metricValue);
        }
        metricWriter.printMetric(metricValue);
    }


    private Map<String, String> populateStats(Configuration config) throws TaskExecutionException {
        ZabbixApi zabbixAPI = createZabbixAPI(config);

        Map<String, String> stats = new HashMap<String, String>();

        ITServiceStatsCollector itServiceStatsCollector = new ITServiceStatsCollector();
        Map<String, String> itServiceStats = itServiceStatsCollector.collect(zabbixAPI);
        stats.putAll(itServiceStats);

        HistoryStatsCollector historyStatsCollector = new HistoryStatsCollector();
        Map<String, String> historyStats = historyStatsCollector.collect(zabbixAPI);
        stats.putAll(historyStats);

        return stats;
    }

    private ZabbixApi createZabbixAPI(Configuration config) throws TaskExecutionException {
        String url = buildZabbixURL(config);
        ZabbixApi zabbixApi = new ZabbixApi(url);
        try {
            zabbixApi.login(config.getUsername(), config.getPassword());
            return zabbixApi;
        } catch (ZabbixApiException e) {
            logger.error("Error while connecting to Zabbix", e);
            throw new TaskExecutionException("Error while connecting to Zabbix", e);
        }
    }

    private String buildZabbixURL(Configuration config) {
        StringBuilder sb = new StringBuilder("http://");
        sb.append(config.getHost()).append(":").append(config.getPort()).append("/").append(config.getJsonRpcPath());
        return sb.toString();
    }

    private String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }

        if ("".equals(filename)) {
            filename = FILE_NAME;
        }
        // for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        // for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    public static void main(String[] args) throws TaskExecutionException {
        ZabbixMonitor zabbixMonitor = new ZabbixMonitor();
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put(CONFIG_ARG, "/home/satish/AppDynamics/Code/extensions/zabbix-monitoring-extension/src/main/resources/config/config.yml");
        zabbixMonitor.execute(taskArgs, null);

    }
}
