package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.ClassLoadingMXBean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exports metrics about JVM classloading.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new ClassLoadingExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_classes_loaded{} 1000
 *   jvm_classes_loaded_total{} 2000
 *   jvm_classes_unloaded_total{} 500
 * </pre>
 */
public class ClassLoadingExports extends Collector {
  private final ClassLoadingMXBean clBean;

  public ClassLoadingExports() {
    this(ManagementFactory.getClassLoadingMXBean());
  }

  public ClassLoadingExports(ClassLoadingMXBean clBean) {
    this.clBean = clBean;
  }

  void addClassLoadingMetrics(List<MetricFamilySamples> sampleFamilies) {
    String applicationId = DefaultExports.getAppId();
    String applicationName = DefaultExports.getAppName();
    GaugeMetricFamily gmf;
    CounterMetricFamily cmf;

    /* Currently loaded class count */
    int loadedClassCount = clBean.getLoadedClassCount();
    gmf = new GaugeMetricFamily(
            "jvm_classes_loaded",
            "The number of classes that are currently loaded in the JVM",
            Arrays.asList("app_id", "application_name"));
    gmf.addMetric(Arrays.asList(applicationId, applicationName), loadedClassCount);
    sampleFamilies.add(gmf);

    /* Total loaded class count */
    long totalLoadedClassCount = clBean.getTotalLoadedClassCount();
    cmf = new CounterMetricFamily(
            "jvm_classes_loaded_total",
            "The total number of classes that have been loaded since the JVM has started execution",
            Arrays.asList("app_id", "application_name"));
    cmf.addMetric(Arrays.asList(applicationId, applicationName), totalLoadedClassCount);
    sampleFamilies.add(cmf);

    /* Unloaded class count */
    long unloadedClassCount = clBean.getUnloadedClassCount();
    cmf = new CounterMetricFamily(
            "jvm_classes_unloaded_total",
            "The total number of classes that have been unloaded since the JVM has started execution",
            Arrays.asList("app_id", "application_name"));
    cmf.addMetric(Arrays.asList(applicationId, applicationName), unloadedClassCount);
    sampleFamilies.add(cmf);
  }


  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addClassLoadingMetrics(mfs);
    return mfs;
  }
}
