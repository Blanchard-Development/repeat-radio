package xyz.srnyx.repeatradio.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyLibrary;


public class LocalAudioLoader implements AudioLoadResultHandler {
    @NotNull private final AudioPlayer player;

    public LocalAudioLoader(@NotNull AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void trackLoaded(@NotNull AudioTrack track) {
        player.startTrack(track, false);
    }

    @Override
    public void playlistLoaded(@NotNull AudioPlaylist playlist) {
        // Do nothing
    }

    @Override
    public void noMatches() {
        // Do nothing
    }

    @Override
    public void loadFailed(@NotNull FriendlyException exception) {
        LazyLibrary.LOGGER.error("An error occurred while loading audio!", exception);
    }
}
