package space.util;

public class BooleanByteUtil
{
	public static byte toByte(boolean[] array)
	{
		byte result = 0;

		for(int i = 0; i < array.length; i++)
		{
			if(array[i])
				result += (1 << i);
		}

		return result;
	}

	public static boolean[] toBooleanArray(byte b)
	{
		boolean[] result = new boolean[8];
		
		for(int i = 0; i < 8; i++)
		    result[i] = (b & (1 << i)) != 0;
		
		return result;
	}
}