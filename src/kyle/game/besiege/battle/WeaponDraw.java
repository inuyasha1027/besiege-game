package kyle.game.besiege.battle;

import kyle.game.besiege.Assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class WeaponDraw extends Actor {
	private float scale_x = 1.6f;
	private float scale_y = 2.5f;
	private Unit unit;
	private int offset_x = 12;
	private int offset_y = 10;
	
	private TextureRegion toDraw;
	
	public WeaponDraw(Unit unit) {
		this.unit = unit;
		
		String filename = "";
		
		switch(unit.weapon) {
		case PITCHFORK :
			filename = "pitchfork";
			break;
		case MILITARY_FORK : 
			filename = "militaryfork";
			break;
		case SPEAR :
			filename = "spear";
			break;
		case HATCHET :
			filename = "axe";
			break;
		case CLUB :
			filename = "club";
			break;
		case PIKE :
			filename = "pike";
			break;
		case HALBERD :
			filename = "halberd";
			break;
		case LONGSWORD :
			filename = "longsword";
			break;
		case BATTLE_AXE :
			filename = "battleaxe";
			break;
		case SHORTSWORD :
			filename = "shortsword";
			break;
		case WAR_HAMMER :
			filename = "warhammer";
			break;
		case MACE :
			filename = "mace";
			break;
		case CAVALRY_SPEAR :
			filename = "spear";
			break;
		case CAVALRY_AXE :
			filename = "axe";
			break;
		case CAVALRY_PICK :
			filename = "warhammer";
			break;
			
		case LANCE :
			filename = "lance";
			break;
		case ARMING_SWORD :
			filename = "shortsword";
			break;
		case FLAIL :
			filename = "morningstar";
			break;
		
		case GUISARME :
			filename = "guisarme";
			break;
		case VOULGE :
			filename = "voulge";
			break;
		case GREATSWORD :
			filename = "claymore";
			break;
		case GLAIVE :
			filename = "bardiche";
			break;
		case FALCHION :
			filename = "falchion";
			break;
		case MAUL :
			filename = "maul";
			break;
		case MORNINGSTAR :
			filename = "morningstar";
			break;
		}
		
		this.toDraw = Assets.weapons.findRegion(filename);
//		this.toDraw = new TextureRegion(new Texture(Gdx.files.internal("weapons/axe")));
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {	
		this.toFront();
		batch.draw(toDraw, offset_x*unit.stage.scale, offset_y*unit.stage.scale, 0, 0, toDraw.getRegionWidth(), toDraw.getRegionHeight(), unit.stage.scale*scale_x, unit.stage.scale*scale_y, 0);		
	}
	
}