///*******************************************************************************
// * Besiege
// * by Kyle Dhillon
// * Source Code available under a read-only license. Do not copy, modify, or distribute.
// ******************************************************************************/
//package kyle.game.besiege;
//
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.scenes.scene2d.Actor;
//
//public class BackgroundMap extends Actor {
//	public static int totalWidth;
//	public static int totalHeight;
//	
//	public BackgroundMap() {
//		totalWidth = Assets.map1.getWidth()*2;
//		totalHeight = Assets.map1.getHeight()*2;
//		setWidth(Assets.map1.getWidth());
//		setHeight(Assets.map1.getHeight());
//		setPosition(0, 0);
//	}
//	
//	@Override
//	public void draw(SpriteBatch batch, float parentAlpha) {

//		super.draw(batch, parentAlpha);
//		//batch.draw(Assets.map.findRegion("smallMap"), getX(), getY(), getWidth(), getHeight());
//		batch.draw(Assets.map3, getX(), getY());
//		batch.draw(Assets.map4, getX() + Assets.map1.getWidth(), getY());
//		batch.draw(Assets.map1, getX(), getY() + Assets.map1.getHeight());
//		batch.draw(Assets.map2, getX() + Assets.map1.getWidth(), getY() + Assets.map1.getHeight());
//	}
//}
