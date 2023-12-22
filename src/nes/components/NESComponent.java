package components;

import java.io.IOException;

import components.mapper.Mapper;
import exceptions.AddressException;
import exceptions.InstructionException;
import exceptions.MapperException;
import exceptions.NotNesFileException;

public interface NESComponent {

	public void start()
			throws AddressException, IOException, InstructionException, NotNesFileException, MapperException;

	public void reset() throws AddressException;

	public void tick() throws AddressException;

	public void initMapping(Mapper mapper) throws AddressException;

}
