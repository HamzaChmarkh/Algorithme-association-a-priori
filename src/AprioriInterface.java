import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class AprioriInterface extends JFrame {
    private JTextField filePathField;
    private JTextField supportField;
    private JTextField confidenceField;
    private JButton runFrequentItemsButton;
    private JButton runAssociationRulesButton;
    private JPanel frequentItemsPanel;
    private JPanel associationRulesPanel;
    private AprioriAlgorithm apriori;

    public AprioriInterface() {
        setTitle("Interface Algorithme Apriori");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color backgroundColor = Color.GRAY;
        getContentPane().setBackground(backgroundColor);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(70, 130, 180)); // Couleur SteelBlue
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        filePathField = new JTextField();
        filePathField.setBackground(Color.WHITE);
        filePathField.setPreferredSize(new Dimension(200, 20)); // Taille préférée réduite
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(filePathField, gbc);

        JButton chooseFileButton = new JButton("Importer un fichier");
        chooseFileButton.setBackground(new Color(100, 149, 237)); // Couleur CornflowerBlue
        chooseFileButton.setForeground(Color.BLACK);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(AprioriInterface.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        inputPanel.add(chooseFileButton, gbc);

        JLabel supportLabel = new JLabel("Support minimum (%) :");
        supportLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST; // Aligner à droite
        inputPanel.add(supportLabel, gbc);

        supportField = new JTextField();
        supportField.setBackground(Color.WHITE);
        supportField.setPreferredSize(new Dimension(100, 20)); // Taille préférée réduite
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST; // Aligner à gauche
        inputPanel.add(supportField, gbc);

        JLabel confidenceLabel = new JLabel("Confiance minimum (%) :");
        confidenceLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST; // Aligner à droite
        inputPanel.add(confidenceLabel, gbc);

        confidenceField = new JTextField();
        confidenceField.setBackground(Color.WHITE);
        confidenceField.setPreferredSize(new Dimension(100, 20)); // Taille préférée réduite
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST; // Aligner à gauche
        inputPanel.add(confidenceField, gbc);

        runFrequentItemsButton = new JButton("Afficher les éléments Fréquents");
        runFrequentItemsButton.setBackground(Color.GREEN);
        runFrequentItemsButton.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(runFrequentItemsButton, gbc);

        runAssociationRulesButton = new JButton("Afficher les règles d'Association");
        runAssociationRulesButton.setBackground(Color.ORANGE);
        runAssociationRulesButton.setForeground(Color.BLACK);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(runAssociationRulesButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        frequentItemsPanel = new JPanel(new BorderLayout());
        frequentItemsPanel.setBackground(new Color(224, 255, 255)); // Couleur LightCyan

        associationRulesPanel = new JPanel(new BorderLayout());
        associationRulesPanel.setBackground(new Color(224, 255, 255)); // Couleur LightCyan

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(frequentItemsPanel), new JScrollPane(associationRulesPanel));
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);

        apriori = new AprioriAlgorithm();

        runFrequentItemsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    runFrequentItems();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(AprioriInterface.this, "Erreur lors de l'exécution de l'algorithme : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        runAssociationRulesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAssociationRules();
            }
        });
    }



    private void runFrequentItems() throws IOException {
        String filePath = filePathField.getText();
        double minSupport = Double.parseDouble(supportField.getText()) / 100.0;

        apriori.loadTransactionsFrom(filePath);
        apriori.eliminateSpacesAfterComma(); // Élimine les espaces avant de générer les itemsets fréquents
        apriori.generateFrequentItemsets(minSupport);

        frequentItemsPanel.removeAll();

        // Create table for frequent items
        String[] frequentItemsColumns = {"Éléments Fréquents", "Support (%)"};
        //List<Map.Entry<Set<String>, Integer>> sortedItemsets = new ArrayList<>(apriori.getItemsets().entrySet());
        List<Map.Entry<Set<String>, Integer>> sortedItemsets = new ArrayList<>(apriori.getItemsets().entrySet());

        // Sort itemsets by size and then lexicographically
        sortedItemsets.sort((entry1, entry2) -> {
            int sizeComparison = Integer.compare(entry1.getKey().size(), entry2.getKey().size());
            if (sizeComparison != 0) {
                return sizeComparison;
            } else {
                return entry1.getKey().toString().compareTo(entry2.getKey().toString());
            }
        });

        Object[][] frequentItemsData = new Object[sortedItemsets.size()][2];
        int row = 0;
        for (Map.Entry<Set<String>, Integer> entry : sortedItemsets) {
            double support = (double) entry.getValue() / apriori.getTransactions().size();
            frequentItemsData[row][0] = entry.getKey().toString();
            frequentItemsData[row][1] = String.format("%.2f", support * 100) ;
            row++;
        }
        JTable frequentItemsTable = new JTable(frequentItemsData, frequentItemsColumns);
        frequentItemsPanel.add(new JScrollPane(frequentItemsTable), BorderLayout.CENTER);

        frequentItemsPanel.revalidate();
        frequentItemsPanel.repaint();
    }


    private void runAssociationRules() {
        double minConfidence = Double.parseDouble(confidenceField.getText()) / 100.0;

        apriori.generateAssociationRules(minConfidence);

        associationRulesPanel.removeAll();

        // Get the rules and sort them by the size of the antecedent
        List<Map.Entry<String, Double>> sortedRules = new ArrayList<>(apriori.getRules().entrySet());
        sortedRules.sort(Comparator.comparingInt(entry -> {
            String[] parts = entry.getKey().split(" => ");
            Set<String> antecedent = new HashSet<>(Arrays.asList(parts[0].replaceAll("[\\[\\]]", "").split(", ")));
            return antecedent.size();
        }));

        // Create table for association rules
        String[] associationRulesColumns = {"Règles d'Association", "Antécédents", "Conséquents", "Confiance (%)", "Lift"};
        Object[][] associationRulesData = new Object[sortedRules.size()][5];
        int row = 0;
        for (Map.Entry<String, Double> entry : sortedRules) {
            String[] parts = entry.getKey().split(" => ");
            Set<String> antecedent = new HashSet<>(Arrays.asList(parts[0].replaceAll("[\\[\\]]", "").split(", ")));
            Set<String> consequent = new HashSet<>(Arrays.asList(parts[1].replaceAll("[\\[\\]]", "").split(", ")));
            double liftValue = apriori.calculateLift(antecedent, consequent);
            associationRulesData[row][0] = entry.getKey();
            associationRulesData[row][1] = antecedent.toString();
            associationRulesData[row][2] = consequent.toString();
            associationRulesData[row][3] = String.format("%.2f", entry.getValue() * 100);
            associationRulesData[row][4] = String.format("%.2f", liftValue);
            row++;
        }
        JTable associationRulesTable = new JTable(associationRulesData, associationRulesColumns);
        associationRulesPanel.add(new JScrollPane(associationRulesTable), BorderLayout.CENTER);

        associationRulesPanel.revalidate();
        associationRulesPanel.repaint();
    }





    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AprioriInterface().setVisible(true);
            }
        });
    }


}





