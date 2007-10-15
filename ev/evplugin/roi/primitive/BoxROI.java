package evplugin.roi.primitive;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jdom.*;

import evplugin.roi.*;
import evplugin.basicWindow.BasicWindow;
import evplugin.imageset.*;

/**
 * Rectangle, Box, or higher dimension
 * 
 * @author Johan Henriksson
 */
public class BoxROI extends ROI
	{
	/******************************************************************************************************
	 *                               Range class                                                          *
	 *****************************************************************************************************/
	public static class Span
		{
		public Span(){all=true;}
		public Span(double start, double end){this.start=start;this.end=end;all=false;}
		public boolean all;
		public double start, end;
		public boolean inRange(double x)
			{
			return all || (x>=start && x<end);
			}
		}
	

	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private static class RectLineIterator extends LineIterator
		{
		int endY;
		public boolean next()
			{
			y++;
			return y<endY;
			//Ignoring boundaries for now
			}	
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public TreeSet<String> regionChannels=new TreeSet<String>(); //Empty=all
	public Span regionFrames=new Span();
	public Span regionX=new Span(50,200);
	public Span regionY=new Span();
	public Span regionZ=new Span();
	
	
	
	public String getROIDesc()
		{
		return "Box";
		}
	
	

	/**
	 * Get channels that at least are partially selected
	 */
	public Set<String> getChannels(Imageset rec)
		{
		TreeSet<String> c=new TreeSet<String>();
		for(String s:rec.channelImages.keySet())
			if(channelInRange(s))
				c.add(s);
		return c;
		}
	
	/**
	 * Get frames that at least are partially selected
	 */
	public Set<Integer> getFrames(Imageset rec, String channel)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		Imageset.ChannelImages ch=rec.getChannel(channel);
		if(ch!=null)
			{
			for(int f:ch.imageLoader.keySet())
				if(regionFrames.inRange(f))
					c.add(f);
			}
		return c;
		}
	
	
	/**
	 * Get slices that at least are partially selected
	 */
	public Set<Integer> getSlice(Imageset rec, String channel, int frame)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		Imageset.ChannelImages ch=rec.getChannel(channel);
		if(ch!=null)
			{
			TreeMap<Integer,EvImage> slices=ch.imageLoader.get(frame);
			if(slices!=null)
				for(int f:slices.keySet())
					if(regionZ.inRange(f))
						c.add(f);
			}
		return c;
		}
	
	/**
	 * Check if a channel is included in the ROI
	 */
	private boolean channelInRange(String channel)
		{
		return regionChannels.isEmpty() || 
		       regionChannels.contains(channel);
		}
	

	public boolean imageInRange(String channel, double frame, int z)
		{
		return channelInRange(channel) && regionFrames.inRange(frame) && regionZ.inRange(z);
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(final Imageset rec, final String channel, final int frame, final int z)
		{
		if(imageInRange(channel, frame, z))
			{
			//todo: check if there is something here
			EvImage im=rec.getChannel(channel).getImageLoader(frame, z);

			//Initial boundary: cover entire image
			RectLineIterator it=new RectLineIterator();
			it.startX=0;
			it.endX=im.getJavaImage().getWidth();
			it.endY=im.getJavaImage().getHeight();
			it.y=0;
			
			//Correct boundaries based on span
			if(!regionX.all)
				{
				if(it.startX<regionX.start)
					it.startX=(int)regionX.start; //todo: change coordinates
				if(it.endX>regionX.end)
					it.endX=(int)regionX.end;
				
				//todo: other coords
				}

			if(!regionY.all)
				{
				if(it.y<regionY.start)
					it.y=(int)regionY.start; //todo: change coordinates
				if(it.endY>regionY.end)
					it.endY=(int)regionY.end;
				
				//todo: other coords
				}

			//todo: sanity check
			
			//One line before when starting to work
			it.y--;
			return it;
			}
				
		return new EmptyLineIterator();
		}
	

	
	
	public void saveMetadata(Element e)
		{
		e.setName("ROI rect");
		
		}
	
	//ImageIterator?
	
	//get iterator: image + line iterator or pixel iterator or entire image?
	//which channels, frames, slices are affected?
	
	
	
	
	
	
	private class SpanWidget implements ActionListener, DocumentListener
		{
		public JTextField spinnerS=new JTextField();
		public JTextField spinnerE=new JTextField();
		public final JCheckBox cSpan;
		public final Span span;
		
		public SpanWidget(String name, Span span)
			{
			cSpan=new JCheckBox(name,!span.all);
			this.span=span;
			spinnerS.setText(""+span.start);
			spinnerE.setText(""+span.end);
			spinnerS.getDocument().addDocumentListener(this);
			spinnerE.getDocument().addDocumentListener(this);
			cSpan.addActionListener(this);
			}

		public void actionPerformed(ActionEvent e){apply();}
		public void changedUpdate(DocumentEvent e){apply();}
		public void insertUpdate(DocumentEvent e){apply();}
		public void removeUpdate(DocumentEvent e){apply();}
		public void apply()
			{
			try
				{
				span.all=!cSpan.isSelected();
				span.start=Double.parseDouble(spinnerS.getText());
				span.end  =Double.parseDouble(spinnerE.getText());
				BasicWindow.updateWindows();
				}
			catch (NumberFormatException e){}
			}
		}
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JPanel getROIWidget()
		{
		final SpanWidget spans[]={
				new SpanWidget("<= Frame <",regionFrames),
				new SpanWidget("<= X <",regionX),
				new SpanWidget("<= Y <",regionY),
				new SpanWidget("<= Z <",regionZ)};	
		JPanel pane=new JPanel(new GridLayout(spans.length+1,3));

		//Channel list
		String outc="";
		for(String s:regionChannels)
			{
			if(outc.equals(""))
				outc=outc+s;
			else
				outc=outc+s+",";
			}
		final JTextField fChannels=new JTextField(outc);
		fChannels.getDocument().addDocumentListener(new DocumentListener()
			{
			public void changedUpdate(DocumentEvent e){apply();}
			public void insertUpdate(DocumentEvent e){apply();}
			public void removeUpdate(DocumentEvent e){apply();}
			public void apply()
				{
				for(SpanWidget w:spans)
					w.apply();
				StringTokenizer tok=new StringTokenizer(fChannels.getText(),",");
				regionChannels.clear();
				while(tok.hasMoreTokens())
					regionChannels.add(tok.nextToken().trim());
				for(String s:regionChannels)
					System.out.print(" "+s);
				System.out.println("");
				BasicWindow.updateWindows();
				}
			});
		
		//Put widgets together
		pane.add(new JLabel("Channels"));
		pane.add(fChannels);
		pane.add(new JLabel(""));
		for(SpanWidget s:spans)
			{
			pane.add(s.spinnerS);
			pane.add(s.cSpan);
			pane.add(s.spinnerE);
			}
		return pane;
		}
	
	
	public class BoxHandle implements Handle
		{
		private final boolean isStartX, isStartY;
		public BoxHandle(Span sx, boolean isStartX, boolean isStartY)
			{
			this.isStartX=isStartX;
			this.isStartY=isStartY;
			}
		
		//TODO: what about "all"?
		
		public double getX()
			{
			if(isStartX) return regionX.start;
			else return regionX.end;
			}

		public double getY()
			{
			if(isStartY) return regionY.start;
			else return regionY.end;
			}

		public void setX(double x)
			{
			if(isStartX) regionX.start=x;
			else regionX.end=x;
			}

		public void setY(double y)
			{
			if(isStartY) regionY.start=y;
			else regionY.end=y;
			}
		}
	
	/**
	 * Get handles for corners
	 */
	public Handle[] getHandles()
		{
		//button in x,y window: place in middle?
		//imagewindow temporary tools: like create box etc
		
		return new Handle[]{};
		}
		
	}
