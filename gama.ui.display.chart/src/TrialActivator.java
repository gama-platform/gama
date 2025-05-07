import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TrialActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("[DEBUG] Load gama.ui.display.chart based on Jzy3d ====");
		System.out.println("[DEBUG] Remember to remove testing hacks in OpenGamaWebsiteHandler & LayeredDisplayDecorator once well implemented ====");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
