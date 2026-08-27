package ECO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopListener
        implements CommandExecutor, Listener {

    private final String TITLE =
            "§6§lMinecraft Markt";

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args) {

        if(!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        Inventory inv =
                Bukkit.createInventory(
                        null,
                        54,
                        TITLE);

        for(Material mat : Material.values()) {

            if(!mat.isItem())
                continue;

            inv.addItem(new ItemStack(mat));
        }

        player.openInventory(inv);

        return true;
    }

    @EventHandler
    public void onClick(
            InventoryClickEvent event) {

        if(!event.getView()
                .getTitle()
                .equals(TITLE))
            return;

        event.setCancelled(true);

        if(event.getCurrentItem() == null)
            return;

        Player player =
                (Player) event.getWhoClicked();

        Material material =
                event.getCurrentItem().getType();

        double basisPreis =
                ShopManager.getPreis(material);

        if(event.isLeftClick()) {

            double kaufpreis =
                    basisPreis
                            * ShopManager.KAUFAUFSCHLAG;

            if(WirtschaftsManager.getGuthaben(
                    player.getUniqueId())
                    < kaufpreis) {

                player.sendMessage(
                        "§cNicht genug Guthaben.");

                return;
            }

            WirtschaftsManager.guthabenAbziehen(
                    player.getUniqueId(),
                    kaufpreis);

            player.getInventory()
                    .addItem(
                            new ItemStack(material));

            player.sendMessage(
                    "§aGekauft: §f"
                            + material.name()
                            + " §7für §e"
                            + String.format(
                            "%.2f",
                            kaufpreis)
                            + "€");
        }

        if(event.isRightClick()) {

            if(!player.getInventory()
                    .contains(material)) {

                player.sendMessage(
                        "§cDu besitzt dieses Item nicht.");

                return;
            }

            player.getInventory()
                    .removeItem(
                            new ItemStack(material,1));

            double steuer =
                    basisPreis
                            * ShopManager.STEUER;

            double auszahlung =
                    basisPreis
                            - steuer;

            WirtschaftsManager.guthabenHinzufuegen(
                    player.getUniqueId(),
                    auszahlung);

            player.sendMessage(
                    "§aVerkauft: §f"
                            + material.name()
                            + " §7für §e"
                            + String.format(
                            "%.2f",
                            auszahlung)
                            + "€");
        }
    }
}