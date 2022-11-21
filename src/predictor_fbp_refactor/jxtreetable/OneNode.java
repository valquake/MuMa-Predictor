package predictor_fbp_refactor.jxtreetable;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OneNode {
	private String formula;
	private boolean isVer;
	private ArrayList<OneNode> children;
	
	public OneNode(String formula, boolean isVer){
		this.formula = formula;
		this.isVer = isVer;
		this.children = new ArrayList<>();
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * @return the isVer
	 */
	public boolean isVer() {
		return isVer;
	}

	@Override
	public String toString() {
		return this.formula + ": " + isVer;
	}
	
	public ArrayList<OneNode> getChildren(){
		return this.children;
	}

	public ArrayList<OneNode> getChildrenOfaChild(String formula) {
		for(OneNode child : children) {
			if(child.getFormula().equals(formula))
				return child.getChildren();
		}
		return null;
	}
	
	public OneNode getFilteredformula(String s){
		OneNode root = new OneNode("root",false);
		String[] words = s.split("\\s+");
		
		for(OneNode child : children) {	
			int check = 0;
			for(int j=0;j<words.length;j++) {
				if(child.getFormula().contains(words[j]))
					check++;				
			}   			
			if(check == words.length)
				root.getChildren().add(child);
			else if(!child.getChildren().isEmpty()){
				List<OneNode> filtered = child.getChildren()
					.stream()
					.filter(t -> {
						int checkf = 0;
						for(int j=0;j<words.length;j++) {
							if(t.getFormula().contains(words[j]))
								checkf++;				
						}   			
						if(checkf == words.length)
							return true;
						return false;
					})
					.collect(Collectors.toList());
				root.getChildren().addAll(filtered);
			}
		}
		return root;
		
	}

	public boolean containsInChild(String found, String derivedFormula) {
		if(found.equals(derivedFormula))
			return true;
		
		for(OneNode child : children) {
			if(child.getFormula().equals(found))
				for(OneNode child_of_child : child.getChildren())
					if(child_of_child.getFormula().equals(derivedFormula))
						return true;
		}
		return false;
	}
}
