package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Map;

import de.blablubbabc.paintball.utils.Translator;

public class Origin {
	
	public Origin() {
		
	}
	
	protected Map<String, String> getDefaultVariablesMap(FragInformations fragInfo) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("killer", fragInfo.getKiller().getName());
		vars.put("killer_color", fragInfo.getKillerColor().toString());
		vars.put("pre_killer", fragInfo.getPreKiller());
		vars.put("after_killer", fragInfo.getAfterKiller());
		vars.put("target", fragInfo.getTarget().getName());
		vars.put("target_color", fragInfo.getTargetColor().toString());
		vars.put("pre_target", fragInfo.getPreTarget());
		vars.put("after_target", fragInfo.getAfterTarget());
		vars.put("feed_color", Paintball.instance.feeder.getFeedColor());
		
		return vars;
	}
	
	public String getKillMessage(FragInformations fragInfo) {
		// return default frag message:
		return Translator.getString("WEAPON_FEED_DEFAULT", getDefaultVariablesMap(fragInfo));
	}
	
}