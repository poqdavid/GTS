package me.nickimpact.gts.pixelmon;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.configuration.AbstractConfig;
import com.nickimpact.impactor.api.configuration.AbstractConfigAdapter;
import com.nickimpact.impactor.api.configuration.ConfigBase;
import com.nickimpact.impactor.api.logger.Logger;
import com.nickimpact.impactor.api.plugins.PluginInfo;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import com.nickimpact.impactor.api.services.plan.PlanData;
import com.nickimpact.impactor.logging.ConsoleLogger;
import com.nickimpact.impactor.logging.SpongeLogger;
import lombok.Getter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.text.Translator;
import me.nickimpact.gts.pixelmon.config.PokemonConfigKeys;
import me.nickimpact.gts.pixelmon.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.pixelmon.entries.PokemonEntry;
import me.nickimpact.gts.pixelmon.text.NucleusPokemonTokens;
import me.nickimpact.gts.pixelmon.ui.PixelmonUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Plugin(id = "gts_reforged", name = "GTS Reforged Bridge", version = "1.0.0", dependencies = @Dependency(id = "gts"))
public class PixelmonBridge extends SpongePlugin {

	@Getter private static PixelmonBridge instance;

	@Inject
	private org.slf4j.Logger fallback;
	private Logger logger;

	private GtsService service;

	private File configDir = new File("config/gts/");
	private ConfigBase config;
	private ConfigBase msgConfig;

	@Listener
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		logger = new ConsoleLogger(this, new SpongeLogger(this, fallback));
		service = Sponge.getServiceManager().provideUnchecked(GtsService.class);
		service.registerEntry(
				"Pokemon",
				PokemonEntry.class,
				new PixelmonUI(),
				"pixelmon:gs_ball"
		);

		this.config = new AbstractConfig(this, new AbstractConfigAdapter(this), new PokemonConfigKeys(), "reforged.conf");
		this.config.init();
		this.msgConfig = new AbstractConfig(this, new AbstractConfigAdapter(this), new PokemonMsgConfigKeys(), "lang/reforged-en_us.conf");
		this.msgConfig.init();
	}

	@Listener
	public void onServerStarted(GameStartingServerEvent e) {
		for(Map.Entry<String, Translator> token : NucleusPokemonTokens.getTokens().entrySet()) {
			service.getTokensService().register(token.getKey(), token.getValue());
		}
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo() {
			@Override
			public String getID() {
				return "gts_reforged";
			}

			@Override
			public String getName() {
				return "GTS Reforged Bridge";
			}

			@Override
			public String getVersion() {
				return "1.0.0";
			}

			@Override
			public String getDescription() {
				return "";
			}
		};
	}

	@Override
	public Optional<PlanData> getPlanData() {
		return Optional.empty();
	}

	@Override
	public List<ConfigBase> getConfigs() {
		return Lists.newArrayList();
	}

	@Override
	public List<SpongeCommand> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Override
	public void onDisconnect() {}

	@Override
	public void onReload() {
		this.config.reload();
		this.msgConfig.reload();
	}

	@Override
	public Path getConfigDir() {
		return this.configDir.toPath();
	}
}