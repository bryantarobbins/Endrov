/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous;

/**
 * @author David Johansson, Arvid Johansson
 */
public class FrivolousComplexLib
	{

	static public float[] getRealArray(FrivolousComplexArray complex)
		{
		float[] result = new float[complex.length];
		for (int i = 0; i<complex.length; i++)
			result[i] = complex.real[i];

		return result;
		}

	static public float[] getRealArrayFirstHalf(FrivolousComplexArray complex)
		{
		float[] result = new float[complex.length<<1];

		for (int i = 0; i<complex.length; i++)
			result[i] = complex.real[i];

		return result;
		}

	static public float[] getCombinedArray(FrivolousComplexArray complex)
		{
		float[] result = new float[complex.length<<1];
		for (int i = 0; i<complex.length; i++)
			{
			result[i<<1] = complex.real[i];
			result[(i<<1)+1] = complex.imag[i];
			}

		return result;
		}

	static public float[] getImaginaryArray(FrivolousComplexArray complex)
		{
		float[] result = new float[complex.length];
		for (int i = 0; i<complex.length; i++)
			result[i] = complex.imag[i];

		return result;
		}

	public static FrivolousComplexArray getComplexArray(float[] combined, int w, int h)
		{
		float[] real = new float[combined.length>>1];
		float[] imag = new float[combined.length>>1];

		for (int i = 0; i<(combined.length>>1); i++)
			{
			real[i] = combined[i<<1];
			imag[i] = combined[(i<<1)+1];
			}

		return new FrivolousComplexArray(real, imag, w, h);
		}

	public static FrivolousComplexArray getComplexMultiplication(FrivolousComplexArray a,
			FrivolousComplexArray b)
		{
		if (a.length!=b.length)
			throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];
		float[] imag = new float[b.length];
		for (int i = 0; i<a.length; i++)
			{
			real[i] = a.real[i]*b.real[i]-a.imag[i]*b.imag[i];
			imag[i] = a.real[i]*b.imag[i]+a.imag[i]*b.real[i];
			}

		return new FrivolousComplexArray(real, imag, a.width, a.height);
		}

	public static FrivolousComplexArray getRealMultiplication(FrivolousComplexArray a,
			FrivolousComplexArray b)
		{
		if (a.length!=b.length)
			throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];

		for (int i = 0; i<a.length; i++)
			real[i] = a.real[i]*b.real[i];

		return new FrivolousComplexArray(real, null, a.width, a.height);
		}

	public static FrivolousComplexArray getRealAddition(FrivolousComplexArray a, FrivolousComplexArray b)
		{
		if (a.length!=b.length)
			throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];

		for (int i = 0; i<a.length; i++)
			real[i] = a.real[i]+b.real[i];

		return new FrivolousComplexArray(real, null, a.width, a.height);
		}

	public static FrivolousComplexArray getRealSum(FrivolousComplexArray[] c)
		{
		float[] real = new float[c[0].length];
		for (int i = 0; i<c[0].length; i++)
			for (int j = 0; j<c.length; j++)
				real[i] += c[j].real[i];

		return new FrivolousComplexArray(real, null, c[0].width, c[0].height);
		}

	public static FrivolousComplexArray getRealDivision(FrivolousComplexArray a, FrivolousComplexArray b)
		{
		if (a.length!=b.length)
			throw new IllegalArgumentException("Matrix sizes must agree!");
		float[] real = new float[a.length];

		for (int i = 0; i<a.length; i++)
			real[i] = a.real[i]/b.real[i];

		return new FrivolousComplexArray(real, null, a.width, a.height);
		}

	public static float[] copyArray(float[] array)
		{
		float[] copy = new float[array.length];
		for (int i = 0; i<array.length; i++)
			copy[i] = array[i];
		return copy;
		}

	public static FrivolousComplexArray getFilledArray(FrivolousComplexArray sizeOf, float fillValue)
		{
		float[] real = new float[sizeOf.length];
		for (int i = 0; i<real.length; i++)
			real[i] = fillValue;
		return new FrivolousComplexArray(real, null, sizeOf.width, sizeOf.height);
		}

	public static FrivolousComplexArray getCrop(
		FrivolousComplexArray array, int width, int height, int offsetX, int offsetY) {
		if(width+offsetX > array.width || offsetX < 0 ||
		   height+offsetY > array.height || offsetY < 0)
			throw new IllegalArgumentException("Can not crop an area outside the original picture!");
		float[] real = new float[width*height];
		float[] imag = new float[width*height];
		for (int i = 0; i<width;i++)
			for (int j = 0; j<height;j++){
				real[i+j*width]=array.real[(i+offsetX)+(j+offsetY)*array.width];
				imag[i+j*width]=array.imag[(i+offsetX)+(j+offsetY)*array.width];
		}
		
		return new FrivolousComplexArray(real, imag, width, height);
	}

	}
