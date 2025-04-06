package fr.cawar.serveur;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFrame;

public class Test implements KeyListener {
    private List<KeyTimePair> keyTimePairs;
    private Tank tank; // Tank à manipuler
    private HashSet<String> pressedKeys; // Ensemble des touches actuellement pressées
    private boolean jsonPrinted; // Indique si le JSON a déjà été imprimé pour le dernier appui

    public Test() {
        this.keyTimePairs = new ArrayList<>();
        this.tank = new Tank(new Coordinate(100, 100)); // Initialisation du tank avec une position de départ
        this.pressedKeys = new HashSet<>(); // Initialisation de l'ensemble des touches pressées
        this.jsonPrinted = false; // Initialisation de l'état d'impression du JSON
    }

    public List<KeyTimePair> getKeyTimePairs() {
        return keyTimePairs;
    }

    public HashSet<String> getPressedKeys() {
        return pressedKeys;
    }

    public void startKeyListener() {
        JFrame frame = new JFrame("Test KeyListener");
        frame.addKeyListener(this);
        frame.setSize(200, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String key = KeyEvent.getKeyText(e.getKeyCode());
        System.out.println("Key pressed: " + key); // Debug: Log key press

        // Ajoute la touche à la liste si elle n'est pas déjà pressée
        if (!pressedKeys.contains(key)) {
            pressedKeys.add(key); // Marque la touche comme pressée
            if (key.equals(Constants.up)) {
                keyTimePairs.add(new KeyTimePair(Constants.up, 0));
            } else if (key.equals(Constants.down)) {
                keyTimePairs.add(new KeyTimePair(Constants.down, 0));
            } else if (key.equals(Constants.left)) {
                keyTimePairs.add(new KeyTimePair(Constants.left, 0));
            } else if (key.equals(Constants.right)) {
                keyTimePairs.add(new KeyTimePair(Constants.right, 0));
            } else if (key.equals(Constants.shoot)) {
                keyTimePairs.add(new KeyTimePair(Constants.shoot, 0));
            } else if (key.equals("P")) { // Touche pour simuler la nouvelle position
                System.out.println("Key 'P' detected."); // Debug: Log "P" key detection
                simulateTankMovement();
            }

            // Imprime le JSON une seule fois pour ce nouvel appui
            if (!jsonPrinted) {
                printKeyTimePairs();
                jsonPrinted = true; // Marque le JSON comme imprimé
            }
        }
    }

    private void simulateTankMovement() {
        // Convertit keyTimePairs en ArrayList<KeyTimePair> pour Tank
        ArrayList<KeyTimePair> tankKeyTimePairs = new ArrayList<>(keyTimePairs);

        // Simule la nouvelle position du tank en fonction des touches pressées
        tank.setKeyTime(tankKeyTimePairs);
        tank.simulatePosition();
        tank.handleBulletExpiration(); // Gère l'expiration des balles
        System.out.println("__________________________________________________");
        System.out.println("------------------Position------------------");
        // Affiche la nouvelle position du tank
        System.out.println("Nouvelle position du tank "+ tank.getId()+" : " + tank.getPosition());
        System.out.println("Nouvelle angle du tank : " + tank.getAngle() * 180 / Math.PI + "°");
        System.out.println("Nouvelle angle du tank : " + tank.getAngle());

        tank.updateAtomic();

        System.out.println("Position Atomique "+ tank.getId()+" : " + tank.getAtomPos());
        System.out.println("Angle Atomique : " + tank.getAtomAngle() * 180 / Math.PI + "°");
        System.out.println("Angle Atomique " + tank.getAtomAngle());

        System.out.println("------------------Balles------------------");
        for(Bullet b : tank.getBulletList()) {
            b.updatePosition(); // Met à jour la position de chaque balle
            System.out.println("Balle " + b.getId() + " : " + b.getPosition());
        }
        // Vide la liste keyTimePairs
        keyTimePairs.clear();
        System.out.println("__________________________________________________");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        String key = KeyEvent.getKeyText(e.getKeyCode());
        pressedKeys.remove(key); // Marque la touche comme relâchée

        // Réinitialise l'état d'impression du JSON lorsque toutes les touches sont relâchées
        if (pressedKeys.isEmpty()) {
            jsonPrinted = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // System.out.println("Key typed: " + e.getKeyChar());
    }

    public void printKeyTimePairs() {
        System.out.println("Current KeyTimePairs:");
        JsonWriter jsonWriter = new JsonWriter();
        System.out.println(jsonWriter.keyTimePairListToJson(keyTimePairs));
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.startKeyListener();
    }
}