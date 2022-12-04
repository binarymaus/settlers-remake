package jsettlers.common.action;

import jsettlers.common.movable.ESpellType;

public class AskCastSpellAction extends Action {

	private ESpellType spell;

	public AskCastSpellAction(ESpellType spell) {
		super(EActionType.ASK_CAST_SPELL);
		this.spell = spell;
	}

	public ESpellType getSpell() {
		return spell;
	}
}
