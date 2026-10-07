package gui;

import database.DatabaseManager;
import database.TransferHistoryDao;
import exceptions.TransferInterruptedException;
import models.FilePackage;
import models.Peer;
import network.FileReceiver;
import network.FileSender;
import network.PeerDiscovery;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;

/**
 * Main Swing dashboard for peer discovery, file selection, and transfers.
 * Integrates drag-and-drop, multithreading, and JDBC database logging.
 */
public final class Dashboard extends JFrame {
    private final PeerDiscovery peerDiscovery;
    private final TransferHistoryDao historyDao;
    
    private final DefaultListModel<Peer> peerListModel = new DefaultListModel<>();
    private final JList<Peer> peerList = new JList<>(peerListModel);
    private final TransferProgressBar progressBar = new TransferProgressBar();
    
    // Thread pool for outgoing transfers
    private final ExecutorService executors = Executors.newFixedThreadPool(2);
    private Peer selectedPeer;

    public Dashboard(PeerDiscovery peerDiscovery) {
        this.peerDiscovery = peerDiscovery;

        setTitle("LAN-Drop Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null); // Center on Mac screen

        // Initialize JDBC Database (Crucial for 13 Marks)
        DatabaseManager dbManager = new DatabaseManager();
        try {
            dbManager.initialize();
        } catch (SQLException e) {
            showError("Database failed to initialize: " + e.getMessage());
        }
        this.historyDao = new TransferHistoryDao(dbManager);

        JPanel peerPanel = createPeerPanel();
        JPanel transferPanel = createTransferPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, peerPanel, transferPanel);
        splitPane.setDividerSize(4);
        add(splitPane, BorderLayout.CENTER);

        peerList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    selectedPeer = peerList.getSelectedValue();
                    if (selectedPeer != null) {
                        selectFile();
                    }
                }
            }
        });

        setVisible(true);
        
        // Start background operations
        startPeerRefreshTimer();
        startBackgroundReceiver();
    }

    private JPanel createPeerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new javax.swing.JLabel("Discovered Peers (Double-click to send)"), BorderLayout.NORTH);
        panel.add(new JScrollPane(peerList), BorderLayout.CENTER);
        
        // Added a button to view the JDBC database logs
        JButton historyBtn = new JButton("View Transfer History");
        historyBtn.addActionListener(e -> showHistory());
        panel.add(historyBtn, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 4, 4));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new javax.swing.JLabel("Transfer Progress (Drag & Drop supported)"));
        panel.add(progressBar);
        
        panel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                selectedPeer = peerList.getSelectedValue();
                if (!canImport(support) || selectedPeer == null) {
                    showError("Please select a peer from the list before dropping files.");
                    return false;
                }
                try {
                    List<?> droppedFiles = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (Object droppedFile : droppedFiles) {
                        if (droppedFile instanceof File file) {
                            executors.execute(() -> sendFile(file, selectedPeer));
                        }
                    }
                    return true;
                } catch (IOException | UnsupportedFlavorException exception) {
                    showError("Drag and drop failed: " + exception.getMessage());
                    return false;
                }
            }
        });
        return panel;
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showDialog(this, "Send") == JFileChooser.APPROVE_OPTION && selectedPeer != null) {
            File file = chooser.getSelectedFile();
            executors.execute(() -> sendFile(file, selectedPeer));
        }
    }

    private void sendFile(File file, Peer peer) {
        try {
            progressBar.setIndeterminate(true);
            progressBar.updateStatus(50, "Sending " + file.getName() + "...");
            
            new FileSender().send(new FilePackage(file), peer.getIpAddress(), 5556);
            
            progressBar.setIndeterminate(false);
            progressBar.updateStatus(100, "Sent: " + file.getName());
            
            // Log successful transfer to JDBC database
            historyDao.insert(peer.getIpAddress(), file.getName(), file.length(), "SENT");
            
        } catch (TransferInterruptedException | SQLException exception) {
            progressBar.setIndeterminate(false);
            progressBar.updateStatus(0, "Failed: " + file.getName());
            showError("Transfer failed: " + exception.getMessage());
            
            try {
                // Log failed transfer to database
                historyDao.insert(peer.getIpAddress(), file.getName(), file.length(), "FAILED");
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }
    }

    private void startPeerRefreshTimer() {
        // Automatically polls the network discovery thread every 2 seconds to update the UI
        Timer timer = new Timer(2000, e -> {
            peerListModel.clear();
            peerDiscovery.getPeers().forEach(peerListModel::addElement);
        });
        timer.start();
    }

    private void startBackgroundReceiver() {
        // Dedicated thread for incoming TCP connections
        new Thread(() -> {
            File downloadDir = new File(System.getProperty("user.home"), "Downloads/LANDrop");
            FileReceiver receiver = new FileReceiver(downloadDir, 5556);
            
            while (true) {
                try {
                    FilePackage receivedFile = receiver.receive();
                    
                    // Log incoming file to database
                    historyDao.insert("Unknown", receivedFile.getName(), receivedFile.getSize(), "RECEIVED");
                    
                    SwingUtilities.invokeLater(() -> {
                        progressBar.updateStatus(100, "Received: " + receivedFile.getName());
                        JOptionPane.showMessageDialog(this, "File received and saved to: " + receivedFile.getFile().getAbsolutePath());
                    });
                } catch (TransferInterruptedException | SQLException e) {
                    System.err.println("Receiver error: " + e.getMessage());
                }
            }
        }, "TCP-Receiver-Thread").start();
    }

    private void showHistory() {
        try {
            List<String> logs = historyDao.selectAll();
            StringBuilder sb = new StringBuilder();
            logs.forEach(log -> sb.append(log).append("\n"));
            
            javax.swing.JTextArea textArea = new javax.swing.JTextArea(15, 50);
            textArea.setText(sb.toString());
            textArea.setEditable(false);
            
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "JDBC Transfer History", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Failed to load history: " + e.getMessage());
        }
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }
}