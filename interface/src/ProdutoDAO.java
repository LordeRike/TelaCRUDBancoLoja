import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
    private Connection conexao;

    // Construtor que recebe a conexão por parâmetro
    public ProdutoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    // CREATE - Inserir Produto
    public void inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produtos (descricao, quantidade_estoque) VALUES (?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, produto.getDescricao());
            stmt.setInt(2, produto.getQuantidadeEstoque());
            stmt.executeUpdate();
        }
    }

    // READ - Listar todos os produtoss
    public List<Produto> listarTodos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos";
        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto p = new Produto();
                p.setIdProduto(rs.getInt("id_produto"));
                p.setDescricao(rs.getString("descricao"));
                p.setQuantidadeEstoque(rs.getInt("quantidade_estoque"));
                produtos.add(p);
            }
        }
        return produtos;
    }

    // UPDATE - Atualizar dados do produtos
    public void atualizarP(Produto produto) throws SQLException {
        String sql = "UPDATE produtos SET descricao = ?, quantidade_estoque = ? WHERE id_produto = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, produto.getDescricao());
            stmt.setInt(2, produto.getQuantidadeEstoque());
            stmt.setInt(3, produto.getIdProduto());
            stmt.executeUpdate();
        }
    }

    // DELETE - Remover produtos por ID
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM produtos WHERE id_produto = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}

