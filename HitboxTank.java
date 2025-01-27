package fr.cawar.serveur;

import java.util.ArrayList;

public class HitboxTank extends Hitbox {
    private double angle;
    private float width;
    private float height;

    public HitboxTank(Coordinate c, double angle) {
        super(c);
        this.angle = angle;
        this.width = Constants.widthTank;
        this.height = Constants.heightTank;
    }

    public void setAngle(double angle) {
        this.angle = angle ;
    }

    public double getAngle() {
        return this.angle ;
    }

    @Override
    public boolean checkObstacleColision(Coordinate obstacle) {
       // Convertir l'angle en radians
        double angleRadians = Math.toRadians(this.angle);

        // Dimensions de ce tank
        double halfWidth = this.width / 2;
        double halfHeight = this.height / 2;

        // Dimensions du carré
        double halfSide = Constants.unit / 2;

        // Calculer la position relative du centre du carré par rapport à ce tank
        double relativeX = obstacle.getX() - this.position.getX();
        double relativeY = obstacle.getY() - this.position.getY();

        // Faire pivoter le système de coordonnées pour ce tank afin qu'il soit aligné avec les axes
        double cosAngle = Math.cos(-angleRadians);
        double sinAngle = Math.sin(-angleRadians);
        double alignedX = cosAngle * relativeX - sinAngle * relativeY;
        double alignedY = sinAngle * relativeX + cosAngle * relativeY;

        // Vérifier s'il y a chevauchement dans le système de coordonnées aligné
        return Math.abs(alignedX) <= (halfWidth + halfSide) && Math.abs(alignedY) <= (halfHeight + halfSide);
    }

    @Override
    public boolean checkTankColision(Coordinate otherPosition, double angle) {
        // Convertir les angles en radians
        double thisAngleRadians = Math.toRadians(this.angle);
        double otherAngleRadians = Math.toRadians(angle);

        // Dimensions de ce tank
        double halfWidthThis = this.width / 2;
        double halfHeightThis = this.height / 2;

        // Dimensions de l'autre tank
        double halfWidthOther = Constants.widthTank / 2;
        double halfHeightOther = Constants.heightTank / 2;

        // Coordonnées du centre de ce tank
        double thisX = this.position.getX();
        double thisY = this.position.getY();

        // Calculer la position relative du centre de l'autre tank par rapport à ce tank
        double relativeX = otherPosition.getX() - thisX;
        double relativeY = otherPosition.getY() - thisY;

        // Faire pivoter le système de coordonnées pour ce tank afin qu'il soit aligné avec les axes
        double cosThis = Math.cos(-thisAngleRadians);
        double sinThis = Math.sin(-thisAngleRadians);
        double alignedX = cosThis * relativeX - sinThis * relativeY;
        double alignedY = sinThis * relativeX + cosThis * relativeY;

        // Vérifier s'il y a chevauchement dans le système de coordonnées aligné
        return Math.abs(alignedX) <= (halfWidthThis + halfWidthOther) && Math.abs(alignedY) <= (halfHeightThis + halfHeightOther);
    }
/*
   public static void main(String[] args) {
        // Créer un objet HitboxTank
        Coordinate tankCenter = new Coordinate(2f, 2f);
        HitboxTank tank = new HitboxTank(tankCenter, 0);

        // Créer un obstacle à tester
        Coordinate obstacle = new Coordinate(3f, 2.5f);

        // Tester la collision de l'obstacle avec le tank
        boolean obstacleCollision = tank.checkObstacleColision(obstacle);
        System.out.println("Collision avec l'obstacle : " + obstacleCollision);

        // Tester la collision avec un autre tank, en supposant que le second tank a pour centre (8, 8) et un angle de 90 degrés
        float otherTankX = 8.0f;
        float otherTankY = 8.0f;
        float otherTankAngle = 90.0f;

        boolean tankCollision = tank.checkTankColision(otherTankX, otherTankY, otherTankAngle);
        System.out.println("Collision avec un autre tank : " + tankCollision);
    }
*/
}
