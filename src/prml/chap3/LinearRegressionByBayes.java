package prml.chap3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import prml.util.XYGraph;

import Jama.Matrix;

/**
 * ���`��A���f���F�x�C�Y�i���֐���I���j
 */
public class LinearRegressionByBayes {
	int numM;
	BasisFunction func;
	double alpha;
	double beta;
	List<double[]> training;
	Matrix mN;
	Matrix SNInv;
	Matrix PHI;
	
	private LinearRegressionByBayes() {}

	public LinearRegressionByBayes(int numM, BasisFunction func, double alpha,
			double beta, List<double[]> training) {
		this.numM = numM;
		this.func = func;
		this.alpha = alpha;
		this.beta = beta;
		this.training = training;

		this.mN = null;
		this.SNInv = null;
		this.PHI = null;
	}

	// w�̎��㕪�z�̃p�����[�^m���擾
	public double[] getParamM() {
		return this.mN.getColumnPackedCopy();
	}

	// w�̎��㕪�z�̃p�����[�^S���擾
	public double[][] getParamS() {
		return this.SNInv.inverse().getArrayCopy();
	}

	/**
	 * �x�C�Y�ɂ����`��A�B�d��w�̎��㕪�z�𐄒�B
	 */
	public void learn() {
		// �w�K�f�[�^:(x,t)={(x_1,t_1),...,(x_n,t_n)
		// �Ȑ��t�B�b�g�Fy(x,w) = w_0 * ��_0(x) + w_1 * ��_1(x) + w_2 * ��_2(x) ...
		//                  = ��_j={0�`M-1} w_j * ��_j(x)

		// w���O���z:
		// p(w)=�K�E�X(w|m_0, S_0)
		// m_0 = 0, S_0 = �� * I
		// w���㕪�z�F
		// p(w|t)=�K�E�X(w|m_N, S_N)
		// m_N = S_N ( S_0^-1 * m_0 + �� * ��^T * t )
		//     = �� * S_N * ��^T * t // m_0���S��0�̏ꍇ
		// S_N = S_0 + �� * ��^T * ��
		// (m:M����(w�̃K�E�X���z�̕���), S:M*M����(w�̃K�E�X���z�̕��U))
		// (��:N*M����, t:N*1����, w:M*�d��)
		// ��_{i,j} = ��_j(x_i)

		// �v��s�񃳂��v�Z
		if(this.PHI == null){ //�w�K�f�[�^���ύX���ꂽ�Ƃ������Čv�Z
			this.PHI = new Matrix(training.size(), this.numM);
			for (int i = 0; i < training.size(); i++) {
				for (int j = 0; j < this.numM; j++) {
					double val_Pij = this.func.phi(j, training.get(i)[0]);
					PHI.set(i, j, val_Pij);
				}
			}
		}

		// �ڕW�ϐ��̌P���f�[�^�x�N�g��t
		Matrix t = new Matrix(training.size(), 1);
		for (int i = 0; i < training.size(); i++) {
			double val_ti = training.get(i)[1];
			t.set(i, 0, val_ti);
		}

		// w�̎��O���z
		// w�̎��O���z�̕���m_0
		// Matrix m0 = new Matrix(numM,1); //�S��0
		// w�̎��O���z�̕��U�̋t�s�� S_0^-1
		Matrix I = new Matrix(this.numM, this.numM);
		for (int i = 0; i < this.numM; i++) {
			for (int j = 0; j < this.numM; j++) {
				if (i == j)
					I.set(i, j, 1.0);
			}
		}
		Matrix S0Inv = I.times(this.alpha);

		// ���㕪�z���v�Z
		this.SNInv = S0Inv.plus(PHI.transpose().times(PHI).times(this.beta));
		this.mN = this.SNInv.inverse().times(PHI.transpose()).times(t).times(this.beta);
	}
	
	//y(x)�̗\�����z�̕���
	public double getPredictiveDistMean(double x){
		// Mean = m_N^T * ��(x)

		//phi(x)
		Matrix phi = new Matrix(this.numM, 1);
		for (int j = 0; j < this.numM; j++) {
			double val_Pj = this.func.phi(j, x);
			phi.set(j, 0, val_Pj);
		}
		
		Matrix y = this.mN.transpose().times(phi);
		
		return y.get(0, 0);
	}

	//y(x)�̗\�����z�̕��U�̓��
	public double getPredictiveDistVar(double x){
		// Var^2 = 1/beta + ��(x)^T * S_N * ��(x)

		//phi(x) 
		Matrix phi = new Matrix(this.numM, 1);
		for (int j = 0; j < this.numM; j++) {
			double val_Pj = this.func.phi(j, x);
			phi.set(j, 0, val_Pj);
		}
		
		Matrix y_Var = phi.transpose().times(this.SNInv.inverse()).times(phi);
		
		return 1.0 / this.beta + y_Var.get(0, 0);
	}

	
	public static void main(String args[]) throws IOException {
		// �w�K�f�[�^�t�@�C����
		String filename = args[0];
		// ���f���̃p�����[�^��
		int m = Integer.parseInt(args[1]);
		// ���֐�
		String BasisFuncName = args[2];
		// alpha, beta
		double alpha = Double.parseDouble(args[3]);
		double beta = Double.parseDouble(args[4]);

		// �f�[�^�����[�h
		BufferedReader br = new BufferedReader(new FileReader(
				new File(filename)));
		String line;
		List<double[]> trainingData = new ArrayList<double[]>();
		try {
			while ((line = br.readLine()) != null) {
				String recStr[] = line.split(" ", 2);
				double[] rec = { Double.parseDouble(recStr[0]),
						Double.parseDouble(recStr[1]) };
				trainingData.add(rec);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ���֐��I��
		BasisFunction func = null;
		if (BasisFuncName.equals("GAUSIAN")) {
			func = new GausianBasisFunction(m, 0.0, 1.0);
		} else if (BasisFuncName.equals("POLY")) {
			func = new PolynomialBasisFunction();
		}

		// ���`��A�x�C�Y
		LinearRegressionByBayes lrBayes = new LinearRegressionByBayes(m, func,
				alpha, beta, trainingData);

		// w�̎��㕪�z�v�Z(�x�C�Y)
		lrBayes.learn();

		// w�̎��㕪�z�̕���m
		double[] w_Avg = lrBayes.getParamM();
		System.out.println("----");
		for (int i = 0; i < w_Avg.length; i++) {
			System.out.println(String.format("m[%d]\t%f", i, w_Avg[i]));
		}

		// w�̎��㕪�z�̕��US
		double[][] w_Var = lrBayes.getParamS();
		System.out.println("----");
		for (int i = 0; i < w_Var.length; i++) {
			System.out.print(String.format("s[%d]", i));
			for (int j = 0; j < w_Var[0].length; j++) {
				System.out.print(String.format("\t%f", w_Var[i][j]));
			}
			System.out.println();
		}

		// �O���t�`��p
		// �����f�[�^
		double[][] sineValues = makeSineValues();
		// �P���f�[�^
		double[][] trainValues = makeTrainValues(trainingData);
		// �w�K����
		double[][] resultValues = makeResultValues(lrBayes);
		// ���U
		double[][] resultPlusSigma = makeSigmaValues(resultValues, lrBayes, 1);
		// ���U
		double[][] resultMinusSigma = makeSigmaValues(resultValues, lrBayes, -1);
		
		// �O���t�\��
		XYGraph xyGraph = new XYGraph(
				String.format("Fit Sine By Bayes(m=%d, %s, alpha=%.03f, beta=%.03f)", m, BasisFuncName, alpha, beta), "X", "Y");
		xyGraph.addDataValues("Sin(x)", sineValues, true);
		xyGraph.addDataValues("Training", trainValues, false);
		xyGraph.addDataValues("BayesFit", resultValues, true);
		xyGraph.addDataValues("BayesFit+sigma", resultPlusSigma, true);
		xyGraph.addDataValues("BayesFit-sigma", resultMinusSigma, true);
		xyGraph.rangeX(0.0, 1.0);
		xyGraph.rangeY(-1.0, 1.0);
		xyGraph.saveGraphAsPNG("sin.png", 500, 300); // view���\�b�h�̌�ɌĂяo���ƁA���삪���������̂Œ���
		xyGraph.view(700, 700);
	}

	private static double[][] makeSineValues() {
		double[][] ret = new double[2][101];
		// 0-1��100�̃f�[�^�Ŗ��߂�
		for (int i = 0; i <= 100; i++) {
			ret[0][i] = i / 100.0; // X
			ret[1][i] = Math.sin(2.0 * Math.PI * ret[0][i]); // Y
		}
		return ret;
	}

	private static double[][] makeTrainValues(List<double[]> trainingDataValues) {
		double[][] ret = new double[2][trainingDataValues.size()];
		int i = 0;
		for (double[] rec : trainingDataValues) {
			ret[0][i] = rec[0]; // X
			ret[1][i] = rec[1]; // Y
			i++;
		}
		return ret;
	}

	private static double[][] makeResultValues(LinearRegressionByBayes lrBayes) {
		double[][] ret = new double[2][101];
		// 0-1��100�̃f�[�^�Ŗ��߂�
		for (int i = 0; i <= 100; i++) {
			ret[0][i] = i / 100.0; // X
			ret[1][i] = lrBayes.getPredictiveDistMean(ret[0][i]); //Y
		}
		return ret;
	}

	private static double[][] makeSigmaValues(double[][] result, LinearRegressionByBayes lrBayes, double numS) {
		double[][] ret = new double[2][result[0].length];

		for (int i = 0; i < result[0].length; i++) {
			ret[0][i] = result[0][i]; // X
			ret[1][i] = result[1][i] + numS * Math.sqrt(lrBayes.getPredictiveDistVar(ret[0][i])); //result + numS * sigma
		}
		return ret;
	}


}
