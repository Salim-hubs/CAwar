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
        this.coord = new AtomicLong((long) angle);
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

    public void setAngle(long phi){
        coord.set(phi) ;
    }

    public long getAngle() {
        return coord.get() ;
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
    // Test des valeurs
    float xTest = 1.5f;
    float yTest = 2.5f;

    // Encodage
    long encoded = encode(xTest, yTest);
    System.out.println("Encoded value: " + encoded);

    // Décodage
    Coordinate decoded = decode(encoded);
    System.out.println("Decoded x: " + decoded.getX());
    System.out.println("Decoded y: " + decoded.getY());
}

    
}
