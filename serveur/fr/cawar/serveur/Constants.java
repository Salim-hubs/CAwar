package fr.cawar.serveur;


public class Constants {
    /* ----- MAP ----- */
    public static final int nbChunk = 3; // Nombre de chunk sur une ligne
    public static final float unit = 1;
    public static final int unitInChunk = 10;
    public static final float lengthMap = unit * unitInChunk * nbChunk;
    public static final float percentObstacleTheoric = 0.15f;
    public static final float percentObstacle = (float) (
    (percentObstacleTheoric * Math.pow(nbChunk * unitInChunk, 2)) / 
    (Math.pow(unitInChunk - 2, 2) * Math.pow(nbChunk, 2)) 
    );

    /* ----- Game variable ----- */
    // Tic
    public static final float ticSeconde = 30.0f ; // Nombre de tic par secondes
    public static final float tic = 1000f/Constants.ticSeconde; // Durée d'un tic

    // Speed 
    public static final float baseSpeed = 0.0025f;
    public static final float speedBullet = 5 * Constants.baseSpeed; // 5 unités par seconde
    public static final float speedTank = Constants.baseSpeed ; 
    public static final float speedAngularDegrees = 90.0f; // degré par seconde
    public static final float speedAngular = (float) Math.toRadians(speedAngularDegrees) / 1000; // Conversion en radians/ms

    // Time to live
    public static final double dtlBullet = 1f * Constants.unitInChunk ;
    // Si on veut un ttl pour parcourir 1 chunk
    // public static final double ttlBullet = Constants.dtlBullet / Constants.speedBullet ;
    public static final double ttlBullet = 5 ;

    // Dimension
    public static final float widthTank = 1 * unit ;
    public static final float heightTank = 2 * unit ;
    public static final float radiusBullet = 0.5f * unit ;
    public static final double halfDiagonal = Math.sqrt((Constants.widthTank * Constants.widthTank) + (Constants.heightTank * Constants.heightTank)) / 2;
    
    // Damage
    public static final int healthTank = 100 ;
    public static final int damageBullet = 10 ;

    // Tank cooldown
    public static final int MAX_SHOOT = 10 ;
    public static final float cooldown = 2000 ;


    // FOV
    public static final double FOV = Constants.unitInChunk * 2.5 ;


    /* ----- Shape ----- */
    public static final String tankShape = "tankShape" ;
    public static final String bulletShape = "bulletShape" ;
    public static final String obstacleShape = "obstacleShape" ;

    /* ----- Keymap ----- */ 
    public static final String up = "Z" ;
    public static final String down = "S" ;
    public static final String left = "Q" ;
    public static final String right = "D" ;
    public static final String shoot = "R" ;


    /* ----- Méthodes ----- */
    // Convertie un index chunk (x, y) en n
    public static int getVectorIndex(int iChunk, int jChunk) {
        return jChunk * Constants.nbChunk + iChunk;
    }

    // Convertie un index chunk n en (x, y)
    public static Coordinate getMatriceIndex(int nChunk) {
        Coordinate coupleChunk = new Coordinate(0, 0);
        coupleChunk.x = nChunk / Constants.nbChunk;
        coupleChunk.y = nChunk % Constants.nbChunk;
        return coupleChunk;
    }

    // Obtient le chunk depuis la coordonée
    public static int chunkFromCoord(Coordinate c) {
        int x = (int) (c.getX() / Constants.unitInChunk);
        int y = (int) (c.getY() / Constants.unitInChunk);
        int chunkIndex = getVectorIndex(x, y);
        
        // Ensure chunkIndex is within valid bounds
        if (chunkIndex < 0 || chunkIndex >= Constants.nbChunk * Constants.nbChunk) {
            System.out.println("Problème chunkFromCoord") ;
            System.out.println("Invalid chunk index calculated: " + chunkIndex);
            return -1; // or handle the error appropriately
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


    
}
