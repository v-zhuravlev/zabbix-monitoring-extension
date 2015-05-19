# Zabbix Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Zabbix is the ultimate enterprise-level software designed for monitoring availability and performance of IT infrastructure components. Zabbix is open source and comes at no cost.

##Prerequisite

- Zabbix java library is not in maven repo. To get it using maven we have to install the library in the local maven repo. zabbix4j 0.1 is checked in to the lib folder. Use the below maven command to install the library to local maven repo. For latest source of zabbix4j please go to https://github.com/myaaaaa-chan/Zabbix4j

mvn install:install-file -Dfile={path to zabbix4j jar} -DgroupId=com.zabbix4j -DartifactId=zabbix4j -Dversion=0.1 -Dpackaging=jar

##Installation

1. Run "mvn clean install"
2. Download and unzip the file 'target/ZabbixMonitor.zip' to \<machineagent install dir\>/monitors
3. Open <b>monitor.xml</b> and configure yml path
4. Open <b>config.yml</b> and configure the zabbix details

<b>monitor.xml</b>
~~~
<argument name="config-file" is-required="true" default-value=""monitors/ZabbixMonitor/config.yml" />
~~~

<b>config.yml</b>
~~~
# Zabbix particulars
host: "172.17.42.1"
port: 32771
username: "admin"
password: "zabbix"
jsonRpcPath: "zabbix/api_jsonrpc.php"


#prefix used to show up metrics in AppDynamics
metricPathPrefix:  "Custom Metrics|Zabbix|"
~~~

##Metrics
The following metrics are reported.

##ITService

All the IT Services configured will be shown here 

| Metric Path  |
|----------------|
| Zabbix/ITService/{IT Service Name} |

##History

All the historic stats gathered by zabbix will be shown here 

| Metric Path  |
|----------------|
| Zabbix/History/{Host ID}/Outgoing network traffic on eth0 |
| Zabbix/History/{Host ID}/Maximum number of opened files |
| Zabbix/History/{Host ID}/Total swap space |
| Zabbix/History/{Host ID}/Number of processes |
| Zabbix/History/{Host ID}/Context switches per second |
| Zabbix/History/{Host ID}/Free swap space |
| Zabbix/History/{Host ID}/Host local time |
| Zabbix/History/{Host ID}/Zabbix queue |
| Zabbix/History/{Host ID}/Free disk space on /etc/resolv.conf |
| Zabbix/History/{Host ID}/Used disk space on /etc/Zabbix/Zabbix_agentd.d |
| Zabbix/History/{Host ID}/Free disk space on /var/lib/mysql |
| Zabbix/History/{Host ID}/Used disk space on /var/lib/mysql |
| Zabbix/History/{Host ID}/System uptime |
| Zabbix/History/{Host ID}/Number of running processes |
| Zabbix/History/{Host ID}/Used disk space on /etc/hostname |
| Zabbix/History/{Host ID}/Total memory |
| Zabbix/History/{Host ID}/Free disk space on /etc/Zabbix/|Zabbix_agentd.d |
| Zabbix/History/{Host ID}/Used disk space on /usr/lib/Zabbix/externalscripts |
| Zabbix/History/{Host ID}/Free disk space on /usr/lib/Zabbix/alertscripts |
| Zabbix/History/{Host ID}/Incoming network traffic on eth0 |
| Zabbix/History/{Host ID}/Checksum of /etc/passwd |
| Zabbix/History/{Host ID}/Free disk space on /etc/hostname |
| Zabbix/History/{Host ID}/Number of logged in users |
| Zabbix/History/{Host ID}/Agent ping |
| Zabbix/History/{Host ID}/Used disk space on /etc/hosts |
| Zabbix/History/{Host ID}/Zabbix queue over 10m |
| Zabbix/History/{Host ID}/Free disk space on /usr/lib/Zabbix/externalscripts |
| Zabbix/History/{Host ID}/Host boot time |
| Zabbix/History/{Host ID}/Used disk space on /etc/resolv.conf |
| Zabbix/History/{Host ID}/Interrupts per second |
| Zabbix/History/{Host ID}/Free disk space on /etc/hosts |
| Zabbix/History/{Host ID}/Used disk space on /usr/lib/Zabbix/alertscripts |
| Zabbix/History/{Host ID}/Available memory |
| Zabbix/History/{Host ID}/Maximum number of processes |
 

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere]() community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:help@appdynamics.com).
