package xyz.srnyx.repeatradio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyLibrary;
import xyz.srnyx.lazylibrary.settings.LazySettings;

import xyz.srnyx.repeatradio.audio.LocalAudioLoader;
import xyz.srnyx.repeatradio.audio.LocalSendHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class RepeatRadio extends LazyLibrary {
    private RepeatConfig config;

    public RepeatRadio() {
        jda.getPresence().setActivity(config.status);
        LOGGER.info("Repeat Radio has finished starting!");
    }

    @Override @NotNull
    public Consumer<LazySettings> getSettings() {
        return newSettings -> newSettings
                .gatewayIntents(GatewayIntent.GUILD_VOICE_STATES)
                .disabledCacheFlags(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOJI,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.STICKER,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.SCHEDULED_EVENTS);
    }

    @Override
    public void onReady() {
        // Load the config
        config = new RepeatConfig(this);
        if (config.file == null || !config.file.exists()) {
            LOGGER.error("The file path provided in the config does not exist!");
            return;
        }

        // Get guild & channel
        final Guild guild = config.guildNode.getGuild();
        if (guild == null) {
            LOGGER.error("The guild ID provided in the config is invalid!");
            return;
        }
        final VoiceChannel channel = config.guildNode.getChannel();
        if (channel == null) {
            LOGGER.error("The channel ID provided in the config is invalid!");
            return;
        }

        // Get variables
        final String path = config.file.getAbsolutePath();
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        final AudioPlayer player = playerManager.createPlayer();
        player.setVolume(50);
        final LocalAudioLoader audioLoader = new LocalAudioLoader(player);
        final AudioManager audioManager = guild.getAudioManager();
        audioManager.setSelfDeafened(true);
        audioManager.setSendingHandler(new LocalSendHandler(player));
        audioManager.openAudioConnection(channel);

        // Start the scheduler
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> playerManager.loadItem(path, audioLoader), config.interval - (System.currentTimeMillis() / 1000) % config.interval, config.interval, TimeUnit.SECONDS);
    }

    @Override
    public void onStop() {
        final Guild guild = config.guildNode.getGuild();
        if (guild != null) guild.getAudioManager().closeAudioConnection();
    }

    public static void main(@NotNull String[] arguments) {
        new RepeatRadio();
    }
}
