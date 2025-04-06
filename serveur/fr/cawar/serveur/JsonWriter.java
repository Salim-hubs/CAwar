package fr.cawar.serveur;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class JsonWriter {
    private List<TankDisplay> tanks;
    private List<BulletDisplay> bullets;

    // Constructeurs, getters et setters
    public JsonWriter() {
        this.tanks = new ArrayList<>();
        this.bullets = new ArrayList<>();
    }

    public List<TankDisplay> getTanks() {
        return tanks;
    }

    public void addTank(int id, Coordinate position, double angle) {
        TankDisplay tank = new TankDisplay(id, position, angle);
        this.tanks.add(tank);
    }

    public List<BulletDisplay> getBullets() {
        return bullets;
    }

    public void addBullet(Coordinate position) {
        BulletDisplay bullet = new BulletDisplay(position);
        this.bullets.add(bullet);
    }
    
    // Méthode pour convertir l'objet en JSON
    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        // Ajouter les tanks
        jsonBuilder.append("\"tanks\": [");
        for (int i = 0; i < tanks.size(); i++) {
            TankDisplay tank = tanks.get(i);
            jsonBuilder.append(String.format(
                "{\"id\": %d, \"position\": {\"x\": %.2f, \"y\": %.2f}, \"angle\": %.2f}",
                tank.getId(), tank.getPosition().getX(), tank.getPosition().getY(), tank.getAngle()
            ));
            if (i < tanks.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("],");

        // Ajouter les balles
        jsonBuilder.append("\"bullets\": [");
        for (int i = 0; i < bullets.size(); i++) {
            BulletDisplay bullet = bullets.get(i);
            jsonBuilder.append(String.format(
                "{\"position\": {\"x\": %.2f, \"y\": %.2f}}",
                bullet.getPosition().getX(), bullet.getPosition().getY()
            ));
            if (i < bullets.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    // Méthode pour convertir une liste de keyTimePair en JSON
    public String keyTimePairListToJson(List<KeyTimePair> list) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");

        for (int i = 0; i < list.size(); i++) {
            KeyTimePair pair = list.get(i);
            jsonBuilder.append(String.format(
                "{\"key\": \"%s\", \"time\": %.2f}", // Utilisation de %.2f pour formater un double avec 2 décimales
                pair.getKey(), pair.getTime()
            ));
            if (i < list.size() - 1) {
                jsonBuilder.append(",");
            }
        }

        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    

    public String hashMapToJson(HashMap<Integer, ArrayList<Coordinate>> map) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        int mapSize = map.size();
        int count = 0;

        for (Integer key : map.keySet()) {
            jsonBuilder.append("\"").append(key).append("\": [");

            ArrayList<Coordinate> coordinates = map.get(key);
            for (int i = 0; i < coordinates.size(); i++) {
                Coordinate coord = coordinates.get(i);
                jsonBuilder.append(String.format(
                    "{\"x\": %.2f, \"y\": %.2f}",
                    coord.getX(), coord.getY()
                ));
                if (i < coordinates.size() - 1) {
                    jsonBuilder.append(",");
                }
            }

            jsonBuilder.append("]");
            if (++count < mapSize) {
                jsonBuilder.append(",");
            }
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    public static void main(String[] args) {
        // Exemple de tanks
        JsonWriter jsonWriter = new JsonWriter();
        jsonWriter.addTank(1, new Coordinate(100, 200), 180);
        jsonWriter.addTank(2, new Coordinate(150, 250), 90);

        // Exemple de balles
        jsonWriter.addBullet(new Coordinate(120, 220));
        jsonWriter.addBullet(new Coordinate(180, 260));

        // Exemple de HashMap
        Map map = new Map();
        map.generateMapData(); // Générer la carte (exemple)
        String obstacleJson = jsonWriter.hashMapToJson(map.getObstacleMap());
        System.out.println(obstacleJson);



        // Génère le JSON
        String json = jsonWriter.toJson();
        System.out.println(json);
    }
}


class TankDisplay {
    private int id;
    private Coordinate position;
    private double angle;

    // Constructeurs, getters et setters
    public TankDisplay(int id, Coordinate position, double angle) {
        this.id = id;
        this.position = position;
        this.angle = angle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}

class BulletDisplay {
    private Coordinate position;

    // Constructeurs, getters et setters
    public BulletDisplay(Coordinate position) {
        this.position = position;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }
}



