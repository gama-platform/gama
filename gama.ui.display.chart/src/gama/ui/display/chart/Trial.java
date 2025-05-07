package gama.ui.display.chart;
import org.eclipse.swt.widgets.Composite;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.SWTChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.SurfaceBuilder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import gama.ui.experiment.views.displays.LayeredDisplayView;

public class Trial extends LayeredDisplayView {

	@Override
	public boolean isCameraLocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCameraDynamic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is2D() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPartName() {
		// TODO Auto-generated method stub
		return "Trial";
	}

	@Override
	protected boolean shouldBeClosedWhenNoExperiments() {
		// TODO Auto-generated method stub
		return false;//super.shouldBeClosedWhenNoExperiments();
	}

	@Override
	protected boolean needsOutput() {
		// TODO Auto-generated method stub
		return false;//super.needsOutput();
	}

	@Override
	public void ownCreatePartControl(Composite c) {
		// TODO Auto-generated method stub
		//super.ownCreatePartControl(c);
		

	    Mapper mapper = new Mapper() {
	      @Override
	      public double f(double x, double y) {
	        return x * Math.sin(x * y);
	      }
	    };
	
    // Define range and precision for the function to plot
    Range range = new Range(-3, 3);
    int steps = 80;
	
    final Shape surface =
            new SurfaceBuilder().orthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(),
            surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

    SWTChartFactory f = new SWTChartFactory(c);
    
    Quality q = Quality.Advanced();
    q.setHiDPIEnabled(true);
    Chart chart = f.newChart(q);
    

    
    // Chart chart = SWTChartFactory.chart(shell);
    chart.getScene().getGraph().add(surface);

    ChartLauncher.openChart(chart);

//
//    shell.setText("name");
//    shell.setSize(800, 600);
//    shell.open();
//
//    while (!shell.isDisposed()) {
//      if (!display.readAndDispatch()) {
//        display.sleep();
//      }
//    }
//    chart.stopAnimation();
//    display.dispose();
    
	// TODO Auto-generated method stub
	//return null;
    //return f.getComposite();
	}

	@Override
	protected Composite createSurfaceComposite(Composite parent) {
		return null;
	}


}
