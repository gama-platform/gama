package gama.ui.experiment.commands;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory;

public class GamaStackRendererFactory extends WorkbenchRendererFactory {
    private GamaStackRenderer myStackRenderer;

    @Override
    public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent) {
	if (uiElement instanceof MPartStack) {
	    if (myStackRenderer == null) {
		myStackRenderer = new GamaStackRenderer();
		initRenderer(myStackRenderer);
	    }

	    return myStackRenderer;
	}

	return super.getRenderer(uiElement, parent);
    }
}