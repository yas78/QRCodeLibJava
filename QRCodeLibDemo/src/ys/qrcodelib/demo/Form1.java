package ys.qrcodelib.demo;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ys.qrcode.ErrorCorrectionLevel;
import ys.qrcode.Symbol;
import ys.qrcode.Symbols;

@SuppressWarnings("serial")
public class Form1 extends JFrame {
    public Form1() {
        initializeComponent();
    }

    private void updateQRCodePanel() {
        btnSave.setEnabled(false);
        qrcodePanel.removeAll();
        qrcodePanel.repaint();

        if (txtData.getText() == null || txtData.getText().isEmpty()) {
            return;
        }

        int version = (int) cmbMaxVersion.getSelectedItem();
        ErrorCorrectionLevel ecLevel = (ErrorCorrectionLevel) cmbErrorCorrectionLevel.getSelectedItem();
        boolean allowStructuredAppend = chkStructuredAppend.isSelected();
        Charset charset = (Charset) cmbCharset.getSelectedItem();

        Symbols symbols = new Symbols(version, ecLevel, allowStructuredAppend, charset);

        try {
            symbols.appendString(txtData.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }

        for (Symbol symbol : symbols) {
            Image image = symbol.get1bppImage((int) numSpinner.getValue());
            ImageIcon icon = new ImageIcon(image);
            JLabel label = new JLabel();
            label.setIcon(icon);
            label.setSize(image.getWidth(null), image.getHeight(null));
            qrcodePanel.add(label);
            qrcodePanel.validate();
            scrollQrcodePanel.repaint();
            this.revalidate();
        }

        btnSave.setEnabled(true);
    }

    private ActionListener btnSave_actionPerformed() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String baseName;
                boolean isMonochrome;

                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(false);
                FileFilter filtermonochrome = new FileNameExtensionFilter("Monochrome Bitmap", "bmp");
                FileFilter filterColor = new FileNameExtensionFilter("24-bit Bitmap", "bmp");

                chooser.addChoosableFileFilter(filtermonochrome);
                chooser.addChoosableFileFilter(filterColor);

                if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File file = chooser.getSelectedFile();
                isMonochrome = chooser.getFileFilter().equals(filtermonochrome);
                baseName = new File(file.getParent(), FileUtil.getFileNameWithoutExtension(file.getName())).getPath();

                int version = (int) cmbMaxVersion.getSelectedItem();
                ErrorCorrectionLevel ecLevel = (ErrorCorrectionLevel) cmbErrorCorrectionLevel.getSelectedItem();
                boolean allowStructuredAppend = chkStructuredAppend.isSelected();
                Charset charset = (Charset) cmbCharset.getSelectedItem();

                Symbols symbols = new Symbols(version, ecLevel, allowStructuredAppend, charset);

                try {
                    symbols.appendString(txtData.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    return;
                }

                for (int i = 0; i < symbols.getCount(); i++) {
                    String filename;

                    if (symbols.getCount() == 1) {
                        filename = baseName;
                    } else {
                        filename = baseName + "_" + String.valueOf(i + 1);
                    }

                    if (isMonochrome) {
                        symbols.get(i).save1bppDIB(filename + ".bmp", (int) numSpinner.getValue());
                    } else {
                        symbols.get(i).save24bppDIB(filename + ".bmp", (int) numSpinner.getValue());
                    }
                }
            }
        };
    }

    private ActionListener action() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateQRCodePanel();
            }
        };
    }

    private ChangeListener numSpinner_stateChanged() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateQRCodePanel();
            }
        };
    }

    private DocumentListener txtData_DocumentUpdate() {
        return new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateQRCodePanel();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateQRCodePanel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateQRCodePanel();
            }
        };
    }

    private void initializeComponent() {
        // Form1
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(700, 550);
        this.setMinimumSize(new Dimension(700, 550));
        this.setLocationRelativeTo(null);

        // qrcodePanel
        this.qrcodePanel = new JPanel();
        this.qrcodePanel.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
        this.scrollQrcodePanel = new JScrollPane(this.qrcodePanel);
        this.scrollQrcodePanel.setBorder(null);
        this.scrollQrcodePanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollQrcodePanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // lblErrorCorrectionLevel
        this.lblErrorCorrectionLevel = new JLabel("Error Correction Level :");
        this.lblErrorCorrectionLevel.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));

        // cmbErrorCorrectionLevel
        this.cmbErrorCorrectionLevel = new JComboBox<ErrorCorrectionLevel>();
        this.cmbErrorCorrectionLevel.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        for (ErrorCorrectionLevel ecLevel : ErrorCorrectionLevel.values()) {
            this.cmbErrorCorrectionLevel.addItem(ecLevel);
        }
        this.cmbErrorCorrectionLevel.setSelectedItem(ErrorCorrectionLevel.M);
        this.cmbErrorCorrectionLevel.addActionListener(action());

        // chkStructuredAppend
        this.chkStructuredAppend = new JCheckBox("Structured Append");
        this.chkStructuredAppend.addActionListener(action());
        this.chkStructuredAppend.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));

        // lblData
        this.lblData = new JLabel("Data :");

        // txtData
        this.txtData = new JTextArea(0, 0);
        this.txtData.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        this.txtData.setTabSize(4);
        this.txtData.getDocument().addDocumentListener(txtData_DocumentUpdate());
        this.scrollTxtData = new JScrollPane(this.txtData);
        this.scrollTxtData.setBorder(null);
        this.scrollTxtData.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // lblMaxVersion
        this.lblMaxVersion = new JLabel("Max Mersion :");
        this.lblMaxVersion.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));

        // cmbMaxVersion
        this.cmbMaxVersion = new JComboBox<Integer>();
        this.cmbMaxVersion.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        for (int i = 1; i <= 40; i++) {
            this.cmbMaxVersion.addItem(i);
        }
        this.cmbMaxVersion.setSelectedIndex(this.cmbMaxVersion.getItemCount() - 1);
        this.cmbMaxVersion.addActionListener(action());

        // numSpinner
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(5, 1, 100, 1);
        this.numSpinner = new JSpinner(spinnerNumberModel);
        this.numSpinner.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        this.numSpinner.addChangeListener(numSpinner_stateChanged());

        // lblModuleSize
        this.lblModuleSize = new JLabel("Module Size :");
        this.lblModuleSize.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));

        // lblCharset
        this.lblCharset = new JLabel("Byte mode Charset :");
        this.lblCharset.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));

        // cmbCharset
        this.cmbCharset = new JComboBox<Charset>();
        for (Charset charset : Charset.availableCharsets().values()) {
            this.cmbCharset.addItem(charset);
        }
        this.cmbCharset.setSelectedItem(Charset.defaultCharset());
        this.cmbCharset.addActionListener(action());

        // btnSave
        this.btnSave = new JButton("Save");
        this.btnSave.addActionListener(btnSave_actionPerformed());

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(
                        groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
                                .createSequentialGroup()
                                .addGroup(groupLayout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup().addGap(9).addComponent(
                                                this.lblData, GroupLayout.PREFERRED_SIZE, 50,
                                                GroupLayout.PREFERRED_SIZE))
                                        .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout
                                                .createParallelGroup(Alignment.LEADING).addGroup(groupLayout
                                                        .createSequentialGroup().addGap(9).addGroup(groupLayout
                                                                .createParallelGroup(Alignment.LEADING)
                                                                .addGroup(groupLayout.createSequentialGroup()
                                                                        .addComponent(this.lblMaxVersion,
                                                                                GroupLayout.PREFERRED_SIZE, 83,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(72).addComponent(this.cmbMaxVersion,
                                                                                GroupLayout.PREFERRED_SIZE, 48,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(groupLayout.createSequentialGroup()
                                                                        .addComponent(this.lblErrorCorrectionLevel,
                                                                                GroupLayout.PREFERRED_SIZE, 143,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(12)
                                                                        .addComponent(this.cmbErrorCorrectionLevel,
                                                                                GroupLayout.PREFERRED_SIZE, 48,
                                                                                GroupLayout.PREFERRED_SIZE)))
                                                        .addGap(25)
                                                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                                .addGroup(groupLayout.createSequentialGroup()
                                                                        .addComponent(this.chkStructuredAppend,
                                                                                GroupLayout.PREFERRED_SIZE, 132,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(25)
                                                                        .addComponent(this.lblModuleSize,
                                                                                GroupLayout.PREFERRED_SIZE, 83,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                                        .addComponent(this.numSpinner,
                                                                                GroupLayout.PREFERRED_SIZE, 46,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(37)
                                                                        .addComponent(this.btnSave,
                                                                                GroupLayout.PREFERRED_SIZE, 103,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(0, 0, Short.MAX_VALUE))
                                                                .addGroup(groupLayout.createSequentialGroup()
                                                                        .addComponent(this.lblCharset).addPreferredGap(
                                                                                ComponentPlacement.UNRELATED)
                                                                        .addComponent(this.cmbCharset,
                                                                                GroupLayout.PREFERRED_SIZE, 303,
                                                                                GroupLayout.PREFERRED_SIZE))))
                                                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                                .addComponent(this.scrollTxtData,
                                                                        GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
                                                                .addComponent(this.scrollQrcodePanel,
                                                                        GroupLayout.DEFAULT_SIZE, 656,
                                                                        Short.MAX_VALUE))))
                                                .addPreferredGap(ComponentPlacement.RELATED)))
                                .addGap(16)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                        .addComponent(this.scrollQrcodePanel, GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE).addGap(10)
                        .addComponent(this.lblData).addGap(10)
                        .addComponent(this.scrollTxtData, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup().addGap(4)
                                                .addComponent(this.lblErrorCorrectionLevel))
                                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(this.cmbErrorCorrectionLevel, GroupLayout.PREFERRED_SIZE,
                                                        21, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(this.lblCharset).addComponent(this.cmbCharset,
                                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE)))
                        .addGap(7)
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addGroup(
                                        groupLayout.createSequentialGroup().addGap(5).addComponent(this.lblMaxVersion))
                                .addGroup(groupLayout.createSequentialGroup().addGap(1)
                                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(this.cmbMaxVersion, GroupLayout.PREFERRED_SIZE, 21,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(this.chkStructuredAppend, GroupLayout.PREFERRED_SIZE, 17,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(this.lblModuleSize)
                                                .addComponent(this.numSpinner, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(this.btnSave, GroupLayout.PREFERRED_SIZE, 23,
                                                        GroupLayout.PREFERRED_SIZE))))
                        .addGap(11)));
        getContentPane().setLayout(groupLayout);
    }

    private JPanel                          qrcodePanel;
    private JScrollPane                     scrollQrcodePanel;
    private JButton                         btnSave;
    private JComboBox<ErrorCorrectionLevel> cmbErrorCorrectionLevel;
    private JCheckBox                       chkStructuredAppend;
    private JLabel                          lblErrorCorrectionLevel;
    private JTextArea                       txtData;
    private JScrollPane                     scrollTxtData;
    private JLabel                          lblData;
    private JLabel                          lblMaxVersion;
    private JComboBox<Integer>              cmbMaxVersion;
    private JSpinner                        numSpinner;
    private JLabel                          lblModuleSize;
    private JLabel                          lblCharset;
    private JComboBox<Charset>              cmbCharset;
}
