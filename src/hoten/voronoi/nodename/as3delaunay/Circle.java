package hoten.voronoi.nodename.as3delaunay;

import hoten.geom.PointH;

public final class Circle extends Object {

    public PointH center;
    public double radius;

    public Circle(double centerX, double centerY, double radius) {
        super();
        this.center = new PointH(centerX, centerY);
        this.radius = radius;
    }

    public String toString() {
        return "Circle (center: " + center + "; radius: " + radius + ")";
    }
}