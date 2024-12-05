package fr.exatio.lgbot.listeners;

import java.util.ArrayList;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import fr.exatio.lgbot.Main;
import fr.exatio.lgbot.game.LGGame;
import fr.exatio.lgbot.game.LGGameOptions;
import fr.exatio.lgbot.game.Roles;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class NewGameListener extends ListenerAdapter {
    private String gameMaster;
    private LGGameOptions options;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if(!event.getMessage().getAuthor().getId().equals(Main.jda.getSelfUser().getId())) return;

        String memberId = event.getMember().getId();
        String buttonId = event.getButton().getId();

        if(buttonId.startsWith("remove") && !buttonId.equals("removePlayer") && !buttonId.equals("removeRole")) {
            if(event.getMember().getId().equals(gameMaster)) {
                options.removeRole(buttonId.substring(6));
                updateEmbed(event.getMessage(), EmbedState.REMOVEROLE);
                event.deferEdit().queue();
                return;
            } else {
                event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
            }
        }

        switch(Objects.requireNonNull(buttonId)) {

            case "createGame":
                if(LGGame.isInGame) {
                    event.reply("Une partie est déjà en cours!").setEphemeral(true).queue();
                } else {
                    gameMaster = event.getMember().getId();
                    options = new LGGameOptions();
                    updateEmbed(event.getMessage(), EmbedState.CONFIG);
                    event.deferEdit().queue();
                }
                break;


            case "addPlayer":
                if(!options.getPlayers().contains(memberId)) {
                    options.addPlayer(memberId);
                    event.reply("Vous avez bien été ajouté à la partie.").setEphemeral(true).queue();
                    updateEmbed(event.getMessage(), EmbedState.CONFIG);
                } else {
                    event.reply("Vous êtes déjà dans la partie").setEphemeral(true).queue();
                }
                break;


            case "removePlayer":
                if(options.getPlayers().contains(memberId)) {
                    options.getPlayers().remove(memberId);
                    event.reply("Vous avez bien été supprimé de la partie.").setEphemeral(true).queue();
                    updateEmbed(event.getMessage(), EmbedState.CONFIG);
                } else {
                    event.reply("Vous n'êtes pas dans la partie").setEphemeral(true).queue();
                }
                break;


            case "goToOptions":
                if(event.getMember().getId().equals(gameMaster)) {
                    updateEmbed(event.getMessage(), EmbedState.CONFIG);
                    event.deferEdit().queue();
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;


            case "addRole":
                if(event.getMember().getId().equals(gameMaster)) {
                    updateEmbed(event.getMessage(), EmbedState.ADDROLE);
                    event.deferEdit().queue();
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;


            case "removeRole":
                if(event.getMember().getId().equals(gameMaster)) {
                    updateEmbed(event.getMessage(), EmbedState.REMOVEROLE);
                    event.deferEdit().queue();
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;
            

            case "cancel":
                if(event.getMember().getId().equals(gameMaster)) {
                    updateEmbed(event.getMessage(), EmbedState.DEFAULT);
                    event.deferEdit().queue();
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;

            case "startGame":
                if(event.getMember().getId().equals(gameMaster)) {

                    if(options.getPlayers().size() > 1) {

                        if(options.getPlayers().size() == options.getSumRoles()) {
                            if(event.getGuild().getTextChannelsByName("game-actuelle", false).isEmpty()) {
                                updateEmbed(event.getMessage(), EmbedState.DEFAULT);
                                event.deferEdit().queue();
                                new LGGame(options).startGame();
                            } else {
                                event.reply("Attendez au moins 30s depuis la dernière partie!").setEphemeral(true).queue();
                            }
                            
                        } else {
                            event.reply("Le nombre de joueurs ne correspond pas au nombre de rôles !").setEphemeral(true).queue();
                        }

                    } else {
                        event.reply("Vous ne pouvez pas lancer une partie avec si peu de joueurs!");
                    }

                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;


            case "addVillager":
                if(event.getMember().getId().equals(gameMaster)) {
                    options.addRole(Roles.DEFAULT_VILLAGER);
                    updateEmbed(event.getMessage(), EmbedState.ADDROLE);
                    event.deferEdit().queue();
                    return;
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;
            
            case "addHunter":
                if(event.getMember().getId().equals(gameMaster)) {
                    options.addRole(Roles.HUNTER);
                    updateEmbed(event.getMessage(), EmbedState.ADDROLE);
                    event.deferEdit().queue();
                    return;
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;


            case "addWolf":
                if(event.getMember().getId().equals(gameMaster)) {
                    options.addRole(Roles.DEFAULT_WOLF);
                    updateEmbed(event.getMessage(), EmbedState.ADDROLE);
                    event.deferEdit().queue();
                    return;
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;


            case "addWitch":
                if(event.getMember().getId().equals(gameMaster)) {
                    options.addRole(Roles.WITCH);
                    updateEmbed(event.getMessage(), EmbedState.ADDROLE);
                    event.deferEdit().queue();
                    return;
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;


            case "addSeer":
                if(event.getMember().getId().equals(gameMaster)) {
                    options.addRole(Roles.SEER);
                    updateEmbed(event.getMessage(), EmbedState.ADDROLE);
                    event.deferEdit().queue();
                    return;
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;

            case "addElder":
                if(event.getMember().getId().equals(gameMaster)) {
                    options.addRole(Roles.ELDER);
                    updateEmbed(event.getMessage(), EmbedState.ADDROLE);
                    event.deferEdit().queue();
                    return;
                } else {
                    event.reply("Vous n'êtes pas l'initialisateur de la game...").setEphemeral(true).queue();
                }
                break;

            default:
                break;
        }
    }

    private void updateEmbed(Message message, EmbedState state) {

        switch (state) {

            case DEFAULT -> {
                Embeds.editMessageToCreateGameEmbed(message);
            }

            case CONFIG -> {
                ArrayList<String> nicknames = new ArrayList<>();

                for (String str : options.getPlayers()) {
                    String name = message.getGuild().retrieveMemberById(str).complete().getEffectiveName();
                    nicknames.add(name);

                }

                message.editMessageEmbeds(Embeds.getChooseOptionsEmbed(nicknames, options.getRoles()))
                        .setActionRows(
                            ActionRow.of(
                                Button.primary("addPlayer", "Je joue !"),
                                Button.danger("removePlayer", "Je ne joue plus..."),
                                Button.primary("addRole", "Ajouter un rôle"),
                                Button.danger("removeRole", "Enlever un rôle"),
                                Button.success("startGame", "Lancer la game")
                            ), 
                            ActionRow.of(Button.secondary("cancel", "Précédent"))
                        )
                        
                        .queue();

            }

            case ADDROLE -> {
                Embeds.editMessageToAddRoleEmbed(message, options.getRoles());
            }

            case REMOVEROLE -> {
                Embeds.editMessageToRemoveRoleEmbed(message, options.getRoles());
            }
        }


    }



}
enum EmbedState {
    DEFAULT,
    CONFIG,
    ADDROLE,
    REMOVEROLE;
}
