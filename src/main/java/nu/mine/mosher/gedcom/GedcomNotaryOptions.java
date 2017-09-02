package nu.mine.mosher.gedcom;

@SuppressWarnings({"access", "WeakerAccess", "unused"})
public class GedcomNotaryOptions extends GedcomOptions {
    public GedcomDataRef ref;
    public String mask = "__GEDCOM__";
    public enum Target { PARENT, SIBLING, CHILD }
    public Target insertIn;
    public Target extractTo;
    public boolean delete;

    public void help() {
        this.help = true;
        System.err.println("Usage: java -jar gedcom-notary-all.jar [OPTIONS] <in.ged >out.ged");
        System.err.println("Hides/extracts GEDCOM tags in NOTEs.");
        System.err.println("Options:");
        System.err.println("-w, --where=EXPR              tag path to hide");
        System.err.println("-m, --mask=MASK               mask to flank tag line with in NOTE");
        System.err.println("-i, --insert={parent|sibling} add to parent value or as sibling");
        System.err.println("-x, --extract={child|sibling} extract and add as child or sibling");
        System.err.println("-d, --delete                  if inserting, delete the original tag line");
        System.err.println("                              if extracting, replace any existing tag line");
        options();
    }

    public void m(final String mask) {
        mask(mask);
    }

    public void mask(final String mask) {
        this.mask = mask;
    }

    public void w(final String expr) throws GedcomDataRef.InvalidSyntax {
        where(expr);
    }

    public void where(final String expr) throws GedcomDataRef.InvalidSyntax {
        this.ref = new GedcomDataRef(expr);
    }

    public void i(final String to) {
        insert(to);
    }

    public void insert(final String to) {
        final Target target = Target.valueOf(to.toUpperCase());
        if (target.equals(Target.CHILD)) {
            throw new IllegalArgumentException("Cannot insert into child.");
        }
        this.insertIn = target;
    }

    public void x(final String to) {
        extract(to);
    }

    public void extract(final String to) {
        final Target target = Target.valueOf(to.toUpperCase());
        if (target.equals(Target.PARENT)) {
            throw new IllegalArgumentException("Cannot extract as parent.");
        }
        this.extractTo = target;
    }

    public void d() {
        delete();
    }

    public void delete() {
        this.delete = true;
    }

    public GedcomNotaryOptions verify() {
        if (this.help) {
            return this;
        }
        if (this.ref == null) {
            throw new IllegalArgumentException("Must specify -w.");
        }
        if (this.insertIn == null && this.extractTo == null) {
            throw new IllegalArgumentException("Must specify -i or -x.");
        }
        if (this.insertIn != null && this.extractTo != null) {
            throw new IllegalArgumentException("Can only specify one of -i or -x.");
        }
        if (this.concToWidth == null) {
            throw new IllegalArgumentException("The -c option is required.");
        }
        return this;
    }
}
