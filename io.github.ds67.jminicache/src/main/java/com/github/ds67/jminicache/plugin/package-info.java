/**
 * This package contains a plugin definition and available plugins.
 * 
 * Plugins implements the {@link com.github.ds67.jminicache.plugin.Plugin} interface and are called synchronously in the cache context.
 * That means that the cache is read or write locked when the call occured. Therefore plugins should be fast to not block the cache for too long.
 * Thats why plugins are not exported from the module. However, the {@link com.github.ds67.jminicache.plugin.AsynchronousSubscriberPlugin} offers the
 * possibility to add subscribers to get(set and removal operations. The publish is asynchronous to all subscribers and will not blcok the cache itself. 
 * 
 * @see com.github.ds67.jminicache.plugin.Plugin for the plugin interface
 * @see com.github.ds67.jminicache.plugin.PluginManager as a class to collect and trigger several plugins
 * @see com.github.ds67.jminicache.plugin.AsynchronousSubscriberPlugin for a {@link java.util.concurrent.Flow.Subscriber} interface
 * @see com.github.ds67.jminicache.plugin.StatisticsPlugin for the plugin which collects access statistics
 * @see com.github.ds67.jminicache.plugin.TimingPlugin for a plugin which measured timings for get, set and removal operations
 * 
 */
package com.github.ds67.jminicache.plugin;