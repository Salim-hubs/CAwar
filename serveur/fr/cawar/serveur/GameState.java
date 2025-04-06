package fr.cawar.serveur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameState {
    private HashMap<Integer, ArrayList<Coordinate>> obstacleMap;
    private List<Tank> tankList = new CopyOnWriteArrayList<>(); // ATOMIQUE


    public GameState(HashMap<Integer, ArrayList<Coordinate>> map) {
        this.obstacleMap = map;
        this.tankList = new CopyOnWriteArrayList<>(); // Conserve la sécurité thread-safe

    }


    public void addPlayer(Tank tank) {
        this.tankList.add(tank) ;
        System.out.println("Ajout du tank ! ID: " + tank.getId());
    }

    public void removePlayer(int tankId) {
        boolean removed = this.tankList.removeIf(tank -> tank.getId() == tankId);
        if (!removed) {
            System.out.println("Tank non trouvé dans la liste ! ID: " + tankId);
        }
    }


    // Processus de simulation du jeu
    public String process(Tank T0, ArrayList<KeyTimePair> key_time) {
        // JSON
        JsonWriter json = new JsonWriter();        

        // Liste des input client
        T0.setKeyTime(key_time);
        
        // Met à jour la position des balles
        for (Bullet Bullet0 : T0.getBulletList()) {
            Bullet0.updatePosition();

            // Check si la balles a touché un obstacle
            int chunk = Constants.chunkFromCoord(Bullet0.getPosition());
            ArrayList<Coordinate> obstacles = this.obstacleMap.get(chunk);
            for (Coordinate o : obstacles) {
                // checkCircleWithCircle(Coordinate c1, float radius1, Coordinate c2, float radius2)
                if (Hitbox.checkCircleWithCircle(Bullet0.getPosition(), Constants.radiusBullet , o, Constants.unit/2)) { /* CHECK COLISIONS AVEC OBSTACLE */
                    T0.deleteBullet(Bullet0); 
                }
            }

            json.addBullet(Bullet0.getPosition()); // Ajout des données de la balle Bullet au JSON
        }


        // Check si le tank est mort
        if (T0.getAtomHp() <= 0) { // Si le tank T0 est mort
            T0.respawn(); // Respawn du tank T0
        }

        // Gère l'expiration des balles
        T0.handleBulletExpiration(); 

        // Simule la position du tank
        Coordinate simulatedPos = T0.simulatePosition();
        boolean isCollided = false;

        // Check colisions avec les obstacles
        int chunkTank = Constants.chunkFromCoord(T0.getPosition());
        List<Coordinate> obstacles = this.obstacleMap.get(chunkTank);
        if (obstacles != null) {
            for (Coordinate o : obstacles) {
                // HitboxObstacle obHit = new HitboxObstacle(Constants.obstacleShape, o);
                // checkRectangleWithCircle(Coordinate rect, double angle, float height, float width, Coordinate circ, float radius)
                if (Hitbox.checkRectangleWithCircle(T0.getPosition(), T0.getAngle(), o, Constants.unit/2)) { /* CHECK COLISIONS AVEC OBSTACLE */
                    isCollided = true; // Collision détectée
                }
            }
        }

        // Pour chaque tank
        for (Tank T : this.tankList) {
            if (T != T0){ // Ne pas comparer avec soi-même
                // Position atomique du tank
                Coordinate atomPosT = T.getAtomPos();
                // Angle atomique du tank
                long atomAngleT = T.getAtomAngle();
                
                // COLISIONS

                // Check si une balle de T0 touche le tank T
                // checkRectangleWithCircle(Coordinate rect, double angle, Coordinate circ, float radius)
                for (Bullet Bullet : T0.getBulletList()) {
                    if(Hitbox.checkRectangleWithCircle(atomPosT, atomAngleT, Bullet.getPosition(), Constants.radiusBullet)) {
                        T.incrementScore(); // Incrémentation du score du tank qui a touché
                        T0.deleteBullet(Bullet); // Supprime la balle de la liste de T0
                    }
                }

                // Check si le tankT0 touche le tank T
                // checkRectangleWithRectangle(Coordinate rect, double angle, Coordinate rect2, double angle2)  
                if(Hitbox.checkRectangleWithRectangle(T0.getPosition(), T0.getAngle(), atomPosT, atomAngleT)) { 
                    isCollided = true; // Collision détectée
                }




                        
                // AFFICHAGE
                // Tank a afficher
                if(Constants.calculateDistance(T0.getPosition(), atomPosT) <= Constants.FOV) {
                    // DONNEE DE T DANS LE JSON
                    json.addTank(T.getId(), atomPosT, atomAngleT); // Ajout des données du tank T au JSON
                }
                // Bullet a afficher
                for (Bullet Bullet : T.getBulletList()) {
                    Coordinate atomPosBullet = Bullet.getAtomicPosition();
                    if(Constants.calculateDistance(T0.getPosition(), atomPosBullet) <= Constants.FOV) {
                        // DONNEE DE LA BALLE DE T DANS LE JSON
                        json.addBullet(atomPosBullet); // Ajout des données de la balle Bullet au JSON
                    }
                }

                
                
            }
        }

        // Update tank
        if (isCollided) {
            // Si le tank est en collision, on remet la position à la position simulée
            T0.setPosition(simulatedPos);
        }
        else {
            // Sinon, on met à jour la position atomique du tank
            T0.updateAtomic();
        }

        json.addTank(T0.getId(), T0.getPosition(), T0.getAngle()); // Ajout des données du tank T0 au JSON

        return json.toJson(); // Retourne le JSON généré   
    }
}

