package kfsteel.expedicao;

public class ItemPedido {
    private int id;
    private String codigoBarras;
    private String descricao;
    private String tipo;
    private String status;

    public ItemPedido(int id, String codigoBarras, String descricao, String tipo, String status) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.descricao = descricao;
        this.tipo = tipo;
        this.status = status;
    }

    public int getId() { return id; }
    public String getCodigoBarras() { return codigoBarras; }
    public String getDescricao() { return descricao; }
    public String getTipo() { return tipo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}