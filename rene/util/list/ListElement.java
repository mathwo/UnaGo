package rene.util.list;

/**
 * The nodes of a list.
 *
 * @see rene.list.ListClass
 */

public class ListElement
// A list node with pointers to previous and next element
// and with a content of type Object.
{
    ListElement Next, Previous; // the chain pointers
    Object Content; // the content of the node
    ListClass L; // Belongs to this list

    public ListElement(Object content)
    // get a new Element with the content and null pointers
    {
        this.Content = content;
        this.Next = this.Previous = null;
        this.L = null;
    }

    // access methods:
    public Object content() {
        return this.Content;
    }

    // modifying methods:
    public void content(Object o) {
        this.Content = o;
    }

    public ListClass list() {
        return this.L;
    }

    public void list(ListClass l) {
        this.L = l;
    }

    public ListElement next() {
        return this.Next;
    }

    public void next(ListElement o) {
        this.Next = o;
    }

    public ListElement previous() {
        return this.Previous;
    }

    public void previous(ListElement o) {
        this.Previous = o;
    }
}
