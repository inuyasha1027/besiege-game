package kyle.game.besiege.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;

public class BattleMap extends Group {
	TextureRegion map;
	int width = 600;
	int height = 600;
	
	public BattleMap(BattleStage bs) {
		this.map = new TextureRegion(new Texture(Gdx.files.internal("gras.jpg")));
	}
	
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.draw(map, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

}
