/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.zabbix.config;


import com.appdynamics.extensions.util.metrics.MetricOverride;

import java.util.List;

public class Configuration {

    private String protocol;
    private String host;
    private int port;
    private String username;
    private String password;
    private String jsonRpcPath;
    private List<String> includeHosts;
    private List<String> excludeHosts;
    private List<String> excludeItems;
    private List<String> includeItems;

    private List<MetricCharacterReplacer> metricCharacterReplacer;

    private int historyMetricsFromMinutes;

    private String metricPathPrefix;
    private MetricOverride[] metricOverrides;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJsonRpcPath() {
        return jsonRpcPath;
    }

    public void setJsonRpcPath(String jsonRpcPath) {
        this.jsonRpcPath = jsonRpcPath;
    }

    public String getMetricPathPrefix() {
        return metricPathPrefix;
    }

    public void setMetricPathPrefix(String metricPathPrefix) {
        this.metricPathPrefix = metricPathPrefix;
    }

    public MetricOverride[] getMetricOverrides() {
        return metricOverrides;
    }

    public void setMetricOverrides(MetricOverride[] metricOverrides) {
        this.metricOverrides = metricOverrides;
    }

    public List<String> getIncludeHosts() {
        return includeHosts;
    }

    public void setIncludeHosts(List<String> includeHosts) {
        this.includeHosts = includeHosts;
    }

    public List<String> getExcludeHosts() {
        return excludeHosts;
    }

    public void setExcludeHosts(List<String> excludeHosts) {
        this.excludeHosts = excludeHosts;
    }

    public List<String> getExcludeItems() {
        return excludeItems;
    }

    public void setExcludeItems(List<String> excludeItems) {
        this.excludeItems = excludeItems;
    }

    public List<String> getIncludeItems() {
        return includeItems;
    }

    public void setIncludeItems(List<String> includeItems) {
        this.includeItems = includeItems;
    }

    public List<MetricCharacterReplacer> getMetricCharacterReplacer() {
        return metricCharacterReplacer;
    }

    public void setMetricCharacterReplacer(List<MetricCharacterReplacer> metricCharacterReplacer) {
        this.metricCharacterReplacer = metricCharacterReplacer;
    }

    public int getHistoryMetricsFromMinutes() {
        return historyMetricsFromMinutes;
    }

    public void setHistoryMetricsFromMinutes(int historyMetricsFromMinutes) {
        this.historyMetricsFromMinutes = historyMetricsFromMinutes;
    }
}
