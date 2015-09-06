package x.mvmn.radawatch.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class SwingHelper {

	public static void moveToScreenCenter(final Component component) {
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

	public static void resizeToScreenProportions(final Component component, final double xProportion, final double yProportion) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension componentNewSize = new Dimension((int) (screenSize.width * yProportion), (int) (screenSize.height * xProportion));
		component.setSize(componentNewSize);
	}

	public static void resizeToScreenProportions(final Component component, final double proportion) {
		resizeToScreenProportions(component, proportion, proportion);
	}

	public static JFrame enframeComponent(final Component component, final String title) {
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

	public static JFrame enframeComponent(final Component component) {
		JFrame frame;
		if (component instanceof Titled) {
			frame = enframeComponent(component, ((Titled) component).getTitle());
		} else {
			frame = enframeComponent(component, null);
		}
		return frame;
	}

	public static void reportError(final boolean calledOffSwingEventDispatchThread, final Component parentWindow, final Throwable ex) {
		if (calledOffSwingEventDispatchThread) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					reportError(parentWindow, ex);
				}
			});
		} else {
			reportError(parentWindow, ex);
		}
	}

	protected static void reportError(final Component parentWindow, final Throwable ex) {
		JOptionPane.showMessageDialog(parentWindow, ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred", JOptionPane.ERROR_MESSAGE);
	}

}
