public class Token {
    public String value;
    public String type;
    public int line;

    public Token(String value, String type, int line) {
        this.value = value;
        this.type = type;
        this.line = line;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", line=" + line +
                '}';
    }
}
