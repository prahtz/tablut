public class Coordinates {
    public int row;
    public int column;

    public Coordinates(int row, int column) {
        this.row = row;
        this.column = column;
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
        if(c.row == this.row && c.column == this.column)
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        return row*10 + column;
    }

    @Override
    public String toString() {
        return "Row: " + row + " Column: " + column;
    }
}
