package app;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import renderer.Renderer;

public class Settings extends JFrame implements ActionListener, ChangeListener {

    private JComboBox viewSettings, imageSettings;
    private final JSlider viewInt = new JSlider(JSlider.HORIZONTAL, 0, 100, 15);
    private final JButton openFile = new JButton("Open File (image)");
    private final JButton saveFile = new JButton("Save File (image)");
    private final JFileChooser fileChooser = new JFileChooser();
    
    private int viewS, viewI;
    private String imageS;
    private boolean reaload, custom;
 
    private String[] fileNames;
    private final String[] types = {".png",".jpg",".gif"};
    private Renderer ren;
    
    public Settings() {
        reaload = true;
        custom = false;
        createSettings();
    }
    
    private void createSettings() {
        setTitle("Settings");
        setSize(300, 300);
        
        LayoutManager m = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        setLayout(m);
	setAlwaysOnTop(true);
         
        getImages();
        addSettings();
        setValues();
        
        pack();
        setVisible(true);
    }
    
    private void getImages() {
        /* Try to look for images in project/images directory */
        try {
            // Get current file directory + add images dir
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            s += "/images";
            
            // Search directory and add images to array 
            File directory;
            List<String> fileNamesL = new ArrayList<String>();
            
            directory = new File(s);
            String[] dirlist = directory.list();

            for(String fileName: dirlist) {
                if(Arrays.asList(types).contains(fileName.substring(fileName.lastIndexOf('.'), fileName.length()))) {
                   fileNamesL.add("./images/" + fileName);
                }
            }
            fileNames = fileNamesL.toArray(new String[fileNamesL.size()]);
        } catch(Exception e) {
            System.out.println("Failed to load images from directory.");
            e.printStackTrace();
        }
    }
    
    private void addSettings() {
        String[] views = { "No effect", "Gaussian Blur", "Edge detection", "Emboss", "Sharpness", "Grayscale", "Pixelization", "Brightness" };
        
        JPanel viewPanel = new JPanel();
		viewSettings = new JComboBox(views);
		viewSettings.setSize(100, 20);
                viewSettings.addActionListener(this);
		viewPanel.add(new JLabel("View Type: "));
		viewPanel.add(viewSettings);
                
        add(viewPanel);
        
        JPanel viewIntPanel = new JPanel();
		viewInt.setSize(100, 20);
                viewInt.setMajorTickSpacing(25);
		viewInt.setPaintTicks(true);
		viewInt.setPaintLabels(true);
                viewInt.addChangeListener(this);
                
		viewIntPanel.add(new JLabel("View Intensity: "));
		viewIntPanel.add(viewInt);
                
        add(viewIntPanel);
        
        JPanel imagePanel = new JPanel();
		imageSettings = new JComboBox(fileNames);
		imageSettings.setSize(100, 20);
                imageSettings.addActionListener(this);
		imagePanel.add(new JLabel("Image: "));
		imagePanel.add(imageSettings);
                
        add(imagePanel);
             
        /* File management (load + save) */
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "gif"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        JPanel imageActionsPanel = new JPanel();
                openFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int returnVal = fileChooser.showOpenDialog(getParent());

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            imageS = fileChooser.getSelectedFile().getAbsolutePath();
                            custom = true;
                            reaload = true;
                        } else {
                            System.out.println("Error loading image.");
                        }
                    }
		});
		imageActionsPanel.add(openFile);
        
		saveFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
                            String file = fileChooser.getSelectedFile().getAbsolutePath();
                            String format = file.substring(file.lastIndexOf('.'), file.length());
                            if(Arrays.asList(types).contains(format)) {
                                ren.saveCanvas(file, format.substring(1));
                            }
                        }
                    }
		});
                imageActionsPanel.add(saveFile);
                
        add(imageActionsPanel);
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == imageSettings) { custom = false; reaload = true; }
        setValues();
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        setValues();
    }
    
    private void setValues() {
        setViewS(viewSettings.getSelectedIndex());
        if(!custom) {
            setImageS(imageSettings.getSelectedItem().toString());
        }
        setViewI(viewInt.getValue());
    }
    
    public int getViewS() {
        return viewS;
    }

    public void setViewS(int viewS) {
        this.viewS = viewS;
    }

    public int getViewI() {
        return viewI;
    }

    public void setViewI(int viewI) {
        this.viewI = viewI;
    }

    public String getImageS() {
        return imageS;
    }

    public void setImageS(String imageS) {
        this.imageS = imageS;
    }

    public boolean isReaload() {
        return reaload;
    }

    public void setReaload(boolean reaload) {
        this.reaload = reaload;
    }

    public void setRen(Renderer ren) {
        this.ren = ren;
    }
}
