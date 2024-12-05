package fr.exatio.lgbot.game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fr.exatio.lgbot.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

public class LGGame {

    public static boolean isInGame = false;
    public static LGGame currentGame;

    private static ArrayList<String> players;
    private static HashMap<Roles, List<String>> roles;

    private final LGGameOptions options;
    private TextChannel textChannel;
    private TextChannel wolfChannel;
    private TextChannel seerChannel;
    private TextChannel witchChannel;
    private TextChannel hunterChannel;

    private List<String> murderTargets;

    private int night = 0;

    private Message dayVoteMessage;
    private HashMap<String, String> dayVotes;

    private Message wolfVoteMessage;
    private HashMap<String, String> wolfVotes;
    private boolean killMostVotedByWolves;

    private boolean doSeer;
    private boolean doWitch;
    private boolean doHunter;

    public boolean hunterKillsSomeone;

    private boolean hasElderRevive;

    private boolean witchLifePotionDisabled;
    private boolean witchDeathPotionDisabled;

    public LGGame(LGGameOptions options) {
        currentGame = this;
        this.options = options;
        players = options.getPlayers();
    }

    public void startGame() {
        isInGame = true;
        generateRoles();
        Main.jda.getGuildById("1040236833738600530").createTextChannel("game-actuelle").complete();
        
        this.textChannel = Main.jda.getGuildById("1040236833738600530").getTextChannelsByName("game-actuelle", false).get(0);
        
        StringBuilder str = new StringBuilder();

        ChannelAction<TextChannel> ca = Main.jda.getGuildById("1040236833738600530").createTextChannel("loups");
        ca.addPermissionOverride(textChannel.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
        roles.forEach((role, list) -> {
            if(role.isWolf()) {
                for(String playerID : list) {
                    ca.addPermissionOverride(textChannel.getGuild().getMemberById(playerID), EnumSet.of(Permission.VIEW_CHANNEL), null);
                    str.append("<@" + playerID + "> ");
                }
            }
        });
        ca.complete();
        this.wolfChannel = Main.jda.getGuildById("1040236833738600530").getTextChannelsByName("loups", false).get(0);
        this.wolfChannel.sendMessage(str.toString() + "vous êtes loups-garous!").complete();


        if(options.getRoles().contains(Roles.SEER)) {

            StringBuilder strS = new StringBuilder();

            ChannelAction<TextChannel> caS = Main.jda.getGuildById("1040236833738600530").createTextChannel("voyante");
            caS.addPermissionOverride(textChannel.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
            roles.forEach((role, list) -> {
                if(role == Roles.SEER) {
                    for(String playerID : list) {
                        caS.addPermissionOverride(textChannel.getGuild().getMemberById(playerID), EnumSet.of(Permission.VIEW_CHANNEL), null);
                        strS.append("<@" + playerID + "> ");
                    }
                }
            });
            caS.complete();
            this.seerChannel = Main.jda.getGuildById("1040236833738600530").getTextChannelsByName("voyante", false).get(0);
            this.seerChannel.sendMessage(strS.toString() + "vous êtes voyante!").complete();
            this.doSeer = true;
        }

        if(options.getRoles().contains(Roles.WITCH)) {

            StringBuilder strW = new StringBuilder();

            ChannelAction<TextChannel> caS = Main.jda.getGuildById("1040236833738600530").createTextChannel("sorcière");
            caS.addPermissionOverride(textChannel.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
            roles.forEach((role, list) -> {
                if(role == Roles.WITCH) {
                    for(String playerID : list) {
                        caS.addPermissionOverride(textChannel.getGuild().getMemberById(playerID), EnumSet.of(Permission.VIEW_CHANNEL), null);
                        strW.append("<@" + playerID + "> ");
                    }
                }
            });
            caS.complete();
            this.witchChannel = Main.jda.getGuildById("1040236833738600530").getTextChannelsByName("sorcière", false).get(0);
            this.witchChannel.sendMessage(strW.toString() + "vous êtes sorcière!").complete();
            this.doWitch = true;
            witchLifePotionDisabled = false;
            witchDeathPotionDisabled = false;
        }

        if(options.getRoles().contains(Roles.ELDER)) {
            this.hasElderRevive = true;
            String id = this.roles.get(Roles.ELDER).get(0);
            textChannel.getGuild().getMemberById(id).getUser().openPrivateChannel()
                .flatMap(channel -> channel.sendMessage("Vous êtes ancien !"))    
                .complete();
        }

        if(options.getRoles().contains(Roles.HUNTER)) {

            StringBuilder strW = new StringBuilder();

            ChannelAction<TextChannel> caS = Main.jda.getGuildById("1040236833738600530").createTextChannel("chasseur");
            caS.addPermissionOverride(textChannel.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
            roles.forEach((role, list) -> {
                if(role == Roles.HUNTER) {
                    for(String playerID : list) {
                        caS.addPermissionOverride(textChannel.getGuild().getMemberById(playerID), EnumSet.of(Permission.VIEW_CHANNEL), null);
                        strW.append("<@" + playerID + "> ");
                    }
                }
            });
            caS.complete();
            this.hunterChannel = Main.jda.getGuildById("1040236833738600530").getTextChannelsByName("chasseur", false).get(0);
            this.hunterChannel.sendMessage(strW.toString() + "vous êtes chasseur!").complete();
            this.doHunter = true;
            hunterKillsSomeone = false;

        }

        this.textChannel.sendMessage("Salon défini comme salle de jeu loup-garou (Game commencée le " + new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss").format(new Date()) + ")").complete();
        startNight();
    }

    public void day() {
        Main.musicManager.stop();

        if(hunterKillsSomeone) {

            textChannel.sendMessage("Le chasseur est mort ce soir! Vote pour quelqu'un à tuer...")
                .setActionRows(getActionRowsOf(players, "hunter"))
                .complete();

        } else {

            checkIfGameEnded();
            if(!isInGame) return;
            dayVotes = new HashMap<>();
            textChannel.sendMessage("Initialisation des votes...")
            .map(msg -> {
                dayVoteMessage = msg;
                return msg;
            })
            .complete();
            updateDayVotes();

        }

        
    }

    public void startNight() {
        if(!isInGame) return;
        this.night += 1;
        Main.musicManager.loadAndPlay();
        this.textChannel.sendMessage("Nuit " + night + "...").complete();
        
        this.doSeer = roles.containsKey(Roles.SEER) && doSeer;
        this.doWitch = roles.containsKey(Roles.WITCH) && doSeer;
        this.murderTargets = new ArrayList<>();

        if(roles.containsKey(Roles.DEFAULT_WOLF)) {
            textChannel.sendMessage("Loups, qui souhaitez-vous dévorer cette nuit ?").complete();
            wolfVotes = new HashMap<>();
            wolfChannel.sendMessage("Initialisation des votes...")
                .map(msg -> {
                    wolfVoteMessage = msg;
                    return msg;
                })
                .complete();
            updateNightVotes();
        }

        
    }


    public void continueNight() {


        if(doSeer) {
            doSeer = false;
            textChannel.sendMessage("Nous allons désormais demander à la voyante qui elle veut observer.").complete();
            seerChannel.sendMessage("Qui voulez-vous observer cette nuit ?")
                .setActionRows(getActionRowsOf(players, "seer"))
                .complete();
        } else if(doWitch && !murderTargets.contains(roles.get(Roles.WITCH).get(0))) {
            doWitch = false;
            textChannel.sendMessage("Nous allons désormais demander à la sorcière si elle veut utiliser une de ses potions.").complete();
            if(killMostVotedByWolves) {
                this.witchChannel.sendMessage("Ce joueur est mort cette nuit: " + textChannel.getGuild().getMemberById(murderTargets.get(0)).getEffectiveName()).complete();
                this.witchChannel.sendMessage("Voulez-vous utiliser votre potion de vie ? Potion de mort ?")
                    .setActionRows(
                        ActionRow.of(
                            Button.success("LG" + night + "witchLife", "Potion de vie").withEmoji(Emoji.fromUnicode("\u2697")).withDisabled(witchLifePotionDisabled),
                            Button.danger("LG" + night + "witchDeath", "Potion de mort").withEmoji(Emoji.fromUnicode("\u2620")).withDisabled(witchDeathPotionDisabled),
                            Button.primary("LG" + night + "witchNothing", "Rien").withDisabled(witchDeathPotionDisabled)
                        )
                    ).complete();
            }

        } else {

            if(murderTargets.size() > 0) {

                ArrayList<String> roleNames = new ArrayList<>();
                for(String mt : murderTargets) {
                    
                    for(int i = 0 ; i < roles.keySet().size() ; i++) {
                        
                        if(roles.get(roles.keySet().toArray()[i]).contains(mt)) {
                            roleNames.add(((Roles)roles.keySet().toArray()[i]).getName());
                        }
                        
                    }

                }

                if(murderTargets.size() == 1) {
                    this.textChannel.sendMessage("Cette nuit... quelqu'un est mort. Il s'agit de : **" + textChannel.getGuild().getMemberById(murderTargets.get(0)).getEffectiveName() + "** (*" + roleNames.get(0) + "*)").complete();
                } else if (murderTargets.size() == 2) {
                    this.textChannel.sendMessage("Cette nuit... deux personnes sont mortes... Il s'agit de : **" + textChannel.getGuild().getMemberById(murderTargets.get(0)).getEffectiveName() + "** (*" + roleNames.get(0) + "*) et **" + textChannel.getGuild().getMemberById(murderTargets.get(1)).getEffectiveName() + "** (*" + roleNames.get(1) + "*).").complete();
                } else {
                    StringBuilder strBuilder = new StringBuilder("**" + textChannel.getGuild().getMemberById(murderTargets.get(0)).getEffectiveName() + "**");
                    for(int i = 1 ; i < murderTargets.size() ; i++) {
                        strBuilder.append(", **" + textChannel.getGuild().getMemberById(murderTargets.get(i)).getEffectiveName() + "** (*" + roleNames.get(i) + "*)");
                    }
                    this.textChannel.sendMessage("Cette nuit... un carnage s'est produit. Les morts sont : " + strBuilder.toString()).complete();


                }

                for(int i = 0 ; i < murderTargets.size() ; i++) {
                    killPlayer(murderTargets.get(i));
                    //TODO : if(amoureux (murderTargets.get(i))) { killPlayer(amoureux) ; sendMessage("Il s'est suicidé")}
                }

            } else {
                this.textChannel.sendMessage("Cette nuit... personne n'est mort!").complete();
            }

            day();
        }

    }


    public void endGame(String winners) {
        this.textChannel.sendMessage("**Partie terminée !** Gagnants : " + winners).queue();
        Main.musicManager.stop();
        isInGame = false;
        this.textChannel.delete().queueAfter(30, TimeUnit.SECONDS);
        this.wolfChannel.delete().queueAfter(30, TimeUnit.SECONDS);
        if(this.seerChannel != null)
            this.seerChannel.delete().queueAfter(30, TimeUnit.SECONDS);
        if(this.witchChannel != null)
            this.witchChannel.delete().queueAfter(30, TimeUnit.SECONDS);
    }

    public LGGameOptions getOptions() {
        return options;
    }

    private void generateRoles() {

        roles = new HashMap<>();
        players = options.getPlayers();

        HashMap<Roles, Integer> rolesCount = options.getRolesCount();
        ArrayList<String> playersIds = (ArrayList<String>) options.getPlayers().clone();
        
        for(Roles r : rolesCount.keySet()) 
        {
            List<String> playersWithTheRole = new ArrayList<>();
            while(playersWithTheRole.size() != rolesCount.get(r)) {
                int index = new Random().nextInt(playersIds.size());
                playersWithTheRole.add(playersIds.get(index));
                playersIds.remove(index);
            }
            
            roles.put(r, playersWithTheRole);
        }

    }

    private void checkIfGameEnded() {
        if(roles.size() == 1) {
            endGame(((Roles) roles.keySet().toArray()[0]).getName());
        } else {
            boolean areAllVillagers = true;
            boolean areAllWolves = true;
            
            for(Roles r : roles.keySet()) {
                if(!r.isVillager()) {
                    areAllVillagers = false;
                }
                if(!r.isWolf()) {
                    areAllWolves = false;
                }
            }
            if(areAllVillagers) endGame("Villageois");
            else if(areAllWolves) endGame("Loup-Garous");
        }
    }

    public void updateDayVotes() {

        StringBuilder stringBuilder = new StringBuilder("Il est l'heure du vote ! Les votes seront pris en compte lorsque tout le monde aura voté. Votes actuels :");
        
        dayVotes.forEach((str, str1) -> {
            String player1 = this.textChannel.getGuild().getMemberById(str).getEffectiveName();
            String player2 = this.textChannel.getGuild().getMemberById(str1).getEffectiveName();
            stringBuilder.append("\n" + player1 + " a voté pour " + player2);
        });
        

        dayVoteMessage.editMessage(stringBuilder.toString())
            .setActionRows(getActionRowsOf(players, "vote"))
            .complete();

        if (dayVotes.size() == players.size()) {
            
            HashMap<String, Integer> hashMap = new HashMap<>();
            dayVotes.forEach((voter, voted) -> {
                if(hashMap.containsKey(voted)) {
                    hashMap.replace(voted, (hashMap.get(voted)+1));
                } else {
                    hashMap.put(voted, 1);
                }
            });


            String mostVoted = (String) hashMap.keySet().toArray()[0];
            int mostVotedVotedNumber = hashMap.get(mostVoted);

            for(int i = 1 ; i < hashMap.size() ; i++) {
                if((Integer)hashMap.values().toArray()[i] > mostVotedVotedNumber) {
                    mostVoted = (String) hashMap.keySet().toArray()[i];
                    mostVotedVotedNumber = (Integer) hashMap.get(mostVoted);
                }
            }

            textChannel.sendMessage("Le village a décidé de voter pour **" + textChannel.getGuild().getMemberById(mostVoted).getEffectiveName() + "**. Il était : " + getRole(mostVoted).getName()).complete();
            
            if(getRole(mostVoted) == Roles.ELDER) {
                doWitch = false;
                doSeer = false;
                doHunter = false;
            }

            killPlayer(mostVoted);
            checkIfGameEnded();
            dayVotes = new HashMap<>();
            startNight();
        } 

    }


    public void updateNightVotes() {
        StringBuilder stringBuilder = new StringBuilder("Il est l'heure du vote ! Les votes seront pris en compte lorsque tout le monde aura voté. Votes actuels :");
        stringBuilder.append("\nVote(s) manquant(s) : " + (roles.get(Roles.DEFAULT_WOLF).size() - wolfVotes.size()));
        wolfVotes.forEach((str, str1) -> {
            String player1 = this.textChannel.getGuild().getMemberById(str).getEffectiveName();
            String player2 = this.textChannel.getGuild().getMemberById(str1).getEffectiveName();
            stringBuilder.append("\n" + player1 + " a voté pour " + player2);
        });
        

        wolfVoteMessage.editMessage(stringBuilder.toString())
            .setActionRows(getActionRowsOf(players, "voteNight"))
            .complete();

        if (wolfVotes.size() == roles.get(Roles.DEFAULT_WOLF).size()) {
            
            HashMap<String, Integer> hashMap = new HashMap<>();
            wolfVotes.forEach((voter, voted) -> {
                if(hashMap.containsKey(voted)) {
                    hashMap.replace(voted, (hashMap.get(voted)+1));
                } else {
                    hashMap.put(voted, 1);
                }
            });


            String mostVoted = (String) hashMap.keySet().toArray()[0];
            int mostVotedVotedNumber = hashMap.get(mostVoted);

            for(int i = 1 ; i < hashMap.size() ; i++) {
                if((Integer)hashMap.values().toArray()[i] > mostVotedVotedNumber) {
                    mostVoted = (String) hashMap.keySet().toArray()[i];
                    mostVotedVotedNumber = (Integer) hashMap.get(mostVoted);
                }
            }

            wolfChannel.sendMessage("Les loups ont décidé leur cible: **" + textChannel.getGuild().getMemberById(mostVoted).getEffectiveName() + "**.").queue();
            wolfVotes = new HashMap<>();
            if(getRole(mostVoted) == Roles.ELDER && hasElderRevive) {
                hasElderRevive = false;
            } else {
                this.murderTargets.add(mostVoted);
                killMostVotedByWolves = true;
            }
            
            
            continueNight();          
        } 

    }

    public void replyToSeer(String id) {
        seerChannel.sendMessage("Le joueur que vous avez observé est: **" + getRole(id).getName() + "**.").complete();
        continueNight();
    }

    public void addDayVote(String idClicker, String idReceiver) {
        dayVotes.put(idClicker, idReceiver);        
    }

    public void addNightVote(String idClicker, String idReceiver) {
        wolfVotes.put(idClicker, idReceiver);        
    }

    private void killPlayer(String id) {
        if(players.contains(id)) {
            for(Roles r : roles.keySet()) {
                if(roles.get(r).contains(id)) {
                    
                    if(r == Roles.HUNTER && !hunterKillsSomeone) {
                        hunterKillsSomeone = true;
                    } else {
                        players.remove(id);
                        if(roles.get(r).size() != 1) {
                            roles.get(r).remove(id);
                        } else {
                            roles.remove(r);
                        }
                    }
                    
                    break;
                }
            }
        }
        
    }


    public boolean isInGame(String id) {
        return players.contains(id);
    }

    public boolean isCurrentNight(int night) {
        return this.night == night;
    }

    public ActionRow[] getActionRowsOf(ArrayList<String> ids, String buttonID) {

        int size = (int) Math.max(1, Math.ceil(ids.size() / 5.0));
        ActionRow[] actionRows = new ActionRow[size];

                
        for (int i = 0; i < size; i++) {

            ArrayList<Button> buttonsToAdd = new ArrayList<>();

            for (int j = i*5; j < Math.min(ids.size() - i * 5, 5); j++) {
                String id = (String) ids.get(j + 5 * i);
                buttonsToAdd.add(Button.primary("LG" + night + buttonID + id, textChannel.getGuild().getMemberById(id).getEffectiveName()));
            }


            if (!buttonsToAdd.isEmpty()) actionRows[i] = ActionRow.of(buttonsToAdd);

        }

        return actionRows;

    }

    public void sendDeathPotionMessage() {
        this.witchChannel.sendMessage("Qui voulez-vous tuer ?")
            .setActionRows(getActionRowsOf(players, "witchDeath"))
            .complete();
    }

    public void killByWitch(String id) {
        this.murderTargets.add(id);
    }

    public void hunterKills(String hunter, String id) {
        textChannel.sendMessage("Le chasseur a décidé de tuer **" + textChannel.getGuild().getMemberById(id).getEffectiveName() + "**. Il était *" + getRole(id) + "*.").complete();
        hunterKillsSomeone = false;
        killPlayer(hunter);
        killPlayer(id);
        day();
    }

    public void disableLifePotion() {
        this.witchLifePotionDisabled = true;
    }

    public void disableDeathPotion() {
        this.witchDeathPotionDisabled = true;
    }

    public void cancelKillByWitch() {
        killMostVotedByWolves = false;
        this.murderTargets.remove(0);
    }
    
    public Roles getRole(String id) {
        Roles role = Roles.NULL;
        for(int i = 0 ; i < roles.size() ; i++) {
            if( ((ArrayList<String>)roles.get(roles.keySet().toArray()[i])).contains(id)) {
                role = (Roles) roles.keySet().toArray()[i];
            }
        }  
        return role;
    }

}
