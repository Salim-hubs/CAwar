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