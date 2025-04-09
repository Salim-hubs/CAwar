package fr.cawar.serveur;
import java.awt.DisplayMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Tank {
    // Etat
    private int score ;
    private int id ;
    private AtomEntity atomHp ; // ATOMIQUE

    // Calcule position
    private Coordinate spawnPoint ;
    private ArrayList<KeyTimePair> key_time ;

    // Position
    private Coordinate position ;
    private double angle ;
    private AtomEntity atomPos ; // ATOMIQUE
    private AtomEntity atomAngle ; // ATOMIQUE


    // Tirs
    private List<Bullet> atomBullet = new CopyOnWriteArrayList<>(); // ATOMIQUE
    private long lastShoot;


    public Tank(Coordinate c) {
        this.spawnPoint = c ;
        this.position = c ;
        this.angle = 0 ;
        this.id = IdGenerator.getInstance().getNextTankId();
        this.score = 0 ;
        this.atomBullet = new CopyOnWriteArrayList<>();
        this.lastShoot = System.currentTimeMillis(); // Temps actuel en millisecondes
        this.key_time = new ArrayList<KeyTimePair>() ;

        this.atomPos = new AtomEntity(this.position) ;
        this.atomAngle = new AtomEntity(this.angle) ;
        this.atomHp = new AtomEntity(Constants.healthTank) ; 
    }

    public void handleBulletExpiration() {
        for (Bullet b : atomBullet) {
            if(b.isValid()) {
                this.deleteBullet(b); // Supprime la balle si le temps est écoulé
            }
        }
    }


    public List<Bullet> getBulletList() {
        return atomBullet; // Retourne directement la liste thread-safe
    }

    public void updateAtomicBullet() {
        for (Bullet b : atomBullet) {
            b.updateAtomic() ;
        }
    }


    public void deleteBullet(Bullet b) {
        if (!atomBullet.remove(b)) {
            System.out.println("Tir non trouvé dans la liste ! Tank :" + this.id);
        }
    }




    public void setKeyTime(ArrayList<KeyTimePair> key_time) {
        this.key_time = key_time ;
    }

    public Coordinate getAtomPos(){
        return this.atomPos.getCoordinate() ;
    }

    public double getAtomAngle() {
        return this.atomAngle.getAngle() ;
    }

    public double getAtomHp() {
        return this.atomHp.getAngle() ;
    }

    public int gotShoot() {
        double currentHealth = this.atomHp.getAngle(); // Obtient la valeur stoquer dans un long
        double newHealth = currentHealth - Constants.damageBullet;
        this.atomHp.setAngle(newHealth);
        return (int) newHealth; // Return the new health value
    }



    public void updateAtomic() {
        this.atomAngle.setAngle((double) this.angle);
        this.atomPos.setCoordinate(this.position) ;
    }

    public Coordinate getSpawnPoint() {
        return this.spawnPoint ;
    }


    public void incrementScore() {
        this.score++ ;
    }

    public Coordinate getPosition() {
        return this.position.getCoordinate() ;
    }



    public void setPosition(Coordinate c) {
        this.position = c ;
    }

    public void respawn() {
        this.setPosition(this.spawnPoint) ;
    }
    public int getId() {
        return this.id ;
    }

    public double getAngle() {
        return this.angle ;
    }


// A CORRIGER
    public Coordinate simulatePosition() {
        Coordinate beforeSimulation = new Coordinate(this.position.getX(), this.position.getY());
        for (KeyTimePair couple : this.key_time) {
            switch (couple.getKey()) {
                /* Update position */
                case Constants.up:
                    this.moveForward(Constants.tic) ;
                    System.out.println("Moove UP ! Tank : " + this.id);
                    break;
                case Constants.down:
                    this.moveBackward(Constants.tic);
                    System.out.println("Moove DOWN ! Tank : " + this.id);
                    break;

                /* Update angle */
                case Constants.left:
                    this.turnLeft(Constants.tic);
                    System.out.println("Rotate LEFT ! Tank : " + this.id);
                    break;
                case Constants.right:
                    this.turnRight(Constants.tic);
                    System.out.println("Rotate RIGHT ! Tank : " + this.id);
                    break;

                /* Shoot */
                case Constants.shoot:
                    this.shoot(couple.getTime());
                    System.out.println("Shoot ! Tank : " + this.id);
                    break;
            }
        }

        // Clamer la position finale après tous les mouvements
        this.position = Constants.clampCoordinate(this.position);
        return beforeSimulation;
    }

    public void moveForward(double time) {
        float posX = this.position.getX();
        float posY = this.position.getY();
        posX += Constants.speedTank * time * Math.cos(this.angle);
        posY += Constants.speedTank * time * Math.sin(this.angle);
        this.setPosition(Constants.clampCoordinate(new Coordinate(posX, posY)));
    }

    public void moveBackward(double time) {
        float posX = this.position.getX();
        float posY = this.position.getY();
        posX -= Constants.speedTank * time * Math.cos(this.angle);
        posY -= Constants.speedTank * time * Math.sin(this.angle);
        this.setPosition(Constants.clampCoordinate(new Coordinate(posX, posY)));
    }


    
    public void turnLeft(double time) {
        double angle = this.angle ;
        angle += Constants.speedAngular * time ;
        angle = (angle + 2 * Math.PI) % (2 * Math.PI); // Normalisation
        this.angle = angle ;
    }

    public void turnRight(double time) {
        double angle = this.angle ;
        angle -= Constants.speedAngular * time ;
        angle = (angle + 2 * Math.PI) % (2 * Math.PI); // Normalisation
        this.angle = angle ;
    }

    public void shoot(double angle) {
        long atm = System.currentTimeMillis(); // Temps actuel en millisecondes
        if(atm - this.lastShoot > Constants.cooldown) {
            Bullet shoot = new Bullet(angle, this.getPosition());
            this.atomBullet.add(shoot) ;
            this.lastShoot = atm ;
            
        }
        else {
            System.out.println("Attendre cooldown Tank : " + this.id + " !");
        }
    }


}