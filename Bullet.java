package fr.cawar.serveur;

public class Bullet {
    private Coordinate initCoord;
    private HitboxBullet hitbox;
    private long timeWhenThrown;  // Temps en millisecondes depuis 1970
    private double angleShoot;
    private int idBullet;
    private AtomEntity atomPos;

    public Bullet(double angle, Coordinate initCoord) {
        this.angleShoot = angle; // Radians
        this.initCoord = initCoord;
        this.hitbox = new HitboxBullet(new Coordinate(initCoord.x, initCoord.y));  // Initialisation de hitbox
        this.timeWhenThrown = System.currentTimeMillis(); // Temps actuel en millisecondes
        this.idBullet = IdGenerator.getInstance().getNextBulletId();
        this.atomPos = new AtomEntity(initCoord);
    }

    public void updateAtomic() {
        this.atomPos.setCoordinate(this.hitbox.getPosition());
    }

    public int getId() {
        return this.idBullet;
    }

    public double getAngleShoot() {
        return this.angleShoot;
    }

    public Coordinate getPosition() {
        return this.hitbox.getPosition();
    }

    public void setPosition(Coordinate c) {
        this.hitbox.setPosition(c);
    }

    public void updatePosition() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - this.timeWhenThrown;
        if (elapsedTime > Constants.ttlBullet) {
            // Appeler une méthode pour gérer la fin de vie du projectile
            handleBulletExpiration();
        } else {
            float positionX = (float) (this.initCoord.getX() + Constants.speedBullet * elapsedTime * Math.cos(angleShoot));
            float positionY = (float) (this.initCoord.getY() + Constants.speedBullet * elapsedTime * Math.sin(angleShoot));
            
            // Clamer les coordonnées avant de les définir dans la hitbox
            this.hitbox.setPosition(Constants.clampCoordinate(new Coordinate(positionX, positionY)));
        }
    }

    public boolean isNotValid() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - this.timeWhenThrown;
        return elapsedTime >= Constants.ttlBullet; // Retourne vrai si la balle est valid
    }

    public boolean checkTankColision(Coordinate position, double angle) {
        return this.hitbox.checkTankColision(position, angle);
    }

    public boolean checkObstacleColision(Coordinate obstacle) {
        return this.hitbox.checkObstacleColision(obstacle);
    }

    private void handleBulletExpiration() {
        // Nettoyer les ressources ici, si nécessaire
        System.out.println("Bullet " + this.idBullet + " from tank has expired and is being cleaned up");
    }
}