/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
revisions:
    Add setOpaque true for JButtons, and use getCrossPlatformLookAndFeelClassName
    to set default look and feel (fix appearance in Mac OSX)
    3/28/2020 - Add ability to load and save
*/
package m6800;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import java.awt.*;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.*;
import javax.swing.UnsupportedLookAndFeelException;
/**
 *
 * @author daves
 */
public class UI extends JFrame implements ActionListener, Runnable {
    JButton ResetButton;
    JButton ZeroButton;
    JButton OneButton;
    JButton TwoButton;
    JButton ThreeButton;
    JButton FourButton;
    JButton FiveButton;
    JButton SixButton;
    JButton SevenButton;
    JButton EightButton;
    JButton NineButton;
    JButton AButton;
    JButton BButton;
    JButton CButton;
    JButton DButton;
    JButton EButton;
    JButton FButton;
    JButton NMIButton;
    JButton IRQButton;
    JLabel HLabel;
    JLabel ILabel;
    JLabel NLabel;
    JLabel ZLabel;
    JLabel VLabel;
    JLabel CLabel;
    
    private final CPU myCPU;
    private final MemoryModule myMemoryModule;
    private final Thread runner;
    
    sevenSegmentDisplay HDisplay;
    sevenSegmentDisplay IDisplay;
    sevenSegmentDisplay NDisplay;
    sevenSegmentDisplay ZDisplay;
    sevenSegmentDisplay VDisplay;
    sevenSegmentDisplay CDisplay;
    
    final static int WINDOWWIDTH = 500;
    final static int WINDOWHEIGHT = 525;
    
    final static int DISPLAYWIDTH = 60;
    final static int DISPLAYSPACEX = 20;
    final static int DISPLAYHEIGHT = 100;
    final static int DISPLAYSTARTX = 10;
    final static int DISPLAYSTARTY = 10;
    
    final static int BUTTONWIDTH  = 50;
    final static int BUTTONHEIGHT = 50;
    final static int BUTTONMARGIN = 2;
    
    final static int BOTTOMPAD = 75;
    
    final static int XCENTER = (WINDOWWIDTH/2);
    
    
    final static int ZEROBUTTONSTARTX = (XCENTER-BUTTONWIDTH);
    final static int ZEROBUTTONSTARTY = WINDOWHEIGHT-BUTTONHEIGHT-BOTTOMPAD;
    final static int RESETBUTTONSTARTX = XCENTER;
    final static int RESETBUTTONSTARTY = WINDOWHEIGHT-BUTTONHEIGHT-BOTTOMPAD;
    final static int ONEBUTTONSTARTX = (XCENTER - (3 * BUTTONWIDTH / 2));
    final static int ONEBUTTONSTARTY = WINDOWHEIGHT-(2*BUTTONHEIGHT)-BOTTOMPAD;
    final static int TWOBUTTONSTARTX = XCENTER - (BUTTONWIDTH/2);
    final static int TWOBUTTONSTARTY = WINDOWHEIGHT-(2*BUTTONHEIGHT)-BOTTOMPAD;
    final static int THREEBUTTONSTARTX = XCENTER + (BUTTONWIDTH/2);
    final static int THREEBUTTONSTARTY = WINDOWHEIGHT-(2*BUTTONHEIGHT)-BOTTOMPAD;
    final static int FOURBUTTONSTARTX = XCENTER - (3 * BUTTONWIDTH / 2);
    final static int FOURBUTTONSTARTY = WINDOWHEIGHT-(3*BUTTONHEIGHT)-BOTTOMPAD;
    final static int FIVEBUTTONSTARTX = XCENTER - (BUTTONWIDTH / 2);
    final static int FIVEBUTTONSTARTY = WINDOWHEIGHT-(3*BUTTONHEIGHT)-BOTTOMPAD;
    final static int SIXBUTTONSTARTX = XCENTER + (BUTTONWIDTH / 2);
    final static int SIXBUTTONSTARTY = WINDOWHEIGHT-(3*BUTTONHEIGHT)-BOTTOMPAD;
    final static int SEVENBUTTONSTARTX = XCENTER - (3 * BUTTONWIDTH / 2);
    final static int SEVENBUTTONSTARTY = WINDOWHEIGHT-(4*BUTTONHEIGHT)-BOTTOMPAD;
    final static int EIGHTBUTTONSTARTX = XCENTER - (BUTTONWIDTH / 2);
    final static int EIGHTBUTTONSTARTY = WINDOWHEIGHT-(4*BUTTONHEIGHT)-BOTTOMPAD;
    final static int NINEBUTTONSTARTX = XCENTER + (BUTTONWIDTH / 2);
    final static int NINEBUTTONSTARTY = WINDOWHEIGHT-(4*BUTTONHEIGHT)-BOTTOMPAD;
    final static int ABUTTONSTARTX = XCENTER - (3 * BUTTONWIDTH / 2);
    final static int ABUTTONSTARTY = WINDOWHEIGHT-(5*BUTTONHEIGHT)-BOTTOMPAD;
    final static int BBUTTONSTARTX = XCENTER - (BUTTONWIDTH / 2);
    final static int BBUTTONSTARTY = WINDOWHEIGHT-(5*BUTTONHEIGHT)-BOTTOMPAD;
    final static int CBUTTONSTARTX = XCENTER + (BUTTONWIDTH / 2);
    final static int CBUTTONSTARTY = WINDOWHEIGHT-(5*BUTTONHEIGHT)-BOTTOMPAD;
    final static int DBUTTONSTARTX = XCENTER - (3 * BUTTONWIDTH / 2);
    final static int DBUTTONSTARTY = WINDOWHEIGHT-(6*BUTTONHEIGHT)-BOTTOMPAD;
    final static int EBUTTONSTARTX = XCENTER - (BUTTONWIDTH / 2);
    final static int EBUTTONSTARTY = WINDOWHEIGHT-(6*BUTTONHEIGHT)-BOTTOMPAD;
    final static int FBUTTONSTARTX = XCENTER + (BUTTONWIDTH / 2);
    final static int FBUTTONSTARTY = WINDOWHEIGHT-(6*BUTTONHEIGHT)-BOTTOMPAD;
    final static int NMIBUTTONSTARTX = 50;
    final static int NMIBUTTONSTARTY = WINDOWHEIGHT/2;
    final static int IRQBUTTONSTARTX = 50;
    final static int IRQBUTTONSTARTY = (WINDOWHEIGHT / 2) - BUTTONHEIGHT;
    
    final static int HDISPLAYSTARTX = XCENTER - (3 * DISPLAYWIDTH + (5 * DISPLAYSPACEX / 2));
    final static int IDISPLAYSTARTX = XCENTER - (2 * DISPLAYWIDTH + (3 * DISPLAYSPACEX / 2));
    final static int NDISPLAYSTARTX = XCENTER - (DISPLAYWIDTH + DISPLAYSPACEX/2);
    final static int ZDISPLAYSTARTX = XCENTER + DISPLAYSPACEX / 2;
    final static int VDISPLAYSTARTX = XCENTER + (DISPLAYWIDTH + (3 * DISPLAYSPACEX / 2));
    final static int CDISPLAYSTARTX = XCENTER + (2*DISPLAYWIDTH + (5 * DISPLAYSPACEX / 2));
   
    final static int LABELWIDTH = 20;
    final static int LABELHEIGHT = 20;
    final static int LABELSTARTY = DISPLAYSTARTY + DISPLAYHEIGHT;
    final static int HLABELX = HDISPLAYSTARTX + DISPLAYWIDTH / 2;
    final static int ILABELX = IDISPLAYSTARTX + DISPLAYWIDTH / 2;
    final static int NLABELX = NDISPLAYSTARTX + DISPLAYWIDTH / 2;
    final static int ZLABELX = ZDISPLAYSTARTX + DISPLAYWIDTH / 2;
    final static int VLABELX = VDISPLAYSTARTX + DISPLAYWIDTH / 2;
    final static int CLABELX = CDISPLAYSTARTX + DISPLAYWIDTH / 2;

    
    final static int HDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x58;
    final static int IDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x48;
    final static int NDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x38;
    final static int ZDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x28;
    final static int VDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x18;
    final static int CDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x8;
    
    /*
    final static int HDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x50;
    final static int IDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x40;
    final static int NDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x30;
    final static int ZDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x20;
    final static int VDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x10;
    final static int CDISPLAYSTART = MemoryModule.DISPLAYSTART + 0x0;
    */
    
    
    final static int DISPLAY_ALT1 = 0x8;
    final static int DISPLAY_ALT2 = 0x80;
    final static int DISPLAY_ALT3 = 0x88;
       
    
    final int iFButtonStartX = 150;
    final int iFButtonStartY = 150;
    
    final static int REFRESHMILLISECONDS = 10;
    
    int debug;
    
    private final JMenuBar menubar;
    private final JMenu menuFile;
    private final JMenu menuHelp;
    private final JMenuItem load;
    private final JMenuItem loadROM;
    private final JMenuItem save;
    private final JMenuItem settings;
    private final JMenuItem about;
    private final JMenuItem exit;
    
    private final JOptionPane dialog;
    
    private FileDialog fileDialog;
    
    FileReader infile = null;
    FileWriter outfile = null;
    String fileName;
    
    public UI (CPU aCPU, MemoryModule aMemoryModule)
    {   
        super ("ET3400A Simulator");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
        }
        runner = new Thread(this);
        HDisplay = new sevenSegmentDisplay(HDISPLAYSTARTX, DISPLAYSTARTY);
        IDisplay = new sevenSegmentDisplay(IDISPLAYSTARTX, DISPLAYSTARTY);
        NDisplay = new sevenSegmentDisplay(NDISPLAYSTARTX, DISPLAYSTARTY);
        ZDisplay = new sevenSegmentDisplay(ZDISPLAYSTARTX, DISPLAYSTARTY);
        VDisplay = new sevenSegmentDisplay(VDISPLAYSTARTX, DISPLAYSTARTY);
        CDisplay = new sevenSegmentDisplay(CDISPLAYSTARTX, DISPLAYSTARTY);
        ResetButton = new JButton("Reset");
        ResetButton.setBounds(RESETBUTTONSTARTX, RESETBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        ResetButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        ResetButton.setBackground(Color.black);
        ResetButton.setForeground(Color.white);
        ResetButton.setOpaque(true);
        ZeroButton = new JButton("0");
        ZeroButton.setBounds(ZEROBUTTONSTARTX, ZEROBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        ZeroButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        ZeroButton.setBackground(Color.black);
        ZeroButton.setForeground(Color.white);
        ZeroButton.setOpaque(true);
        OneButton = new JButton("<HTML>ACCA<br>&nbsp;&nbsp;&nbsp;&nbsp;1</HTML>");
        OneButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        OneButton.setBounds(ONEBUTTONSTARTX, ONEBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        OneButton.setHorizontalAlignment(SwingConstants.CENTER);
        OneButton.setBackground(Color.black);
        OneButton.setForeground(Color.white);
        OneButton.setOpaque(true);
        TwoButton = new JButton("<HTML>ACCB<br>&nbsp;&nbsp;&nbsp;&nbsp;2</HTML>");
        TwoButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        TwoButton.setBounds(TWOBUTTONSTARTX, TWOBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        TwoButton.setHorizontalAlignment(SwingConstants.CENTER);
        TwoButton.setBackground(Color.black);
        TwoButton.setForeground(Color.white);
        TwoButton.setOpaque(true);
        ThreeButton = new JButton("<HTML>PC<br>&nbsp;&nbsp;3</HTML>");
        ThreeButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        ThreeButton.setBounds(THREEBUTTONSTARTX, THREEBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        ThreeButton.setHorizontalAlignment(SwingConstants.CENTER);
        ThreeButton.setBackground(Color.black);
        ThreeButton.setForeground(Color.white);
        ThreeButton.setOpaque(true);
        FourButton = new JButton("<HTML>INDEX<br>&nbsp;&nbsp;&nbsp;&nbsp;4</HTML>");
        FourButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        FourButton.setBounds(FOURBUTTONSTARTX, FOURBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        FourButton.setHorizontalAlignment(SwingConstants.CENTER);
        FourButton.setBackground(Color.black);
        FourButton.setForeground(Color.white);
        FourButton.setOpaque(true);
        FiveButton = new JButton("<HTML>CC<br>&nbsp;&nbsp;5</HTML>");
        FiveButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        FiveButton.setBounds(FIVEBUTTONSTARTX, FIVEBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        FiveButton.setHorizontalAlignment(SwingConstants.CENTER);
        FiveButton.setBackground(Color.black);
        FiveButton.setForeground(Color.white);
        FiveButton.setOpaque(true);
        SixButton = new JButton("<HTML>SP<br>&nbsp;&nbsp;6</HTML>");
        SixButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        SixButton.setBounds(SIXBUTTONSTARTX, SIXBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        SixButton.setHorizontalAlignment(SwingConstants.CENTER);
        SixButton.setBackground(Color.black);
        SixButton.setForeground(Color.white);
        SixButton.setOpaque(true);
        SevenButton = new JButton("<HTML>RTI<br>&nbsp;&nbsp;7</HTML>");
        SevenButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        SevenButton.setBounds(SEVENBUTTONSTARTX, SEVENBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        SevenButton.setHorizontalAlignment(SwingConstants.CENTER);
        SevenButton.setBackground(Color.black);
        SevenButton.setForeground(Color.white);
        SevenButton.setOpaque(true);
        EightButton = new JButton("<HTML>SS<br>&nbsp;&nbsp;8</HTML>");
        EightButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        EightButton.setBounds(EIGHTBUTTONSTARTX, EIGHTBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        EightButton.setHorizontalAlignment(SwingConstants.CENTER);
        EightButton.setBackground(Color.black);
        EightButton.setForeground(Color.white);
        EightButton.setOpaque(true);
        NineButton = new JButton("<HTML>BR<br>&nbsp;&nbsp;9</HTML>");
        NineButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        NineButton.setBounds(NINEBUTTONSTARTX, NINEBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        NineButton.setHorizontalAlignment(SwingConstants.CENTER);
        NineButton.setBackground(Color.black);
        NineButton.setForeground(Color.white);
        NineButton.setOpaque(true);
        AButton = new JButton("<HTML>AUTO<br>&nbsp;&nbsp;&nbsp;&nbsp;A</HTML>");
        AButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        AButton.setBounds(ABUTTONSTARTX, ABUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        AButton.setHorizontalAlignment(SwingConstants.CENTER);
        AButton.setBackground(Color.black);
        AButton.setForeground(Color.white);
        AButton.setOpaque(true);
        BButton = new JButton("<HTML>BACK<br>&nbsp;&nbsp;&nbsp;&nbsp;B</HTML>");
        BButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        BButton.setBounds(BBUTTONSTARTX, BBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        BButton.setHorizontalAlignment(SwingConstants.CENTER);
        BButton.setBackground(Color.black);
        BButton.setForeground(Color.white);
        BButton.setOpaque(true);
        CButton = new JButton("<HTML>CHAN<br>&nbsp;&nbsp;&nbsp;&nbsp;C</HTML>");
        CButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        CButton.setBounds(CBUTTONSTARTX, CBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        CButton.setHorizontalAlignment(SwingConstants.CENTER);
        CButton.setBackground(Color.black);
        CButton.setForeground(Color.white);
        CButton.setOpaque(true);
        DButton = new JButton("<HTML>DO<br>&nbsp;&nbsp;D</HTML>");
        DButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        DButton.setBounds(DBUTTONSTARTX, DBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        DButton.setHorizontalAlignment(SwingConstants.CENTER);
        DButton.setBackground(Color.black);
        DButton.setForeground(Color.white);
        DButton.setOpaque(true);
        EButton = new JButton("<HTML>EXAM<br>&nbsp;&nbsp;&nbsp;&nbsp;E</HTML>");
        EButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        EButton.setBounds(EBUTTONSTARTX, EBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        EButton.setHorizontalAlignment(SwingConstants.CENTER);
        EButton.setBackground(Color.black);
        EButton.setForeground(Color.white);
        EButton.setOpaque(true);
        FButton = new JButton("<HTML>FWD<br>&nbsp;&nbsp;&nbsp;F</HTML>");
        FButton.setMargin(new Insets(BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN,BUTTONMARGIN));
        FButton.setBounds(FBUTTONSTARTX, FBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        FButton.setHorizontalAlignment(SwingConstants.CENTER);
        FButton.setBackground(Color.black);
        FButton.setForeground(Color.white);
        FButton.setOpaque(true);
        NMIButton = new JButton("<HTML>NMI</HTML>");
        NMIButton.setBounds(NMIBUTTONSTARTX, NMIBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        IRQButton = new JButton("<HTML>IRQ</HTML>");
        IRQButton.setBounds(IRQBUTTONSTARTX, IRQBUTTONSTARTY, BUTTONWIDTH, BUTTONHEIGHT);
        HLabel = new JLabel("H");
        HLabel.setForeground(Color.blue);
        HLabel.setBounds(HLABELX, LABELSTARTY, LABELHEIGHT, LABELWIDTH);
        ILabel = new JLabel("I");
        ILabel.setForeground(Color.blue);
        ILabel.setBounds(ILABELX, LABELSTARTY, LABELHEIGHT, LABELWIDTH);
        NLabel = new JLabel("N");
        NLabel.setForeground(Color.blue);
        NLabel.setBounds(NLABELX, LABELSTARTY, LABELHEIGHT, LABELWIDTH);
        ZLabel = new JLabel("Z");
        ZLabel.setForeground(Color.blue);
        ZLabel.setBounds(ZLABELX, LABELSTARTY, LABELHEIGHT, LABELWIDTH);
        VLabel = new JLabel("V");
        VLabel.setForeground(Color.blue);
        VLabel.setBounds(VLABELX, LABELSTARTY, LABELHEIGHT, LABELWIDTH);
        CLabel = new JLabel("C");
        CLabel.setForeground(Color.blue);
        CLabel.setBounds(CLABELX, LABELSTARTY, LABELHEIGHT, LABELWIDTH);
        menuFile = new JMenu("File");
        load = new JMenuItem("Load");
        loadROM = new JMenuItem("Load ROM");
        save = new JMenuItem("Save");
        settings = new JMenuItem("Settings");
        exit = new JMenuItem("Exit");
        menuFile.add(load);
        menuFile.add(loadROM);
        menuFile.add(save);
        menuFile.add(settings);
        menuFile.add(exit);
        menuHelp = new JMenu("Help");
        about = new JMenuItem("About");
        menuHelp.add(about);
        menubar = new JMenuBar();
        menubar.add(menuFile);
        menubar.add(menuHelp);
        dialog = new JOptionPane();
        myCPU = aCPU;
        myMemoryModule = aMemoryModule;
    }
    
    public void FinishUIInit()
    {
        myPanel jpane = new myPanel(HDisplay, IDisplay, NDisplay, ZDisplay, VDisplay, CDisplay);
        jpane.setLayout(null);
        setSize(WINDOWWIDTH, WINDOWHEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        ResetButton.addActionListener(this);
        ZeroButton.addActionListener(this);
        OneButton.addActionListener(this);
        TwoButton.addActionListener(this);
        ThreeButton.addActionListener(this);
        FourButton.addActionListener(this);
        FiveButton.addActionListener(this);
        SixButton.addActionListener(this);
        SevenButton.addActionListener(this);
        EightButton.addActionListener(this);
        NineButton.addActionListener(this);
        AButton.addActionListener(this);
        BButton.addActionListener(this);
        CButton.addActionListener(this);
        DButton.addActionListener(this);
        EButton.addActionListener(this);
        FButton.addActionListener(this);
        NMIButton.addActionListener(this);
        IRQButton.addActionListener(this);
        load.addActionListener(this);
        loadROM.addActionListener(this);
        save.addActionListener(this);
        settings.addActionListener(this);
        exit.addActionListener(this);
        about.addActionListener(this);
        jpane.add(ResetButton);
        jpane.add(ZeroButton);
        jpane.add(OneButton);
        jpane.add(TwoButton);
        jpane.add(ThreeButton);
        jpane.add(FourButton);
        jpane.add(FiveButton);
        jpane.add(SixButton);
        jpane.add(SevenButton);
        jpane.add(EightButton);
        jpane.add(NineButton);
        jpane.add(AButton);
        jpane.add(BButton);
        jpane.add(CButton);
        jpane.add(DButton);
        jpane.add(EButton);
        jpane.add(FButton);
        jpane.add(NMIButton);
        jpane.add(IRQButton);
        jpane.add(HLabel);
        jpane.add(ILabel);
        jpane.add(NLabel);
        jpane.add(ZLabel);
        jpane.add(VLabel);
        jpane.add(CLabel);
        add(jpane);
        setJMenuBar(menubar);
        runner.start();
    }
    
    @Override
    public void actionPerformed (ActionEvent evt) {
        Object source = evt.getSource();
        if(source == ResetButton)
            ResetAction();
        if(source == ZeroButton)
            KeyAction(0);
        if(source == OneButton)
            KeyAction(1);
        if(source == TwoButton)
            KeyAction(2);
        if(source == ThreeButton)
            KeyAction(3);
        if(source == FourButton)
            KeyAction(4);
        if(source == FiveButton)
            KeyAction(5);
        if(source == SixButton)
            KeyAction(6);
        if(source == SevenButton)
            KeyAction(7);
        if(source == EightButton)
            KeyAction(8);
        if(source == NineButton)
            KeyAction(9);
        if(source == AButton)
            KeyAction(10);
        if(source == BButton)
            KeyAction(11);
        if(source == CButton)
            KeyAction(12);
        if(source == DButton)
            KeyAction(13);
        if(source == EButton)
            KeyAction(14);
        if(source == FButton)
            KeyAction(15);
        if(source == NMIButton)
            myCPU.NMIReq();
        if(source == IRQButton)
            myCPU.IRQReq();
        if(source == about)
        {
            JOptionPane.showMessageDialog(this, "By Dave Sherman\n" + "email: davesherman74@yahoo.com\n" + M6800.VERSION + " (c) 2022", "About ET3400A Simulator", JOptionPane.PLAIN_MESSAGE);
        }
        if(source == settings)
        {
            JSlider clockSlider = getSlider(dialog, myCPU);
            dialog.setMessage(new Object[] { "Clock Rate", clockSlider} );
            dialog.setOptionType(JOptionPane.DEFAULT_OPTION);
            JDialog newDialog = dialog.createDialog(this, "Settings");
            newDialog.setVisible(true);
            
        }
        if(source == load)
        {
            fileDialog = new FileDialog(this, "Open S-Record", FileDialog.LOAD);
            fileDialog.setFile("*.*");  // changgd 5/9/2022, was *.s19
            fileDialog.setVisible(true);
            if(fileDialog.getFile().compareTo("null") != 0)
            {
                fileName = fileDialog.getDirectory() + fileDialog.getFile();
                try {
                    infile = new FileReader(fileName);
                } catch (FileNotFoundException exc) {
                    
                }
                if (infile != null)
                {
                    if(M6800.ReadSRecordFile(infile, myMemoryModule, myCPU) != SRecord.NO_ERROR)
                        JOptionPane.showMessageDialog(this, "Error Loading S-Record", "Error", JOptionPane.ERROR_MESSAGE);
                    try {
                        infile.close();
                    } catch (IOException exc) {
                        
                    }
                }
                
            }
        }
        if(source == loadROM)
        {
            fileDialog = new FileDialog(this, "Open S-Record", FileDialog.LOAD);
            fileDialog.setFile("*.*");  // changgd 5/9/2022, was *.s19
            fileDialog.setVisible(true);
            if(fileDialog.getFile().compareTo("null") != 0)
            {
                fileName = fileDialog.getDirectory() + fileDialog.getFile();
                try {
                    infile = new FileReader(fileName);
                } catch (FileNotFoundException exc) {
                    
                }
                if (infile != null)
                {
                    if(M6800.ReadSRecordFileROM(infile, myMemoryModule, myCPU) != SRecord.NO_ERROR)
                        JOptionPane.showMessageDialog(this, "Error Loading S-Record", "Error", JOptionPane.ERROR_MESSAGE);
                    try {
                        infile.close();
                    } catch (IOException exc) {
                        
                    }
                }
                
            }
        }
        if(source == save)
        {
            fileDialog = new FileDialog(this, "Save S-Record", FileDialog.SAVE);
            fileDialog.setFile("new.s19");
            fileDialog.setVisible(true);
            if(fileDialog.getFile().compareTo("null") != 0)
            {
                fileName = fileDialog.getDirectory() + fileDialog.getFile();
                try {
                    outfile = new FileWriter(fileName);
                } catch (IOException exc) {
                }
                if(outfile != null)
                {
                    M6800.WriteSRecordFile(outfile, myMemoryModule, myCPU);
                    try {
                        outfile.close();
                    } catch (IOException exc) {
                        
                    }
                }
            }
        }
        if(source == exit)
            System.exit(0);
        
    }
    
    public void ResetAction ()
    {
        myCPU.ResetRequest();
    }
    
    public void KeyAction (int iValue)
    {
        myMemoryModule.KeypadWrite(iValue);
    }
    
    
    /*
    // SegmentMemoryRead
    // Convert values in dispay memory to display segment bits.
    //       a
    //      ----
    //   f | g  | b
    //     |----|
    //   e |    | c
    //      ----
    //       d
    //            dp
    //
    // weighting:  a = bit 6
    //             b = bit 5
    //             c = bit 4
    //             d = bit 3
    //             e = bit 2
    //             f = bit 1
    //             g = bit 0 (lowest address)
    //             dp= bit 7 (highest address)
    */
    public int SegmentMemoryRead(int MemAddress)
    {
        int iAddress = MemAddress - 1;
        int iSegmentValue = 0;
        int iCounter;
        // 5/9/2022 - per ET-3400A documentation, bits 3 and 7 in the display address are "don't care".
        // check for values at these locations too
        for(iCounter = 0;iCounter < sevenSegmentDisplay.MAXSEGMENTS;iCounter++)
        {
            switch(iCounter)
            {
                case 0: //highest address, DP position
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.DPWEIGHTING : 0);
                break;
                case 1: // A segment
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.AWEIGHTING : 0);
                break;
                case 2: // B segment
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.BWEIGHTING : 0);
                break;
                case 3: // C segment
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.CWEIGHTING : 0);
                break;
                case 4: // D segment
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.DWEIGHTING : 0);
                break;
                case 5: // E segment
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.EWEIGHTING : 0);
                break;
                case 6: // F segment
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.FWEIGHTING : 0);
                break;
                case 7: // G segment
                    iSegmentValue |= (((myMemoryModule.MemRead(iAddress) & 1) > 0) ? sevenSegmentDisplay.GWEIGHTING : 0);
                break;   
            }
            iAddress--;
        }
        
        return (iSegmentValue);
    }
    
    public void Refresh()
    {
        HDisplay.SetSegments(SegmentMemoryRead(HDISPLAYSTART));
        IDisplay.SetSegments(SegmentMemoryRead(IDISPLAYSTART));
        NDisplay.SetSegments(SegmentMemoryRead(NDISPLAYSTART));
        ZDisplay.SetSegments(SegmentMemoryRead(ZDISPLAYSTART));
        VDisplay.SetSegments(SegmentMemoryRead(VDISPLAYSTART));
        CDisplay.SetSegments(SegmentMemoryRead(CDISPLAYSTART));
        repaint();
    }
    
    @Override
    public void run ()
    {
        
        while(true)
        {
            Refresh();
            try {Thread.sleep(REFRESHMILLISECONDS); }
            catch (InterruptedException exc) {
            debug = 1;
            }
        }
        
    }
    
    static JSlider getSlider (final JOptionPane optionPane, CPU aCPU)
    {
        JSlider slider = new JSlider(CPU.MINCLOCKDELAY, CPU.MAXCLOCKDELAY, aCPU.GetClockDelay());
        ChangeListener listener = (ChangeEvent changeEvent) -> {
            JSlider theSlider = (JSlider) changeEvent.getSource();
            if(!theSlider.getValueIsAdjusting())
            {
                aCPU.SetClockDelay(theSlider.getValue());
            }
        };
        slider.addChangeListener(listener);
        return slider;
    }
}

class myPanel extends JPanel {
    
    private final sevenSegmentDisplay Hdisp;
    private final sevenSegmentDisplay Idisp;
    private final sevenSegmentDisplay Ndisp;
    private final sevenSegmentDisplay Vdisp;
    private final sevenSegmentDisplay Zdisp;
    private final sevenSegmentDisplay Cdisp;
    
    public myPanel(sevenSegmentDisplay H,
                    sevenSegmentDisplay I,
                    sevenSegmentDisplay N,
                    sevenSegmentDisplay Z,
                    sevenSegmentDisplay V,
                    sevenSegmentDisplay C)
    {
        Hdisp = H;
        Idisp = I;
        Ndisp = N;
        Zdisp = Z;
        Vdisp = V;
        Cdisp = C;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(UI.HDISPLAYSTARTX, UI.DISPLAYSTARTY, UI.DISPLAYWIDTH, UI.DISPLAYHEIGHT);
        g.fillRect(UI.IDISPLAYSTARTX, UI.DISPLAYSTARTY, UI.DISPLAYWIDTH, UI.DISPLAYHEIGHT);
        g.fillRect(UI.NDISPLAYSTARTX, UI.DISPLAYSTARTY, UI.DISPLAYWIDTH, UI.DISPLAYHEIGHT);
        g.fillRect(UI.ZDISPLAYSTARTX, UI.DISPLAYSTARTY, UI.DISPLAYWIDTH, UI.DISPLAYHEIGHT);
        g.fillRect(UI.VDISPLAYSTARTX, UI.DISPLAYSTARTY, UI.DISPLAYWIDTH, UI.DISPLAYHEIGHT);
        g.fillRect(UI.CDISPLAYSTARTX, UI.DISPLAYSTARTY, UI.DISPLAYWIDTH, UI.DISPLAYHEIGHT);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(sevenSegmentDisplay.SEGMENTLINEWIDTH));
        Hdisp.drawSegments(g2d);
        Idisp.drawSegments(g2d);
        Ndisp.drawSegments(g2d);
        Zdisp.drawSegments(g2d);
        Vdisp.drawSegments(g2d);
        Cdisp.drawSegments(g2d);
        g2d.setStroke(new BasicStroke(sevenSegmentDisplay.DEFAULTLINEWIDTH));
    }
}

class sevenSegmentDisplay {
    private final int X,Y;
    private int segmentBits;
    public final static int VSEGMENTHEIGHT = (UI.DISPLAYHEIGHT / 2 - 20);
    public final static int HSEGMENTWIDTH = (UI.DISPLAYWIDTH - 30);
    
    public final static float SEGMENTLINEWIDTH = 4.0f;
    public final static float DEFAULTLINEWIDTH = 1.0f;
    public final static int ASEGMENTSTARTX = 15;
    public final static int ASEGMENTSTARTY = 10;
    public final static int ASEGMENTENDX = (ASEGMENTSTARTX + HSEGMENTWIDTH);
    public final static int ASEGMENTENDY = 10;
    public final static int BSEGMENTSTARTX = (ASEGMENTENDX + 5);
    public final static int BSEGMENTSTARTY = 12;
    public final static int BSEGMENTENDX = (BSEGMENTSTARTX - 2);
    public final static int BSEGMENTENDY = (BSEGMENTSTARTY + VSEGMENTHEIGHT);
    public final static int CSEGMENTSTARTX = BSEGMENTENDX - 1;
    public final static int CSEGMENTSTARTY = BSEGMENTENDY + 5;
    public final static int CSEGMENTENDX = (CSEGMENTSTARTX - 2);
    public final static int CSEGMENTENDY = CSEGMENTSTARTY + VSEGMENTHEIGHT;
    public final static int DSEGMENTSTARTX = ASEGMENTSTARTX - 5;
    public final static int DSEGMENTSTARTY = CSEGMENTENDY + 2;
    public final static int DSEGMENTENDX = (DSEGMENTSTARTX+HSEGMENTWIDTH);
    public final static int DSEGMENTENDY = DSEGMENTSTARTY;
    public final static int ESEGMENTSTARTX = ASEGMENTSTARTX - 7;
    public final static int ESEGMENTSTARTY = CSEGMENTSTARTY;
    public final static int ESEGMENTENDX = (ESEGMENTSTARTX - 2);
    public final static int ESEGMENTENDY = CSEGMENTENDY;
    public final static int FSEGMENTSTARTX = ASEGMENTSTARTX - 5;
    public final static int FSEGMENTSTARTY = BSEGMENTSTARTY;
    public final static int FSEGMENTENDX = (FSEGMENTSTARTX - 2);
    public final static int FSEGMENTENDY = BSEGMENTENDY;
    public final static int GSEGMENTSTARTX = ASEGMENTSTARTX - 2;
    public final static int GSEGMENTSTARTY = ASEGMENTSTARTY + VSEGMENTHEIGHT+4;
    public final static int GSEGMENTENDX = (GSEGMENTSTARTX + HSEGMENTWIDTH);
    public final static int GSEGMENTENDY = GSEGMENTSTARTY;
    public final static int DPCENTERX = UI.DISPLAYWIDTH - 14;
    public final static int DPCENTERY = UI.DISPLAYHEIGHT - 16;
    public final static int DPRADIUS = 7;
    public final static int MAXSEGMENTS = 8; //includes decimal point
    public final static int DPWEIGHTING  = (1<<7);
    public final static int AWEIGHTING = (1);
    public final static int BWEIGHTING = (1<<1);
    public final static int CWEIGHTING = (1<<2);
    public final static int DWEIGHTING = (1<<3);
    public final static int EWEIGHTING = (1<<4);
    public final static int FWEIGHTING = (1<<5);
    public final static int GWEIGHTING = (1<<6);
    
    public sevenSegmentDisplay(int X, int Y)
    {
        this.X = X;
        this.Y = Y;
        segmentBits = 0;
    }
    public void drawSegments (Graphics g) {
        if(((segmentBits & 1) > 0))
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.black);
        }
        g.drawLine(X + ASEGMENTSTARTX, Y + ASEGMENTSTARTY, X + ASEGMENTENDX, Y + ASEGMENTENDY);
        if(((segmentBits & 1<<1) > 0))
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.black);
        }
        g.drawLine(X + BSEGMENTSTARTX, Y + BSEGMENTSTARTY, X + BSEGMENTENDX, Y + BSEGMENTENDY);
        if(((segmentBits & 1<<2) > 0))
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.black);
        }
        g.drawLine(X + CSEGMENTSTARTX, Y + CSEGMENTSTARTY, X + CSEGMENTENDX, Y + CSEGMENTENDY);
        if(((segmentBits & 1<<3) > 0))
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.black);
        }
        g.drawLine(X + DSEGMENTSTARTX, Y + DSEGMENTSTARTY, X + DSEGMENTENDX, Y + DSEGMENTENDY);
        if(((segmentBits & 1<<4) > 0))
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.black);
        }
        g.drawLine(X + ESEGMENTSTARTX, Y + ESEGMENTSTARTY, X + ESEGMENTENDX, Y + ESEGMENTENDY);
        if(((segmentBits & 1<<5) > 0))
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.black);
        }
        g.drawLine(X + FSEGMENTSTARTX, Y + FSEGMENTSTARTY, X + FSEGMENTENDX, Y + FSEGMENTENDY);
        if(((segmentBits & 1<<6) > 0))
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.black);
        }
        g.drawLine(X + GSEGMENTSTARTX, Y + GSEGMENTSTARTY, X + GSEGMENTENDX, Y + GSEGMENTENDY);
        
        Graphics2D circle;
        circle = (Graphics2D) g;
        if(((segmentBits & 1<<7) > 0))
        {
            circle.setColor(Color.red);
        }
        else
        {
            circle.setColor(Color.black);
        }
        circle.fillOval(X+DPCENTERX, Y+DPCENTERY, DPRADIUS, DPRADIUS);
    }
    
    public void SetSegments(int segs)
    {
        segmentBits = segs;
    }   
}