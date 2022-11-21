package predictor_fbp_refactor.multithread;

public class ResultThread {
	private boolean isVer;
	private String formula;
	private String derivedFormula;
	
	/**
	 * @return the derivedFormula
	 */
	public String getDerivedFormula() {
		return derivedFormula;
	}
	/**
	 * @param derivedFormula the derivedFormula to set
	 */
	public void setDerivedFormula(String derivedFormula) {
		this.derivedFormula = derivedFormula;
	}
	public ResultThread(boolean isVer, String formula) {
		this.isVer = isVer;
		this.formula = formula;
		this.derivedFormula = null;
	}
	/**
	 * @return the isVer
	 */
	public boolean isVer() {
		return isVer;
	}
	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}
	
}
