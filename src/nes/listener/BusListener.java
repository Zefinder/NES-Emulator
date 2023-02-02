package listener;

public interface BusListener {

	public void onValueChanged(boolean isCpuBus, int address, byte value);
}
