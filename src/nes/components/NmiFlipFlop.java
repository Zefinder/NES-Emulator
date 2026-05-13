package components;

import java.util.concurrent.atomic.AtomicBoolean;

public class NmiFlipFlop extends Component {

	private AtomicBoolean nmiState;
	
	public NmiFlipFlop() {
		this.nmiState = new AtomicBoolean(false);
	}
	
	public void setNmiState(boolean nmiState) {
		this.nmiState.set(nmiState);
	}
	
	public boolean getNmiState() {
		return nmiState.get();
	}
	
	@Override
	public boolean checkImpl() {
		// Not link to another component
		return true;
	}

}
