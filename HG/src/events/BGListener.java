package events;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import main.BGMain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.CropState;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

import utilities.BGChat;
import utilities.BGCooldown;
import utilities.BGCornucopia;
import utilities.BGDisguise;
import utilities.BGFBattle;
import utilities.BGFeast;
import utilities.BGFiles;
import utilities.BGKit;
import utilities.BGReward;
import utilities.BGSign;
import utilities.BGTeam;
import utilities.BGVanish;
import utilities.Updater;

public class BGListener implements Listener {
	Logger log = BGMain.getPluginLogger();
	private String last_quit;
	private String last_headshot;
	
	public static ArrayList<Player> viperList = new ArrayList<Player>();
	public static ArrayList<Player> monkList = new ArrayList<Player>();
	public static ArrayList<Player> thiefList = new ArrayList<Player>();
	public static ArrayList<Player> ghostList = new ArrayList<Player>();
	public static ArrayList<Player> thorList = new ArrayList<Player>();
	public static ArrayList<Player> timeList = new ArrayList<Player>();
	public static ArrayList<Player> freezeList = new ArrayList<Player>();

	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		if(BGMain.isSpectator(p))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event) {
		if (BGMain.DENY_CHECK_WORLDBORDER.booleanValue()) {
			return;
		}
		if (BGMain.inBorder(event.getTo()))
			return;
		Vehicle s = event.getVehicle();
		if (s.isEmpty())
			return;
		Entity Passenger = s.getPassenger();
		if (!(Passenger instanceof Player))
			return;

		BGChat.printPlayerChat((Player) Passenger, BGMain.WORLD_BORDER_MSG);
		s.teleport(event.getFrom());
		Bukkit.getServer().getWorlds().get(0)
				.playEffect(s.getLocation(), Effect.ENDER_SIGNAL, 5);
		Bukkit.getServer().getWorlds().get(0)
				.playEffect(s.getLocation(), Effect.SMOKE, 5);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Action a = event.getAction();
		if(BGMain.isSpectator(p)) {
			event.setCancelled(true);
			return;
		}
		
		if (BGMain.DENY_BLOCKBREAK.booleanValue()
				& (!p.hasPermission("bg.admin.editblocks") || !p.hasPermission("bg.admin.*"))) {
			event.setCancelled(true);
			return;
		}

		if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
			if ((BGKit.hasAbility(p, Integer.valueOf(5)) & p.getItemInHand()
					.getType() == Material.COOKIE)) {
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.INCREASE_DAMAGE, BGFiles.abconf.getInt("AB.5.Duration") * 20, 0));
				p.getInventory().removeItem(
						new ItemStack[] { new ItemStack(Material.COOKIE, 1) });
			}
		}
		
		if (a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR) {
			if (BGKit.hasAbility(p, Integer.valueOf(4)) && p.getItemInHand() != null && 
					p.getItemInHand().getType().equals(Material.FIREBALL)) {
				Player player = p;
				Fireball fire;
				Vector lookat = player.getLocation().getDirection().multiply(10);
				fire = player.getWorld().spawn(player.getLocation().add(lookat), Fireball.class);
				fire.setShooter(player);

				p.getInventory()
						.removeItem(
								new ItemStack[] { new ItemStack(
										Material.FIREBALL, 1) });
			}
		}
		
		try{
			if (BGKit.hasAbility(p, 11) && a == Action.RIGHT_CLICK_BLOCK && p.getItemInHand()
					.getType() == Material.STONE_AXE) {
				if(!thorList.contains(p)) {
					thorList.add(p);
					BGCooldown.thorCooldown(p);
					Block block = event.getClickedBlock();
					Location loc = block.getLocation();
					World world = Bukkit.getServer().getWorlds().get(0);
					world.strikeLightning(loc);
				}else {
					BGChat.printPlayerChat(p, BGFiles.abconf.getString("AB.11.Expired"));
				}
			}
			
			if (BGKit.hasAbility(p, 16) && p.getItemInHand()
					.getType() == Material.APPLE && (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)) {
				
				if(!ghostList.contains(p)) {
					ghostList.add(p);
					BGCooldown.ghostCooldown(p);
					
					Player[] players = Bukkit.getServer().getOnlinePlayers();
					
					BGCooldown.showPlayerCooldown(p, players);
					p.getInventory().removeItem(new ItemStack[] { new ItemStack(Material.APPLE, 1)});
					for(Player player : players) {
						if(player.getName().equals(p.getName())) {
							continue;
						}
						if(BGKit.hasAbility(player, 21)) {
							continue;
						}
						player.hidePlayer(p);
					}
					BGChat.printPlayerChat(p, BGFiles.abconf.getString("AB.16.invisible"));
				}else {
					BGChat.printPlayerChat(p, BGFiles.abconf.getString("AB.16.Expired"));
				}
			}
			if(BGKit.hasAbility(p, 22) && !BGMain.DENY_DAMAGE_PLAYER &&
					p.getItemInHand().getType() == Material.WATCH &&
					(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)) {
				
				if(!timeList.contains(p)) {
					timeList.add(p);
					BGCooldown.timeCooldown(p);
					
					p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.WATCH,1)});
					
					int radius = BGFiles.abconf.getInt("AB.22.radius");
					
					List<Entity> entities = p.getNearbyEntities(radius+30, radius+30, radius+30);
					for(Entity e : entities) {
						
						if(!e.getType().equals(EntityType.PLAYER) || BGMain.isSpectator((Player)e) || BGMain.isGameMaker((Player) e))
							continue;
						Player target = (Player) e;
						if(BGMain.TEAM) {
							
							if(BGTeam.isInTeam(p, target.getName()))
								continue;
						}
						if(p.getLocation().distance(target.getLocation()) < radius) {
							
							freezeList.add(target);
							String text = BGFiles.abconf.getString("AB.22.target");
							text = text.replace("<player>", p.getName());
							BGChat.printPlayerChat(target, text);
							BGCooldown.freezeCooldown(target);
						}	
					}
					BGChat.printPlayerChat(p, BGFiles.abconf.getString("AB.22.success"));
				}else {
					BGChat.printPlayerChat(p, BGFiles.abconf.getString("AB.22.Expired"));
				}
			}
		} catch(NullPointerException e) {
			
		} catch(Exception e) {
			e.printStackTrace();
		}

		if ((p.getItemInHand().getType() == Material.COMPASS & BGMain.COMPASS
				.booleanValue())) {
			Boolean found = Boolean.valueOf(false);
			for (int i = 0; i < 300; i++) {
				List<Entity> entities = p.getNearbyEntities(i, 64.0D, i);
				for (Entity e : entities) {
					if ((!e.getType().equals(EntityType.PLAYER))|| BGMain.isSpectator((Player) e) || BGMain.isGameMaker((Player) e))
						continue;
					if(BGMain.TEAM) {
						if(BGTeam.isInTeam(p, ((Player) e).getName()))
								continue;
					}
					p.setCompassTarget(e.getLocation());
					Double distance = p.getLocation().distance(
							e.getLocation());
					DecimalFormat df = new DecimalFormat("##.#");
					BGChat.printPlayerChat(p, "Tracking player \""
							+ ((Player) e).getName() + "\" | Distance: "
							+ df.format(distance));
					found = Boolean.valueOf(true);
					break;
				}

				if (found.booleanValue())
					break;
			}
			if (!found.booleanValue()) {
				BGChat.printPlayerChat(p,
						"No players in range. Compass points to spawn.");
				p.setCompassTarget(BGMain.spawn);
			}
		}		
	}

	@EventHandler
	public void onServerPing(ServerListPingEvent event) {
		if (BGMain.DENY_LOGIN)
			event.setMotd(BGMain.MOTD_PROGRESS_MSG.replace("&", "�"));
		else
			event.setMotd(BGMain.MOTD_COUNTDOWN_MSG.replace("&", "�").replace("<time>", BGMain.TIME(BGMain.COUNTDOWN)));
	}

	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent event) {
		if(BGMain.isSpectator(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		if (BGMain.DENY_BLOCKBREAK.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		if(BGMain.isSpectator(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		if (BGMain.DENY_BLOCKPLACE.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityShootArrow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player)
			if(BGMain.isSpectator((Player) event.getEntity())) {
				event.setCancelled(true);
				return;
			}
		if (((event.getEntity() instanceof Player)) && (BGMain.DENY_SHOOT_BOW.booleanValue())) {
			event.getBow().setDurability((short) 0);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile entity =  event.getEntity();

		if (entity.getType() == EntityType.ARROW) {
			Arrow arrow = (Arrow) entity;
			LivingEntity shooter = arrow.getShooter();
			if (shooter.getType() == EntityType.PLAYER) {
				Player player = (Player) shooter;
				if(BGMain.isSpectator(player)) {
					arrow.remove();
					return;
				}
				if (BGKit.hasAbility(player, Integer.valueOf(1))) {
					Bukkit.getServer().getWorlds().get(0).createExplosion(arrow.getLocation(), 2.0F, false);
					arrow.remove();
				} else {
					return;
				}
			} else {
				return;
			}
		}

		if (entity.getType() == EntityType.SNOWBALL) {
			Snowball ball = (Snowball) entity;
			LivingEntity shooter = ball.getShooter();
			if (shooter.getType() == EntityType.PLAYER) {
				Player player = (Player) shooter;
				if(BGMain.isSpectator(player)) {
					return;
				}
				if (BGKit.hasAbility(player, Integer.valueOf(3)).booleanValue()) {
					Bukkit.getServer().getWorlds().get(0)
							.createExplosion(ball.getLocation(), 0.0F);
					for (Entity e : ball.getNearbyEntities(3.0D, 3.0D, 3.0D))
						if ((e.getType() == EntityType.PLAYER)) {
							Player pl = (Player) e;
							if (pl.getName() != player.getName()) {
								pl.addPotionEffect(new PotionEffect(
										PotionEffectType.BLINDNESS, 100, 1));
								pl.addPotionEffect(new PotionEffect(
										PotionEffectType.CONFUSION, 160, 1));
							}
						}
				}
			} else {
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(BGMain.isSpectator(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		if (BGMain.DENY_ITEMDROP.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (BGMain.DENY_DAMAGE_ENTITY) {
			event.setCancelled(true);
			return;
		}
		
		ArrayList<Block> remove = new ArrayList<Block>();
		for(Block b : event.blockList()) {
			if(BGCornucopia.isCornucopiaBlock(b)) {
				remove.add(b);
				continue;
			}
			if(BGFeast.isFeastBlock(b)) {
				remove.add(b);
				continue;
			}
			if(BGFBattle.isBattleBlock(b)) {
				remove.add(b);
				continue;
			}
		}
		event.blockList().removeAll(remove);
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(BGMain.isSpectator(event.getPlayer()) || BGMain.isGameMaker(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		if (BGMain.DENY_ITEMPICKUP.booleanValue())
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		Player p = event.getPlayer();
		
		if(BGMain.isGameMaker(p))
			BGMain.remGameMaker(p);
		if(BGMain.isSpectator(p))
			BGMain.remSpectator(p);
		
		if (BGMain.DENY_LOGIN.booleanValue() || BGMain.ADV_CHAT_SYSTEM)
			event.setLeaveMessage(null);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();

		if (BGMain.DENY_LOGIN &&
				!(BGMain.SPECTATOR_SYSTEM && p.hasPermission("bg.spectator")) &&
				!(p.hasPermission("bg.admin.logingame") || p.hasPermission("bg.admin.*"))) {
			event.setKickMessage(ChatColor.RED + BGMain.GAME_IN_PROGRESS_MSG);
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, event.getKickMessage());
		} else if (event.getResult() == Result.KICK_FULL) {
			if (p.hasPermission("bg.vip.full") || p.hasPermission("bg.admin.full") 
					|| Bukkit.getServer().getMaxPlayers() > BGMain.getGamers().length) {
				event.allow();
			} else {
				event.setKickMessage(ChatColor.RED + BGMain.SERVER_FULL_MSG.replace("<players>", Integer.toString(Bukkit.getOnlinePlayers().length)));
			}
		}

		BGVanish.updateVanished();
	}

	@EventHandler
	public void onPlayerOutBorder(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (BGMain.DENY_CHECK_WORLDBORDER.booleanValue()) {
			return;
		}
		if (!BGMain.inBorder(event.getTo())) {
			if (BGMain.inBorder(event.getFrom())) {
				BGChat.printPlayerChat(p, BGMain.WORLD_BORDER_MSG);
				event.setTo(event.getFrom());
				p.teleport(event.getFrom());
				Bukkit.getServer().getWorlds().get(0)
						.playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 5);
				Bukkit.getServer().getWorlds().get(0)
						.playEffect(p.getLocation(), Effect.SMOKE, 5);
				return;
			}
			p.teleport(p.getWorld().getSpawnLocation().add(0.0D, 20.0D, 0.0D));
			
			Bukkit.getServer().getWorlds().get(0)
					.playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 5);
			
			Bukkit.getServer().getWorlds().get(0)
					.playEffect(p.getLocation(), Effect.SMOKE, 5);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		
		if (!BGMain.COMPASS.booleanValue() || !BGMain.AUTO_COMPASS.booleanValue())
			return;
		Boolean found = Boolean.valueOf(false);
		for (int i = 0; i < 300; i++) {
			List<Entity> entities = p.getNearbyEntities(i, 64.0D, i);
			for (Entity e : entities) {
				if ((e.getType().equals(EntityType.PLAYER)) && !BGMain.isSpectator((Player) e)) {
					p.setCompassTarget(e.getLocation());
					found = Boolean.valueOf(true);
					break;
				}
			}
			if (found.booleanValue())
				break;
		}
		if (!found.booleanValue()) {
			p.setCompassTarget(BGMain.spawn);
		}
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		Player p = e.getEntity().getKiller();
		if (BGKit.hasAbility(p, Integer.valueOf(7)).booleanValue()) {
			if (e.getEntityType() == EntityType.PIG) {
				e.getDrops().clear();
				e.getDrops().add(new ItemStack(Material.PORK, BGFiles.abconf.getInt("AB.7.Amount")));
			}
		}
	}

	@EventHandler
	public void onFall(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(BGMain.isSpectator(p))
				return;
			if (BGKit.hasAbility(p, Integer.valueOf(8))) {
				if (e.getCause() == DamageCause.FALL) {
					if (e.getDamage() > 4) {
						e.setCancelled(true);
						p.damage(4);
					}
					List<Entity> nearbyEntities = e.getEntity()
							.getNearbyEntities(5, 5, 5);
					for (Entity target : nearbyEntities) {
						if (target instanceof Player) {
							if(BGMain.isSpectator((Player) target) || BGMain.isGameMaker((Player) target))
								continue;
							Player t = (Player) target;
							if(BGMain.TEAM) {
								
								if(BGTeam.isInTeam(p, t.getName()))
									continue;
							}
							if (t.isSneaking())
								t.damage(e.getDamage() / 2, e.getEntity());
							else
								t.damage(e.getDamage(), e.getEntity());
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		BGVanish.updateVanished();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!BGMain.DENY_LOGIN & BGMain.ADV_CHAT_SYSTEM) {
			BGChat.printDeathChat("�e" + event.getJoinMessage());
		}

		if (BGMain.DENY_LOGIN || BGMain.ADV_CHAT_SYSTEM) {
			event.setJoinMessage(null);
		}

		Player p = event.getPlayer();
		
		p.getInventory().clear();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setExp(0);
		
		if (BGMain.DENY_LOGIN) {
			if (p.hasPermission("bg.admin.gamemaker") || p.hasPermission("bg.admin.*")) {
				BGMain.addGameMaker(p);
			} else if(BGMain.SPECTATOR_SYSTEM && p.hasPermission("bg.spectator")) {
				BGMain.addSpectator(p);
			}
		} else {
			if (!BGMain.ADV_CHAT_SYSTEM && !BGMain.ITEM_MENU)
				BGChat.printKitChat(p);
		}

		if(BGMain.isSpectator(p)) {
			p.setPlayerListName(ChatColor.GRAY + getShortStr(p.getName()) + ChatColor.RESET);
			p.setDisplayName(ChatColor.GRAY + p.getName() + ChatColor.RESET);
		} else if (BGMain.winner(p)) {
			p.setPlayerListName(ChatColor.GOLD + getShortStr(p.getName())
					+ ChatColor.RESET);
			p.setDisplayName(ChatColor.GOLD + p.getName() + ChatColor.RESET);
		} else if (p.hasPermission("bg.admin.color")
				|| p.hasPermission("bg.admin.*")) {
			p.setPlayerListName(ChatColor.RED + getShortStr(p.getName())
					+ ChatColor.RESET);
			p.setDisplayName(ChatColor.RED + p.getName() + ChatColor.RESET);
		} else if (p.hasPermission("bg.vip.color")
				|| p.hasPermission("bg.vip.*")) {
			p.setPlayerListName(ChatColor.BLUE + getShortStr(p.getName())
					+ ChatColor.RESET);
			p.setDisplayName(ChatColor.BLUE + p.getName() + ChatColor.RESET);
		} else {
			p.setPlayerListName(p.getName());
			p.setDisplayName(p.getName());
		}
		
		if(!BGMain.DENY_LOGIN) {
		//Creating a written book.
		List<String> pages = BGFiles.bookconf.getStringList("content");
		List<String> content = new ArrayList<String>();
		List<String> page = new ArrayList<String>();
		for(String line : pages)  {
			line = line.replace("<server_title>", BGMain.SERVER_TITLE);
			line = line.replace("<space>", "�r\n");
			line = line.replace("&", "�");
			if(!line.contains("<newpage>")) {
				page.add(line + "\n");
			} else {
				String pagestr = "";
				for(String l : page)
					pagestr = pagestr + l;
				content.add(pagestr);
				page.clear();
			}
		}
		String pagestr = "";
		for(String l : page)
			pagestr = pagestr + l;
		content.add(pagestr);	
		page.clear();
		
		ItemStack item = new ItemStack(387,1);
		BookMeta im = (BookMeta) item.getItemMeta();
			im.setPages(content);
			im.setAuthor(BGFiles.bookconf.getString("author"));
			im.setTitle(BGFiles.bookconf.getString("title"));
		item.setItemMeta(im);
		p.getInventory().addItem(item);
		}
		
		String playerName = p.getName();
		
		if (BGMain.SQL_USE) {
			Integer PL_ID = BGMain.getPlayerID(playerName);
			if (PL_ID == null) {
				BGMain.SQLquery("INSERT INTO `PLAYERS` (`NAME`) VALUES ('"
						+ playerName + "') ;");
			}
		}
		
		if (BGMain.REW) {
			Integer PL_ID = BGMain.getCoins(BGMain.getPlayerID(playerName));
			if (PL_ID == null) {
				BGReward.createUser(playerName);
			}
		}
		
		if(p.hasPermission("bg.admin.check")) {
			if(!BGMain.instance.getDescription().getVersion().contains("-DEV")) {
				Updater updater = new Updater(BGMain.instance, "bukkitgames", BGMain.getPFile(), Updater.UpdateType.NO_DOWNLOAD, false);
				boolean update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
			
				if (update) {
					String newversion = updater.getLatestVersionString();
					BGChat.printPlayerChat(p, "�bUpdate available: " + newversion + " �r/bgdownload");
				}
			}
		}
	}

	private String getShortStr(String s) {
		if (s.length() == 16) {
			String shorts = s.substring(0, s.length() - 4);
			return shorts;
		}
		if (s.length() == 15) {
			String shorts = s.substring(0, s.length() - 3);
			return shorts;
		}
		if (s.length() == 14) {
			String shorts = s.substring(0, s.length() - 2);
			return shorts;
		}
		if (s.length() == 13) {
			String shorts = s.substring(0, s.length() - 1);
			return shorts;
		}
		return s;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		if(BGMain.isSpectator(p)) {
			event.setCancelled(true);
			return;
		}
		
		if ((BGMain.DENY_BLOCKBREAK.booleanValue() & (!p.hasPermission("bg.admin.editblocks") 
				|| !p.hasPermission("bg.admin.*")))) {
			event.setCancelled(true);
			return;
		}

		if((BGMain.CORNUCOPIA_PROTECTED && BGCornucopia.isCornucopiaBlock(event.getBlock())) || (BGMain.FEAST_PROTECTED && BGFeast.isFeastBlock(event.getBlock())) || BGFBattle.isBattleBlock(event.getBlock())) {
			BGChat.printPlayerChat(p, "�cYou can't destroy this block!");
			event.setCancelled(true);
			return;
		}
		
		Block b = event.getBlock();
		
		if(BGMain.DEATH_SG_PROTECTED && BGSign.signs.contains(b.getLocation())) {
				event.setCancelled(true);
				return;
		}
		
		if (BGKit.hasAbility(p, 2) && b.getType() == Material.LOG) {
			World w = Bukkit.getServer().getWorld(BGMain.WORLD_TEMPOARY_NAME);
			Double y = b.getLocation().getY() + 1;
			Location l = new Location(w, b.getLocation().getX(), y, b
					.getLocation().getZ());
			while (l.getBlock().getType() == Material.LOG) {
				l.getBlock().breakNaturally();
				y++;
				l.setY(y);
			}
		}		
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		for(Block b : event.getBlocks()) {
			if(BGCornucopia.isCornucopiaBlock(b)) {
				event.setCancelled(true);
				break;
			}
			if(BGFeast.isFeastBlock(b)) {
				event.setCancelled(true);
				break;
			}
			if(BGFBattle.isBattleBlock(b)) {
				event.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		Block b = event.getBlock();
		if(BGCornucopia.isCornucopiaBlock(b)) {
			event.setCancelled(true);
			return;
		}
		if(BGFeast.isFeastBlock(b)) {
			event.setCancelled(true);
			return;
		}
		if(BGFBattle.isBattleBlock(b)) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		if((BGMain.CORNUCOPIA_PROTECTED && BGCornucopia.isCornucopiaBlock(event.getBlock())) || 
			(BGMain.FEAST_PROTECTED && BGFeast.isFeastBlock(event.getBlock())) || 
			BGFBattle.isBattleBlock(event.getBlock())) {
			
			event.setCancelled(true);
			return;
		}
	}
		
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		if(BGMain.isSpectator(p)) {
			event.setCancelled(true);
			return;
		}
		if ((BGMain.DENY_BLOCKPLACE.booleanValue() & (!p.hasPermission("bg.admin.editblocks") 
				|| !p.hasPermission("bg.admin.*")))) {
			event.setCancelled(true);
			return;
		}
		
		if((BGMain.CORNUCOPIA_PROTECTED && BGCornucopia.isCornucopiaBlock(event.getBlock())) || (BGMain.FEAST_PROTECTED && BGFeast.isFeastBlock(event.getBlock())) || BGFBattle.isBattleBlock(event.getBlock())) {
			BGChat.printPlayerChat(p, "�cYou can't place this block!");
			event.setCancelled(true);
			return;
		}
		
		Block block = event.getBlockPlaced();
		if (BGKit.hasAbility(p, 10) && block.getType() == Material.CROPS) {
			block.setData(CropState.RIPE.getData());
		}
		if (BGKit.hasAbility(p, 10) && block.getType() == Material.MELON_SEEDS) {
			block.setData(CropState.RIPE.getData());
		}
		if (BGKit.hasAbility(p, 10) && block.getType() == Material.PUMPKIN_SEEDS) {
			block.setData(CropState.RIPE.getData());
		}
		if (BGKit.hasAbility(p, 10) && block.getType() == Material.SAPLING) {
			TreeType t = getTree(block.getData());
			Bukkit.getServer().getWorlds().get(0).generateTree(block.getLocation(), t);
		}
	}
	
    public TreeType getTree(int data) {
        TreeType tretyp = TreeType.TREE;
        switch(data) {
        case 0:
            tretyp = TreeType.TREE;
            break;
        case 1:
            tretyp = TreeType.REDWOOD;
            break;
        case 2:
            tretyp = TreeType.BIRCH;
            break;
        case 3:
            tretyp = TreeType.JUNGLE;
            break;
        default:
            tretyp = TreeType.TREE;
        }
        return tretyp;
    }
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(BGMain.isSpectator(event.getPlayer())) {
			if(BGMain.ADV_CHAT_SYSTEM) {
				BGChat.printPlayerChat(event.getPlayer(), "�cSpectators can't chat.");
				event.setCancelled(true);			
				return;
			} else {
				event.getRecipients().clear();
				event.getRecipients().addAll(BGMain.getSpectators());
				event.getRecipients().addAll(getOnlineOps());
				event.setFormat("�o[SPECTATORS] �r" + event.getFormat());
			}
		}
			
		if (BGMain.ADV_CHAT_SYSTEM) {
			String m = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
			String s = ChatColor.stripColor(m);
			if(s.length() <= 53) {
				BGChat.playerChatMsg(m);
				log.info("[CHAT] " + m);
			} else {
				BGChat.printPlayerChat(event.getPlayer(), "�cYour message is too long!");
			}
			event.setCancelled(true);
		}
	}

	private ArrayList<Player> getOnlineOps() {
		ArrayList<Player> ops = new ArrayList<Player>();
		for(Player p : Bukkit.getServer().getOnlinePlayers()) {
			if(p.isOp())
				ops.add(p);
		}
				
		return ops;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		
		if(BGMain.isGameMaker(p)) {
			event.setQuitMessage(null);
			BGMain.remGameMaker(p);
			return;
		}
		
		if (BGMain.isSpectator(p)) {
			event.setQuitMessage(null);
			BGMain.remSpectator(p);
			return;
		}
		
		if (!BGMain.DENY_LOGIN.booleanValue() & BGMain.ADV_CHAT_SYSTEM) {
			BGChat.printDeathChat("�e" + event.getQuitMessage());
		}

		if (BGMain.DENY_LOGIN.booleanValue() || BGMain.ADV_CHAT_SYSTEM) {
			event.setQuitMessage(null);
		}

		if (BGMain.QUIT_MSG.booleanValue() & !p.isDead()) {
			BGChat.printDeathChat(p.getName() + " left the game.");
			if (!BGMain.ADV_CHAT_SYSTEM) {
				BGChat.printDeathChat(BGMain.getGamers().length - 1
						+ " players remaining.");
				BGChat.printDeathChat("");
			}
			Location light = p.getLocation();
			last_quit = p.getName();
			p.setHealth(0);
			Bukkit.getServer().getWorlds().get(0)
					.strikeLightningEffect(light.add(0.0D, 100.0D, 0.0D));
		}

		if (BGMain.NEW_WINNER != p.getName()
				& BGMain.DENY_LOGIN.booleanValue()) {
			Bukkit.getServer().getScheduler()
					.scheduleSyncDelayedTask(BGMain.instance, new Runnable() {
						public void run() {
							BGMain.checkwinner();

							if (BGMain.SQL_USE) {
								Integer PL_ID = BGMain.getPlayerID(last_quit);
								if (last_quit == BGMain.NEW_WINNER) {
									
								} else {
									BGMain.SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `DEATH_REASON` = 'QUIT' WHERE `REF_PLAYER` = "
											+ PL_ID
											+ " AND `REF_GAME` = "
											+ BGMain.SQL_GAMEID + " ;");
								}
							}
						}
					}, 60L);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		
		Entity damager = event.getDamager();
		Entity defender = event.getEntity();
		
		if(damager instanceof Player)
			if(BGMain.isSpectator((Player) damager)) {
				event.setCancelled(true);
				return;
			}
		
		if (BGMain.DENY_DAMAGE_ENTITY & !(event.getEntity() instanceof Player)) {
			return;
		}
		if (BGMain.DENY_DAMAGE_PLAYER & event.getEntity() instanceof Player) {
			return;
		}
		if (event.getEntity().isDead()) {
			return;
		}

		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player p = (Player) arrow.getShooter();
				if (!BGKit.hasAbility(p, 9)) {
					return;
				}
				if (p.getLocation().distance(event.getEntity().getLocation()) >= BGFiles.abconf.getInt("AB.9.Distance")) {
					if (event.getEntity() instanceof LivingEntity) {
						LivingEntity victom = (LivingEntity) event.getEntity();
						if (victom instanceof Player) {
							Player v = (Player) victom;
							ItemStack helmet = v.getInventory().getHelmet();
							if (helmet == null) {
								BGChat.printDeathChat(v.getName()
										+ " was headshotted by " + p.getName()
										+ ".");
								if (!BGMain.ADV_CHAT_SYSTEM) {
									BGChat.printDeathChat((BGMain
											.getGamers().length - 1)
											+ " players remaining.");
									BGChat.printDeathChat("");
								}
								Location light = v.getLocation();
								Bukkit.getServer()
										.getWorlds().get(0)
										.strikeLightningEffect(
												light.add(0.0D, 100.0D, 0.0D));
								last_headshot = v.getName();
								v.setHealth(0);
								v.kickPlayer(ChatColor.RED + v.getName()
										+ " was headshotted by " + p.getName()
										+ ".");

								if(BGMain.REW && BGMain.COINS_FOR_KILL != 0){
									BGReward.giveCoins(p.getName(), BGMain.COINS_FOR_KILL);
									if(BGMain.COINS_FOR_KILL == 1)
										BGChat.printPlayerChat(p, "You got 1 Coin for killing "+v.getName());
									else
										BGChat.printPlayerChat(p, "You got "+BGMain.COINS_FOR_KILL+" Coins for killing "+v.getName());
								}
								if (BGMain.SQL_USE) {
									Integer PL_ID = BGMain.getPlayerID(v
											.getName());
									Integer KL_ID = BGMain.getPlayerID(p
											.getName());
									BGMain.SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `REF_KILLER` = "
											+ KL_ID
											+ ", `DEATH_REASON` = 'HEADSHOT' WHERE `REF_PLAYER` = "
											+ PL_ID
											+ " AND `REF_GAME` = "
											+ BGMain.SQL_GAMEID + " ;");
								}
							} else {
								helmet.setDurability((short) (helmet
										.getDurability() + 20));
								v.getInventory().setHelmet(helmet);
							}
						} else {
							BGChat.printPlayerChat(p, "Headshot!");
							victom.setHealth(0);
						}
					}
				}
			}
		}
		
		if (damager instanceof Player) {
			
			Player dam = (Player)damager;
			if(BGKit.hasAbility(dam, 12)) {
				
				if (dam.getHealth() == 20) {
					return;
				}
				
				dam.setHealth(dam.getHealth()+1);
			}
			
			if (defender.getType() == EntityType.PLAYER) {
				
				Player def = (Player)defender;
				
				if(BGKit.hasAbility(dam, 13) && dam.getItemInHand().getType() == Material.BLAZE_ROD && def.getItemInHand() != null) {
				
					if (!monkList.contains(dam)) {
						
						int random = (int) (Math.random()* (BGFiles.abconf.getInt("AB.13.Chance")-1)+1);
						if (random == 1) {
							monkList.add(dam);
							BGCooldown.monkCooldown(dam);
							def.getInventory().clear(def.getInventory().getHeldItemSlot());
							BGChat.printPlayerChat(dam, BGFiles.abconf.getString("AB.13.Success"));
							BGChat.printPlayerChat(def, BGFiles.abconf.getString("AB.13.Success"));
						}
					}else {
						
						BGChat.printPlayerChat(dam, BGFiles.abconf.getString("AB.13.Expired"));
					}
				}
				
				if(BGKit.hasAbility(dam, 15) && dam.getItemInHand().getType() == Material.STICK && def.getItemInHand() != null) {
					
					if(!thiefList.contains(dam)) {
						int random = (int) (Math.random()* (BGFiles.abconf.getInt("AB.15.Chance")-1)+1);
						if(random == 1) {
							thiefList.add(dam);
							BGCooldown.thiefCooldown(dam);
							dam.getInventory().clear(dam.getInventory().getHeldItemSlot());
							dam.getInventory().addItem(def.getItemInHand());
							def.getInventory().clear(def.getInventory().getHeldItemSlot());
							BGChat.printPlayerChat(dam, BGFiles.abconf.getString("AB.15.Success"));
							BGChat.printPlayerChat(def, BGFiles.abconf.getString("AB.15.Success"));
						}
					}else {
						
						BGChat.printPlayerChat(dam, BGFiles.abconf.getString("AB.15.Expired"));
					}
				}
				
				if (BGKit.hasAbility(dam, 19)) {
					
					int random = (int) (Math.random()* (BGFiles.abconf.getInt("AB.19.Chance")-1)+1);
					if(random == 1 && !viperList.contains(def)) {
						
						def.addPotionEffect(new PotionEffect(PotionEffectType.POISON, BGFiles.abconf.getInt("AB.19.Duration")*20, 1));
						viperList.add(def);
						BGChat.printPlayerChat(dam, BGFiles.abconf.getString("AB.19.Damager"));
						BGChat.printPlayerChat(def, BGFiles.abconf.getString("AB.19.Defender"));
						BGCooldown.viperCooldown(def);
					}
				}
			}
			
			EntityType mob = defender.getType();
			
			if (BGMain.ADV_ABI && BGKit.hasAbility(dam, 17) && BGDisguise.getDisguiseType(mob) != null) {
				
				DisguiseType mt = BGDisguise.getDisguiseType(mob);
				BGDisguise.disguise(dam, mt);
			}
			
			if (BGKit.hasAbility(dam, 18) && dam.getItemInHand().getType() == Material.AIR) {
				
				event.setDamage(event.getDamage()+ 4);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			if(BGMain.isSpectator((Player) event.getEntity())) {
				event.setCancelled(true);
				return;
			}
		}
		
		if ((BGMain.DENY_DAMAGE_PLAYER.booleanValue() & event.getEntity() instanceof Player)) {
			event.setCancelled(true);
			return;
		}

		if ((BGMain.DENY_DAMAGE_ENTITY.booleanValue() & !(event
				.getEntity() instanceof Player))) {
			event.setCancelled(true);
			return;
		}
		
		if ((event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK & event
				.getEntity() instanceof Player)) {
			Player p = (Player) event.getEntity();
			if ((BGKit.hasAbility(p, Integer.valueOf(6)).booleanValue() & !p
					.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))) {
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.INCREASE_DAMAGE, 200, 1));
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.FIRE_RESISTANCE, 260, 1));
			}
		}
		
		Entity en = event.getEntity();
		if (en.getType() == EntityType.PLAYER) {
			Player player = (Player)en;
			if(BGKit.hasAbility(player, 17) && BGMain.ADV_ABI) {
				BGDisguise.unDisguise(player);
			}
			if(BGKit.hasAbility(player, 18)) {
				event.setDamage(event.getDamage() - 1);
			}
		}
	}

	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player dp = event.getEntity();
		
		if (BGMain.isSpectator(dp) || BGMain.isGameMaker(dp)) {
			event.setDeathMessage(null);
			event.getDrops().clear();
			event.setDroppedExp(0);
			return;
		}
		
		if(BGKit.hasAbility(dp, 23)) {
			
			Bukkit.getServer().getWorlds().get(0).createExplosion(dp.getLocation(), 2.5F, BGFiles.abconf.getBoolean("AB.23.Burn"));
		}
		
		if (BGMain.DEATH_SIGNS) {
			Location loc = dp.getLocation();
			String fl = BGFiles.dsign.getString("FIRST_LINE");
			String sl = BGFiles.dsign.getString("SECOND_LINE");
			String tl = BGFiles.dsign.getString("THIRD_LINE");
			String fol = BGFiles.dsign.getString("FOURTH_LINE");
			
			if(fl != null)	
				fl = fl.replace("[name]", dp.getName());
			if(sl != null)
				sl = sl.replace("[name]", dp.getName());
			if(tl != null)
				tl = tl.replace("[name]", dp.getName());
			
			if(fol != null)
				fol = fol.replace("[name]", dp.getName());
			
			BGSign.createSign(loc, fl, sl, tl, fol);
		}
		
		
		if(dp.getKiller() != null && dp.getKiller() instanceof Player) {
			Player killer = dp.getKiller();
			if(BGKit.hasAbility(killer, 14)) {
				if(killer.getFoodLevel() <= 14) {
					killer.setFoodLevel(killer.getFoodLevel()+ 6);
				}else {
					killer.setFoodLevel(20);
				}
			}
			if(BGMain.REW && last_headshot != dp.getName() && BGMain.COINS_FOR_KILL != 0){
				BGReward.giveCoins(killer.getName(), BGMain.COINS_FOR_KILL);
				if(BGMain.COINS_FOR_KILL == 1)
					BGChat.printPlayerChat(killer, "You got 1 Coin for killing "+dp.getName());
				else
					BGChat.printPlayerChat(killer, "You got "+BGMain.COINS_FOR_KILL+" Coins for killing "+dp.getName());
			}
		}

		if (last_quit == event.getEntity().getName() || last_headshot == event.getEntity().getName()) {
			event.setDeathMessage(null);
			return;
		}

		if (BGMain.DEATH_MSG) {
			Player p = event.getEntity();

			if (BGMain.SQL_USE) {
				Integer PL_ID = BGMain.getPlayerID(p.getName());

				Integer KL_ID = null;
				if (p.getKiller() != null) {
					KL_ID = BGMain.getPlayerID(p.getKiller().getName());
				} else {
					KL_ID = null;
				}
				
				String cause = null;
				try{
					cause = p.getLastDamageCause().getCause().name().toString();
				}catch (NullPointerException e) {
					
				}

				BGMain.SQLquery("UPDATE `PLAYS` SET deathtime = NOW(), `REF_KILLER` = "
						+ KL_ID
						+ ", `DEATH_REASON` = '"
						+ cause
						+ "' WHERE `REF_PLAYER` = "
						+ PL_ID
						+ " AND `REF_GAME` = " + BGMain.SQL_GAMEID + " ;");
			}
			
			Location light = p.getLocation();
			p.kickPlayer(ChatColor.RED + event.getDeathMessage() + ".");
			
			BGChat.printDeathChat(event.getDeathMessage() + ".");
			if (!BGMain.ADV_CHAT_SYSTEM) {
				BGChat.printDeathChat(BGMain.getGamers().length + " players remaining.");
				BGChat.printDeathChat("");
			}
			Bukkit.getServer().getWorlds().get(0).strikeLightningEffect(light.add(0, 100, 0));
		}

		event.setDeathMessage(null);
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getTarget();
		if (entity != null) {
			if (entity instanceof Player) {
				Player player = (Player)entity;
				if(BGKit.hasAbility(player, 20) && event.getReason() == TargetReason.CLOSEST_PLAYER) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(event.getMessage().contains("/me"))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void freezePlayers(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		
		if(freezeList.contains(p)) {
			event.setCancelled(true);
			BGChat.printPlayerChat(p, BGFiles.abconf.getString("AB.22.move"));
		}
	}
}
