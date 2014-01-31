/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
public class MyPacker {
        public static void main (String[] args) throws Exception {
                TexturePacker2.process("A:/Users/Kyle/Dropbox/LibGDX/besiege-game-android/assets/textures","A:/Users/Kyle/Dropbox/LibGDX/besiege-game-android/assets", "atlas1");
                TexturePacker2.process("A:/Users/Kyle/Dropbox/LibGDX/besiege-game-android/additional/map", "A:/Users/Kyle/Dropbox/LibGDX/besiege-game-android/assets", "mapAtlas");
        }
}
