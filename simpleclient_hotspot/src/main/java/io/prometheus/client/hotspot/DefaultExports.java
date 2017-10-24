package io.prometheus.client.hotspot;

import java.util.ArrayList;

/**
 * Registers the default Hotspot collectors.
 * <p>
 * This is intended to avoid users having to add in new
 * registrations every time a new exporter is added.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   DefaultExports.initialize();
 * }
 * </pre>
 */
public class DefaultExports {
  private static boolean initialized = false;
  private static String appId = "not-set";
  private static String appName = "not-set";
  /**
   * Register the default Hotspot collectors.
   */
  public static synchronized void initialize() {
    if (!initialized) {
      new StandardExports().register();
      new MemoryPoolsExports().register();
      new GarbageCollectorExports().register();
      new ThreadExports().register();
      new ClassLoadingExports().register();
      new VersionInfoExports().register();
      initialized = true;
    }


  }

  public static String getAppId() {return appId;}
  public static String getAppName() {return appName;}
  public static void setAppId(String id) {appId = id;}
  public static void setAppName(String name) {appName = name;}

}