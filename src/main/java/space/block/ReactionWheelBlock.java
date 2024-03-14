package space.block;

public class ReactionWheelBlock extends SimpleFacingBlock
{
	private final float torque; 
	
	public ReactionWheelBlock(Settings settings, float torque)
	{
		super(settings);
		this.torque = torque;
	}
	
	public float getTorque()
	{
		return torque;
	}
}