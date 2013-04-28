package mud;

import java.util.*;

import mud.MUDObject;
import mud.objects.*;
import mud.utils.Utils;
import mud.net.Client;

/**
 * 
 * This replaces ArrayList(s) for object types in MUDServer
 * 
 * @author joshgit
 *
 */

public class ObjectDB {

    private static int nextId = 0;

    // TreeMap allows instant retrieval by id and by name.
    final private TreeMap<String, MUDObject>  objsByName = new TreeMap<String, MUDObject>();
    final private TreeMap<Integer, MUDObject> objsById = new TreeMap<Integer, MUDObject>();
    
    // holds unused database references, that exist due to "recycled" objects
    private Stack<Integer> unusedDBNs = new Stack<Integer>();
    private List<Integer> reservedDBNs = new LinkedList<Integer>();

    /** As long as we construct objects and insert them into this db afterwards as a separate step, getting the next id is somewhat of a hack.
     *  We could easily construct objects with an invalid id or never add them to this db.
     */
    public int peekNextId() {
    	if( !unusedDBNs.empty() ) { return unusedDBNs.peek(); }
    	else { return nextId; }
    }

    // do not use, yet
    public void reserveID() {
    	final int id;

    	if( !unusedDBNs.empty() ) { id = unusedDBNs.pop(); }
    	else { id = nextId++; }

    	reservedDBNs.add(id);
    }

    public void addUnused(int unusedId) {
    	MUDObject mobj = get(unusedId);
    	int next = peekNextId();{

    		if( reservedDBNs.contains(unusedId) ) {
    			reservedDBNs.remove(unusedId);
    			unusedDBNs.push(unusedId);
    		}
    		else if( mobj instanceof NullObject ) {
    			NullObject no = (NullObject) mobj;

    			if( !no.isLocked() ) {
    				System.out.println("dbref isn't in use");
    				unusedDBNs.push(unusedId);
    			}
    			else {
    				System.out.println("NullObject is locked");
    			}
    		}
    		else if(unusedDBNs.empty() && unusedId == next - 1) { // we just made a new one, but it's the most recent one in the db, just go back one
    			nextId--;
    		}
    		else {
    			System.out.println("Something is already using that id!");
    		}
    	}
    }

    public int getSize() {
    	return objsByName.size();
    }

    public void addAsNew(final MUDObject item) {
    	if( !unusedDBNs.empty() ) { item.setDBRef( unusedDBNs.pop() ); }
    	else { item.setDBRef(nextId++); }
        add(item);
    }

    public void add(final MUDObject item) {
        objsByName.put(item.getName(), item);
        objsById.put(item.getDBRef(), item);
        nextId++;
        
        if(item instanceof NullObject) { addUnused(item.getDBRef()); }
    }

    public void allocate(final int n) {
        // Until we travel back to 1970 and convert this codebase into C, we probably don't need to pre-allocate things.
        // Unless users are creating rooms faster that the JVM can allocate objects...
    }

    // Send all objects
    public void dump(final Client client, final MUDServer server) {
        int i = 0;
        for (final MUDObject obj : objsByName.values()) {
            server.send(String.format("%s: %s (#%s)", i, obj.getName(), obj.getDBRef()), client);
            i += 1;
        }
    }
    
    public void dump() {
    	int i = 0;
        for (final MUDObject obj : objsByName.values()) {
        	System.out.println(String.format("%s: %s (#%s)", i, obj.getName(), obj.getDBRef()));
            i += 1;
        }
    }

    // Ensure object is in both maps, overwriting any object in the id map.
    public void set(final int n, final MUDObject item) {
        objsById.put(n, item);
        objsByName.put(item.getName(), item);
    }

    public boolean hasName(final String name) {
        return objsByName.containsKey(name);
    }

    public MUDObject getByName(final String name) {
        return objsByName.get(name);
    }

    public MUDObject get(final int n) {
        return objsById.get(n);
    }

    public List<MUDObject> findByLower(final String name) {
        final LinkedList<MUDObject> acc = new LinkedList<MUDObject>();
        for (final MUDObject obj : objsByName.values()) {
            if (obj.getName().toLowerCase().contains(name.toLowerCase())) {
                acc.add(obj);
            }
        }
        return acc;
    }

    public int[] getFlagCounts(final String[] letters) {
        final int[] counts = new int[letters.length];
        Arrays.fill(counts, 0);

        for (final MUDObject obj : objsByName.values()) {
            for (final ObjectFlag f : obj.getFlags()) {
                for (int i = 0; i < letters.length; i++) {
                    if (f.toString().startsWith(letters[i])) {
                        counts[i] += 1;
                    }
                }
            }
        }
        return counts;
    }

    // Serialize all objects via `toDB` and save array to file.
    public void save(final String filename) {
    	final String[] old = Utils.loadStrings(filename);    // old (current in file) database
    	final String[] toSave = new String[objsById.size()]; // new (save to file) database
		
    	int index = 0;
        
		for (final MUDObject obj : objsById.values()) {
        	if(obj instanceof NullObject) {
        		NullObject no = (NullObject) obj;
        		
        		// a locked NullObject means an ignored line whose data we are ignoring (but want to keep)
        		if( no.isLocked() ) {
        			System.out.println(index + " Old Data: " + old[index] + " Keeping...");
        			toSave[index] = old[index]; // keep old data
        		}
        		else {
        			System.out.println(index + " No Previous Data, Overwriting...");
        			toSave[index] = obj.toDB(); // just save the null object
        		}
        		
        		index++;
        		continue; // skip to next object
        	}
        	
        	toSave[index] = obj.toDB();
        	
            index++;
        }
        
		Utils.saveStrings(filename, toSave);
    }

    ///////////// THINGS
    final private ArrayList<Thing> things = new ArrayList<Thing>();

    public List<Thing> getThingsForRoom(final int roomId) {
        final List<Thing> acc = new LinkedList<Thing>();
		for (final Thing t : things) {
			if (t.getLocation() == roomId) {
                acc.add(t);
			}
		}
        return acc;
    }

    public Thing getThing(final int roomId, final String name) {
		for (final Thing t : things) {
			if (t.getLocation() == roomId && t.getName().equals(name))
            {
                return t;
			}
		}
        return null;
    }

    public void addThing(final Thing t) {
        things.add(t);
    }

    public void removeThing(final Thing t) {
        things.remove(t);
    }

	public void placeThingsInRooms(final MUDServer parent) {
		for (final Thing t : things) {
			if (t != null) {
                final Room room = parent.getRoom(t.getLocation());
                if (room != null) {
                    room.contents.add(t);
                }
			}
		}
	}

    ////////////// ROOMS
    final private Map<Integer, Room> roomsById   = new HashMap<Integer, Room>();
    final private Map<String, Room>  roomsByName = new HashMap<String, Room>();

    // must add room to both maps
    public void addRoom(final Room r) {
        roomsByName.put(r.getName(), r);
        roomsById.put(r.getDBRef(), r);
    }

    // must remove room from both maps
    public void removeRoom(final Room r) {
        roomsByName.values().remove(r);
        roomsById.values().remove(r);
    }

    public Room getRoomByName(final String name) {
        return roomsByName.get(name);
    }

    public Room getRoomById(final int id) {
        return roomsById.get(id);
    }

    public List<Room> getRoomsByType(final RoomType type) {
        final List<Room> acc = new LinkedList<Room>();
        for (final Room r : roomsById.values()) {
            if (r.getRoomType().equals(type)) {
                acc.add(r);
            }
        }
        return acc;
    }

    public List<Room> getWeatherRooms() {
        return getRoomsByType(RoomType.OUTSIDE);
    }

    public List<Room> getRoomsByParentLocation(final int loc) {
        final List<Room> acc = new LinkedList<Room>();
        for (final Room r : roomsById.values()) {
            if (r.getParent() == loc) {
                acc.add(r);
            }
        }
        return acc;
    }

    public List<Room> getRoomsByLocation(final int loc) {
        final List<Room> acc = new LinkedList<Room>();
        for (final Room r : roomsById.values()) {
            if (r.getLocation() == loc) {
                acc.add(r);
            }
        }
        return acc;
    }

    /////////////// EXITS
    final Map<Integer, Exit> exitsById   = new HashMap<Integer, Exit>();
    final Map<String, Exit>  exitsByName = new HashMap<String, Exit>();

    public Exit getExit(final int dbref) {
        return exitsById.get(dbref);
    }

    public Exit getExit(final String name) {
        return exitsByName.get(name);
    }

    public void addExit(final Exit e) {
        exitsByName.put(e.getName(), e);
        exitsById.put(e.getDBRef(), e);
    }

    public void removeExit(final Exit e) {
        exitsByName.values().remove(e);
        exitsById.values().remove(e);
    }

	/**
	 * Go through all the exits that exist in the database and
	 * place/attach them in/to the respective rooms they are part of
	 */
	public void loadExits(final MUDServer parent) {
        parent.debug("Loading exits:", 2);
		for (final Exit e : exitsById.values()) {
			if( e.getExitType() == ExitType.DOOR ) {
				final Room room = getRoomById(e.getLocation());

				if (room != null) {
					room.getExits().add(e);
					parent.debug("Exit " + e.getDBRef() + " added to room " + room.getDBRef() + ".", 2);
				}

				/*final Room room1 = getRoomById(e.getDestination());

				if (room1 != null) {
					room1.getExits().add(e);
					parent.debug("Exit (Door)" + e.getDBRef() + " added to room " + room1.getDBRef() + ".", 2);
				}*/
			}
			else {
				final Room room = getRoomById(e.getLocation());
				
				if (room != null) {
					room.getExits().add(e);
					parent.debug("Exit " + e.getDBRef() + " added to room " + room.getDBRef() + ".", 2);
				}
			}
		}
        parent.debug("Done loading exits:", 2);
	}

    //////////////////// CREATURES
    // Should rooms store their own creatures?
    final private ArrayList<Creature> creeps = new ArrayList<Creature>();

    public void addCreature(final Creature c) {
        creeps.add(c);
    }

    public List<Creature> getCreatures() {
        return new ArrayList<Creature>(creeps);
    }

    public List<Creature> getCreatureByRoom(final int loc) {
        final List<Creature> acc = new LinkedList<Creature>();
        for (final Creature c : creeps) {
            if (c.getLocation() == loc) {
                acc.add(c);
            }
        }
        return acc;
    }

    ////////////////// NPCs
    final Map<Integer, NPC> npcsById   = new HashMap<Integer, NPC>();
    final Map<String, NPC>  npcsByName = new HashMap<String, NPC>();

    public void addNPC(final NPC npc) {
        npcsById.put(npc.getDBRef(), npc);
        npcsByName.put(npc.getName(), npc);
        npcsByName.put(npc.getCName(), npc);
    }

    public NPC getNPC(final String name) {
        return npcsByName.get(name);
    }

    public NPC getNPC(final int dbref) {
        return npcsById.get(dbref);
    }

    public List<NPC> getNPCs() {
        return new ArrayList<NPC>(npcsById.values());
    }

    public List<NPC> getNPCsByRoom(final int loc) {
        final List<NPC> acc = new LinkedList<NPC>();
        for (final NPC n : npcsById.values()) {
            if (n.getLocation() == loc) {
                acc.add(n);
            }
        }
        return acc;
    }

    ////////////////// ITEMS
    private ArrayList<Item> items = new ArrayList<Item>();

    public void addItem(final Item i) {
        items.add(i);
    }

    public void addItemsToRooms() {
		for (final Item item : items) {
			final Room r = getRoomById(item.getLocation());
            if (r != null) {
                r.contents1.add(item);
            }
        }
    }
    
    /**
     * Get the items held by a specific player
     * 
     * @return a map of with key: item, and value: player that holds references to the items the player is holding
     */
    public Map<Item, Player> getItemsHeld() {
        final Map<Item, Player> acc = new HashMap<Item, Player>();
		for (final Item item : items) {
            final MUDObject obj = get(item.getLocation());
            if (obj instanceof Player) {
                acc.put(item, (Player) obj);
            }
        }
        return acc;
    }
    
    /**
     * Get a List containing references to the items located in the
     * object with dbref, loc.
     * 
     * @param loc the dbref of the object where the items are located
     * @return list of items located in the object referred to by dbref
     */
    public List<Item> getItemsByLoc(final int loc) {
        final List<Item> acc = new LinkedList<Item>();
		for (final Item item : items) {
            if (item.getLocation() == loc) {
                acc.add(item);
            }
        }
        return acc;
    }

    ///////////////////////////// PLAYERS
    final Map<String, Player> players = new HashMap<String, Player>();

    public void addPlayer(final Player p) {
        players.put(p.getName(), p);
    }

    public Player getPlayer(final String name) {
        return players.get(name);
    }

    public int getNumPlayers(final Classes c) {
        int count = 0;
        for (final Player p : players.values()) {
            if (c.equals(p.getPClass())) {
                count += 1;
            }
        }
        return count;
    }
    
    public List<Player> getPlayersByRoom(final int loc) {
        final List<Player> acc = new LinkedList<Player>();
        for (final Player p : players.values()) {
            if (p.getLocation() == loc) {
                acc.add(p);
            }
        }
        return acc;
    }
}