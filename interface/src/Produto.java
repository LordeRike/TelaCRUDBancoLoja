public class Produto{
    private int idProduto;
    private String descricao;
    Integer quantidadeEstoque;

    // Construtores
    public Produto() {}
    public Produto(int idProduto, String descricao, int quantidadeEstoque) {
        this.idProduto = idProduto;
        this.descricao = descricao;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    // Getters e Setters
    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode estar vazia.");            
        }
        this.descricao = descricao;
    }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(Integer qtd) {
        if (qtd == null || qtd < 0) {
            throw new IllegalArgumentException("Quantidade não pode estar vazia");
            
        }
        this.quantidadeEstoque = qtd;
    }
}
