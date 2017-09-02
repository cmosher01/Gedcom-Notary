package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.mopper.ArgParser;

import java.io.IOException;
import java.util.*;

import static nu.mine.mosher.logging.Jul.log;

// Created by Christopher Alan Mosher on 2017-08-25

public class GedcomNotary implements Gedcom.Processor {
    private final GedcomNotaryOptions options;
    private final NotaryExtractor extractor;
    private GedcomTree tree;
    private final List<TreeNode<GedcomLine>> deletes = new ArrayList<>(256);

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
        deleteFlaggedNodes();
        return true;
    }

    private void select(final TreeNode<GedcomLine> node, final int level) {
        final ListIterator<TreeNode<GedcomLine>> c = node.childrenList();
        while (c.hasNext()) {
            final TreeNode<GedcomLine> cnode = c.next();
            final GedcomLine ln = cnode.getObject();
            if (this.options.ref.matches(level, cnode)) {
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
        final ArrayList<TreeNode<GedcomLine>> added;
        if (node.getObject().isPointer()) {
            added = extractFrom(node, this.tree.getNode(node.getObject().getPointer()), c);
        } else {
            added = extractFrom(node, node, c);
        }
        if (this.options.delete) {
            removeOtherSiblings(node.parent(), added);
        }
    }

    private void removeOtherSiblings(final TreeNode<GedcomLine> parent, final ArrayList<TreeNode<GedcomLine>> added) {
        final Set<String> addedTags = new HashSet<>();
        added.forEach(n -> addedTags.add(n.getObject().getTagString()));
        for (final TreeNode<GedcomLine> node : parent) {
            if (!added.contains(node)) {
                if (addedTags.contains(node.getObject().getTagString())) {
                    this.deletes.add(node);
                }
            }
        }
    }

    private ArrayList<TreeNode<GedcomLine>> extractFrom(
        final TreeNode<GedcomLine> nodeAnchor,
        final TreeNode<GedcomLine> nodeValue,
        final ListIterator<TreeNode<GedcomLine>> c) {
        final ArrayList<TreeNode<GedcomLine>> added = new ArrayList<>(4);
        final int level = nodeAnchor.getObject().getLevel();
        log().finer("checking value: " + nodeValue.getObject().getValue());
        String[] tag_rest = this.extractor.extract(nodeValue.getObject().getValue());
        while (!tag_rest[0].isEmpty()) {
            log().fine("found: " + tag_rest[0]);
            final TreeNode<GedcomLine> unwrapped;
            if (this.options.extractTo.equals(GedcomNotaryOptions.Target.CHILD)) {
                unwrapped = unwrap(tag_rest[0], level + 1);
                nodeAnchor.addChild(unwrapped);
            } else {
                unwrapped = unwrap(tag_rest[0], level);
                c.add(unwrapped);
            }
            added.add(unwrapped);
            nodeValue.setObject(nodeValue.getObject().replaceValue(tag_rest[1]));
            tag_rest = this.extractor.extract(nodeValue.getObject().getValue());
        }
        return added;
    }

    private void insert(final TreeNode<GedcomLine> node, final ListIterator<TreeNode<GedcomLine>> c) {
        if (this.options.delete) {
            // TODO log warning if line has children
            c.remove();
        }
        final TreeNode<GedcomLine> parNode = node.parent();
        final GedcomLine parLine = parNode.getObject();
        switch (this.options.insertIn) {
            case PARENT:
                parNode.setObject(parLine.replaceValue(wrap(node) + parLine.getValue()));
                break;
            case SIBLING:
                c.add(new TreeNode<>(parLine.createChild(GedcomTag.NOTE, wrap(node))));
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
            t = s.substring(0, sp);
            v = s.substring(sp + 1);
        }
        return new TreeNode<>(new GedcomLine(lev, "", t, v));
    }

    private String wrap(final TreeNode<GedcomLine> node) {
        final GedcomLine line = node.getObject();
        return this.options.mask + ": " + line.getTagString() + " " + line.getValue() + " " + this.options.mask;
    }

    private void deleteFlaggedNodes() {
        // TODO log at info level?
        this.deletes.forEach(TreeNode::removeFromParent);
    }
}
