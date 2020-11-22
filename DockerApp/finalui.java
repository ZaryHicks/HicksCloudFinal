// Zachary Hicks
// UI Application for Cloud Computing Final Project (Option 2)
// This application communicates with a GCP Cluster, and retrieves the results of
// the Inverted Indices and Top-N Algorithms processed on Hadoop

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.BoxLayout;

public class finalui extends JFrame{	
	// Main content pane of our frame
	private Container contentPane;
	
	// This string array holds the names of the .tar files provided for us to select from
	// These files exist on the Cluster, and selections here define which groups of files we will use for this run
	private String[] files = null;
	
	
	public finalui(){
		// The main content pane of our applications
		contentPane = getContentPane();
		
		// upon creation, we create and set our landing page
		constructLanding();
		
		// Code to instantiate and display our frame
		setTitle("ZarySearch Engine");
		setLocationRelativeTo(null); // middle of screen
		setPreferredSize(new Dimension(600, 400)); // min size
		setMinimumSize(new Dimension(400, 300)); // min size
		setSize(new Dimension(600, 400));
		//toFront(); // bring to front
		setVisible(true); // make visible
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // exit on close
	}
	
	// Creates the panel and content that shows when the app starts
	public void constructLanding(){
		// This starting page lets the player select their files, then send it to the cluster
		
		// Clear the window
		contentPane.removeAll();
		
		// Create the base panel
		JPanel landing = new JPanel();
		landing.setLayout(new BoxLayout(landing, BoxLayout.Y_AXIS));
		landing.setAlignmentX(Component.CENTER_ALIGNMENT);
		landing.setPreferredSize(new Dimension(500, 300));

		// create the label at the top
		JLabel title = new JLabel("Please Select the Files to Load into ZarySearch:");
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		landing.add(Box.createVerticalGlue());
		landing.add(title);
		landing.add(Box.createVerticalGlue());
		
		// Panel for showing selected files (if we have any)
		JPanel selFiles = new JPanel();
		if(files != null){
			JLabel selText = new JLabel("Selected Files: ");
			selFiles.add(selText);
			for(String s: files){
				JLabel l = new JLabel(s);
				selFiles.add(l);
			}
		}
		landing.add(selFiles);
		landing.add(Box.createVerticalGlue());
		
		// Create our 2 buttons
		JButton chooseFiles = new JButton("Choose Files");
		chooseFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectFiles();
			}
		});
		chooseFiles.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton constructII = new JButton("Construct Inverted Indices / Load Engine");
		constructII.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// HERE we will send FILES array however we must to GCP
				// send(Files);
				
				// HERE we would WAIT for the Cluster to return that we have succeeded
				// wait();
				
				// Now that II are constructed, we will go to the next page
				engineLoaded();
			}
		});
		if(files == null){
			constructII.setEnabled(false);
		}
		else{
			constructII.setEnabled(true);
		}
		constructII.setAlignmentX(Component.CENTER_ALIGNMENT);
		landing.add(chooseFiles);
		landing.add(Box.createVerticalGlue());
		landing.add(constructII);
		landing.add(Box.createVerticalGlue());
		
		// set this to content pane and revalidate
		contentPane.add(landing, BorderLayout.CENTER);
		contentPane.revalidate();
	}			
	
	// Opens up list dialog to let the user select their files for this run
	public void selectFiles(){
		// Clear the window
		contentPane.removeAll();
		
		// base panel
		JPanel selectFile = new JPanel();
		selectFile.setLayout(new BoxLayout(selectFile, BoxLayout.Y_AXIS));
		selectFile.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectFile.setPreferredSize(new Dimension(500, 300));
		
		// create the labels at the top
		JLabel title = new JLabel("Select all of the files you would like loaded into the engine:");
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel info = new JLabel("Use 'Shift' or 'Ctrl' to select multiple");
		info.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectFile.add(Box.createVerticalGlue());
		selectFile.add(title);
		selectFile.add(Box.createVerticalGlue());
		selectFile.add(info);
		selectFile.add(Box.createVerticalGlue());
		
		// Create the list of options
		String[] options = {"Hugo", "Shakespeare", "Tolstoy"};
		final JList list = new JList(options);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		selectFile.add(listScroller);
		selectFile.add(Box.createVerticalGlue());
		
		// Secondary panel for buttons
		JPanel bs = new JPanel();
		bs.setLayout(new BoxLayout(bs, BoxLayout.X_AXIS));
		
		// Create buttons
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// set list of files to send out, return to landing
				Object[] objs = list.getSelectedValues();
				files = new String[objs.length];
				for(int i = 0; i < objs.length; i++){
					files[i]  = String.valueOf(objs[i]);
				}
				if(objs.length == 0) files = null;
				constructLanding();
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				constructLanding();
			}
		});
		
		// Add to bs
		bs.add(Box.createHorizontalGlue());
		bs.add(submit);
		bs.add(Box.createHorizontalGlue());
		bs.add(cancel);
		bs.add(Box.createHorizontalGlue());
		
		// add bs to main
		selectFile.add(bs);
		selectFile.add(Box.createVerticalGlue());
		
		// set this to content pane and revalidate
		contentPane.add(selectFile, BorderLayout.CENTER);
		contentPane.revalidate();
	}
	
	// Creates Engine Loaded panel (after return from GCP) Allows user to select what they want to do
	public void engineLoaded(){
		// Once we have loaded the engine (constructed II), we give the user the option to Search or Top-N
		
		// Clear the window
		contentPane.removeAll();
		
		// Create the base panel
		JPanel loaded = new JPanel();
		loaded.setLayout(new BoxLayout(loaded, BoxLayout.Y_AXIS));
		loaded.setAlignmentX(Component.CENTER_ALIGNMENT);
		loaded.setPreferredSize(new Dimension(500, 300));
		
		// Create Labels
		JLabel title = new JLabel("Engine Loaded Successfully. Inverted Indices Constructed");
		JLabel title2 = new JLabel("Please Select your Action:");
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		title2.setAlignmentX(Component.CENTER_ALIGNMENT);
		loaded.add(Box.createVerticalGlue());
		loaded.add(title);
		loaded.add(Box.createVerticalGlue());		
		loaded.add(title2);
		loaded.add(Box.createVerticalGlue());		
		
		// Create 2 buttons - Search For Term, and Top-N
		JButton search = new JButton("Search For Term");
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchForTerm();
			}
		});
		search.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton topN = new JButton("Top-N");
		topN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performTopN();
			}
		});
		topN.setAlignmentX(Component.CENTER_ALIGNMENT);
		loaded.add(search);
		loaded.add(Box.createVerticalGlue());
		loaded.add(topN);
		loaded.add(Box.createVerticalGlue());
		
		// set this to content pane and revalidate
		contentPane.add(loaded, BorderLayout.CENTER);
		contentPane.revalidate();
	}
	
	
	// page for entering search term
	public void searchForTerm(){
		// This page allows us to enter the Term that we will search our II for
		
		// Clear the window
		contentPane.removeAll();
		
		// base panel
		JPanel enterTerm = new JPanel();
		enterTerm.setLayout(new BoxLayout(enterTerm, BoxLayout.Y_AXIS));
		enterTerm.setAlignmentX(Component.CENTER_ALIGNMENT);
		enterTerm.setPreferredSize(new Dimension(500, 300));
		
		// Label
		JLabel title = new JLabel("Enter Your Desired Search Term (Strings only)");
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		enterTerm.add(Box.createVerticalGlue());
		enterTerm.add(title);
		enterTerm.add(Box.createVerticalGlue());
		
		// Text Field
		final JTextField term = new JTextField();
		term.setAlignmentX(Component.CENTER_ALIGNMENT);
		term.setMaximumSize(new Dimension(100, 50));
		enterTerm.add(term);
		enterTerm.add(Box.createVerticalGlue());
		
		// Submit buttons
		JButton search = new JButton("Search");
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = term.getText();
				
				// Send this string to GCP for Term search of II
				// SEND
				
				// Wait for return data
				// wait
				// results = wait();
				
				// Load Results
				searchResults(s);
			}
		});
		search.setAlignmentX(Component.CENTER_ALIGNMENT);
		enterTerm.add(search);
		enterTerm.add(Box.createVerticalGlue());
		
		// set this to content pane and revalidate
		contentPane.add(enterTerm, BorderLayout.CENTER);
		contentPane.revalidate();
	}
	
	// page for displaying results of Search Term
	public void searchResults(String s){
		// Clear the window
		contentPane.removeAll();
		
		// base panel
		JPanel allResults = new JPanel();
		allResults.setLayout(new BoxLayout(allResults, BoxLayout.Y_AXIS));
		allResults.setAlignmentX(Component.CENTER_ALIGNMENT);
		allResults.setPreferredSize(new Dimension(500, 300));
		allResults.add(Box.createVerticalGlue());
		
		// top horz panel
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// top panel two terms
		JPanel topInfo = new JPanel();
		topInfo.setLayout(new BoxLayout(topInfo, BoxLayout.Y_AXIS));
		topInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// top labels
		JLabel term = new JLabel("You searched for the term: " + s);
		JLabel time = new JLabel("Your search was executed in XXX ms");
		topInfo.add(Box.createVerticalGlue());
		topInfo.add(term);
		topInfo.add(time);
		topInfo.add(Box.createVerticalGlue());
		
		top.add(Box.createHorizontalGlue());
		top.add(topInfo);
		top.add(Box.createHorizontalGlue());
		
		// Return button
		JButton ret = new JButton("Back");
		ret.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engineLoaded();
			}
		});
		ret.setAlignmentX(Component.CENTER_ALIGNMENT);
		top.add(ret);
		top.add(Box.createHorizontalGlue());
		allResults.add(top);
		allResults.add(Box.createVerticalGlue());
		
		// bottom panel (table)
		JPanel bot = new JPanel();
		bot.setLayout(new BoxLayout(bot, BoxLayout.X_AXIS));
		bot.setAlignmentX(Component.CENTER_ALIGNMENT);
		allResults.add(bot);
		allResults.add(Box.createVerticalGlue());
		
		// at top, TERM
		// SEARCH TIME IN MS
		
		// table in a scroll
		// DOC ID, DOC FOLDER, DOC NAME, FREQ
		
		
		// set this to content pane and revalidate
		contentPane.add(allResults, BorderLayout.CENTER);
		contentPane.revalidate();
	}
	
	
	// page for entering Top-N value
	public void performTopN(){
		// This page allows us to enter the N value to perform Top-N with on our documents
		
		// Clear the window
		contentPane.removeAll();
		
		// base panel
		JPanel enterN = new JPanel();
		enterN.setLayout(new BoxLayout(enterN, BoxLayout.Y_AXIS));
		enterN.setAlignmentX(Component.CENTER_ALIGNMENT);
		enterN.setPreferredSize(new Dimension(500, 300));
		
		// Label
		JLabel title = new JLabel("Enter Your Desired N Value (Integers only)");
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		enterN.add(Box.createVerticalGlue());
		enterN.add(title);
		enterN.add(Box.createVerticalGlue());
		
		// Text Field
		final JTextField term = new JTextField();
		term.setAlignmentX(Component.CENTER_ALIGNMENT);
		term.setMaximumSize(new Dimension(100, 50));
		enterN.add(term);
		enterN.add(Box.createVerticalGlue());
		
		// Submit buttons
		JButton search = new JButton("Search");
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = term.getText();
				
				// Send this string to GCP for Top-N of files
				// SEND
				
				// Wait for return data
				// wait
				// results = wait();
				
				// Load Results
				topNResults(s);
			}
		});
		search.setAlignmentX(Component.CENTER_ALIGNMENT);
		enterN.add(search);
		enterN.add(Box.createVerticalGlue());
		
		
		// set this to content pane and revalidate
		contentPane.add(enterN, BorderLayout.CENTER);
		contentPane.revalidate();
	}
	
	// page for displaying results of Top-N
	public void topNResults(String s){
		// Clear the window
		contentPane.removeAll();
		
		// base panel
		JPanel topnResults = new JPanel();
		topnResults.setLayout(new BoxLayout(topnResults, BoxLayout.Y_AXIS));
		topnResults.setAlignmentX(Component.CENTER_ALIGNMENT);
		topnResults.setPreferredSize(new Dimension(500, 300));
		
		// top panel
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// top label and back button
		JLabel term = new JLabel("Top-" + s + " Frequent Terms:");
		top.add(Box.createHorizontalGlue());
		top.add(term);
		top.add(Box.createHorizontalGlue());
		
		// Return button
		JButton ret = new JButton("Back");
		ret.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engineLoaded();
			}
		});
		ret.setAlignmentX(Component.CENTER_ALIGNMENT);
		top.add(ret);
		top.add(Box.createHorizontalGlue());
		
		// bottom panel
		JPanel bot = new JPanel();
		bot.setLayout(new BoxLayout(bot, BoxLayout.X_AXIS));
		bot.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//table
		// scrollable panel for table
		// TERM, FREQ
		// from MOST to least (of top n)
		
		topnResults.add(Box.createVerticalGlue());
		topnResults.add(top);
		topnResults.add(Box.createVerticalGlue());
		topnResults.add(bot);
		topnResults.add(Box.createVerticalGlue());
		
		// set this to content pane and revalidate
		contentPane.add(topnResults, BorderLayout.CENTER);
		contentPane.revalidate();
	}
	
	
	public static void main(String args[]){
		finalui app = new finalui();
	}
}