package fr.cawar.serveur;

import java.util.concurrent.atomic.AtomicLong;

public class AtomEntity {
    private AtomicLong coord;


    // Constructeur
    public AtomEntity(Coordinate c) {
        this.coord = new AtomicLong(encode(c.getX(), c.getY()));
    }

    // Constructeur pour l'angle
    public AtomEntity(double angle) {
        this.coord = new AtomicLong(Double.doubleToRawLongBits(angle)); // Encode l'angle en long
    }



    // Méthode pour modifier la coordonnée
    public void setCoordinate(Coordinate c) {
        coord.set(encode(c.getX(), c.getY()));
    }

    // Méthode pour obtenir la coordonnée
    public Coordinate getCoordinate() {
        long value = coord.get();
        return decode(value);
    }


    // Méthode pour définir l'angle
    public void setAngle(double phi) {
        this.coord.set(Double.doubleToRawLongBits(phi)); // Encode le double en long
    }

    // Méthode pour obtenir l'angle
    public double getAngle() {
        return Double.longBitsToDouble(coord.get()); // Décode le long en double
    }


    // Méthode privée pour encoder deux floats dans un long
    private static long encode(float x, float y) {
        int xBits = Float.floatToIntBits(x); // Convertit float en bits int
        int yBits = Float.floatToIntBits(y); // Convertit float en bits int
        return ((long) xBits << 32) | (yBits & 0xFFFFFFFFL); // Combine les deux en un long
    }

    // Méthode privée pour décoder un long en deux floats
    private static Coordinate decode(long value) {
        int xBits = (int) (value >> 32); // Récupère les bits de la partie haute
        int yBits = (int) value; // Récupère les bits de la partie basse
        float x = Float.intBitsToFloat(xBits); // Convertit les bits en float
        float y = Float.intBitsToFloat(yBits); // Convertit les bits en float
        return new Coordinate(x, y);
    }


public static void main(String[] args) {
    AtomEntity a = new AtomEntity(new Coordinate(1.5f, 2f)) ;
    AtomEntity b = new AtomEntity(20) ;

    Coordinate cor = new Coordinate(2, 3) ;
    a.setCoordinate(cor) ;
    b.setAngle(10) ;

    System.out.println(a.getCoordinate()) ;
    System.out.println(b.getAngle()) ;
}

    
}
