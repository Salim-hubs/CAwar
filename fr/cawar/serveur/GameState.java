package fr.cawar.serveur;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {
    private HashMap<Integer, ArrayList<Coordinate>> obstacleMap;
    private List<Tank> tankList = new CopyOnWriteArrayList<>();

    public GameState(HashMap<Integer, ArrayList<Coordinate>> map) {
        this.obstacleMap = map;
        this.tankList = new CopyOnWriteArrayList<>();
    }

    public void addPlayer(Tank tank) {
        this.tankList.add(tank);
        System.out.println("Ajout du tank ! ID: " + tank.getId());
    }

    public void removePlayer(int tankId) {
        boolean removed = this.tankList.removeIf(tank -> tank.getId() == tankId);
        if (!removed) {
            System.out.println("Tank non trouvé dans la liste ! ID: " + tankId);
        }
    }

    public String environmentData(Tank T0) {
        JsonWriter json = new JsonWriter();

        // Met à jour la position des balles du tank T0 et gère les collisions avec les obstacles
        List<Bullet> bulletsCopy = new ArrayList<>(T0.getBulletList());
        for (Bullet b : bulletsCopy) {
            b.updatePosition();
            int chunk = Constants.chunkFromCoord(b.getPosition());
            List<Coordinate> obstacles = obstacleMap.get(chunk);
            if (obstacles != null) {
                for (Coordinate o : obstacles) {
                    if (Hitbox.rectanglesCircleCollide(
                                o, Constants.unit, Constants.unit, 0,
                                b.getPosition(), Constants.radiusBullet
                        )) {
                        T0.deleteBullet(b);
                        break;
                    }
                }
            }
            json.addBullet(b.getPosition());
        }

        // Vérifie si le tank T0 est mort et le fait respawn si besoin
        if (T0.getAtomHp() <= 0) {
            T0.respawn();
            T0.updateAtomic();
        }

        // Gère l'expiration des balles du tank T0
        T0.handleBulletExpiration();

        // Pour chaque tank dans la partie (sauf T0)
        for (Tank T : this.tankList) {
            if (T != T0){
                Coordinate atomPosT = T.getAtomPos();
                double atomAngleT = T.getAtomAngle();

                // Vérifie si une balle de T0 touche le tank T
                for (Bullet Bullet : T0.getBulletList()) {
                    if(Hitbox.rectanglesCircleCollide(
                                                atomPosT, Constants.widthTank, Constants.heightTank, atomAngleT,
                                                Bullet.getPosition(), Constants.radiusBullet
                    )) {
                        T.gotShoot();
                        T0.incrementScore();
                        T0.deleteBullet(Bullet);
                    }
                }

                // Ajoute les tanks et balles visibles dans le champ de vision (FOV)
                if(Constants.calculateDistance(T0.getPosition(), atomPosT) <= Constants.FOV) {
                    json.addTank(T.getId(), atomPosT, atomAngleT);
                }
                for (Bullet b : T.getBulletList()) {
                    Coordinate pos = b.getAtomicPosition();
                    if (Constants.calculateDistance(T0.getPosition(), pos) <= Constants.FOV) {
                        json.addBullet(pos);
                    }
                }
            }
        }

        // Met à jour la position atomique des balles de T0
        T0.updateAtomicBullet();

        // Ajoute les données du tank T0 au JSON
        json.addTank(T0.getId(), T0.getPosition(), T0.getAngle());

        json.setType("routine");
        String jsonOutput = json.toJson();

        return jsonOutput;
    }

    public String process(Tank T0, ArrayList<KeyTimePair> key_time) {
        JsonWriter json = new JsonWriter();

        // Applique les entrées utilisateur au tank T0
        T0.setKeyTime(key_time);

        // Met à jour la position des balles de T0 et gère les collisions avec les obstacles
        for (Bullet Bullet0 : T0.getBulletList()) {
            Bullet0.updatePosition();

            int chunk = Constants.chunkFromCoord(Bullet0.getPosition());
            ArrayList<Coordinate> obstacles = this.obstacleMap.get(chunk);
            if (obstacles != null && !obstacles.isEmpty()) {
                for (Coordinate o : obstacles) {
                    if (Hitbox.rectanglesCircleCollide(
                                    o, Constants.unit, Constants.unit, 0,
                                    Bullet0.getPosition(), Constants.radiusBullet
                            )) {
                        T0.deleteBullet(Bullet0);
                    }
                }
            }

            json.addBullet(Bullet0.getPosition());
        }

        // Vérifie si le tank T0 est mort et le fait respawn si besoin
        if (T0.getAtomHp() <= 0) {
            T0.respawn();
            T0.updateAtomic();
        }

        // Gère l'expiration des balles du tank T0
        T0.handleBulletExpiration();

        // Simule la prochaine position du tank T0
        Coordinate simulatedPos = T0.simulatePosition();

        boolean isCollided = false;

        // Vérifie les collisions du tank T0 avec les obstacles du chunk courant
        int chunkTank = Constants.chunkFromCoord(T0.getPosition());
        List<Coordinate> obstacles = this.obstacleMap.get(chunkTank);
        if (obstacles != null) {
            for (Coordinate o : obstacles) {
                if (Hitbox.rectanglesCircleCollide(
                                T0.getPosition(), Constants.widthTank, Constants.heightTank, T0.getAngle(),
                                o, Constants.radiusBullet
                    )) {
                    isCollided = true;
                    break;
                }
            }
        }

        // Pour chaque tank dans la partie (sauf T0)
        for (Tank T : this.tankList) {
            if (T != T0){
                Coordinate atomPosT = T.getAtomPos();
                double atomAngleT = T.getAtomAngle();

                // Vérifie si une balle de T0 touche le tank T
                for (Bullet Bullet : T0.getBulletList()) {
                    if(Hitbox.rectanglesCircleCollide(
                                atomPosT, Constants.widthTank, Constants.heightTank, atomAngleT,
                                Bullet.getPosition(), Constants.radiusBullet
                        )) {
                        T.gotShoot();
                        T0.incrementScore();
                        T0.deleteBullet(Bullet);
                    }
                }

                // Vérifie si le tank T0 touche un autre tank
                if(Hitbox.rectanglesCollide(
                    T0.getPosition(), Constants.widthTank, Constants.heightTank, T0.getAngle(),
                    atomPosT, Constants.widthTank, Constants.heightTank, atomAngleT
                )) {
                    isCollided = true;
                }

                // Ajoute les tanks et balles visibles dans le champ de vision (FOV)
                if(Constants.calculateDistance(T0.getPosition(), atomPosT) <= Constants.FOV) {
                    json.addTank(T.getId(), atomPosT, atomAngleT);
                }
                for (Bullet Bullet : T.getBulletList()) {
                    Coordinate atomPosBullet = Bullet.getAtomicPosition();
                    if(Constants.calculateDistance(T0.getPosition(), atomPosBullet) <= Constants.FOV) {
                        json.addBullet(atomPosBullet);
                    }
                }
            }
        }

        // Met à jour la position du tank T0 selon les collisions détectées
        if (isCollided) {
            T0.setPosition(simulatedPos);
            T0.updateAtomic();
        }
        else {
            T0.updateAtomic();
        }

        // Met à jour la position atomique des balles de T0
        T0.updateAtomicBullet();

        // Ajoute les données du tank T0 au JSON
        json.addTank(T0.getId(), T0.getPosition(), T0.getAngle());

        json.setType("routine");
        String jsonOutput = json.toJson();

        return jsonOutput;
    }
}


