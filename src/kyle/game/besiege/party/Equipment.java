/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;

public enum Equipment {
	SHIELD ("Shield", Type.OFFHAND, 0, 1, -1),
	IRON_BREASTPLATE ("Breastplate", Type.CHEST, 0, 3, -2),
	IRON_HELM ("Iron Helm", Type.HEAD, 0, 1, 0);
	
	public final String name;
	public final Type type;
	public final int atkMod;
	public final int defMod;
	public final int spdMod;
	public enum Type {
		OFFHAND, CHEST, HEAD, LEGS, ARMS
	}
	
	private Equipment(String name, Type type, int atkMod, int defMod, int spdMod) {
		this.name = name;
		this.type = type;
		this.atkMod = atkMod;
		this.defMod = defMod;
		this.spdMod = spdMod;
	}
	@Override
	public String toString() {
		return name;
	}
}
