package fr.cawar.serveur;


public class KeyTimePair {
    private String key;
    private double time;

    public KeyTimePair(String key, double time) {
        this.key = key;
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public double getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Key: " + key + ", Time: " + time;
    }
}
