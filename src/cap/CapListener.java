package cap;

//Listeners for the Cap
public interface CapListener{
	//listener - listen to the cap modifications
	public abstract void hasChanged(float cap, float oldCap, float pitch, float roll);
}