import socket
import json
import time
import threading
import pygame
import math


#javac -cp ".;libs\gson-2.10.1.jar" *.java
#java -cp ".;libs\gson-2.10.1.jar" Serveur


# -------------------- CONFIG --------------------
ID_TANK = 1
scale_factor = 30
cell_size = 12 * scale_factor
SCREEN_WIDTH = 800
SCREEN_HEIGHT = 600
Keys = ["Z", "Q", "S", "D", "Space", "Escape", "R"]
HOST = 'localhost'  # Adresse IP du serveur
# ------------------------------------------------

# -------------------- GAME STATE --------------------
class GameState:
    def __init__(self):
        self.lock = threading.Lock()
        self.state = {"tanks": [], "bullets": [], "obstacles": []}
        

    def update(self, new_state):
        with self.lock:
            self.state = new_state

    def get(self):
        with self.lock:
            return self.state.copy()

# ----------------------------------------------------

# -------------------- CLIENT JEU --------------------
class ClientGame:
    def __init__(self, host=HOST, port=5000):
        self.game_state = GameState()
        self.host = host
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.pressed_keys = set()
        self.lock = threading.Lock()
        self.running = True
        self.init_obstacles = [] 
        self.id = None # Initialisé dans receive_loop
        self.screen = None  # Initialisé dans run_game_loop


    def connect(self):
        try:
            self.sock.connect((self.host, self.port))
            print(" Connecté au serveur.")
        except Exception as e:
            print(" Impossible de se connecter :", e)
            self.running = False
            return

        threading.Thread(target=self.receive_loop, daemon=True).start()
        threading.Timer(0.05, self.send_pressed_keys_loop).start()


    def receive_loop(self):
        buffer = ""
        while self.running:
            try:
                data = self.sock.recv(8192)
                if not data:
                    break
                buffer += data.decode('utf-8')
                while '\n' in buffer:
                    line, buffer = buffer.split('\n', 1)
                    if not line.strip():
                        continue
                    try:
                        json_data = json.loads(line)
                        # ...traitement JSON comme avant...
                        if json_data.get("type") == "init":
                            self.id = json_data.get("id")
                            raw_map = json_data.get("map", {})
                            self.init_obstacles = []
                            for coord_list in raw_map.values():
                                for coord in coord_list:
                                    x = coord["x"] * scale_factor
                                    y = coord["y"] * scale_factor
                                    self.init_obstacles.append((x, y))
                            print("Carte initialisée")
                        elif json_data.get("type") == "routine":
                            # print(json_data)
                            self.game_state.update({
                                "tanks": json_data.get("tanks", []),
                                "bullets": json_data.get("bullets", []),
                                "obstacles": json_data.get("obstacles", [])
                            })
                    except json.JSONDecodeError as e:
                        print("Erreur de décodage JSON :", e)
                    except Exception as e:
                        print("Erreur interne lors du traitement de la ligne :", e)
            except Exception as e:
                print("Erreur de réception :", e)
                self.running = False
                break




    def send_pressed_keys_loop(self):
        if not self.running:
            return
        self.send_pressed_keys()
        threading.Timer(0.02, self.send_pressed_keys_loop).start()

                
    def send_pressed_keys(self):
        with self.lock:
            if self.pressed_keys:
                # Vérifie que self.screen est bien défini
                if not hasattr(self, "screen"):
                    return

                # Position souris
                mouse_x, mouse_y = pygame.mouse.get_pos()

                # Centre de l’écran
                center_x = self.screen.get_width() // 2
                center_y = self.screen.get_height() // 2

                # Vecteur direction souris-centre
                dx = mouse_x - center_x
                dy = mouse_y - center_y

                # Angle en radians (y inversé car l’axe Y est vers le bas en Pygame)
                angle = math.atan2(dy, dx)

                # Prépare les données
                key_angle_pairs = [{"key": key, "time": angle} for key in self.pressed_keys]
                json_data = json.dumps(key_angle_pairs)

                try:
                    self.sock.sendall((json_data + "\n").encode('utf-8'))
                    print("Envoi JSON :", json_data)
                except Exception as e:
                    print("❌ Erreur d’envoi :", e)

            # Envoi clic séparément
            if "click" in self.pressed_keys:
                try:
                    click_data = json.dumps([{"key": "click", "time": angle}])
                    self.sock.sendall((click_data + "\n").encode('utf-8'))
                except Exception as e:
                    print("❌ Erreur d’envoi clic :", e)
                self.pressed_keys.discard("click")

    def get_camera_offset(self, x, y):
        return x - SCREEN_WIDTH // 2, y - SCREEN_HEIGHT // 2

    def draw_floor(self, surface, camera_offset, floor_img):
        floor_w, floor_h = floor_img.get_size()
        start_x = int(camera_offset[0] // floor_w) * floor_w
        start_y = int(camera_offset[1] // floor_h) * floor_h

        for x in range(start_x, int(camera_offset[0] + SCREEN_WIDTH), floor_w):
            for y in range(start_y, int(camera_offset[1] + SCREEN_HEIGHT), floor_h):
                surface.blit(floor_img, (x - camera_offset[0], y - camera_offset[1]))
    
    def draw_tanks(self, surface, tanks, camera_offset, tank_img):
        img_w, img_h = tank_img.get_size()
        for tank in tanks:
            x = tank["position"]["x"] * scale_factor
            y = tank["position"]["y"] * scale_factor
            angle_deg = -math.degrees(tank["angle"])
            rotated = pygame.transform.rotate(tank_img, angle_deg)
            rect = rotated.get_rect(center=(x - camera_offset[0], y - camera_offset[1]))
            surface.blit(rotated, rect)

    def draw_obstacles(self, surface, obstacles, camera_offset, img):
        img_w, img_h = img.get_size()
        for obs in obstacles:
            if isinstance(obs, tuple):
                x, y = obs
            else:
                x = obs["position"]["x"] * scale_factor
                y = obs["position"]["y"] * scale_factor
            # Décale pour centrer l'image sur la position logique
            surface.blit(img, (x - camera_offset[0] - img_w // 2, y - camera_offset[1] - img_h // 2))


    def draw_bullets(self, surface, bullets, camera_offset):
        radius = 5
        for bullet in bullets:
            x = bullet["position"]["x"] * scale_factor
            y = bullet["position"]["y"] * scale_factor
            screen_x = int(x - camera_offset[0])
            screen_y = int(y - camera_offset[1])
            pygame.draw.circle(surface, (128, 0, 255), (screen_x, screen_y), radius)

    def run_game_loop(self):
        pygame.init()
        screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
        self.screen = screen  # REND LE SCREEN ACCESSIBLE PARTOUT
        pygame.display.set_caption("CAWAR - Game")
        clock = pygame.time.Clock()

        # Chargement images
        ENEMY_IMAGE = pygame.image.load("images/cube.png").convert_alpha()
        ENEMY_IMAGE = pygame.transform.scale(ENEMY_IMAGE, (scale_factor*1.5, scale_factor*1.5))
        TANK_IMAGE = pygame.image.load("images/tankbody.png").convert_alpha()
        TANK_IMAGE = pygame.transform.rotate(TANK_IMAGE, 90)
        TANK_IMAGE = pygame.transform.scale(TANK_IMAGE, (scale_factor * 3, scale_factor * 2))
        FLOOR_IMAGE = pygame.image.load("images/floor1.png").convert_alpha()
        FLOOR_IMAGE = pygame.transform.scale(FLOOR_IMAGE, (100, 100))

        while self.running:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    self.running = False

                # gestion du clic souris
                elif event.type == pygame.MOUSEBUTTONDOWN:
                    # event.button : 1 gauche, 2 molette, 3 droite
                    if event.button == 1:  # clic gauche
                        # on peut directement déclencher un shoot
                        with self.lock:
                            # tu peux stocker un tuple (key, mouse_pos)
                            self.pressed_keys.add("click")
                    continue

                elif event.type == pygame.KEYDOWN:
                    key = pygame.key.name(event.key).capitalize()
                    if key in Keys or True:
                        with self.lock:
                            self.pressed_keys.add(key)
                elif event.type == pygame.KEYUP:
                    key = pygame.key.name(event.key).capitalize()
                    with self.lock:
                        self.pressed_keys.discard(key)

            screen.fill((0, 0, 0))
            state = self.game_state.get()

            tanks = state.get("tanks", [])
            bullets = state.get("bullets", [])
            obstacles = state.get("obstacles", [])

            player_tank = next((t for t in tanks if t["id"] == self.id), None)
            if player_tank:
                px = player_tank["position"]["x"] * scale_factor
                py = player_tank["position"]["y"] * scale_factor
            else:
                px, py = 0, 0

            offset = self.get_camera_offset(px, py)

            self.draw_floor(screen, offset, FLOOR_IMAGE)
            all_obstacles = self.init_obstacles + state.get("obstacles", [])
            self.draw_obstacles(screen, all_obstacles, offset, ENEMY_IMAGE)

            self.draw_tanks(screen, tanks, offset, TANK_IMAGE)
            self.draw_bullets(screen, bullets, offset)
            # print("Tanks :", self.id)

            # Affichage des coordonnées du tank en haut à droite
            if player_tank:
                font = pygame.font.SysFont(None, 28)
                pos_text = f"({player_tank['position']['x']:.2f}, {player_tank['position']['y']:.2f})"
                text_surf = font.render(pos_text, True, (238,130,238))
                text_rect = text_surf.get_rect(topright=(SCREEN_WIDTH - 10, 10))
                screen.blit(text_surf, text_rect)

            pygame.display.flip()
            clock.tick(60)
            if 'Escape' in self.pressed_keys:
                self.running = False

        pygame.quit()
        self.sock.close()
        print(" Déconnecté proprement")
        

# -------------------- MAIN --------------------
if __name__ == "__main__":
    client = ClientGame()
    client.connect()
    if client.running:
        client.run_game_loop()
