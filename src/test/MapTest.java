//package test;
//
//import kyle.game.besiege.Map;
//
//import com.badlogic.gdx.ApplicationListener;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Screen;
//import com.badlogic.gdx.graphics.FPSLogger;
//import com.badlogic.gdx.graphics.GL10;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//
//public class MapTest implements Screen {
//	Texture texture;
//	SpriteBatch batch;
//	FPSLogger fpsLog;
//
//	public MapTest() {
////		System.out.println("Map test");
//		Map map = Map.generate();
//		texture = map.bg;
//
//		batch = new SpriteBatch();
//		fpsLog = new FPSLogger();
//	}
//
//	@Override
//	public void render(float delta) {
//		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
//		batch.begin();
//
//		batch.draw(texture, 0, 0);
//
//		batch.end();
//
//		fpsLog.log();
//	}
//
//	@Override
//	public void dispose () {
//		batch.dispose();
//		texture.dispose();
//	}
//
//	@Override
//	public void resize(int width, int height) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void pause() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void resume() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void show() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void hide() {
//		// TODO Auto-generated method stub
//		
//	}
//}