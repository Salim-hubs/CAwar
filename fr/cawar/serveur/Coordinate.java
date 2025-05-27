package fr.cawar.serveur;

public class Coordinate {
    public float x;
    public float y;

    public Coordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(Coordinate c) {
        this.x = c.x;
        this.y = c.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    // Renvoie une copie de coordinate
    public Coordinate getCoordinate() {
        return new Coordinate(this.x, this.y);
    }

    public float getX() {
        return this.x ;
    }
    
    public float getY() {
        return this.y ;
    }

    public void setX(float x) {
        this.x = x ;
    }

    public void setY(float y){
        this.y = y ;
    }

}
