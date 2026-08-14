import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class JanelaProduto extends JFrame {
    private JTable tabelaP;
    private DefaultTableModel modeloTabelaP;
    private ProdutoDAO produtoDAO;

    private JTextField txtId_produto;
    private JTextField txtDescricao;
    private JTextField txtQuantidade_Estoque;

    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnLimpar;

    public JanelaProduto(Connection conexao) {
        super("Gerenciamento de Produtos (CRUD)");
        this.produtoDAO = new ProdutoDAO(conexao);

        setSize(850, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        carregarDadosTabela();
    }

    public void inicializarComponentes() {
        String[] colunas = {"ID", "Descrição", "Quantidade"};
        modeloTabelaP = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaP = new JTable(modeloTabelaP);
        tabelaP.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tabelaP);

        tabelaP.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && tabelaP.getSelectedRow() != -1) {
                    preencherCamposComLinhaSelecionada();                    
                }
            }
        });

        JPanel pnlFormulario = new JPanel(new GridBagLayout());
        pnlFormulario.setBorder(BorderFactory.createTitledBorder("Dados do Produto"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8,8,8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlFormulario.add(new JLabel("ID:"), gbc);
        txtId_produto = new JTextField(5);
        txtId_produto.setEditable(false);
        txtId_produto.setFocusable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        pnlFormulario.add(txtId_produto, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnlFormulario.add(new JLabel("Descrição:"), gbc);
        txtDescricao = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        pnlFormulario.add(txtDescricao, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnlFormulario.add(new JLabel("Quantidade:"), gbc);
        txtQuantidade_Estoque = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        pnlFormulario.add(txtQuantidade_Estoque, gbc);

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,10));
        btnSalvar = new JButton("Salvar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Novo / Limpar");

        pnlBotoes.add(btnSalvar);
        pnlBotoes.add(btnExcluir);
        pnlBotoes.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        pnlFormulario.add(pnlBotoes, gbc);

        btnSalvar.addActionListener(e -> salvarProduto());
        btnExcluir.addActionListener(e -> excluirProduto());
        btnLimpar.addActionListener(e -> limparCampos());

        // --- 4. SEPARAÇÃO DA JANELA EM DOIS LADOS (SPLIT PANE) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, pnlFormulario);
        splitPane.setDividerLocation(420); // Posição inicial do divisor entre a tabela e o formulário
        splitPane.setResizeWeight(0.5);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    public void carregarDadosTabela() {
        modeloTabelaP.setRowCount(0);
        try {
            List<Produto> lista = produtoDAO.listarTodos();
            for (Produto p : lista) {
                Object[] linha = {
                    p.getIdProduto(),
                    p.getDescricao(),
                    p.getQuantidadeEstoque()
                };
                modeloTabelaP.addRow(linha);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
              "Erro ao carregar produtos: \n" + e.getMessage(),
              "Erro",
              JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherCamposComLinhaSelecionada() {
        int linhaSelecionada = tabelaP.getSelectedRow();
        if (linhaSelecionada != -1) {
            txtId_produto.setText(modeloTabelaP.getValueAt(linhaSelecionada, 0).toString());
            txtDescricao.setText(modeloTabelaP.getValueAt(linhaSelecionada, 0).toString());
            txtQuantidade_Estoque.setText(modeloTabelaP.getValueAt(linhaSelecionada, 0).toString());            
        }
    }

    private void salvarProduto() {
        String descricao = txtDescricao.getText().trim();
        String qtdString = txtQuantidade_Estoque.getText().trim();

        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do produto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;            
        }

        try {
            int qtd = Integer.parseInt(qtdString);
            Produto produto = new Produto();
            produto.setDescricao(descricao);
            produto.setQuantidadeEstoque(qtd);

            if (txtId_produto.getText().isEmpty()) {
                produtoDAO.inserir(produto);
                JOptionPane.showMessageDialog(this, "Produto inserido com sucesso!");                
            } else {
                produto.setIdProduto(Integer.parseInt(txtId_produto.getText()));
                produtoDAO.atualizarP(produto);
                JOptionPane.showMessageDialog(this, "Produto inserido com sucesso!");
            }

            limparCampos();
            carregarDadosTabela();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar produto:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirProduto() {
        if (txtId_produto.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;            
        }

        int confirmacao = JOptionPane.showConfirmDialog(
            this,
            "Tem certeza que deseja excluir este produto?",
            "Confirmação de Exclusão",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId_produto.getText());
                produtoDAO.excluir(id);
                JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!");

                limparCampos();
                carregarDadosTabela();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir produto:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtId_produto.setText("");
        txtDescricao.setText("");
        txtQuantidade_Estoque.setText("");
        tabelaP.clearSelection();
    }    
}
