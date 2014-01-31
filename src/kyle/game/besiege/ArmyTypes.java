/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
//package kyle.game.besiege;
//
//import java.util.Scanner;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.utils.Array;
//
//public class ArmyTypes {
//	public static int PATROL = 1;
//	public static int MERCHANT = 2;
//	public static int FARMER = 3;
//	
//	private Array<String> names;
//	private Array<Integer> lowerTroopBound;
//	private Array<Integer> upperTroopBound;
//	private Array<Integer> money;
//	
//	public ArmyTypes() {
//		names = new Array<String>();
//		lowerTroopBound = new Array<Integer>();
//		upperTroopBound = new Array<Integer>();
//		money = new Array<Integer>();
//		
//		Scanner scanner = new Scanner(Gdx.files.internal("armyTypes.txt").readString());
//		while (scanner.hasNextLine()) {
//			int index = scanner.nextInt();
//			names.add(scanner.next());
//			lowerTroopBound.add(scanner.nextInt());
//			upperTroopBound.add(scanner.nextInt());
//			money.add(scanner.nextInt());
//		}
//		scanner.close();
//	}
//	
//	public String getName(int index) {
//		return names.get(index);
//	}
//	public int getLowerTroopBound(int index) {
//		return lowerTroopBound.get(index);
//	}
//	public int getUpperTroopBound(int index) {
//		return upperTroopBound.get(index);
//	}
//	public int getMoney(int index) {
//		return money.get(index);
//	}
//}
