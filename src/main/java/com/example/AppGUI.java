package com.example;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class AppGUI extends JFrame {
    private JTextArea outputArea;

    public AppGUI() {
        // Configuração da janela principal
        setTitle("Cadastro de Propriedades Rústicas - Madeira");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Barra de menus
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Ficheiro");
        JMenuItem loadCSV = new JMenuItem("Carregar CSV");
    
        fileMenu.add(loadCSV);
        menuBar.add(fileMenu);

        // Painel com botões para funcionalidades
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1, 5, 5));
        
        // Opção 2: Representar como grafo de propriedades
        JButton representarGrafoPropriedadesButton = new JButton("Representar como grafo de propriedades");
        representarGrafoPropriedadesButton.addActionListener(e -> outputArea.setText("Função: Representar como grafo de propriedades"));
        buttonPanel.add(representarGrafoPropriedadesButton);

        // Opção 3: Calcular área média
        JButton calcularAreaMediaButton = new JButton("Calcular área média das propriedades");
        calcularAreaMediaButton.addActionListener(e -> outputArea.setText("Função: Calcular área média das propriedades"));
        buttonPanel.add(calcularAreaMediaButton);

        // Opção 4: Calcular área média com propriedades adjacentes do mesmo proprietário
        JButton calcularAreaMediaAdjacenteButton = new JButton("Calcular área média com adjacentes do mesmo proprietário");
        calcularAreaMediaAdjacenteButton.addActionListener(e -> outputArea.setText("Função: Calcular área média com adjacentes do mesmo proprietário"));
        buttonPanel.add(calcularAreaMediaAdjacenteButton);

        // Opção 5: Representar relação de vizinhança entre proprietários
        JButton representarGrafoVizinhosButton = new JButton("Representar grafo de vizinhança entre proprietários");
        representarGrafoVizinhosButton.addActionListener(e -> outputArea.setText("Função: Representar grafo de vizinhança entre proprietários"));
        buttonPanel.add(representarGrafoVizinhosButton);

        // Opção 6: Gerar sugestões para troca de propriedades
        JButton gerarSugestoesTrocaButton = new JButton("Gerar sugestões para troca de propriedades");
        gerarSugestoesTrocaButton.addActionListener(e -> outputArea.setText("Função: Gerar sugestões para troca de propriedades"));
        buttonPanel.add(gerarSugestoesTrocaButton);

        // Área de exibição de saída
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Adicionar componentes à janela
        setJMenuBar(menuBar);
        add(buttonPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

   

    // Método principal para executar a GUI
    public static void runApp() {
        SwingUtilities.invokeLater(() -> {
            AppGUI appGUI = new AppGUI();
            appGUI.setVisible(true);
        });
    }
}
