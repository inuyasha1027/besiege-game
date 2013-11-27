/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hoten.voronoi;

import hoten.geom.PointH;
import java.util.ArrayList;

/**
 * Center.java Function Date Jun 6, 2013
 *
 * @author Connor
 */
public class Center {

    public int index;
    public PointH loc;
    public ArrayList<Corner> corners = new ArrayList();//good
    public ArrayList<Center> neighbors = new ArrayList();//good
    public ArrayList<Edge> borders = new ArrayList();
    
    public boolean border, ocean, water, coast;
    public double elevation;
    public double moisture;
    public Biomes biome;
    public double area;

    public Center() {
    }

    public Center(PointH loc) {
        this.loc = loc;
    }
}
