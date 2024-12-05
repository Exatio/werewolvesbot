package fr.exatio.lgbot.game;

import org.jetbrains.annotations.NotNull;

import fr.exatio.lgbot.Main;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InGameListener extends ListenerAdapter {
    
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if(!event.getMessage().getAuthor().getId().equals(Main.jda.getSelfUser().getId())) return;

        String memberId = event.getMember().getId();
        String buttonId = event.getButton().getId();
        
        if(buttonId.startsWith("LG")) {

            if(LGGame.isInGame) {

                LGGame current = LGGame.currentGame;

                if(current.isInGame(memberId)) {

                    buttonId = buttonId.substring(2);

                    String night;
                    String id;
        
        
                    if(buttonId.contains("voteNight")) {
        
                        String informations[] = buttonId.split("voteNight");
                        
                        night = informations[0];
                        id = informations[1];
        
                        if(current.isCurrentNight(Integer.parseInt(night))) {

                            current.addNightVote(memberId, id);
                            current.updateNightVotes();
                            event.reply("Votre vote a été pris en compte").setEphemeral(true).queue();

                        } else {
                            event.reply("Vous ne pouvez plus réagir sur ce message...").setEphemeral(true).queue();
                        }
        
    
                    } else if(buttonId.contains("vote")) {

                        String informations[] = buttonId.split("vote");
                        
                        night = informations[0];
                        id = informations[1];
        
                        if(current.isCurrentNight(Integer.parseInt(night))) {

                            current.addDayVote(memberId, id);
                            current.updateDayVotes();
                            event.reply("Votre vote a été pris en compte").setEphemeral(true).queue();

                        } else {
                            event.reply("Vous ne pouvez plus réagir sur ce message...").setEphemeral(true).queue();
                        }

                    } else if(buttonId.contains("seer")) {
                        
                        String informations[] = buttonId.split("seer");
                        
                        night = informations[0];
                        id = informations[1];
        
                        if(current.isCurrentNight(Integer.parseInt(night))) {

                            current.replyToSeer(id);
                            event.reply("Votre demande a été prise en compte").setEphemeral(true).queue();

                        } else {
                            event.reply("Vous ne pouvez plus réagir sur ce message...").setEphemeral(true).queue();
                        }

                    } else if(buttonId.contains("witch")) {
                        
                        String informations[] = buttonId.split("witch");
                        
                        night = informations[0];
                        id = informations[1];
        
                        if(current.isCurrentNight(Integer.parseInt(night))) {

                            if(id.equals("Life")) {
                                current.cancelKillByWitch();
                                current.disableLifePotion();
                                current.continueNight();
                            } else if(id.equals("Nothing")) {
                                current.continueNight();
                            } else if(id.equals("Death")) {
                                current.sendDeathPotionMessage();
                                current.disableDeathPotion();
                            } else if(id.startsWith("Death")) {
                                String toKillId = id.substring(5);
                                current.killByWitch(toKillId);
                                current.continueNight();
                            }
                            
                            event.reply("Votre demande a été prise en compte").setEphemeral(true).queue();

                        } else {
                            event.reply("Vous ne pouvez plus réagir sur ce message...").setEphemeral(true).queue();
                        }

                    } else if(buttonId.contains("hunter")) {

                        String informations[] = buttonId.split("hunter");
                        
                        night = informations[0];
                        id = informations[1];

                        if(current.isCurrentNight(Integer.parseInt(night))) { 
                            if(current.getRole(memberId) == Roles.HUNTER && current.hunterKillsSomeone)  {
                                current.hunterKills(memberId, id);
                                event.reply("Votre choix a été enregistré !").setEphemeral(true).queue();
                            } else {
                                event.reply("Vous n'avez pas le droit de faire ça!'").setEphemeral(true).queue();
                            }
                        } else {
                            event.reply("Vous ne pouvez plus réagir sur ce message...").setEphemeral(true).queue();
                        }
                    }

                } else {
                    event.reply("Vous n'êtes pas dans la partie...").setEphemeral(true).queue();
                }
                

            } else {
                event.reply("Il n'y a pas de partie en cours...").setEphemeral(true).queue();
            }
            

        }

        
    }
    
}
