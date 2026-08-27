package ECO; // HIER deinen Paketnamen eintragen!

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaktion implements Listener {

    public static Map<String, Object> dictionary = new HashMap<>();
    private static final double STEUERSATZ = 0.19; // 19%

    public static void price() {
        dictionary.put("Nether", 700);
        dictionary.put("diamant", 400);
        dictionary.put("Emerald", 450);
        dictionary.put("Gold", 350);
        dictionary.put("Iron", 100);
        dictionary.put("Copper", 90);
        dictionary.put("Wood", 10);
        dictionary.put("Flowers", 2);
        dictionary.put("Stein", 20);
        dictionary.put("Coal", 15);
        dictionary.put("Lapislazuli", 200);
        dictionary.put("Redstone", 120);
        dictionary.put("Quartz", 300);
        dictionary.put("Ancient Debris", 500);
    }

    public static void setupVillagerShop(Villager villager) {
        if (dictionary.isEmpty()) {
            price();
            // wenn das Dictionary keinen eintrag zu einem Gegenstand hat wird der default price gesetzt
        }
        List<MerchantRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
            String itemName = entry.getKey();
            int preis = (Integer) entry.getValue();

            Material mat = stringToMaterial(itemName);
            if (mat == null) continue;
            // Nur fortsetzen wenn das Item/Material existiert und gültig ist.

            ItemStack inputItem = new ItemStack(mat, 1);
            ItemStack rewardItem = new ItemStack(Material.EMERALD, preis);
            //Eingabe des materials / Ausgabe von Emeralds in höhe des Preises

            ItemMeta meta = rewardItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6Preis: " + preis + "€");
                List<String> lore = new ArrayList<>();
                lore.add("§7Inklusive §e19% Minecraft Steuern§7!");
                lore.add("§7Du erhältst nach Kauf eine Rechnung.");
                meta.setLore(lore);
                rewardItem.setItemMeta(meta);
            }

            MerchantRecipe recipe = new MerchantRecipe(rewardItem, 99999);
            recipe.addIngredient(inputItem);
            recipes.add(recipe);
        }
        villager.setRecipes(recipes);
    }

    @EventHandler
    public void onPlayerTrade(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory)) return;
        if (event.getRawSlot() != 2) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        Player spieler = (Player) event.getWhoClicked();
        MerchantInventory inv = (MerchantInventory) event.getInventory();
        MerchantRecipe rezept = inv.getSelectedRecipe();
        if (rezept == null) return;

        ItemStack eingabeItem = rezept.getIngredients().get(0);
        ItemStack ergebnisItem = rezept.getResult();

        boolean istVerkaufAnVillager = ergebnisItem.getType() == Material.EMERALD;
        boolean istEinkaufVomVillager = eingabeItem.getType() == Material.EMERALD;

        if (istEinkaufVomVillager) {
            String internerName = getDictionaryKeyFromMaterial(ergebnisItem.getType());
            if (internerName != null && dictionary.containsKey(internerName)) {
                int basisPreis = (Integer) dictionary.get(internerName);
                int menge = ergebnisItem.getAmount();

                double nettoGesamt = basisPreis * menge;
                double steuerBetrag = nettoGesamt * STEUERSATZ;
                double bruttoGesamt = nettoGesamt + steuerBetrag;

                // Echtzeit-Abbuchung vom Offline-Konto
                WirtschaftsManager.guthabenAbziehen(spieler.getUniqueId(), bruttoGesamt);
                sendeEinkaufsRechnung(spieler, internerName, menge, nettoGesamt, steuerBetrag, bruttoGesamt);
            }
        } else if (istVerkaufAnVillager) {
            String internerName = getDictionaryKeyFromMaterial(eingabeItem.getType());
            if (internerName != null && dictionary.containsKey(internerName)) {
                int basisPreis = (Integer) dictionary.get(internerName);
                int menge = eingabeItem.getAmount();

                double bruttoEinnahme = basisPreis * menge;
                double steuerAbzug = bruttoEinnahme * STEUERSATZ;
                double nettoAuszahlung = bruttoEinnahme - steuerAbzug;

                // Echtzeit-Gutschrift auf das Offline-Konto
                WirtschaftsManager.guthabenHinzufuegen(spieler.getUniqueId(), nettoAuszahlung);

                spieler.sendMessage("§r");
                spieler.sendMessage("§7§m----------------------------------------");
                spieler.sendMessage("§6§l[Ankauf] §aErfolgreich verkauft!");
                spieler.sendMessage("§7Artikel: §f" + menge + "x " + internerName);
                spieler.sendMessage("§7Brutto-Wert: §e" + String.format("%.2f", bruttoEinnahme) + "€");
                spieler.sendMessage("§c- 19% MwSt. Abzug: §c" + String.format("%.2f", steuerAbzug) + "€");
                spieler.sendMessage("§aAuszahlung erhalten: §r§a§l" + String.format("%.2f", nettoAuszahlung) + "€");
                spieler.sendMessage("§7§m----------------------------------------");
            }
        }
    }

    private void sendeEinkaufsRechnung(Player spieler, String item, int menge, double netto, double steuer, double brutto) {
        spieler.sendMessage("§r");
        spieler.sendMessage("§7§m----------------------------------------");
        spieler.sendMessage("§6§lRECHNUNG §7(Minecraft Finanzamt)");
        spieler.sendMessage("§7Beleg-Typ: §fEinkauf bei Villager");
        spieler.sendMessage("§7Posten: §f" + menge + "x " + item);
        spieler.sendMessage("§7§m----------------------------------------");
        spieler.sendMessage("§7Netto-Betrag:      §f" + String.format("%.2f", netto) + "€");
        spieler.sendMessage("§7Zzgl. 19% MwSt.:    §e" + String.format("%.2f", steuer) + "€");
        spieler.sendMessage("§7§m----------------------------------------");
        spieler.sendMessage("§6§lGesamtbetrag:     §r§6§l" + String.format("%.2f", brutto) + "€");
        spieler.sendMessage("§7§oVielen Dank für Ihren Einkauf!§r");
        spieler.sendMessage("§7§m----------------------------------------");
    }

    private static Material stringToMaterial(String name) {
        switch (name.toLowerCase()) {
            case "nether": return Material.NETHERITE_INGOT;
            case "diamant": return Material.DIAMOND;
            case "emerald": return Material.EMERALD;
            case "gold": return Material.GOLD_INGOT;
            case "iron": return Material.IRON_INGOT;
            case "copper": return Material.COPPER_INGOT;
            case "wood": return Material.OAK_LOG;
            case "flowers": return Material.DANDELION;
            case "stein": return Material.STONE;
            case "coal": return Material.COAL;
            case "lapislazuli": return Material.LAPIS_LAZULI;
            case "redstone": return Material.REDSTONE;
            case "quartz": return Material.QUARTZ;
            case "ancient debris": return Material.ANCIENT_DEBRIS;
            default: return null;
        }
    }

    private static String getDictionaryKeyFromMaterial(Material material) {
        switch (material) {
            case NETHERITE_INGOT: return "Nether";
            case DIAMOND: return "diamant";
            case EMERALD: return "Emerald";
            case GOLD_INGOT: return "Gold";
            case IRON_INGOT: return "Iron";
            case COPPER_INGOT: return "Copper";
            case OAK_LOG: return "Wood";
            case DANDELION: return "Flowers";
            case STONE: return "Stein";
            case COAL: return "Coal";
            case LAPIS_LAZULI: return "Lapislazuli";
            case REDSTONE: return "Redstone";
            case QUARTZ: return "Quartz";
            case ANCIENT_DEBRIS: return "Ancient Debris";
            default: return null;
        }
    }
}
