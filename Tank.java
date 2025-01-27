package fr.cawar.serveur;
import java.util.ArrayList;



public class Tank {
    private HitboxTank hitbox ;
    private int hp ;
    private int id ;
    private int score ;
    private Coordinate spawnPoint ;
    private Bullet[] listBullet ;
    private ArrayList<Bullet> deletedBullet ;

    private ArrayList<KeyTimePair> key_time ;

    private AtomEntity atomPos ;
    private AtomEntity atomAngle ;
    private AtomEntity atomHp ;

    

    public Tank(Coordinate c, int id) {
        this.spawnPoint = c ;
        this.hitbox = new HitboxTank(c, 0) ;
        this.hp = Constants.healthTank ;
        this.id = id ;
        this.score = 0 ;
        this.listBullet = new Bullet[Constants.MAX_SHOOT]; 

        this.key_time = new ArrayList<KeyTimePair>() ;

        this.atomPos = new AtomEntity(c) ;
        this.atomAngle = new AtomEntity(this.hitbox.getAngle()) ;
        this.atomHp = new AtomEntity(Constants.healthTank) ; 
    }


    public void shoot(double angle) {
        Bullet shoot = new Bullet(angle, this.getPosition());
        // Chercher le premier emplacement disponible (null) dans le tableau
        for (int i = 0; i < Constants.MAX_SHOOT; i++) {
            if (this.listBullet[i] == null) {
                this.listBullet[i] = shoot;
                return;
            }
        }
        // Si aucun emplacement disponible, afficher un message
        System.out.println("Pas d'espace disponible pour un nouveau tir !");
    }

    public Bullet[] getBulletList() {
        return this.listBullet;
    }

    public void deleteBullet(Bullet b) {
        // Parcourir le tableau pour trouver et supprimer la balle
        for (int i = 0; i < Constants.MAX_SHOOT; i++) {
            if (this.listBullet[i] == b) {
                this.listBullet[i] = null; // Supprimer en remplaçant par null
                return;
            }
        }
        System.out.println("Tir non trouvé dans la liste !");
    }





    public void setKeyTime(ArrayList<KeyTimePair> key_time) {
        this.key_time = key_time ;
    }

    public Coordinate getAtomPos(){
        return this.atomPos.getCoordinate() ;
    }

    public long getAtomAngle() {
        return this.atomAngle.getAngle() ;
    }

    public long getAtomHp() {
        return this.atomHp.getAngle() ;
    }

    public int gotShoot() {
        long currentHealth = this.atomHp.getAngle();
        long newHealth = currentHealth - Constants.damageBullet;
        this.atomHp.setAngle(newHealth);
        return (int) newHealth; // Return the new health value
    }



    public void updateAtomic() {
        this.atomAngle.setAngle((long) this.hitbox.getAngle());
        this.atomPos.setCoordinate(this.hitbox.getPosition()) ;
    }

    public Coordinate getSpawnPoint() {
        return this.spawnPoint ;
    }


    public boolean checkObstacleColision(Coordinate obstacle){
        return this.hitbox.checkObstacleColision(obstacle) ;
    }

    public boolean checkTankColision(Coordinate otherPosition, double angle){
        return this.hitbox.checkTankColision(otherPosition, angle) ;
    }

    public void incrementScore() {
        this.score++ ;
    }

    public Coordinate getPosition() {
        return this.hitbox.getPosition() ;
    }



    public void setPosition(Coordinate c) {
        this.hitbox.setPosition(c) ;
    }

    public void respawn() {
        this.setPosition(this.spawnPoint) ;
    }

    public Coordinate simulatePosition() {
        Coordinate beforeSimulation = this.hitbox.getPosition();
        for (KeyTimePair couple : this.key_time) {
            switch (couple.getKey()) {
                /* Update position */
                case Constants.up:
                    this.moveForward(couple.getTime());
                    break;
                case Constants.down:
                    this.moveBackward(couple.getTime());
                    break;

                /* Update angle */
                case Constants.left:
                    this.turnLeft(couple.getTime());
                    break;
                case Constants.right:
                    this.turnRight(couple.getTime());
                    break;

                /* Shoot */
                case Constants.shoot:
                    this.shoot(couple.getTime());
                    break;
            }
        }

        // Clamer la position finale après tous les mouvements
        this.hitbox.setPosition(Constants.clampCoordinate(this.hitbox.getPosition()));
        System.out.println("Tank ID: " + this.id + " final position after simulation: " + this.hitbox.getPosition().toString());
        return beforeSimulation;
    }

    public void moveForward(double time) {
        float posX = this.hitbox.getX();
        float posY = this.hitbox.getY();
        posX += Constants.speedTank * time * Math.cos(this.hitbox.getAngle());
        posY += Constants.speedTank * time * Math.sin(this.hitbox.getAngle());
        this.hitbox.setPosition(Constants.clampCoordinate(new Coordinate(posX, posY)));
        System.out.println("Tank ID: " + this.id + " moved forward to position: " + this.hitbox.getPosition().toString());
    }

    public void moveBackward(double time) {
        float posX = this.hitbox.getX();
        float posY = this.hitbox.getY();
        posX -= Constants.speedTank * time * Math.cos(this.hitbox.getAngle());
        posY -= Constants.speedTank * time * Math.sin(this.hitbox.getAngle());
        this.hitbox.setPosition(Constants.clampCoordinate(new Coordinate(posX, posY)));
        System.out.println("Tank ID: " + this.id + " moved backward to position: " + this.hitbox.getPosition().toString());
    }


    
    public void turnLeft(double time) {
        double angle = this.hitbox.getAngle() ;
        angle += Constants.speedAngular * time ;
        angle = (angle + 2 * Math.PI) % (2 * Math.PI); // Normalisation
        this.hitbox.setAngle(angle) ;
        System.out.println("Tank ID: " + this.id + " turned left to angle: " + this.hitbox.getAngle());
    }

    public void turnRight(double time) {
        double angle = this.hitbox.getAngle() ;
        angle -= Constants.speedAngular * time ;
        angle = (angle + 2 * Math.PI) % (2 * Math.PI); // Normalisation
        this.hitbox.setAngle(angle) ;
        System.out.println("Tank ID: " + this.id + " turned right to angle: " + this.hitbox.getAngle());
    }

    public int getId() {
        return this.id ;
    }

    public double getAngle() {
        return this.hitbox.getAngle() ;
    }


/*
    public static void main(String[] args) {
        // Initialisation de la position initiale du tank
        Coordinate spawnPoint = new Coordinate(50, 50);

        // Création d'un tank avec un ID généré
        Tank tank = new Tank(spawnPoint, IdGenerator.getInstance().getNextTankId());
        System.out.println("Tank créé avec ID : " + tank.getId());

        // Tirer quelques balles
        System.out.println("Le tank commence à tirer des balles...");
        tank.shoot(Math.PI / 4); // Tirer à un angle de 45 degrés
        tank.shoot(Math.PI / 2); // Tirer à un angle de 90 degrés
        tank.shoot(Math.PI);     // Tirer à un angle de 180 degrés

        // Afficher les informations sur les balles tirées
        ArrayList<Bullet> bullets = tank.getBulletList();
        System.out.println("Balles tirées : ");
        for (Bullet bullet : bullets) {
            System.out.println("- ID : " + bullet.getId() +
                               ", Angle : " + bullet.getAngleShoot() +
                               ", Tiré par : " + bullet.getIdShooter());
        }

        for (Bullet bullet : bullets) {
            bullet.updatePosition() ;
            System.out.println("Balle id " + bullet.getId() + " Position : " + bullet.getPosition().toString()) ;
        }

        // Exemple de suppression de balle
        if (!bullets.isEmpty()) {
            Bullet toDelete = bullets.get(0);
            tank.deleteBullet(toDelete);
            System.out.println("Balle supprimée : ID " + toDelete.getId());
        }

        // Afficher la liste des balles supprimées
        ArrayList<Bullet> deletedBullets = tank.getDeletedBulletList();
        System.out.println("Balles supprimées : ");
        for (Bullet bullet : deletedBullets) {
            System.out.println("- ID : " + bullet.getId());
        }

        // Liste des mouvements du tank (exemple)
        ArrayList<KeyTimePair> keyTimeList = new ArrayList<>();
        keyTimeList.add(new KeyTimePair(Constants.up, 10000.0));  // Se déplacer vers le haut pendant 10 secondes
        keyTimeList.add(new KeyTimePair(Constants.left, 50));  // Tourner à gauche pendant 0.5 seconde
        keyTimeList.add(new KeyTimePair(Constants.up, 10000.0));  // Se déplacer vers le bas pendant 10 secondes

        // Simulation du mouvement et affichage de la position avant/après
        Coordinate beforePosition = tank.simulatePosition(keyTimeList);
        System.out.println("Position avant simulation : " + beforePosition);
        System.out.println("Position après simulation : " + tank.getPosition().toString());
    }

*/
}



