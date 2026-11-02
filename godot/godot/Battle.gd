extends Node3D

var enemy_hp: int = 500
var turn: int = 1
var hp_label: Label
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
	create_battle_ui()

func hero_attack(hero_index: int):
	if enemy_hp <= 0:
		return

	var hero = heroes[hero_index]
	enemy_hp -= hero["damage"]
hp_label.text = "Ejderha Canı: " + str(max(enemy_hp, 0))
	print(hero["name"], " saldırdı!")
	print("Ejderha kalan canı: ", max(enemy_hp, 0))

	if enemy_hp <= 0:
		print("EJDERHA YENİLDİ!")
hp_label.text = "ZAFER! EJDERHA YENİLDİ!"
	else:
		enemy_turn()

func enemy_turn():
	turn += 1
	print("Ejderha karşı saldırı yaptı! Tur: ", turn)
func create_battle_ui():
	var layer = CanvasLayer.new()
	add_child(layer)

	var panel = PanelContainer.new()
	panel.position = Vector2(20, 20)
	panel.custom_minimum_size = Vector2(350, 0)
	layer.add_child(panel)

	var box = VBoxContainer.new()
	panel.add_child(box)

	var title = Label.new()
	title.text = "GG - EJDERHA SAVAŞI"
	box.add_child(title)

	hp_label = Label.new()
	hp_label.name = "EnemyHP"
	hp_label.text = "Ejderha Canı: " + str(enemy_hp)
	box.add_child(hp_label)

	for i in range(heroes.size()):
		var button = Button.new()
		button.text = heroes[i]["name"] + " - Saldır"
		button.custom_minimum_size = Vector2(300, 55)
		button.pressed.connect(hero_attack.bind(i))
		box.add_child(button)
