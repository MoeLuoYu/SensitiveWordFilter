package xyz.moeluoyu.sensitivewordfilter.listener;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.moeluoyu.sensitivewordfilter.SensitiveWordFilter;

import java.util.ArrayList;
import java.util.List;

public class ListenerClass implements Listener {
    private final SensitiveWordFilter filter;

    public ListenerClass(SensitiveWordFilter filter) {
        this.filter = filter;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 获取玩家发送的原始消息
        String message = event.getMessage();
        // 使用敏感词过滤器过滤消息
        String filteredMessage = filter.filter(message);
        // 将过滤后的消息设置回事件中，以便发送给其他玩家
        event.setMessage(filteredMessage);
    }

    // 处理铁砧点击事件
    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.getInventory() instanceof AnvilInventory) {
            AnvilInventory anvil = (AnvilInventory) event.getInventory();
            // 获取铁砧中第一个物品（待命名的物品）
            ItemStack item = anvil.getItem(0);
            if (item != null) {
                // 获取铁砧的第二个槽位（名称输入）的物品（通常为新命名的物品）
                ItemStack result = anvil.getItem(2);
                if (result != null && result.hasItemMeta()) {
                    ItemMeta meta = result.getItemMeta();
                    if (meta.hasDisplayName()) {
                        String name = meta.getDisplayName();
                        String filteredName = filter.filter(name);
                        // 如果名称被修改，更新显示名称
                        if (!name.equals(filteredName)) {
                            meta.setDisplayName(filteredName);
                            result.setItemMeta(meta);
                            // 更新铁砧的第二个槽位的物品
                            anvil.setItem(2, result);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        // 获取告示牌的状态
        Sign sign = (Sign) event.getBlock().getState();
        // 遍历告示牌的每一行
        for (int i = 0; i < 4; i++) {
            // 获取当前行的文字
            String line = event.getLine(i);
            // 过滤当前行的文字
            String filteredLine = filter.filter(line);
            // 设置过滤后的文字到当前行
            event.setLine(i, filteredLine);
        }
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        BookMeta bookMeta = event.getNewBookMeta();
        List<String> pages = bookMeta.getPages(); // 获取书本的页面内容

        // 创建一个新的可变 List 来存储过滤后的页面内容
        List<String> filteredPages = new ArrayList<>();
        // 过滤书本的标题（名称）
        String originalTitle = bookMeta.getTitle();
        if (originalTitle != null) {
            String filteredTitle = filter.filter(originalTitle);
            bookMeta.setTitle(filteredTitle);
        }

        // 过滤书本的作者
        String originalAuthor = bookMeta.getAuthor();
        if (originalAuthor != null) {
            String filteredAuthor = filter.filter(originalAuthor);
            bookMeta.setAuthor(filteredAuthor);
        }

        // 遍历书本的每一页，进行敏感词过滤
        for (String pageContent : pages) {
            String filteredContent = filter.filter(pageContent);
            filteredPages.add(filteredContent);
        }

        // 更新书本内容
        bookMeta.setPages(filteredPages);
        event.setNewBookMeta(bookMeta);
    }
}
