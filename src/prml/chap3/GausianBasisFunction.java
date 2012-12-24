package prml.chap3;

/**
 * �K�E�X���֐�
 */
public class GausianBasisFunction implements BasisFunction {

	double[] u;
	double s;

	// �p�����[�^���w�肷��
	// �K�E�X�̕��ςƕ��U���w��B
	// �������Au[0]�͖�������B�iu[0]�̓�_0(x)�ɑ������A���̂Ƃ��͕K����_0(x)=1�ł��邽�߁j
	public GausianBasisFunction(double[] u, double s) {
		this.u = u;
		this.s = s;
	}

	// ��`��ƃ��f�������w��B�p�����[�^�͎�������
	public GausianBasisFunction(int m, double begin, double end) {
		this.u = makeAutoParamU(m, begin, end);
		this.s = makeAutoParamS(m, begin, end);;
	}

	// ���֐��̌���
	// �������Aj=0�̏ꍇ�A�K���P��Ԃ��B
	@Override
	public double phi(int j, double x) {
		if (j == 0) {
			return 1;
		}
		return Math.exp(-1 * Math.pow(x - u[j], 2) / (2 * s * s));
	}

	// �p�����[�^u�������I�ɒ�������
	// ��`����AM�œ����ɕ�������ʒu�ɂ���B
	// �߂�l�F ret[1...m] = param u[1...m]
	// ex:M=10, ��`��F0.0 - 1.0
	// u = NaN, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9
	private static double[] makeAutoParamU(int m, double begin, double end) {
		double[] u = new double[m];
		
		double s = (end - begin) / m;
		u[0] = Double.NaN; //ret[0]�͎g��Ȃ�
		for (int i = 1; i < m; i++) {
			u[i] = begin + s * i;
		}
		return u;
	}

	// �p�����[�^s�������I�ɒ�������i��`������f�����œ������j
	// �߂�l�Fparam s
	// ex:m=10, ��`��F0.0 - 1.0
	// s = 0.1
	private static double makeAutoParamS(int m, double begin, double end) {
		double s = (end - begin) / m;
		return s;
	}
}
