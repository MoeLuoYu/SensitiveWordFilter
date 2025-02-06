package xyz.moeluoyu.sensitivewordfilter.handler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabCompleterHandler implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 一级子命令的补全
            List<String> commands = Arrays.asList("mode", "reload");
            return StringUtil.copyPartialMatches(args[0], commands, new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
            // mode 子命令下的二级子命令补全
            List<String> modes = Arrays.asList("permissive", "enforcing");
            return StringUtil.copyPartialMatches(args[1], modes, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}