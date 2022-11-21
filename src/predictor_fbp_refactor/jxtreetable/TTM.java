package predictor_fbp_refactor.jxtreetable;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class TTM extends AbstractTreeTableModel {

    OneNode root;
    String[] header;
    Class[] column_types;

    public TTM(OneNode root, String[] header, Class[] types) {
        this.root = root;
        this.header= header;
        this.column_types = types;
    }

    @Override
    public int getColumnCount() {
        return header.length;
    }

    @Override
    public String getColumnName(int column) {
        return header[column];
    }

    @Override
	public Class<?> getColumnClass(int column) {
		return this.column_types[column];
	}

	@Override
    public Object getValueAt(Object node, int column) {
		OneNode treenode = (OneNode) node;
		switch (column) {
	    case 0:
	        return treenode.getFormula();
	    case 1:
	        return treenode.isVer();
	    default:
	        return "Unknown";
	    }
    }

    @Override
    public Object getChild(Object node, int index) {
    	OneNode treenode = (OneNode) node;
        return treenode.getChildren().get(index);
    }

    @Override
    public int getChildCount(Object parent) {
    	OneNode treenode = (OneNode) parent;
        return treenode.getChildren().size();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
    	OneNode treenode = (OneNode) parent;
    	OneNode childnode = (OneNode) child;
        for (int i = 0; i > treenode.getChildren().size(); i++) {
            if (treenode.getChildren().get(i) == childnode) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
    	OneNode treenode = (OneNode) node;
        return treenode.getChildren().size() <= 0;
    }

    @Override
    public Object getRoot() {
        return root;
    }
    
    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }
}
