package io.prometheus.client.hotspot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;

public class StandardExportsTest {
  
  private static final String[] LABEL_NAMES = {"app_id", "application_name"};
  private static final String[] LABEL_VALUES = {DefaultExports.getAppId(), DefaultExports.getAppName()};

  static class StatusReaderTest extends StandardExports.StatusReader {
    BufferedReader procSelfStatusReader() throws FileNotFoundException {
      return new BufferedReader(new StringReader("Name:   cat\nVmSize:\t5900 kB\nVmRSS:\t   360 kB\n"));
    }
  }

  // Interface with a signature equivalent to com.sun.management.OperatingSystemMXBean and
  // com.ibm.lang.management.OperatingSystemMXBean, without explicitly depending on either of
  // these classes, used to test reflection.
  interface GenericOperatingSystemMXBean extends OperatingSystemMXBean {
    public long getCommittedVirtualMemorySize();
    public long getTotalSwapSpaceSize();
    public long getFreeSwapSpaceSize();
    public long getProcessCpuTime();
    public long getFreePhysicalMemorySize();
    public long getTotalPhysicalMemorySize();
  }

  // Interface with a signature equivalent to com.sun.management.UnixOperatingSystemMXBean and
  // com.ibm.lang.management.UnixOperatingSystemMXBean, without explicitly depending on either of
  // these classes, used to test reflection.
  interface UnixOperatingSystemMXBean extends GenericOperatingSystemMXBean {
    public long getOpenFileDescriptorCount();
    public long getMaxFileDescriptorCount();
  }

  UnixOperatingSystemMXBean osBean;
  RuntimeMXBean runtimeBean;

  @Before
  public void setUp() {
    osBean = mock(UnixOperatingSystemMXBean.class);
    when(osBean.getName()).thenReturn("Linux");
    when(osBean.getProcessCpuTime()).thenReturn(123L);
    when(osBean.getOpenFileDescriptorCount()).thenReturn(10L);
    when(osBean.getMaxFileDescriptorCount()).thenReturn(20L);
    runtimeBean = mock(RuntimeMXBean.class);
    when(runtimeBean.getStartTime()).thenReturn(456L);
  }

  @Test
  public void testStandardExports() {
    CollectorRegistry registry = new CollectorRegistry();
    new StandardExports(new StatusReaderTest(), osBean, runtimeBean).register(registry);

    assertEquals(123 / 1.0E9,
        registry.getSampleValue("process_cpu_seconds_total", LABEL_NAMES, LABEL_VALUES), .0000001);
    assertEquals(10,
        registry.getSampleValue("process_open_fds", LABEL_NAMES, LABEL_VALUES), .001);
    assertEquals(20,
        registry.getSampleValue("process_max_fds", LABEL_NAMES, LABEL_VALUES), .001);
    assertEquals(456 / 1.0E3,
        registry.getSampleValue("process_start_time_seconds", LABEL_NAMES, LABEL_VALUES), .0000001);
    assertEquals(5900 * 1024,
        registry.getSampleValue("process_virtual_memory_bytes", LABEL_NAMES, LABEL_VALUES), .001);
    assertEquals(360 * 1024,
        registry.getSampleValue("process_resident_memory_bytes", LABEL_NAMES, LABEL_VALUES), .001);
  }

  @Test
  public void testNonUnixStandardExports() {
    GenericOperatingSystemMXBean genericOsBean = mock(GenericOperatingSystemMXBean.class);
    when(genericOsBean.getName()).thenReturn("Windows");
    when(genericOsBean.getProcessCpuTime()).thenReturn(123L);
    
    CollectorRegistry registry = new CollectorRegistry();
    new StandardExports(new StatusReaderTest(), genericOsBean, runtimeBean).register(registry);

    assertEquals(123 / 1.0E9,
        registry.getSampleValue("process_cpu_seconds_total", LABEL_NAMES, LABEL_VALUES), .0000001);
    assertEquals(456 / 1.0E3,
        registry.getSampleValue("process_start_time_seconds", LABEL_NAMES, LABEL_VALUES), .0000001);
  }

  @Test
  public void testBrokenProcStatusReturnsOtherStats() {
    class StatusReaderBroken extends StandardExports.StatusReader {
      BufferedReader procSelfStatusReader() throws FileNotFoundException {
        return new BufferedReader(new StringReader("Name:   cat\nVmSize:\n"));
      }
    }

    CollectorRegistry registry = new CollectorRegistry();
    new StandardExports(new StatusReaderBroken(), osBean, runtimeBean).register(registry);

    assertEquals(123 / 1.0E9,
      registry.getSampleValue("process_cpu_seconds_total", LABEL_NAMES, LABEL_VALUES), .0000001);
    assertNull(registry.getSampleValue("process_resident_memory_bytes", LABEL_NAMES, LABEL_VALUES));
  }
}
