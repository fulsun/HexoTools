package pers.fulsun.cleanpicfxml.utils;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import pers.fulsun.cleanpicfxml.bean.ImageReference;

import java.util.ArrayList;
import java.util.List;

public class MarkdownVisitor extends AbstractVisitor {
    public List<ImageReference> imageRefs = new ArrayList<>();

    public MarkdownVisitor() {
    }

    @Override
    public void visit(Image image) {
        String title = image.getTitle();
        if (title == null) {
            Node altNode = image.getFirstChild();
            if (altNode instanceof Text) {
                title = ((Text) altNode).getLiteral();
            }
        }
        imageRefs.add(new ImageReference(title, image.getDestination()));
        super.visit(image);
    }
}
