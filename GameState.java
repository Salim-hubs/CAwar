package fr.cawar.serveur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState {
    private HashMap<Integer, ArrayList<Coordinate>> obstacleMap;
    private ArrayList<Tank> tankList;

    public GameState(HashMap<Integer, ArrayList<Coordinate>> map, ArrayList<Tank> tankList) {
        this.obstacleMap = map;
        this.tankList = tankList;
    }

    public ArrayList<Tank> getTankList() {
        return tankList;
    }

    public void process(Tank T0, ArrayList<KeyTimePair> key_time) {
        T0.setKeyTime(key_time);
        Coordinate simulatedPos = T0.simulatePosition();
        for (Bullet Bullet0 : T0.getBulletList()) {
            Bullet0.updatePosition();
        }

        for (Tank Ti : tankList) {
            Coordinate atomPosTi = Ti.getAtomPos();
            long atomAngleTi = Ti.getAtomAngle();

            if ((Ti.getId() != T0.getId() && Constants.calculateDistance(T0.getPosition(), atomPosTi) < Constants.virtualFOV)) {
                boolean isCollided = false;
                Bullet[] listT0 = T0.getBulletList();
                int i = 0;

                while (!isCollided && i < Constants.MAX_SHOOT) {
                    Bullet B0 = listT0[i];
                    if (B0 != null) {
                        if (B0.checkTankColision(atomPosTi, atomAngleTi)) {
                            Ti.incrementScore();
                            if (Ti.gotShoot() <= 0) {
                                Ti.respawn();
                            }
                        }

                        int chunk = Constants.chunkFromCoord(B0.getPosition());
                        ArrayList<Coordinate> obstacles = this.obstacleMap.get(chunk);
                        if (obstacles != null) {
                            int index = 0;
                            while (!isCollided && index < obstacles.size()) {
                                Coordinate obstacle = obstacles.get(index);
                                if (B0.checkObstacleColision(obstacle)) {
                                    isCollided = true;
                                }
                                index++;
                            }
                        }
                    }
                    i++;
                }

                boolean isNewPosInvalid = false;
                if (Constants.calculateDistance(T0.getPosition(), atomPosTi) < Constants.unitInChunk) {
                    if (T0.checkTankColision(atomPosTi, atomAngleTi)) {
                        isNewPosInvalid = true;
                    }
                }

                int chunkTank = Constants.chunkFromCoord(T0.getPosition());
                List<Coordinate> obstacles = this.obstacleMap.get(chunkTank);
                if (obstacles != null) {
                    int index = 0;
                    while (!isNewPosInvalid && index < obstacles.size()) {
                        Coordinate obstacle = obstacles.get(index);
                        if (T0.checkObstacleColision(obstacle)) {
                            isNewPosInvalid = true;
                        }
                        index++;
                    }
                }

                if (isNewPosInvalid) {
                    Ti.setPosition(simulatedPos);
                }
            }
        }

        T0.updateAtomic();
        for (Bullet Bu0 : T0.getBulletList()) {
            Bu0.updateAtomic();
        }
    }

    public void printAtomData() {
        for (Tank tank : tankList) {
            System.out.println("Tank ID: " + tank.getId());
            System.out.println("Position: " + tank.getAtomPos().toString());
            System.out.println("Angle: " + tank.getAtomAngle().to);
            System.out.println("HP: " + tank.getAtomHp());
            for (Bullet bullet : tank.getBulletList()) {
                if (bullet != null) {
                    System.out.println("Bullet ID: " + bullet.getId() + " Position: " + bullet.getPosition().toString());
                }
            }
        }
    }
}
