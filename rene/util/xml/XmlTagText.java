package rene.util.xml;

public class XmlTagText extends XmlTag {
    String Content;

    public XmlTagText(String s) {
        super("#PCDATA");
        this.Content = s;
    }

    public String getContent() {
        return this.Content;
    }
}
