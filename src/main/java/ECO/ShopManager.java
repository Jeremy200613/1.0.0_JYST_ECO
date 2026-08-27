package ECO;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ShopManager {

    public static final double KAUFAUFSCHLAG = 1.05;
    public static final double STEUER = 0.19;

    private static final Map<Material, Double> preise =
            new HashMap<>();

    static {
        preise.put(Material.DIAMOND, 400.0);
        preise.put(Material.EMERALD, 450.0);
        preise.put(Material.GOLD_INGOT, 350.0);
        preise.put(Material.IRON_INGOT, 100.0);
        preise.put(Material.COPPER_INGOT, 90.0);
        preise.put(Material.NETHERITE_INGOT, 700.0);
    }

    public static double getPreis(Material material) {
        if(preise.containsKey(material))
            return preise.get(material);

        return standardPreis(material);
    }
    private static double standardPreis(Material material) {

        String name = material.name();

        if(name.contains("NETHERITE"))
            return 700;

        if(name.contains("DIAMOND"))
            return 400;

        if(name.contains("EMERALD"))
            return 450;

        if(name.contains("GOLD"))
            return 350;

        if(name.contains("IRON"))
         return 100;

        if(name.contains("COPPER"))
            return 90;

        if(name.contains("REDSTONE"))
            return 120;

        if(name.contains("LAPIS"))
            return 200;

        if(name.contains("QUARTZ"))
            return 300;

        if(name.contains("LOG"))
            return 10;

        return 20;
    }

}
