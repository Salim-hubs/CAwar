package fr.cawar.serveur;



abstract class Hitbox {
    protected Coordinate position ;
        
    public Hitbox(Coordinate c) {
        this.position = c ;
    }

    public Coordinate getPosition() {
        return this.position.getCoordinate() ;
    }

    public void setPosition(Coordinate c) {
        this.position = c ;
    }

    public void setX(float x) {
        this.position.setX(x) ;
    }

    public void setY(float y) {
        this.position.setY(y) ;
    }

    public float getX() {
        return this.position.getX() ;
    }

    public float getY() {
        return this.position.getY() ;
    }



    public abstract boolean checkObstacleColision(Coordinate obstacle);

    public abstract boolean checkTankColision(Coordinate c, double angle) ;
    
}