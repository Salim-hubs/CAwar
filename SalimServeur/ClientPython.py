import socket
import json
import time
import threading
import pygame

class Client:
    def __init__(self, host='localhost', port=5000):
        self.host = host
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.pressed_keys = set()
        self.lock = threading.Lock()
        self.running = True

    def connect(self):
        self.sock.connect((self.host, self.port))
        print("🔗 Connecté au serveur.")

        # Thread pour recevoir les messages
        threading.Thread(target=self.receive_loop, daemon=True).start()

        # Timer pour envoyer les touches toutes les 200ms
        threading.Timer(0.2, self.send_pressed_keys_loop).start()

        # Lancer pygame pour gérer les inputs clavier
        self.start_input_window()

    def receive_loop(self):
        while self.running:
            try:
                data = self.sock.recv(4096)
                if not data:
                    break
                print("📥 Reçu du serveur :", data.decode('utf-8'))
            except Exception as e:
                print("❌ Erreur de réception :", e)
                break

    def send_pressed_keys_loop(self):
        if not self.running:
            return
        self.send_pressed_keys()
        threading.Timer(0.2, self.send_pressed_keys_loop).start()

    def send_pressed_keys(self):
        with self.lock:
            if self.pressed_keys:
                timestamp = int(time.time() * 1000)
                key_time_pairs = [{"key": key, "timestamp": timestamp} for key in self.pressed_keys]
                json_data = json.dumps(key_time_pairs)
                try:
                    self.sock.sendall((json_data + "\n").encode('utf-8'))
                    print("📤 Envoi JSON :", json_data)
                except Exception as e:
                    print("❌ Erreur d’envoi :", e)

    def start_input_window(self):
        pygame.init()
        screen = pygame.display.set_mode((200, 200))
        pygame.display.set_caption("Client CAWAR")

        while self.running:
            for event in pygame.event.get():
                with self.lock:
                    if event.type == pygame.QUIT:
                        self.running = False
                    elif event.type == pygame.KEYDOWN:
                        key = pygame.key.name(event.key).capitalize()
                        self.pressed_keys.add(key)
                    elif event.type == pygame.KEYUP:
                        key = pygame.key.name(event.key).capitalize()
                        self.pressed_keys.discard(key)

        pygame.quit()
        self.sock.close()
        print("🔌 Déconnecté du serveur.")

if __name__ == "__main__":
    client = Client()
    client.connect()
