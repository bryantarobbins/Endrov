package endrov.filterBasic;
import endrov.ev.PluginDef;


public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Basic filters";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		return new Class[]{
				ContrastBrightnessFilter.class, 
				EqualizeHistogram.class,
				DeinterlaceFilter.class,
				FilterAddConst.class
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
