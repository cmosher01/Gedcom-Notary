package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.mopper.ArgParser;

import java.io.IOException;
import java.util.ListIterator;

import static nu.mine.mosher.logging.Jul.log;

// Created by Christopher Alan Mosher on 2017-08-25

public class GedcomNotary implements Gedcom.Processor {
    private final GedcomNotaryOptions options;
    private final NotaryExtractor extractor;
    private GedcomTree tree;

    public static void main(final String... args) throws InvalidLevel, IOException {
        log();
        final GedcomNotaryOptions options = new ArgParser<>(new GedcomNotaryOptions()).parse(args).verify();
        new Gedcom(options, new GedcomNotary(options)).main();
        System.out.flush();
        System.err.flush();
    }

    private GedcomNotary(final GedcomNotaryOptions options) {
        this.options = options;
        this.extractor = new NotaryExtractor(this.options.mask);
    }

    @Override
    public boolean process(final GedcomTree tree) {
        this.tree = tree;
        select(tree.getRoot(), 0);
        return true;
    }

    private void select(final TreeNode<GedcomLine> node, final int level) {
        final ListIterator<TreeNode<GedcomLine>> c = node.childrenList();
        while (c.hasNext()) {
            final TreeNode<GedcomLine> cnode = c.next();
            final GedcomLine ln = cnode.getObject();
            if (ln.getTagString().toLowerCase().equals(this.options.ref.get(level))) {
                if (this.options.ref.at(level)) {
                    log().finer("matched: " + ln);
                    where(cnode, c);
                } else {
                    log().finest("checking within: " + ln);
                    select(cnode, level + 1);
                }
            }
        }
    }


    private void where(final TreeNode<GedcomLine> node, final ListIterator<TreeNode<GedcomLine>> c) {
        if (this.options.insertIn != null) {
            insert(node, c);
        } else {
            extract(node, c);
        }
    }

    private void extract(final TreeNode<GedcomLine> node, final ListIterator<TreeNode<GedcomLine>> c) {
        if (node.getObject().isPointer()) {
            extractFrom(node, this.tree.getNode(node.getObject().getPointer()), c);
        } else {
            extractFrom(node, node, c);
        }
    }

    private void extractFrom(final TreeNode<GedcomLine> nodeAnchor, final TreeNode<GedcomLine> nodeValue, final ListIterator<TreeNode<GedcomLine>> c) {
        final int level = nodeAnchor.getObject().getLevel();
        String[] tag_rest = this.extractor.extract(nodeValue.getObject().getValue());
        while (!tag_rest[0].isEmpty()) {
            if (this.options.extractTo.equals(GedcomNotaryOptions.Target.CHILD)) {
                nodeAnchor.addChild(unwrap(tag_rest[0], level + 1));
            } else {
                c.add(unwrap(tag_rest[0], level));
            }
            nodeValue.setObject(nodeValue.getObject().replaceValue(tag_rest[1]));
            tag_rest = this.extractor.extract(nodeValue.getObject().getValue());
        }
    }

    private void insert(final TreeNode<GedcomLine> node, final ListIterator<TreeNode<GedcomLine>> c) {
        if (this.options.delete) {
            // TODO log warning if line has children
            c.remove();
        }
        final TreeNode<GedcomLine> parNode = node.parent();
        final GedcomLine parLine = parNode.getObject();
        switch(this.options.insertIn) {
            case PARENT:
                parNode.setObject(parLine.replaceValue(wrap(node)+parLine.getValue()));
            break;
            case SIBLING:
                c.add(new TreeNode<>(parLine.createChild(GedcomTag.NOTE,wrap(node))));
            break;
            default:
                throw new IllegalStateException();
        }
    }

    private TreeNode<GedcomLine> unwrap(final String s, final int lev) {
        final String t, v;
        final int sp = s.indexOf(' ');
        if (sp < 0) {
            t = s;
            v = "";
        } else {
            t = s.substring(0,sp);
            v = s.substring(sp+1);
        }
        return new TreeNode<>(new GedcomLine(lev, "", t, v));
    }

    private String wrap(final TreeNode<GedcomLine> node) {
        final GedcomLine line = node.getObject();
        return this.options.mask+": "+line.getTagString()+" "+line.getValue()+" "+this.options.mask;
    }
}
