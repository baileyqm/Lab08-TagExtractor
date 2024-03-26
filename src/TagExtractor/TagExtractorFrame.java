package TagExtractor;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.CREATE;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TagExtractorFrame extends JFrame{
    JPanel mainPnl = new JPanel(new BorderLayout());
    JPanel textViewerPnl, fileBarPnl,cmdPnl;
    JTextArea originalTextArea, filteredTextArea;
    JLabel fileBarLbl;
    JButton quitBtn, uploadBtn,stopWordBtn;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    Font buttonFont = new Font("Arial", Font.BOLD, 20);
    Font regFont = new Font("Arial", Font.PLAIN, 15);

    JFileChooser chooser = new JFileChooser();
    JFileChooser filterChooser = new JFileChooser();
    File selectedFile;
    File selectedStopFile;
    String rec = "";
    TreeMap<String, Integer> wordMap = new TreeMap<>();
    ArrayList<String> rawFileWords = new ArrayList<>();
    private Set<String> stopWords = new TreeSet<>();
    private ArrayList<String> filteredFileWords = new ArrayList<>();
    ArrayList<String> dataFileArray;

    public TagExtractorFrame(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Tag Extractor Program");

        add(mainPnl);

        generateFileBar();
        mainPnl.add(fileBarPnl, BorderLayout.NORTH);

        generateTextViewerPnl();
        mainPnl.add(textViewerPnl, BorderLayout.CENTER);

        generateCmdPnl();
        mainPnl.add(cmdPnl,BorderLayout.SOUTH);

        pack();
        setSize((int)(.80*screenSize.width),(int)(.80*screenSize.height));
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void generateFileBar(){
        fileBarPnl = new JPanel();
        fileBarLbl = new JLabel("Current File Selected: None");
        fileBarLbl.setFont(regFont);
        fileBarPnl.add(fileBarLbl);
    }
    private void generateTextViewerPnl(){
        textViewerPnl = new JPanel(new GridLayout(1,2));
        originalTextArea = new JTextArea("");
        originalTextArea.setFont(regFont);
        originalTextArea.setEditable(false);
        filteredTextArea = new JTextArea("");
        filteredTextArea.setFont(regFont);
        filteredTextArea.setEditable(false);
        originalTextArea.setMargin(new Insets(10,10,0,5));
        filteredTextArea.setMargin(new Insets(10,10,0,5));
        JScrollPane scroller1 = new JScrollPane(originalTextArea);
        JScrollPane scroller2 = new JScrollPane(filteredTextArea);

        textViewerPnl.add(scroller1);
        textViewerPnl.add(scroller2);

    }

    public void generateCmdPnl(){
        cmdPnl = new JPanel(new GridLayout(1,3));
        cmdPnl.setPreferredSize(new Dimension(100,50));
        uploadBtn = new JButton("Choose a Text File!");
        uploadBtn.setFont(buttonFont);
        uploadBtn.addActionListener(e ->{
            FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES (.txt)", "txt", "text");
            chooser.setFileFilter(filter);
            originalTextArea.setText("");
            try
            {
                File workingDirectory = new File(System.getProperty("user.dir")+ "/src/TagExtractor");

                chooser.setCurrentDirectory(workingDirectory);

                if(chooser.showOpenDialog(textViewerPnl) == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = chooser.getSelectedFile();
                    Path file = selectedFile.toPath();
                    InputStream in =
                            new BufferedInputStream(Files.newInputStream(file, CREATE));
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(in));
                    // Finally we can read the file LOL!
                    while(reader.ready())
                    {
                        rec = reader.readLine();
                        originalTextArea.append(rec + "\n");
                        rawFileWords.addAll(List.of(rec.toLowerCase().split("\s")));
                        for (int i = 0; i < rawFileWords.size()-1; i++) {
                            rawFileWords.set(i, rawFileWords.get(i).trim());
                        }
                    }
                    reader.close(); // must close the file to seal it and flush buffer

                    fileBarLbl.setText("Current File Selected: "+ selectedFile.getName());
                }
            }
            catch (FileNotFoundException k)
            {
                System.out.println("File not found!!!");
                k.printStackTrace();
            }
            catch (IOException k)
            {
                k.printStackTrace();
            }
        });
        cmdPnl.add(uploadBtn);

        stopWordBtn = new JButton("Select a Stop Word Filter and Continue!");
        stopWordBtn.setFont(buttonFont);
        stopWordBtn.addActionListener(e ->{
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(textViewerPnl,"Please select a file first!","Uh-oh, did you select a file?",JOptionPane.ERROR_MESSAGE);
            }else {
                FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES (.txt)", "txt", "text");
                chooser.setFileFilter(filter);
                filteredTextArea.setText("");
                    File workingStopDirectory = new File(System.getProperty("user.dir")+ "/src/TagExtractor");

                    filterChooser.setCurrentDirectory(workingStopDirectory);
                    filterChooser.setFileFilter(filter);



                    if(filterChooser.showOpenDialog(textViewerPnl) == JFileChooser.APPROVE_OPTION)
                    {
                        selectedStopFile = filterChooser.getSelectedFile();
                        Path stopFilePath = selectedStopFile.toPath();

                        try(Stream<String> lines = Files.lines(stopFilePath))
                        {
                             stopWords = lines.collect(Collectors.toSet());
                        }
                        catch (IOException r)
                        {
                            System.out.println("Failed to open the stop word file");
                            r.printStackTrace();
                        }

                        filteredFileWords.clear();

                        for (int textFileInx = 0; textFileInx < rawFileWords.size()-1; textFileInx++) {

                            String w = rawFileWords.get(textFileInx).toLowerCase();
                            w = w.replaceAll("_", " ");
                            w = w.replaceAll("--", " ");
                            w = w.replaceAll("-", " ");

                            if(w.contains(" "))
                            {
                                filteredFileWords.addAll(List.of(w.split(" ")));
                            }

                            w = w.replaceAll("[\\W]", "");
                            w = w.replaceAll("[\\d]", "");
                            w = w.replaceAll("[^a-zA-Z0-9]", "");
                            //w= w.replaceAll("\"", "");
                            // w= w.replaceAll("\\*", "").trim();
                            w = w.trim();


                            if((!stopWords.contains(w))){
                                filteredFileWords.add(w);
                            }
                        }

                        wordMap = new TreeMap<>();

                        for(String s :filteredFileWords){
                            if(!s.isEmpty()){
                                wordMap.merge(s, 1,(oldValue, newValue) -> oldValue + newValue);
                            }
                        }

                        dataFileArray = new ArrayList<>();

                        for (String k : wordMap.keySet()){
                            dataFileArray.add("\""+ k +"\"" + " occurs " + wordMap.get(k) + " times\n");
                            filteredTextArea.append( "\""+ k +"\"" + " occurs " + wordMap.get(k) + " times\n\n");
                        }

                        //hashmap.merge(key, value, remappingFunction);
                        // prices.merge("Shirt", 100, (oldValue, newValue) -> oldValue + newValue)

                    }
            }

            if (JOptionPane.showConfirmDialog(textViewerPnl,"Would you like to save the extracted tags?","Would You Like to Save?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0){

                File workingDirectory = new File(System.getProperty("user.dir"));
                Path file = Paths.get(workingDirectory.getPath() + "/src/TagExtractor/ExtractedTags.txt");
                try
                {
                    // Typical java pattern of inherited classes
                    // we wrap a BufferedWriter around a lower level BufferedOutputStream
                    OutputStream out =
                            new BufferedOutputStream(Files.newOutputStream(file, CREATE));
                    BufferedWriter writer =
                            new BufferedWriter(new OutputStreamWriter(out));

                    // Finally can write the file LOL!

                    for(String rec : dataFileArray)
                    {
                        writer.write(rec, 0, rec.length());  // stupid syntax for write rec
                        // 0 is where to start (1st char) the write
                        // rec. length() is how many chars to write (all)
                        writer.newLine();  // adds the new line

                    }
                    writer.close(); // must close the file to seal it and flush buffer
                }
                catch (IOException p)
                {
                    p.printStackTrace();
                }


            }

        });

        cmdPnl.add(stopWordBtn);


        quitBtn = new JButton("Quit");
        quitBtn.setFont(buttonFont);
        quitBtn.addActionListener(e -> System.exit(0));
        cmdPnl.add(quitBtn);
    }

}

