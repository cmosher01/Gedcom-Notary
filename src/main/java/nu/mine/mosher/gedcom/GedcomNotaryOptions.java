package nu.mine.mosher.gedcom;

@SuppressWarnings({"access", "WeakerAccess", "unused"})
public class GedcomNotaryOptions extends GedcomOptions {
//    public Expr where;
    public String mask = "__GEDCOM__";
    public enum Target { PARENT, SIBLING, CHILD };
    public Target insertIn;
    public Target extractTo = Target.SIBLING;
    public boolean delete = false;

    public void help() {
        this.help = true;
        System.err.println("Usage: java -jar gedcom-notary-all.jar [OPTIONS] <in.ged >out.ged");
        System.err.println("Hides/extracts GEDCOM tags in NOTEs.");
        System.err.println("Options:");
        System.err.println("-w, --where                     tag path to hide");
        System.err.println("-m, --mask                      mask to flank tag line with in NOTE");
        System.err.println("-i, --insertin={parent|sibling} add to parent value or as sibling");
        System.err.println("-x, --extractto={child|sibling} extract and add as child or sibling");
        System.err.println("-d, --delete                    if inserting, delete the original tag line");
        options();
    }

    public GedcomNotaryOptions verify() {
        if (this.help) {
            return this;
        }
//        if (this.where == null) {
//            throw new IllegalArgumentException("Must specify -w.");
//        }
        if (this.insertIn == null && this.extractTo == null) {
            throw new IllegalArgumentException("Must specify -h or -x.");
        }
        if (this.insertIn != null && this.extractTo != null) {
            throw new IllegalArgumentException("Can only specify one of -h or -x.");
        }
        return this;
    }
}
