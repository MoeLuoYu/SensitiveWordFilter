package xyz.moeluoyu.sensitivewordfilter;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.moeluoyu.sensitivewordfilter.handler.CommandHandler;
import xyz.moeluoyu.sensitivewordfilter.handler.TabCompleterHandler;
import xyz.moeluoyu.sensitivewordfilter.listener.ListenerClass;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        SensitiveWordFilter filter = new SensitiveWordFilter(this);
        getLogger().info("SensitiveWordFilter 已启用");
        getLogger().info("定制插件找落雨，买插件上速德优，速德优（北京）网络科技有限公司出品，落雨QQ：1498640871");
        getServer().getPluginManager().registerEvents(new ListenerClass(filter), this);
        getCommand("sensitivewordfilter").setExecutor(new CommandHandler(filter, this));
        getCommand("sensitivewordfilter").setTabCompleter(new TabCompleterHandler());
    }
    @Override
    public void onDisable() {
        getLogger().info("SensitiveWordFilter 已禁用");
    }
}