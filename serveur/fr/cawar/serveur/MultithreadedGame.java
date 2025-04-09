package fr.cawar.serveur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadedGame {
    public static void main(String[] args) {
        // Initialisation de la carte et des obstacles
        Map map = new Map();
        map.generateMapData();
        HashMap<Integer, ArrayList<Coordinate>> obstacleMap = map.getObstacleMap();

        // Initialisation des points de spawn
        ArrayList<Coordinate> spawnPoints = map.findSpawnPoint(2);

        // Initialisation de GameState
        GameState gameState = new GameState(obstacleMap);

        // Création des tanks
        List<Tank> tanks = new ArrayList<>();
        for (int i = 0; i < spawnPoints.size(); i++) {
            Tank tank = new Tank(spawnPoints.get(i));
            tanks.add(tank);
            gameState.addPlayer(tank);
        }

        // Création d'un pool de threads
        ExecutorService executor = Executors.newFixedThreadPool(tanks.size());

        // Création d'une instance de Test pour récupérer les inputs
        Test test = new Test();
        test.startKeyListener();

        // Lancement des threads pour chaque tank
        for (Tank tank : tanks) {
            executor.submit(() -> {
                while (true) {
                    // Vérifie si la touche "P" est pressée
                    if (test.getPressedKeys().contains("P")) {
                        System.out.println("Touche 'P' détectée."); // Debug: Vérifie si la touche est détectée

                        // Récupération des inputs
                        ArrayList<KeyTimePair> inputs = new ArrayList<>(test.getKeyTimePairs());

                        // Processus de simulation pour le tank
                        String json = gameState.process(tank, inputs);

                        if (json != null && !json.isEmpty()) {
                            System.out.println("JSON généré : " + json); // Debug: Affiche le JSON généré
                        } else {
                            System.out.println("Aucun JSON généré ou JSON vide."); // Debug: Indique que le JSON est vide ou nul
                        }

                        // Affichage de la carte en tableau 2D
                        if (tank.getId() == 1)
                            printMapWithTanksAndObstacles(map, tanks);
                    }

                    // Pause pour limiter la fréquence des mises à jour
                    try {
                        Thread.sleep((long) (1000 / Constants.ticSeconde)); // 30 tics par seconde
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // Arrêt du pool de threads à la fin du jeu
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdownNow();
            System.out.println("Jeu terminé.");
        }));
    }

    private static void printMapWithTanksAndObstacles(Map map, List<Tank> tanks) {
        int width = map.getLenght();
        int height = map.getLenght();
        char[][] grid = new char[height][width];

        // Initialisation de la carte avec des espaces vides
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = '.'; // '.' représente une cellule vide
            }
        }

        // Placement des obstacles
        for (ArrayList<Coordinate> obstacleList : map.getObstacleMap().values()) {
            for (Coordinate coord : obstacleList) {
                grid[(int) coord.getY()][(int) coord.getX()] = '0'; // 'i' représente un obstacle
            }
        }

        // Placement des tanks
        for (Tank tank : tanks) {
            Coordinate pos = tank.getPosition();
            grid[(int) pos.getY()][(int) pos.getX()] = (char) ('0' + tank.getId()); // ID du tank
        }

        // Affichage de la carte avec bordures
        System.out.println("+" + "-".repeat(width * 2) + "+");
        for (char[] row : grid) {
            System.out.print("| ");
            for (char cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println("|");
        }
        System.out.println("+" + "-".repeat(width * 2) + "+");
    }
}