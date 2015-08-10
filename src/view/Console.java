package view;

import glacier.ArchiveOperation;
import glacier.Vault;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.almworks.sqlite4java.SQLiteException;

import model.Archive;
import dao.SqliteDAO;

public class Console extends JFrame{
    private JLabel mStatusLabel;
    private JPanel mMainPanel;
    private JPanel mPanelTop;  //panel for file chooser
    private JPanel mPanelArchives;  //panel for displaying image
    private JPanel mPanelInfo; //panel for displaying information
    private JPanel mPanelControl; //panel for buttons
    private JTextField mTextBox; //text field for coordinates for current image
    private JTextField mFileField;
    private JTextArea mTextArea; //text area for all image files
    private JButton downloadButton;
    private JButton uploadButton;
    private JButton quitButton;
    private JList<String> mArchiveList;
    private List<String> selectedFiles;
    private List<Tuple<String>> selectedIds;
	private static String region = "us-west-2";
	private static String vaultName = "2014_Digital_Democracy";
	private List<Archive> archives;
	private JComboBox<String> mVaultComboBox;
	private JComboBox<String> mRegionComboBox;
	
	private class Tuple<T> {
		List<T> members;
		public Tuple() {
			members = new ArrayList<T>();
		}
		public T get(int i) {
			// TODO Auto-generated method stub
			return this.members.get(i);
		}
		public void add(T member) {
			// TODO Auto-generated method stub
			members.add(member);
		}
	}
	
    
	public Console() {
		super();
		initUI();
	}
	
	private void initUI() {
		mMainPanel = new JPanel();
		mFileField = new JTextField(50);
		mPanelTop = prepareFileField(mFileField, "Select Files");
		mPanelArchives = new JPanel();
		mPanelInfo = new JPanel();
		mPanelControl = new JPanel();
		
		String[] vaults = new String[] {"2014_Digital_Democracy", "2015_Digital_Democracy"};
		mVaultComboBox = new JComboBox<>(vaults);
		String[] regions = new String[] {"us-west-2"};
		mRegionComboBox = new JComboBox<>(regions);
		mPanelTop.add(mRegionComboBox);
		mPanelTop.add(mVaultComboBox);
		
		JLabel archiveListLabel = new JLabel("Uploaded Files");
		mPanelArchives.add(archiveListLabel);
		Object[] ids = getArchiveList();
		mArchiveList = createArchiveList(ids);
		
		mStatusLabel = new JLabel();
		//mTextBox = new JTextField(50);
		mTextArea = new JTextArea(10,50);
		JScrollPane scTextArea = new JScrollPane(mTextArea);
		
		//mPanelTop.setBackground(Color.RED);
		
		//mPanelArchives.setBackground(Color.BLUE);
		mPanelArchives.add(mArchiveList);
		mPanelInfo.setLayout(new BoxLayout(mPanelInfo, BoxLayout.PAGE_AXIS));
		//mPanelInfo.setBackground(Color.GREEN);
		mPanelInfo.add(mStatusLabel);
		//mPanelInfo.add(mTextBox);
		mPanelInfo.add(scTextArea);
		//mPanelControl.setBackground(Color.BLACK);
		
		mMainPanel.setLayout(new BoxLayout(mMainPanel, BoxLayout.PAGE_AXIS));

		mMainPanel.add(mPanelTop);
		mMainPanel.add(mPanelArchives);
		mMainPanel.add(mPanelInfo);
		mMainPanel.add(mPanelControl);
		JScrollPane scrollpane = new JScrollPane(mMainPanel);
		getContentPane().add(scrollpane);
		

		quitButton = new JButton("Quit");

		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		
		uploadButton = new JButton("Upload");
		uploadButton.setEnabled(false);

		uploadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setEnabledButtons(false);
				vaultName = (String) mVaultComboBox.getSelectedItem();
				region = (String) mRegionComboBox.getSelectedItem();
				Vault vault = new Vault(region, vaultName);
				ArchiveOperation archiveOperation = new ArchiveOperation(vault);
				SqliteDAO conn = null;
				try {
					String homeDir = System.getProperty("user.home");
					System.out.println(homeDir);
					conn = SqliteDAO.connect(homeDir + "/.db/archives");
					conn.open();
					for (String fileName : selectedFiles) {
						File file = new File(fileName);
						String archiveId = archiveOperation.upload(fileName);
						System.out.println(archiveId + " = " + fileName);
						mTextArea.append(fileName + " => " + archiveId + "\n");
						Archive archive = new Archive(archiveId,fileName,vaultName,file.length(),System.currentTimeMillis());
						conn.insertArchive(archive);
					}
					msgbox(Integer.toString(selectedFiles.size()) + " files uploaded.");
				} catch (Exception e){
					e.printStackTrace();
					msgbox(e.toString());
				} finally {
					conn.close();
					setEnabledButtons(true);
				}
				updateArchiveList();
			}
		});

		downloadButton = new JButton("Download");
		downloadButton.setEnabled(false);

		downloadButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				setEnabledButtons(false);
				region = (String) mRegionComboBox.getSelectedItem();
				selectedIds = getSelectedIds();
				SqliteDAO conn = null;
				try {
					String homeDir = System.getProperty("user.home");
					System.out.println(homeDir);
					conn = SqliteDAO.connect(homeDir + "/.db/archives");
					conn.open();
					for (Tuple<String> archiveId : selectedIds) {
						File f = new File(archiveId.get(2));
						String filename = f.getName();
						String vName = archiveId.get(1);
						Vault vault = new Vault(region, vName);
						ArchiveOperation archiveOperation = new ArchiveOperation(vault);
						String path = homeDir+File.separator+filename;
						int result = archiveOperation.download(archiveId.get(0),path);
						mTextArea.append(archiveId + " : status=" + Integer.toString(result) + "\n");
					}
					msgbox(Integer.toString(selectedIds.size()) + " files downloaded.");
				} catch (Exception e){
					e.printStackTrace();
					msgbox(e.toString());
				} finally {
					conn.close();
					setEnabledButtons(true);
				}
			}
		});
		
		mArchiveList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (mArchiveList.getSelectedIndex() == -1) {
			        //No selection, disable fire button.
					downloadButton.setEnabled(false);

			    } else {
			        //Selection, enable the fire button.
			        	downloadButton.setEnabled(true);
			    }
			}});


		mPanelControl.setLayout(new FlowLayout(FlowLayout.LEADING));
		mPanelControl.add(uploadButton);		
		mPanelControl.add(downloadButton);
		mPanelControl.add(quitButton);

		setTitle("Glacier Tool");
		setSize(1224, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}
	
	/*
	 * Creates and returns a JPanel containing sub components that make up the
	 * input file selection section
	 */
	private JPanel prepareFileField(JTextField fileField, String label) {
		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout(FlowLayout.LEADING));

		panel.add(new JLabel(label));
		panel.add(fileField);
		panel.add(prepareBrowseButton(fileField));

		return panel;
	}

	/*
	 * Creates and returns a JButton that can be used to browse for any given
	 * file. The input JTextField is associated with the returned button such
	 * that when the browse button is used to select a file, the full file name
	 * is written to the input JTextField
	 */
	private JButton prepareBrowseButton(final JTextField fileField) {
		JButton fileBrowse = new JButton("Browse");

		fileBrowse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String homeDir = System.getProperty("user.home");
				JFileChooser chooser = new JFileChooser(homeDir);
				chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(chooser);

				if (returnVal == JFileChooser.CANCEL_OPTION) {
					System.out.println("cancelled");
				}

				else if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = chooser.getSelectedFiles();
					String fileText = "Selected " + files.length + " files";
					//fileField.setText(fastaFile.getAbsolutePath());
					System.out.println("fasta file length: " + files.length);
					System.out.println(files[0].getAbsolutePath());
					selectedFiles = new ArrayList<String>();
					for (File filename : files) {
						selectedFiles.add(filename.getAbsolutePath());
					}
					fileField.setText(fileText);
					if(selectedFiles.size() > 0) {
						uploadButton.setEnabled(true);
					}
				}

				else {
					System.out.println("Encountered Unknown Error");
					System.exit(0);
				}
			}
		});

		return fileBrowse;
	}
	
	private JList createArchiveList(Object[] data) {
		JList list = new JList(data);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(80, 50));		
		return list;
	}
	
	private Object[] getArchiveList() {
		List<Object> archiveIds = new ArrayList<Object>();
		SqliteDAO conn = null;
		try {
			String homeDir = System.getProperty("user.home");
			System.out.println(homeDir);
			conn = SqliteDAO.connect(homeDir + "/.db/archives");
			conn.open();
			this.archives = conn.retrieveArchives(null);
			archiveIds.addAll(archives);
			
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			msgbox(e.toString());
			e.printStackTrace();
			
		} finally {
			conn.close();
		}
		return archiveIds.toArray();
	}
	
	private List<Tuple<String>> getSelectedIds() {
		int[] vals = mArchiveList.getSelectedIndices();//.getSelectedValuesList();
		List<Tuple<String>> archiveIds = new ArrayList<Tuple<String>>();
		for(int val:vals) {
			Tuple<String> tuple = new Tuple<String>();
			String id = this.archives.get(val).getArchiveId();
			tuple.add(id);
			String vault = this.archives.get(val).getVault();
			tuple.add(vault);
			String path = this.archives.get(val).getFileName();
			tuple.add(path);
			archiveIds.add(tuple);
		}
		return archiveIds;
	}
	
	private void setEnabledButtons(boolean mode) {
		this.downloadButton.setEnabled(mode);
		this.uploadButton.setEnabled(mode);
		if (mode) {
			this.mMainPanel.setCursor(Cursor.getDefaultCursor());
		} else {
			this.mMainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}
	
	private void updateArchiveList() {
		Object[] data = getArchiveList();
		String[] ids = new String[data.length];
		for(int i=0;i < data.length;i++){
			ids[i] = data[i].toString();
		}
		this.mArchiveList.setListData(ids);
	}
	
	private void msgbox(String s){
		JOptionPane.showMessageDialog(null, s);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Console console = new Console();
                console.setVisible(true);
            }
        });
	}

}
