package prml.util;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

public class XYGraph {

	private JFreeChart chart;
	
	public XYGraph(String graphName, String labelX, String labelY){
		this.chart = ChartFactory.createXYLineChart(
				graphName, // The chart title
				labelX, // x axis label
				labelY, // y axis label
				null, // The dataset for the chart
				PlotOrientation.VERTICAL, true, // Is a legend required?
				false, // Use tooltips
				false // Configure chart to generate URLs?
				);
	}

	//�f�[�^��ǉ��idouble[0]=X���W���X�g�Adouble[1]=Y���W���X�g�j�B�����\�����邩�ۂ�
	public void addDataValues(String name, double[][] dataValuses, boolean line){
		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries(name, dataValuses);
		
		XYPlot plot = this.chart.getXYPlot();

		int datasetCnt = plot.getDatasetCount();
		plot.setDataset(datasetCnt, dataset);

		XYLineAndShapeRenderer renderer;
		if(line){
			renderer = new XYLineAndShapeRenderer(
				true, // ����\�����Ȃ�
				false // �_��\������
				);
		}else{
			renderer = new XYLineAndShapeRenderer(
				false, // ����\�����Ȃ�
				true // �_��\������
				);
		}
		
		plot.setRenderer(datasetCnt, renderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
	}

	//X���W�̕\�����
	public void rangeX(double min, double max){
		ValueAxis xAxis = this.chart.getXYPlot().getDomainAxis();
		xAxis.setAutoRange(false);
		xAxis.setRange(min, max);
	}

	//Y���W�̕\�����
	public void rangeY(double min, double max){
		ValueAxis yAxis = this.chart.getXYPlot().getRangeAxis();
		yAxis.setAutoRange(false);
		yAxis.setRange(min, max);
	}
	
	//�\���i��ʃT�C�Y���w��j
	public void view(int sizeX, int sizeY){
		String graphName = this.chart.getTitle().getText();
		JFrame frame = new JFrame(graphName);
		frame.getContentPane().add(new ChartPanel(this.chart));
		frame.setSize(sizeX, sizeY);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	//�ۑ��i�t�@�C�����Ɖ�ʃT�C�Y���w��j
	public void saveGraphAsPNG(String filename, int sizeX, int sizeY) throws IOException {
		File imageFile = new File(filename);
		ChartUtilities.saveChartAsPNG(imageFile, this.chart, sizeX, sizeY);
	}

	//�e�X�g�p���C��
	public static void main(String[] args) throws IOException{
		//�T���v���f�[�^(sin��cos��y=x-1)
		final int NUM_VALUES = 60;
		double[][] sineValues = new double[2][NUM_VALUES];
		double[][] cosineValues = new double[2][NUM_VALUES];
		// X values
		for (int i = 0; i < NUM_VALUES; i++) {
			sineValues[0][i] = i / 10.0;
			cosineValues[0][i] = i / 10.0;
		}
		// Y values
		for (int i = 0; i < NUM_VALUES; i++) {
			sineValues[1][i] = Math.sin(sineValues[0][i]);
			cosineValues[1][i] = Math.cos(cosineValues[0][i]);
		}
		//�T���v���f�[�^(y=a*x-1)
		double[][] scatterplotValues = new double[2][10];
		for (int i = 0; i < 10; i++) {
			scatterplotValues[0][i] = (double) i; // x
			scatterplotValues[1][i] = (double) i * (0.25 / 2.0) - 1.0; // y
		}

		//�\���e�X�g
		XYGraph xyGraph = new XYGraph("Sine / Cosine Curves", "X", "Y");
		xyGraph.addDataValues("Sin(x)", sineValues, true);
		xyGraph.addDataValues("Cos(x)", cosineValues, true);
		xyGraph.addDataValues("y=a*x-1", scatterplotValues, false);
		xyGraph.saveGraphAsPNG("sin-cos.png", 500, 300); //��ɕۑ����Ȃ��ƁA�O���t�����������Ȃ�
		xyGraph.view(700, 700);
	}

}
