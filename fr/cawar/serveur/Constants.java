package fr.cawar.serveur;
import java.util.ArrayList;


public class Constants {
    /* ----- Serveur ----- */
    public static final int nbMaxPlayer = 10000; // Nombre maximum de joueur
    public static final int port = 80 ;
    public static final String ip = "127.0.0.1" ;

    /* ----- MAP ----- */
    public static final int nbChunk = 20;
    public static final float unit = 1;
    public static final int unitInChunk = 10; 
    public static final float lengthMap = unit * unitInChunk * nbChunk;
    public static final float percentObstacleTheoric = 0.025f;
    public static final float percentObstacle = (float) (
    (percentObstacleTheoric * Math.pow(nbChunk * unitInChunk, 2)) / 
    (Math.pow(unitInChunk - 2, 2) * Math.pow(nbChunk, 2)) 
    );

    /* ----- Game variable ----- */
    // Tic
    public static final float ticSeconde = 10.0f ; // Nombre de tic par secondes
    public static final float tic = 1000f/Constants.ticSeconde; // Durée d'un tic

    // Speed 
    public static final float baseSpeed = 0.005f;
    public static final float speedBullet = 1f * Constants.baseSpeed;
    public static final float speedTank = Constants.baseSpeed ; 
    public static final float speedAngularDegrees = 150.0f; // degré par seconde
    public static final float speedAngular = (float) Math.toRadians(speedAngularDegrees) / 1000; // Conversion en radians/ms

    // Time to live
    public static final double dtlBullet = 4f * Constants.unitInChunk ;
    // public static final double ttlBullet = Constants.dtlBullet / Constants.speedBullet ;
    public static final double ttlBullet = 1000;


    // Dimension
    public static final float widthTank = 2 * unit ; // 2 * unit = 2m
    public static final float heightTank = 3 * unit ; // 3 * unit = 3m
    public static final float radiusBullet = 0.5f * unit ;
    public static final double halfDiagonal = Math.sqrt((Constants.widthTank * Constants.widthTank) + (Constants.heightTank * Constants.heightTank)) / 2;
    
    // Damage
    public static final int healthTank = 100 ;
    public static final int damageBullet = 35 ;

    // Tank cooldown
    public static final int MAX_SHOOT = 10 ;
    public static final float cooldown = 200 ;


    // FOV
    public static final double FOV = Constants.unitInChunk * 5; // Champ de vision du tank;


    /* ----- Shape ----- */
    public static final String tankShape = "tankShape" ;
    public static final String bulletShape = "bulletShape" ;
    public static final String obstacleShape = "obstacleShape" ;

    /* ----- Keymap ----- */ 
    public static final String up = "Z" ;
    public static final String down = "S" ;
    public static final String left = "D" ;
    public static final String right = "Q" ;
    public static final String shoot = "Space" ;


    /* ----- Méthodes ----- */

    // Convertie un index chunk n en (x, y)
    public static Coordinate getMatriceIndex(int nChunk) {
        Coordinate coupleChunk = new Coordinate(0, 0);
        coupleChunk.x = nChunk / Constants.nbChunk;
        coupleChunk.y = nChunk % Constants.nbChunk;
        return coupleChunk;
    }


    // Convertie un index chunk (x, y) en n
    public static int getVectorIndex(int iChunk, int jChunk) {
        return jChunk * Constants.nbChunk + iChunk;
    }



    // Obtient le chunk depuis la coordonée
    public static int chunkFromCoord(Coordinate c) {
        int x = (int) (c.getX() / Constants.unitInChunk);
        int y = (int) (c.getY() / Constants.unitInChunk);
        int chunkIndex = getVectorIndex(x, y);
        
        // Ensure chunkIndex is within valid bounds
        if (chunkIndex < 0 || chunkIndex >= Constants.nbChunk * Constants.nbChunk) {
            // System.out.println("Problème chunkFromCoord") ;
            // System.out.println("Invalid chunk index calculated: " + chunkIndex);
            return -1; 
        }


        return chunkIndex;
    }


    public static double calculateDistance(Coordinate a, Coordinate b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }


    public boolean collisionBox(Coordinate box1, float box1W, float box1H, Coordinate box2, float box2W, float box2H) {
        return !(box2.getX() >= box1.getX() + box1W    // Trop à droite
            || box2.getX() + box2W <= box1.getX()   // Trop à gauche
            || box2.getY() >= box1.getY() + box1H   // Trop en bas
            || box2.getY() + box2H <= box1.getY()); // Trop en haut
    }

    
    public static Coordinate clampCoordinate(Coordinate c) {
        float clampedX = Math.max(0, Math.min(c.getX(), Constants.unitInChunk * Constants.nbChunk));
        float clampedY = Math.max(0, Math.min(c.getY(), Constants.unitInChunk * Constants.nbChunk));
        return new Coordinate(clampedX, clampedY);
    }




    public static ArrayList<Integer> getNeighbours(int index) {
        ArrayList<Integer> neighbours = new ArrayList<>();
        neighbours.add(index); 

        int width = Constants.nbChunk;
        int height = Constants.nbChunk;

        int x = index % width;
        int y = index / height;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {

                if (dx == 0 && dy == 0) continue; // skip la case centrale

                int nx = x + dx;
                int ny = y + dy;

                // Vérifie qu'on est dans la grille
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    int neighbourIndex = ny * width + nx;
                    neighbours.add(neighbourIndex);
                }
            }
        }

        return neighbours;
    }


    public static void main(String[] args) {
        for (int i = 0; i < Constants.nbChunk*Constants.unitInChunk; i++) {
            for (int j = 0; j < Constants.nbChunk*Constants.unitInChunk; j++) {
                Coordinate c = new Coordinate(i, j);
                int chunkIndex = Constants.chunkFromCoord(c);
                System.out.println("Chunk index for (" + i + ", " + j + "): " + chunkIndex);
            }
        }
            
    }
}
