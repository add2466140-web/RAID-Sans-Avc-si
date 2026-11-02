extends Node3D

var enemy_hp: int = 500
var turn: int = 1

var heroes = [
	{"name": "Ardan", "damage": 45},
	{"name": "Bruna", "damage": 40},
	{"name": "Lyra", "damage": 65},
	{"name": "Kael", "damage": 70},
	{"name": "Vex", "damage": 60}
]

func _ready():
	print("GG SAVAŞ BAŞLADI!")
	print("Ejderha canı: ", enemy_hp)

func hero_attack(hero_index: int):
	if enemy_hp <= 0:
		return

	var hero = heroes[hero_index]
	enemy_hp -= hero["damage"]

	print(hero["name"], " saldırdı!")
	print("Ejderha kalan canı: ", max(enemy_hp, 0))

	if enemy_hp <= 0:
		print("EJDERHA YENİLDİ!")
	else:
		enemy_turn()

func enemy_turn():
	turn += 1
	print("Ejderha karşı saldırı yaptı! Tur: ", turn)
