package xyz.moeluoyu.sensitivewordfilter;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.moeluoyu.sensitivewordfilter.handler.CommandHandler;
import xyz.moeluoyu.sensitivewordfilter.handler.TabCompleterHandler;
import xyz.moeluoyu.sensitivewordfilter.listener.ListenerClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensitiveWordFilter extends JavaPlugin {
    private List<String> sensitiveWords;
    private List<String> properNouns;
    private FilterMode filterMode;
    private boolean debugMode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("SensitiveWordFilter 已启用");
        getLogger().info("定制插件找落雨，买插件上速德优，速德优（北京）网络科技有限公司出品，落雨QQ：1498640871");
        
        // 初始化敏感词过滤器
        loadSensitiveWords();
        loadProperNouns();
        loadFilterMode();
        loadDebugMode();
        
        // 注册事件监听器和命令处理器
        getServer().getPluginManager().registerEvents(new ListenerClass(this), this);
        getCommand("sensitivewordfilter").setExecutor(new CommandHandler(this, this));
        getCommand("sensitivewordfilter").setTabCompleter(new TabCompleterHandler());
    }
    
    @Override
    public void onDisable() {
        getLogger().info("SensitiveWordFilter 已禁用");
    }

    private void loadSensitiveWords() {
        sensitiveWords = new ArrayList<>();
        File sensitiveWordsFile = new File(getDataFolder(), "sensitive_words.txt");

        // 检查文件是否存在，如果不存在则从资源文件中释放
        if (!sensitiveWordsFile.exists()) {
            try (InputStream inputStream = getResource("sensitive_words.txt");
                 FileOutputStream outputStream = new FileOutputStream(sensitiveWordsFile)) {
                if (inputStream == null) {
                    getLogger().severe("无法找到敏感词资源文件 sensitive_words.txt");
                    return;
                }
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                getLogger().info("成功释放敏感词文件 sensitive_words.txt");
            } catch (IOException e) {
                getLogger().severe("加载敏感词文件时发生错误: " + e.getMessage());
            }
        }

        try {
            sensitiveWords = Files.readAllLines(sensitiveWordsFile.toPath());
        } catch (IOException e) {
            getLogger().severe("读取敏感词文件时发生错误: " + e.getMessage());
        }
    }

    private void loadProperNouns() {
        properNouns = new ArrayList<>();
        File properNounsFile = new File(getDataFolder(), "proper_nouns.txt");

        // 检查文件是否存在，如果不存在则从资源文件中释放
        if (!properNounsFile.exists()) {
            try (InputStream inputStream = getResource("proper_nouns.txt");
                 FileOutputStream outputStream = new FileOutputStream(properNounsFile)) {
                if (inputStream == null) {
                    getLogger().severe("无法找到专有名词资源文件 proper_nouns.txt");
                    return;
                }
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                getLogger().info("成功释放专有名词文件 proper_nouns.txt");
            } catch (IOException e) {
                getLogger().severe("加载专有名词文件时发生错误: " + e.getMessage());
            }
        }

        try {
            properNouns = Files.readAllLines(properNounsFile.toPath());
        } catch (IOException e) {
            getLogger().severe("读取专有名词文件时发生错误: " + e.getMessage());
        }
    }

    private void loadFilterMode() {
        FileConfiguration config = getConfig();
        String mode = config.getString("filter-mode", "permissive");
        filterMode = FilterMode.valueOf(mode.toUpperCase());
    }

    private void loadDebugMode() {
        FileConfiguration config = getConfig();
        debugMode = config.getBoolean("debug-mode", false);
    }

    public void setFilterMode(FilterMode mode) {
        this.filterMode = mode;
        FileConfiguration config = getConfig();
        config.set("filter-mode", mode.toString().toLowerCase());
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().severe("保存配置文件时发生错误: " + e.getMessage());
        }
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        FileConfiguration config = getConfig();
        config.set("debug-mode", debugMode);
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().severe("保存配置文件时发生错误: " + e.getMessage());
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * 自定义权限检查方法，用于检查玩家是否有豁免权限
     * 不依赖Bukkit的权限系统，而是使用配置文件中的豁免列表
     * @param sender 要检查的命令发送者
     * @return 如果有豁免权限返回true，否则返回false
     */
    private boolean hasExemptPermission(CommandSender sender) {
        // 如果发送者是控制台，则豁免
        if (!(sender instanceof Player)) {
            return true;
        }
        
        // 从配置文件中获取豁免列表
        FileConfiguration config = getConfig();
        List<String> exemptList = config.getStringList("exempt-players");
        
        // 检查玩家是否在豁免列表中
        return exemptList.contains(sender.getName());
    }
    
    /**
     * 获取豁免玩家列表
     * @return 豁免玩家列表
     */
    public List<String> getExemptPlayers() {
        FileConfiguration config = getConfig();
        return new ArrayList<>(config.getStringList("exempt-players"));
    }
    
    /**
     * 添加玩家到豁免列表
     * @param playerName 玩家名
     * @return 如果成功添加返回true，如果玩家已在列表中返回false
     */
    public boolean addExemptPlayer(String playerName) {
        FileConfiguration config = getConfig();
        List<String> exemptList = new ArrayList<>(config.getStringList("exempt-players"));
        
        if (exemptList.contains(playerName)) {
            return false;
        }
        
        exemptList.add(playerName);
        config.set("exempt-players", exemptList);
        
        try {
            config.save(new File(getDataFolder(), "config.yml"));
            return true;
        } catch (IOException e) {
            getLogger().severe("保存配置文件时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从豁免列表中移除玩家
     * @param playerName 玩家名
     * @return 如果成功移除返回true，如果玩家不在列表中返回false
     */
    public boolean removeExemptPlayer(String playerName) {
        FileConfiguration config = getConfig();
        List<String> exemptList = new ArrayList<>(config.getStringList("exempt-players"));
        
        if (!exemptList.contains(playerName)) {
            return false;
        }
        
        exemptList.remove(playerName);
        config.set("exempt-players", exemptList);
        
        try {
            config.save(new File(getDataFolder(), "config.yml"));
            return true;
        } catch (IOException e) {
            getLogger().severe("保存配置文件时发生错误: " + e.getMessage());
            return false;
        }
    }

    public String filter(CommandSender sender, String input) {
        // Debug: 记录原始输入
        if (debugMode) {
            getLogger().info("[DEBUG] 过滤器被调用，发送者: " + (sender != null ? sender.getName() : "null") + ", 原始输入: " + input);
        }

        // 检查玩家是否有豁免权限 - 使用自定义检查，不依赖Bukkit权限系统
        if (sender != null && hasExemptPermission(sender)) {
            if (debugMode) {
                getLogger().info("[DEBUG] 发送者有豁免权限，返回原始输入");
            }
            return input;
        }

        String originalInput = input;

        // 先将专有名词替换为临时占位符
        Map<String, String> placeholderMap = new HashMap<>();
        for (int i = 0; i < properNouns.size(); i++) {
            String properNoun = properNouns.get(i);
            String placeholder = "@@PROPERNOUN" + i + "@@";
            placeholderMap.put(placeholder, properNoun);
            input = input.replace(properNoun, placeholder);
        }

        if (debugMode && !placeholderMap.isEmpty()) {
            getLogger().info("[DEBUG] 已替换 " + placeholderMap.size() + " 个专有名词为临时占位符");
        }

        boolean filtered = false;
        if (filterMode == FilterMode.ENFORCING) {
            for (String word : sensitiveWords) {
                if (input.contains(word)) {
                    if (debugMode) {
                        getLogger().info("[DEBUG] 严格模式: 检测到敏感词 '" + word + "'，返回 ***");
                    }
                    return "***";
                }
            }
        } else {
            for (String word : sensitiveWords) {
                if (input.contains(word)) {
                    if (debugMode) {
                        getLogger().info("[DEBUG] 宽容模式: 检测到敏感词 '" + word + "'，替换为 " + repeat(word.length()));
                    }
                    input = input.replace(word, repeat(word.length()));
                    filtered = true;
                }
            }
        }

        // 将临时占位符还原为专有名词
        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }

        if (debugMode) {
            if (filtered || !input.equals(originalInput)) {
                getLogger().info("[DEBUG] 过滤完成，原始内容: " + originalInput + ", 过滤后内容: " + input);
            } else {
                getLogger().info("[DEBUG] 未检测到敏感词，返回原始内容");
            }
        }

        return input;
    }

    // 自定义的 repeat 方法，用于在 Java 8 中重复字符串
    private String repeat(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append("*");
        }
        return result.toString();
    }

    public void reloadWords() {
        // 重新加载配置文件
        reloadConfig();
        
        // 重新加载所有配置项
        loadFilterMode();
        loadDebugMode();
        
        // 重新加载敏感词和专有名词文件
        loadSensitiveWords();
        loadProperNouns();
        
        getLogger().info("成功重新加载配置文件、敏感词和专有名词文件");
        if (debugMode) {
            getLogger().info("[DEBUG] Debug模式已启用");
            getLogger().info("[DEBUG] 当前过滤模式: " + filterMode.toString().toLowerCase());
            getLogger().info("[DEBUG] 豁免玩家列表: " + getConfig().getStringList("exempt-players"));
        }
    }

    public enum FilterMode {
        PERMISSIVE, ENFORCING
    }
    public FilterMode getFilterMode() {
        return filterMode;
    }
}