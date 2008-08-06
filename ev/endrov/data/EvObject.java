package endrov.data;

import javax.swing.*;
import org.jdom.*;

public abstract class EvObject
	{
	public boolean metaObjectModified=false;
	public abstract void saveMetadata(Element e);
	public abstract String getMetaTypeDesc();
	public abstract void buildMetamenu(JMenu menu);
	}