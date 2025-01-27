import pygame
import random
import math
from collections import defaultdict

scale_factor = 30 #ceci represente la taille des enemies (carré)


####### CREA DICT DE TEST


cell_size = 12*scale_factor   # valeur a changer pour la demo ! (12 pour le vrai et 5 pour montrer le fonctionnement)


obstacles = []
for i in range (0, 50000):
    x = random.randint(0,1000*scale_factor)
    y = random.randint(0,1000*scale_factor)
    obstacles.append([x,y])
       

grid_test = defaultdict(list)
for x, y in obstacles:
    cell_x = x // cell_size
    cell_y = y // cell_size
    grid_test[(cell_x, cell_y)].append((x, y))



####### FIN CREA DICT DE TEST




# Initialisation de Pygame
pygame.init()

# Définir les dimensions de la fenêtre
SCREEN_WIDTH = 800
SCREEN_HEIGHT = 600
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
clock = pygame.time.Clock()

class Player(pygame.sprite.Sprite):
    def __init__(self, x, y):
        super().__init__()
        self.original_image = pygame.image.load("images/tankbody.png").convert_alpha()
        self.original_image = pygame.transform.rotate(self.original_image, 90)
        self.original_image = pygame.transform.scale(self.original_image, (scale_factor*3, scale_factor*2))

        self.image = self.original_image  # Copie de l'image originale
        self.rect = self.image.get_rect(center=(x, y))  # Centrage initial
        self.x = x
        self.y = y
        self.angle = 0  # Angle initial de rotation

    def update(self):
        keys = pygame.key.get_pressed()
        can_move = True  #ce bool sert a avoir qu'une action possible, soit on tourne, soit on avance/recule, comme un vrai tank quoi !
       
        # Gestion des mouvements de rotation
        if keys[pygame.K_LEFT] and can_move == True:  # Tourner à gauche
            self.angle += 5  # Augmente l'angle de rotation
            can_move = False
        if keys[pygame.K_RIGHT] and can_move == True:  # Tourner à droite
            self.angle -= 5  # Diminue l'angle de rotation
            can_move = False
        # Gestion des mouvements avant/arrière

        if keys[pygame.K_UP] and can_move == True:  # Avancer
            self.x += 5 * math.cos(math.radians(self.angle))
            self.y -= 5 * math.sin(math.radians(self.angle))
            can_move = False
        if keys[pygame.K_DOWN] and can_move == True:  # Reculer
            self.x -= 5 * math.cos(math.radians(self.angle))
            self.y += 5 * math.sin(math.radians(self.angle))
            can_move = False

        if keys[pygame.K_a]: #bouton de test
            print(f"Position du joueur actuelle : {self.x}, {self.y}")
        # Appliquer la rotation
        self.image = pygame.transform.rotate(self.original_image, self.angle)
        self.rect = self.image.get_rect(center=(SCREEN_WIDTH // 2 , SCREEN_HEIGHT // 2 ))  # Réalignez le centre après la rotation

    def draw(self, surface):
        surface.blit(self.image, self.rect)




# Créer une instance de Player
player = Player(SCREEN_WIDTH // 2 , SCREEN_HEIGHT // 2 +75)  # + 75 pour mieux centrer le sprit tank

# Ajouter le sprite dans un groupe
sprites = pygame.sprite.Group()
sprites.add(player)



# Optimisation des accès disques sinon 500000 accès disque la ça fait planter
ENEMY_IMAGE = pygame.image.load("images/Barbed_Wire.png").convert_alpha()
ENEMY_IMAGE = pygame.transform.scale(ENEMY_IMAGE, (scale_factor, scale_factor))



class Enemy(pygame.sprite.Sprite): #classe des obstacles, ne pas se méprendre avec le nom
    def __init__(self, x, y, player):
        super().__init__()
        # Charger l'image de l'ennemi
        self.image = ENEMY_IMAGE
        self.rect = self.image.get_rect()
        self.rect.topleft = (x, y)
        self.x = x
        self.y = y
        self.player = player
        self.angle = 0
        self.distance = math.sqrt(pow(y-player.y,2) + pow(x-player.x,2))

    def draw(self, screen, camera_offset):
        # Calculer la position relative à la caméra
        screen_pos = (self.x - camera_offset[0], self.y - camera_offset[1])


        # Dessiner l'image sur l'écran à la position calculée
        screen.blit(self.image, screen_pos)

# Créer plusieurs ennemis
enemies = pygame.sprite.Group()
grid_test_enemie = defaultdict(list)
for x, y in obstacles:
    enemy = Enemy(x,y,player)
    enemies.add(enemy)
    cell_x = x // cell_size
    cell_y = y // cell_size
    grid_test_enemie[(cell_x, cell_y)].append(enemy)


def get_camera_offset(player):
    return player.x - 400, player.y - 300  # Décalage pour centrer sur l'écran (800x600)




#### partie cannon
# Classe Cannon
class Cannon(pygame.sprite.Sprite):
    def __init__(self):
        super().__init__()
        self.image = pygame.image.load("images/tankhead.png").convert_alpha()
        self.image = pygame.transform.scale(self.image, (50, 50))
        self.original_image = self.image  # On garde une copie de l'image originale
        self.rect = self.image.get_rect()
        self.position = (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 )  # Position du pivot du canon

    def update(self, mouse_pos):
        # Calcul de l'angle entre le pivot du canon et la souris
        dx = mouse_pos[0] - self.position[0]
        dy = mouse_pos[1] - self.position[1]
        angle = math.degrees(math.atan2(-dy, dx)) - 90  # On soustrait 90 degrés pour corriger l'orientation

        # Rotation de l'image autour du pivot
        self.image = pygame.transform.rotate(self.original_image, angle)

        # Mettre à jour le rectangle pour qu'il soit centré sur le pivot
        self.rect = self.image.get_rect(center=self.position)

    def draw(self, surface):
        surface.blit(self.image, self.rect)

cannon = Cannon()



## Partie sol
FLOOR_IMAGE = pygame.image.load("images/floor.png").convert_alpha()
FLOOR_IMAGE = pygame.transform.scale(FLOOR_IMAGE, (100, 100))  # Adapter la taille si nécessaire
def draw_floor(surface, camera_offset):
    # Dimensions de l'image du sol
    floor_width, floor_height = FLOOR_IMAGE.get_size()

    # Calcul des coordonnées de départ pour le dessin et les convertir en entiers
    start_x = int(camera_offset[0] // floor_width) * floor_width
    start_y = int(camera_offset[1] // floor_height) * floor_height

    floor_width = int(floor_width)
    floor_height = int(floor_height)
    # Dessiner le sol en mosaïque dans la zone visible
    for x in range(start_x, int(camera_offset[0] + SCREEN_WIDTH), floor_width):
        for y in range(start_y, int(camera_offset[1] + SCREEN_HEIGHT), floor_height):
            surface.blit(FLOOR_IMAGE, (x - camera_offset[0], y - camera_offset[1]))



# Boucle principale
running = True
while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

    screen.fill((0, 0, 0))

    # Calcul de l'offset de la caméra
    camera_offset = get_camera_offset(player)
    draw_floor(screen, camera_offset)
    # Obtenir la position de la souris
    mouse_position = pygame.mouse.get_pos()
    # Mettre à jour le canon
    cannon.update(mouse_position)
    player.update()

    # Mise à jour des ennemis proches a l'aide du chunk
    enemies_to_draw = []
    for dx in [-1, 0, 1]:
        for dy in [-1, 0, 1]:
            chunk_x = player.x // cell_size + dx
            chunk_y = player.y // cell_size + dy
            enemies_to_draw.extend(grid_test_enemie.get((chunk_x, chunk_y), []))

    for enemy in enemies_to_draw:
        enemy.draw(screen, camera_offset)

    # Mise à jour du joueur
    player.draw(screen)
    cannon.draw(screen)

    pygame.display.flip()
    clock.tick(60)
    # Partie transmission des données au serveur (touche cliqué et a t'il tirer ?)

pygame.quit()