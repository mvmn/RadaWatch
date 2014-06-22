package x.mvmn.radawatch.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class SwingHelper {

	public static void moveToScreenCenter(Component component) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension componentSize = component.getSize();
		int newComponentX = screenSize.width - componentSize.width;
		if (newComponentX >= 0)
			newComponentX = newComponentX / 2;
		else
			newComponentX = 0;
		int newComponentY = screenSize.height - componentSize.height;
		if (newComponentY >= 0)
			newComponentY = newComponentY / 2;
		else
			newComponentY = 0;
		component.setLocation(newComponentX, newComponentY);
	}

	public static void resizeToScreenProportions(Component component, double xProportion, double yProportion) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension componentNewSize = new Dimension((int) (screenSize.width * yProportion), (int) (screenSize.height * xProportion));
		component.setSize(componentNewSize);
	}

	public static void resizeToScreenProportions(Component component, double proportion) {
		resizeToScreenProportions(component, proportion, proportion);
	}

	public static JFrame enframeComponent(Component component, String title) {
		JFrame result;
		if (title != null)
			result = new JFrame(title);
		else
			result = new JFrame();
		result.getContentPane().add(component);
		result.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		result.pack();
		resizeToScreenProportions(result, 0.7);
		moveToScreenCenter(result);
		return result;
	}

	public static JFrame enframeComponent(Component component) {
		JFrame frame;
		if (component instanceof Titled) {
			frame = enframeComponent(component, ((Titled) component).getTitle());
		} else {
			frame = enframeComponent(component, null);
		}
		return frame;
	}
}
