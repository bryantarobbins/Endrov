/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import endrov.util.ProgressHandle;
import endrov.util.lazy.MemoizeX;




/**
 * Endrov image plane. Can be swapped to disk, lazily read and lazily generated. Images can share data using
 * copy-on-write semantics; copies following this are called shadows.
 * 
 * The user has to ensure exclusive access to memory and that it is in fact in memory before writing to it.
 * Thread-safety should be implemented by locking on the EvImage object. Memory is kept in place if lock()
 * is called prior to writing it. Remember to unlock() it after use or it can never be swapped out.
 * 
 * 
 * 
 * The damn data representation problem! can have another class, EvImageData, with all possible representations.
 * These are: 
 * * Signed/unsigned 8/16/32bit integer array
 * * float/double array 
 * * AWT image
 * 
 * Bonus feature: Java cannot handle unsigned data! AWT does some low-level interpretation. ways around:
 * * let these go from -127 to 127. Non-standard!
 * * ignore sign. +-* are the same on binary level. / is not and need special code; / is not good for integer images anyway
 * * cut one bit to make it fit
 * 
 * Support for extremely large pictures, how does this affect interface?
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvImagePlane  
	{

	//TODO
	//Memoize is not forgetful. Would be nice with a semi-lazy version that remembers evaluation time
	//and can throw away the result after a while. Forgetful memoization. But it should be possible
	//to permanently force evaluation such as when the source will become unavailable.
	
	
	/** Image this image shadows */
	private EvImagePlane shadowedImage=null;
	/** Images shadowing this image */
	private WeakHashMap<EvImagePlane, Object> shadowedBy=new WeakHashMap<EvImagePlane, Object>(1,1.0f);
	/** Pending lazy operations */
	//private WeakHashMap<Memoize<?>, Object> pendingLazy=new WeakHashMap<Memoize<?>, Object>(1,1.0f);
	private WeakHashMap<MemoizeX<?>, Object> pendingLazy=new WeakHashMap<MemoizeX<?>, Object>();
		
	/** 
	 * Connection to I/O. Allows lazy reading by postponing load operation. Also allows lazy generation by putting a generator as a loader.
	 * 
	 * */
	public EvImageReader io=null;
	
  /** Force rewrite, such as change of compression */ 
	public boolean isDirty=false;   
	
	/** In-memory image. Set to null if there is none. */
	private EvPixels memoryPixels=null;

	/** Swap file */
	private File swapIm=null;
	
	/**
	 * Cache: pointer to loaded image
	 */
	private SoftReference<EvPixels> cachedPixels=new SoftReference<EvPixels>(null);

	//Changes in resolution might screw up pending operations. Need to encapsulate!
	//TODO

	/**
	 * For I/O manager only: The image has been written to disk and there is an io-object set up to read it.
	 * Hence the image need not stay in image anymore. This unlocks the memory by placing the image in the cache.
	 */
	public void ioIsNowOnDisk(ProgressHandle progh)
		{
		cachedPixels=new SoftReference<EvPixels>(getPixels(progh));
		memoryPixels=null;
		isDirty=false;
		EvImageCache.addToCache(this);
		}
	
	public EvImagePlane()
		{
		}

	/**
	 * Create an image and setPixelsReference
	 */
	public EvImagePlane(EvPixels p)
		{
		setPixelsReference(p);
		}

	/**
	 * Register a lazy operation. Before this image is changed, it will be executed
	 */
	public void registerLazyOp(MemoizeX<?> op)
		{
		pendingLazy.put(op,null);
		}

	
	/**
	 * Make sure this is a hard copy. Always safe to call. Seldom useful, use only if you know what you are doing
	 */
	public void makeSureHardCopy(ProgressHandle progh)
		{
		if(shadowedImage!=null)
			getShadowDataInternal(progh);
		}
	
	/**
	 * Copy data from shadowed image here. Make sure this truly is a shadowed image before calling
	 */
	private void getShadowDataInternal(ProgressHandle progh)
		{
		EvPixels otherPixels=shadowedImage.getPixels(progh);
		if(otherPixels!=null)
			memoryPixels=new EvPixels(otherPixels);
		else
			memoryPixels=null;
		
		shadowedImage.shadowedBy.remove(this);
		shadowedImage=null;
		}
	
	/**
	 * Eliminate dependencies:
	 * * Give data to all images that shadow this image
	 * * Execute pending lazy operations
	 */
	private void eliminateDependencies(ProgressHandle progh)
		{
		for(EvImagePlane evim:new LinkedList<EvImagePlane>(shadowedBy.keySet()))
			evim.getShadowDataInternal(progh);
		for(MemoizeX<?> op:new LinkedList<MemoizeX<?>>(pendingLazy.keySet()))
			op.get(progh);
		}
	
	
	
	/**
	 * Make an image that points to this image for data.
	 * Data is copy-on-write
	 * 
	 *  
	 */
	public EvImagePlane makeShadowCopy()
		{
		EvImagePlane copy=new EvImagePlane();
		copy.shadowedImage=this;
		shadowedBy.put(copy, null);
		return copy;
		}
	
	/**
	 * Precise copy of the image that contains its own data
	 */
	public EvImagePlane makeHardCopy(ProgressHandle progh)
		{
		//This could be made potentially faster, keeping it abstract for now
		EvImagePlane copy=makeShadowCopy();
		copy.getShadowDataInternal(progh);
		return copy;
		}
	
	/**
	 * Must be called prior to making changes to mutable objects
	 */
	public void prepareForWrite(ProgressHandle progh)
		{
		eliminateDependencies(progh);
		if(shadowedImage!=null)
			getShadowDataInternal(progh);
		}
	
	
	
	
	
	
	/**
	 * ONLY for use by I/O system
	 */
	public EvPixels getMemoryImage()
		{
		return memoryPixels;
		}

	
	/**
	 * ONLY for use by I/O system
	 */
	public void setMemoryImage(BufferedImage im)
		{
		if(im==null)
			this.memoryPixels=null;
		else
			this.memoryPixels.setPixels(im);
		}
	public void setMemoryImage(EvPixels im)
		{
		if(im==null)
			this.memoryPixels=null;
		else
			this.memoryPixels.setPixels(im);
		}
	
	
	/**
	 * Remove cached image. Can be called whenever.
	 */
	public void clearCachedImage()
		{
		cachedPixels.clear();
		}
	
	/**
	 * Get AWT representation of image. This should be as fast as it can be, but since AWT has limitations, data might be lost.
	 * It is the choice for rendering or if AWT is guaranteed to be able to handle the image.
	 * 
	 * This image is read-only unless it is again set to be the AWT image of this EVImage. (best-practice?)
	 * This has to be done *before* writing to the image.
	 * TODO is this the best way? separate method?
	 * 
	 * 
	 * This does not give a copy. to be damn sure there won't be any problems, you need to lock the data!
	 * 
	 * @deprecated use expixels?
	 * 
	 */
	public BufferedImage getJavaImage(ProgressHandle progh)
		{
		return getPixels(progh).getReadOnly(EvPixelsType.AWT).getAWT();
		}

	
	public EvPixels getPixels()
		{
		return getPixels(null);
		}

	/**
	 * Get pixel data for image
	 * 
	 * TODO changes to pixels should stay
	 */
	public EvPixels getPixels(ProgressHandle progh)
		{
		//Use in-memory image
		if(memoryPixels!=null)
			return memoryPixels;
		else
			{
			//Use cache-memory
			EvPixels loaded=cachedPixels.get();
			if(loaded!=null)
				{
				EvImageCache.addToCache(this);
				return loaded;
				}
			else
				{
				//Load from swap memory if previously unloaded
				if(swapIm!=null)
					{
					try
						{
						memoryPixels=new EvPixels(ImageIO.read(swapIm));
						swapIm=null;
						return memoryPixels;
						}
					catch (IOException e)
						{
						e.printStackTrace();
						return null;
						}
					}
				else
					{
					//If this image shadows another one, use it
					if(shadowedImage!=null)
						return shadowedImage.getPixels(progh);
					else
						{
						//Use IO to load image (might also execute operation)
						loaded=new EvPixels(io.get(progh));
						cachedPixels=new SoftReference<EvPixels>(loaded);
						EvImageCache.addToCache(this);
						return loaded;
						}
					}
				}
			}
		}
	
	
	
	
	/**
	 * Get array representation of image.
	 * In the next-gen EV imaging API this will be the central function but at the moment
	 * the AWT interface is faster.
	 * 
	 * 2D arrray? java has trouble with these. the primary interface should maybe be a 1d-array+width
	 * 
	 * @deprecated use getPixels
	 */
	public double[][] getArrayImage(ProgressHandle progh)
		{
		return getPixels(progh).getArrayDouble2D();
		}
	
	/**
	 * Check if this memory has been modified since it was loaded into memory
	 */
	public boolean modified()
		{
		return memoryPixels!=null || swapIm!=null || isDirty || (shadowedImage!=null && shadowedImage.modified());
		}
	
	/**
	 * Modify image by setting a new image in this container. AWT format: Only use this format if no data will be lost.
	 * Will call prepareForWrite automatically
	 * 
	 * @deprecated
	 */
	public void setImage(BufferedImage im)
		{
		prepareForWrite(new ProgressHandle());
		this.memoryPixels=new EvPixels(im);
		cachedPixels.clear();
		cachedPixels=new SoftReference<EvPixels>(null); //Really needed?
		}

	/**
	 * Set pixel data. Will NOT make a copy, makes a reference. Caller has to supply a copy if the pixels are to be used elsewhere as well. 
	 */
	public void setPixelsReference(EvPixels im)
		{
		prepareForWrite(new ProgressHandle()); //Better handling of shadowing would eliminate the need for this!
		this.memoryPixels=im;
		cachedPixels.clear();
		cachedPixels=new SoftReference<EvPixels>(null); //Really needed?
		}

	
	
	//what to do about this? now it points to io
	
	
	public String toString()
		{
		return "EvImage mempxl: "+memoryPixels+" shdw:"+shadowedImage+" shdwBy#:"+shadowedBy.size()+" io "+io;
		}

	
	public void registerMemoizeXdepends(MemoizeX<?> memoize)
		{
		if(io!=null)
			memoize.dependsOn(io);
		registerLazyOp(memoize);
		}

	}
