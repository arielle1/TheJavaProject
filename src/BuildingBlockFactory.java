import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import processing.core.PImage;

/*
 * Factory Class for BuildingBlock. This is where we will get the different kinds of building blocks from
 */
public class BuildingBlockFactory {

	// TODO : have images?
	private PImage[] images;
	
	public BuildingBlockFactory() {
		
		/* TODO
		images[0] = loadImage(this.getClass().getResource("/res/'ImageName1'").getPath());
		images[1] = loadImage(this.getClass().getResource("/res/'ImageName2'").getPath());
		.
		.
		.
		images[n] = loadImage(this.getClass().getResource("/res/'ImageNameNN'").getPath());
		*/
		
	}
	
	
	// creates a new road type building block, with/without red lights
	public BuildingBlock getRoad(int dir, boolean redLight) {
		
		BuildingBlock road = new BuildingBlock();
		
			Matrix<Boolean> stateMatrixRedLight = new Matrix<>(4,4,false);
			Matrix<Boolean> stateMatrixGreenLight = new Matrix<>(4,4,false);
			stateMatrixGreenLight.set(Direction.antiDir(dir), dir, true);
			
			if(redLight) {
				road.addState(stateMatrixRedLight);
			}
			road.addState(stateMatrixGreenLight);
		
		/* TODO
			PImage roadImage = new PImage();
			road.setImage(roadImage);
		*/
		
		return road;
		
	}
	
	// create a curve ( left = -1, right = 1)
	public BuildingBlock getCurve(int dir, int bend, boolean redLight) {
		
		BuildingBlock curve = new BuildingBlock();
		
		Matrix<Boolean> stateMatrixRedLight = new Matrix<>(4,4,false);
		Matrix<Boolean> stateMatrixGreenLight = new Matrix<>(4,4,false);
		stateMatrixGreenLight.set(Direction.antiDir(dir), Direction.dirBend(dir, bend), true);
		
		if(redLight) {
			curve.addState(stateMatrixRedLight);
		}
		curve.addState(stateMatrixGreenLight);
	
	/* TODO
		PImage curveImage = new PImage();
		curve.setImage(curveImage);
	*/
	
	return curve;
		
	}
	
	public  BuildingBlock getCrossing(int dirX, int dirY, boolean redLight) {
		
		BuildingBlock crossing;
		BuildingBlock road1 = getRoad(dirX, redLight);
		BuildingBlock road2 = getRoad(dirY, redLight);
		
		if( redLight ) {
			// add the roads together to get an alternating redLight switch function
			road1.removeState(0);
			road2.removeState(0);
			crossing = blockSum(road1,road2);
		}
		else {
			// fuse the roads together to get a single always open intersection
			crossing = blockFuse(road1,road2);
		}
		
		return crossing;
		
	}

	// Similar to crossing. exit = true makes the curve part an exit, exit = false makes the curve part an an input
	public BuildingBlock getTCrossing(int dirRoad, int curveBend, boolean exit,  boolean redLight) {
		
		BuildingBlock T_crossing;
		BuildingBlock curve;
		BuildingBlock road =  getRoad(dirRoad, redLight);
		if( exit ) {
			curve = getCurve(dirRoad, curveBend, redLight);
		}
		// else reverse the curve, making it  an input curve
		else {
			curve = getCurve(Direction.antiDir(dirRoad), - curveBend, redLight);
			curve.revert();
		}
		
		if( redLight ) {
			road.removeState(0);
			curve.removeState(0);
			T_crossing = blockSum(road,curve);
		}
		else {
			T_crossing = blockFuse(road,curve);
		}
		
		return T_crossing;
		
	}
	
	public BuildingBlock blockFuse(BuildingBlock... blocks) {
		
		BuildingBlock fusedBlock = new BuildingBlock();
		int maxState = BuildingBlock.max(blocks).maxState();
		// "Fuse" (OR) the matrices together
		for(int stateNum = 0; stateNum < maxState; stateNum++) {
			
			Matrix<Boolean> tempFusedMatrix = new Matrix<Boolean>(4,4,false);
			
			for( BuildingBlock block : blocks ) {
				
				// Sum up a state over all blocks
				tempFusedMatrix = tempFusedMatrix.directOp(block.getState(stateNum), Matrix.boolOr);
				
			}
			
			// add one state, summed over all blocks to the fused block
			fusedBlock.addState(tempFusedMatrix);
			
		}
		
		return fusedBlock;
	}
	
	public BuildingBlock blockSum(BuildingBlock... blocks) {
		
		BuildingBlock blockSum = new BuildingBlock();
		// "Add" the matrices together
		for( BuildingBlock block : blocks ) {
			for(int stateNum = 0; stateNum < block.maxState(); stateNum++) {
				blockSum.addState(block.getState(stateNum));
			}
		}
		
		return blockSum;
		
	}
	
}
