package predictor_fbp_refactor;


import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.swingx.JXTreeTable;
import predictor_fbp_refactor.jxtreetable.TTTreeCellRenderer;
import predictor_fbp_refactor.multithread.ResultThread;
import predictor_fbp_refactor.reactionsystem.RS;
import predictor_fbp_refactor.reactionsystem.RSFactory;
import predictor_fbp_refactor.exceptions.MustContradictionException;
import predictor_fbp_refactor.exceptions.NoReactionsFoundException;
import predictor_fbp_refactor.jtree.AttractorCellRenderer;
import predictor_fbp_refactor.jxtreetable.OneNode;
import predictor_fbp_refactor.jxtreetable.TTM;

import java.awt.Component;
import java.awt.Font;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTree;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author valer
 */
public class NewJFrame extends javax.swing.JFrame{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> true_formulas;
    private TableRowSorter<TableModel> tableSorter;
    private Dimension size_table;
	private DefaultMutableTreeNode attractors_root;
	private JTree attractors_tree;
	private JScrollPane attractors_scrollPane;
	private JProgressBar progressBar;
	private JLabel progressBarLabel;
	private JButton btnFilter;

    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
    	setFont(new Font("Arial", Font.PLAIN, 14));
    	try {
    		UIManager.setLookAndFeel(
    	            UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        initComponents();
        
        reactants_field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            
            public void changed() {
                reactants_filled = !reactants_field.getText().isEmpty();
                
                addReaction_button.setEnabled(reactants_filled & products_filled);
            }
        });
        
        products_field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            
            private void changed() {
                products_filled = !products_field.getText().isEmpty();
                
                addReaction_button.setEnabled(reactants_filled & products_filled);
            }
        });
        
        this.predictor_field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            
            private void changed() {
                computePredictor_button.setEnabled(!predictor_field.getText().isEmpty());
            }
        });
        
        this.listModel = new DefaultListModel<String>();
        this.jList2.setModel(this.listModel);
        
        this.jList2.addListSelectionListener((ListSelectionEvent e) -> {            
            this.deleteSelected_button.setEnabled(jList2.getSelectedIndex() != -1);
        });
        
        /*this.SolutionsOverjTable.setModel(this.model_table_over);
        Dimension size_table = this.SolutionsOverjTable.getSize();
        this.SolutionsOverjTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.SolutionsOverjTable.getColumn("Formula").setPreferredWidth(Math.round((size_table.width) * 0.85f));
        this.SolutionsOverjTable.getColumn("Is verified?").setPreferredWidth(Math.round((size_table.width) * 0.15f));
        tableSorter = new TableRowSorter<>(this.SolutionsOverjTable.getModel()){
            @Override public boolean isSortable(int column) {
                return false;
              }
        };
        this.SolutionsOverjTable.getColumn("Formula").setCellRenderer(new TextAreaCellRenderer());
        this.SolutionsOverjTable.setRowSorter(tableSorter);
        ActionListener myFilterActionListener = a -> doFilter();

        this.SatisfiedjCheckBox.addActionListener(myFilterActionListener);
        this.NotSatisfiedjCheckBox.addActionListener(myFilterActionListener);*/
        size_table = this.treeTableSolutions.getSize();
        this.treeTableSolutions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.treeTableSolutions.getColumn("Formula").setPreferredWidth(Math.round((size_table.width) * 0.85f));
        this.treeTableSolutions.getColumn("Is verified?").setPreferredWidth(Math.round((size_table.width) * 0.15f));
        tableSorter = new TableRowSorter<>(this.treeTableSolutions.getModel()){
            @Override public boolean isSortable(int column) {
                return false;
              }
        };
        this.treeTableSolutions.setRowSorter((RowSorter<? extends TableModel>) tableSorter);
        ActionListener myFilterActionListener = a -> doFilter();

        this.SatisfiedjCheckBox.addActionListener(myFilterActionListener);
        this.NotSatisfiedjCheckBox.addActionListener(myFilterActionListener);
        
        textFieldFilter.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				btnFilter.doClick(1);
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				btnFilter.doClick(1);
				
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        btnFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OneNode solutions;
				if(!root_solutions.getChildren().isEmpty()) {
					
					if(NotSatisfiedjCheckBox.isSelected() && SatisfiedjCheckBox.isSelected() )
						solutions = root_solutions;					
					else if(!NotSatisfiedjCheckBox.isSelected() && !SatisfiedjCheckBox.isSelected()) {
						treeTableSolutions = new JXTreeTable(new TTM(new OneNode("", false), new String[]{"Formula", "Is verified?"}, new Class [] {
				                java.lang.String.class, java.lang.Boolean.class
				            }));
				        refreshTreeTable();				        
				        return;
					}
					else if(NotSatisfiedjCheckBox.isSelected())
						solutions = root_solutions_only_false;
					else 
						solutions = root_solutions_only_true;			
					
					if(textFieldFilter.getText().length()>0) {
						root_solutions_filtered = solutions.getFilteredformula(textFieldFilter.getText());
						treeTableSolutions = new JXTreeTable(new TTM(root_solutions_filtered, new String[]{"Formula", "Is verified?"}, new Class [] {
		                        java.lang.String.class, java.lang.Boolean.class
		                    }));
					}else 
						treeTableSolutions = new JXTreeTable(new TTM(solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
		                        java.lang.String.class, java.lang.Boolean.class
		                    }));
					refreshTreeTable();
			}
				
				
			}

        });
        
        this.deleteSelected_button.addActionListener((e) -> {
            int index = this.jList2.getSelectedIndex();   
            if (index != -1){
                this.rs.deleteReaction(index);
                listModel.remove(index);
                
                Set<String> insts = this.rs.allInstancesOfRS();
                if(!insts.contains("create"))
                	Utils.DUMMY_NODE = false;
                
                // enable all the actions about predictor
                this.predictor_field.setEnabled(true);
                this.steps_spinner.setEnabled(true);
                this.saveRS_button.setEnabled(true);
                this.jText_SolutionMaybeMust.setText("");
                predictor_field_res.setText("");
                steps_field_res.setText("");
                this.NotSatisfiedjCheckBox.setEnabled(false);
                this.SatisfiedjCheckBox.setEnabled(false);
                this.jButtonGenerateSolutionsOver.setEnabled(false);
                this.true_formulas = null;
                this.root_solutions = new OneNode("", false);
            	this.treeTableSolutions = new JXTreeTable(new TTM(this.root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
                        java.lang.String.class, java.lang.Boolean.class
                    }));
            	refreshTreeTable();
                
                if(listModel.size() == 0){
                    // We need to disable all the actions about predictor
                    this.predictor_field.setEnabled(false);
                    this.steps_spinner.setEnabled(false);
                    this.saveRS_button.setEnabled(false);

                }
            }
        });
        
        this.rs = null;
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel1.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jLabel2 = new javax.swing.JLabel();
        jLabel2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jLabel3 = new javax.swing.JLabel();
        jLabel3.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jLabel4 = new javax.swing.JLabel();
        jLabel4.setFont(new Font("Tahoma", Font.PLAIN, 12));
        reactants_field = new javax.swing.JTextField();
        reactants_field.setFont(new Font("Tahoma", Font.PLAIN, 14));
        products_field = new javax.swing.JTextField();
        products_field.setFont(new Font("Tahoma", Font.PLAIN, 14));
        inhibitors_field = new javax.swing.JTextField();
        inhibitors_field.setFont(new Font("Tahoma", Font.PLAIN, 14));
        jLabel5 = new javax.swing.JLabel();
        jLabel5.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jLabel6 = new javax.swing.JLabel();
        jLabel6.setFont(new Font("Tahoma", Font.PLAIN, 12));
        addReaction_button = new javax.swing.JButton();
        addReaction_button.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        deleteSelected_button = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jList2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        saveRS_button = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        computePredictor_button = new javax.swing.JButton();
        computePredictor_button.setFont(new Font("Tahoma", Font.PLAIN, 12));
        predictor_field = new javax.swing.JTextField();
        predictor_field.setFont(new Font("Tahoma", Font.PLAIN, 14));
        jLabel7 = new javax.swing.JLabel();
        jLabel7.setFont(new Font("Tahoma", Font.PLAIN, 12));
        steps_spinner = new javax.swing.JSpinner();
        steps_spinner.setFont(new Font("Tahoma", Font.PLAIN, 14));
        jPanel5 = new javax.swing.JPanel();
        jTabbedPaneSolution = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jButtonGenerateSolutionsOver = new javax.swing.JButton();
        jButtonGenerateSolutionsOver.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jLabel10 = new javax.swing.JLabel();
        jLabel10.setFont(new Font("Tahoma", Font.PLAIN, 12));
        NotSatisfiedjCheckBox = new javax.swing.JCheckBox();
        NotSatisfiedjCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
        SatisfiedjCheckBox = new javax.swing.JCheckBox();
        SatisfiedjCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jScrollPane6 = new javax.swing.JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.root_solutions = new OneNode("prova", false);
        this.root_solutions_only_true = new OneNode("prova", false);
        this.root_solutions_only_false = new OneNode("prova", false);
        this.treeTableSolutions = new JXTreeTable(new TTM(this.root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            }));
        treeTableSolutions.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel8.setFont(new Font("Tahoma", Font.PLAIN, 12));
        predictor_field_res = new javax.swing.JTextField();
        predictor_field_res.setFont(new Font("Tahoma", Font.PLAIN, 14));
        jLabel9 = new javax.swing.JLabel();
        jLabel9.setFont(new Font("Tahoma", Font.PLAIN, 12));
        steps_field_res = new javax.swing.JTextField();
        steps_field_res.setFont(new Font("Tahoma", Font.PLAIN, 14));
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuBar1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jMenu_New = new javax.swing.JMenu();
        jMenuItemNew = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Over-Approximation");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setMaximumSize(new Dimension(900, 406));
        setResizable(false);

        jLabel1.setText("Reaction:");

        jLabel2.setText("Reactants");

        jLabel3.setText("Products");

        jLabel4.setText("Inhibitors");

        jLabel5.setText("→");

        jLabel6.setText("|");

        addReaction_button.setText("Add Reaction");
        addReaction_button.setEnabled(false);
        addReaction_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addReaction_buttonActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Reaction System"));
        jPanel1.setToolTipText("");
        jPanel1.setName(""); // NOI18N

        deleteSelected_button.setText("Delete Selected");
        deleteSelected_button.setEnabled(false);

        jList2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jList2);

        saveRS_button.setText("Save Reaction System");
        saveRS_button.setEnabled(false);
        saveRS_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveRS_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveRS_button)
                        .addGap(18, 18, 18)
                        .addComponent(deleteSelected_button)
                        .addGap(25, 25, 25)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteSelected_button)
                    .addComponent(saveRS_button))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Predictor"));

        computePredictor_button.setText("Compute Predictor");
        computePredictor_button.setEnabled(false);
        computePredictor_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                computePredictor_buttonActionPerformed(evt);
            }
        });

        predictor_field.setEnabled(false);

        jLabel7.setText("Steps: ");

        steps_spinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        steps_spinner.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addGap(20)
        			.addComponent(predictor_field, GroupLayout.PREFERRED_SIZE, 171, GroupLayout.PREFERRED_SIZE)
        			.addGap(18)
        			.addComponent(jLabel7, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(steps_spinner, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
        			.addComponent(computePredictor_button)
        			.addGap(23))
        );
        jPanel2Layout.setVerticalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(Alignment.LEADING, jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(predictor_field, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
        				.addComponent(computePredictor_button)
        				.addComponent(jLabel7)
        				.addComponent(steps_spinner, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel2.setLayout(jPanel2Layout);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Predictor Results"));

        jTabbedPaneSolution.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jButtonGenerateSolutionsOver.setText("Generate Solutions");
        jButtonGenerateSolutionsOver.setEnabled(false);
        jButtonGenerateSolutionsOver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateSolutionsOverActionPerformed(evt);
            }
        });

        jLabel10.setText("Filter by:");

        NotSatisfiedjCheckBox.setText("Not Satisfied");
        NotSatisfiedjCheckBox.setEnabled(false);

        SatisfiedjCheckBox.setText("Satifsfied");
        SatisfiedjCheckBox.setEnabled(false);
        this.treeTableSolutions.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane6.setViewportView(this.treeTableSolutions);
        jText_SolutionMaybeMust = new javax.swing.JTextArea();
        
        jText_SolutionMaybeMust.setEditable(false);
        jText_SolutionMaybeMust.setColumns(2);
        jText_SolutionMaybeMust.setRows(5);

        jScrollPane5.setViewportView(jText_SolutionMaybeMust);
        
        textFieldFilter = new JTextField();
        textFieldFilter.setFont(new Font("Tahoma", Font.PLAIN, 12));
        textFieldFilter.setColumns(10);
        
        btnFilter = new JButton("...");
        btnFilter.setVisible(false);
        
        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4Layout.setHorizontalGroup(
        	jPanel4Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel4Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane6, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
        				.addComponent(jScrollPane5, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 550, GroupLayout.PREFERRED_SIZE)
        				.addGroup(Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
        					.addComponent(textFieldFilter, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnFilter)
        					.addPreferredGap(ComponentPlacement.RELATED, 175, Short.MAX_VALUE)
        					.addComponent(jButtonGenerateSolutionsOver))
        				.addGroup(Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
        					.addComponent(jLabel10, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(NotSatisfiedjCheckBox, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
        					.addGap(18)
        					.addComponent(SatisfiedjCheckBox, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
        					.addGap(0, 286, Short.MAX_VALUE)))
        			.addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
        	jPanel4Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel4Layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
        			.addGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING, false)
        				.addGroup(jPanel4Layout.createSequentialGroup()
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(jButtonGenerateSolutionsOver)
        					.addGap(18))
        				.addGroup(Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
        					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        					.addGroup(jPanel4Layout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(textFieldFilter, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        						.addComponent(btnFilter))
        					.addGap(8)))
        			.addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 228, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
        			.addGroup(jPanel4Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel10)
        				.addComponent(NotSatisfiedjCheckBox)
        				.addComponent(SatisfiedjCheckBox))
        			.addGap(8))
        );
        jPanel4.setLayout(jPanel4Layout);

        jTabbedPaneSolution.addTab("Must/Maybe set based predictor", jPanel4);
        
        attractors_scrollPane = new JScrollPane();

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3Layout.setHorizontalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel3Layout.createSequentialGroup()
        			.addGap(5)
        			.addComponent(attractors_scrollPane, GroupLayout.PREFERRED_SIZE, 560, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(5, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel3Layout.createSequentialGroup()
        			.addGap(5)
        			.addComponent(attractors_scrollPane, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(343, Short.MAX_VALUE))
        );
        
        attractors_root = new DefaultMutableTreeNode("root");
        attractors_tree = new JTree(attractors_root);
        attractors_tree.setRootVisible(false);
        attractors_tree.setCellRenderer(new AttractorCellRenderer());
        attractors_scrollPane.setViewportView(attractors_tree);
        jPanel3.setLayout(jPanel3Layout);

        jTabbedPaneSolution.addTab("Attractors", jPanel3);

        jLabel8.setText("Predictor");

        predictor_field_res.setEditable(false);

        jLabel9.setText("Steps");

        steps_field_res.setEditable(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5Layout.setHorizontalGroup(
        	jPanel5Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel5Layout.createSequentialGroup()
        			.addGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel5Layout.createSequentialGroup()
        					.addGap(160)
        					.addGroup(jPanel5Layout.createParallelGroup(Alignment.TRAILING)
        						.addGroup(jPanel5Layout.createSequentialGroup()
        							.addComponent(jLabel8, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
        							.addGap(31))
        						.addGroup(jPanel5Layout.createSequentialGroup()
        							.addComponent(predictor_field_res, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)))
        					.addGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
        						.addGroup(jPanel5Layout.createSequentialGroup()
        							.addGap(74)
        							.addComponent(jLabel9, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
        						.addGroup(jPanel5Layout.createSequentialGroup()
        							.addGap(51)
        							.addComponent(steps_field_res, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE))))
        				.addGroup(jPanel5Layout.createSequentialGroup()
        					.addGap(22)
        					.addComponent(jTabbedPaneSolution, GroupLayout.PREFERRED_SIZE, 572, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(439, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
        	jPanel5Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel5Layout.createSequentialGroup()
        			.addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel8)
        				.addComponent(jLabel9))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(predictor_field_res, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
        				.addComponent(steps_field_res, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE))
        			.addGap(20)
        			.addComponent(jTabbedPaneSolution, GroupLayout.PREFERRED_SIZE, 470, GroupLayout.PREFERRED_SIZE)
        			.addGap(23))
        );
        jPanel5.setLayout(jPanel5Layout);

        jTabbedPaneSolution.getAccessibleContext().setAccessibleName("Under");

        jMenu_New.setText("File");

        jMenuItemNew.setText("New Reaction System");
        jMenuItemNew.addActionListener(e -> jMenuItemNewActionPerformed(e));
        jMenu_New.add(jMenuItemNew);
        jMenuBar1.add(jMenu_New);
        
        jMenuItemOpen = new JMenuItem();
        jMenuItemOpen.setText("Import Reaction System");
        jMenuItemOpen.addActionListener(e -> jMenuItemOpenActionPerformed(e));
        jMenu_New.add(jMenuItemOpen);
        
        setJMenuBar(jMenuBar1);
        
        mnNewMenu = new JMenu("Options");
        jMenuBar1.add(mnNewMenu);
        
        noInhibitorRadioButtonItem = new JRadioButtonMenuItem("No inhibitors RS");
        noInhibitorRadioButtonItem.addItemListener(e -> {
        	if (e.getStateChange() == ItemEvent.SELECTED) {
                Utils.NO_INHIBITORS = true;
            }
            else if (e.getStateChange() == ItemEvent.DESELECTED) {
            	Utils.NO_INHIBITORS = false;
            }
        	
        	if(rs != null) {
        		List<Reaction> reacts = rs.getListofReactions();
        		RS temp_rs = null;
        		for(int i = 0; i < reacts.size(); i++) {    			
            		Reaction r0 = reacts.get(i);
            		String[] rnt = Arrays.copyOf(r0.reactants().toArray(), r0.reactants().size(), String[].class);
            		String[] inh = Arrays.copyOf(r0.inhibitors().toArray(), r0.inhibitors().size(), String[].class);
            		String[] pdt = Arrays.copyOf(r0.products().toArray(), r0.products().size(), String[].class);
            		if(i == 0)
            			temp_rs = RSFactory.getRS(rnt, inh, pdt);
            		else temp_rs.addReaction(rnt, inh, pdt);
        		}
        		
        		if(!rs.isOverNull()) {
	        		if(rs.getPredictorOverapprox() != null)
						try {
							Map.Entry<Must, Maybe> overapprox_sol = temp_rs.computeOverApproximationEfficiently(rs.getReqProd(), rs.getSteps());
							jText_SolutionMaybeMust.setText(overapprox_sol.getKey().toString() + "\n"
                                    + overapprox_sol.getValue().toString());
						} catch (MustContradictionException | NoReactionsFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	        		
	        		this.root_solutions = new OneNode("", false);
	            	this.treeTableSolutions = new JXTreeTable(new TTM(this.root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
	                        java.lang.String.class, java.lang.Boolean.class
	                    }));
	            	refreshTreeTable();
        		}
        		rs = temp_rs;
        	}
        });

        noInhibitorRadioButtonItem.setToolTipText("It converts the actual RS into "
        		+ "a no-inhibitors RS.");
        mnNewMenu.add(noInhibitorRadioButtonItem);
        
        try {
			jMenuItemOpen.setIcon(new ImageIcon(ImageIO.read(new File("images/open.png"))));
			jMenu_New.setIcon(new ImageIcon(ImageIO.read(new File("images/menu.png"))));
			jMenuItemNew.setIcon(new ImageIcon(ImageIO.read(new File("images/newfile.png"))));
			mnNewMenu.setIcon(new ImageIcon(ImageIO.read(new File("images/settings.png"))));
			
			mnNewMenu_1 = new JMenu("Multithread");
			mnNewMenu.add(mnNewMenu_1);
			
			rdbtnmntmNewRadioItem = new JRadioButtonMenuItem("Enable Multithread");
			mnNewMenu_1.add(rdbtnmntmNewRadioItem);
			
			mntmNewMenuItem = new JMenuItem("Set #Thread");
			mnNewMenu_1.add(mntmNewMenuItem);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setHgap(2);
        flowLayout.setVgap(2);
        flowLayout.setAlignment(FlowLayout.RIGHT);
        

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addGroup(layout.createSequentialGroup()
        							.addContainerGap()
        							.addGroup(layout.createParallelGroup(Alignment.LEADING)
        								.addGroup(layout.createSequentialGroup()
        									.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        										.addGroup(layout.createSequentialGroup()
        											.addComponent(jLabel1)
        											.addPreferredGap(ComponentPlacement.UNRELATED)
        											.addComponent(reactants_field, GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
        											.addPreferredGap(ComponentPlacement.RELATED))
        										.addGroup(layout.createSequentialGroup()
        											.addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
        											.addGap(49)))
        									.addGroup(layout.createParallelGroup(Alignment.LEADING)
        										.addGroup(layout.createSequentialGroup()
        											.addGap(11)
        											.addComponent(jLabel5)
        											.addPreferredGap(ComponentPlacement.UNRELATED)
        											.addComponent(products_field, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE))
        										.addGroup(layout.createSequentialGroup()
        											.addGap(48)
        											.addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)))
        									.addGap(18)
        									.addGroup(layout.createParallelGroup(Alignment.LEADING)
        										.addGroup(layout.createSequentialGroup()
        											.addGap(29)
        											.addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
        										.addGroup(layout.createSequentialGroup()
        											.addComponent(jLabel6)
        											.addGap(18)
        											.addComponent(inhibitors_field, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))))
        								.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        								.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        							.addGap(20))
        						.addGroup(layout.createSequentialGroup()
        							.addGap(394)
        							.addComponent(addReaction_button, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)))
        					.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
        						.addComponent(jSeparator1, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE)
        						.addComponent(jPanel5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)))
        				.addGroup(layout.createSequentialGroup()
        					.addContainerGap()
        					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 1149, Short.MAX_VALUE)))
        			.addContainerGap())
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGap(10)
        			.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, 578, GroupLayout.PREFERRED_SIZE))
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(jLabel3)
        						.addComponent(jLabel4)
        						.addComponent(jLabel2))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(reactants_field, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
        						.addComponent(products_field, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
        						.addComponent(inhibitors_field, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
        						.addComponent(jLabel6)
        						.addComponent(jLabel1)
        						.addComponent(jLabel5))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(addReaction_button, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        					.addGap(18)
        					.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
        			.addContainerGap())
        );
        
        progressBarLabel = new JLabel("");
        panel.add(progressBarLabel);
        
        progressBar = new JProgressBar(0,100);
        panel.add(progressBar);
        getContentPane().setLayout(layout);
        progressBar.setVisible(false);

        pack();
    }// </editor-fold>                        

	private void jMenuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {                                              
        try {
            JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            fileChooser.setFileFilter(new MuMaFileFilter());
            int n = fileChooser.showOpenDialog(this);
            if (n == JFileChooser.APPROVE_OPTION) {
                this.listModel.removeAllElements();            
                this.rs = null;
                Utils.DUMMY_NODE = false;
                File f = fileChooser.getSelectedFile();
                try (BufferedReader read = new BufferedReader(new FileReader(f))) {
                    String line = read.readLine();
                    Pattern twopart = Pattern.compile("^([A-Z\\s]*) -> ([A-Z\\s]*)(?: \\| ([A-Z\\s]*)$)?$");
                    Pattern alternative_twopart = Pattern.compile("^\\(\\{(.*?)\\}\\,\\{(.*?)\\}\\,\\{(.*?)\\}\\)$");
                    
                    while(line != null) {
                        line = line.replaceAll("→", "->");
                        Matcher m = twopart.matcher(line);
                        
                        if(!m.matches()) {
                        	m = alternative_twopart.matcher(line);
                        	
                        	if(!m.matches()) {
	                        	JOptionPane.showMessageDialog(this, "Error while loading " + f.getName() 
		                            + "\nPlease, check that the file is well formed.",
		                            "Not a matching file", JOptionPane.ERROR_MESSAGE);
	                        	return;
                        	}
                        	
                        	String group_R = m.group(1).replace(",", " ");
                    		String group_I = m.group(2) != null ? m.group(2).replace(",", " ") : "";
                    		String group_P = m.group(3).replace(",", " ");

                        	if(!addToRS(group_R, group_I, group_P)) {
                        		this.listModel.removeAllElements();            
                                this.rs = null;
                        		return;
                        	}
                        	
                        	line = group_I.equals("") ? group_R + " -> " + group_P : group_R + " -> " + group_P + " | " + group_I;
                        }
                        else {
                        	String group_R = m.group(1);
                    		String group_I = m.group(3) != null ? m.group(3) : "";
                    		String group_P = m.group(2);
                        	
                        	if(!addToRS(group_R, group_I, group_P)) {
                        		this.listModel.removeAllElements();            
                                this.rs = null;
                        		return;
                        	}
                        }
                        
                        
                        this.listModel.addElement(line.replaceAll(" -> ", " → "));
                        line = read.readLine();
                    }
                    
                    if(!this.listModel.isEmpty()){
                        // enable all the actions about predictor
                        this.predictor_field.setEnabled(true);
                        this.steps_spinner.setEnabled(true);
                        this.saveRS_button.setEnabled(true);
                        this.jText_SolutionMaybeMust.setText("");
                        predictor_field_res.setText("");
                        steps_field_res.setText("");
                        this.NotSatisfiedjCheckBox.setEnabled(false);
                        this.SatisfiedjCheckBox.setEnabled(false);
                        this.jButtonGenerateSolutionsOver.setEnabled(false);
                        this.true_formulas = null;
                        this.root_solutions = new OneNode("", false);
                    	this.treeTableSolutions = new JXTreeTable(new TTM(this.root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
                                java.lang.String.class, java.lang.Boolean.class
                            }));
                    	refreshTreeTable();
                    }else{
                        JOptionPane.showMessageDialog(this, "Error while loading " + f.getName() 
                                + "\nPlease, check that the file is well sized.",
                        "RS is empty", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
          } catch(HeadlessException | IOException | PatternSyntaxException e){
              JOptionPane.showMessageDialog(this, "Something goes wrong." 
                                + "\nPlease, check that the file is well sized.",
                        "General error", JOptionPane.ERROR_MESSAGE);
          }
    }                                             

    
    private void addReaction_buttonActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        String reactants = reactants_field.getText();
        String products = products_field.getText();        
        String inhibitors = inhibitors_field.getText();
       
        // We need to check if reaction is well formatted (only letters, numbers and some special symbols)
        if(!isReactionWellFormatted(reactants, products, inhibitors)) {
        	JOptionPane.showMessageDialog(this, "Badly formatted text! \n"
        			+ "Please, fill in the fields only with letters (you can use both uppercase and lowercase), spaces and optionally /, ., - and _. \n"
        			+ "Note that it must contain at least one letter.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
            
        try{
        	String[] in = inhibitors.isBlank() ? new String[]{} : inhibitors.split("\\s+");
        	
            if(this.rs == null)
            	this.rs = RSFactory.getRS(reactants.split("\\s+"), in, products.split("\\s+"));
            else this.rs.addReaction(reactants.split("\\s+"), in, products.split("\\s+"));
            
            String option_in = inhibitors.length() != 0 ? " | " + inhibitors : "";
            
            // add to jlist
            this.listModel.addElement(reactants + " → " + products + option_in);
            
            // enable all the actions about predictor
            this.predictor_field.setEnabled(true);
            this.steps_spinner.setEnabled(true);
            
        }catch(IllegalArgumentException e){
            JOptionPane.showMessageDialog(this, e.getMessage() + "\nNon-legal reaction!",
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        reactants_field.setText("");
        products_field.setText("");
        inhibitors_field.setText("");
        saveRS_button.setEnabled(true);
    }                                                  

    private boolean isReactionWellFormatted(String reactants, String products, String inhibitors) {
        String regex = "^(?=.*[A-Za-z])[ A-Za-z0-9-_.\\/]{1,}$";
        
        boolean containsLettersAndNumbersOptionalSC = Pattern.matches(regex, reactants)
                && Pattern.matches(regex, products) 
                && (inhibitors.isEmpty() || Pattern.matches(regex, inhibitors));
        
		return containsLettersAndNumbersOptionalSC;
	}
    
    private boolean isStringWellFormatted(String s) {
        String regex = "^(?=.*[A-Za-z])[ A-Za-z0-9-_.\\/]{1,}$";
        
        boolean containsLettersAndNumbersOptionalSC = Pattern.matches(regex, s);
        
		return containsLettersAndNumbersOptionalSC;
	}
    

	private void computePredictor_buttonActionPerformed(java.awt.event.ActionEvent evt) {                                                        
        int steps = (int) this.steps_spinner.getValue();
        String p = this.predictor_field.getText();
        
        if(!isStringWellFormatted(p)) {
        	JOptionPane.showMessageDialog(this, "Badly formatted text! \n"
        			+ "Please, fill in the fields only with letters (you can use both uppercase and lowercase), spaces and optionally /, ., - and _. \n"
        			+ "Note that it must contain at least one letter.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        
        try {
            Set<String> ps = new HashSet<>(Arrays.asList(p.split("\\s+")));
            
            Instant start = Instant.now();
            System.out.println(ps.toString() + " "+steps);
            Map.Entry<Must, Maybe> sol = null;
            sol = this.rs.computeOverApproximationEfficiently(ps, steps);
            Instant finish = Instant.now();
            long elapsedTime = Duration.between(start, finish).toMillis();
            System.out.println("\nTime for computing overapproximation: " + elapsedTime);
            
            List<String> all_products = Arrays.asList(p.split("\\s+"));
            String ordered_p = all_products.stream()
                                .sorted()
                                .reduce((t, u) -> {
                                    return t + " " + u; 
                                })
                                .orElse("");
            
            predictor_field_res.setText(ordered_p);
            steps_field_res.setText(Integer.toString(steps));

            jText_SolutionMaybeMust.setText("");
            jText_SolutionMaybeMust.setText(sol.getKey().toString() + "\n"
                                    + sol.getValue().toString());
            this.root_solutions = new OneNode("", false);
        	this.treeTableSolutions = new JXTreeTable(new TTM(this.root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
                    java.lang.String.class, java.lang.Boolean.class
                }));
        	refreshTreeTable();
            
            jButtonGenerateSolutionsOver.setEnabled(true);  
            this.true_formulas = null;
        } catch (NoReactionsFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage() + " is not produced by any reaction, so "
                    + "it is not possible to calculate the predictor without introducing something into the context!",
                    "No Reaction found", JOptionPane.ERROR_MESSAGE);
        } catch (MustContradictionException ex){
            JOptionPane.showMessageDialog(this, "Result in calculation requires "
                    + "the simultaneous presence/absence of substances " + ex.getMessage()
                            + ".\n So it is not possible to obtain " + p + " in " + steps + " steps!", "Must contradiction", JOptionPane.ERROR_MESSAGE);
        }
        this.predictor_field.setText("");
        this.steps_spinner.setValue(1);
    }                                                       

    private void jButtonGenerateSolutionsOverActionPerformed(java.awt.event.ActionEvent evt) {
    	if(worker != null)
	        if (!worker.isDone())
	            worker.cancel(true); //destroy previous worker
    	this.root_solutions = new OneNode("root", false);
    	this.root_solutions_only_false = new OneNode("root", false);
    	this.root_solutions_only_true = new OneNode("root", false);
    	this.treeTableSolutions = new JXTreeTable(new TTM(this.root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            }));
        this.NotSatisfiedjCheckBox.setEnabled(true);
        this.SatisfiedjCheckBox.setEnabled(true);
        this.NotSatisfiedjCheckBox.setSelected(true);
        this.SatisfiedjCheckBox.setSelected(true);        
        
        initWorker();
        try {
        	worker.execute();
		}catch(UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Unsupported operation", JOptionPane.ERROR_MESSAGE);
		}
    }  
    
    private SwingWorker<Void, Integer> worker;
    
    private void initWorker() {
        worker = new SwingWorker<Void, Integer>() {
        	
        	List<String> deriving_formulas = new ArrayList<>();
            @Override
            protected void process(List<Integer> chunks) {
            	
                int value = chunks.get(0);
                if(!progressBar.isVisible())
                	progressBar.setVisible(true);
                progressBar.setValue(value);
                
                if(value == 10)
                	progressBarLabel.setText("Computing all possible formulas");
                else if(value == 25)
                	progressBarLabel.setText("Checking all formulas");
                else if(value == 75)
                	progressBarLabel.setText("Saving checks in JTree");
                else if(value == 90)
                	progressBarLabel.setText("Displaying results");
            }
            
            

            @Override
			protected void done() {
            	try {
            		get();
            	}catch(Exception e) {
            		e.printStackTrace();
            	}
                progressBar.setVisible(false);
                progressBarLabel.setText("");
				super.done();
			}

			@Override
            protected Void doInBackground() throws Exception {
            	publish(10);
                List<String> ll = rs.generateAllSets();
                List<ResultThread> results = null;
            	Instant absolute_start = Instant.now();
            	publish(25);
    	        if(!Utils.MULTITHREAD)
    	        	results = rs.checkAllFormulas(ll);
    	        else
    	        	results = rs.checkAllFormulasMultithread(ll);
    	        
    	        publish(75);
    	        
    	        Instant absolute_finish = Instant.now();
    	        long absolute_elapsed_time = Duration.between(absolute_start, absolute_finish).toMillis();
    	        System.out.println("TOTAL TIME: " + absolute_elapsed_time);
    	
    	    	for(ResultThread res : results) {
    	    		if(!Utils.MULTITHREAD)
	    	    		if(res.getDerivedFormula() == null) {
	    	    			root_solutions.getChildren().add(new OneNode(res.getFormula(), res.isVer()));
	    	    			
	    	    			if(res.isVer())
	        	    			root_solutions_only_true.getChildren().add(new OneNode(res.getFormula(), res.isVer()));
	        	    		else root_solutions_only_false.getChildren().add(new OneNode(res.getFormula(), res.isVer()));
	    	    		}else {
	    	    			root_solutions.getChildrenOfaChild(res.getDerivedFormula()).add(new OneNode(res.getFormula(), res.isVer()));
	    	    			root_solutions_only_true.getChildrenOfaChild(res.getDerivedFormula()).add(new OneNode(res.getFormula(), res.isVer()));
	    	    		}
    	    		else {
    	    			
    	    			// it can be that thread that found this solution didn't get primal deriving formula
    	    			String found = res.isVer() ? deriving_formulas.stream()
    	    					.filter(t -> rs.isSubsequence(t, res.getFormula()))
    	    					.findFirst()
    	    					.orElse("") : "";
    	    			
    	    			if(res.getDerivedFormula() == null) {
    	    				// if there is a deriving formula, then current formula must be added as a child
    	    				if(!found.isEmpty())
    	    					root_solutions.getChildrenOfaChild(found).add(new OneNode(res.getFormula(), res.isVer()));
    	    				else root_solutions.getChildren().add(new OneNode(res.getFormula(), res.isVer()));
	    	    			
	    	    			if(res.isVer()) {
	    	    				if(!found.isEmpty())
	    	    					root_solutions_only_true.getChildrenOfaChild(found).add(new OneNode(res.getFormula(), res.isVer()));
	    	    				else {
	    	    					deriving_formulas.add(res.getFormula());
	    	    					root_solutions_only_true.getChildren().add(new OneNode(res.getFormula(), res.isVer()));
	    	    				}
	    	    			}else root_solutions_only_false.getChildren().add(new OneNode(res.getFormula(), res.isVer()));
	    	    		}else {
	    	    			// maybe the thread found a deriving formula that contains another deriving formula more general
	    	    			// this operation unify them
	    	    			if(!found.isEmpty()) {
	    	    				root_solutions.getChildrenOfaChild(found).add(new OneNode(res.getFormula(), res.isVer()));
		    	    			root_solutions_only_true.getChildrenOfaChild(found).add(new OneNode(res.getFormula(), res.isVer()));

	    	    				if(!root_solutions.containsInChild(found, res.getDerivedFormula())) {
		    	    				root_solutions.getChildrenOfaChild(found).add(new OneNode(res.getDerivedFormula(), res.isVer()));
			    	    			root_solutions_only_true.getChildrenOfaChild(found).add(new OneNode(res.getDerivedFormula(), res.isVer()));
	    	    				}
	    	    			}else {
	    	    				
	    	    				System.out.println("New deriving formula: " + res.getDerivedFormula().toString());
	    	    				// there is a real new deriving formula, add it to list
	    	    				deriving_formulas.add(res.getDerivedFormula());
	    	    				root_solutions.getChildrenOfaChild(res.getDerivedFormula()).add(new OneNode(res.getFormula(), res.isVer()));
		    	    			root_solutions_only_true.getChildrenOfaChild(res.getDerivedFormula()).add(new OneNode(res.getFormula(), res.isVer()));

	    	    			}
	    	    		}
    	    		}
    	    		
    	    	}
    	    	publish(90);
    	        if(Utils.ATTRACTOR_DETECTOR) {
    		        LinkedHashSet<LinkedHashSet<LinkedHashSet<String>>> cycles = rs.getAllCycles();
    		        attractors_root.removeAllChildren();
    		        
    		        int n_cycles = 1;
    		        for(LinkedHashSet<LinkedHashSet<String>> cycle : cycles) {             	
    		        	DefaultMutableTreeNode attr = new DefaultMutableTreeNode("Attr"+n_cycles);
    		        	int n_states = 1;
    		        	for(LinkedHashSet<String> state : cycle) {
    		        		DefaultMutableTreeNode st = new DefaultMutableTreeNode("State"+n_states);
    		        		for(String member : state) 
    		        			st.add(new DefaultMutableTreeNode(member));
    		        		attr.add(st);
    		        		n_states++;
    		        	}
    		        	n_cycles++;
    		        	attractors_root.add(attr);
    		        }
    		       
    		        attractors_tree = new JTree(attractors_root);
    		        attractors_tree.setRootVisible(false);
    		        attractors_tree.setCellRenderer(new AttractorCellRenderer());
    		        attractors_tree.setShowsRootHandles(true);
    		        attractors_tree.putClientProperty("JTree.lineStyle", "Angled");
    		        attractors_scrollPane.setViewportView(attractors_tree);
    		        
    	        }
    	        
    	        treeTableSolutions = new JXTreeTable(new TTM(root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
    	                java.lang.String.class, java.lang.Boolean.class
    	            }));
    	        refreshTreeTable();
    	        publish(100);
                return null;
            }

        };
    }

    private void refreshTreeTable() {   
        this.treeTableSolutions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.treeTableSolutions.getColumn("Formula").setPreferredWidth(Math.round((size_table.width) * 0.85f));
        this.treeTableSolutions.getColumn("Is verified?").setPreferredWidth(Math.round((size_table.width) * 0.15f));
        this.treeTableSolutions.setTreeCellRenderer(new TTTreeCellRenderer(this.treeTableSolutions));
        
        tableSorter = new TableRowSorter<>(this.treeTableSolutions.getModel()){
            @Override public boolean isSortable(int column) {
                return false;
              }
        };
        this.treeTableSolutions.setRowSorter((RowSorter<? extends TableModel>) tableSorter);
    	jScrollPane6.setViewportView(this.treeTableSolutions);
	}
    
    private boolean addToRS(String group_R, String group_I, String group_P) {
    		if(!isReactionWellFormatted(group_R, group_P, group_I)) {
    			JOptionPane.showMessageDialog(this, "Something goes wrong." 
                        + "\nPlease, check that the file is well sized.",
                "General error", JOptionPane.ERROR_MESSAGE);
    			
    			return false;
    		}
    		
            String[] in = group_I != null? group_I.split("\\s+") : new String[]{};
            String[] r = group_R.split("\\s+");
            String[] p = group_P.split("\\s+");
            
            if(this.rs == null)
                this.rs = RSFactory.getRS(r, in, p);
            else
            	this.rs.addReaction(r, in, p);
            
           return true;
	}

	private void jMenuItemNewActionPerformed(java.awt.event.ActionEvent evt) {                                             
        this.rs = null;
        Utils.DUMMY_NODE = false;
        this.listModel.removeAllElements();
        this.root_solutions = new OneNode("", false);
    	this.treeTableSolutions = new JXTreeTable(new TTM(this.root_solutions, new String[]{"Formula", "Is verified?"}, new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            }));
    	refreshTreeTable();
        this.jList2.removeAll();
        this.jText_SolutionMaybeMust.setText("");
        this.reactants_field.setText("");
        this.products_field.setText("");
        this.inhibitors_field.setText("");
        predictor_field_res.setText("");
        steps_field_res.setText("");
        this.NotSatisfiedjCheckBox.setEnabled(false);
        this.SatisfiedjCheckBox.setEnabled(false);
        this.jButtonGenerateSolutionsOver.setEnabled(false);
        this.deleteSelected_button.setEnabled(false);
        this.computePredictor_button.setEnabled(false);
        this.addReaction_button.setEnabled(false);
        this.predictor_field.setEnabled(false);
        this.steps_spinner.setEnabled(false);
        this.saveRS_button.setEnabled(false);
        this.true_formulas = null;
    }                                            

    private void saveRS_buttonActionPerformed(java.awt.event.ActionEvent evt) {                                              
    try {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setFileFilter(new MuMaFileFilter());
        int n = fileChooser.showSaveDialog(this);
        if (n == JFileChooser.APPROVE_OPTION) {
          File f = fileChooser.getSelectedFile();
            try (BufferedWriter write = new BufferedWriter(new FileWriter(f))) {
                for(int i = 0; i < this.listModel.size(); i++){
                    write.append(((String) this.listModel.getElementAt(i)) + "\n");
                    write.flush();
                } }
        }
      } catch (HeadlessException | IOException ex) {
          JOptionPane.showMessageDialog(this, "Something went wrong while writing the file.",
                        "General error", JOptionPane.ERROR_MESSAGE);
      }
      
    }                                             

   private void doFilter() {	   

	   if(!NotSatisfiedjCheckBox.isSelected() && !SatisfiedjCheckBox.isSelected()) {
		   this.treeTableSolutions = new JXTreeTable(new TTM(new OneNode("", false), new String[]{"Formula", "Is verified?"}, new Class [] {
	                java.lang.String.class, java.lang.Boolean.class
	            }));
	        refreshTreeTable();
	        return;
	   }
	   
	   btnFilter.doClick();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NewJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JCheckBox NotSatisfiedjCheckBox;
    private javax.swing.JCheckBox SatisfiedjCheckBox;
    //private JTable SolutionsOverjTable;
    private JXTreeTable treeTableSolutions;
    private OneNode root_solutions;
    private OneNode root_solutions_only_true;
    private OneNode root_solutions_only_false;
    private OneNode root_solutions_filtered;
    private javax.swing.JButton addReaction_button;
    private javax.swing.JButton computePredictor_button;
    private javax.swing.JButton deleteSelected_button;
    private javax.swing.JTextField inhibitors_field;
    private javax.swing.JButton jButtonGenerateSolutionsOver;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<String> jList2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemNew;
    private javax.swing.JMenu jMenu_New;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPaneSolution;
    private javax.swing.JTextArea jText_SolutionMaybeMust;
    private javax.swing.JTextField predictor_field;
    private javax.swing.JTextField predictor_field_res;
    private javax.swing.JTextField products_field;
    private javax.swing.JTextField reactants_field;
    private javax.swing.JButton saveRS_button;
    private javax.swing.JTextField steps_field_res;
    private javax.swing.JSpinner steps_spinner;
    // End of variables declaration                   
    private DefaultListModel listModel;
    
    private boolean reactants_filled = false;
    private boolean products_filled = false;
    private RS rs;
    private JMenu mnNewMenu;
    private JRadioButtonMenuItem noInhibitorRadioButtonItem;
    private JMenuItem jMenuItemOpen;
    private JTextField textFieldFilter;
    private JMenu mnNewMenu_1;
    private JRadioButtonMenuItem rdbtnmntmNewRadioItem;
    private JMenuItem mntmNewMenuItem;
}