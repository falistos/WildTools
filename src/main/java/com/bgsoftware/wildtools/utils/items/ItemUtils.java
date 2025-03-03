package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.objects.tools.WHarvesterTool;
import com.bgsoftware.wildtools.utils.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class ItemUtils {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location, ItemsDropper itemsDropper){
        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);
        if(location != null && !additionalItems.isEmpty()){
            Executor.sync(() -> {
                for(ItemStack additional : additionalItems.values()) {
                    if (additional != null && additional.getType() != Material.AIR) {
                        if(itemsDropper != null) {
                            itemsDropper.addDrop(additional, location);
                        }
                        else {
                            location.getWorld().dropItemNaturally(location, additional);
                        }
                    }
                }
            });
        }
    }

    public static void formatItemStack(ToolItemStack toolItemStack){
        Tool tool = toolItemStack.getTool();

        if(tool == null)
            return;

        ItemMeta meta = toolItemStack.getItemMeta();
        int usesLeft = toolItemStack.getUses();
        String ownerName = "None", ownerUUID = toolItemStack.getOwner();
        String enabled = Locale.HARVESTER_SELL_ENABLED.getMessage(), disabled = Locale.HARVESTER_SELL_DISABLED.getMessage();

        if(enabled == null) enabled = "";
        if(disabled == null) disabled = "";

        try {
            ownerName = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName();
        }catch(Exception ignored){}

        if(meta.hasDisplayName()){
            meta.setDisplayName(tool.getItemStack().getItemMeta().getDisplayName()
                    .replace("{}", usesLeft + "")
                    .replace("{owner}", ownerName)
                    .replace("{sell-mode}", toolItemStack.hasSellMode() ? enabled : disabled));
        }

        if(meta.hasLore()){
            List<String> lore = new ArrayList<>();

            for(String line : tool.getItemStack().getItemMeta().getLore())
                lore.add(line
                        .replace("{}", usesLeft + "")
                        .replace("{custom-dur}", generateBar(usesLeft, tool.getDefaultUses()))
                        .replace("{owner}", ownerName)
                        .replace("{sell-mode}", toolItemStack.hasSellMode() ? enabled : disabled));

            meta.setLore(lore);
        }

        toolItemStack.setItemMeta(meta);
    }

    public static void reduceDurability(ToolItemStack toolItemStack, Player pl, int amount) {
        Tool tool = toolItemStack.getTool();

        if(tool == null)
            return;

        if(tool.isUnbreakable() || pl.getGameMode() == GameMode.CREATIVE)
            return;

        ItemStack clonedTools = null;

        if(toolItemStack.getAmount() > 1){
            clonedTools = toolItemStack.getItem().clone();
            clonedTools.setAmount(clonedTools.getAmount() - 1);
            toolItemStack.setAmount(1);
        }

        if(tool.isUsingDurability()){
            int unbLevel = toolItemStack.getEnchantmentLevel(Enchantment.DURABILITY);

            // Durability Reduce Chance: (100/(Level+1))%
            if (unbLevel != 0) {
                int chance = new Random().nextInt(100);
                if (chance > (100 / (unbLevel + 1)))
                    return;
            }

            toolItemStack.setDurability((short) (toolItemStack.getDurability() + amount));

            if(toolItemStack.getDurability() > toolItemStack.getMaxDurability())
                plugin.getNMSAdapter().breakTool(toolItemStack, pl);
        }

        else{
            int usesLeft = toolItemStack.getUses();
            toolItemStack.setUses((usesLeft -= amount));

            if (usesLeft <= 0) {
                plugin.getNMSAdapter().breakTool(toolItemStack, pl);
            }

            //Update name and lore
            else {
                // Update durability depends on the uses
                if(tool.isUsesProgress()) {
                    float usesPercentage = (float) usesLeft / tool.getDefaultUses();
                    toolItemStack.setDurability((short) Math.round((1 - usesPercentage) * toolItemStack.getMaxDurability()));
                }

                if(toolItemStack.hasItemMeta())
                    ItemUtils.formatItemStack(toolItemStack);
            }
        }

        if(clonedTools != null)
            ItemUtils.addItem(clonedTools, pl.getInventory(), pl.getLocation(), null);
    }

    public static int getDurability(Player player, ToolItemStack toolItemStack) {
        Tool tool = toolItemStack.getTool();

        boolean unbreakable = tool != null && tool.isUnbreakable();
        boolean usingDurability = tool == null || tool.isUsingDurability();

        if(unbreakable || player.getGameMode() == GameMode.CREATIVE)
            return Integer.MAX_VALUE;

        return usingDurability ? toolItemStack.getMaxDurability() - toolItemStack.getDurability() + 1 : toolItemStack.getUses();
    }

    public static boolean isCrops(Material type){
        return WHarvesterTool.crops.contains(type.name()) && type != Material.CACTUS &&
                type != WMaterial.SUGAR_CANE.parseMaterial() && type != WMaterial.MELON.parseMaterial() &&
                type != Material.PUMPKIN && !type.name().equals("BAMBOO");
    }

    public static String generateBar(int current, int max) {
        char barChar = '▎';
        ChatColor emptyColor = ChatColor.GRAY;
        ChatColor filledColor = ChatColor.DARK_GREEN;
        int length = 20;

        ////////////////////////////////////

        double completedPercentage = Math.abs((double) current / max);

        if (completedPercentage <= 0.2) filledColor = ChatColor.DARK_RED;
        else if (completedPercentage <= 0.5) filledColor = ChatColor.GOLD;

        int completedBars = (int) Math.floor((completedPercentage * length));

        String bars = filledColor+""+current+" ";
        for (int i = 1; i < completedBars; i++)
            bars += filledColor + ""+barChar;

        for (int i = 1; i < (length - completedBars); i++)
            bars += emptyColor + ""+barChar;

        return bars;
    }

}
