import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class JanelaClientes extends JFrame {

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private ClienteDAO clienteDAO;

    // Campos do formulário
    private JTextField txtId;
    private JTextField txtNome;
    private JTextField txtEndereco;

    // Botões
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnLimpar;

    public JanelaClientes(Connection conexao) {
        super("Gerenciamento de Clientes (CRUD)");
        this.clienteDAO = new ClienteDAO(conexao);

        setSize(850, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        carregarDadosTabela();
    }

    private void inicializarComponentes() {
        // --- 1. CONFIGURAÇÃO DA TABELA (ESQUERDA) ---
        String[] colunas = {"ID", "Nome", "Endereço"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Células não editáveis diretamente no grid
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Permite selecionar apenas 1 linha por vez
        JScrollPane scrollPane = new JScrollPane(tabela);

        // Listener para preencher os campos de texto ao clicar numa linha da tabela
        tabela.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                    preencherCamposComLinhaSelecionada();
                }
            }
        });

        // --- 2. CONFIGURAÇÃO DO FORMULÁRIO (DIREITA) ---
        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setBorder(BorderFactory.createTitledBorder("Dados do Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID (Apenas leitura, pois é gerado/controlado pelo Banco de Dados)
        gbc.gridx = 0; gbc.gridy = 0;
        painelFormulario.add(new JLabel("ID:"), gbc);
        txtId = new JTextField(5);
        txtId.setEditable(false);
        txtId.setFocusable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        painelFormulario.add(txtId, gbc);

        // Nome
        gbc.gridx = 0; gbc.gridy = 1;
        painelFormulario.add(new JLabel("Nome:"), gbc);
        txtNome = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        painelFormulario.add(txtNome, gbc);

        // Endereço
        gbc.gridx = 0; gbc.gridy = 2;
        painelFormulario.add(new JLabel("Endereço:"), gbc);
        txtEndereco = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        painelFormulario.add(txtEndereco, gbc);

        // --- 3. PAINEL DE BOTÕES DE AÇÃO ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnSalvar = new JButton("Salvar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Novo / Limpar");

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnLimpar);

        // Adiciona os botões no final do formulário
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        painelFormulario.add(painelBotoes, gbc);

        // Configuração das ações dos botões
        btnSalvar.addActionListener(e -> salvarCliente());
        btnExcluir.addActionListener(e -> excluirCliente());
        btnLimpar.addActionListener(e -> limparCampos());

        // --- 4. SEPARAÇÃO DA JANELA EM DOIS LADOS (SPLIT PANE) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, painelFormulario);
        splitPane.setDividerLocation(420); // Posição inicial do divisor entre a tabela e o formulário
        splitPane.setResizeWeight(0.5);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    // Carrega/Recarrega todos os clientes do banco no Grid
    public void carregarDadosTabela() {
        modeloTabela.setRowCount(0);
        try {
            List<Cliente> lista = clienteDAO.listarTodos();
            for (Cliente c : lista) {
                Object[] linha = {
                    c.getIdCliente(),
                    c.getNome(),
                    c.getEndereco()
                };
                modeloTabela.addRow(linha);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar os clientes:\n" + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Preenche os campos de texto com os dados da linha selecionada no grid
    private void preencherCamposComLinhaSelecionada() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada != -1) {
            txtId.setText(modeloTabela.getValueAt(linhaSelecionada, 0).toString());
            txtNome.setText(modeloTabela.getValueAt(linhaSelecionada, 1).toString());
            txtEndereco.setText(modeloTabela.getValueAt(linhaSelecionada, 2).toString());
        }
    }

    // Lógica para Incluir (CREATE) ou Atualizar (UPDATE)
    private void salvarCliente() {
        String nome = txtNome.getText().trim();
        String endereco = txtEndereco.getText().trim();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Cliente cliente = new Cliente();
            cliente.setNome(nome);
            cliente.setEndereco(endereco);

            if (txtId.getText().isEmpty()) {
                // Inserir novo cliente (CREATE)
                clienteDAO.inserir(cliente);
                JOptionPane.showMessageDialog(this, "Cliente inserido com sucesso!");
            } else {
                // Atualizar cliente existente (UPDATE)
                cliente.setIdCliente(Integer.parseInt(txtId.getText()));
                clienteDAO.atualizar(cliente);
                JOptionPane.showMessageDialog(this, "Cliente atualizado com sucesso!");
            }

            limparCampos();
            carregarDadosTabela();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar cliente:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Lógica para Excluir (DELETE)
    private void excluirCliente() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(
            this,
            "Tem certeza que deseja excluir este cliente?",
            "Confirmação de Exclusão",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                clienteDAO.excluir(id);
                JOptionPane.showMessageDialog(this, "Cliente excluído com sucesso!");

                limparCampos();
                carregarDadosTabela();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir cliente:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Reseta o formulário para inclusão de um novo cliente
    private void limparCampos() {
        txtId.setText("");
        txtNome.setText("");
        txtEndereco.setText("");
        tabela.clearSelection();
    }
}