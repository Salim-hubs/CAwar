package fr.cawar.serveur;

import java.util.ArrayList;
import java.util.HashMap;

public class Map {
    private int length;
    private int[][] mapData;
    private HashMap<Integer, ArrayList<Coordinate>> obstacleList = new HashMap<>();

    // Constructeur
    public Map() {
        this.length = Constants.unitInChunk * Constants.nbChunk;
        this.mapData = new int[this.length][this.length];
    }

    public int getLenght() {
        return this.length;
    }
    

    // Génération de la map et des listes d'obstacles
    public void generateMapData() {
        System.out.print("Generate Map\n");
        System.out.print("Generate list of obstacles\n");
        for (int iChunk = 0; iChunk < this.length / Constants.unitInChunk; iChunk++) {
            for (int jChunk = 0; jChunk < this.length / Constants.unitInChunk; jChunk++) {
                int startI = iChunk * Constants.unitInChunk;
                int startJ = jChunk * Constants.unitInChunk;

                for (int i = startI + 1; i < startI + Constants.unitInChunk - 1; i++) {
                    for (int j = startJ + 1; j < startJ + Constants.unitInChunk - 1; j++) {
                        double randomValue = Math.random();
                        if (randomValue <= Constants.percentObstacle) {
                            this.mapData[i][j] = 1;
                            int vectorIndex = Constants.getVectorIndex(iChunk, jChunk);
                            this.addHashMap(vectorIndex, j * Constants.unit, i * Constants.unit);
                        }
                    }
                }
            }
        }
    }
    
    public ArrayList<Coordinate> getObstacleList(int chunk) {
        return this.obstacleList.get(chunk);
    }

    
    // Ajoute une coordonné (x, y) en fonction de la clé chunk
    public void addHashMap(int chunk, float x, float y) {
        if (obstacleList.containsKey(chunk)) { // Si la clé du chunk est déjà instancier
            obstacleList.get(chunk).add(new Coordinate(x, y));
        } else {
            ArrayList<Coordinate> listCoord = new ArrayList<>();
            listCoord.add(new Coordinate(x, y));
            obstacleList.put(chunk, listCoord);
        }
    }

    public HashMap<Integer, ArrayList<Coordinate>> getObstacleMap() {
        return obstacleList;
    }
        
    // Trouve un nombre de points de spawn valide
    public ArrayList<Coordinate> findSpawnPoint(int nbPoint) {
        ArrayList<Coordinate> spawnPoints = new ArrayList<>();
        if (mapData == null || mapData.length == 0 || mapData[0].length == 0) return spawnPoints;

        int rows = mapData.length;
        int cols = mapData[0].length;

        int border = 3;
        // Parcourir la matrice pour trouver des emplacements vides
        for (int i = 1; i <= rows - Constants.heightTank - border; i++) {
            for (int j = 1; j <= cols - Constants.widthTank - border; j++) {
                if (isAreaEmpty(i, j, Constants.widthTank + border, Constants.heightTank + border)) {
                    spawnPoints.add(new Coordinate(j, i)); // Ajoute le coin supérieur gauche de l'emplacement vide
                    
                    // Marque la zone comme occupée
                    markArea(i, j, Constants.widthTank + border, Constants.heightTank + border);
                    
                    if (spawnPoints.size() == nbPoint) {
                        return spawnPoints;
                    }
                }
            }
        }

        return spawnPoints;
    }

    // Vérifie si la zone spécifiée dans mapData est vide (tous les éléments sont 0)
    private boolean isAreaEmpty(int startRow, int startCol, float width, float height) {
        for (int i = startRow; i < startRow + height; i++) {
            for (int j = startCol; j < startCol + width; j++) {
                if (mapData[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // Marque la zone spécifiée comme occupée en la remplissant avec une valeur non nulle
    private void markArea(int startRow, int startCol, float width, float height) {
        for (int i = startRow; i < startRow + height; i++) {
            for (int j = startCol; j < startCol + width; j++) {
                mapData[i][j] = -1; // Remplit la zone avec une valeur non nulle pour indiquer qu'elle est occupée
            }
        }
    }


    public void displayMapData() {
        for (int i = 0; i < this.length; i++) {
            for (int j = 0; j < this.length; j++) {
                System.out.print(mapData[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void displayObstacleList() {
        System.out.print("Liste d'obstacles\n");
        for (int chunk = 0; chunk < Constants.nbChunk * Constants.nbChunk; chunk++) {
            System.out.print(chunk + " : ");
            if (obstacleList.containsKey(chunk)) {
                for (Coordinate c : obstacleList.get(chunk)) {
                    System.out.print(c.toString());
                }
            }
            System.out.println();
        }
    }

    public void displayPercentObstacle() {
        float percent = 0;
        for(int i=0; i<this.length; i++){
            for(int j=0; j<this.length; j++){
                if(this.mapData[i][j] == 1)
                    percent++ ;
            }
        }
        percent = percent/(this.length*this.length) ;
        System.out.print("Pourcentage d'obstacles : ");
        System.out.print(percent);
    }

    // Fonction pour afficher la liste des points de spawn
    public static void displaySpawnPoints(ArrayList<Coordinate> spawnPoints) {
        System.out.print("\nSpawn point\n");
        for (Coordinate sp : spawnPoints) {
            System.out.print(sp.toString());
        }
    }


    public static void main(String[] args) {
        Map map = new Map();
        map.generateMapData();
        map.displayMapData();
        map.displayObstacleList();
        map.displayPercentObstacle();
        ArrayList<Coordinate> spawnPoints = map.findSpawnPoint(2);
        displaySpawnPoints(spawnPoints);
        for (Coordinate sp : spawnPoints) {
            System.out.println("Generated spawn point: " + sp);
        }
    }


}
