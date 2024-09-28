import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AprioriAlgorithm {

    private List<Set<String>> transactions;
    private Map<Set<String>, Integer> itemsets;
    private Map<String, Double> rules;

    private Map<String, Double> lifts;
    private Map<String, String[]> antecedentsConsequents;


    public AprioriAlgorithm() {
        transactions = new ArrayList<>();
        itemsets = new HashMap<>();//
        rules = new HashMap<>();
        lifts = new HashMap<>();
        antecedentsConsequents = new HashMap<>();
    }

    // charger des transactions à partir d'un fichier . Elle utilise la virgule comme séparateur pour diviser chaque ligne du fichier en éléments distincts.
    public void loadTransactionsFrom(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] items = line.split(",");//split() est utilisée pour diviser une chaîne en un tableau de chaînes de caractères en utilisant un séparateur spécifique(,);puis nous créons un ensemble (Set) à partir de ces éléments pour représenter une transaction unique.
            Set<String> transaction = new HashSet<>(Arrays.asList(items));//prend cette liste et la transforme en un ensemble (HashSet). Comme les ensembles ne permettent pas les doublons
            transactions.add(transaction);
        }

        reader.close();
    }






    public void generateFrequentItemsets(double minSupport) {
        itemsets.clear();
        Map<Set<String>, Integer> frequent1Itemsets = generateFrequent1Itemsets(minSupport);
        itemsets.putAll(frequent1Itemsets);

        // Sort frequent itemsets by size
        List<Map.Entry<Set<String>, Integer>> sortedItemsets = new ArrayList<>(frequent1Itemsets.entrySet());
        sortedItemsets.sort(Comparator.comparingInt(entry -> entry.getKey().size()));

        System.out.println("\nFréquent Itemsets de taille 1 :");
        for (Map.Entry<Set<String>, Integer> entry : sortedItemsets) {
            Set<String> itemset = entry.getKey();
            double support = (double) entry.getValue() / transactions.size();
            System.out.println(itemset + " : " + entry.getValue() + " (Support: " + String.format("%.2f", support * 100) + ")");
        }

        Map<Set<String>, Integer> frequentKItemsets = frequent1Itemsets;
        int k = 2;

        while (!frequentKItemsets.isEmpty()) {
            System.out.println("\nFréquent Itemsets de taille " + k + " :");
            frequentKItemsets = generateCandidateItemsets(frequentKItemsets);
            frequentKItemsets = pruneInfrequentItemsets(frequentKItemsets, minSupport);
            itemsets.putAll(frequentKItemsets);

            List<Map.Entry<Set<String>, Integer>> sortedKItemsets = new ArrayList<>(frequentKItemsets.entrySet());
            sortedKItemsets.sort(Comparator.comparingInt(entry -> entry.getKey().size()));

            for (Map.Entry<Set<String>, Integer> entry : sortedKItemsets) {
                Set<String> itemset = entry.getKey();
                double support = (double) entry.getValue() / transactions.size();
                System.out.println(itemset + " : " + entry.getValue() + " (Support: " + String.format("%.2f", support * 100) + ")");
            }

            k++;
        }
    }






    public void generateAssociationRules(double minConfidence) {
        rules.clear();
        lifts.clear();
        antecedentsConsequents.clear();

        // Sort itemsets by size
        List<Map.Entry<Set<String>, Integer>> sortedItemsets = new ArrayList<>(itemsets.entrySet());
        sortedItemsets.sort(Comparator.comparingInt(entry -> entry.getKey().size()));

        // Generate rules for itemsets of size 1 to size n
        for (int i = 1; i < sortedItemsets.size(); i++) {
            for (Map.Entry<Set<String>, Integer> entry : sortedItemsets) {
                Set<String> itemset = entry.getKey();
                int supportCount = entry.getValue();

                if (itemset.size() == i) { // Check if itemset is of size i
                    generateRulesForItemset(itemset, supportCount, minConfidence);
                }
            }
        }
        pruneInfrequentRules(minConfidence); // Appel pour élaguer les règles inférieures au seuil de confiance

        System.out.println("\n Règles d'association :");
        for (Map.Entry<String, Double> rule : rules.entrySet()) {
            String[] parts = rule.getKey().split(" => ");
            if (parts.length == 2) {
                System.out.println(parts[0] + " -> " + parts[1] + " (Confiance: " + String.format("%.2f", rule.getValue()) + ")");
            }
        }
    }








    //pour but de générer des règles d'association à partir d'un ensemble d'éléments fréquent (appelé itemset).
    private void generateRulesForItemset(Set<String> itemset, int supportCount, double minConfidence) {
        int n = itemset.size();//la taille de l'ensemble d'éléments (itemset).
        List<Set<String>> subsets = generateSubsets(itemset);//une liste contenant tous les sous-ensembles possibles de itemset.

        for (Set<String> subset : subsets) {
            if (subset.size() < n) {
                Set<String> consequent = new HashSet<>(itemset);
                consequent.removeAll(subset);
                int subsetCount = itemsets.getOrDefault(subset, 0);
                double confidence = (double) supportCount / subsetCount;

                if (!Double.isInfinite(confidence) && subsetCount > 0 && confidence >= minConfidence) {
                    // rules.put(subset.toString() + " => " + consequent.toString(), confidence);
                    String rule = subset.toString() + " => " + consequent.toString();
                    rules.put(rule, confidence);
                    //
                    antecedentsConsequents.put(rule, new String[]{subset.toString(), consequent.toString()});
                }
            }
        }
    }





    private List<Set<String>> generateSubsets(Set<String> set) {
        List<Set<String>> subsets = new ArrayList<>();
        generateSubsetsRecursive(set, new HashSet<>(), 0, subsets);
        return subsets;
    }
    //generateSubsetsRecursive est la fonction qui fait tout le travail pour générer les sous-ensembles. Voici comment elle fonctionne pas à pas
    private void generateSubsetsRecursive(Set<String> set, Set<String> currentSubset, int start, List<Set<String>> subsets) {
        subsets.add(new HashSet<>(currentSubset));

        for (int i = start; i < set.size(); i++) {
            List<String> items = new ArrayList<>(set);
            currentSubset.add(items.get(i));
            generateSubsetsRecursive(set, currentSubset, i + 1, subsets);
            currentSubset.remove(items.get(i));
        }
    }

    //the first function for start the processus in frequence items
    //Générer les Ensembles d'Items Fréquents juste une item
    private Map<Set<String>, Integer> generateFrequent1Itemsets(double minSupport) {
        Map<Set<String>, Integer> frequent1Itemsets = new HashMap<>();//stocker les ensembles fréquents de taille 1 ainsi que leur support

        Map<String, Integer> counts = new HashMap<>();//compter le nombre d'occurrences de chaque élément dans toutes les transactions.

        for (Set<String> transaction : transactions) {
            for (String item : transaction) {
                Set<String> itemset = new HashSet<>(Collections.singletonList(item));//n'est pas afficher une ordre aleatoire
                counts.put(item, counts.getOrDefault(item, 0) + 1);
                frequent1Itemsets.put(itemset, counts.get(item));//add chaque item et le leur support
            }
        }

        return pruneInfrequentItemsets(frequent1Itemsets, minSupport);
    }
    //frequentKItemsets est une carte (map) qui contient des ensembles fréquents de taille k et leur nombre d'occurrences
    //Générer les Candidats pour les Ensembles d'Items Fréquents de Taille k+1:
    private Map<Set<String>, Integer> generateCandidateItemsets(Map<Set<String>, Integer> frequentKItemsets) {
        Map<Set<String>, Integer> candidateItemsets = new HashMap<>();//stocker les ensembles candidats de taille k +1 et initialise leur nombre d'occurrences à zéro.

        for (Set<String> itemset1 : frequentKItemsets.keySet()) {
            for (Set<String> itemset2 : frequentKItemsets.keySet()) {
                if (itemset1.size() != itemset2.size()) {
                    continue;
                }

                Set<String> candidate = new HashSet<>(itemset1);
                candidate.addAll(itemset2);

                if (candidate.size() == itemset1.size() + 1) {
                    candidateItemsets.put(candidate, 0);
                }
            }
        }

        return candidateItemsets;
    }

    //remove the item is not frequency about all items of transaction
    //.Élaguer les Ensembles d'Items Infréquents(c'est dire que eleminer les item n'est pas frequent)
    private Map<Set<String>, Integer> pruneInfrequentItemsets(Map<Set<String>, Integer> candidateItemsets, double minSupport) {
        Map<Set<String>, Integer> frequentItemsets = new HashMap<>();//stocker les item la plus frequent

        for (Set<String> transaction : transactions) { //Boucle sur chaque transaction
            for (Set<String> itemset : candidateItemsets.keySet()) {//Boucle sur chaque itemset candidat
                if (transaction.containsAll(itemset)) {
                    frequentItemsets.put(itemset, frequentItemsets.getOrDefault(itemset, 0) + 1);// add
                }
            }
        }

        frequentItemsets.entrySet().removeIf(entry -> ((double) entry.getValue() / transactions.size()) < minSupport);
        return frequentItemsets;//la méthode retourne la liste mise à jour frequentItemsets la liste final de itemset plus frequent
    }
    public double calculateLift(Set<String> antecedent, Set<String> consequent) {
        int antecedentCount = 0;
        int consequentCount = 0;
        int jointCount = 0;

        for (Set<String> transaction : transactions) {
            if (transaction.containsAll(antecedent)) {
                antecedentCount++;
            }
            if (transaction.containsAll(consequent)) {
                consequentCount++;
            }
            if (transaction.containsAll(antecedent) && transaction.containsAll(consequent)) {
                jointCount++;
            }
        }

        double supportAntecedent = (double) antecedentCount / transactions.size();
        double supportConsequent = (double) consequentCount / transactions.size();
        double supportJoint = (double) jointCount / transactions.size();

        if (supportAntecedent == 0 || supportConsequent == 0) {
            return 0.0;
        }

        return supportJoint / (supportAntecedent * supportConsequent);
    }
    public Map<String, Double> getLifts() {
        return lifts;
    }

    public Map<String, String[]> getAntecedentsConsequents() {
        return antecedentsConsequents;
    }

    public Map<Set<String>, Integer> getItemsets() {
        return itemsets;
    }

    public Map<String, Double> getRules() {
        return rules;
    }
    public List<Set<String>> getTransactions() {
        return transactions;
    }


    public void eliminateSpacesAfterComma() {
        for (int i = 0; i < transactions.size(); i++) {
            Set<String> cleanedTransaction = new HashSet<>();
            for (String item : transactions.get(i)) {
                // Élimine les espaces en début et fin de chaque item
                String trimmedItem = item.trim();
                // Supprime l'espace après une virgule s'il existe
                String cleanedItem = trimmedItem.replaceAll(",\\s+", ",");
                cleanedTransaction.add(cleanedItem);
            }
            transactions.set(i, cleanedTransaction); // Remplace la transaction par la version nettoyée
        }
    }


    private void pruneInfrequentRules(double minConfidence) {
        rules.entrySet().removeIf(entry -> entry.getValue() < minConfidence);
    }


}