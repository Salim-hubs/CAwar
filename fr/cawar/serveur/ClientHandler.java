package fr.cawar.serveur;
import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<ClientHandler> clients;
    private Tank currentTank;
    private GameState gamestate;
    private Serveur serveur;
    //private int id;
    private Gson gson = new Gson();

    public ClientHandler(Socket socket, List<ClientHandler> clients,Serveur serveur) {
        this.socket = socket;
        this.clients = clients;
        this.serveur = serveur;
        this.gamestate = serveur.getGamestate();
        this.currentTank = null; // Initialisation du tank
        //this.id = clients.size() + 1; // ID unique pour chaque client

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            //send(new Message("connected", "Joueur connecté avec l ID : " + id));
            //assigner au client le tank
            
        } catch (IOException e) {
            System.err.println("Erreur à l'initialisation d'un client.");
        }
    }

    @Override
    public void run() {
        try {
            serveur.createTank(this);
            System.out.println("🧪 ClientHandler prêt à lire les paquets...");
            System.out.println("🎧 ClientHandler démarré.");

            String ligne;
            long lastTime = System.nanoTime(); // Temps de la dernière exécution
            long interval = 1000000000L / 30; // 30Hz
            long lastMessageTime = System.currentTimeMillis(); // Temps du dernier message reçu
            long timeout = 60_000; // 10 secondes d'inactivité

            while (true) {
                try {
                    if (socket.isClosed()) {
                        System.out.println("❌ Socket fermé, déconnexion du client détectée.");
                        break;
                    }

                    if (in.ready()) {
                        ligne = in.readLine();
                        if (ligne == null) {
                            System.out.println("❌ Client déconnecté.");
                            break;
                        }

                        lastMessageTime = System.currentTimeMillis(); // Mise à jour du dernier message

                        long currentTime = System.nanoTime();
                        if (currentTime - lastTime >= interval) {
                            try {
                                routine(ligne);
                            } catch (Exception e) {
                                System.err.println("❌ Erreur dans routine : " + e.getMessage());
                                e.printStackTrace();
                            }
                            lastTime = currentTime;
                        }
                    } else {
                        // Vérifie si le client est inactif depuis trop longtemps
                        routine(null);
                        if (System.currentTimeMillis() - lastMessageTime > timeout) {
                            System.out.println("⌛ Inactivité détectée : client considéré comme déconnecté.");
                            break;
                        }

                        Thread.sleep(10); // Pause légère pour éviter un CPU à 100%
                    }

                } catch (InterruptedException e) {
                    System.err.println("❌ Thread interrompu : " + e.getMessage());
                    break;
                } catch (IOException e) {
                    System.err.println("❌ Erreur I/O pendant la lecture : " + e.getMessage());
                    break;
                } catch (Exception e) {
                    System.err.println("❌ Erreur inattendue : " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Exception lors de la création du tank ou démarrage du client : " + e.getMessage());
        } finally {
            try {
                if (currentTank != null) {
                    gamestate.removePlayer(currentTank.getId());
                    System.out.println("👋 Joueur retiré de l'état du jeu.");
                }
                clients.remove(this);
                if (!socket.isClosed()) {
                    socket.close();
                }
                System.out.println("✅ Socket fermé et client retiré.");
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }



    // @Override
    // public void run() {
    //     try {
    //         serveur.createTank(this);
    //         System.out.println("🧪 ClientHandler prêt à lire les paquets...");
    //         System.out.println("🎧 ClientHandler démarré.");

    //         String ligne;
    //         while ((ligne = in.readLine()) != null) {
    //             try {
    //                 routine(ligne);
    //             } catch (Exception e) {
    //                 System.err.println("❌ Erreur dans routine : " + e.getMessage());
    //                 e.printStackTrace();
    //             }
    //         }

    //     } catch (IOException e) {
    //         System.out.println("Client déconnecté : " + e.getMessage());
    //         gamestate.removePlayer(this.currentTank.getId()); // Supprime le tank de l'état du jeu
    //     } finally {
    //         try {
    //             clients.remove(this);
    //             socket.close();
    //         } catch (IOException e) {
    //             System.err.println("Erreur lors de la déconnexion.");
    //         }
    //     }
    // }


    public  void send(String message) {
        //String json = gson.toJson(message);
        out.println(message);
        //out.println(message);
        // System.out.println(" Envoyé au client: " + message);
    }


    public void routine(String jsonInputClient) {
        // System.out.println("📨 Paquet JSON reçu : " + jsonInputClient);

        
        try {
            if (jsonInputClient == null) {
                String jsonDataServer = this.gamestate.environmentData(currentTank);
                Serveur.sendMessage(jsonDataServer, this);
            } else {
                ArrayList<KeyTimePair> key_time = JsonWriter.jsonToKeyTimePairList(jsonInputClient);

                // 👇 Vérifie si Escape est pressée
                boolean escapePressed = key_time.stream()
                    .anyMatch(k -> k.getKey().equalsIgnoreCase("Escape"));

                if (escapePressed) {
                    System.out.println("🚪 Touche Escape détectée, fermeture du socket...");
                    socket.close(); // Déclenchera la fin du thread dans run()
                    return;
                }
                

                
                System.out.println("Changement de chunk détecté : " +
                    "\nTank id : " + currentTank.getId() + 
                    "\nChunk : " + Constants.chunkFromCoord(currentTank.getPosition()) +
                    "\nPosition : " + currentTank.getPosition().toString());


                String jsonDataServer = this.gamestate.process(currentTank, key_time);
                Serveur.sendMessage(jsonDataServer, this);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur JSON ou traitement : " + e.getMessage());
            e.printStackTrace(System.err);
        }

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            System.err.println("Erreur lors de la pause du thread.");
            e.printStackTrace(System.err);
        }
    }

    

    public void setCurrentTank(Tank tank) {
        this.currentTank = tank;
    }


    
}