package hu.frankdavid.springular.core.rpc;

public class Dog {
    private String type;
    private int age;
    private Color color;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public static enum Color {
        BLACK, WHITE, BLUE;
    }
}