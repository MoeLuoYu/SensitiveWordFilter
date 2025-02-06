package xyz.moeluoyu.sensitivewordfilter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensitiveWordFilter {
    private final JavaPlugin plugin;
    private List<String> sensitiveWords;
    private List<String> properNouns;
    private FilterMode filterMode;

    public SensitiveWordFilter(JavaPlugin plugin) {
        this.plugin = plugin;
        loadSensitiveWords();
        loadProperNouns();
        loadFilterMode();
    }

    private void loadSensitiveWords() {
        sensitiveWords = new ArrayList<>();
        File sensitiveWordsFile = new File(plugin.getDataFolder(), "sensitive_words.txt");

        // 检查文件是否存在，如果不存在则从资源文件中释放
        if (!sensitiveWordsFile.exists()) {
            try (InputStream inputStream = plugin.getResource("sensitive_words.txt");
                 FileOutputStream outputStream = new FileOutputStream(sensitiveWordsFile)) {
                if (inputStream == null) {
                    plugin.getLogger().severe("无法找到敏感词资源文件 sensitive_words.txt");
                    return;
                }
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                plugin.getLogger().info("成功释放敏感词文件 sensitive_words.txt");
            } catch (IOException e) {
                plugin.getLogger().severe("加载敏感词文件时发生错误: " + e.getMessage());
            }
        }

        try {
            sensitiveWords = Files.readAllLines(sensitiveWordsFile.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("读取敏感词文件时发生错误: " + e.getMessage());
        }
    }

    private void loadProperNouns() {
        properNouns = new ArrayList<>();
        File properNounsFile = new File(plugin.getDataFolder(), "proper_nouns.txt");

        // 检查文件是否存在，如果不存在则从资源文件中释放
        if (!properNounsFile.exists()) {
            try (InputStream inputStream = plugin.getResource("proper_nouns.txt");
                 FileOutputStream outputStream = new FileOutputStream(properNounsFile)) {
                if (inputStream == null) {
                    plugin.getLogger().severe("无法找到专有名词资源文件 proper_nouns.txt");
                    return;
                }
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                plugin.getLogger().info("成功释放专有名词文件 proper_nouns.txt");
            } catch (IOException e) {
                plugin.getLogger().severe("加载专有名词文件时发生错误: " + e.getMessage());
            }
        }

        try {
            properNouns = Files.readAllLines(properNounsFile.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("读取专有名词文件时发生错误: " + e.getMessage());
        }
    }

    private void loadFilterMode() {
        FileConfiguration config = plugin.getConfig();
        String mode = config.getString("filter-mode", "permissive");
        filterMode = FilterMode.valueOf(mode.toUpperCase());
    }

    public void setFilterMode(FilterMode mode) {
        this.filterMode = mode;
        FileConfiguration config = plugin.getConfig();
        config.set("filter-mode", mode.toString().toLowerCase());
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件时发生错误: " + e.getMessage());
        }
    }

    public String filter(String input) {
        // 先将专有名词替换为临时占位符
        Map<String, String> placeholderMap = new HashMap<>();
        for (int i = 0; i < properNouns.size(); i++) {
            String properNoun = properNouns.get(i);
            String placeholder = "@@PROPERNOUN" + i + "@@";
            placeholderMap.put(placeholder, properNoun);
            input = input.replace(properNoun, placeholder);
        }

        if (filterMode == FilterMode.ENFORCING) {
            for (String word : sensitiveWords) {
                if (input.contains(word)) {
                    return "***";
                }
            }
        } else {
            for (String word : sensitiveWords) {
                input = input.replace(word, repeat(word.length()));
            }
        }

        // 将临时占位符还原为专有名词
        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
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
        loadSensitiveWords();
        loadProperNouns();
        plugin.getLogger().info("成功重新加载敏感词和专有名词文件");
    }

    public enum FilterMode {
        PERMISSIVE, ENFORCING
    }
    public FilterMode getFilterMode() {
        return filterMode;
    }
}
