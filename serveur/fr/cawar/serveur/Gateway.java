package fr.cawar.serveur;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Gateway {
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients;
    private AtomicInteger currentPlayerCount;
    private GameState gamestate;
    private HashMap<Integer, ArrayList<Coordinate>> obstacleMap;
    private ArrayList<Coordinate> spawnPoints;
    private volatile boolean running = true;
    private String jsonObstacles; // JSON des obstacles

    public Gateway(String ip, int port) throws IOException {
        this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip)); // Créer un serveur sur l'adresse IP et le port spécifiés
        this.serverSocket.setSoTimeout(0); // Disable timeout for server socket
        this.currentPlayerCount = new AtomicInteger(0); // Compteur de joueurs connectés
        this.clients = new CopyOnWriteArrayList<>();
        initializeGame(); // Générer les données initiales de la carte

    }

    private void initializeGame() {
        // Effectuer une seul et unique fois au démarage de la partie
        Map map = new Map();
        map.generateMapData(); // Générer la carte (exemple)
        HashMap<Integer, ArrayList<Coordinate>> obstaclesMap = map.getObstacleMap(); // Récupérer la liste d'obstacles à envoyer au client a sa connexion
        this.obstacleMap = map.getObstacleMap(); // Convertir la liste d'obstacles en JSON
        this.spawnPoints = map.findSpawnPoint(Constants.nbMaxPlayer); // Trouver les points de spawn pour 10 joueurs (On définit le nombre de joueurs ici)
        this.gamestate = new GameState(obstaclesMap); // Créer l'état du jeu avec la carte et les points de spawn
        this.jsonObstacles = JsonWriter.hashMapToJson(obstacleMap); // Convertir la liste d'obstacles en JSON

        
    }

    public void stop() {
        this.running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("Server started. Waiting for clients...");
        while (true) {
            try {
                if(currentPlayerCount.get() < Constants.nbMaxPlayer) {
                    // Accepter une connexion client
                    Socket clientSocket = serverSocket.accept();
                    // Créer un gestionnaire pour ce client
                    Coordinate sp = this.spawnPoints.get(currentPlayerCount.get() % this.spawnPoints.size()); // Récupérer le point de spawn pour le joueur
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this.gamestate, this.obstacleMap, sp); // Récupérer le point de spawn pour le joueur
                    // Ajouter le gestionnaire à la liste des clients
                    clients.add(clientHandler);
                    // Lancer un nouveau thread pour gérer la communication avec ce client
                    new Thread(clientHandler).start();
                    System.out.println("Client connected.");
                }
            } catch (IOException e) {
                if (!running) break; 
                // Gérer les erreurs lors de l'acceptation des connexions
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private Tank currentTank;
        private Coordinate spawnPoint;
        private GameState gamestate;
        private HashMap<Integer, ArrayList<Coordinate>> obstacleMap;

        public ClientHandler(Socket socket, GameState gameState, HashMap<Integer, ArrayList<Coordinate>> obstacleMap, Coordinate spawnPoint) {
            this.spawnPoint = spawnPoint; // Point de spawn pour le joueur
            this.gamestate = gameState; // État du jeu
            this.obstacleMap = obstacleMap; // JSON des obstacles
            this.socket = socket; // Initialisation du socket client
        }

        @Override
        public void run() {
            try {
                // Initialisation des flux d'entrée et de sortie
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Initialisation du client
                initializeClient();

                // Boucle de communication avec le client
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    try {
                        // Traiter chaque paquet reçu
                        handlePacket(inputLine);
                        Thread.sleep(20); // Pause pour éviter de surcharger le serveur
                    } catch (Exception e) {
                        System.err.println("Error handling packet: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                }
            } catch (IOException e) {
                if (!socket.isClosed()) { // Vérifie si le socket est déjà fermé
                    System.err.println("Error handling client: " + e.getMessage());
                    disconnect();
                }
            } finally {
                // Déconnecter le client proprement
                disconnect();
            }
        }

        private void initializeClient() {
            int i = currentPlayerCount.getAndIncrement(); // Incrémente le nombre de joueurs actuel et récupère l'index
            this.currentTank = new Tank(this.spawnPoint); // Crée un tank avec le premier point de spawn
            JsonWriter writer = new JsonWriter(); // Crée un objet JsonWriter pour générer le JSON
            writer.addTank(currentTank.getId(), currentTank.getPosition(), currentTank.getAngle()); // Ajout du tank au JSON
            // __________________________
            writer.addBullet(currentTank.getPosition()); // Ajout de la balle au JSON
            // Ajout des obstacles a afficher
            ArrayList<Integer> neighboorChunk = Constants.getNeighbours(Constants.chunkFromCoord(currentTank.getPosition())); // Récupère la liste des obstacles du chunk    
            for (int chunk : neighboorChunk) {
                ArrayList<Coordinate> chunkObList = this.obstacleMap.get(chunk); // Récupère la liste des obstacles du chunk
                if (chunkObList != null) {
                    writer.addChunkObstacles(chunkObList); // Ajout des obstacles du chunk au JSON
                }
            }

            // __________________________
            String infoTank = writer.toJson(); // Génère le JSON pour le client
            // Ajout du tank a l'état du jeu
            gamestate.addPlayer(currentTank); // Ajoute le tank à l'état du jeu

            // Envoie le JSON au client
            sendPacket(infoTank); // Envoie le JSON au client
        }

        public void sendPacket(String jsonPacket) {
            // Envoyer un paquet JSON au client
            out.println(jsonPacket);
        }


        private void handlePacket(String jsonPacket) {
            System.out.println("Received packet: " + jsonPacket);
            try {
                // Vérifier si le paquet est un heartbeat
                if (jsonPacket.contains("\"heartbeat\":true")) {
                    System.out.println("Heartbeat received, connection is alive.");
                    return; // Ne pas traiter davantage
                }

                // Traiter les paquets normaux
                ArrayList<KeyTimePair> key_time = JsonWriter.jsonToKeyTimePairList(jsonPacket);
                String jsonDataServer = gamestate.process(currentTank, key_time);
                sendPacket(jsonDataServer);
            } catch (Exception e) {
                System.err.println("Error processing packet: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void disconnect() {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (!socket.isClosed()) socket.close(); // Vérifie si le socket est déjà fermé avant de le fermer
            } catch (IOException e) {
                System.err.println("Error disconnecting client: " + e.getMessage());
            } finally {
                clients.remove(this);
                if (currentTank != null) {
                    gamestate.removePlayer(currentTank.getId());
                }
                System.out.println("Client disconnected.");
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Démarrer le serveur sur un port différent (par exemple, 12345)
            Gateway gateway = new Gateway(Constants.ip, 12345); // Update port here
            gateway.start();
        } catch (IOException e) {
            // Gérer les erreurs lors du démarrage du serveur
            System.err.println("Server error: " + e.getMessage());
        }
    }
}