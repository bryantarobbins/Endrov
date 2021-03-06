/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous;

public class FrivolousSettings
	{
	public static final double UPPER_LIMIT_LAMBDA = 150;
	public static final double LOWER_LIMIT_LAMBDA = 0;

	/*
	 * Parameters for distorting the image, the user should be able to change
	 * these. (direct or indirect?)
	 */
	public double parameter_noise_lambda = 50;

	public boolean setNoiseLambda(String s) throws NumberFormatException,	IllegalValueException
		{
		return setNoiseLambda(Double.parseDouble(s));
		}

	public boolean setNoiseLambda(double d) throws IllegalValueException
		{
		if (LOWER_LIMIT_LAMBDA>d||UPPER_LIMIT_LAMBDA<d)
			throw new IllegalValueException();
		if (d==parameter_noise_lambda)
			return false;
		parameter_noise_lambda = d;
		return true;
		}

	
	
	
	public class IllegalValueException extends Exception
		{
		private static final long serialVersionUID = 7060340369335670722L;

		public IllegalValueException(String message)
			{
			super(message);
			}

		public IllegalValueException()
			{
			super();
			}
		}
	}
