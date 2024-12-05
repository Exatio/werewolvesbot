package fr.exatio.lgbot;

import javax.security.auth.login.LoginException;

import fr.exatio.lgbot.game.InGameListener;
import fr.exatio.lgbot.listeners.MessageCr;
import fr.exatio.lgbot.listeners.NewGameListener;
import fr.exatio.lgbot.music.MusicManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {

    public static MusicManager musicManager;
    public static JDA jda;

    public static void main(String[] args) throws LoginException, InterruptedException {

        JDABuilder builder = JDABuilder.createDefault("YOUR TOKEN HERE");
        builder.setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS);
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setBulkDeleteSplittingEnabled(true);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        builder.addEventListeners(new MessageCr(), new NewGameListener(), new InGameListener());
        builder.setActivity(Activity.playing("déchiqueter sa nourriture"));
        jda = builder.build();
        jda.awaitReady();

        musicManager = new MusicManager();
    }

}
