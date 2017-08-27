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
        for (final TreeNode<GedcomLine> rec : tree.getRoot()) {
            final GedcomLine gedcomLine = rec.getObject();
            if (gedcomLine.getTag().equals(GedcomTag.OBJE)) {
                final ListIterator<TreeNode<GedcomLine>> c = rec.childrenList();
                while (c.hasNext()) {
                    final TreeNode<GedcomLine> node = c.next();
                    if (node.getObject().getTag().equals(GedcomTag.NOTE)) {
                        where(node, c);
                    }
                }
            }
        }
        return true;
    }


    private void where(final TreeNode<GedcomLine> node, final ListIterator<TreeNode<GedcomLine>> c) {
        if (this.options.insertIn != null) {
            final TreeNode<GedcomLine> parNode = node.parent();
            final GedcomLine parLine = parNode.getObject();
            switch(this.options.insertIn) {
                case PARENT:
                    parNode.setObject(parLine.replaceValue(wrap(node)+parLine.getValue()));
                break;
                case SIBLING:
                    c.add(new TreeNode<>(GedcomLine.create(parLine.getLevel()+1,GedcomTag.NOTE,wrap(node))));
                break;
                default:
                    throw new IllegalStateException();
            }
            if (this.options.delete) {
                c.remove();
            }
        } else if (this.options.extractTo != null) {
            switch(this.options.extractTo) {
                case CHILD:
                    throw new IllegalStateException();
//                break;
                case SIBLING:
                    GedcomLine noteLine = node.getObject();
                    String[] tag_rest = this.extractor.extract(noteLine.getValue());
                    while (!tag_rest[0].isEmpty()) {
                        c.add(unwrap(tag_rest[0], noteLine.getLevel()));
                        noteLine = noteLine.replaceValue(tag_rest[1]);
                        node.setObject(noteLine);
                        tag_rest = this.extractor.extract(noteLine.getValue());
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        } else {
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


    //    private static void sort(final TreeNode<GedcomLine> root, final Loader loader) {
//        root.sort((node1, node2) -> compareTopLevelRecords(node1, node2, loader));
//    }
//
//    private static int compareTopLevelRecords(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Loader loader) {
//        int c = 0;
//        if (c == 0) {
//            c = compareTags(node1, node2, mapTopLevelOrder);
//        }
//        if (c == 0) {
//            final GedcomTag tag = node1.getObject().getTag();
//            if (tag.equals(GedcomTag.INDI)) {
//                c = compareIndi(node1, node2, loader);
//            } else if (tag.equals(GedcomTag.SOUR)) {
//                c = compareSour(node1, node2, loader);
//            } else if (tag.equals(GedcomTag.FAM)) {
//                c = compareFam(node1, node2, loader);
//            } else if (tag.equals(GedcomTag.NOTE)) {
//                c = compareNote(node1, node2, loader);
//            } else if (tag.equals(GedcomTag.OBJE)) {
//                c = getTitlFromObje(node1).compareToIgnoreCase(getTitlFromObje(node2));
//            }
//        }
//        return c;
//    }
//
//    private static int compareIndi(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Loader loader) {
//        int c = 0;
//        if (c == 0) {
//            c = loader.lookUpPerson(node1).getNameSortable().compareToIgnoreCase(loader.lookUpPerson(node2).getNameSortable());
//        }
//        if (c == 0) {
//            c = loader.lookUpPerson(node1).getBirth().compareTo(loader.lookUpPerson(node2).getBirth());
//        }
//        if (c == 0) {
//            c = loader.lookUpPerson(node1).getID().compareTo(loader.lookUpPerson(node2).getID());
//        }
//        return c;
//    }
//
//    private static int compareFam(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Loader loader) {
//        // TODO fix ordering of families
//        return node1.getObject().getID().compareTo(node2.getObject().getID());
//    }
//
//    private static int compareSour(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Loader loader) {
//        int c = 0;
//        if (c == 0) {
//            c = loader.lookUpSource(node1).getTitle().compareToIgnoreCase(loader.lookUpSource(node2).getTitle());
//        }
//        if (c == 0) {
//            c = loader.lookUpSource(node1).getAuthor().compareToIgnoreCase(loader.lookUpSource(node2).getAuthor());
//        }
//        return c;
//    }
//
//    private static int compareNote(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Loader loader) {
//        return node1.getObject().getValue().compareTo(node2.getObject().getValue());
//    }
//
//    private static void indisForFamily(final TreeNode<GedcomLine> fam, final Loader loader) {
//        String h = "";
//        String w = "";
//        String c1 = "";
//        String c2 = "";
//        for (final TreeNode<GedcomLine> m : fam) {
//            final GedcomTag t = m.getObject().getTag();
//            if (t.equals(GedcomTag.HUSB)) {
//                h = m.getObject().getPointer();
//            } else if (t.equals(GedcomTag.WIFE)) {
//                w = m.getObject().getPointer();
//            } else if (t.equals(GedcomTag.CHIL)) {
//                if (c1.isEmpty()) {
//                    c1 = m.getObject().getPointer();
//                } else if (c2.isEmpty()) {
//                    c2 = m.getObject().getPointer();
//                }
//            }
//        }
//        String id1 = "";
//        String id2 = "";
//        if (!h.isEmpty() && !w.isEmpty()) {
//            id1 = h;
//            id2 = w;
//        } else if (!h.isEmpty() && !c1.isEmpty()) {
//            id1 = h;
//            id2 = c1;
//        } else if (!w.isEmpty() && !c1.isEmpty()) {
//            id1 = w;
//            id2 = c1;
//        } else if (!c1.isEmpty() && !c2.isEmpty()) {
//            id1 = c1;
//            id2 = c2;
//        } else {
//            log().warning("FAM with less than two individuals.");
//        }
//        TreeNode<GedcomLine> i1 = loader.getGedcom().getNode(id1);
//        TreeNode<GedcomLine> i2 = loader.getGedcom().getNode(id2);
//        // TODO put these into a strucutre, and use it to sort FAMs by (calling compareIndi for each one)
//    }
//
//
//
//    private static void deepSort(final TreeNode<GedcomLine> node, final Loader loader) {
//        node.forEach(c -> deepSort(c, loader));
//
//        if (node.getChildCount() > 0 && node.getObject() != null) {
//            final GedcomTag tag = node.getObject().getTag();
//            if (tag.equals(GedcomTag.INDI)) {
//                node.sort((node1, node2) -> compareIndis(node1, node2, loader));
//            } else if (tag.equals(GedcomTag.HEAD) && node.getObject().getLevel() == 0) {
//                node.sort((node1, node2) -> compareTags(node1, node2, mapHeadOrder));
//            } else if (tag.equals(GedcomTag.SOUR) && node.getObject().getLevel() == 0) {
//                node.sort((node1, node2) -> compareTags(node1, node2, mapSourOrder));
//            } else if (tag.equals(GedcomTag.SOUR) && node.getObject().getLevel() > 0) {
//                node.sort((node1, node2) -> compareTags(node1, node2, mapCitationOrder));
//            } else if (tag.equals(GedcomTag.FAM)) {
//                node.sort((node1, node2) -> compareFams(node1, node2, loader));
//            } else if (GedcomTag.setIndividualEvent.contains(tag) ||
//                GedcomTag.setIndividualAttribute.contains(tag) ||
//                GedcomTag.setFamilyEvent.contains(tag)) {
//                node.sort((node1, node2) -> compareTags(node1, node2, mapEventOrder));
//            }
//        }
//    }
//
//    private static int compareIndis(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Loader loader) {
//        int c = 0;
//        final Event event1 = loader.lookUpEvent(node1);
//        final Event event2 = loader.lookUpEvent(node2);
//        if (event1 == null && event2 == null) {
//            c = compareTags(node1, node2, mapIndiOrder);
//        } else if (event1 == null) {
//            c = -1;
//            // TODO heuristic event ordering, such as BIRT < CHR, DEAT < PROB, DEAT < BURI, BIRT < other < DEAT
//        } else if (event2 == null) {
//            c = +1;
//            // TODO heuristic event ordering, such as BIRT < CHR, DEAT < PROB, DEAT < BURI, BIRT < other < DEAT
//        } else {
//            final DatePeriod d1 = event1.getDate();
//            final DatePeriod d2 = event2.getDate();
//            if (d1 == null && d2 == null) {
//                c = 0;
//            } else if (d2 == null) {
//                c = -1;
//            } else if (d1 == null) {
//                c = +1;
//            } else {
//                c = event1.getDate().compareTo(event2.getDate());
//            }
//            if (c == 0) {
//                // TODO heuristic event ordering, such as BIRT < CHR, DEAT < PROB, DEAT < BURI, BIRT < other < DEAT
//            }
//        }
//        return c;
//    }
//
//    private static int compareFams(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Loader loader) {
//        int c = 0;
//        final Event event1 = loader.lookUpEvent(node1);
//        final Event event2 = loader.lookUpEvent(node2);
//        if (event1 == null && event2 == null) {
//            c = compareTags(node1, node2, mapFamOrder);
//            if (c == 0) {
//                final GedcomLine line1 = node1.getObject();
//                final GedcomLine line2 = node2.getObject();
//                if (line1.getTag().equals(GedcomTag.CHIL)) {
//                    final TreeNode<GedcomLine> indi1 = loader.getGedcom().getNode(line1.getPointer());
//                    final Person person1 = loader.lookUpPerson(indi1);
//                    final TreeNode<GedcomLine> indi2 = loader.getGedcom().getNode(line2.getPointer());
//                    final Person person2 = loader.lookUpPerson(indi2);
//                    c = person1.getBirth().compareTo(person2.getBirth());
//                }
//                if (c == 0) {
//                    final String v1 = line1.isPointer() ? line1.getPointer() : line1.getValue();
//                    final String v2 = line2.isPointer() ? line2.getPointer() : line2.getValue();
//                    c = v1.compareTo(v2);
//                }
//            }
//        } else if (event1 == null) {
//            c = -1;
//        } else if (event2 == null) {
//            c = +1;
//        } else {
//            final DatePeriod d1 = event1.getDate();
//            final DatePeriod d2 = event2.getDate();
//            if (d1 == null && d2 == null) {
//                c = 0;
//            } else if (d2 == null) {
//                c = -1;
//            } else if (d1 == null) {
//                c = +1;
//            } else {
//                c = event1.getDate().compareTo(event2.getDate());
//            }
//        }
//        return c;
//    }
//
//    private static int compareTags(final TreeNode<GedcomLine> node1, final TreeNode<GedcomLine> node2, final Map<GedcomTag, Integer> mapOrder) {
//        final Integer o1 = mapOrder.get(node1.getObject().getTag());
//        final Integer o2 = mapOrder.get(node2.getObject().getTag());
//
//        if (o1 == null && o2 == null) {
//            return 0;
//        }
//        if (o1 == null) {
//            return -1;
//        }
//        if (o2 == null) {
//            return +1;
//        }
//        if (o1 < o2) {
//            return -1;
//        }
//        if (o2 < o1) {
//            return +1;
//        }
//        return 0;
//    }
//
//
//
//    private static String getTitlFromObje(final TreeNode<GedcomLine> node) {
//        String titl = findChild(node, GedcomTag.TITL);
//        for (final TreeNode<GedcomLine> c : node) {
//            final String t = findChild(c, GedcomTag.TITL);
//            if (!t.isEmpty()) {
//                titl = t;
//            }
//        }
//        return titl;
//    }

    private static String findChild(final TreeNode<GedcomLine> item, final GedcomTag tag) {
        return findChild(item, tag.toString());
    }

    private static String findChild(final TreeNode<GedcomLine> item, final String tag) {
        for (final TreeNode<GedcomLine> c : item) {
            final GedcomLine gedcomLine = c.getObject();
            if (gedcomLine.getTagString().equals(tag)) {
                return gedcomLine.isPointer() ? gedcomLine.getPointer() : gedcomLine.getValue();
            }
        }
        return "";
    }
}
