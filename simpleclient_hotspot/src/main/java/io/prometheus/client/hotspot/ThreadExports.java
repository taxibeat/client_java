package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exports metrics about JVM thread areas.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new ThreadExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_threads_current{} 300
 *   jvm_threads_daemon{} 200
 *   jvm_threads_peak{} 410
 *   jvm_threads_started_total{} 1200
 * </pre>
 */
public class ThreadExports extends Collector {
  private final ThreadMXBean threadBean;

  public ThreadExports() {
    this(ManagementFactory.getThreadMXBean());
  }

  public ThreadExports(ThreadMXBean threadBean) {
    this.threadBean = threadBean;
  }

  void addThreadMetrics(List<MetricFamilySamples> sampleFamilies) {
    String applicationId = DefaultExports.getAppId();
    String applicationName = DefaultExports.getAppName();
    GaugeMetricFamily gmf;
    CounterMetricFamily cmf;

    /* Current JVM threads */
    int jvmThreadsCurrent = threadBean.getThreadCount();
    gmf = new GaugeMetricFamily(
            "jvm_threads_current",
            "Current thread count of a JVM", Arrays.asList("app_id", "application_name"));
    gmf.addMetric(Arrays.asList(applicationId, applicationName), jvmThreadsCurrent);
    sampleFamilies.add(gmf);

    /* Daemon thread count */
    int daemonThreadCount = threadBean.getDaemonThreadCount();
    gmf = new GaugeMetricFamily(
            "jvm_threads_daemon",
            "Daemon thread count of a JVM", Arrays.asList("app_id", "application_name"));
    gmf.addMetric(Arrays.asList(applicationId, applicationName), daemonThreadCount);
    sampleFamilies.add(gmf);

    /* Peak thread count */
    int peakThreadCount = threadBean.getPeakThreadCount();
    gmf = new GaugeMetricFamily(
            "jvm_threads_peak",
            "Peak thread count of a JVM", Arrays.asList("app_id", "application_name"));
    gmf.addMetric(Arrays.asList(applicationId, applicationName), peakThreadCount);
    sampleFamilies.add(gmf);

    /* Started thread count */
    long totalStartedThreadCount = threadBean.getTotalStartedThreadCount();
    cmf = new CounterMetricFamily(
            "jvm_threads_started_total",
            "Started thread count of a JVM", Arrays.asList("app_id", "application_name"));
    cmf.addMetric(Arrays.asList(applicationId, applicationName), totalStartedThreadCount);
    sampleFamilies.add(cmf);

    /* Deadlocked threads waiting to acquire object monitors or ownable synchronizers count */
    double findDeadlockedThreads = nullSafeArrayLength(threadBean.findDeadlockedThreads());
    gmf = new GaugeMetricFamily(
            "jvm_threads_deadlocked",
            "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers",
            Arrays.asList("app_id", "application_name"));
    gmf.addMetric(Arrays.asList(applicationId, applicationName), findDeadlockedThreads);
    sampleFamilies.add(gmf);


    /* Deadlocked threads waiting to acquire object monitors count */
    double monitorDeadlockedThreads = nullSafeArrayLength(threadBean.findMonitorDeadlockedThreads());
    gmf = new GaugeMetricFamily(
            "jvm_threads_deadlocked_monitor",
            "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors",
            Arrays.asList("app_id", "application_name"));
    gmf.addMetric(Arrays.asList(applicationId, applicationName), monitorDeadlockedThreads);
    sampleFamilies.add(gmf);
  }

  private static double nullSafeArrayLength(long[] array) {
    return null == array ? 0 : array.length;
  }

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addThreadMetrics(mfs);
    return mfs;
  }
}
