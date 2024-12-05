package fr.exatio.lgbot.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import fr.exatio.lgbot.game.Roles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Embeds {

    public static void sendCreateGameEmbed(TextChannel channel) {
        channel.sendMessageEmbeds(getCreateGameEmbed())
                .setActionRow(
                        Button.primary("createGame", "Créer une partie")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDC3A")))
                .queue();
    }

    public static void editMessageToCreateGameEmbed(Message message) {
        message.editMessageEmbeds(getCreateGameEmbed())
                .setActionRow(
                        Button.primary("createGame", "Créer une partie")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDC3A")))
                .queue();
    }

    private static MessageEmbed getCreateGameEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Loup Garou de Thiercelieux");
        eb.setColor(Color.darkGray);
        eb.setDescription("Bluffez et débattez afin d'éliminer l'équipe adverse !");
        eb.addField("Les villageois", "Le jour, ils débattent pour débusquer les loups", true);
        eb.addField("Les Loups-Garous", "Chaque nuit, ils éliminent un villageois" , true);
        eb.setFooter("Copyright © lui-même, 2022");
        return eb.build();
    }

    public static MessageEmbed getChooseOptionsEmbed(ArrayList<String> players, ArrayList<Roles> roles) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Nouvelle partie");
        eb.setColor(Color.green);
        eb.setDescription("Choisissez ici les options pour votre nouvelle partie de loup-garou !");

        StringBuilder stringBuilder = new StringBuilder();

        if(!players.isEmpty()) {
            for(int i = 0 ; i < players.size() ; i++) {
                stringBuilder.append(players.get(i));
               if(i != players.size()-1) stringBuilder.append(", ");
            }
        }
        String str = stringBuilder.toString();

        eb.addField("Joueurs", str.isBlank() ? "Aucun joueur" : str, false);


        HashMap<String, Integer> map = new HashMap<>();

        for(Roles r : roles) {
            if(!map.containsKey(r.getName())) {
                map.put(r.getName(), 1);
            } else {
                map.replace(r.getName(), map.get(r.getName()) + 1);
            }
        }

        StringBuilder stringBuilder1 = new StringBuilder();
        for(int i = 0 ; i < map.size() ; i++) {
            String current = (String) map.keySet().toArray()[i];
            stringBuilder1.append(current);
            int number = map.get(current);
            if(number != 1) {
                stringBuilder1.append(" (")
                        .append(map.get(current))
                        .append(")");
            }
            if(i != map.size()-1) stringBuilder1.append(", ");

        }
        String str1 = stringBuilder1.toString();

        eb.addField("Rôles", str1.isBlank() ? "Aucun rôle" : str1, false);

        return getNumberOfRoles(eb, map);
    }

    @NotNull
    private static MessageEmbed getNumberOfRoles(EmbedBuilder eb, HashMap<String, Integer> map) {
        int nbRoles = 0;
        for(int i = 0 ; i < map.size() ; i++) {
            nbRoles += (Integer) map.values().toArray()[i];
        }

        eb.addField("Nombre de rôles", String.valueOf(nbRoles), true);

        return eb.build();
    }

    public static void editMessageToAddRoleEmbed(Message message, ArrayList<Roles> roles) {

        boolean witchDisabled = false;
        boolean seerDisabled = false;
        boolean elderDisabled = false;
        boolean hunterDisabled = false;

        for(Roles r : roles) {
            if(r.getName().equals("Sorcière")) witchDisabled = true;
            if(r.getName().equals("Voyante")) seerDisabled = true;
            if(r.getName().equals("Ancien")) elderDisabled = true;
            if(r.getName().equals("Chasseur")) hunterDisabled = true;
        }

        message.editMessageEmbeds(getAddRoleEmbed(roles))
                .setActionRows(
                        ActionRow.of(
                                Button.primary("addVillager", "Villageois"),
                                Button.primary("addWolf", "Loup"),
                                Button.primary("addWitch", "Sorcière").withDisabled(witchDisabled),
                                Button.primary("addSeer", "Voyante").withDisabled(seerDisabled),
                                Button.primary("addElder", "Ancien").withDisabled(elderDisabled)
                        ),
                        ActionRow.of(
                                Button.primary("addHunter", "Chasseur").withDisabled(hunterDisabled)
                        ),
                        ActionRow.of(
                                Button.danger("goToOptions", "Précédent")
                        )
                )
                .queue();

    }
    private static MessageEmbed getAddRoleEmbed(ArrayList<Roles> roles) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Ajouter un rôle");
        eb.setDescription("Pour ajouter un rôle, appuyez sur le bouton correspondant");

        HashMap<String, Integer> map = new HashMap<>();

        for(Roles r : roles) {
            if(!map.containsKey(r.getName())) {
                map.put(r.getName(), 1);
            } else {
                map.replace(r.getName(), map.get(r.getName()) + 1);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0 ; i < map.size() ; i++) {
            String current = (String) map.keySet().toArray()[i];
            stringBuilder.append(current);
            int number = map.get(current);
            if(number != 1) {
                stringBuilder.append(" (")
                        .append(map.get(current))
                        .append(")");
            }
            if(i != map.size()-1) stringBuilder.append(", ");

        }
        String str = stringBuilder.toString();

        eb.addField("Rôles actuels", str.isBlank() ? "Aucun rôle" : str, true);

        return getNumberOfRoles(eb, map);
    }

    public static void editMessageToRemoveRoleEmbed(Message message, ArrayList<Roles> roles) {

        if(roles.size() != 0) {

            ArrayList<Button> buttons = new ArrayList<>();
            for (Roles r : roles) {
                boolean attributed = false;

                for (Button button : buttons) {
                    if (button.getId().equals("remove" + r.getName())) {
                        attributed = true;
                        String newLabel = button.getLabel();
                        Pattern pattern = Pattern.compile("[0-9]+");
                        Matcher matcher = pattern.matcher(newLabel);
                        if (matcher.find()) {
                            newLabel = newLabel.replace(matcher.group(), String.valueOf(Integer.parseInt(matcher.group()) + 1));
                        } else {
                            newLabel = newLabel + " (2)";
                        }

                        buttons.set(buttons.indexOf(button), Button.primary("remove" + r.getName(), newLabel));
                    }
                }

                if (!attributed) buttons.add(Button.primary("remove" + r.getName(), r.getName()));

            }

            int size = (int) Math.max(1, Math.ceil(buttons.size() / 5.0));
            ActionRow actionRows1[] = new ActionRow[size + 1];

            for (int i = 0; i < size; i++) {

                ArrayList<Button> buttonsToAdd = new ArrayList<>();

                for (int j = 0; j < Math.min(buttons.size() - i * 5, 5); j++) {
                    buttonsToAdd.add(buttons.get(j + 5 * i));
                }


                if (!buttonsToAdd.isEmpty()) actionRows1[i] = ActionRow.of(buttonsToAdd);
            }

            actionRows1[actionRows1.length - 1] = ActionRow.of(Button.danger("goToOptions", "Précédent"));

            message.editMessageEmbeds(getRemoveRoleEmbed(roles))
                    .setActionRows(actionRows1)
                    .queue();
        } else {
            message.editMessageEmbeds(getRemoveRoleEmbed(roles))
                    .setActionRow(Button.danger("goToOptions", "Précédent"))
                    .queue();
        }
    }

    private static MessageEmbed getRemoveRoleEmbed(ArrayList<Roles> roles) {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Enlever un rôle");
        eb.setDescription("Pour enlever un rôle, appuyez sur le bouton correspondant.");

        HashMap<String, Integer> map = new HashMap<>();

        for(Roles r : roles) {
            if(!map.containsKey(r.getName())) {
                map.put(r.getName(), 1);
            } else {
                map.replace(r.getName(), map.get(r.getName()) + 1);
            }
        }

        StringBuilder stringBuilder1 = new StringBuilder();
        for(int i = 0 ; i < map.size() ; i++) {
            String current = (String) map.keySet().toArray()[i];
            stringBuilder1.append(current);
            int number = map.get(current);
            if(number != 1) {
                stringBuilder1.append(" (")
                        .append(map.get(current))
                        .append(")");
            }
            if(i != map.size()-1) stringBuilder1.append(", ");

        }
        String str1 = stringBuilder1.toString();

        eb.addField("Rôles actuels", str1.isBlank() ? "Aucun rôle" : str1, false);

        return getNumberOfRoles(eb, map);
    }



}
