package fr.cawar.serveur;

import java.util.ArrayList;
import java.util.List;

public class Hitbox {
    // Collision obstacle et bullet
    public static boolean checkCircleWithCircle(Coordinate c1, float radius1, Coordinate c2, float radius2) {
        return Constants.calculateDistance(c1, c2) <= radius1 + radius2;
    }

    // Collision tank - (bullet, obstacle)
    public static boolean checkRectangleWithCircle(Coordinate rect, double angle, Coordinate circ, float radius) {
        // Calcul éliminatoire
        if (Constants.calculateDistance(rect, circ) > (Constants.halfDiagonal + radius)) {
            return false;
        }

        List<Coordinate> list = findCircleRectangle(rect, angle);
        float rRad = Constants.widthTank / 2; // Utilise Constants.widthTank pour le rayon

        for (Coordinate c : list) {
            // Vérifie les collisions avec les 3 cercles
            if (checkCircleWithCircle(c, rRad, circ, radius)) {
                return true;
            }
        }
        return false;
    }

    // Collision tank - tank
    public static boolean checkRectangleWithRectangle(Coordinate rect, double angle, Coordinate rect2, double angle2) {
        // Calcul éliminatoire
        if (Constants.calculateDistance(rect, rect2) > (Constants.halfDiagonal * 2)) {
            return false;
        }

        List<Coordinate> list = findCircleRectangle(rect, angle);
        List<Coordinate> list2 = findCircleRectangle(rect2, angle2);
        float rRad = Constants.widthTank / 2; // Utilise Constants.widthTank pour le rayon
        float rRad2 = Constants.widthTank / 2; // Utilise Constants.widthTank pour le rayon du deuxième rectangle

        for (Coordinate c : list) {
            for (Coordinate c2 : list2) {
                // Vérifie les collisions avec les 3 cercles
                if (checkCircleWithCircle(c, rRad, c2, rRad2)) {
                    return true;
                }
            }
        }
        return false;
    }

    //Approximation de Rect avec RECT par SAT

    public static boolean rectanglesCollide(
            Coordinate pos1, float width1, float height1, double angle1,
            Coordinate pos2, float width2, float height2, double angle2
    ) {
        Coordinate[] rect1 = getRotatedRectangleCorners(pos1, width1, height1, angle1);
        Coordinate[] rect2 = getRotatedRectangleCorners(pos2, width2, height2, angle2);

        Coordinate[] axes = {
                new Coordinate(rect1[1].x - rect1[0].x, rect1[1].y - rect1[0].y),
                new Coordinate(rect1[3].x - rect1[0].x, rect1[3].y - rect1[0].y),
                new Coordinate(rect2[1].x - rect2[0].x, rect2[1].y - rect2[0].y),
                new Coordinate(rect2[3].x - rect2[0].x, rect2[3].y - rect2[0].y)
        };

        for (Coordinate axis : axes) {
            float len = (float) Math.sqrt(axis.x * axis.x + axis.y * axis.y);
            float ax = axis.x / len;
            float ay = axis.y / len;

            float min1 = Float.MAX_VALUE, max1 = -Float.MAX_VALUE;
            for (Coordinate p : rect1) {
                float proj = p.x * ax + p.y * ay;
                min1 = Math.min(min1, proj);
                max1 = Math.max(max1, proj);
            }

            float min2 = Float.MAX_VALUE, max2 = -Float.MAX_VALUE;
            for (Coordinate p : rect2) {
                float proj = p.x * ax + p.y * ay;
                min2 = Math.min(min2, proj);
                max2 = Math.max(max2, proj);
            }

            if (max1 < min2 || max2 < min1) {
                return false;
            }
        }

        return true;
    }

    public static boolean rectanglesCircleCollide(
            Coordinate rectPos, float rectWidth, float rectHeight, double rectAngle,
            Coordinate circlePos, float circleRadius
    ) {
        Coordinate[] rectCorners = getRotatedRectangleCorners(rectPos, rectWidth, rectHeight, rectAngle);
        for (Coordinate corner : rectCorners) {
            if (checkCircleWithCircle(corner, Constants.widthTank / 2, circlePos, circleRadius)) {
                return true;
            }
        }

        // Check if the circle's center is inside the rectangle
        return rectanglesCollide(rectPos, rectWidth, rectHeight, rectAngle,
                circlePos, circleRadius * 2, circleRadius * 2, 0);
    }

    public static Coordinate[] getRotatedRectangleCorners(Coordinate center, float width, float height, double angle) {
        float hw = width / 2;
        float hh = height / 2;

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Coordinate[] corners = new Coordinate[4];
        corners[0] = new Coordinate(center.x - hw * (float) cos + hh * (float) sin,
                center.y - hw * (float) sin - hh * (float) cos);
        corners[1] = new Coordinate(center.x + hw * (float) cos + hh * (float) sin,
                center.y + hw * (float) sin - hh * (float) cos);
        corners[2] = new Coordinate(center.x + hw * (float) cos - hh * (float) sin,
                center.y + hw * (float) sin + hh * (float) cos);
        corners[3] = new Coordinate(center.x - hw * (float) cos - hh * (float) sin,
                center.y - hw * (float) sin + hh * (float) cos);

        return corners;
    }


    private static List<Coordinate> findCircleRectangle(Coordinate rect, double angle) {
        List<Coordinate> list = new ArrayList<>();
        // Utilise Constants.heightTank pour la hauteur
        float halfHeight = Constants.heightTank / 2;

        // Calcul des décalages en fonction de l'angle
        float offsetX = (float) (halfHeight * Math.cos(angle));
        float offsetY = (float) (halfHeight * Math.sin(angle));

        // Calcul des coordonnées des deux centres
        list.add(new Coordinate(rect.getX() + offsetX, rect.getY() + offsetY)); // r1
        list.add(new Coordinate(rect.getX() - offsetX, rect.getY() - offsetY)); // r2
        list.add(new Coordinate(rect)); // Centre du rectangle

        return list;
    }
}