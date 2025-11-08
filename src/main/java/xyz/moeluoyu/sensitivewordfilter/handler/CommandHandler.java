package xyz.moeluoyu.sensitivewordfilter.handler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.moeluoyu.sensitivewordfilter.SensitiveWordFilter;

public class CommandHandler implements CommandExecutor {
    private final SensitiveWordFilter filter;

    public CommandHandler(SensitiveWordFilter filter, SensitiveWordFilter plugin) {
        this.filter = filter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查发送者是否有管理员权限
        if (!sender.hasPermission("sensitivewordfilter.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        // 如果没有子命令，显示默认消息
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /sensitivewordfilter <mode|reload|debug|exempt>");
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
            case "debug":
                // 处理debug命令
                if (args.length == 1) {
                    // 显示当前debug状态
                    boolean currentDebug = filter.isDebugMode();
                    sender.sendMessage(ChatColor.GREEN + "当前Debug状态: " + (currentDebug ? "启用" : "禁用"));
                    return false;
                } else if (args.length == 2) {
                    String state = args[1].toLowerCase();
                    switch (state) {
                        case "on":
                        case "true":
                        case "enable":
                            filter.setDebugMode(true);
                            sender.sendMessage(ChatColor.GREEN + "Debug模式已启用");
                            return true;
                        case "off":
                        case "false":
                        case "disable":
                            filter.setDebugMode(false);
                            sender.sendMessage(ChatColor.GREEN + "Debug模式已禁用");
                            return true;
                        default:
                            sender.sendMessage(ChatColor.RED + "参数错误，请使用 /sensitivewordfilter debug <on|off>");
                            return false;
                    }
                } else {
                    // 如果有超过两个参数，返回参数错误
                    sender.sendMessage(ChatColor.RED + "参数错误，请使用 /sensitivewordfilter debug <on|off>");
                    return false;
                }
            case "exempt":
                // 处理豁免玩家列表命令
                if (args.length == 1) {
                    // 显示当前豁免玩家列表
                    sender.sendMessage(ChatColor.GREEN + "当前豁免玩家列表:");
                    java.util.List<String> exemptList = filter.getExemptPlayers();
                    if (exemptList.isEmpty()) {
                        sender.sendMessage(ChatColor.YELLOW + "  (空)");
                    } else {
                        for (String player : exemptList) {
                            sender.sendMessage(ChatColor.YELLOW + "  - " + player);
                        }
                    }
                    return false;
                } else if (args.length == 3) {
                    // 检查执行者是否在豁免列表中（控制台除外）
                    if (!filter.getExemptPlayers().contains(sender.getName()) && sender instanceof Player) {
                        sender.sendMessage(ChatColor.RED + "你没有权限修改豁免列表！只有豁免列表中的玩家才能执行此操作。");
                        return true;
                    }
                    
                    String action = args[1].toLowerCase();
                    String playerName = args[2];
                    
                    switch (action) {
                        case "add":
                            if (filter.addExemptPlayer(playerName)) {
                                sender.sendMessage(ChatColor.GREEN + "已将 " + playerName + " 添加到豁免列表");
                            } else {
                                sender.sendMessage(ChatColor.RED + playerName + " 已在豁免列表中");
                            }
                            return true;
                        case "remove":
                            if (filter.removeExemptPlayer(playerName)) {
                                sender.sendMessage(ChatColor.GREEN + "已将 " + playerName + " 从豁免列表中移除");
                            } else {
                                sender.sendMessage(ChatColor.RED + playerName + " 不在豁免列表中");
                            }
                            return true;
                        default:
                            sender.sendMessage(ChatColor.RED + "参数错误，请使用 /sensitivewordfilter exempt <add|remove> <玩家名>");
                            return false;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "参数错误，请使用 /sensitivewordfilter exempt 或 /sensitivewordfilter exempt <add|remove> <玩家名>");
                    return false;
                }
            default:
                // 未知的一级子命令，显示默认消息
                sender.sendMessage(ChatColor.RED + "未知的子命令，请使用 /sensitivewordfilter <mode|reload|debug|exempt>");
                return false;
        }
    }
}