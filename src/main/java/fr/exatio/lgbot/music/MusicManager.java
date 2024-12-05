package fr.exatio.lgbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import fr.exatio.lgbot.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicManager {

    private final AudioPlayerManager playerManager;
    private final AudioManager audioManager;
    private final AudioPlayer player;
    public MusicManager() throws InterruptedException {

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        this.audioManager = Main.jda.getGuildById("1040236833738600530").getAudioManager();

        player = playerManager.createPlayer();

        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));

        player.addListener(event -> {
            if(event instanceof TrackEndEvent) {
                if(((TrackEndEvent) event).endReason == AudioTrackEndReason.FINISHED) {
                    loadAndPlay();
                }
            }
        });
    }

    private void play(AudioTrack track) {
        if(!audioManager.isConnected()) {
            audioManager.openAudioConnection(Main.jda.getVoiceChannelById("1040236834397110285"));
        }
        player.setVolume(30);
        player.playTrack(track);
    }
    public void loadAndPlay() {

        playerManager.loadItem("https://www.youtube.com/watch?v=i9eG4E-5yVQ", new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }

    public void stop() {
        player.stopTrack();
    }
}
