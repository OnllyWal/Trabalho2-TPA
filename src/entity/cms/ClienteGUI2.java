package entity.cms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ClienteGUI2 extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private BufferDeClientes bufferDeClientes;
    private final int TAMANHO_BUFFER = 10000;
    private int registrosCarregados = 0; // Contador de registros já carregados
    private String arquivoSelecionado;
    private boolean arquivoCarregado = false; // Para verificar se o arquivo foi carregado

    public ClienteGUI2() {
        setTitle("Gerenciamento de Clientes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
    }


    private void carregarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        int retorno = fileChooser.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = fileChooser.getSelectedFile().getAbsolutePath();
            bufferDeClientes.associaBuffer(new ArquivoCliente()); // Substitua por sua implementação
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado); // Passa o nome do arquivo aqui
            registrosCarregados = 0; // Reseta o contador
            tableModel.setRowCount(0); // Limpa a tabela
            carregarMaisClientes(); // Carrega os primeiros clientes
            arquivoCarregado = true; // Marca que o arquivo foi carregado
        }
    }
    private void criarInterface() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelBotoes = new JPanel();
        
        JButton btnCarregar = new JButton("Carregar Clientes");
        JButton btnInserir = new JButton("Inserir Cliente");
        JButton btnPesquisar = new JButton("Pesquisar Cliente");
        JButton btnRemover = new JButton("Remover Cliente");
        
        tableModel = new DefaultTableModel(new String[]{"#", "Nome", "Sobrenome", "Telefone", "Endereço", "Credit Score"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Adiciona um listener ao JScrollPane para carregar mais clientes ao rolar
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollPane.getVerticalScrollBar().getValueIsAdjusting()) {
                    // Verifica se estamos no final da tabela e se o arquivo foi carregado
                    if (arquivoCarregado && 
                        scrollPane.getVerticalScrollBar().getValue() + 
                        scrollPane.getVerticalScrollBar().getVisibleAmount() >= 
                        scrollPane.getVerticalScrollBar().getMaximum()) {
                        carregarMaisClientes();
                    }
                }
            }
        });

        btnCarregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarArquivo();
            }
        });

        btnInserir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inserirCliente();
            }
        });

        btnPesquisar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pesquisarCliente();
            }
        });

        btnRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerCliente();
            }
        });

        panelBotoes.add(btnCarregar);
        panelBotoes.add(btnInserir);
        panelBotoes.add(btnPesquisar);
        panelBotoes.add(btnRemover);

        panel.add(panelBotoes, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        add(panel);
    }

    private void carregarMaisClientes() {
        // Carrega apenas 10.000 registros de cada vez
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER); // Chama o método com o tamanho do buffer
        if (clientes != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null) { // Verifica se o cliente não é nulo
                    tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, cliente.getNome(), cliente.getSobrenome(), cliente.getTelefone(), cliente.getEndereco(), cliente.getCreditScore()});
                }
            }
            registrosCarregados += clientes.length; // Atualiza o contador
        }
    }

    private void inserirCliente() {
        JTextField nomeField = new JTextField();
        JTextField sobrenomeField = new JTextField();
        JTextField telefoneField = new JTextField();
        JTextField enderecoField = new JTextField();
        JTextField creditScoreField = new JTextField();

        Object[] message = {
            "Nome:", nomeField,
            "Sobrenome:", sobrenomeField,
            "Telefone:", telefoneField,
            "Endereço:", enderecoField,
            "Credit Score:", creditScoreField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Inserir Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Cliente novoCliente = new Cliente(
                        nomeField.getText(),
                        sobrenomeField.getText(),
                        telefoneField.getText(),
                        enderecoField.getText(),
                        Integer.parseInt(creditScoreField.getText())
                );
                bufferDeClientes.adicionaAoBuffer(novoCliente); // Exemplo de método fictício
                tableModel.addRow(new Object[]{
                        tableModel.getRowCount() + 1,
                        novoCliente.getNome(),
                        novoCliente.getSobrenome(),
                        novoCliente.getTelefone(),
                        novoCliente.getEndereco(),
                        novoCliente.getCreditScore()
                });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir cliente: " + ex.getMessage());
            }
        }
    }

    private void pesquisarCliente() {
        String nomePesquisa = JOptionPane.showInputDialog(this, "Digite o nome do cliente para pesquisar:");
        if (nomePesquisa != null && !nomePesquisa.trim().isEmpty()) {
            try {
                Cliente cliente = bufferDeClientes.pesquisarPorNome(nomePesquisa); // Método fictício
                if (cliente != null) {
                    JOptionPane.showMessageDialog(this, "Cliente encontrado:\n" +
                            "Nome: " + cliente.getNome() + "\n" +
                            "Sobrenome: " + cliente.getSobrenome() + "\n" +
                            "Telefone: " + cliente.getTelefone() + "\n" +
                            "Endereço: " + cliente.getEndereco() + "\n" +
                            "Credit Score: " + cliente.getCreditScore());
                } else {
                    JOptionPane.showMessageDialog(this, "Cliente não encontrado.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao pesquisar cliente: " + ex.getMessage());
            }
        }
    }

    private void removerCliente() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String nome = (String) tableModel.getValueAt(selectedRow, 1);
            try {
                bufferDeClientes.removerPorNome(nome); // Método fictício para exemplificar
                tableModel.removeRow(selectedRow);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao remover cliente: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para remover.");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteGUI2 gui = new ClienteGUI2();
            gui.setVisible(true);
        });
    }
}
