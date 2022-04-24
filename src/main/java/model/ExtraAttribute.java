package model;

public class ExtraAttribute{
    final AttributeType type;
    final String attrName;
    final int attrIndex;

    public ExtraAttribute(AttributeType type, String attrName, int attrIndex) {
        this.type = type;
        this.attrName = attrName;
        this.attrIndex = attrIndex;
    }
}
