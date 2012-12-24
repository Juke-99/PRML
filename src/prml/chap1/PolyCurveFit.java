package prml.chap1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import prml.util.XYGraph;

import Jama.Matrix;

public class PolyCurveFit {

	ArrayList<double[]> training;

	public PolyCurveFit() {
		this.training = new ArrayList<double[]>();
	}

	public void addTrainingData(double[] data) {
		this.training.add(data);
	}

	public List<double[]> getTrainingDataValues() {
		return this.training;
	}

	public double[] getWeightsByMinSqrtErr(int m) {
		// �w�K�f�[�^:(x,t)={(x_1,t_1),...,(x_n,t_n)
		// �Ȑ��t�B�b�g�Fy(x,w) = w_0 + w_1 * x^1 + w_2 * x^2 ... = ��_j=0�`M w_j * x^j

		// Aw = T (A:m*m����, T:m*1����, w:m*1�����̏d��)
		// A_ij = ��_n=1�`N (x_n)^{i+j}
		// T_i = ��_n=1�`N (x_n)^i * t_n
		// w�d��

		//�s��A�𒼐ڌv�Z
		Matrix A = new Matrix(m + 1, m + 1);
		for (int i = 0; i < m + 1; i++) {
			for (int j = 0; j < m + 1; j++) {
				double val_Aij = 0;
				for (int n = 0; n < this.training.size(); n++) {
					val_Aij += Math.pow(this.training.get(n)[0], i + j);
				}
				A.set(i, j, val_Aij);
			}
		}
		
		// �s��T
		Matrix T = new Matrix(m + 1, 1);
		for (int i = 0; i < m + 1; i++) {
			double val_Ti = 0;
			for (int n = 0; n < this.training.size(); n++) {
				val_Ti += this.training.get(n)[1]
						* Math.pow(this.training.get(n)[0], i);
			}
			T.set(i, 0, val_Ti);
		}

		Matrix w = A.solve(T);

		// for debug
//		printMatrix(A);
//		printMatrix(T);
//		printMatrix(w);
//		Matrix Residual = A.times(w).minus(T);
//		printMatrix(Residual);

		return w.getColumnPackedCopy();

	}


	public double[] getWeightsByMinSqrtErrReg(int m, double r) {
		// �w�K�f�[�^:(x,t)={(x_1,t_1),...,(x_n,t_n)
		// �Ȑ��t�B�b�g�Fy(x,w) = w_0 + w_1 * x^1 + w_2 * x^2 ... = ��_j=0�`M w_j * x^j

		// (A+��I)w = T (A:m*m����, ��:�������p�W��, I:m*m�����P�ʍs��, T:m*1����, w:m*1�����̏d��)
		// A_ij = ��_n=1�`N (x_n)^{i+j}
		// T_i = ��_n=1�`N (x_n)^i * t_n
		// w�d��

		// �s��A
		Matrix A = new Matrix(m + 1, m + 1);
		for (int i = 0; i < m + 1; i++) {
			for (int j = 0; j < m + 1; j++) {
				double val_Aij = 0;
				for (int n = 0; n < this.training.size(); n++) {
					val_Aij += Math.pow(this.training.get(n)[0], i + j);
				}
				A.set(i, j, val_Aij);
			}
		}

		// �s���I
		Matrix rI = new Matrix(m + 1, m + 1);
		for (int i = 0; i < m + 1; i++) {
			for (int j = 0; j < m + 1; j++) {
				if (i == j)
					rI.set(i, j, r);
				else
					rI.set(i, j, 0.0);
			}
		}

		// �s��T
		Matrix T = new Matrix(m + 1, 1);
		for (int i = 0; i < m + 1; i++) {
			double val_Ti = 0;
			for (int n = 0; n < this.training.size(); n++) {
				val_Ti += this.training.get(n)[1]
						* Math.pow(this.training.get(n)[0], i);
			}
			T.set(i, 0, val_Ti);
		}

		Matrix A_rI = A.plus(rI);
		Matrix w = A_rI.solve(T);

		// for debug
		// printMatrix(A);
		// printMatrix(T);
		// printMatrix(w);
		// Matrix Residual = A.times(w).minus(T);
		// printMatrix(Residual);

		return w.getColumnPackedCopy();

	}

	private static void printMatrix(Matrix x) {
		for (int j = 0; j < x.getColumnDimension(); j++) {
			System.out.print("\t");
			System.out.print("[" + j + "]");
		}
		System.out.println();
		for (int i = 0; i < x.getRowDimension(); i++) {
			System.out.print("[" + i + "]");
			for (int j = 0; j < x.getColumnDimension(); j++) {
				System.out.print("\t");
				System.out.print(x.get(i, j));
			}
			System.out.println();
		}
		return;
	}

	public static void main(String args[]) throws IOException {
		// �w�K�f�[�^�t�@�C����
		String filename = args[0];
		// ����
		int m = Integer.parseInt(args[1]);
		// ln ��
		int ln_r = Integer.parseInt(args[2]);

		// �t�B�b�e�B���O�I�u�W�F�N�g
		PolyCurveFit pcFitEM = new PolyCurveFit();

		// �f�[�^�����[�h���Ċw�K�f�[�^�Ƃ��Ēǉ�
		BufferedReader br = new BufferedReader(new FileReader(
				new File(filename)));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String recStr[] = line.split(" ", 2);
				double[] rec = { Double.parseDouble(recStr[0]),
						Double.parseDouble(recStr[1]) };
				pcFitEM.addTrainingData(rec);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// �d�݌v�Z(���a�덷�ŏ�)
		double[] w_MinSqrtErr = pcFitEM.getWeightsByMinSqrtErr(m);
		System.out.println("----");
		for (int i = 0; i < w_MinSqrtErr.length; i++) {
			System.out.println(w_MinSqrtErr[i]);
		}

		// �d�݌v�Z(���a�덷+�������ŏ�)
		double r = Math.pow(Math.E, ln_r);
		double[] w_MinSqrtErrReg = pcFitEM.getWeightsByMinSqrtErrReg(m, r);
		System.out.println("----");
		for (int i = 0; i < w_MinSqrtErrReg.length; i++) {
			System.out.println(w_MinSqrtErrReg[i]);
		}
		
		//�O���t�`��p
		//�����f�[�^
		double[][] sineValues = makeSineValues();
		//�P���f�[�^
		double[][] trainValues = makeTrainValues(pcFitEM.getTrainingDataValues());
		//�w�K����(���a)
		double[][] resultValues = makeResultValues(w_MinSqrtErr, m);
		//�w�K����(���a)
		double[][] resultValuesReg = makeResultValues(w_MinSqrtErrReg, m);

		//�O���t�\��
		XYGraph xyGraph = new XYGraph("Fit Sine(m="+ String.valueOf(m)+")", "X", "Y");
		xyGraph.addDataValues("Sin(x)", sineValues, true);
		xyGraph.addDataValues("Training", trainValues, false);
		xyGraph.addDataValues("MinSqrtErr", resultValues, true);
		xyGraph.addDataValues("MinSqrtErrReg(ln rabmda = "+String.valueOf(ln_r)+")", resultValuesReg, true);
		xyGraph.rangeX(0.0, 1.0);
		xyGraph.rangeY(-1.0, 1.0);
		xyGraph.saveGraphAsPNG("sin.png", 500, 300); //view���\�b�h�̌�ɌĂяo���ƁA���삪���������̂Œ���
		xyGraph.view(700, 700);

		
	}

	private static double[][] makeSineValues() {
		double[][] ret = new double[2][101];
		//0-1��100�̃f�[�^�Ŗ��߂�
		for(int i=0; i<=100; i++){
			ret[0][i] = i/100.0; //X
			ret[1][i] = Math.sin(2.0 * Math.PI * ret[0][i]); //Y
		}
		return ret;
	}

	private static double[][] makeTrainValues(List<double[]> trainingDataValues) {
		double[][] ret = new double[2][trainingDataValues.size()];
		int i=0;
		for(double[] rec: trainingDataValues){
			ret[0][i] = rec[0]; //X
			ret[1][i] = rec[1]; //Y
			i++;
		}
		return ret;
	}

	private static double[][] makeResultValues(double[] w, int m) {
		double[][] ret = new double[2][101];
		//0-1��100�̃f�[�^�Ŗ��߂�
		for(int i=0; i<=100; i++){
			ret[0][i] = i/100.0; //X
			for(int j=0; j<=m; j++){ //Y
				ret[1][i] += w[j] * Math.pow(ret[0][i],j); 
			}
		}
		return ret;
	}


}
