module silosgroup.teste {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens kfsteel.expedicao to javafx.fxml;
    exports kfsteel.expedicao;
}