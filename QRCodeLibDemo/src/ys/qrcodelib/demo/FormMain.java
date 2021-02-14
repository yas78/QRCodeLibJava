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

import ys.qrcode.Constants;
import ys.qrcode.ErrorCorrectionLevel;
import ys.qrcode.Symbol;
import ys.qrcode.Symbols;

@SuppressWarnings("serial")
public class FormMain extends JFrame {
    public FormMain() {
        setTitle("QR Code");
        initializeComponent();
    }

    private void updateQRCodePanel() {
        btnSave.setEnabled(false);
        qrcodePanel.removeAll();
        qrcodePanel.repaint();

        if (txtData.getText() == null || txtData.getText().isEmpty()) {
            return;
        }

        ErrorCorrectionLevel ecLevel = (ErrorCorrectionLevel) cmbErrorCorrectionLevel.getSelectedItem();
        int version = (int) cmbMaxVersion.getSelectedItem();
        boolean allowStructuredAppend = chkStructuredAppend.isSelected();
        Charset charset = (Charset) cmbCharset.getSelectedItem();

        Symbols symbols = new Symbols(ecLevel, version, allowStructuredAppend, charset.name());

        try {
            symbols.appendText(txtData.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }

        for (Symbol symbol : symbols) {
            Image image = symbol.getImage((int) numSpinner.getValue());
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
                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(false);
                FileFilter filterMonochrome = new FileNameExtensionFilter("Monochrome Bitmap (*.bmp)", "bmp");
                FileFilter filterColor = new FileNameExtensionFilter("24-bit Bitmap (*.bmp)", "bmp");
                FileFilter filterSvg = new FileNameExtensionFilter("SVG (*.svg)", "svg");

                chooser.addChoosableFileFilter(filterMonochrome);
                chooser.addChoosableFileFilter(filterColor);
                chooser.addChoosableFileFilter(filterSvg);

                if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File file = chooser.getSelectedFile();
                String baseName = new File(file.getParent(), FileUtil.getFileNameWithoutExtension(file.getName())).getPath();

                FileFilter filter = chooser.getFileFilter();
                boolean isMonochrome = (filter == filterMonochrome);
                String ext;

                if (filter == filterMonochrome) {
                    ext = ".bmp";
                } else if (filter == filterColor) {
                    ext = ".bmp";
                } else if ( filter == filterSvg) {
                    ext = ".svg";
                } else {
                    ext = null;
                }

                ErrorCorrectionLevel ecLevel = (ErrorCorrectionLevel) cmbErrorCorrectionLevel.getSelectedItem();
                int version = (int) cmbMaxVersion.getSelectedItem();
                boolean allowStructuredAppend = chkStructuredAppend.isSelected();
                Charset charset = (Charset) cmbCharset.getSelectedItem();
                int moduleSize = (int) numSpinner.getValue();

                Symbols symbols = new Symbols(ecLevel, version, allowStructuredAppend, charset.name());

                try {
                    symbols.appendText(txtData.getText());
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

                    switch (ext.toLowerCase()) {
                    case ".bmp":
                        symbols.get(i).saveBitmap(filename + ext, moduleSize, isMonochrome);
                        break;
                    case ".svg":
                        symbols.get(i).saveSvg(filename + ext, moduleSize);

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
        this.chkStructuredAppend.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        this.chkStructuredAppend.setSelected(false);
        this.chkStructuredAppend.addActionListener(action());

        // lblData
        this.lblData = new JLabel("Data :");
        this.lblData.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));

        // txtData
        this.txtData = new JTextArea(0, 0);
        this.txtData.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        this.txtData.setTabSize(4);
        this.txtData.getDocument().addDocumentListener(txtData_DocumentUpdate());
        this.scrollTxtData = new JScrollPane(this.txtData);
        this.scrollTxtData.setBorder(null);
        this.scrollTxtData.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // lblMaxVersion
        this.lblMaxVersion = new JLabel("Max Version :");
        this.lblMaxVersion.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));

        // cmbMaxVersion
        this.cmbMaxVersion = new JComboBox<Integer>();
        this.cmbMaxVersion.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        for (int i = Constants.MIN_VERSION; i <= Constants.MAX_VERSION; i++) {
            this.cmbMaxVersion.addItem(i);
        }
        this.cmbMaxVersion.setSelectedIndex(this.cmbMaxVersion.getItemCount() - 1);
        this.cmbMaxVersion.addActionListener(action());

        // numSpinner
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(5, 2, 100, 1);
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
        this.cmbCharset.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        this.cmbCharset.addItem(Charset.forName("Shift_JIS"));
        this.cmbCharset.addItem(Charset.forName("UTF-8"));
        this.cmbCharset.setSelectedItem(Charset.forName("Shift_JIS"));
        this.cmbCharset.addActionListener(action());

        // btnSave
        this.btnSave = new JButton("Save");
        this.btnSave.setFont(new Font("MS UI Gothic", Font.PLAIN, 13));
        this.btnSave.setEnabled(false);
        this.btnSave.addActionListener(btnSave_actionPerformed());

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout
                .createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout
                                .createParallelGroup(Alignment.LEADING).addGroup(
                                        groupLayout.createSequentialGroup().addGap(9)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(this.lblErrorCorrectionLevel,
                                                                        GroupLayout.PREFERRED_SIZE, 143,
                                                                        GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(this.lblMaxVersion))
                                                .addGap(6)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(this.cmbMaxVersion, GroupLayout.PREFERRED_SIZE,
                                                                48, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(this.cmbErrorCorrectionLevel,
                                                                GroupLayout.PREFERRED_SIZE, 48,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(groupLayout.createSequentialGroup().addGap(26)
                                                                .addComponent(this.lblCharset))
                                                        .addGroup(groupLayout.createSequentialGroup().addGap(18)
                                                                .addComponent(this.chkStructuredAppend,
                                                                        GroupLayout.PREFERRED_SIZE, 132,
                                                                        GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                                        .addGroup(groupLayout.createSequentialGroup().addGap(31)
                                                                .addComponent(this.lblModuleSize,
                                                                        GroupLayout.PREFERRED_SIZE, 83,
                                                                        GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(this.numSpinner,
                                                                        GroupLayout.PREFERRED_SIZE, 46,
                                                                        GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.RELATED,
                                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(this.btnSave, GroupLayout.PREFERRED_SIZE,
                                                                        103, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(groupLayout.createSequentialGroup().addGap(3)
                                                                .addComponent(this.cmbCharset,
                                                                        GroupLayout.PREFERRED_SIZE, 289,
                                                                        GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(ComponentPlacement.RELATED, 20, Short.MAX_VALUE))
                                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                .addComponent(this.scrollQrcodePanel, GroupLayout.DEFAULT_SIZE, 656,
                                                        Short.MAX_VALUE)
                                                .addComponent(this.scrollTxtData, GroupLayout.DEFAULT_SIZE, 656,
                                                        Short.MAX_VALUE))))
                        .addGap(16))
                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                        .addComponent(this.lblData, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(622, Short.MAX_VALUE)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                        .addComponent(this.scrollQrcodePanel, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(this.lblData)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(this.scrollTxtData, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(this.cmbErrorCorrectionLevel, GroupLayout.PREFERRED_SIZE, 21,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(this.lblErrorCorrectionLevel)
                                .addComponent(this.cmbCharset, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(this.lblCharset))
                        .addGap(8)
                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblMaxVersion)
                                .addComponent(this.cmbMaxVersion, GroupLayout.PREFERRED_SIZE, 21,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(this.numSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(this.lblModuleSize)
                                .addComponent(this.chkStructuredAppend, GroupLayout.PREFERRED_SIZE, 17,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(this.btnSave, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
                        .addGap(8)));
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
