package fr.exatio.lgbot.game;

import java.util.Arrays;
import java.util.List;

public enum Roles {
    
    NULL("Null", "Null", false),
    DEFAULT_VILLAGER("Villager", "Villageois", true),
    HUNTER("Hunter", "Chasseur", true),
    DEFAULT_WOLF("Wolf", "Loup", false),
    WITCH("Witch", "Sorcière", true),
    ELDER("Elder", "Ancien", true),
    SEER("Seer", "Voyante", true)
    ;

    private Roles(String identifier, String name, boolean villager) {
        this.name = name;
        this.identifier = identifier;
        this.villager = villager;
    }

    private String identifier, name;
    private boolean villager;

    public String getName() {
        return this.name;
    }
    
    public String getIdentifier() {
        return this.identifier;
    }

    public boolean isVillager() {
        return villager;
    }

    public boolean isWolf() {
        return villager ? false : this.identifier.toLowerCase().contains("wolf");
    }

    public static Roles getByName(String name) {
        List<Roles> l = Arrays.asList(Roles.values());
        Roles r = Roles.NULL;
        for(Roles role : l) {
            if(role.name.equals(name)) {
                r = role;
            }
        }
        return r;
    }
    

}
