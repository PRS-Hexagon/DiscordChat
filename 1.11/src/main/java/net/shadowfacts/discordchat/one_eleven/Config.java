package net.shadowfacts.discordchat.one_eleven;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
import net.shadowfacts.discordchat.api.IConfig;
import net.shadowfacts.discordchat.api.permission.Permission;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shadowfacts
 */
public class Config implements IConfig {

	private File file;
	private com.typesafe.config.Config config;

	public void init(File file) throws IOException {
		this.file = file;

		config = ConfigFactory.parseFile(file).withFallback(config);
		save();
	}

	@Override
	public void save() throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}

		ConfigObject toRender = config.root().withOnlyKey("discordchat");
		String s = toRender.render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false));
		FileUtils.write(file, s);
	}

	@Override
	public String getToken() {
		return config.getString("discordchat.discord.token");
	}

	@Override
	public String getServerID() {
		return config.getString("discordchat.discord.server");
	}

	@Override
	public String getChannelID() {
		return config.getString("discordchat.discord.channel");
	}

	@Override
	public String getCommandPrefix() {
		return config.getString("discordchat.commands.prefix");
	}

	@Override
	public Map<String, Permission> getPermissions() {
		com.typesafe.config.Config config = this.config.getConfig("discordchat.permissions");
		Map<String, Permission> map = new HashMap<>();
		config.entrySet().stream().map(Map.Entry::getKey).forEach(key -> {
			Permission value;
			try {
				value = Permission.valueOf(config.getString(key));
			} catch (IllegalArgumentException e) {
				OneElevenMod.logger.warn(e, "Invalid permission '" + config.getString(key) + "' for user '" + key + "'");
				return;
			}
			map.put(key, value);
		});
		return map;
	}

	@Override
	public void setPermissions(Map<String, Permission> permissions) {
		Map<String, String> map = new HashMap<>();
		permissions.forEach((key, value) -> {
			map.put(key, value.name());
		});
		config = config.withValue("discordchat.permissions", ConfigValueFactory.fromAnyRef(map));
	}

	@Override
	public boolean sendDeathMessages() {
		return config.getBoolean("discordchat.relay.deaths");
	}

	@Override
	public boolean sendAchievementMessages() {
		return config.getBoolean("discordchat.relay.achievements");
	}

	@Override
	public boolean sendJoinLeaveMessages() {
		return config.getBoolean("discordchat.relay.joinleave");
	}

}