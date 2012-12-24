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
 * ���`��A���f���i���֐��Łj
 */
public class LinearRegression {
	int numM;
	BasisFunction func;
	
	public LinearRegression(int numM, BasisFunction func) {
		this.numM = numM;
		this.func = func;
	}

/**
 * ���덷�ɂ����`��A
 *
 * �w�K�f�[�^:(x,t)={(x_1,t_1),...,(x_n,t_n)
 * �Ȑ��t�B�b�g�Fy(x,w) = w_0 * ��_0(x) + w_1 * ��_1(x) + w_2 * ��_2(x) ... 
 *               = ��_j={0�`M-1} w_j * ��_j(x)
 * ��^T * �� * w = ��^T�@* t (��:N*M����, t:N*1����, w:M*1����)
 * ��_{i,j} = ��_j(x_i)
 */
	public double[] getWeightsByMinSqrtErr(List<double[]> training) {
		//�v��s�񃳂��v�Z
		Matrix PHI = new Matrix(training.size(), numM);
		for (int i = 0; i < training.size(); i++) {
			for (int j = 0; j < numM; j++) {
				double val_Pij = func.phi(j, training.get(i)[0]);
				PHI.set(i, j, val_Pij);
			}
		}
		
		//�ړI�ϐ��̌P���f�[�^�x�N�g��t
		Matrix t = new Matrix(training.size(), 1);
		for (int i = 0; i < training.size(); i++) {
			double val_Ti = training.get(i)[1];
			t.set(i, 0, val_Ti);
		}

		//�d�݃x�N�g��w
		Matrix w = PHI.transpose().times(PHI).inverse().times(PHI.transpose()).times(t);
				
		return w.getColumnPackedCopy();

	}

/**
 * ���덷�Ɛ������ɂ����`��A
�@* �w�K�f�[�^:(x,t)={(x_1,t_1),...,(x_n,t_n)
 * �Ȑ��t�B�b�g�Fy(x,w) = w_0 * ��_0(x) + w_1 * ��_1(x) + w_2 * ��_2(x) ... 
 *                 = ��_j={0�`M-1} w_j * ��_j(x)
 *
 * ( �� * I - ��^T * �� ) * w = ��^T�@* t (��:N*M����, t:N*1����, w:M*1����, �ɁF�������W��)
 * ��_{i,j} = ��_j(x_i)
 *
 * @param training
 * @param r �������W��
 * @return �d��
 */
	public double[] getWeightsByMinSqrtErrReg(List<double[]> training, double r) {
		//�v��s�񃳂��v�Z
		Matrix PHI = new Matrix(training.size(), numM);
		for (int i = 0; i < training.size(); i++) {
			for (int j = 0; j < numM; j++) {
				double val_Pij = func.phi(j, training.get(i)[0]);
				PHI.set(i, j, val_Pij);
			}
		}
		
		//�ړI�ϐ��̌P���f�[�^�x�N�g��t
		Matrix t = new Matrix(training.size(), 1);
		for (int i = 0; i < training.size(); i++) {
			double val_Ti = training.get(i)[1];
			t.set(i, 0, val_Ti);
		}

		// �s���I
		Matrix rI = new Matrix(numM, numM);
		for (int i = 0; i < numM; i++) {
			for (int j = 0; j < numM; j++) {
				if (i == j)
					rI.set(i, j, r);
				else
					rI.set(i, j, 0.0);
			}
		}

		//�d�݃x�N�g��w
		Matrix w = (rI.plus(PHI.transpose().times(PHI))).inverse().times(PHI.transpose()).times(t);
				
		return w.getColumnPackedCopy();
	}

	public static void main(String args[]) throws IOException {
		// �w�K�f�[�^�t�@�C����
		String filename = args[0];
		// ���f���̃p�����[�^��
		int m = Integer.parseInt(args[1]);
		//�@���֐�
		String BasisFuncName = args[2];
		// ln ��
		int ln_r = Integer.parseInt(args[3]);

		//���֐��I��
		BasisFunction func = null;
		if(BasisFuncName.equals("GAUSIAN")){
			func = new GausianBasisFunction(m, 0.0, 1.0);
		} else if(BasisFuncName.equals("POLY")){
			func = new PolynomialBasisFunction();
		}

		// ���`��A�I�u�W�F�N�g
		LinearRegression pcFitEM = new LinearRegression(m, func);

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

		// �d�݌v�Z(���a�덷�ŏ�)
		double[] w_MinSqrtErr = pcFitEM.getWeightsByMinSqrtErr(trainingData);
		System.out.println("----");
		for (int i = 0; i < w_MinSqrtErr.length; i++) {
			System.out.println(w_MinSqrtErr[i]);
		}

		// �d�݌v�Z(���a�덷+�������ŏ�)
		double r = Math.pow(Math.E, ln_r);
		double[] w_MinSqrtErrReg = pcFitEM.getWeightsByMinSqrtErrReg(trainingData, r);
		System.out.println("----");
		for (int i = 0; i < w_MinSqrtErrReg.length; i++) {
			System.out.println(w_MinSqrtErrReg[i]);
		}
		
		//�O���t�`��p
		//�����f�[�^
		double[][] sineValues = makeSineValues();
		//�P���f�[�^
		double[][] trainValues = makeTrainValues(trainingData);
		//�w�K����(���a)
		double[][] resultValues = makeResultValues(w_MinSqrtErr, m, func);
		//�w�K����(���a+������)
		double[][] resultValuesReg = makeResultValues(w_MinSqrtErrReg, m, func);

		//�O���t�\��
		XYGraph xyGraph = new XYGraph("Fit Sine(m="+ String.valueOf(m)+", Func="+ BasisFuncName+")", "X", "Y");
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

	private static double[][] makeResultValues(double[] w, int m, BasisFunction func) {
		double[][] ret = new double[2][101];
		//0-1��100�̃f�[�^�Ŗ��߂�
		for(int i=0; i<=100; i++){
			ret[0][i] = i/100.0; //X
			for(int j=0; j<m; j++){ //Y
				ret[1][i] += w[j] * func.phi(j, ret[0][i]); 
			}
		}
		return ret;
	}
}
