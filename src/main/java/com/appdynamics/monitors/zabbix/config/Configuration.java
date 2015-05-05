package com.appdynamics.monitors.zabbix.config;


import com.appdynamics.extensions.util.metrics.MetricOverride;

public class Configuration {

    private String host;
    private int port;
    private String username;
    private String password;
    private String jsonRpcPath;

    private String metricPathPrefix;
    private MetricOverride[] metricOverrides;

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
}
