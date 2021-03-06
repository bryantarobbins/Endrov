/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.JImageButton;
import endrov.gui.icon.BasicIcon;
import endrov.util.math.Vector2i;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: kernel constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitMorphConstKernel extends FlowUnitBasic
	{
	private int[] extent=new int[]{2,2,2,2}; //Extension left, right, up, down
	private static enum TypePixel{NONE,HIT,MISS}
	
	private HashSet<Vector2i> hitlist=new HashSet<Vector2i>();
	private HashSet<Vector2i> misslist=new HashSet<Vector2i>();
	private boolean isBinary=false;

	public FlowUnitMorphConstKernel()
		{
		textPosition=TEXTABOVE;
		}
	
	private TypePixel getPixelType(Vector2i v)
		{
		if(hitlist.contains(v))
			return TypePixel.HIT;
		else if(misslist.contains(v))
			return TypePixel.MISS;
		else
			return TypePixel.NONE;
		}

	private void setIsBinary(boolean b)
		{
		isBinary=b;
		}

	/**
	 * Cycle pixel type at given position
	 */
	private void cyclePixelType(Vector2i v)
		{
		TypePixel curType=getPixelType(v);
		hitlist.remove(v);
		misslist.remove(v);
		if(curType==TypePixel.NONE)
			curType=TypePixel.HIT;
		else if(curType==TypePixel.HIT)
			curType=TypePixel.MISS;
		else
			curType=TypePixel.NONE;
		
		if(curType==TypePixel.HIT)
			hitlist.add(v);
		else if(curType==TypePixel.MISS)
			misslist.add(v);
		}
	
	//private static ImageIcon icon=null;//new ImageIcon(FlowUnitMorphConstKernel.class.getResource("jhBoolean.png"));
	private static final String metaType="constMorphKernel2D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Kernel",metaType,FlowUnitMorphConstKernel.class, 
				CategoryInfo.icon,"Constant kernel");
		Flow.addUnitType(decl);
		}
	
	public String toXML(Element e)
		{
		for(Vector2i v:hitlist)
			{
			Element ne=new Element("hit");
			ne.setAttribute("x", ""+v.x);
			ne.setAttribute("y", ""+v.y);
			e.addContent(ne);
			}
		for(Vector2i v:misslist)
			{
			Element ne=new Element("miss");
			ne.setAttribute("x", ""+v.x);
			ne.setAttribute("y", ""+v.y);
			e.addContent(ne);
			}
		for(int i=0;i<4;i++)
			e.setAttribute("extent"+i, ""+extent[i]);
		e.setAttribute("isBinary",""+isBinary);
		return metaType;
		}

	public void fromXML(Element e)
		{
		for(int i=0;i<4;i++)
			extent[i]=Integer.parseInt(e.getAttributeValue("extent"+i));
		for(Object one:e.getChildren())
			{
			Element ne=(Element)one;
			if(ne.getName().equals("hit"))
				hitlist.add(new Vector2i(
						Integer.parseInt(ne.getAttributeValue("x")),
						Integer.parseInt(ne.getAttributeValue("y"))));
			else if(ne.getName().equals("miss"))
				misslist.add(new Vector2i(
						Integer.parseInt(ne.getAttributeValue("x")),
						Integer.parseInt(ne.getAttributeValue("y"))));
			}
		isBinary=Boolean.parseBoolean(e.getAttributeValue("isBinary"));
		}

	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		if(isBinary)
			lastOutput.put("out", new MorphKernelGeneralBinary(hitlist,misslist)); 
		else
			lastOutput.put("out", new MorphKernelGeneralGray(hitlist,misslist)); 
		}
	

	/**
	 * Panel with all pixels annotated
	 * @author Johan Henriksson
	 */
	private class KernelPixels extends JPanel implements MouseListener
		{
		private static final long serialVersionUID = 1L;
		private final int gsize=16;

		public KernelPixels()
			{
			addMouseListener(this);
			}
		
		@Override
		public Dimension getMinimumSize()
			{
			int numX=extent[0]+extent[1]+1;
			int numY=extent[2]+extent[3]+1;
			System.out.println("min size "+new Dimension(numX*gsize+1,numY*gsize+1));
			return new Dimension(numX*gsize+1,numY*gsize+1);
			}
		
		@Override
		public Dimension getPreferredSize()
			{
			return getMinimumSize();
			}
		
		private int getOffsetX()
			{
			int startX=-extent[0];
			int numX=extent[0]+extent[1]+1;
			int residual=(getWidth() - numX*gsize)/2;
			return Math.max(0,residual)-startX*gsize;
			}
		
		private int getOffsetY()
			{
			int startY=-extent[2]; //Follow pixel coordinates
			int numY=extent[2]+extent[3]+1;
			int residual=(getHeight() - numY*gsize)/2;
			return Math.max(0,residual)-startY*gsize;
			}
		
		
		protected void paintComponent(Graphics g)
			{
			int startX=-extent[0];
			int startY=-extent[2]; //Follow pixel coordinates
			int endX=extent[1];
			int endY=extent[3];

			int ox=getOffsetX();
			int oy=getOffsetY();
			
			//Draw grid
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			for(int ay=startY;ay<=endY;ay++)
				for(int ax=startX;ax<=endX;ax++)
					{
					int px=ox+ax*gsize;
					int py=oy+ay*gsize;
					g.setColor(Color.GRAY);
					if(ax==0 && ay==0)
						g.fillRect(px, py, gsize+1, gsize+1);
					else
						g.drawRect(px, py, gsize, gsize);
					TypePixel type=getPixelType(new Vector2i(ax,ay)); //TypePixel.hit;
					if(type==TypePixel.HIT)
						{
						g.setColor(Color.RED);
						g.drawLine(px+2, py+2, px+gsize-1-2,py+gsize-1-2);
						g.drawLine(px+2, py+gsize-1-2, px+gsize-1-2,py+2);
						}
					else if(type==TypePixel.MISS)
						{
						g.setColor(Color.BLUE);
						g.drawLine(px+2, py+2, px+gsize-1-2,py+gsize-1-2);
						g.drawLine(px+2, py+gsize-1-2, px+gsize-1-2,py+2);
						}
					}
			}

		/**
		 * Mouse click: update position
		 */
		public void mouseClicked(MouseEvent e)
			{
			int x=(int)Math.ceil((e.getX()-getOffsetX())/(double)gsize);
			int y=(int)Math.ceil((e.getY()-getOffsetY())/(double)gsize);
			cyclePixelType(new Vector2i(x-1,y-1));
			removeOutside();
			repaint();
			}

		public void mouseEntered(MouseEvent arg0){}
		public void mouseExited(MouseEvent arg0){}
		public void mousePressed(MouseEvent arg0){}
		public void mouseReleased(MouseEvent arg0){}
		}

	
	
	
	/**
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel
		{
		private static final long serialVersionUID = 1L;
		private KernelPixels pPixels=new KernelPixels();

		private JCheckBox cIsBinary=new JCheckBox("Binary");
		
		public TotalPanel()
			{
			super(new BorderLayout());
			
			JPanel sub=new JPanel(new BorderLayout());

			JButton bLeftPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
			JButton bLeftMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");
			JButton bRightPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
			JButton bRightMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");
			JButton bUpPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
			JButton bUpMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");
			JButton bDownPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
			JButton bDownMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");

			sub.add(EvSwingUtil.layoutEvenVertical(bLeftMinus,bLeftPlus),BorderLayout.WEST);
			sub.add(EvSwingUtil.layoutEvenVertical(bRightMinus,bRightPlus),BorderLayout.EAST);
			sub.add(EvSwingUtil.layoutEvenHorizontal(bUpMinus,bUpPlus),BorderLayout.NORTH);
			sub.add(EvSwingUtil.layoutEvenHorizontal(bDownMinus,bDownPlus),BorderLayout.SOUTH);
			sub.add(pPixels,BorderLayout.CENTER);
			add(sub,BorderLayout.CENTER);
			add(cIsBinary,BorderLayout.SOUTH);
			setOpaque(false);
			sub.setOpaque(false);
			cIsBinary.setOpaque(false);
			
			cIsBinary.setSelected(isBinary);
			
			addExtentListener(0, bLeftPlus, bLeftMinus);
			addExtentListener(1, bRightPlus, bRightMinus);
			addExtentListener(2, bUpPlus, bUpMinus);
			addExtentListener(3, bDownPlus, bDownMinus);
			cIsBinary.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){setIsBinary(cIsBinary.isSelected());}});
			
			}
				
		private void addExtentListener(final int i, JButton bPlus, JButton bMinus)
			{
			bMinus.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
					{
					if(extent[i]>0)
						extent[i]--;
					newExtent();
					}});
			bPlus.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				extent[i]++;
				newExtent();
				}});
			}

		/**
		 * Called whenever the size of the kernel has changed and the component should resize
		 */
		private void newExtent()
			{
			removeOutside();
			
			//TODO actually works for other flows when they are moved!
			//e.g. comment
			
			//getParent().remove(this);
			//getParent().add(this);
			
			System.out.println("new extent!!!");
			setSize(getMinimumSize());
			invalidate();
			revalidate();
			((FlowView)getParent()).doFlowSwingLayout();
			getParent().repaint();
			}
		

		}

	/**
	 * Remove pixels outside extent
	 */
	private void removeOutside()
		{
		for(Vector2i v:new LinkedList<Vector2i>(hitlist))
			if(v.x<-extent[0] || v.x>extent[1] || v.y<-extent[2] || v.y>extent[3])
				hitlist.remove(v);
		for(Vector2i v:new LinkedList<Vector2i>(misslist))
			if(v.x<-extent[0] || v.x>extent[1] || v.y<-extent[2] || v.y>extent[3])
				misslist.remove(v);
		}

	public Component getGUIcomponent(final FlowView p)
		{
		return new TotalPanel();
		}

	@Override
	public String getBasicShowName()
		{
		return "Make kernel";
		}

	@Override
	public Color getBackground()
		{
		return CategoryInfo.bgColor;
		}

	@Override
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
		}

	@Override
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}

	@Override
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", MorphKernel.FLOWTYPE);
		}

	public String getHelpArticle()
		{
		return "Flow Morphology";
		}

	}
