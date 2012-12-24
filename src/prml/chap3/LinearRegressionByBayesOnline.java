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
 * ���`��A���f���F�x�C�Y-�I�����C���w�K�i���֐���I���j
 */
public class LinearRegressionByBayesOnline {
	int numM;
	BasisFunction func;
	double alpha;
	double beta;
	Matrix mN;
	Matrix SNInv;

	public LinearRegressionByBayesOnline(int numM, BasisFunction func, double alpha,
			double beta) {
		this.numM = numM;
		this.func = func;
		this.alpha = alpha;
		this.beta = beta;
		this.mN = new Matrix(numM, 1);
		this.SNInv = new Matrix(numM, numM);

		// ���O���z
		// m_0 �͑S��0
		for (int i = 0; i < numM; i++) {
			mN.set(i, 0, 0.0);
		}

		// S_0^-1 = �� * I
		Matrix I = new Matrix(numM, numM);
		for (int i = 0; i < numM; i++) {
			for (int j = 0; j < numM; j++) {
				if (i == j)
					I.set(i, j, 1.0);
				else
					I.set(i, j, 0.0);
			}
		}
		SNInv = I.times(alpha);
	}

	// �p�����[�^m���擾
	public double[] getParamM() {
		return mN.getColumnPackedCopy();
	}

	// �p�����[�^S���擾
	public double[][] getParamS() {
		return SNInv.inverse().getArrayCopy();
	}

	// �����w�K
	public void learnOnline(double x, double t) {
		// �w�K�f�[�^:(x,t)={(x_1,t_1),...,(x_n,t_n)
		// �Ȑ��t�B�b�g�Fy(x,w) = w_0 * ��_0(x) + w_1 * ��_1(x) + w_2 * ��_2(x) ...
		//                  = ��_j={0�`M-1} w_j * ��_j(x)

		// w���㕪�z�����w�K�F
		// p(w|t)=�K�E�X(w|m_N, S_N)
		// m_N+1 = S_N+1 ( S_N^-1 * m_N + �� * ��_N+1 * t_N+1 )
		// S_N+1^-1 = S_N^-1 + �� * ��_N+1 * ��_N+1^T )
		// (m:M����(w�̃K�E�X���z����), S:M*M����(�K�E�X���z�̕��U))
		// (��_N:M*1����, t_N:�X�J���[)
		// ��_N+1{j} = ��_j (x_N+1)

		// �v��s�� �� �̊w�K�Ώۃf�[�^�̂� ��_N+1
		Matrix PHIN1 = new Matrix(this.numM, 1);
		for (int j = 0; j < numM; j++) {
			double val_PNj = func.phi(j, x);
			PHIN1.set(j, 0, val_PNj);
		}

		// ���㕪�z���X�V
		Matrix SN1Inv = this.SNInv.plus(PHIN1.times(PHIN1.transpose()).times(this.beta));
		Matrix mN1 = SN1Inv.inverse().times(
				this.SNInv.times(this.mN).plus(
						PHIN1.times(t).times(this.beta)));

		this.SNInv = SN1Inv;
		this.mN = mN1;
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

		// ���֐��I��
		BasisFunction func = null;
		if (BasisFuncName.equals("GAUSIAN")) {
			func = new GausianBasisFunction(m, 0.0, 1.0);
		} else if (BasisFuncName.equals("POLY")) {
			func = new PolynomialBasisFunction();
		}

		// �t�B�b�e�B���O�I�u�W�F�N�g
		LinearRegressionByBayesOnline lrBayesOnLine = new LinearRegressionByBayesOnline(m, func,
				alpha, beta);

		// �f�[�^�����[�h���Ċw�K�f�[�^�Ƃ��Ēǉ����Ȃ���w�K
		BufferedReader br = new BufferedReader(new FileReader(
				new File(filename)));
		String line;
		List<double[]> trainData = new ArrayList<double[]>(); // �O���t�`��p�Ƀf�[�^��ێ�
		List<double[]> w_Avgs = new ArrayList<double[]>(); //�e�w�K���ʂ̏d��w��ێ�
		try {
			while ((line = br.readLine()) != null) {
				String recStr[] = line.split(" ", 2);
				double[] rec = { Double.parseDouble(recStr[0]),
						Double.parseDouble(recStr[1]) };
				trainData.add(rec);
				lrBayesOnLine.learnOnline(rec[0], rec[1]); //�w�K
				w_Avgs.add(lrBayesOnLine.getParamM()); //w�̎��㕪�z�̕���
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// �O���t�`��p
		// �����f�[�^
		double[][] sineValues = makeSineValues();
		// �P���f�[�^
		double[][] trainValues = makeTrainValues(trainData);
		// �w�K����
		List<double[][]> resultValues = new ArrayList<double[][]>();
		for(int i=0; i<w_Avgs.size(); i++){
			resultValues.add(makeResultValues(w_Avgs.get(i), m, func));
		}
		// �O���t�\��
		XYGraph xyGraph = new XYGraph("Fit Sine(m=" + String.valueOf(m) + ")",
				"X", "Y");
		xyGraph.addDataValues("Sin(x)", sineValues, true);
		xyGraph.addDataValues("Training", trainValues, false);
		for(int i=0; i<resultValues.size(); i++){
			xyGraph.addDataValues(
					String.format("BayesFit-%d(alpha=%f, beta=%f)", i, alpha, beta),
					resultValues.get(i), true);
		}
		xyGraph.rangeX(0.0, 1.0);
		xyGraph.rangeY(-1.0, 1.0);
		xyGraph.saveGraphAsPNG("sin-bayes-online.png", 500, 700); // view���\�b�h�̌�ɌĂяo���ƁA���삪���������̂Œ���
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

	private static double[][] makeResultValues(double[] w, int m,
			BasisFunction func) {
		double[][] ret = new double[2][101];
		// 0-1��100�̃f�[�^�Ŗ��߂�
		for (int i = 0; i <= 100; i++) {
			ret[0][i] = i / 100.0; // X
			for (int j = 0; j < m; j++) { // Y
				ret[1][i] += w[j] * func.phi(j, ret[0][i]);
			}
		}
		return ret;
	}
}
