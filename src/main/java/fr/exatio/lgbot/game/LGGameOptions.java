package fr.exatio.lgbot.game;

import java.util.ArrayList;
import java.util.HashMap;

public class LGGameOptions {

    private final ArrayList<String> players;
    private final HashMap<Roles, Integer> rolesCount;
    private final ArrayList<String> rolesID;

    public LGGameOptions() {
        this.players = new ArrayList<>();
        this.rolesCount = new HashMap<>();
        this.rolesID = new ArrayList<>();
    }

    public void addPlayer(String player) {
        this.players.add(player);
    }

    public void addRole(Roles role) {
        if(rolesCount.containsKey(role)) {
            rolesCount.replace(role, rolesCount.get(role) + 1);
        } else {
            rolesCount.put(role, 1);
        }
    }

    public void removeRole(String roleName) {
        Roles role = Roles.getByName(roleName);
        if(rolesCount.get(role) == 1) {
            rolesCount.remove(role);
        } else {
            rolesCount.replace(role, rolesCount.get(role) - 1);
        }
    }

    public ArrayList<String> getPlayers() {
        return this.players;
    }

    public HashMap<Roles, Integer> getRolesCount() {
        return this.rolesCount;
    }

    public int getSumRoles() {
        int i = 0;
        for(int integer : this.rolesCount.values()) {
            i += integer;
        }
        return i;
    }

    public ArrayList<Roles> getRoles() {

        ArrayList<Roles> al = new ArrayList<>();
        for(int i = 0 ; i < rolesCount.size() ; i++) {
            Roles role = (Roles) rolesCount.keySet().toArray()[i];
            for (int j = 0 ; j < rolesCount.get(role) ; j++) {
                al.add(role);
            }
        }

        return al;

    }

}
