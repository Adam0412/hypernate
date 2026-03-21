package hu.bme.mit.ftsrg.hypernate.registry;

public class AttributeMapperDescriptor {
    private AttributeDescriptor forAttr;
    private String mapperClassName;

    public AttributeMapperDescriptor(AttributeDescriptor attrdesc, String mapperName) {
        this.forAttr = attrdesc;
        this.mapperClassName = mapperName;
    }

    public AttributeDescriptor getAttributeDescriptor() {
        return forAttr;
    }

    public String getMapperName() {
        return mapperClassName;
    }

    public void setAttributeDescriptor(AttributeDescriptor forAttr) {
        this.forAttr = forAttr;
    }
}
