/**
 *
 */
package gama.ui.experiment.commands;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import jakarta.inject.Inject;

/**
 *
 */
public class GamaStackRenderer extends StackRenderer {

    @Inject private IPresentationEngine renderer;

    @Override
    protected boolean requiresFocus(MPart element) {
	// TODO Auto-generated method stub
	return super.requiresFocus(element);
    }

    @Override
    protected void updateTab(CTabItem cti, MPart part, String attName, Object newValue) {
	// TODO Auto-generated method stub
	super.updateTab(cti, part, attName, newValue);
    }

    @Override
    public Object createWidget(MUIElement element, Object parent) {
	// TODO Auto-generated method stub
	return super.createWidget(element, parent);
    }

    @Override
    public void adjustTopRight(CTabFolder tabFolder) {
	// TODO Auto-generated method stub
	super.adjustTopRight(tabFolder);
    }

    @Override
    protected void createTab(MElementContainer<MUIElement> stack, MUIElement element) {
	// TODO Auto-generated method stub
	super.createTab(stack, element);
    }

    @Override
    public void childRendered(MElementContainer<MUIElement> parentElement, MUIElement element) {
	// TODO Auto-generated method stub
	super.childRendered(parentElement, element);
    }

    @Override
    public CTabItem findItemForPart(MPart part) {
	// TODO Auto-generated method stub
	return super.findItemForPart(part);
    }

    @Override
    public void hideChild(MElementContainer<MUIElement> parentElement, MUIElement child) {
	// TODO Auto-generated method stub
	super.hideChild(parentElement, child);
    }

    @Override
    public void hookControllerLogic(MUIElement me) {
	// TODO Auto-generated method stub
	super.hookControllerLogic(me);
    }

    @Override
    public void showAvailableItems(MElementContainer<?> stack, CTabFolder tabFolder) {
	// TODO Auto-generated method stub
	super.showAvailableItems(stack, tabFolder);
    }

    @Override
    public void showAvailableItems(MElementContainer<?> stack, CTabFolder tabFolder, boolean forceCenter) {
	// TODO Auto-generated method stub
	super.showAvailableItems(stack, tabFolder, forceCenter);
    }

    @Override
    protected void showTab(MUIElement element) {

	super.showTab(element);

    }

    @Override
    protected void showMenu(ToolItem item) {
	// TODO Auto-generated method stub
	super.showMenu(item);
    }

    @Override
    protected boolean isClosable(MPart part) {
	// TODO Auto-generated method stub
	return super.isClosable(part);
    }

    @Override
    protected boolean isDetachable(MPart part) {
	// TODO Auto-generated method stub
	return super.isDetachable(part);
    }

    @Override
    protected boolean imageChanged() {
	// TODO Auto-generated method stub
	return super.imageChanged();
    }

    @Override
    public void postProcess(MUIElement element) {
	// TODO Auto-generated method stub
	super.postProcess(element);
    }

    @Override
    public void processContents(MElementContainer<MUIElement> me) {
	// TODO Auto-generated method stub
	super.processContents(me);
    }

    @Override
    public void styleElement(MUIElement element, boolean active) {
	// TODO Auto-generated method stub
	super.styleElement(element, active);
    }

    @Override
    public void setCSSInfo(MUIElement me, Object widget) {
	// TODO Auto-generated method stub
	super.setCSSInfo(me, widget);
    }

    @Override
    protected void reapplyStyles(Widget widget) {
	// TODO Auto-generated method stub
	super.reapplyStyles(widget);
    }

    @Override
    public void bindWidget(MUIElement me, Object widget) {
	// TODO Auto-generated method stub
	super.bindWidget(me, widget);
    }

    @Override
    protected Widget getParentWidget(MUIElement element) {
	// TODO Auto-generated method stub
	return super.getParentWidget(element);
    }

    @Override
    public void disposeWidget(MUIElement element) {
	// TODO Auto-generated method stub
	super.disposeWidget(element);
    }

    @Override
    public String getToolTip(MUILabel element) {
	// TODO Auto-generated method stub
	return super.getToolTip(element);
    }

    @Override
    protected Image getImageFromURI(String iconURI) {
	// TODO Auto-generated method stub
	return super.getImageFromURI(iconURI);
    }

    @Override
    public Image getImage(MUILabel element) {
	// TODO Auto-generated method stub
	return super.getImage(element);
    }

    @Override
    protected int calcVisibleIndex(MUIElement element) {
	// TODO Auto-generated method stub
	return super.calcVisibleIndex(element);
    }

    @Override
    protected int calcIndex(MUIElement element) {
	// TODO Auto-generated method stub
	return super.calcIndex(element);
    }

    @Override
    public void forceFocus(MUIElement element) {
	// TODO Auto-generated method stub
	super.forceFocus(element);
    }

    @Override
    protected IEclipseContext getContextForParent(MUIElement element) {
	// TODO Auto-generated method stub
	return super.getContextForParent(element);
    }

    @Override
    protected IEclipseContext getContext(MUIElement part) {
	// TODO Auto-generated method stub
	return super.getContext(part);
    }

    @Override
    public void activate(MPart element) {
	// TODO Auto-generated method stub
	super.activate(element);
    }

    @Override
    public void removeGui(MUIElement element, Object widget) {
	// TODO Auto-generated method stub
	super.removeGui(element, widget);
    }

    @Override
    public Object getUIContainer(MUIElement element) {
	// TODO Auto-generated method stub
	return super.getUIContainer(element);
    }

    @Override
    public int getStyleOverride(MUIElement mElement) {
	// TODO Auto-generated method stub
	return super.getStyleOverride(mElement);
    }

    @Override
    protected void populateTabMenu(Menu menu, MPart part) {
	super.populateTabMenu(menu, part);
    }
}