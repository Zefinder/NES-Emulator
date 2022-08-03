package nes.components;

import java.io.IOException;

import nes.components.mapper.Mapper;
import nes.exceptions.AddressException;
import nes.exceptions.InstructionException;
import nes.exceptions.MapperException;
import nes.exceptions.NotNesFileException;

public interface Component {
	
	public void start() throws AddressException, IOException, InstructionException, NotNesFileException, MapperException;
	public void reset() throws AddressException;
	public void tick() throws AddressException;
	public void initMapping(Mapper mapper) throws AddressException;

}
