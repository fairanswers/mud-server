package mud.foe.items;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.Command;
import mud.Constants;
import mud.ObjectFlag;

import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.misc.Device;
import mud.foe.misc.FileSystem;
import mud.foe.misc.Module;
import mud.foe.misc.Tag;

import mud.interfaces.ExtraCommands;
import mud.misc.Slot;
import mud.net.Client;

import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;

import mud.utils.Time;
import mud.utils.Utils;

// TODO need to deal with these commands having a null parent here...
public class PipBuck extends Item implements Device, ExtraCommands {
	private Integer id;
	private String name;
	private FileSystem fs;

	private List<Module> modules;
	private List<Tag> tags;

	// TODO these should probably be private variables
	boolean efs_enabled = false;

	private int max_power = 6;
	private int current_power = 6;

	private static Map<String, Command> commands = new Hashtable<String, Command>() {
		{
			put("enable",
					new Command("enable a module") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						if( arg.equalsIgnoreCase("efs") ) {
							p.efs_enabled = true;
							send("Eyes-Forward Sparkle ENABLED", client);
							return;
						}

						// get the intended module
						final Module module = p.getModule(arg);

						// enable the module
						if( module != null ) {
							if( module.getPowerReq() <= p.current_power) {
								send("Enabling Module: " + module.getName(), client);

								p.enableModule( module );

								if( module instanceof ExtraCommands ) {
									ExtraCommands ec = (ExtraCommands) module;

									for(Map.Entry<String, Command> cmdE : ec.getCommands().entrySet()) {
										final String text = cmdE.getKey();
										final Command cmd = cmdE.getValue();
										
										initCmd(cmd);
										
										player.commandMap.put( cmdE.getKey(), cmdE.getValue() );
										
										debug("Added " + cmdE.getKey() + " to player's command map from " + p.getName() + " module: " + module.getName());
									}
								}
							}
							else send("PipBuck: Insufficent power to enable ", client);
						}
					}
				}
				public int getAccessLevel() { return Constants.USER; }
			});
			put("disable",
					new Command("disable a module") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						if( arg.equalsIgnoreCase("efs") ) {
							p.efs_enabled = false;
							send("Eyes-Forward Sparkle DISABLED", client);
							return;
						}

						// get the intended module
						final Module module = p.getModule(arg);

						// disable the module
						if( module != null ) {
							if( module instanceof ExtraCommands ) {
								ExtraCommands ec = (ExtraCommands) module;

								for(Map.Entry<String, Command> cmd : ec.getCommands().entrySet()) {
									//p.commands.remove( cmd.getKey() );
									player.commandMap.remove( cmd.getKey() );
									debug("Removed " + cmd.getKey() + " from " + p.getName() + " module: " + module.getName() + " from player's command map.");
								}
							}

							send("Disabling Module: " + module.getName(), client);

							p.disableModule( module );
						}
					}
				}
				public int getAccessLevel() { return Constants.USER; }
			});
			put("register",
					new Command("register a pipbuck tag") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck
					
					// register <tag name>=<tag id>
					if( p != null ) {
						final String[] args = arg.split("=");
						
						if( args.length == 2 ) {
							final String tagName = args[0];
							final Integer tagId = Utils.toInt(args[1], -1);
							
							if( tagId != -1 ) {
								p.setTag(tagName, tagId);
							}
						}
					}
				}
				
				public int getAccessLevel() { return Constants.USER; }
				});
			put("slot",
					new Command("attach a module to your device") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						// get modules list
						final List<Module> modules = p.getModules();

						Module module = null;
						Item item = null;

						for(final Item item2 : player.getInventory()) {
							if( item2 != null ) {
								if(item2 instanceof Module) {
									debug("Found a Module - \'" + item2.getName() + "\'");

									item = item2;
									module = (Module) item;

									if( !modules.contains(module) && module.getName().toLowerCase().equals(arg.toLowerCase()) ) {
										break;
									}
								}
								else debug("Not a Module - \'" + item2.getName() + "\'");
							}
						}

						player.getInventory().remove( item );

						// slot module
						if( module != null ) {
							p.addModule( module );
							send("You slot the " + module.getName() + " into your Pipbuck.", client);
						}
						else send("You don't have such a module.", client);
					}
				}
				public int getAccessLevel() { return Constants.USER; }
			});
			put("tags",
					new Command("list your registed pipbuck tags") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						send("--- Tags", client);
						
						for(final Tag tag : p.tags) {
							send("" + tag, client);
						}
					}
				}
				
				public int getAccessLevel() { return Constants.USER; }
				});
			put("unslot",
					new Command("detach a module from your device") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						// NOTE: we know that modules are Items, so this is reasonably safe...
						final Module module = p.getModule(arg);
						final Item item = (Item) module;

						// unslot module
						if( module != null ) {
							p.removeModule( module );
							send("You unslot the " + module.getName() + " from your Pipbuck.", client);
							player.getInventory().add( item );
						}
						else send("You don't have such a module.", client);
					}
				}
				public int getAccessLevel() { return Constants.USER; }
			});
			put("modules",
					new Command("list the modules attached to your device") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						final List<Module> modules = p.getModules();

						if( modules.size() > 0 ) {
							send("Modules", client);
							for(Module module : modules) { send(module.getName(), client); }
						}
						else send("No Modules", client);
					}
				}
				public int getAccessLevel() { return Constants.USER; }
			});
			put("vp",
					new Command("view pipbuck") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						if( arg.equals("") ) {
							// get the current amount of ambient radiation (rads/sec)
							int rads = 0;

							final Object value = getRoom( player.getLocation() ).getProperty("_game/rads");

							if( value != null) rads = Integer.parseInt((String) value);

							final StringBuilder sb = new StringBuilder();

							// TODO really need a nice utility function for this (did it elsewhere in main class I think)
							final Time game_time = getGameTime();

							int hours = game_time.hour;
							int minutes = game_time.minute;

							if( hours < 9 ) sb.append(" " + hours);
							else            sb.append(hours);

							sb.append(":");

							if( minutes < 9 ) sb.append("0" + minutes);
							else            sb.append(minutes);
							
							// TODO consider using String.format for time here

							send("Looking at your Pipbuck, you note that: ", client);

							send(Utils.padRight("", '-', 40), client);

							send("the time is " + sb.toString(), client);
							send("current radiation exposure is: " + rads + " rads/sec.", client);

							sb.delete(0, sb.length());

							for(final Module module : p.getModules()) {
								if( module.isEnabled() ) sb.append( colors(module.getName(), "green") + ", " );
								else                     sb.append( colors(module.getName(), "yellow") + ", " );
							}

							if( sb.length() > 2 ) sb.delete(sb.length() - 2, sb.length());

							send("Currently slotted modules: " + sb.toString(), client);

							final int curr_p = p.getPower();
							final int max_p = p.getMaxPower();

							sb.delete(0, sb.length());

							sb.append( Utils.padRight("",  '|', curr_p) );
							sb.append( Utils.padRight("", ' ', max_p - curr_p) );

							send("Remaining Power: [" + colors(sb.toString(), "green") + "]", client);

							sb.delete(0, sb.length());

							send(Utils.padRight("", '-', 40), client);
						}
						else if( arg.equalsIgnoreCase("items") ) {
							send(Utils.padRight("", '-', 40), client);
							
							for(final Item item : player.getInventory()) {
								send(item.getName(), client);
							}
							
							send(Utils.padRight("", '-', 40), client);
						}
					}
				}

				public int getAccessLevel() { return Constants.USER; }
			});
			// scan/efs
			put("efs",
					new Command("eyes-forward sparkle") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);      // get player
					final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

					if( p != null ) {
						if( p.efs_enabled ) {
							// the idea here is to look for living things and/or pipbuck tags and mark them as:
							// hostile, neutral, friendly (red, yellow, green)

							final Room room = getRoom( player.getLocation() );

							// get a list of all living creatures in range
							final List list = new LinkedList();

							// for each creature decide if they are a threat
							for(final Object obj : list) {
							}

							// it'd be nice to use tags to see if we know/know of any of the identified creatures

							// scan for other pipbucks

							// check against known ids (tags are set/acquired and associated with device ids)
							//for(final Tag tag : p.tags) {}
						}
					}
				}

				public int getAccessLevel() { return Constants.USER; }

			});
		}
	};

	public PipBuck( String name ) {
		this(-1, name);
	}

	public PipBuck( int dbref, String name ) {
		super(dbref, "PipBuck", EnumSet.noneOf(ObjectFlag.class), "A Stable-Tec PipBuck", -1);

		this.item_type = FOEItemTypes.PIPBUCK;
		this.slot_type = FOESlotTypes.LFHOOF;

		this.equippable = true;

		this.name = name;
		this.fs = new FileSystem();
		this.modules = new LinkedList<Module>();
	}

	protected PipBuck(PipBuck template) {
		super( template );

		this.name = template.name;
		this.fs = template.fs; // need to give FileSystem a clone method
		this.modules = new LinkedList<Module>();
	}

	/*public String getName() {
		return this.name;
	}*/

	public String getDeviceName() {
		return "";
	}

	public DeviceType getDeviceType() {
		return DeviceType.PIPBUCK;
	}

	/**
	 * Add a module to the PipBuck
	 * 
	 * @param mod
	 * @return
	 */
	public boolean addModule(Module mod) {
		return this.modules.add(mod);
	}

	/**
	 * Remove a module from the PipBuck
	 * 
	 * @param mod
	 * @return
	 */
	public boolean removeModule(Module mod) {
		return this.modules.remove(mod);
	}

	public Module getModule(final String moduleName) {
		for(Module module : modules) {
			if(moduleName.toLowerCase().equals( module.getName().toLowerCase() )) {
				return module;
			}
		}

		return null;
	}

	@Override
	public List<Module> getModules() {
		return this.modules;
	}

	private void enableModule(final Module module) {
		current_power -= module.getPowerReq();
		module.enable();
	}

	private void disableModule(final Module module) {
		module.disable();
		current_power += module.getPowerReq();
	}

	@Override
	public Map<String, Command> getCommands() {
		//final Map<String, Command> temp = new Hashtable<String, Command>();
		//temp.putAll( commands );
		return commands;
	}

	public int getPower() {
		return this.current_power;
	}

	public int getMaxPower() {
		return this.max_power;
	}

	public boolean setTag(final String tag, final Integer id) {
		boolean valid_tag = true;
		
		// test for appropriate format
		if( tag.length() != 10 ) {
			valid_tag = false;
		}
		else {
			final String alpha = tag.substring(0, 3);
			final String numeric = tag.substring(3, tag.length());

			for(final char ch : alpha.toCharArray()) {
				if( !Character.isLetter(ch) ) valid_tag = false;
			}

			for(final char ch : numeric.toCharArray()) {
				if( !Character.isDigit(ch) ) valid_tag = false;
			}
		}
		
		if( valid_tag ) {
			this.tags.add( new Tag(tag, id) );
		}
		
		return valid_tag;
	}

	@Override
	public String toString() {
		return name + "(" + type.toString() + ")";
	}

	@Override
	public PipBuck clone() {
		return new PipBuck(this);
	}

	private static final PipBuck getPipBuck(final Player player) {
		PipBuck p = null;

		final Slot slot = player.getSlots().get("special");
		final Slot slot1 = player.getSlots().get("special2");

		if( slot != null && !slot.isEmpty() )       p = (PipBuck) slot.getItem();
		else if( slot1 != null && !slot.isEmpty() ) p = (PipBuck) slot.getItem();

		return p;
	}
}