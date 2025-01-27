package fr.cawar.serveur;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private AtomicInteger idTank;
    private AtomicInteger idBullet;

    // Instance unique de la classe
    private static final IdGenerator instance = new IdGenerator();

    // Constructeur privé pour empêcher l'instanciation directe
    private IdGenerator() {
        this.idTank = new AtomicInteger(1);  // Initialisation de l'ID de tank
        this.idBullet = new AtomicInteger(1); // Initialisation de l'ID de balle
    }

    // Méthode pour obtenir l'instance unique
    public static IdGenerator getInstance() {
        return instance;
    }

    // Méthode pour obtenir le prochain ID de tank
    public int getNextTankId() {
        return idTank.getAndIncrement();
    }

    // Méthode pour obtenir le prochain ID de balle
    public int getNextBulletId() {
        return idBullet.getAndIncrement();
    }
}
