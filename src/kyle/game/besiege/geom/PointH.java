/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kyle.game.besiege.geom;

/**
 * Point.java Function Date Jun 13, 2013
 *
 * @author Connor
 */
public class PointH {

   public static double distance(PointH _coord, PointH _coord0) {
        return Math.sqrt((_coord.x - _coord0.x) * (_coord.x - _coord0.x) + (_coord.y - _coord0.y) * (_coord.y - _coord0.y));
    }
    public double x, y;

    public PointH(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString(){
        return x + ", " + y;
    }

    public double l2() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }
}
