package xyz.moeluoyu.sensitivewordfilter.handler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.moeluoyu.sensitivewordfilter.Main;
import xyz.moeluoyu.sensitivewordfilter.SensitiveWordFilter;

public class CommandHandler implements CommandExecutor {
    private final SensitiveWordFilter filter;
    private final Main plugin;

    public CommandHandler(SensitiveWordFilter filter, Main plugin) {
        this.filter = filter;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 如果没有子命令，显示默认消息
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /sensitivewordfilter <mode|reload>");
            return false;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "mode":
                // 如果只有 "mode" 命令，显示当前模式和 mode 子命令的用法
                if (args.length == 1) {
                    SensitiveWordFilter.FilterMode currentMode = filter.getFilterMode();
                    sender.sendMessage(ChatColor.GREEN + "当前过滤模式: " + currentMode.toString().toLowerCase());
                    sender.sendMessage(ChatColor.YELLOW + "可用的过滤模式: \npermissive(宽容模式)\nenforcing(严格模式)");
                    return false;
                }
                // 如果有二级子命令
                if (args.length == 2) {
                    String mode = args[1].toLowerCase();
                    switch (mode) {
                        case "permissive":
                        case "enforcing":
                            try {
                                SensitiveWordFilter.FilterMode filterMode = SensitiveWordFilter.FilterMode.valueOf(mode.toUpperCase());
                                filter.setFilterMode(filterMode);
                                sender.sendMessage(ChatColor.GREEN + "过滤模式已设置为: " + filterMode.toString().toLowerCase());
                                return true;
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(ChatColor.RED + "无效的过滤模式，请输入 permissive 或 enforcing。");
                                return false;
                            }
                        default:
                            sender.sendMessage(ChatColor.RED + "参数错误，请使用 /sensitivewordfilter mode <permissive|enforcing>");
                            return false;
                    }
                } else {
                    // 如果有超过两个参数，返回参数错误
                    sender.sendMessage(ChatColor.RED + "参数错误，请使用 /sensitivewordfilter mode <permissive|enforcing>");
                    return false;
                }
            case "reload":
                // 如果只有 "reload" 命令，执行重载操作
                if (args.length == 1) {
                    filter.reloadWords();
                    sender.sendMessage(ChatColor.GREEN + "成功重新加载敏感词和专有名词文件");
                    return true;
                } else {
                    // 如果有多余参数，返回参数错误
                    sender.sendMessage(ChatColor.RED + "参数错误，请使用 /sensitivewordfilter reload");
                    return false;
                }
            default:
                // 未知的一级子命令，显示默认消息
                sender.sendMessage(ChatColor.RED + "未知的子命令，请使用 /sensitivewordfilter <mode|reload>");
                return false;
        }
    }
}
