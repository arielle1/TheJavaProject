import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class BlockGroup extends BlockMap<Pos,BuildingBlock> {

	private static GUI gui;
	private static BlockMap<Pos, BlockGroup> parent;
	//private BlockMap<Pos, BuildingBlock> innerBlockMap;
	private Set<Map.Entry<Pos, BuildingBlock>> innerMapSet;
	
	private int rows;
	private int cols;
	private int groupAxis;
	private boolean diagonal;
	private int speedLimit;
	private boolean focused;
	private Pos origin;
	private int cost;

	private HashMap<Pos, BlockGroup> neighborMap;
	private List<BlockGroup> blockLinkList;
	
	///////// CONSTRUCORS (2) //////////
	
	public BlockGroup(int axis) {
		
		innerMapSet = entrySet();
		groupAxis = axis;
		speedLimit = 0;
		focused = false;
		origin = new Pos(0,0);
		blockLinkList = new ArrayList<>();
		neighborMap = new BlockMap<Pos, BlockGroup>();
		
	}
	
	// Holding single block using singleton matrix
	public BlockGroup(BuildingBlock block) {
		
		put(new Pos(0,0), block);
		innerMapSet = entrySet();
		groupAxis = Direction.EAST;
		speedLimit = 0;
		focused = false;
		origin = new Pos(0,0);
		blockLinkList = new ArrayList<>();
		neighborMap = new BlockMap<Pos, BlockGroup>();
		cost = block.cost();
		
	}
	
	// Static constructor that should be called to initialize static fields
	public static void initGroups(GUI newGui, BlockMap<Pos,BlockGroup> newParent) {
		gui = newGui;
		parent = newParent;
	}
	
	////////// ////////// //////////
	
	public int cost() {
		
		int cost = 0;
		
		for(BuildingBlock block : this.values()) {
			cost = cost + block.cost();
		}
		
		return cost;
		
	}
	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public int groupAxis() {
		return groupAxis;
	}
	
	public int getSpeedLimit() {
		return speedLimit;
	}
	
	public void setSpeedLimit(int speedLimit) {
		this.speedLimit = speedLimit;
	}
	
	public boolean focused() {
		return focused;
	}
	
	public void setFocused(boolean bool) {
		focused = bool;
	}
	
	// this methods only works on "singleton BlockGroups", which is all we use in this version
	public BuildingBlock getBlock() {
		
		if( this.size() == 1 ) {
			return this.get(new Pos(0,0));
		}
		
		return null;
		
	}
	
	public int neighbors() {
		return neighborMap.size();
	}
	
	public void addNeighbor(Pos relativePos, BlockGroup neighbor) {
		
		neighborMap.put(relativePos, neighbor);
		// make the internal connections between blocks on BuildingBlock scale
		// if not connectable the connection will get "refused", nothing will happen
		if( getBlock() != null && neighbor.getBlock() != null ) {
			getBlock().connectWith(relativePos, neighbor.getBlock());
		}
	}
	
	public void removeNeighbor(BlockGroup neighbor) {
		
		if( getBlock() != null && neighbor.getBlock() != null ) {
			getBlock().removeConnection(neighbor.getBlock());
		}
		neighborMap.values().remove(neighbor);
	}
	
	// updates the map of neighbors and updates the corresponding neighbors of any changes in this
	// using addNeighbor enables forwarding to BuildingBlocks with connectWith, so any connections can be changed as well
	public void updateNeighbors() {
		
		Pos thisPos = gui.getPos(this);
		removeFromNeighbors();
		neighborMap.clear();
		
		for(int x = thisPos.x - 1; x <= thisPos.x + 1; x++) {
			for(int y = thisPos.y - 1; y <= thisPos.y + 1; y++) {
				
				BlockGroup blockNeighbor = gui.getBlock(new Pos(x,y));
					
				if( !(x == thisPos.x && y == thisPos.y) && blockNeighbor != null) {
					addNeighbor(new Pos(x - thisPos.x, y - thisPos.y), blockNeighbor);
					blockNeighbor.addNeighbor(new Pos(thisPos.x - x, thisPos.y - y), this);
				}
				
			}
		}
		
	}
	
	// removes this from neighbors
	public void removeFromNeighbors() {
		for(BlockGroup neighbor : neighborMap.values()) {
			neighbor.removeNeighbor(this);
		}
	}
	
	public void changeState() {
		System.out.println("changingState");
		for(BuildingBlock block : values()) {
			
			block.setState( (block.currentStateNumber() + 1) %  block.maxState);
			
		}
		gui.blockStateChanged(this);
		/*
		for( BlockGroup blockGroup : blockLinkList ) {
			blockGroup.changeState();
		}
		*/
	
	}
	
	public void addLink(BlockGroup blockGroup) {
		
		if( ! blockLinkList.contains(blockGroup) ) {
			blockLinkList.add(blockGroup);
		}
		
	}
	
	public void removeLink(BlockGroup blockGroup) {
		
		blockLinkList.remove(blockGroup);
		
	}
	
	public BuildingBlock put(Pos pos, BuildingBlock block) {
		
		BuildingBlock returnBlock = super.put(pos, block);
		
		return returnBlock;
		
	}
	
	public BuildingBlock put(Pos origin, BlockGroup blockGroup) {
		
		for(Map.Entry<Pos, BuildingBlock> blockEntry : blockGroup.entrySet()) {
			
			BuildingBlock block = blockEntry.getValue();
			//block.translateGroupOrigin(origin);
			super.put(origin.add(blockEntry.getKey()), blockEntry.getValue());
			
		}
		
		return new BuildingBlock();
		
	}
	
	public void display() {
		
		for(Map.Entry<Pos, BuildingBlock> blockEntry : innerMapSet ) {
			
			BuildingBlock blockValue = blockEntry.getValue();
			
			blockValue.display(blockEntry.getKey());
			
		}
		
	}
	
	public void displayEdit() {
		
		for(Map.Entry<Pos, BuildingBlock> blockEntry : innerMapSet ) {
			
			BuildingBlock blockValue = blockEntry.getValue();
			
			blockValue.displayEdit(blockEntry.getKey());
			
		}
		
	}
	
	public void rotate() {
		
		for(Map.Entry<Pos, BuildingBlock> blockEntry : innerMapSet ) {
			
			blockEntry.getKey().rotateWithCheck();
			blockEntry.getValue().rotate();
			
		}
		
		
		if( diagonal ){
			groupAxis = Direction.dirBend(groupAxis, 1);
		}
		
		diagonal = !diagonal;
		
	}
	
	public void flip() {
		
		for(Map.Entry<Pos, BuildingBlock> blockEntry : innerMapSet ) {
			
			blockEntry.getKey().flip(groupAxis);
			blockEntry.getValue().flip(groupAxis);
			
		}
		
	}
	
	public void revert() {
		
		for(Map.Entry<Pos, BuildingBlock> blockEntry : innerMapSet ) {
			
			blockEntry.getValue().revert();
			
		}
		
		groupAxis = Direction.antiDir(groupAxis);
		
	}
	
	public Pos getOrigin() {
		return origin;
	}
	
	// relative origin between groups, can be used in fusing between groups
	public void translateOrigin(Pos pos) {
		origin = origin.add(pos);
		
	}
	
	public BlockGroup clone() {
		
		BlockGroup groupClone = new BlockGroup(groupAxis);
		
		for(Map.Entry<Pos, BuildingBlock> blockEntry : innerMapSet) {
			
			groupClone.put(new Pos(blockEntry.getKey().x, blockEntry.getKey().y), blockEntry.getValue().clone());
			
		}
		
		return groupClone;
		
	}
	
	public static BlockGroup groupFuse(BlockGroup... groups) {
		
		BlockGroup fusedGroup = new BlockGroup(Direction.EAST);
		
		for(BlockGroup blockGroup : groups) {
			
			fusedGroup.put(blockGroup.getOrigin(), blockGroup);
			
		}
		
		return fusedGroup;
		
	}
	
	////////// FACTORY METHODS /////////
	// All have  EAST direction , LEFT bend by default
	
	public static BlockGroup newLongRoad(int length, int dir, boolean redLight) {
		
		BlockGroup roadGroup = new BlockGroup(dir);
		Pos tempPos;
		int cost = 0;
		
		for(int i = 0; i < length; i++) {
			
			tempPos = new Pos(i,0);
			tempPos.setRotationBounds(-i,i,-i,i);
			Road newRoad = new Road(dir, redLight, gui);
			roadGroup.put(tempPos, newRoad);
			cost = cost + newRoad.cost();
		}
		
		roadGroup.setCost(cost);
		
		return roadGroup;
		
	}
	
	public static BlockGroup newLaneRoad(int lanes, int dir, boolean redLight) {
		
		BlockGroup roadGroup = new BlockGroup(dir);
		BlockGroup tempGroup;
		int cost = 0;
		
		for(int i = 0; i < lanes; i++) {
			
			tempGroup = newLongRoad(lanes, dir, redLight);
			tempGroup.translateOrigin(new Pos(0,i));
			roadGroup = BlockGroup.groupFuse(roadGroup, tempGroup);
			cost = cost + tempGroup.cost();
			
		}
		
		roadGroup.setCost(cost);
		
		return roadGroup;
		
	}
	
	
	public static BlockGroup newRoad(int lanes, boolean redLight) {
		
		BlockGroup roadGroup = new BlockGroup(Direction.EAST);
		BuildingBlock tempBlock;
		Pos tempPos;
		int rotationDistance;
		int cost = 0;
		
		for(int i = 0; i < lanes; i++) {
			for(int j = 0; j < lanes; j++) {
				
				tempPos = new Pos(i,j);
				rotationDistance = Math.max(i, j);
				
				if( i == j) {
					tempPos.setAlternate(false);
					tempPos.setRotateCheck(false);
					tempPos.setRotateJump(1);
				}
				else if( i < j) {
					tempPos.setAlternate(true);
					tempPos.setRotateCheck(true);
					tempPos.setRotateJump(2);
				}
				else if( i > j) {
					tempPos.setAlternate(false);
					tempPos.setRotateCheck(false);
					tempPos.setRotateJump(1);
				}
				
				tempBlock = new Road(Direction.EAST, redLight, gui);
				tempPos.setRotationBounds(- rotationDistance, rotationDistance, - rotationDistance, rotationDistance);
				roadGroup.put(tempPos, tempBlock);
				
				cost = cost + tempBlock.cost();
				
			}
		}
		
		roadGroup.setCost(cost);
		
		return roadGroup;
		
	}
	
	public static BlockGroup newCurve(int size, boolean redLight) {
		
		BlockGroup curveGroup = new BlockGroup(Direction.EAST);
		BuildingBlock tempBlock;
		Pos tempPos;
		int rotationDistance;
		int cost = 0;
		
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				
				tempPos = new Pos(i,j);
				rotationDistance = Math.max(i, j);
				
				if( i == j ) {
					tempBlock = new Curve(Direction.EAST, Direction.LEFT, redLight, gui);
				}
				
				else if( i < j) {
					tempBlock = new Road(Direction.EAST, redLight, gui);
					tempPos.setAlternate(true);
					tempPos.setRotateCheck(true);
					tempPos.setRotateJump(2);
					
				}
				else {
					tempBlock = new Road(Direction.NORTH, redLight, gui);
					tempPos.setAlternate(true);
					tempPos.setRotateCheck(false);
					tempPos.setRotateJump(2);
					
				}
				
				
				tempPos.setRotationBounds(- rotationDistance, rotationDistance, - rotationDistance, rotationDistance);
				curveGroup.put(tempPos, tempBlock);
				cost = cost + tempBlock.cost();
				
			}
		}
		
		curveGroup.setCost(cost);
		
		return curveGroup;
		
	}
	
	public static BlockGroup newBendedRoad(int size, int dir, int bend, boolean redLight) {
		
		BlockGroup bendedRoadGroup = new BlockGroup(dir);
		BuildingBlock tempBlock;
		Pos tempPos;
		int cost = 0;
		
		for(int i = 0; i < size; i++) {
			for(int j = i; j < size; j++) {
				
				tempBlock = new BendedRoad(dir, bend, redLight, gui);
				tempPos = new Pos(i,j);
				
				bendedRoadGroup.put(tempPos, tempBlock);
				cost = cost + tempBlock.cost();
				System.out.println(tempPos);
				
			}
		}
		
		bendedRoadGroup.setCost(cost);
		
		return bendedRoadGroup;
		
	}

}
