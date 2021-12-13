package com.inaoe.rna.view;


import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

import javax.swing.*;
import java.awt.*;


public class SineGraph extends JFrame {

    private static final long serialVersionUID = 1L;

    public SineGraph(double[] x)  {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        DataTable data = new DataTable(Integer.class, Double.class);
        
        for (int i = 0; i < x.length; i ++) {
            data.add(i+1, x[i]  );
        }
        Shape newShape = new Rectangle.Double(-1, -1, 2, 2);
        XYPlot plot = new XYPlot(data);
        getContentPane().add(new InteractivePanel(plot));
        
        LineRenderer lines = new DefaultLineRenderer2D();
        BasicStroke bs = new BasicStroke(2);
        lines.setStroke(bs);
        plot.setLineRenderers(data, lines);
        plot.getPointRenderers(data).get(0).setShape(newShape);
        
        Color color = Color.blue;
        plot.getPointRenderers(data).get(0).setColor(color);
        plot.getLineRenderers(data).get(0).setColor(color);
    }
}
