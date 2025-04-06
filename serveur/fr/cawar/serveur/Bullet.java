package fr.cawar.serveur;

public class Bullet {
    // Calcule position
    private Coordinate initCoord;
    private double angleShoot;
    private float timeWhenThrown;  // Nombre de tics

    // Position
    private Coordinate position ;
    private AtomEntity atomPos;

    // id
    private int idBullet ;



    public Bullet(double angle, Coordinate initCoord) {
        this.angleShoot = angle; // Radians
        this.initCoord = initCoord;
        this.timeWhenThrown = (int) Constants.ttlBullet ;
        this.idBullet = IdGenerator.getInstance().getNextBulletId();
        this.position = initCoord ;
        this.atomPos = new AtomEntity(initCoord);
    }

    public float getTimeWhenThrown() {
        return this.timeWhenThrown;
    }   

    public int getId() {
        return this.idBullet ;
    }

    public void updateAtomic() {
        this.atomPos.setCoordinate(this.position);
    }

    public Coordinate getAtomicPosition() {
        return this.atomPos.getCoordinate() ;
    }

    public double getAngleShoot() {
        return this.angleShoot;
    }

    public Coordinate getPosition() {
        return this.position.getCoordinate() ;
    }

    public void setPosition(Coordinate c) {
        this.position = c ;
    }

    public void updatePosition() {
        if (this.timeWhenThrown > 0) {
            float positionX = (float) (this.position.getX() + Constants.speedBullet * Constants.tic * Math.cos(angleShoot));
            float positionY = (float) (this.position.getY() + Constants.speedBullet * Constants.tic * Math.sin(angleShoot));
            
            // Clamer les coordonnées avant de les définir dans la hitbox
            this.position = Constants.clampCoordinate(new Coordinate(positionX, positionY));
        }
        this.timeWhenThrown-- ;
    }

    public boolean isValid() { // Retourne vrai si la balle est valid
        return this.timeWhenThrown > 0 ;
    }

    public static void main(String[] args) throws InterruptedException  {
    Bullet b1 = new Bullet(Math.PI/4, new Coordinate(10, 10)) ;
    System.out.println(b1.getPosition()) ;
    Bullet b2 = new Bullet(3*Math.PI/4, new Coordinate(10, 10)) ;
    System.out.println(b1.getPosition()) ;
    Bullet b3 = new Bullet(-Math.PI/4, new Coordinate(10, 10)) ;
    System.out.println(b1.getPosition()) ;
    Bullet b4 = new Bullet(-3*Math.PI/4, new Coordinate(10, 10)) ;
    System.out.println(b1.getPosition()) ;
    Thread.sleep(2000) ;
    b1.updatePosition() ;
    b2.updatePosition() ;
    b3.updatePosition() ;
    b4.updatePosition() ;
    System.out.println(b1.getPosition()) ;
    System.out.println(b2.getPosition()) ;
    System.out.println(b3.getPosition()) ;
    System.out.println(b4.getPosition()) ;

    
    }
    
}