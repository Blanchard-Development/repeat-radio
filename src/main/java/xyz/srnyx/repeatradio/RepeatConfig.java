package xyz.srnyx.repeatradio;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;


public class RepeatConfig {
    @NotNull private final RepeatRadio repeatRadio;
    @Nullable public final Activity status;
    public final long interval;
    @Nullable public final File file;
    @NotNull public final GuildNode guildNode;

    public RepeatConfig(@NotNull RepeatRadio repeatRadio) {
        this.repeatRadio = repeatRadio;

        // statusType & statusMessage
        Activity.ActivityType statusType = null;
        final String statusTypeName = repeatRadio.settings.fileSettings.file.yaml.node("status", "type").getString();
        if (statusTypeName != null) try {
            statusType = Activity.ActivityType.valueOf(statusTypeName);
        } catch (final IllegalArgumentException ignored) {
            // Ignored
        }
        final String statusMessage = repeatRadio.settings.fileSettings.file.yaml.node("status", "message").getString();
        this.status = statusType == null || statusMessage == null ? null : Activity.of(statusType, statusMessage);

        // interval
        this.interval = repeatRadio.settings.fileSettings.file.yaml.node("interval").getLong();

        // file
        final String filePath = repeatRadio.settings.fileSettings.file.yaml.node("file").getString();
        this.file = filePath == null ? null : new File(filePath);

        // guildNode
        this.guildNode = new GuildNode();
    }

    public class GuildNode {
        public final long id;
        public final long channelId;

        public GuildNode() {
            final ConfigurationNode node = repeatRadio.settings.fileSettings.file.yaml.node("guild");
            this.id = node.node("id").getLong();
            this.channelId = node.node("channel").getLong();
        }

        @Nullable
        public Guild getGuild() {
            return repeatRadio.jda.getGuildById(id);
        }

        @Nullable
        public VoiceChannel getChannel() {
            final Guild guild = getGuild();
            return guild == null ? null : guild.getVoiceChannelById(channelId);
        }
    }
}
