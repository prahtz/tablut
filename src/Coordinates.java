public class Coordinates {
    private int row;
    private int column;

    public Coordinates(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
        return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Coordinates c = (Coordinates) obj;
        if(c.getRow() == this.row && c.getColumn() == this.column)
            return true;
        return false;
    }
}
