package kfsteel.expedicao;

public class Projeto {
    private final int id;
    private final String numeroPedido;
    private final String corEtiqueta;
    private final int totalItens;
    private final int separados;

    public Projeto(int id, String numeroPedido, String corEtiqueta, int totalItens, int separados) {
        this.id = id;
        this.numeroPedido = numeroPedido;
        this.corEtiqueta = corEtiqueta;
        this.totalItens = totalItens;
        this.separados = separados;
    }

    public int getId() { return id; }
    public String getNumeroPedido() { return numeroPedido; }
    public String getCorEtiqueta() { return corEtiqueta; }
    public int getTotalItens() { return totalItens; }
    public int getSeparados() { return separados; }

    /** Converte o nome digitado da etiqueta em uma cor hexadecimal para exibição. */
    public static String corHex(String cor) {
        String c = cor == null ? "" : cor.toLowerCase();
        if (c.contains("amarel")) return "#eab308";
        if (c.contains("azul")) return "#3b82f6";
        if (c.contains("verde")) return "#22c55e";
        if (c.contains("vermelh")) return "#ef4444";
        if (c.contains("laranja")) return "#f97316";
        if (c.contains("roxo") || c.contains("roxa")) return "#a855f7";
        if (c.contains("rosa")) return "#ec4899";
        if (c.contains("branc")) return "#f8fafc";
        if (c.contains("pret")) return "#000000";
        return "#94a3b8";
    }
}
