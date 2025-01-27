package fr.cawar.serveur;

import java.util.ArrayList;

public class GameThread extends Thread {
    private GameState gameState;
    private Tank tank;
    private ArrayList<KeyTimePair> keyTimePairs;

    public GameThread(GameState gameState, Tank tank, ArrayList<KeyTimePair> keyTimePairs) {
        this.gameState = gameState;
        this.tank = tank;
        this.keyTimePairs = keyTimePairs;
    }

    @Override
    public void run() {
        System.out.println("Thread démarré pour le tank : " + tank.getId());
        gameState.process(tank, keyTimePairs);
        System.out.println("Thread terminé pour le tank : " + tank.getId());

        // Print positions of all tanks
        for (Tank t : gameState.getTankList()) {
            System.out.println("Tank ID: " + t.getId() + " Position: " + t.getPosition().toString());
        }

        // Print positions of all bullets
        for (Tank t : gameState.getTankList()) {
            for (Bullet b : t.getBulletList()) {
                if (b != null) {
                    System.out.println("Bullet ID: " + b.getId() + " Position: " + b.getPosition().toString());
                }
            }
        }
    }

    public static void main(String[] args) {
        int nbJoueur = 3;

        Map map = new Map();
        map.generateMapData();
        map.displayMapData();

        ArrayList<Tank> tankList = new ArrayList<>();
        ArrayList<KeyTimePair> listKeyTme = new ArrayList<>();
        ArrayList<GameThread> threads = new ArrayList<>();

        ArrayList<Coordinate> spawnPoints = map.findSpawnPoint(nbJoueur);

        // Crée des tanks
        for (int i = 0; i < nbJoueur; i++) {
            Tank t = new Tank(spawnPoints.get(i), IdGenerator.getInstance().getNextTankId());
            tankList.add(t);
            t.updateAtomic() ;
            System.out.println(t.getPosition().toString());
        }

        // Simule les entrées pour les tanks
        for (int i = 0; i < nbJoueur; i++) {
            listKeyTme.add(new KeyTimePair(Constants.up, 5000.0));
            listKeyTme.add(new KeyTimePair(Constants.right, 2000.0));
            listKeyTme.add(new KeyTimePair(Constants.down, 3000.0));
            listKeyTme.add(new KeyTimePair(Constants.left, 1000.0));
            listKeyTme.add(new KeyTimePair(Constants.up, 4000.0));
            listKeyTme.add(new KeyTimePair(Constants.right, 3000.0));
            listKeyTme.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTme.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTme.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTme.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTme.add(new KeyTimePair(Constants.shoot, 0));
        }

        GameState gameState = new GameState(map.getObstacleMap(), tankList);

        // Créer et démarrer les threads
        for (int i = 0; i < nbJoueur; i++) {
            GameThread gameThread = new GameThread(gameState, tankList.get(i), listKeyTme);
            threads.add(gameThread);
            gameThread.start();
        }
        

        // Attendre la fin de tous les threads
        for (GameThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Vérification des collisions terminée.");
    }
}
