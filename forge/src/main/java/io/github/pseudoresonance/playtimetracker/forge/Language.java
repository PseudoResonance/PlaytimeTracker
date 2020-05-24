package io.github.pseudoresonance.playtimetracker.forge;

import io.github.pseudoresonance.playtimetracker.common.ILanguage;
import net.minecraft.client.resources.I18n;

public class Language implements ILanguage {

	@Override
	public String translate(String key, Object... parameters) {
		return I18n.format(key, parameters);
	}

}
