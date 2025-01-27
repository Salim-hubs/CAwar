package fr.cawar.serveur;

import java.util.ArrayList ;



public class HitboxBullet extends Hitbox {
    private double radius ;

    public HitboxBullet(Coordinate c){
        super(c) ;
        this.radius = Constants.radiusBullet ;
    }


    // Méthode pour vérifier la collision avec un carré non orienté
    @Override
    public boolean checkObstacleColision(Coordinate obstacle) {
        // Calculer les demi-longueurs des côtés du carré
        float halfSide = Constants.unit / 2;

        // Condition éliminatoire pour accélerer les calculs
        if(Constants.calculateDistance(this.position, obstacle) > halfSide*1.41 + this.radius) 
            return false ;

        // Trouver le point sur le carré le plus proche du centre du cercle
        float closestX = clamp(this.position.getX(), obstacle.getX() - halfSide, obstacle.getX() + halfSide);
        float closestY = clamp(this.position.getY(), obstacle.getY() - halfSide, obstacle.getY() + halfSide);

        // Calculer la distance entre ce point et le centre du cercle
        float distanceX = this.position.getX() - closestX;
        float distanceY = this.position.getY() - closestY;

        // Si la distance est inférieure au rayon du cercle, il y a collision
        return (distanceX * distanceX + distanceY * distanceY) < (this.radius * this.radius);
    }

    // Fonction pour limiter la valeur entre min et max
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }


    // Méthode pour vérifier la collision avec un rectangle orienté
    @Override
    public boolean checkTankColision(Coordinate position, double angle) {
        // Condition éliminatoire pour accélérer les calculs
        float maxdist = (float) Math.sqrt(Math.pow(Constants.heightTank / 2, 2) + Math.pow(Constants.widthTank / 2, 2));
        if (Constants.calculateDistance(this.position, position) > maxdist + this.radius) {
            return false;
        }

        // Convertir l'angle en radians
        double angleRadians = Math.toRadians(angle);

        // Utiliser les constantes pour les dimensions du rectangle
        float halfWidth = Constants.widthTank / 2;
        float halfHeight = Constants.heightTank / 2;

        // Calculer le point sur le rectangle le plus proche du centre du cercle
        // en tenant compte de l'orientation du rectangle
        float cosAngle = (float) Math.cos(-angleRadians);
        float sinAngle = (float) Math.sin(-angleRadians);

        // Coordonnées du cercle par rapport au centre du rectangle
        float circleXfromRect = this.position.getX() - position.getX();
        float circleYfromRect = this.position.getY() - position.getY();

        // Coordonnées du cercle dans le système de coordonnées aligné avec le rectangle
        float alignedCircleX = cosAngle * circleXfromRect - sinAngle * circleYfromRect;
        float alignedCircleY = sinAngle * circleXfromRect + cosAngle * circleYfromRect;

        // Trouver le point le plus proche sur le rectangle aligné
        float closestX = clamp(alignedCircleX, -halfWidth, halfWidth);
        float closestY = clamp(alignedCircleY, -halfHeight, halfHeight);

        // Calculer la distance entre ce point et le centre du cercle dans le système aligné
        float distanceX = alignedCircleX - closestX;
        float distanceY = alignedCircleY - closestY;

        // Si la distance est inférieure au rayon du cercle, il y a collision
        return (distanceX * distanceX + distanceY * distanceY) < (this.radius * this.radius);
    }
}

/*
    public static void main(String[] args) {

        Coordinate bullet = new Coordinate(0.4f, 0.04f) ;
        Coordinate o = new Coordinate(1, 1) ;

        HitboxBullet b = new HitboxBullet(bullet) ;
        System.out.println("Distance max colision : " + (Math.sqrt(2) * (Constants.unit/2) + Constants.radiusBullet/2)) ;
        System.out.println("Distance 2 centres : " + Constants.calculateDistance(bullet, o)) ;
        System.out.println(b.checkObstacleColision(o)) ;

        System.out.println("\nCollision tank bullet\n") ;    

        // Exemple de test
        HitboxBullet bulletT = new HitboxBullet(new Coordinate(1.1f, 0.67f)); // Cercle à (3, 4) avec rayon unit
        float squareX = 2f; // Centre du rectangle
        float squareY = 2f;
        float angle = 0f; // Rectangle orienté 


        // Vérification de la collision
        boolean isCollision = bulletT.checkTankColision(squareX, squareY, angle);

        // Résultat
        if (isCollision) {
            System.out.println("Collision détectée !");
        } else {
            System.out.println("Pas de collision.");
        }

    }
    

}
*/



/*
    // Calcule si un point est dans le cercle
    private boolean colisionPointCercle(Coordinate point) {
        double dx = point.getX() - position.getX();
        double dy = point.getY() - position.getY();
        double distanceSquared = dx * dx + dy * dy;

        return distanceSquared <= radius * radius;
    }

    public boolean checkObstacleColision(Coordinate c) {
        if(calculateDistance(this.position, c) > Math.sqrt(2) * (Constants.unit/2) + Constants.radiusBullet)
            return false ; // Cas ou il y a forcément pas colisions


        if(calculateDistance(this.position, c) < Constants.unit/2 + Constants.radiusBullet)
            return true ; // Cas ou il y a forcément colisions

        if(Constants.collisionBox(this.position, Constants.radiusBullet, Constants.radiusBullet, c, Constants.unit, Constants.unit) == 0)
            return false ; // Premier test

        double unit = Constants.unit;
        double halfUnit = unit / 2;
        ArrayList<Coordinate> corners = new ArrayList<>();
        // Bas-gauche
        corners.add(new Coordinate(c.getX() - halfUnit, c.getY() - halfUnit));
        // Bas-droit
        corners.add(new Coordinate(c.getX() + halfUnit, c.getY() - halfUnit));
        // Haut-gauche
        corners.add(new Coordinate(c.getX() - halfUnit, c.getY() + halfUnit));
        // Haut-droit
        corners.add(new Coordinate(c.getX() + halfUnit, c.getY() + halfUnit));

        if (this.colisionPointCercle(corners[0]) ||
            this.colisionPointCercle(corners[1]) ||
            this.colisionPointCercle(corners[2]) ||
            this.colisionPointCercle(corners[3])) 
            return true; // Deuxième test


    }
*/