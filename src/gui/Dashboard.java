package gui;

import models.FilePackage;
import models.Peer;
import network.FileSender;
import network.PeerDiscovery;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferSupport;

/**
 * Main Swing dashboard for peer discovery, file selection, and transfers.
 */
public final class Dashboard extends JFrame {
    private final PeerDiscovery peerDiscovery;
    private final DefaultListModel<Peer> peerListModel = new DefaultListModel<>();
    private final JList<Peer> peerList = new JList<>(peerListModel);
    private final TransferProgressBar progressBar = new TransferProgressBar();
    private final ExecutorService executors = Executors.newFixedThreadPool(2);
    private Peer selectedPeer;

    public Dashboard(PeerDiscovery peerDiscovery) {
        this.peerDiscovery = peerDiscovery;

        setTitle("File Transfer Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

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
        refreshPeers();
    }

    private JPanel createPeerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new javax.swing.JLabel("Discovered peers"), BorderLayout.NORTH);
        panel.add(new JScrollPane(peerList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 4, 4));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new javax.swing.JLabel("Transfer progress"));
        panel.add(progressBar);
        panel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public void importData(TransferSupport support) {
                if (!canImport(support) || selectedPeer == null) {
                    return;
                }
                try {
                    List<?> droppedFiles = (List<?>) support.getTransferable().getTransferData(
                            DataFlavor.javaFileListFlavor);
                    for (Object droppedFile : droppedFiles) {
                        if (droppedFile instanceof File file) {
                            executors.execute(() -> sendFile(file, selectedPeer));
                        }
                    }
                } catch (IOException exception) {
                    showError(exception.getMessage());
                }
            }
        });
        return panel;
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showDialog(this, "Send") == JFileChooser.APPROVE_OPTION
                && selectedPeer != null) {
            File file = chooser.getSelectedFile();
            executors.execute(() -> sendFile(file, selectedPeer));
        }
    }

    private void sendFile(File file, Peer peer) {
        try {
            progressBar.setIndeterminate(true);
            new FileSender().send(new FilePackage(file), peer.getIpAddress(), 5556);
            progressBar.setValue(100);
        } catch (IOException exception) {
            progressBar.setValue(0);
            showError(exception.getMessage());
        } finally {
            progressBar.setIndeterminate(false);
        }
    }

    private void refreshPeers() {
        SwingUtilities.invokeLater(() -> {
            peerListModel.clear();
            peerDiscovery.getPeers().forEach(peerListModel::addElement);
        });
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(
                this, message, "Transfer error", javax.swing.JOptionPane.ERROR_MESSAGE));
    }
}
