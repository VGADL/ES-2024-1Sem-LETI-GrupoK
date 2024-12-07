package iscteiul.ista.gestaoterritorio;

import org.jgrapht.Graphs;

import java.util.*;

public class PropertiesExchangeSuggestion {

    /**
     * Gera sugestões de troca de propriedades para maximizar a área média por proprietário.
     *
     * @param csvReader Objeto CsvReader que contém os registos lidos do ficheiro CSV.
     * @param graphTerreno Grafo dos terrenos ligados por adjacências.
     * @param graphProprietario Grafo dos proprietários e suas adjacências.
     * @return Lista de sugestões de trocas otimizadas.
     */
    public List<ExchangeSuggestion> generateExchangeSuggestions(CsvReader csvReader, GraphTerreno graphTerreno, GraphProprietario graphProprietario) {
        List<ExchangeSuggestion> suggestions = new ArrayList<>();

        // Obter todos os proprietários do grafo
        Set<String> owners = graphProprietario.getGraph().vertexSet();
        //System.out.println("Proprietários encontrados: " + owners);

        // Comparar pares únicos de proprietários
        for (String owner1 : owners) {
            for (String owner2 : owners) {
                if (owner1.compareTo(owner2) >= 0) continue; // Evitar duplicação de pares
               // System.out.println("Analisando troca entre " + owner1 + " e " + owner2);

                // Obter propriedades de ambos os proprietários
                Set<String> propertiesOwner1 = getOwnerProperties(owner1, csvReader, graphTerreno);
                Set<String> propertiesOwner2 = getOwnerProperties(owner2, csvReader, graphTerreno);

                // Processar pares de propriedades
                for (String prop1 : propertiesOwner1) {
                    for (String prop2 : propertiesOwner2) {
                        if (graphTerreno.getGraph().containsVertex(prop1) && graphTerreno.getGraph().containsVertex(prop2)) {
                            analyzeAndAddSuggestion(
                                    owner1, owner2, prop1, prop2, csvReader, graphTerreno, suggestions
                            );
                        }
                    }
                }
            }
        }

        // Ordenar sugestões por ganho total de área e potencial
        suggestions.sort(Comparator.comparingDouble(ExchangeSuggestion::getTotalPotentialGain).reversed()
                .thenComparingDouble(ExchangeSuggestion::getPotential));

        // Limitar às 20 melhores sugestões
        if (suggestions.size() > 20) {
            suggestions = suggestions.subList(0, 20);
        }
        return suggestions;
    }

    /**
     * Analisa um par de propriedades para uma possível troca e adiciona a sugestão à lista.
     */
    private void analyzeAndAddSuggestion(String owner1, String owner2, String prop1, String prop2,
                                         CsvReader csvReader, GraphTerreno graphTerreno, List<ExchangeSuggestion> suggestions) {
        double area1 = getPropertyArea(prop1, csvReader);
        double area2 = getPropertyArea(prop2, csvReader);

        // Calcular as áreas médias antes e depois da troca
        double avgAreaOwner1Before = calculateAverageAreaForOwner(owner1, csvReader, graphTerreno);
        double avgAreaOwner2Before = calculateAverageAreaForOwner(owner2, csvReader, graphTerreno);
        double avgAreaOwner1After = calculateAverageAreaForOwnerAfterSwap(owner1, prop1, prop2, csvReader, graphTerreno);
        double avgAreaOwner2After = calculateAverageAreaForOwnerAfterSwap(owner2, prop2, prop1, csvReader, graphTerreno);

        double gainOwner1 = avgAreaOwner1After - avgAreaOwner1Before;
        double gainOwner2 = avgAreaOwner2After - avgAreaOwner2Before;

        // Calcular o potencial de realização da troca
        double potential = 1 / (Math.abs(area1 - area2) + 1); // Evitar divisão por zero

        // Adicionar a sugestão sem filtrar por ganhos positivos
        suggestions.add(new ExchangeSuggestion(owner1, owner2, prop1, prop2, gainOwner1 + gainOwner2, potential));
        //System.out.println("Troca sugerida: " + owner1 + " (" + prop1 + ") ↔ " + owner2 + " (" + prop2 + ")");
    }

    /**
     * Calcula a área média dos terrenos de um proprietário, considerando os terrenos adjacentes.
     *
     * @param owner        Nome do proprietário.
     * @param csvReader    Objeto CsvReader para consultar os dados das propriedades.
     * @param graphTerreno Grafo que representa a relação entre os terrenos.
     * @return Área média dos terrenos pertencentes ao proprietário.
     */
    private double calculateAverageAreaForOwner(String owner, CsvReader csvReader, GraphTerreno graphTerreno) {
        //System.out.println("A calcular área média para o proprietário: " + owner);
        Set<String> ownerProperties = getOwnerProperties(owner, csvReader, graphTerreno);
        double totalArea = 0;
        int count = 0;

        for (String property : ownerProperties) {
            totalArea += getPropertyArea(property, csvReader);
            count++;

            if (!graphTerreno.getGraph().containsVertex(property)) continue;

            Set<String> adjacentProperties = Graphs.neighborSetOf(graphTerreno.getGraph(), property);
            for (String adjacent : adjacentProperties) {
                totalArea += getPropertyArea(adjacent, csvReader);
                count++;
            }
        }

        double averageArea = count > 0 ? totalArea / count : 0;
        //System.out.println("Área média calculada para " + owner + ": " + averageArea);
        return averageArea;
    }
    /**
     * Calcula a área média dos terrenos de um proprietário após uma troca específica.
     *
     * @param owner           Nome do proprietário.
     * @param propertyToRemove ID da propriedade a ser removida.
     * @param propertyToAdd    ID da propriedade a ser adicionada.
     * @param csvReader        Objeto CsvReader para consultar os dados das propriedades.
     * @param graphTerreno     Grafo que representa a relação entre os terrenos.
     * @return Nova área média dos terrenos pertencentes ao proprietário após a troca.
     */
    private double calculateAverageAreaForOwnerAfterSwap(String owner, String propertyToRemove, String propertyToAdd, CsvReader csvReader, GraphTerreno graphTerreno) {
        Set<String> ownerProperties = getOwnerProperties(owner, csvReader, graphTerreno);

        // Subtrair a área da propriedade a ser removida e adicionar a área da nova propriedade
        double totalArea = 0;
        int count = 0;

        for (String property : ownerProperties) {
            if (property.equals(propertyToRemove)) continue; // Remover a propriedade antiga
            totalArea += getPropertyArea(property, csvReader);
            count++;

            // Adicionar áreas dos terrenos adjacentes
            if (graphTerreno.getGraph().containsVertex(property)) {
                Set<String> adjacentProperties = Graphs.neighborSetOf(graphTerreno.getGraph(), property);
                for (String adjacent : adjacentProperties) {
                    totalArea += getPropertyArea(adjacent, csvReader);
                    count++;
                }
            }
        }

        // Adicionar a nova propriedade e seus adjacentes
        totalArea += getPropertyArea(propertyToAdd, csvReader);
        count++;

        Set<String> adjacentToAdd = Graphs.neighborSetOf(graphTerreno.getGraph(), propertyToAdd);

        for (String adjacent : adjacentToAdd) {
            totalArea += getPropertyArea(adjacent, csvReader);
            count++;
        }

        return count > 0 ? totalArea / count : 0;
    }

    /**
     * Obtém as propriedades pertencentes a um proprietário.
     *
     * @param owner        Nome do proprietário.
     * @param csvReader    Objeto CsvReader para consultar os dados das propriedades.
     * @param graphTerreno Grafo que representa a relação entre os terrenos.
     * @return Conjunto de IDs das propriedades pertencentes ao proprietário.
     */
    private Set<String> getOwnerProperties(String owner, CsvReader csvReader, GraphTerreno graphTerreno) {
       // System.out.println("Obtendo propriedades para o proprietário: " + owner);
        Set<String> properties = new HashSet<>();
        List<Map<String, String>> records = csvReader.getRecords();

        for (Map<String, String> record : records) {
            if (record.get("OWNER").equals(owner)) {
                String terrainId = record.get("OBJECTID");
                properties.add(terrainId);
            }
        }
        //System.out.println("Propriedades obtidas para " + owner + ": " + properties);
        return properties;
    }

    /**
     * Obtém a área de uma propriedade a partir do ID.
     *
     * @param propertyId ID da propriedade.
     * @param csvReader  Objeto CsvReader para consultar os dados das propriedades.
     * @return Área da propriedade ou 0 se não for possível obter o valor.
     */
    private double getPropertyArea(String propertyId, CsvReader csvReader) {
        for (Map<String, String> record : csvReader.getRecords()) {
            if (record.get("OBJECTID").equals(propertyId)) {
                try {
                    return Double.parseDouble(record.get("Shape_Area"));
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao processar a área: " + record.get("Shape_Area"));
                    return 0;
                }
            }
        }
        return 0;
    }

    public static class ExchangeSuggestion {
        private final String owner1;
        private final String owner2;
        private final String property1;
        private final String property2;
        private final double totalPotentialGain;
        private final double potential;

        public ExchangeSuggestion(String owner1, String owner2, String property1, String property2, double totalPotentialGain, double potential) {
            this.owner1 = owner1;
            this.owner2 = owner2;
            this.property1 = property1;
            this.property2 = property2;
            this.totalPotentialGain = totalPotentialGain;
            this.potential = potential;
        }
        /**
         * Obtém o ganho total potencial da troca.
         *
         * @return Ganho total potencial.
         */

        public double getTotalPotentialGain() {
            return totalPotentialGain;
        }
        /**
         * Obtém o potencial da troca.
         *
         * @return Potencial da troca.
         */
        public double getPotential() {
            return potential;
        }

        /**
         * Obtém o proprietário do primeiro terreno.
         *
         * @return Uma {@code String} representando o proprietário do primeiro terreno.
         */
        public String getOwner1() {
            return owner1;
        }

        /**
         * Obtém o proprietário do segundo terreno.
         *
         * @return Uma {@code String} que representa o proprietário do segundo terreno.
         */
        public String getOwner2() {
            return owner2;
        }

        /**
         * Obtém a identificação ou descrição da primeira propriedade.
         *
         * @return Uma {@code String} que representa a primeira propriedade.
         */
        public String getProperty1() {
            return property1;
        }

        /**
         * Obtém a identificação ou descrição da segunda propriedade.
         *
         * @return Uma {@code String} que representa a segunda propriedade.
         */
        public String getProperty2() {
            return property2;
        }


        @Override
        public String toString() {
            return "Troca: Proprietário " + owner1 + " e Proprietário " + owner2 +
                    " - Terreno " + property1 + " por Terreno " + property2 +
                    " | Ganho Total de Área: " + totalPotentialGain +
                    " | Potencial: " + potential;
        }
    }

}
