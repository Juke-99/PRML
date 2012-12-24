package prml.chap3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import prml.util.XYGraph;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * ���`��A���f���F�x�C�Y�i���֐���I���j
 */
public class LinearRegressionEvidence extends LinearRegressionByBayes{
	double[] phiEV;
	
	public LinearRegressionEvidence(int numM, BasisFunction func, double alpha,
			double beta, List<double[]> training) {
		super(numM, func, alpha, beta, training);
		this.phiEV = null;
	}

	// alpha���擾
	public double getAlpha(){
		return this.alpha;
	}
	
	// beta���擾
	public double getBeta(){
		return this.beta;
	}

	/**
	 * alpha��beta���������߁B
	 */
	public void arrangeHyperParam() {
		// �� = �� /(m_N^T * m_N)
		// ��^-1 = 1/(N-��) ��_{n=1..N}{ t_n - m_N^T ��(x_n) }^2
		// �� = ��_{i=0�`M-1} ��_i / ( �� + ��_i )
		// ��_i : �� * ��^T * ���@�̍s��̌ŗL�l
		
		// ��_i : �� * ��^T * ���@�̍s��̌ŗL�l(��Ɉ�񂾂��v�Z����΂n�j)
		// �����ł́A�� �͂������� ��^T * �� �̌ŗL�l���v�Z����
		if(this.phiEV == null){
			this.phiEV = new double[this.numM];
			Matrix R = this.PHI.transpose().times(this.PHI).eig().getD();
			for(int j=0; j<this.numM; j++){
				this.phiEV[j] = R.get(j, j);
			}
		}

		// ��_i : �� * ��^T * ���@�̍s��̌ŗL�l
		// �����ł́A����������
		double[] lambdas = new double[this.numM];
		for(int j=0; j<this.numM; j++){
			lambdas[j] = this.beta * this.phiEV[j];
		}

		// �� = ��_{i=0�`M-1} ��_i / ( �� + ��_i )
		double gamma = 0;
		for(int j=0; j<this.numM; j++){
			gamma += lambdas[j] / (this.alpha + lambdas[j]);
		}

		// �� = �� /(m_N^T * m_N)
		this.alpha = gamma / this.mN.transpose().times(this.mN).get(0, 0);

		// ��^-1 = 1/(N-��) ��_{n=1..N}{ t_n - m_N^T * ��(x_n) }^2 = 1/(N-��) * || t - �� * m_N ||^2
		double betaInv = 0;
		Matrix t = new Matrix(training.size(), 1);
		for (int i = 0; i < training.size(); i++) {
			double val_ti = training.get(i)[1];
			t.set(i, 0, val_ti);
		}
		double e = t.minus(this.PHI.times(this.mN)).normF();
		betaInv = 1.0/((double)this.training.size() - gamma) * Math.pow(e, 2);
		this.beta = 1.0 / betaInv;
	}
	
	//�G�r�f���X���v�Z
	public double calcEvidence(){
		// ln p(t|��,��) = M/2 * ln �� + N/2 * ln �� - E(m_N) - 1/2 ln |A| - N/2 * ln(2��)
		// E(m_N) = ��/2 * || t - �� * m_N ||^2 + ��/2 * m_N^T * m_N
		// A = �� * I + �� * ��^T * �� 
		
		double evidence = 0.0;

		//�w�b�Z�s��A
		Matrix I = new Matrix(this.numM, this.numM);
		for (int i = 0; i < this.numM; i++) {
			for (int j = 0; j < this.numM; j++) {
				if (i == j)
					I.set(i, j, 1.0);
			}
		}
		Matrix A = I.times(this.alpha).plus( this.PHI.transpose().times(this.PHI).times(this.beta));

		//E(m_N)
		Matrix t = new Matrix(training.size(), 1);
		for (int i = 0; i < training.size(); i++) {
			double val_ti = training.get(i)[1];
			t.set(i, 0, val_ti);
		}
		double e = t.minus(this.PHI.times(this.mN)).normF();
		double mNex = this.beta / 2.0 * Math.pow(e, 2) + this.alpha / 2.0 * this.mN.transpose().times(this.mN).get(0, 0);

		//�G�r�f���X
		//ln p(t|��,��) = M/2 * ln �� + N/2 * ln �� - E(m_N) - 1/2 ln |A| - N/2 * ln(2��)
		evidence = ((double)this.numM) / 2.0 * Math.log(this.alpha)
					+ ((double)this.training.size()) / 2.0 * Math.log(this.beta)
					- mNex
					- 1.0 / 2.0 * Math.log(A.det())
					- ((double)this.training.size()) / 2.0 * Math.log(Math.PI);

//http://sage.math.canterbury.ac.nz/home/pub/95/
// A.det��A.norm�ɂ���Ƌ��ȏ��ɑ�̂����Ă���炵���@���@�m���ɂ����Ȃ�
//		evidence = ((double)this.numM) / 2.0 * Math.log(this.alpha)
//				+ ((double)this.training.size()) / 2.0 * Math.log(this.beta)
//				- mNex
//				- 1.0 / 2.0 * Math.log(A.norm1())
//				- ((double)this.training.size()) / 2.0 * Math.log(Math.PI);

		return evidence;
	}
	

	
	public static void main(String args[]) throws IOException {
		// �w�K�f�[�^�t�@�C����
		String filename = args[0];
		// ���f���̃p�����[�^��
		int mMax = Integer.parseInt(args[1]);
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

		//���f������ς��Ȃ���G�r�f���X���v�Z
		double[] evidences = new double[mMax+1];
		for(int m = 1; m <= mMax; m++){
			// ���֐��I��
			BasisFunction func = null;
			if (BasisFuncName.equals("GAUSIAN")) {
				func = new GausianBasisFunction(m, 0.0, 1.0);
			} else if (BasisFuncName.equals("POLY")) {
				func = new PolynomialBasisFunction();
			}
		
			// ���`��A�x�C�Y
			LinearRegressionEvidence lrBayes = new LinearRegressionEvidence(m, func,
					alpha, beta, trainingData);
			// w�̎��㕪�z�v�Z(�x�C�Y)
			lrBayes.learn();
			// �G�r�f���X
			evidences[m] = lrBayes.calcEvidence();
		}
		
		// �O���t�`��
		// �G�r�f���X�f�[�^
		double[][] evidenceValues = makeEvidenceValues(evidences);
		
		// �O���t�\��
		XYGraph xyGraph = new XYGraph(
				String.format("Evidence(%s, alpha=%.03f, beta=%.03f)", BasisFuncName, alpha, beta), "M", "Evidence");
		xyGraph.addDataValues("Evidence", evidenceValues, true);
		xyGraph.rangeX(0.0, mMax);
		xyGraph.saveGraphAsPNG("evidence.png", 500, 300); // view���\�b�h�̌�ɌĂяo���ƁA���삪���������̂Œ���
		xyGraph.view(700, 700);


		//---------------------------------------------------
		//���f�������Œ肵�Aaplha��beta�𒲐߂��Ȃ���A�G�r�f���X���v�Z
		int cntMax = 10;
		double[] evidences2 = new double[cntMax+1];
		// ���֐��I��
		BasisFunction func = null;
		if (BasisFuncName.equals("GAUSIAN")) {
			func = new GausianBasisFunction(mMax, 0.0, 1.0);
		} else if (BasisFuncName.equals("POLY")) {
			func = new PolynomialBasisFunction();
		}
		// ���`��A�x�C�Y
		LinearRegressionEvidence lrBayes = new LinearRegressionEvidence(mMax, func,
				alpha, beta, trainingData);
		System.out.println("------------------------");
		System.out.println("alpha\tbeta");
		for(int cnt = 1; cnt <= cntMax; cnt++){
			System.out.println(lrBayes.getAlpha() + "\t" + lrBayes.getBeta());
			// w�̎��㕪�z�v�Z(�x�C�Y)
			lrBayes.learn();
			// �G�r�f���X
			evidences2[cnt] = lrBayes.calcEvidence();
			//alpha��beta�𒲐�
			lrBayes.arrangeHyperParam();
		}
		// �O���t�\��
		double[][] evidenceValues2 = makeEvidenceValues(evidences2);
		XYGraph xyGraph2 = new XYGraph(
				String.format("Evidence(%s, M=%d)", BasisFuncName, mMax), "cnt", "Evidence");
		xyGraph2.addDataValues("Evidence", evidenceValues2, true);
		xyGraph2.rangeX(0.0, mMax);
		xyGraph2.saveGraphAsPNG("evidence2.png", 500, 300); // view���\�b�h�̌�ɌĂяo���ƁA���삪���������̂Œ���
		xyGraph2.view(700, 700);
		
	}

	private static double[][] makeEvidenceValues(double[] evidences) {
		double[][] ret = new double[2][evidences.length - 1];
		for (int i = 1; i < evidences.length; i++) {
			//0�Ԗڂ͏Ȃ�
			ret[0][i-1] = i;
			ret[1][i-1] = evidences[i];
		}
		return ret;
	}
}
