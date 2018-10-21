package de.officialwayn.mcsnow.snowballfight;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.officialwayn.mcsnow.Main;
import de.officialwayn.mcsnow.utils.ItemCreator;

public class SnowballFight {

	private ArrayList<Player> spieler;
	private String status;
	private Player owner;
	private String ownername;

	private HashMap<Player, ItemStack[]> playerinventory;
	private HashMap<Player, ItemStack[]> playerarmor;

	private HashMap<Player, Integer> exp;

	private HashMap<Player, Location> locs;

	private int count = 61;
	private int cc;

	public SnowballFight(Player owner) {
		this.spieler = new ArrayList<>();
		this.owner = owner;
		this.status = "lobby";
		this.ownername = owner.getDisplayName();
		this.playerinventory = new HashMap<>();
		this.playerarmor = new HashMap<>();
		this.exp = new HashMap<>();
		this.locs = new HashMap<>();
		startCount();
		addPlayer(owner);
	}

	public void startCount() {
		cc = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {

			@Override
			public void run() {
				if (count != 0) {
					count--;
				}

				if (count == 60 || count == 30 || count == 20 || count == 10 || count == 5 || count == 3 || count == 2
						|| count == 1) {
					if (count == 20) {
						if (getSpieler().size() < 2) {
							count = 61;
							sendMessage("§cEs werden mind. 3 Spieler benötigt!");
							return;

						}
					}
					sendMessage("§7Das Spiel beginnt in §b" + count + " §7Sekunde" + (count != 1 ? "n" : ""));

				} else if (count == 0) {
					if (getSpieler().size() < 2) {

						count = 61;
						sendMessage("§cEs werden mind. 3 Spieler benötigt!");
						return;

					}
					Bukkit.getScheduler().cancelTask(cc);
					setStatus("ingame");
					for (Player players : Bukkit.getOnlinePlayers()) {
						if (!getSpieler().contains(players)) {
							getSpieler().forEach(spieler -> spieler.hidePlayer(players));
							getSpieler().forEach(spieler -> players.hidePlayer(spieler));
						} else {
							players.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 16));
						}
					}

					sendMessage(
							"§7Los gehts! §bSammel Schneebälle indem du auf Schnee rechtsklickst. §c§lVersuche zu überleben und gewinne einen tollen Preis!");
				}
			}
		}, 20, 20);
	}

	public void checkEnd() {
		if (this.spieler.size() == 1) {
			if (!this.status.equals("lobby")) {
				Bukkit.broadcastMessage(Main.getPrefix() + "§7Die §bSchneeballschlacht §7von " + ownername + " §7hat "
						+ this.spieler.get(0).getDisplayName() + " §7gewonnen");

				Player p = this.spieler.get(0);
				teleportToLocation(p);

				p.getInventory().clear();
				p.getInventory().setArmorContents(null);

				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {

					@Override
					public void run() {
						ItemStack[] inv = getPlayerinventory().get(p);
						ItemStack[] armor = getPlayerarmor().get(p);

						p.getInventory().setContents(inv);
						p.getInventory().setArmorContents(armor);
						ItemStack is = ItemCreator.createItem(Material.CHEST, 1, "§5Wundertüte");
						p.getInventory().addItem(is);
						if (SnowballFightEvent.level.containsKey(p)) {
							p.setLevel(SnowballFightEvent.level.get(p));
							SnowballFightEvent.level.remove(p);
						}
					}
				}, 20);

				for (Player players : Bukkit.getOnlinePlayers()) {
					players.showPlayer(p);
					p.showPlayer(players);
				}

				SnowBallFightUtil.removeGame(owner);
			}
		}
	}

	public void crashGame() {
		for (Player players : Bukkit.getOnlinePlayers()) {
			for (Player players2 : this.spieler) {
				teleportToLocation(players2);
				players2.sendMessage(Main.getPrefix() + "§cDie Schneeballschlacht wurde abgebrochen.");
				players.showPlayer(players2);
				players2.showPlayer(players);

				players2.getInventory().clear();
				players2.getInventory().setArmorContents(null);

				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {

					@Override
					public void run() {
						ItemStack[] inv = getPlayerinventory().get(players2);
						ItemStack[] armor = getPlayerarmor().get(players2);

						players2.getInventory().setContents(inv);
						players2.getInventory().setArmorContents(armor);
					}
				}, 20);

			}
		}

		SnowBallFightUtil.removeGame(owner);
		System.out.println("test");

	}

	public void sendMessage(String msg) {
		this.spieler.forEach(players -> players.sendMessage(Main.getPrefix() + msg));
	}

	public void removePlayer(Player p, Player p2) {
		if (this.spieler.contains(p)) {
			teleportToLocation(p);
			if (getStatus().equals("lobby")) {
				p.sendMessage(Main.getPrefix() + "§cDu hast das Spiel verlassen");
				sendMessage(p.getDisplayName() + " §7hat das Spiel verlassen");
			} else {
				getSpieler().forEach(players -> players.hidePlayer(p));
				for (Player players : Bukkit.getOnlinePlayers()) {
					players.showPlayer(p);
					p.showPlayer(players);
				}
			}

			p.getInventory().clear();
			p.getInventory().setArmorContents(null);

			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {

				@Override
				public void run() {
					ItemStack[] inv = getPlayerinventory().get(p);
					ItemStack[] armor = getPlayerarmor().get(p);

					p.getInventory().setContents(inv);
					p.getInventory().setArmorContents(armor);
				}
			}, 20);

			this.spieler.remove(p);

		}
	}

	public HashMap<Player, Integer> getExp() {
		return exp;
	}

	public HashMap<Player, Location> getLocs() {
		return locs;
	}

	public void teleportToLocation(Player p) {
		if (this.locs.containsKey(p)) {
			p.teleport(this.locs.get(p));
		}
	}

	public void addPlayer(Player p) {
		if (!spieler.contains(p) && !SnowBallFightUtil.inGame(p)) {
			this.spieler.add(p);
			SnowballFightEvent.level.put(p, p.getLevel());

			if (p != owner) {
				p.teleport(this.owner.getLocation());
				this.locs.put(p, p.getLocation());
			}
			sendMessage(p.getDisplayName() + " §7hat das Spiel betreten.");
			ItemStack[] inv = p.getInventory().getContents();
			ItemStack[] armor = p.getInventory().getArmorContents();

			getPlayerinventory().put(p, inv);
			getPlayerarmor().put(p, armor);

			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setLevel(0);
			p.setHealth(20.0);
			p.setFoodLevel(20);

		} else {
			p.sendMessage(Main.getPrefix() + "§cDu bist bereits in einem Spiel!");
		}
	}

	public ArrayList<Player> getSpieler() {
		return spieler;
	}

	public void setSpieler(ArrayList<Player> spieler) {
		this.spieler = spieler;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public String getOwnername() {
		return ownername;
	}

	public void setOwnername(String ownername) {
		this.ownername = ownername;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCc() {
		return cc;
	}

	public void setCc(int cc) {
		this.cc = cc;
	}

	public HashMap<Player, ItemStack[]> getPlayerinventory() {
		return playerinventory;
	}

	public void setPlayerinventory(HashMap<Player, ItemStack[]> playerinventory) {
		this.playerinventory = playerinventory;
	}

	public HashMap<Player, ItemStack[]> getPlayerarmor() {
		return playerarmor;
	}

	public void setPlayerarmor(HashMap<Player, ItemStack[]> playerarmor) {
		this.playerarmor = playerarmor;
	}

}
