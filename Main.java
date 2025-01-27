package fr.cawar.serveur;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        int nbJoueur = 3;

        Map map = new Map();
        map.generateMapData();
        map.displayMapData();

        ArrayList<Tank> tankList = new ArrayList<>();
        ArrayList<KeyTimePair> listKeyTime = new ArrayList<>();
        ArrayList<GameThread> threads = new ArrayList<>();

        ArrayList<Coordinate> spawnPoints = map.findSpawnPoint(nbJoueur);

        // Crée des tanks
        for (int i = 0; i < nbJoueur; i++) {
            Tank t = new Tank(spawnPoints.get(i), IdGenerator.getInstance().getNextTankId());
            tankList.add(t);
            t.updateAtomic();
        }

        // Simule les entrées pour les tanks
        for (int i = 0; i < nbJoueur; i++) {
            listKeyTime.add(new KeyTimePair(Constants.up, 5000.0));
            listKeyTime.add(new KeyTimePair(Constants.right, 2000.0));
            listKeyTime.add(new KeyTimePair(Constants.down, 3000.0));
            listKeyTime.add(new KeyTimePair(Constants.left, 1000.0));
            listKeyTime.add(new KeyTimePair(Constants.up, 4000.0));
            listKeyTime.add(new KeyTimePair(Constants.right, 3000.0));
            listKeyTime.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTime.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTime.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTime.add(new KeyTimePair(Constants.shoot, 0));
            listKeyTime.add(new KeyTimePair(Constants.shoot, 0));
        }

        GameState gameState = new GameState(map.getObstacleMap(), tankList);

        // Créer et démarrer les threads
        for (int i = 0; i < nbJoueur; i++) {
            GameThread gameThread = new GameThread(gameState, tankList.get(i), listKeyTime);
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

        // Boucle qui appelle GameState.process et affiche les données atomiques
        for (int i = 0; i < nbJoueur; i++) {
            gameState.process(tankList.get(i), listKeyTime);
            gameState.printAtomData();
        }

        System.out.println("Vérification des collisions terminée.");
    }
}
