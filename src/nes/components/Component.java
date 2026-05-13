package components;

import exceptions.ComponentCheckException;

public abstract class Component {

	public void check() throws ComponentCheckException {
		if (!checkImpl()) {
			throw new ComponentCheckException(
					"Error when checking component %s... Are you sure you didn't forget anything?".formatted(this.getClass().getSimpleName()));
		}
	}

	protected abstract boolean checkImpl();

}
