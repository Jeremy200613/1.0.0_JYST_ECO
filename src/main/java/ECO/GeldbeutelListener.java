package ECO;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GeldbeutelListener implements Listener {

    private void aktualisiereGeldbeutel(Player spieler, ItemStack item) {
        if (item == null || item.getType() != Material.BUNDLE) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            double kontostand = WirtschaftsManager.getGuthaben(spieler.getUniqueId());
            meta.setDisplayName("§6§lGeldbeutel von " + spieler.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Inhalt:");
            lore.add("§e" + String.format("%.2f", kontostand) + " €");
            lore.add("§8§oHält dein Erspartes sicher.");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player spieler = (Player) event.getWhoClicked();
            aktualisiereGeldbeutel(spieler, event.getCurrentItem());
            aktualisiereGeldbeutel(spieler, event.getCursor());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player spieler = event.getPlayer();
        aktualisiereGeldbeutel(spieler, event.getItem());
    }
}
