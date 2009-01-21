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
		return new Class[]{};
/*		return new Class[]{
				NoisePepperAndSalt.class, 
				NoisePoisson.class,
				NoiseExponential.class,
				ContrastBrightnessFilter.class, 
				EqualizeHistogram.class,
				DeinterlaceFilter.class,
				ConfocalXShiftFilter.class,
				FilterAddConst.class
				};*/
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
