package legacy;

public class Speleologist implements Processable{
    private static final long serialVersionUID = 1L;
    public static final String NAME = "SP";

    @Override
    public String process(String msg) {
        if (msg.contains(KB.OK) || msg.contains(KB.END)) {
            return KB.REQUEST;
        } else if (msg.contains(KB.NONE)) {
            System.out.println(EnvAgent.NAME + ": " + msg);
            String result = KB.predicateToFeel(msg);
            System.out.println(NAME + ": " + result);
            return result;
        } else if (msg.length() > 0) {
            System.out.println(Navigator.NAME + ": " + msg);
            return KB.wrapAction(msg);
        }
        return "";
    }
}