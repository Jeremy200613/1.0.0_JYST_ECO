package ECO; // HIER deinen Paketnamen eintragen!

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WirtschaftsManager implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final File datei;
    private final FileConfiguration konfiguration;
    private static final Map<UUID, Double> konten = new HashMap<>();

    public WirtschaftsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.datei = new File(plugin.getDataFolder(), "speicher.yaml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.konfiguration = YamlConfiguration.loadConfiguration(datei);
        kontenLaden();
    }

    public static double getGuthaben(UUID uuid) {
        return konten.getOrDefault(uuid, 100.0); // 100.0€ Startguthaben
    }

    public static void setGuthaben(UUID uuid, double betrag) {
        konten.put(uuid, Math.max(0.0, betrag));
    }

    public static void guthabenHinzufuegen(UUID uuid, double betrag) {
        setGuthaben(uuid, getGuthaben(uuid) + betrag);
    }

    public static void guthabenAbziehen(UUID uuid, double betrag) {
        setGuthaben(uuid, getGuthaben(uuid) - betrag);
    }

    public void kontenLaden() {
        if (konfiguration.getConfigurationSection("konten") == null) return;
        for (String uuidString : konfiguration.getConfigurationSection("konten").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);
            double betrag = konfiguration.getDouble("konten." + uuidString);
            konten.put(uuid, betrag);
        }
    }

    public void kontenSpeichern() {
        for (Map.Entry<UUID, Double> eintrag : konten.entrySet()) {
            konfiguration.set("konten." + eintrag.getKey().toString(), eintrag.getValue());
        }
        try {
            konfiguration.save(datei);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte speicher.yaml nicht speichern: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("balance")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cDieser Befehl kann nur von Spielern genutzt werden.");
                    return true;
                }
                Player spieler = (Player) sender;
                sender.sendMessage("§6[Konto] §7Dein Guthaben beträgt: §e" + String.format("%.2f", getGuthaben(spieler.getUniqueId())) + "€");
                return true;
            }
            if (args.length == 1) {
                Player ziel = Bukkit.getPlayer(args[0]);
                if (ziel == null) {
                    sender.sendMessage("§cDer Spieler ist zurzeit offline.");
                    return true;
                }
                sender.sendMessage("§6[Konto] §7Guthaben von §f" + ziel.getName() + "§7: §e" + String.format("%.2f", getGuthaben(ziel.getUniqueId())) + "€");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("pay")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cNur Spieler können Überweisungen tätigen.");
                return true;
            }
            Player spieler = (Player) sender;
            if (args.length < 2) {
                spieler.sendMessage("§cVerwendung: /pay [Spieler] [Betrag]");
                return true;
            }
            Player ziel = Bukkit.getPlayer(args[0]);
            if (ziel == null) {
                spieler.sendMessage("§cDieser Spieler ist offline.");
                return true;
            }
            if (ziel.getUniqueId().equals(spieler.getUniqueId())) {
                spieler.sendMessage("§cDu kannst dir nicht selbst Geld senden.");
                return true;
            }
            try {
                double betrag = Double.parseDouble(args[1]);
                if (betrag <= 0) {
                    spieler.sendMessage("§cDer Betrag muss über 0€ liegen.");
                    return true;
                }
                if (getGuthaben(spieler.getUniqueId()) < betrag) {
                    spieler.sendMessage("§cDu hast nicht genug Geld auf dem Konto!");
                    return true;
                }
                guthabenAbziehen(spieler.getUniqueId(), betrag);
                guthabenHinzufuegen(ziel.getUniqueId(), betrag);
                spieler.sendMessage("§aDu hast §f" + ziel.getName() + " §e" + String.format("%.2f", betrag) + "€ §agesendet.");
                ziel.sendMessage("§aDu hast von §f" + spieler.getName() + " §e" + String.format("%.2f", betrag) + "€ §aerhalten.");
            } catch (NumberFormatException e) {
                spieler.sendMessage("§cBitte gib eine gültige Zahl als Betrag an.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("eco")) {
            if (!sender.hasPermission("system.admin.economy")) {
                sender.sendMessage("§cKeine Berechtigung.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§cVerwendung: /eco [give/take/set] [Spieler] [Betrag]");
                return true;
            }
            String aktion = args[0].toLowerCase();
            Player ziel = Bukkit.getPlayer(args[1]);
            if (ziel == null) {
                sender.sendMessage("§cDieser Spieler ist offline.");
                return true;
            }
            try {
                double betrag = Double.parseDouble(args[2]);
                if (aktion.equals("give")) {
                    guthabenHinzufuegen(ziel.getUniqueId(), betrag);
                    sender.sendMessage("§6[Admin] §f" + ziel.getName() + " §a+" + betrag + "€ §7gegeben.");
                } else if (aktion.equals("take")) {
                    guthabenAbziehen(ziel.getUniqueId(), betrag);
                    sender.sendMessage("§6[Admin] §f" + ziel.getName() + " §c-" + betrag + "€ §7abgezogen.");
                } else if (aktion.equals("set")) {
                    setGuthaben(ziel.getUniqueId(), betrag);
                    sender.sendMessage("§6[Admin] §f" + ziel.getName() + "§7s Konto auf §e" + betrag + "€ §7gesetzt.");
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cBitte gib eine gültige Zahl an.");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> vorschlaege = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("pay") || command.getName().equalsIgnoreCase("balance")) {
            if (args.length == 1) {
                String eingabe = args[0].toLowerCase();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(eingabe)) vorschlaege.add(p.getName());
                }
            }
        }
        if (command.getName().equalsIgnoreCase("eco")) {
            if (args.length == 1) {
                String eingabe = args[0].toLowerCase();
                if ("give".startsWith(eingabe)) vorschlaege.add("give");
                if ("take".startsWith(eingabe)) vorschlaege.add("take");
                if ("set".startsWith(eingabe)) vorschlaege.add("set");
            } else if (args.length == 2) {
                String eingabe = args[1].toLowerCase();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(eingabe)) vorschlaege.add(p.getName());
                }
            }
        }
        return vorschlaege;
    }
}
