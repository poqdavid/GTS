package me.nickimpact.gts.generations.deprecated;

import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.config.PixelmonEntityList;
import me.nickimpact.gts.generations.GenerationsBridge;
import me.nickimpact.gts.generations.utils.GsonUtils;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import me.nickimpact.gts.api.deprecated.Entry;

@Deprecated
@JsonTyping("pokemon")
public class PokemonEntry extends Entry<Pokemon, EntityPixelmon> {

	public PokemonEntry() {}

	@Override
	public String getInnerElementOrDefault() {
		return this.getElement().nbtJSON;
	}

	@Override
	public EntityPixelmon getEntry() {
		GenerationsBridge.getInstance().getPluginLogger().debug(this.element.nbtJSON == null ? "null" : "populated");
		try {
			return (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(JsonToNBT.getTagFromJson(this.element.nbtJSON), (World) Sponge.getServer().getWorlds().iterator().next());
		} catch (Exception e) {
			return (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(GsonUtils.deserialize(this.element.nbtJSON), (World) Sponge.getServer().getWorlds().iterator().next());
		}
	}
}
