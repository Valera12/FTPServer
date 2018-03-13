package Model;

import javax.swing.table.AbstractTableModel;
import Controller.ServerFTP;
import Model.UsersDB;

public class UsersDB extends AbstractTableModel {
    private final String header[] = {
            "User",
            "Password"
    };

    private String data[][] = {
            {"user1","123"},
            {"anonymous", "anonymous"}
    };

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return header.length;
    }

    @Override
    public String getColumnName(int col) {
        return header[col];
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }
}
