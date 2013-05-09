package coordinates;

import java.util.EventListener;

public interface LocalisationListener extends EventListener{
	public void newPosition(int oldPosition[], int newPosition[]);
}
