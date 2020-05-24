package io.github.pseudoresonance.playtimetracker.fabric;

import io.github.pseudoresonance.playtimetracker.common.ILanguage;
import net.minecraft.client.resource.language.I18n;

public class Language implements ILanguage {

	@Override
	public String translate(String key, Object... parameters) {
		return I18n.translate(key, parameters);
	}

}
