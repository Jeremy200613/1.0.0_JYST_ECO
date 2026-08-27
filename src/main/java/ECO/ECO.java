package ECO; // HIER deinen Paketnamen eintragen!

import org.bukkit.plugin.java.JavaPlugin;

public class ECO extends JavaPlugin {

    private WirtschaftsManager wirtschaftsManager;

    @Override
    public void onEnable() {
        // Preise für das Verkaufs-Dictionary laden
        Transaktion.price();

        // initialisieren (erstellt und lädt die speicher.yaml)
        this.wirtschaftsManager = new WirtschaftsManager(this);
        getServer().getPluginManager().registerEvents(new Transaktion(), this);
        getServer().getPluginManager().registerEvents(new GeldbeutelListener(), this);

        // Befehle und Tab-Vervollständigung registrieren
        if (getCommand("balance") != null) {
            getCommand("balance").setExecutor(wirtschaftsManager);
            getCommand("balance").setTabCompleter(wirtschaftsManager);
        }
        if (getCommand("pay") != null) {
            getCommand("pay").setExecutor(wirtschaftsManager);
            getCommand("pay").setTabCompleter(wirtschaftsManager);
        }
        if (getCommand("eco") != null) {
            getCommand("eco").setExecutor(wirtschaftsManager);
            getCommand("eco").setTabCompleter(wirtschaftsManager);
        }



        if(getCommand("shop") != null){
            ShopListener shop = new ShopListener();
            getCommand("shop").setExecutor(shop);
            getServer().getPluginManager()
                    .registerEvents(shop, this);
        }


        ShopListener shop = new ShopListener();

        if (getCommand("shop") != null) {
            getCommand("shop").setExecutor(shop);
        }

        getServer().getPluginManager().registerEvents(shop, this);

        getLogger().info("Wirtschafts- und Steuersystem erfolgreich geladen!");
    }

    @Override
    public void onDisable() {
        // Sichert alle Kontostände offline auf der Festplatte
        if (wirtschaftsManager != null) {
            wirtschaftsManager.kontenSpeichern();
        }
        getLogger().info("Daten sicher in speicher.yaml gespeichert!");
    }
}
