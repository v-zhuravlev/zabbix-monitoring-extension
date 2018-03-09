/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.zabbix;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.util.metrics.Metric;
import com.appdynamics.extensions.util.metrics.MetricFactory;
import com.appdynamics.extensions.yml.YmlReader;
import com.appdynamics.monitors.zabbix.config.Configuration;
import com.appdynamics.monitors.zabbix.config.MetricCharacterReplacer;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZabbixMonitor extends AManagedMonitor {

    private static final Logger logger = Logger.getLogger(ZabbixMonitor.class);
    public static final String METRIC_SEPARATOR = "|";
    private static final String CONFIG_ARG = "config-file";
    private static final String FILE_NAME = "monitors/ZabbixMonitor/config.yml";

    public ZabbixMonitor() {
        printVersion(true);
    }

    private void printVersion(boolean toConsole) {
        String details = ZabbixMonitor.class.getPackage().getImplementationTitle();
        String msg = "Using Monitor Version [" + details + "]";
        logger.info(msg);
        if (toConsole) {
            System.out.println(msg);
        }
    }

    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {

        printVersion(false);

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
            String metricPath = aMetric.getMetricPath();
            List<MetricCharacterReplacer> metricCharacterReplacers = config.getMetricCharacterReplacer();

            for (MetricCharacterReplacer metricCharacterReplacer : metricCharacterReplacers) {
                String replace = metricCharacterReplacer.getReplace();
                String replaceWith = metricCharacterReplacer.getReplaceWith();

                logger.debug("Replacing " + replace + " with " + replaceWith + " in " + metricPath);

                Pattern pattern = Pattern.compile(replace);

                Matcher matcher = pattern.matcher(metricPath);
                metricPath = matcher.replaceAll(replaceWith);
            }

            logger.debug("Metric name after applying replacers " + metricPath);

            printMetric(metricPathPrefix + metricPath, aMetric.getMetricValue().toString(), aMetric.getAggregator(), aMetric.getTimeRollup(), aMetric.getClusterRollup());
        }
    }

    private void printMetric(String metricName, String metricValue, String aggType, String timeRollupType, String clusterRollupType) {
        MetricWriter metricWriter = getMetricWriter(metricName,
                aggType,
                timeRollupType,
                clusterRollupType
        );
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
        Map<String, String> itServiceStats = itServiceStatsCollector.collect(zabbixAPI, config);
        stats.putAll(itServiceStats);

        HistoryStatsCollector historyStatsCollector = new HistoryStatsCollector();
        Map<String, String> historyStats = historyStatsCollector.collect(zabbixAPI, config);
        stats.putAll(historyStats);

        return stats;
    }

    protected ZabbixApi createZabbixAPI(Configuration config) throws TaskExecutionException {
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
        StringBuilder sb = new StringBuilder(config.getProtocol()).append("://");
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
}