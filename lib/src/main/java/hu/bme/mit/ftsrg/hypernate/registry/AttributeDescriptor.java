package hu.bme.mit.ftsrg.hypernate.registry;

public class AttributeDescriptor {
    private PrimaryKeyDescriptor pk;
    private String attrFieldName;
    private AttributeMapperDescriptor mapper;

    public AttributeDescriptor(PrimaryKeyDescriptor pk, String attrFieldName, AttributeMapperDescriptor mapper) {
        this.pk = pk;
        this.attrFieldName = attrFieldName;
        this.mapper = mapper;
    }

    public PrimaryKeyDescriptor getPrimaryKeyDescriptor() {
        return pk;
    }

    public String getAttrFieldName() {
        return attrFieldName;
    }

    public AttributeMapperDescriptor getAttributeMapperDescriptor() {
        return mapper;
    }
}
