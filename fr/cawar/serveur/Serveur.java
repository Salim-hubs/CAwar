package fr.cawar.serveur;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

    public class Serveur {
        private static final int PORT = 5000;
        private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
        //private static final List<Map<String, Integer>> obstacles = new ArrayList<>();
        private AtomicInteger CurrentPlayerAccount = new AtomicInteger(0);
        private GameState gamestate;
        private ArrayList<Coordinate> spawnPoints;
        private String jsonObstacles; // JSON des obstacles

        
        public Serveur() {
            initialiserGameState(); // Appelle une méthode pour initialiser l'état du jeu
        }

        private void initialiserGameState() {
            Map map = new Map();
            map.generateMapData();
            HashMap<Integer, ArrayList<Coordinate>> obstacleMap = map.getObstacleMap();
            //this.obstacleMap = map.getObstacleMap();
            if(map.getLenght() <= 10){
                map.displayObstacleList();
                map.displayMapData(); // Affiche la carte générée
            }
            map.displayPercentObstacle();
            this.gamestate = new GameState(obstacleMap);
            this.spawnPoints = map.findSpawnPoint(Constants.nbMaxPlayer);
            this.jsonObstacles = JsonWriter.hashMapToJson(obstacleMap);
        }
    
            

        public static void main(String[] args) {
            //ici on va generer la map à base de :
            
            Serveur serveur = new Serveur();
            // on a l'état du jeu 
            
            //GameState gameState = new GameState(obstacleMap);

            System.out.println("🎮 Serveur CAWAR lancé sur le port " + PORT);
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println(" Nouveau client : " + socket.getInetAddress());



                    ClientHandler clientThread = new ClientHandler(socket, clients,serveur);
                    
                    clients.add(clientThread);
                    clientThread.start();

                    // Envoi du JSON des obstacles au client
                   // sendMessage(serveur.jsonObstacles, clientThread);

                }
            } catch (IOException e) {
                System.err.println(" Erreur serveur : " + e.getMessage());
            }
        }

        // private static void broadcastObstacles() {
        //     Message obstacleMessage = new Message("obstacles", obstacles);
        //     synchronized (clients) {
        //         for (ClientHandler client : clients) {
        //             client.send(obstacleMessage);

        //         }
        //     }
        // }

        static void sendMessage(String JsonMessage, ClientHandler client) {
            // Envoie un message JSON à un client spécifique
            client.send(JsonMessage + "\n"); 
        }   

        public void createTank(ClientHandler client) {
            if(this.CurrentPlayerAccount.get() < Constants.nbMaxPlayer || spawnPoints.size()>0) { // Vérifie si le nombre de joueurs actuel est inférieur au nombre maximum de joueurs
                int i = this.CurrentPlayerAccount.getAndIncrement(); // Incrémente le nombre de joueurs actuel et récupère l'index
                Tank currentTank = new Tank(spawnPoints.get(i%spawnPoints.size())); // Crée un tank avec le premier point de spawn
                client.setCurrentTank(currentTank); // Assigne le tank au client
                this.gamestate.addPlayer(currentTank); // Ajoute le tank à l'état du jeu

                System.out.println("🛠️ Création du tank pour le joueur #" + i);
                System.out.println("    ➤ Position : " + currentTank.getPosition());
                System.out.println("    ➤ Angle    : " + currentTank.getAngle());

                JsonWriter writer = new JsonWriter(); // Crée un objet JsonWriter pour générer le JSON
                writer.addTank(currentTank.getId(), currentTank.getPosition(), currentTank.getAngle()); // Ajout du tank au JSON
                writer.setType("init"); // Définit le type de message
                String infoTank = writer.toJson(); // Génère le JSON pour le client
                // Ajout du tank a l'état du jeu

                String initPacket = JsonWriter.initPacket(currentTank.getId(), this.jsonObstacles); // Crée le paquet d'initialisation
                // System.err.println("Paquet d'initialisation envoyé : " + initPacket);
                sendMessage(initPacket, client); // Envoie le paquet d'initialisation au client
            
            } else {
                // Si le nombre maximum de joueurs est atteint, on ne crée pas de nouveau tank
                System.out.println("Nombre maximum de joueurs atteint.");
            }
        }

        public GameState getGamestate() {
            return this.gamestate;
        }


        }
        // javac -cp ".:libs/gson-2.10.1.jar" *.java
        // java -cp ".:libs/gson-2.10.1.jar" Serveur
        // java -cp ".:libs/gson-2.10.1.jar" Client

        //crééer une fonction pour creer un tank pour chaque client 

