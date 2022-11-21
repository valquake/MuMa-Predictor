package predictor_fbp_refactor.jtree;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class AttractorCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		int d = node.getLevel();
		int count = node.getChildCount();
		
		if(d == 2)
			try {
				setIcon(new ImageIcon(ImageIO.read(new File("images/state.png"))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if(d == 1) {
			try {
				setIcon(new ImageIcon(ImageIO.read(new File("images/cycle.png"))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(expanded && count == 1) {
				String t = getText();
				int last = t.indexOf(":");
				t = last == -1 ? t : t.substring(0, last-1);
				setText(t+": Fixed point attractor");
			}else if(expanded && count > 1) {
				String t = getText();
				int last = t.indexOf(":");
				t = last == -1 ? t : t.substring(0, last-1);
				setText(t+": attractor, length="+count);
			}else if(!expanded) {
				String t = getText();
				int last = t.indexOf(":");
				t = last == -1 ? t : t.substring(0, last-1);
				setText(t);
			}
			
		}
		
	
		
			
		return this;
	}

}
