package predictor_fbp_refactor.jxtreetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.jdesktop.swingx.JXTreeTable;

public class TTTreeCellRenderer extends DefaultTreeCellRenderer{
	private static final String closed = "../../images/arrow_closed.png";
	    private static final String open ="../../images/arrow_open.png";
	    private static final String leaf_i = "images/leafs.png";
	    private final Color evenColor = new Color(0xE6_F0_FF);
		private JXTreeTable treeTableSolutions;

		public TTTreeCellRenderer(JXTreeTable t) {
			this.treeTableSolutions = t;
		}
		
	    @Override
	    public Component getTreeCellRendererComponent(JTree tree, Object value,
	        boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
	        OneNode node = (OneNode) value;
	        try {
				//setClosedIcon(new ImageIcon(ImageIO.read(new File("images/leaf.png"))));
				//setOpenIcon(new ImageIcon(ImageIO.read(new File("images/leaf.png"))));
				setLeafIcon(new ImageIcon(ImageIO.read(new File(leaf_i))));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        Component c = super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
	        setFont(new Font("Tahoma", Font.PLAIN, 12));
	        setText(node.getFormula());
	        int string_width = c.getFontMetrics(getFont()).stringWidth(node.getFormula());
	        int rect_width = treeTableSolutions.getColumnModel().getColumn(0).getPreferredWidth();
	        
	        if(sel && string_width > rect_width)
	        	setToolTipText(node.getFormula());
	        else
	        	setToolTipText(null);
	        return this;
	    }
}
