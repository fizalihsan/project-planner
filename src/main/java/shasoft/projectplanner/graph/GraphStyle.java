package shasoft.projectplanner.graph;

// TODO: convert to immutable usages and remove setters
public class GraphStyle {

    private String color;
    private String style;
    private Integer fontSize;
    private String shape;

    private GraphStyle(Builder builder) {
        this.color = builder.color;
        this.style = builder.style;
        this.fontSize = builder.fontSize;
        this.shape = builder.shape;
    }

    public static class Builder {
        private String color;
        private String style;
        private Integer fontSize;
        private String shape;

        public Builder color(String color) {this.color = color; return this;}
        public Builder style(String style) {this.style = style; return this;}
        public Builder fontSize(Integer fontSize) {this.fontSize = fontSize; return this;}
        public Builder shape(String shape) {this.shape = shape; return this;}

        public GraphStyle build() { return new GraphStyle(this);}
    }

    public String toDotRepresentation() {
        String dot = "";
        dot = color != null ? dot + "color=\"" + color + "\"\n" : dot;
        dot = style != null ? dot + "style=\"" + style + "\"\n" : dot;
        dot = fontSize != null ? dot + "fontSize=\"" + fontSize + "\"\n" : dot;
        dot = shape != null ? dot + "shape=\"" + shape + "\"\n" : dot;

        return dot;
    }

    public String toDotRepresentationOnSameLine() {
        String dot = "";
        dot = color != null ? dot + "color=\"" + color + "\"" : dot;
        if (style != null) {
            dot = dot.length() >= 1 && !dot.endsWith(",") ? dot + "," : dot;
            dot += "style=\"" + style + "\"";
        }
        if (fontSize != null) {
            dot = dot.length() >= 1 && !dot.endsWith(",") ? dot + "," : dot;
            dot += "fontSize=\"" + fontSize + "\"";
        }
        if (shape != null) {
            dot = dot.length() >= 1 && !dot.endsWith(",") ? dot + "," : dot;
            dot += "shape=\"" + shape + "\"";
        }

        return dot;
    }

    @Override
    public String toString() {
        return "GraphStyle{" +
                "color='" + color + '\'' +
                ", style='" + style + '\'' +
                ", fontSize=" + fontSize +
                ", shape='" + shape + '\'' +
                '}';
    }

    public String getColor() {
        return color;
    }

    public String getStyle() {
        return style;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public String getShape() {
        return shape;
    }

    // adding setters too for convenience. So strictly speaking these classes are not immutable.
    public void setColor(String color) {
        this.color = color;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
