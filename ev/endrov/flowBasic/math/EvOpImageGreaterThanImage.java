package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * A>B
 * 
 * TODO what type to output? type parameter?
 */
public class EvOpImageGreaterThanImage extends EvOpSlice1
	{
	public EvPixels exec1(EvPixels... p)
		{
		return greater(p[0],p[1]);
		}
	
	public static EvPixels greater(EvPixels a, EvPixels b)
		{
	/*	
		//TODO Should use the common higher type here
		a=a.convertTo(EvPixelsType.TYPE_INT, true);
		b=b.convertTo(EvPixelsType.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=bool2int(aPixels[i]>bPixels[i]);
		
		return out;
*/		
		//TODO Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		b=b.getReadOnly(EvPixelsType.DOUBLE);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.INT,w,h);
		double[] aPixels=a.getArrayDouble();
		double[] bPixels=b.getArrayDouble();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=bool2int(aPixels[i]>bPixels[i]);
		
		return out;
		}
	
	private static int bool2int(boolean b)
		{
		return b ? 1 : 0;
		}
	}