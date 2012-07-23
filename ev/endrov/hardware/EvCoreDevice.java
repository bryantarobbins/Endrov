package endrov.hardware;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.recording.device.HWAutoFocus;
import endrov.recording.device.HWCamera;
import endrov.recording.device.HWShutter;
import endrov.recording.device.HWSpatialLightModulator;
import endrov.recording.device.HWStage;
import endrov.util.EvMathUtil;


public class EvCoreDevice implements EvDevice
	{
	public EvDeviceObserver event=new EvDeviceObserver();
	private TreeMap<String, String> propMap=new TreeMap<String, String>();
	private TreeMap<String, DevicePropertyType> propType=new TreeMap<String, DevicePropertyType>();
	
	public EvCoreDevice()
		{
		propMap.put("AutoShutter", "1"); //What value??
		propType.put("AutoShutter", DevicePropertyType.getEditableBooleanState());
		//ChannelGroup - unused
		//ImageProcessor - unused
		//Initialize - unused
		
		
		updateDeviceCategories();
		}
	
	public void updateDeviceCategories()
		{
		updateCategorySetting("AutoFocus", HWAutoFocus.class);
		updateCategorySetting("Camera", HWCamera.class);
		updateCategorySetting("Focus", HWStage.class);
		updateCategorySetting("SLM", HWSpatialLightModulator.class);
		updateCategorySetting("Shutter", HWShutter.class);
		updateCategorySetting("XYStage", HWStage.class);
		event.emit(this, this);
		}
	
	
	private void updateCategorySetting(String name, Class<? extends EvDevice> cl)
		{
		DevicePropertyType type=collectDeviceCategory(name, cl);
		propType.put(name, type);
		
		String value=propMap.get(name);
		if(value==null)
			value="";
		
		//Any other device than the null device is a better default!
		if(value.equals("") && type.categories.size()>1)
			for(String s:type.categories)
				if(!s.equals(""))
					{
					value=s;
					break;
					}
		propMap.put(name, value);
		}
	
	
	private static boolean isXYstage(EvDevicePath p)
		{
		HWStage d=(HWStage)p.getDevice();
		if(d.getNumAxis()==2)
			{
			//This can be improved
			return true;
			}
		else
			return false;
		}
	
	/**
	 * Collect all devices belonging to a particular category
	 */
	private static DevicePropertyType collectDeviceCategory(String name, Class<? extends EvDevice> cl)
		{
		Set<EvDevicePath> paths=EvHardware.getDeviceMap(cl).keySet();
		LinkedList<EvDevicePath> lpaths=new LinkedList<EvDevicePath>();
		for(EvDevicePath p:paths)
			{
			if(name.equals("XYStage"))
				{
				if(isXYstage(p))
					lpaths.add(p);
				}
			else if(name.equals("Focus"))
				{
				if(!isXYstage(p))
					lpaths.add(p);
				}
			else
				lpaths.add(p);
			}
		
		String[] spaths=new String[lpaths.size()+1];
		spaths[0]="";
		for(int i=0;i<lpaths.size();i++)
			spaths[i+1]=lpaths.get(i).toString();
		return DevicePropertyType.getEditableCategoryState(spaths);
		}
	
	
	

	public String getDescName()
		{
		return "Endrov core device";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		return Collections.unmodifiableSortedMap(propMap);
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		return Collections.unmodifiableSortedMap(propType);
		}

	public String getPropertyValue(String prop)
		{
		return propMap.get(prop);
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		String val=propMap.get(prop);
		if(val!=null)
			return val.equals("1");
		else
			return null;
		}

	public boolean hasConfigureDialog()
		{
		return false;
		}
	public void openConfigureDialog(){}


	public void setPropertyValue(String prop, String value)
		{
		propMap.put(prop, value);
		event.emit(this, this);
		}

	public void setPropertyValue(String prop, boolean value)
		{
		setPropertyValue(prop, ""+EvMathUtil.toInt(value));
		}


	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}
	
	
	
	
	
	public EvDevicePath getCurrentDeviceAutofocus()
		{
		return new EvDevicePath(getPropertyValue("AutoFocus"));
		}
	
	public EvDevicePath getCurrentDeviceCamera()
		{
		return new EvDevicePath(getPropertyValue("Camera"));
		}
	
	public EvDevicePath getCurrentDeviceFocus()
		{
		return new EvDevicePath(getPropertyValue("Focus"));
		}
	
	public EvDevicePath getCurrentDeviceSLM()
		{
		return new EvDevicePath(getPropertyValue("SLM"));
		}
	
	public EvDevicePath getCurrentDeviceShutter()
		{
		return new EvDevicePath(getPropertyValue("Shutter"));
		}
	
	public EvDevicePath getCurrentDeviceXYStage()
		{
		return new EvDevicePath(getPropertyValue("XYStage"));
		}

	public boolean getAutoShutter()
		{
		return getPropertyValueBoolean("AutoShutter");
		}
	
	
	}
