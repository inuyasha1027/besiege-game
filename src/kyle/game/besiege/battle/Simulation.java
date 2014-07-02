package kyle.game.besiege.battle;

import kyle.game.besiege.Assets;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;

public class Simulation extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Stage mainstage;
	BattleStage bs;

	
	@Override
	public void create () {
		Assets.load();
		batch = new SpriteBatch();

		bs = new BattleStage(null, null, null); 
		
		mainstage = new Stage();
		mainstage.addActor(bs);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mainstage.act();
		mainstage.draw();
	}
}
